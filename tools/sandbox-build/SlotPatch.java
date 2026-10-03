import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;
import java.nio.file.*;
import java.util.*;

/**
 * Raises the number of ability slots from 4 to 5 in the original 0.1.0 class files, in place, without recompiling
 * them. Every edit checks that it found exactly the bytecode it expects and aborts otherwise.
 * The matching source edits are in src/main/java (AbilityState, AbilityManager, AbilityBarHud, AbilityInput,
 * ModKeys, AbilityWheelScreen).
 *
 * Usage: SlotPatch <classes root containing com/curseddomain/...>
 */
public class SlotPatch {
    static Path root;

    public static void main(String[] args) throws Exception {
        root = Paths.get(args[0]);
        String base = "com/curseddomain/";
        patch(base + "technique/ability/AbilityState", cn -> {
            intArray(method(cn, "<init>", null), true);
            intArray(method(cn, "deserializeNBT", "(Lnet/minecraft/core/HolderLookup$Provider;Lnet/minecraft/nbt/CompoundTag;)V"), true);
            lengthCheck(method(cn, "deserializeNBT", "(Lnet/minecraft/core/HolderLookup$Provider;Lnet/minecraft/nbt/CompoundTag;)V"));
        });
        patch(base + "technique/ability/AbilityManager", cn -> {
            constant(method(cn, "assignSlot", null), Opcodes.ICONST_3, Opcodes.IF_ICMPGT, Opcodes.ICONST_4);
            listOf(method(cn, "sync", "(Lnet/minecraft/server/level/ServerPlayer;)V"));
        });
        patch(base + "client/hud/AbilityBarHud", cn -> {
            loop(method(cn, "render", null));
            bipush(method(cn, "lambda$render$0", null), 80, 100);
        });
        patch(base + "client/input/AbilityInput", cn -> {
            loop(method(cn, "onTick", null));
            MethodNode clinit = method(cn, "<clinit>", null);
            boolArray(clinit);
            intArray(clinit, false);
        });
        patch(base + "client/input/ModKeys", cn -> keys(method(cn, "<clinit>", null)));
        patch(base + "client/gui/AbilityWheelScreen", cn -> {
            loop(method(cn, "render", null));
            bipush(method(cn, "keyPressed", null), 52, 53);
        });
        System.out.println("slot patch applied");
    }

    interface Edit { void apply(ClassNode cn); }

    static void patch(String name, Edit edit) throws Exception {
        Path p = root.resolve(name + ".class");
        ClassNode cn = new ClassNode();
        new ClassReader(Files.readAllBytes(p)).accept(cn, 0);
        edit.apply(cn);
        ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_MAXS); // frames are unchanged: no new branches
        cn.accept(cw);
        Files.write(p, cw.toByteArray());
        System.out.println("patched " + name);
    }

    static MethodNode method(ClassNode cn, String name, String desc) {
        List<MethodNode> found = new ArrayList<>();
        for (MethodNode m : cn.methods) if (m.name.equals(name) && (desc == null || m.desc.equals(desc))) found.add(m);
        if (found.size() != 1) throw new IllegalStateException(cn.name + "." + name + ": expected 1 method, found " + found.size());
        return found.get(0);
    }

    static AbstractInsnNode next(AbstractInsnNode n) {
        n = n.getNext();
        while (n != null && n.getOpcode() < 0) n = n.getNext();
        return n;
    }

    static AbstractInsnNode prev(AbstractInsnNode n) {
        n = n.getPrevious();
        while (n != null && n.getOpcode() < 0) n = n.getPrevious();
        return n;
    }

    static void check(boolean ok, String what) {
        if (!ok) throw new IllegalStateException("pattern not found: " + what);
    }

    /** `for (i = 0; i < 4; i++)`: ILOAD, ICONST_4, IF_ICMPGE -> ICONST_5. */
    static void loop(MethodNode m) {
        int n = 0;
        for (AbstractInsnNode insn : m.instructions.toArray()) {
            if (insn.getOpcode() == Opcodes.ICONST_4 && next(insn).getOpcode() == Opcodes.IF_ICMPGE && prev(insn).getOpcode() == Opcodes.ILOAD) {
                m.instructions.set(insn, new InsnNode(Opcodes.ICONST_5));
                n++;
            }
        }
        check(n == 1, m.name + " loop bound (found " + n + ")");
    }

    /** Replaces opcode `from` (followed by `follow`) with `to`; must occur exactly once. */
    static void constant(MethodNode m, int from, int follow, int to) {
        int n = 0;
        for (AbstractInsnNode insn : m.instructions.toArray()) {
            if (insn.getOpcode() == from && next(insn).getOpcode() == follow) {
                m.instructions.set(insn, new InsnNode(to));
                n++;
            }
        }
        check(n == 1, m.name + " constant (found " + n + ")");
    }

    /** `new boolean[4]` -> `new boolean[5]`. */
    static void boolArray(MethodNode m) {
        int n = 0;
        for (AbstractInsnNode insn : m.instructions.toArray()) {
            if (insn instanceof IntInsnNode arr && arr.getOpcode() == Opcodes.NEWARRAY && arr.operand == Opcodes.T_BOOLEAN && prev(arr).getOpcode() == Opcodes.ICONST_4) {
                m.instructions.set(prev(arr), new InsnNode(Opcodes.ICONST_5));
                n++;
            }
        }
        check(n == 1, m.name + " boolean[] (found " + n + ")");
    }

    static void bipush(MethodNode m, int from, int to) {
        int n = 0;
        for (AbstractInsnNode insn : m.instructions.toArray()) {
            if (insn instanceof IntInsnNode i && i.getOpcode() == Opcodes.BIPUSH && i.operand == from) {
                i.operand = to;
                n++;
            }
        }
        check(n == 1, m.name + " bipush " + from + " (found " + n + ")");
    }

    /** `arraylength; iconst_4; if_icmpne` -> compare with 5. */
    static void lengthCheck(MethodNode m) {
        int n = 0;
        for (AbstractInsnNode insn : m.instructions.toArray()) {
            if (insn.getOpcode() == Opcodes.ICONST_4 && prev(insn).getOpcode() == Opcodes.ARRAYLENGTH && next(insn).getOpcode() == Opcodes.IF_ICMPNE) {
                m.instructions.set(insn, new InsnNode(Opcodes.ICONST_5));
                n++;
            }
        }
        check(n == 1, m.name + " length check (found " + n + ")");
    }

    static int iconst(AbstractInsnNode n) {
        int op = n.getOpcode();
        check(op >= Opcodes.ICONST_M1 && op <= Opcodes.ICONST_5, "iconst");
        return op - Opcodes.ICONST_0;
    }

    /**
     * A 4-element int[] literal: (ICONST_4 or already-patched ICONST_5) NEWARRAY T_INT, 4x (DUP, idx, value, IASTORE).
     * Grows it to 5; the new element is 4 when `sequential` ({0,1,2,3}) or a copy of the last value otherwise.
     */
    static void intArray(MethodNode m, boolean sequential) {
        int n = 0;
        for (AbstractInsnNode insn : m.instructions.toArray()) {
            if (insn instanceof IntInsnNode arr && arr.getOpcode() == Opcodes.NEWARRAY && arr.operand == Opcodes.T_INT) {
                AbstractInsnNode size = prev(arr);
                if (size.getOpcode() != Opcodes.ICONST_4 && size.getOpcode() != Opcodes.ICONST_5) continue;
                AbstractInsnNode cur = arr;
                int lastValue = 0;
                for (int i = 0; i < 4; i++) {
                    AbstractInsnNode dup = next(cur), idx = next(dup), val = next(idx), store = next(val);
                    check(dup.getOpcode() == Opcodes.DUP && iconst(idx) == i && store.getOpcode() == Opcodes.IASTORE, m.name + " int[] element " + i);
                    lastValue = iconst(val);
                    if (sequential) check(lastValue == i, m.name + " sequential int[]");
                    cur = store;
                }
                check(next(cur).getOpcode() != Opcodes.DUP, m.name + " int[] has more than 4 elements");
                if (size.getOpcode() == Opcodes.ICONST_4) m.instructions.set(size, new InsnNode(Opcodes.ICONST_5));
                InsnList add = new InsnList();
                add.add(new InsnNode(Opcodes.DUP));
                add.add(new InsnNode(Opcodes.ICONST_4));
                add.add(new InsnNode(Opcodes.ICONST_0 + (sequential ? 4 : lastValue)));
                add.add(new InsnNode(Opcodes.IASTORE));
                m.instructions.insert(cur, add);
                n++;
            }
        }
        check(n == 1, m.name + " int[] literal (found " + n + ")");
    }

    /** List.of(slots[0], ..., slots[3]) -> List.of(slots[0], ..., slots[4]). */
    static void listOf(MethodNode m) {
        String of4 = "(Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;)Ljava/util/List;";
        int n = 0;
        for (AbstractInsnNode insn : m.instructions.toArray()) {
            if (insn instanceof MethodInsnNode call && call.owner.equals("java/util/List") && call.name.equals("of") && call.desc.equals(of4)) {
                AbstractInsnNode box = prev(call), load = prev(box), idx = prev(load), field = prev(idx), obj = prev(field);
                check(box instanceof MethodInsnNode b && b.name.equals("valueOf") && load.getOpcode() == Opcodes.IALOAD
                    && idx.getOpcode() == Opcodes.ICONST_3 && field instanceof FieldInsnNode f && f.name.equals("slots")
                    && obj instanceof VarInsnNode, "List.of(slots...)");
                InsnList add = new InsnList();
                add.add(obj.clone(null));
                add.add(field.clone(null));
                add.add(new InsnNode(Opcodes.ICONST_4));
                add.add(new InsnNode(Opcodes.IALOAD));
                add.add(box.clone(null));
                m.instructions.insertBefore(call, add);
                call.desc = "(Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;)Ljava/util/List;";
                n++;
            }
        }
        check(n == 1, "List.of in sync (found " + n + ")");
    }

    /** SLOTS = {key(ability_1..4)} -> add key("ability_5", M). */
    static void keys(MethodNode m) {
        AbstractInsnNode size = null, lastStore = null;
        MethodInsnNode factory = null;
        for (AbstractInsnNode insn : m.instructions.toArray()) {
            if (insn instanceof TypeInsnNode t && t.getOpcode() == Opcodes.ANEWARRAY && t.desc.equals("net/minecraft/client/KeyMapping")) size = prev(t);
            if (insn instanceof LdcInsnNode ldc && "ability_4".equals(ldc.cst)) {
                AbstractInsnNode code = next(ldc), call = next(code), store = next(call);
                check(code instanceof IntInsnNode c && c.operand == 78 && call instanceof MethodInsnNode && store.getOpcode() == Opcodes.AASTORE, "ability_4 key");
                factory = (MethodInsnNode)call;
                lastStore = store;
            }
        }
        check(size != null && size.getOpcode() == Opcodes.ICONST_4 && lastStore != null, "ModKeys.SLOTS literal");
        check(next(lastStore).getOpcode() != Opcodes.DUP, "SLOTS already has 5 keys");
        m.instructions.set(size, new InsnNode(Opcodes.ICONST_5));
        InsnList add = new InsnList();
        add.add(new InsnNode(Opcodes.DUP));
        add.add(new InsnNode(Opcodes.ICONST_4));
        add.add(new LdcInsnNode("ability_5"));
        add.add(new IntInsnNode(Opcodes.BIPUSH, 77));
        add.add(factory.clone(null));
        add.add(new InsnNode(Opcodes.AASTORE));
        m.instructions.insert(lastStore, add);
    }
}

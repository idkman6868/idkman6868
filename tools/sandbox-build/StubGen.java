import org.objectweb.asm.*;
import org.objectweb.asm.signature.*;
import org.objectweb.asm.tree.*;
import java.io.*;
import java.nio.file.*;
import java.util.*;

/**
 * Builds compile-only stubs of every external (non-JDK) class/member referenced by a set of class files,
 * plus manual additions from a spec file. Usage: StubGen <classesDir> <specFile> <outDir>
 */
public class StubGen {
    static final String MOD = "com/curseddomain/";

    static class M { String name, desc; boolean isStatic; boolean itf; }
    static class F { String name, desc; boolean isStatic; }
    static class C {
        String name; boolean itf; boolean isEnum; String sup = null; Set<String> ifaces = new LinkedHashSet<>();
        Map<String, M> methods = new LinkedHashMap<>(); Map<String, F> fields = new LinkedHashMap<>();
        int arity = 0; String samName, samDesc; Integer innerAccess = null; boolean forceClass; String csig;
    }
    static Map<String, C> classes = new TreeMap<>();
    static Set<String> modClasses = new HashSet<>();
    static Map<String, String> msigs = new HashMap<>();
    static Map<String, String[]> throwsMap = new HashMap<>();
    static Map<String, ClassNode> modNodes = new HashMap<>();
    static Map<String, Integer> overrideAccess = new HashMap<>();
    /** For a reference whose owner is a mod class, find the external ancestor that really declares it. */
    static String resolveOwner(String owner, String name, String desc, boolean field) {
        String cur = owner;
        while (cur != null && modNodes.containsKey(cur)) {
            ClassNode cn = modNodes.get(cur);
            if (field) { for (FieldNode f : cn.fields) if (f.name.equals(name)) return null; }
            else { for (MethodNode m : cn.methods) if (m.name.equals(name) && m.desc.equals(desc)) return null; }
            cur = cn.superName;
        }
        return (cur != null && external(cur)) ? cur : null;
    }

    static boolean external(String n) {
        if (n == null) return false;
        if (n.startsWith("[")) return false;
        return !(n.startsWith(MOD) || n.startsWith("java/") || n.startsWith("javax/") || n.startsWith("jdk/") || n.startsWith("sun/"))
            && !modClasses.contains(n);
    }
    static C cls(String n) {
        C c = classes.computeIfAbsent(n, k -> { C x = new C(); x.name = k; return x; });
        // ensure outer classes exist
        int i = n.lastIndexOf('$');
        if (i > 0) cls(n.substring(0, i));
        return c;
    }
    static void type(Type t) {
        if (t.getSort() == Type.ARRAY) type(t.getElementType());
        else if (t.getSort() == Type.OBJECT && external(t.getInternalName())) cls(t.getInternalName());
        else if (t.getSort() == Type.METHOD) { type(t.getReturnType()); for (Type a : t.getArgumentTypes()) type(a); }
    }
    static void desc(String d) { if (d != null) type(Type.getType(d)); }
    static void name(String n) { if (n.startsWith("[")) type(Type.getType(n)); else if (external(n)) cls(n); }
    static void method(String owner, String name, String desc, boolean isStatic, boolean itf) {
        if (owner.startsWith("[")) return;
        if (modNodes.containsKey(owner) && !name.equals("<init>")) { String r = resolveOwner(owner, name, desc, false); if (r != null) owner = r; }
        if (!external(owner)) { desc(desc); return; }
        C c = cls(owner);
        if (itf) c.itf = true;
        M m = c.methods.computeIfAbsent(name + desc, k -> { M x = new M(); x.name = name; x.desc = desc; return x; });
        m.isStatic |= isStatic; desc(desc);
    }
    static void field(String owner, String name, String desc, boolean isStatic) {
        if (modNodes.containsKey(owner)) { String r = resolveOwner(owner, name, desc, true); if (r != null) owner = r; }
        if (!external(owner)) { desc(desc); return; }
        C c = cls(owner);
        F f = c.fields.computeIfAbsent(name, k -> { F x = new F(); x.name = name; x.desc = desc; return x; });
        f.isStatic |= isStatic; desc(desc);
    }
    static void signature(String sig) {
        if (sig == null) return;
        new SignatureReader(sig).accept(new SignatureVisitor(Opcodes.ASM9) {
            Deque<String> stack = new ArrayDeque<>(); Deque<Integer> counts = new ArrayDeque<>();
            @Override public void visitClassType(String n) { stack.push(n); counts.push(0); }
            @Override public void visitInnerClassType(String n) { finish(); String outer = stack.isEmpty() ? "" : stack.peek(); stack.push(outer + "$" + n); counts.push(0); }
            @Override public void visitTypeArgument() { counts.push(counts.pop() + 1); }
            @Override public SignatureVisitor visitTypeArgument(char w) { counts.push(counts.pop() + 1); return this; }
            void finish() { if (stack.isEmpty()) return; String n = stack.pop(); int k = counts.pop(); if (external(n)) { C c = cls(n); c.arity = Math.max(c.arity, k);} stack.push(n); counts.push(0); stack.pop(); counts.pop(); stack.push(n); counts.push(0);}
            @Override public void visitEnd() { if (stack.isEmpty()) return; String n = stack.pop(); int k = counts.pop(); if (external(n)) { C c = cls(n); c.arity = Math.max(c.arity, k);} }
        });
    }

    public static void main(String[] args) throws Exception {
        Path in = Paths.get(args[0]);
        List<ClassNode> nodes = new ArrayList<>();
        try (var s = Files.walk(in)) {
            for (Path p : (Iterable<Path>) s.filter(x -> x.toString().endsWith(".class"))::iterator) {
                ClassNode cn = new ClassNode();
                new ClassReader(Files.readAllBytes(p)).accept(cn, 0);
                nodes.add(cn); modClasses.add(cn.name); modNodes.put(cn.name, cn);
            }
        }
        for (ClassNode cn : nodes) {
            if (external(cn.superName)) cls(cn.superName);
            for (String i : cn.interfaces) if (external(i)) cls(i).itf = true;
            signature(cn.signature);
            if (cn.innerClasses != null) for (InnerClassNode ic : cn.innerClasses) {
                if (external(ic.name)) {
                    C c = cls(ic.name); c.innerAccess = ic.access;
                    if ((ic.access & Opcodes.ACC_INTERFACE) != 0) c.itf = true;
                    if ((ic.access & Opcodes.ACC_ENUM) != 0) c.isEnum = true;
                }
            }
            for (FieldNode f : cn.fields) { desc(f.desc); signature(f.signature); }
            for (MethodNode m : cn.methods) {
                desc(m.desc); signature(m.signature);
                if (m.localVariables != null) for (LocalVariableNode l : m.localVariables) { desc(l.desc); signature(l.signature); }
                if (m.tryCatchBlocks != null) for (TryCatchBlockNode t : m.tryCatchBlocks) if (t.type != null) name(t.type);
                for (AbstractInsnNode insn : m.instructions) {
                    if (insn instanceof MethodInsnNode mi) method(mi.owner, mi.name, mi.desc, mi.getOpcode() == Opcodes.INVOKESTATIC, mi.itf);
                    else if (insn instanceof FieldInsnNode fi) field(fi.owner, fi.name, fi.desc, fi.getOpcode() == Opcodes.GETSTATIC || fi.getOpcode() == Opcodes.PUTSTATIC);
                    else if (insn instanceof TypeInsnNode ti) name(ti.desc);
                    else if (insn instanceof MultiANewArrayInsnNode ma) desc(ma.desc);
                    else if (insn instanceof LdcInsnNode ldc && ldc.cst instanceof Type t) type(t);
                    else if (insn instanceof InvokeDynamicInsnNode indy) {
                        desc(indy.desc);
                        if (indy.bsm.getOwner().equals("java/lang/invoke/LambdaMetafactory")) {
                            String iface = Type.getReturnType(indy.desc).getInternalName();
                            Type sam = (Type) indy.bsmArgs[0];
                            if (external(iface)) { C c = cls(iface); c.itf = true; c.samName = indy.name; c.samDesc = sam.getDescriptor(); }
                            desc(sam.getDescriptor());
                            Handle h = (Handle) indy.bsmArgs[1];
                            method(h.getOwner(), h.getName(), h.getDesc(), h.getTag() == Opcodes.H_INVOKESTATIC, h.isInterface());
                        }
                    }
                }
            }
        }
        for (ClassNode cn : nodes) {
            String anc = cn.superName; while (anc != null && modNodes.containsKey(anc)) anc = modNodes.get(anc).superName;
            if (anc == null || !external(anc)) continue;
            for (MethodNode m : cn.methods) {
                if ((m.access & (Opcodes.ACC_PRIVATE | Opcodes.ACC_STATIC)) != 0 || m.name.startsWith("<") || (m.access & Opcodes.ACC_SYNTHETIC) != 0) continue;
                if ((m.access & Opcodes.ACC_PROTECTED) != 0) overrideAccess.put(anc + "." + m.name + m.desc, Opcodes.ACC_PROTECTED);
            }
        }
        // manual spec
        if (args.length > 2) {
            for (String line : Files.readAllLines(Paths.get(args[1]))) {
                line = line.trim(); if (line.isEmpty() || line.startsWith("#")) continue;
                String[] p = line.split("\\s+");
                switch (p[0]) {
                    case "class" -> { C c = cls(p[1]); for (int i = 2; i < p.length; i++) {
                        if (p[i].equals("super")) c.sup = p[++i];
                        else if (p[i].equals("implements")) c.ifaces.add(p[++i]);
                        else if (p[i].equals("interface")) c.itf = true;
                        else if (p[i].equals("class")) c.forceClass = true;
                        else if (p[i].equals("enum")) c.isEnum = true;
                        else if (p[i].startsWith("arity=")) c.arity = Integer.parseInt(p[i].substring(6));
                    } }
                    case "method", "static" -> { method(p[1], p[2], p[3], p[0].equals("static"), false); }
                    case "imethod", "istatic" -> { method(p[1], p[2], p[3], p[0].equals("istatic"), true); }
                    case "field", "sfield" -> field(p[1], p[2], p[3], p[0].equals("sfield"));
                    case "csig" -> cls(p[1]).csig = p[2];
                    case "throws" -> throwsMap.put(p[1] + "." + p[2] + p[3], Arrays.copyOfRange(p, 4, p.length));
                    case "msig" -> { msigs.put(p[1] + "." + p[2] + p[3], p[4]); }
                    case "sam" -> { C c = cls(p[1]); c.itf = true; c.samName = p[2]; c.samDesc = p[3]; desc(p[3]); }
                    default -> throw new IllegalArgumentException(line);
                }
            }
        }
        Path out = Paths.get(args[args.length - 1]);
        for (C c : classes.values()) {
            if (c.forceClass) c.itf = false;
            ClassWriter cw = new ClassWriter(0);
            int acc = Opcodes.ACC_PUBLIC | (c.itf ? Opcodes.ACC_INTERFACE | Opcodes.ACC_ABSTRACT : 0) | (c.isEnum ? Opcodes.ACC_ENUM : 0);
            String sup = c.itf ? "java/lang/Object" : (c.sup != null ? c.sup : (c.isEnum ? "java/lang/Enum" : "java/lang/Object"));
            String sig = null;
            if (c.arity > 0) {
                StringBuilder sb = new StringBuilder("<");
                for (int i = 0; i < c.arity; i++) sb.append("T").append(i).append(":Ljava/lang/Object;");
                sb.append(">L").append(sup).append(";");
                for (String i : c.ifaces) sb.append("L").append(i).append(";");
                sig = sb.toString();
            }
            if (c.isEnum && !c.itf && c.sup == null) {
                sig = (c.arity > 0 ? sig : "Ljava/lang/Enum<L" + c.name + ";>;" + String.join("", c.ifaces.stream().map(i -> "L" + i + ";").toList()));
            }
            if (c.csig != null) sig = c.csig;
            cw.visit(Opcodes.V21, acc, c.name, sig, sup, c.ifaces.toArray(new String[0]));
            // inner class attributes: own + direct children
            int d = c.name.lastIndexOf('$');
            if (d > 0) cw.visitInnerClass(c.name, c.name.substring(0, d), c.name.substring(d + 1), innerAcc(c));
            for (C o : classes.values()) {
                int od = o.name.lastIndexOf('$');
                if (od > 0 && o.name.substring(0, od).equals(c.name)) cw.visitInnerClass(o.name, c.name, o.name.substring(od + 1), innerAcc(o));
            }
            for (F f : c.fields.values()) {
                cw.visitField(Opcodes.ACC_PUBLIC | (f.isStatic ? Opcodes.ACC_STATIC : 0) | (c.itf ? Opcodes.ACC_STATIC | Opcodes.ACC_FINAL : 0), f.name, f.desc, null, null).visitEnd();
            }
            boolean hasCtor = false;
            for (M m : c.methods.values()) {
                if (m.name.equals("<clinit>")) continue;
                boolean ctor = m.name.equals("<init>");
                if (ctor && c.itf) continue;
                hasCtor |= ctor;
                boolean abs = c.itf && !m.isStatic && c.samName != null && m.name.equals(c.samName) && m.desc.equals(c.samDesc);
                int vis = Opcodes.ACC_PUBLIC;
                if (!c.itf) for (String k = c.name; k != null; k = classes.containsKey(k) ? classes.get(k).sup : null) {
                    // protected if any mod override of this member (declared on this class or a stub subclass chain) is protected
                    if (overrideAccess.containsKey(k + "." + m.name + m.desc)) { vis = Opcodes.ACC_PROTECTED; break; }
                }
                for (Map.Entry<String,Integer> e : overrideAccess.entrySet()) if (e.getKey().endsWith("." + m.name + m.desc) && isSub(e.getKey().substring(0, e.getKey().lastIndexOf('.', e.getKey().indexOf('(') )), c.name)) vis = Opcodes.ACC_PROTECTED;
                int ma = vis | (m.isStatic ? Opcodes.ACC_STATIC : 0) | (abs ? Opcodes.ACC_ABSTRACT : 0);
                Type[] at = Type.getArgumentTypes(m.desc);
                if (at.length > 0 && at[at.length - 1].getSort() == Type.ARRAY) ma |= Opcodes.ACC_VARARGS;
                MethodVisitor mv = cw.visitMethod(ma, m.name, m.desc, msigs.get(c.name + "." + m.name + m.desc), throwsMap.get(c.name + "." + m.name + m.desc));
                if (!abs) { mv.visitCode(); mv.visitInsn(Opcodes.ACONST_NULL); mv.visitInsn(Opcodes.ATHROW); mv.visitMaxs(1, 1 + at.length * 2 + 1); }
                mv.visitEnd();
            }
            if (c.itf && c.samName != null && !c.methods.containsKey(c.samName + c.samDesc)) {
                cw.visitMethod(Opcodes.ACC_PUBLIC | Opcodes.ACC_ABSTRACT, c.samName, c.samDesc, null, throwsMap.get(c.name + "." + c.samName + c.samDesc)).visitEnd();
            }
            if (!c.itf && !hasCtor) {
                MethodVisitor mv = cw.visitMethod(Opcodes.ACC_PROTECTED, "<init>", "()V", null, null);
                mv.visitCode(); mv.visitInsn(Opcodes.ACONST_NULL); mv.visitInsn(Opcodes.ATHROW); mv.visitMaxs(1, 1); mv.visitEnd();
            }
            cw.visitEnd();
            Path f = out.resolve(c.name + ".class");
            Files.createDirectories(f.getParent());
            Files.write(f, cw.toByteArray());
        }
        // report
        try (PrintWriter pw = new PrintWriter(Files.newBufferedWriter(out.resolve("stub-report.txt")))) {
            for (C c : classes.values()) {
                pw.println((c.itf ? "interface " : c.isEnum ? "enum " : "class ") + c.name + (c.sup != null ? " extends " + c.sup : "") + (c.arity > 0 ? " arity=" + c.arity : "") + (c.samName != null ? " sam=" + c.samName + c.samDesc : ""));
                for (F f : c.fields.values()) pw.println("   " + (f.isStatic ? "static " : "") + "field " + f.name + " " + f.desc);
                for (M m : c.methods.values()) pw.println("   " + (m.isStatic ? "static " : "") + m.name + m.desc);
            }
        }
        System.out.println("stubbed " + classes.size() + " classes");
    }
    static boolean isSub(String sub, String sup) {
        for (String k = sub; k != null; k = classes.containsKey(k) ? classes.get(k).sup : null) if (k.equals(sup)) return true;
        return false;
    }
    static int innerAcc(C c) {
        int a = c.innerAccess != null ? c.innerAccess : (Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC);
        a |= Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC; a &= ~(Opcodes.ACC_PRIVATE | Opcodes.ACC_PROTECTED | Opcodes.ACC_FINAL | Opcodes.ACC_ABSTRACT);
        if (c.itf) a |= Opcodes.ACC_INTERFACE | Opcodes.ACC_ABSTRACT; else a &= ~(Opcodes.ACC_INTERFACE);
        if (c.isEnum) a |= Opcodes.ACC_ENUM;
        return a;
    }
}

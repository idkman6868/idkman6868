import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;
import java.nio.file.*;
import java.util.*;

/**
 * Removes a stub method from a class when an ancestor stub declares the same method (same name and descriptor) with a
 * generic signature and the subclass copy has none. StubGen copies a method onto whichever class the bytecode named
 * it on, and those raw copies hide the ancestor's generic types (ConfigValue.get(): T becomes Object). Removing them
 * is safe: the descriptor javac emits is the same either way. Usage: StubPrune <stubsDir>
 */
public class StubPrune {
    public static void main(String[] args) throws Exception {
        Path root = Paths.get(args[0]);
        Map<String, ClassNode> nodes = new HashMap<>();
        Map<String, Path> files = new HashMap<>();
        try (var walk = Files.walk(root)) {
            for (Path p : walk.filter(f -> f.toString().endsWith(".class")).toList()) {
                ClassNode cn = new ClassNode();
                new ClassReader(Files.readAllBytes(p)).accept(cn, 0);
                nodes.put(cn.name, cn);
                files.put(cn.name, p);
            }
        }
        int removed = 0;
        for (ClassNode cn : nodes.values()) {
            boolean changed = false;
            for (Iterator<MethodNode> it = cn.methods.iterator(); it.hasNext(); ) {
                MethodNode m = it.next();
                if (m.name.startsWith("<") || m.signature != null || (m.access & Opcodes.ACC_STATIC) != 0) continue;
                if (genericInAncestor(nodes, cn, m, new HashSet<>())) {
                    it.remove();
                    changed = true;
                    removed++;
                }
            }
            if (changed) {
                ClassWriter cw = new ClassWriter(0);
                cn.accept(cw);
                Files.write(files.get(cn.name), cw.toByteArray());
            }
        }
        System.out.println("pruned " + removed + " raw stub methods");
    }

    static boolean genericInAncestor(Map<String, ClassNode> nodes, ClassNode cn, MethodNode m, Set<String> seen) {
        List<String> parents = new ArrayList<>();
        if (cn.superName != null) parents.add(cn.superName);
        parents.addAll(cn.interfaces);
        for (String p : parents) {
            ClassNode pn = nodes.get(p);
            if (pn == null || !seen.add(p)) continue;
            for (MethodNode pm : pn.methods) {
                if (pm.name.equals(m.name) && pm.desc.equals(m.desc) && pm.signature != null) return true;
            }
            if (genericInAncestor(nodes, pn, m, seen)) return true;
        }
        return false;
    }
}

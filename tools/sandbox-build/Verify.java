import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;
import org.objectweb.asm.tree.analysis.*;
import java.nio.file.*;
/** Structural bytecode check (stack heights, operand kinds, locals) for every method; needs no other classes. */
public class Verify {
    public static void main(String[] a) throws Exception {
        int bad = 0, methods = 0;
        for (String f : a) {
            ClassNode cn = new ClassNode();
            new ClassReader(Files.readAllBytes(Paths.get(f))).accept(cn, 0);
            for (MethodNode m : cn.methods) {
                if ((m.access & Opcodes.ACC_ABSTRACT) != 0) continue;
                methods++;
                try { new Analyzer<>(new BasicVerifier()).analyze(cn.name, m); }
                catch (AnalyzerException e) { bad++; System.out.println(cn.name + "." + m.name + m.desc + ": " + e.getMessage()); }
            }
        }
        System.out.println(a.length + " classes, " + methods + " methods checked, " + bad + " failures");
    }
}

// Prints CullingLang entries so they can be merged into en_us.json without running datagen.
import net.neoforged.neoforge.common.data.LanguageProvider;
public class Dump {
    public static void main(String[] a) throws Exception {
        LanguageProvider p = new LanguageProvider();
        com.curseddomain.datagen.lang.CullingLang.addTo(p);
        StringBuilder sb = new StringBuilder();
        for (var e : p.entries.entrySet()) sb.append(e.getKey()).append('\u0000').append(e.getValue()).append('\u0001');
        System.out.print(sb);
    }
}

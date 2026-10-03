package net.neoforged.neoforge.common.data;
import java.util.LinkedHashMap;
import java.util.Map;
public class LanguageProvider {
    public final Map<String, String> entries = new LinkedHashMap<>();
    public void add(String key, String value) {
        if (entries.put(key, value) != null) throw new IllegalStateException("Duplicate translation key " + key);
    }
}

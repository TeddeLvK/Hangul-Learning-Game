package com.koreangame;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class Glossary {
    private static final String[] PARTICLES = {
        "에서는", "에서도", "에게는", "한테는", "까지는", "부터는", "보다는",
        "에서", "으로", "께서", "한테", "에게", "부터", "까지", "보다", "처럼", "같이", "이랑",
        "이에요", "예요", "이야", "야",
        "은", "는", "이", "가", "을", "를", "에", "도", "의", "만", "와", "과", "랑", "께", "로"
    };

    private final Map<String, String> entries = new HashMap<>();

    public void load(Path path) throws IOException {
        BufferedReader br;
        if (Files.exists(path)) {
            br = Files.newBufferedReader(path, StandardCharsets.UTF_8);
        } else {
            InputStream in = Glossary.class.getResourceAsStream("/data/glossary.txt");
            if (in == null) {
                return;
            }
            br = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8));
        }
        try (BufferedReader reader = br) {
            String line;
            while ((line = reader.readLine()) != null) {
                int eq = line.indexOf('=');
                if (eq > 0) {
                    entries.put(line.substring(0, eq).trim(), line.substring(eq + 1).trim());
                }
            }
        }
    }

    public Map<String, String> getAllEntries() {
        return Collections.unmodifiableMap(entries);
    }

    public String lookupWord(String rawWord) {
        if (rawWord == null || rawWord.isEmpty()) {
            return null;
        }
        String cleaned = stripPunctuation(rawWord);
        if (cleaned.isEmpty()) {
            return null;
        }
        String exact = entries.get(cleaned);
        if (exact != null) {
            return cleaned + "  —  " + exact;
        }
        for (String p : PARTICLES) {
            if (cleaned.endsWith(p) && cleaned.length() > p.length()) {
                String stem = cleaned.substring(0, cleaned.length() - p.length());
                String meaning = entries.get(stem);
                if (meaning != null) {
                    return stem + "  —  " + meaning;
                }
            }
        }
        return null;
    }

    public String lookupPhrase(String phrase) {
        if (phrase == null || phrase.isEmpty()) {
            return null;
        }
        StringBuilder sb = new StringBuilder();
        String[] words = phrase.split("\\s+");
        for (String w : words) {
            String hit = lookupWord(w);
            if (hit != null && sb.indexOf(hit) < 0) {
                if (sb.length() > 0) {
                    sb.append("   |   ");
                }
                sb.append(hit);
            }
        }
        return sb.length() > 0 ? sb.toString() : null;
    }

    private String stripPunctuation(String s) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < s.length(); i++) {
            char ch = s.charAt(i);
            if (ch >= 0xAC00 && ch <= 0xD7A3) {
                sb.append(ch);
            }
        }
        return sb.toString();
    }
}

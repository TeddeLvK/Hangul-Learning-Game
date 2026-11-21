package com.koreangame;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;

public class SaveManager {
    public int currentLevel = 1;
    public int unlockedLevel = 1;
    public int xp = 0;
    public float playerX = 0.0f;
    public float playerY = 0.0f;
    public boolean hasSavedPos = false;
    public final Set<String> completedEntities = new HashSet<>();
    public final Set<Integer> seenIntros = new HashSet<>();

    private final Path savePath;

    public SaveManager(Path savePath) {
        this.savePath = savePath;
    }

    public void load() {
        if (!Files.exists(savePath)) {
            return;
        }
        try (BufferedReader br = Files.newBufferedReader(savePath, StandardCharsets.UTF_8)) {
            String line;
            while ((line = br.readLine()) != null) {
                int eq = line.indexOf('=');
                if (eq <= 0) {
                    continue;
                }
                String k = line.substring(0, eq).trim();
                String v = line.substring(eq + 1).trim();
                switch (k) {
                    case "currentLevel":
                        currentLevel = Integer.parseInt(v);
                        break;
                    case "unlockedLevel":
                        unlockedLevel = Integer.parseInt(v);
                        break;
                    case "xp":
                        xp = Integer.parseInt(v);
                        break;
                    case "playerX":
                        playerX = Float.parseFloat(v);
                        hasSavedPos = true;
                        break;
                    case "playerY":
                        playerY = Float.parseFloat(v);
                        break;
                    case "completed":
                        if (!v.isEmpty()) {
                            for (String id : v.split(",")) {
                                if (!id.isEmpty()) {
                                    completedEntities.add(id);
                                }
                            }
                        }
                        break;
                    case "seenIntros":
                        if (!v.isEmpty()) {
                            for (String id : v.split(",")) {
                                if (!id.isEmpty()) {
                                    seenIntros.add(Integer.parseInt(id));
                                }
                            }
                        }
                        break;
                    default:
                        break;
                }
            }
        } catch (IOException | NumberFormatException ignored) {
        }
    }

    public void save() {
        Path tmp = savePath.resolveSibling(savePath.getFileName().toString() + ".tmp");
        try (BufferedWriter bw = Files.newBufferedWriter(tmp, StandardCharsets.UTF_8)) {
            bw.write("currentLevel=" + currentLevel + "\n");
            bw.write("unlockedLevel=" + unlockedLevel + "\n");
            bw.write("xp=" + xp + "\n");
            if (hasSavedPos) {
                bw.write("playerX=" + String.format(java.util.Locale.US, "%.1f", playerX) + "\n");
                bw.write("playerY=" + String.format(java.util.Locale.US, "%.1f", playerY) + "\n");
            }
            bw.write("completed=" + String.join(",", new TreeSet<>(completedEntities)) + "\n");
            StringBuilder seenSb = new StringBuilder();
            for (Integer id : new TreeSet<>(seenIntros)) {
                if (seenSb.length() > 0) {
                    seenSb.append(",");
                }
                seenSb.append(id);
            }
            bw.write("seenIntros=" + seenSb + "\n");
        } catch (IOException ignored) {
            return;
        }
        try {
            Files.move(tmp, savePath, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
        } catch (IOException e) {
            try {
                Files.move(tmp, savePath, StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException ignored) {
            }
        }
    }
}

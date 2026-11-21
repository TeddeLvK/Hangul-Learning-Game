package com.koreangame;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public final class CurriculumLoader {
    private CurriculumLoader() {}

    public static List<Level> load(Path filePath) throws IOException {
        List<Level> levels = new ArrayList<>();
        Level currentLevel = null;
        Entity currentEntity = null;

        try (BufferedReader br = Files.newBufferedReader(filePath, StandardCharsets.UTF_8)) {
            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) {
                    continue;
                }
                if (line.equals("ENDLEVEL")) {
                    if (currentLevel != null) {
                        levels.add(currentLevel);
                    }
                    currentLevel = null;
                    currentEntity = null;
                    continue;
                }
                String[] parts = line.split("\\|", -1);
                String tag = parts[0];
                if (tag.equals("LEVEL")) {
                    currentLevel = new Level(
                        Integer.parseInt(parts[1]),
                        parts[2],
                        parts[3],
                        Integer.parseInt(parts[4]),
                        Integer.parseInt(parts[5])
                    );
                } else if (tag.equals("SUMMARY") && currentLevel != null) {
                    currentLevel.summary = parts[1];
                } else if (tag.equals("TIP") && currentLevel != null) {
                    currentLevel.tips.add(parts[1]);
                } else if (tag.equals("VOCAB") && currentLevel != null) {
                    for (String pair : parts[1].split(";")) {
                        String[] kv = pair.split("=", 2);
                        if (kv.length == 2) {
                            currentLevel.vocab.add(kv);
                        }
                    }
                } else if (tag.equals("MAP") && currentLevel != null) {
                    currentLevel.mapRows.add(parts[1]);
                } else if (tag.equals("ENTITY") && currentLevel != null) {
                    currentEntity = new Entity(
                        parts[1],
                        parts[2],
                        parts[3],
                        Integer.parseInt(parts[4]),
                        Integer.parseInt(parts[5]),
                        parts[6]
                    );
                    currentLevel.entities.add(currentEntity);
                } else if (tag.equals("TASK") && currentEntity != null) {
                    String type = parts[1];
                    String prompt = parts[2];
                    if (type.equals("build")) {
                        List<String> toks = splitNonEmpty(parts[3]);
                        List<String> dists = splitNonEmpty(parts[4]);
                        currentEntity.tasks.add(new Task(
                            type, prompt, Collections.emptyList(), "", toks, dists, parts[5]
                        ));
                    } else {
                        List<String> opts = splitNonEmpty(parts[3]);
                        currentEntity.tasks.add(new Task(
                            type, prompt, opts, parts[4], Collections.emptyList(), Collections.emptyList(), parts[5]
                        ));
                    }
                }
            }
        }
        return levels;
    }

    private static List<String> splitNonEmpty(String s) {
        if (s == null || s.isEmpty()) {
            return Collections.emptyList();
        }
        return Arrays.asList(s.split(";"));
    }
}

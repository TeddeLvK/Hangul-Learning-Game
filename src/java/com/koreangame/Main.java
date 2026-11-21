package com.koreangame;

import java.nio.file.Paths;
import java.util.List;
import javax.swing.SwingUtilities;

public final class Main {
    private Main() {}

    public static void main(String[] args) throws Exception {
        List<Level> levels = CurriculumLoader.load(Paths.get("src/data/levels.txt"));
        Glossary glossary = new Glossary();
        glossary.load(Paths.get("src/data/glossary.txt"));
        SaveManager save = new SaveManager(Paths.get("save_data.txt"));
        save.load();
        SwingUtilities.invokeLater(() -> {
            GameWindow win = new GameWindow(levels, save, glossary);
            win.setFullscreen(true);
        });
    }
}

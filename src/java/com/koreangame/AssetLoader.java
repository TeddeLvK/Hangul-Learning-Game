package com.koreangame;

import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import javax.imageio.ImageIO;

public class AssetLoader {
    private static final String[] FONT_CANDIDATES = {
        "AppleSDGothicNeo-Regular",
        "Apple SD Gothic Neo",
        "AppleGothic",
        "Malgun Gothic",
        "NanumGothic",
        "Noto Sans CJK KR",
        "Gulim",
        "SansSerif"
    };

    private final Path assetsRoot;
    private final String fontFamily;
    private final Map<String, Font> fontCache = new HashMap<>();
    private final Map<String, BufferedImage> imageCache = new HashMap<>();
    private final Set<String> missingImages = new HashSet<>();

    public AssetLoader(Path assetsRoot) {
        this.assetsRoot = assetsRoot;
        this.fontFamily = detectKoreanFont();
    }

    private String detectKoreanFont() {
        String[] available = GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames();
        Set<String> set = new HashSet<>();
        for (String name : available) {
            set.add(name);
        }
        for (String candidate : FONT_CANDIDATES) {
            if (set.contains(candidate)) {
                return candidate;
            }
        }
        return Font.SANS_SERIF;
    }

    public Font getFont(int size, boolean bold) {
        String key = size + ":" + bold;
        return fontCache.computeIfAbsent(
            key,
            k -> new Font(fontFamily, bold ? Font.BOLD : Font.PLAIN, size)
        );
    }

    public BufferedImage getImage(String relKey) {
        if (missingImages.contains(relKey)) {
            return null;
        }
        BufferedImage cached = imageCache.get(relKey);
        if (cached != null) {
            return cached;
        }
        File file = assetsRoot.resolve(relKey + ".png").toFile();
        if (!file.exists()) {
            missingImages.add(relKey);
            return null;
        }
        try {
            BufferedImage img = ImageIO.read(file);
            if (img != null) {
                imageCache.put(relKey, img);
                return img;
            }
        } catch (IOException ignored) {
        }
        missingImages.add(relKey);
        return null;
    }
}

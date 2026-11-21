package com.koreangame;

import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import javax.imageio.ImageIO;

public class AssetLoader {
    private final Path assetsRoot;
    private final Map<String, BufferedImage> imageCache = new HashMap<>();
    private final Set<String> missingImages = new HashSet<>();
    private final String fontFamily;

    public AssetLoader(Path assetsRoot) {
        this.assetsRoot = assetsRoot;
        this.fontFamily = pickKoreanFont();
    }

    private String pickKoreanFont() {
        String[] preferred = {
            "Apple SD Gothic Neo",
            "Malgun Gothic",
            "NanumGothic",
            "Noto Sans CJK KR",
            "AppleGothic",
            "SansSerif"
        };
        Set<String> available = new HashSet<>();
        for (String name : GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames()) {
            available.add(name);
        }
        for (String p : preferred) {
            if (available.contains(p)) {
                return p;
            }
        }
        return Font.SANS_SERIF;
    }

    public Font getFont(int size, boolean bold) {
        return new Font(fontFamily, bold ? Font.BOLD : Font.PLAIN, size);
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
        if (file.exists()) {
            try {
                BufferedImage img = ImageIO.read(file);
                if (img != null) {
                    imageCache.put(relKey, img);
                    return img;
                }
            } catch (IOException ignored) {
            }
        }
        try (InputStream in = AssetLoader.class.getResourceAsStream("/Assets/" + relKey + ".png")) {
            if (in != null) {
                BufferedImage img = ImageIO.read(in);
                if (img != null) {
                    imageCache.put(relKey, img);
                    return img;
                }
            }
        } catch (IOException ignored) {
        }
        missingImages.add(relKey);
        return null;
    }
}

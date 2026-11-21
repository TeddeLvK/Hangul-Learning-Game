package com.koreangame;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;

public final class UIUtils {
    public static final int TILE_SIZE = 48;
    public static final int MAP_COLS = 18;
    public static final int MAP_ROWS = 12;
    public static final int HUD_HEIGHT = 64;
    public static final int SCREEN_WIDTH = MAP_COLS * TILE_SIZE;
    public static final int SCREEN_HEIGHT = MAP_ROWS * TILE_SIZE + HUD_HEIGHT;

    public static final Color BG = new Color(24, 28, 38);
    public static final Color HUD = new Color(28, 34, 48);
    public static final Color HUD_BORDER = new Color(58, 70, 96);
    public static final Color PANEL = new Color(32, 38, 54);
    public static final Color PANEL_LIGHT = new Color(44, 52, 74);
    public static final Color PANEL_DARK = new Color(22, 26, 38);
    public static final Color ACCENT = new Color(86, 156, 246);
    public static final Color ACCENT_HOVER = new Color(112, 178, 255);
    public static final Color SUCCESS = new Color(64, 186, 114);
    public static final Color SUCCESS_HOVER = new Color(84, 206, 134);
    public static final Color ERROR = new Color(224, 86, 98);
    public static final Color GOLD = new Color(248, 202, 78);
    public static final Color TEXT = new Color(242, 246, 252);
    public static final Color MUTED = new Color(168, 178, 202);
    public static final Color DIM = new Color(10, 14, 22, 200);

    private UIUtils() {}

    public static void drawButton(
        Graphics2D g,
        Rectangle r,
        String label,
        Font font,
        Color bg,
        Color hoverBg,
        Point mouse,
        Color textColor,
        Color border
    ) {
        boolean hover = mouse != null && r.contains(mouse);
        g.setColor(hover ? hoverBg : bg);
        g.fillRoundRect(r.x, r.y, r.width, r.height, 14, 14);
        if (border != null) {
            g.setColor(border);
            g.setStroke(new BasicStroke(2f));
            g.drawRoundRect(r.x, r.y, r.width, r.height, 14, 14);
        }
        g.setFont(font);
        g.setColor(textColor);
        FontMetrics fm = g.getFontMetrics();
        int tx = r.x + (r.width - fm.stringWidth(label)) / 2;
        int ty = r.y + (r.height - fm.getHeight()) / 2 + fm.getAscent();
        g.drawString(label, tx, ty);
    }

    public static List<String> wrapText(String text, FontMetrics fm, int maxWidth) {
        List<String> lines = new ArrayList<>();
        String[] words = text.split(" ");
        StringBuilder cur = new StringBuilder();
        for (String w : words) {
            String test = cur.length() == 0 ? w : cur + " " + w;
            if (fm.stringWidth(test) <= maxWidth) {
                cur.setLength(0);
                cur.append(test);
            } else {
                if (cur.length() > 0) {
                    lines.add(cur.toString());
                }
                cur.setLength(0);
                cur.append(w);
            }
        }
        if (cur.length() > 0) {
            lines.add(cur.toString());
        }
        return lines;
    }
}

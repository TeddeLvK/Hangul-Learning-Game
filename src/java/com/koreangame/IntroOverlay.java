package com.koreangame;

import java.awt.BasicStroke;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.KeyEvent;
import java.util.List;

public class IntroOverlay {
    private final Level level;
    private final AssetLoader assets;
    private final Rectangle closeBtn = new Rectangle(UIUtils.SCREEN_WIDTH / 2 - 105, UIUtils.SCREEN_HEIGHT - 88, 210, 44);

    public IntroOverlay(Level level, AssetLoader assets) {
        this.level = level;
        this.assets = assets;
    }

    public boolean onKeyPressed(int keyCode) {
        return keyCode == KeyEvent.VK_ENTER
            || keyCode == KeyEvent.VK_SPACE
            || keyCode == KeyEvent.VK_ESCAPE
            || keyCode == KeyEvent.VK_TAB;
    }

    public boolean onMouseClicked(Point p) {
        return closeBtn.contains(p);
    }

    public void draw(Graphics2D g, Point mouse) {
        g.setColor(UIUtils.DIM);
        g.fillRect(0, 0, UIUtils.SCREEN_WIDTH, UIUtils.SCREEN_HEIGHT);

        Rectangle panel = new Rectangle(42, 30, UIUtils.SCREEN_WIDTH - 84, UIUtils.SCREEN_HEIGHT - 60);
        g.setColor(UIUtils.PANEL);
        g.fillRoundRect(panel.x, panel.y, panel.width, panel.height, 20, 20);
        g.setColor(UIUtils.ACCENT);
        g.setStroke(new BasicStroke(2f));
        g.drawRoundRect(panel.x, panel.y, panel.width, panel.height, 20, 20);

        Font fHead = assets.getFont(21, true);
        Font fSec = assets.getFont(15, true);
        Font fBody = assets.getFont(14, false);
        Font fBtn = assets.getFont(15, true);

        int y = panel.y + 42;
        g.setFont(fHead);
        g.setColor(UIUtils.GOLD);
        g.drawString("Level " + level.id + ": " + level.title, panel.x + 26, y);
        y += 28;

        g.setFont(fBody);
        g.setColor(UIUtils.TEXT);
        FontMetrics fmBody = g.getFontMetrics();
        for (String line : UIUtils.wrapText(level.summary, fmBody, panel.width - 52)) {
            g.drawString(line, panel.x + 26, y);
            y += 22;
        }
        y += 14;

        g.setFont(fSec);
        g.setColor(UIUtils.ACCENT);
        g.drawString("Good Things to Know", panel.x + 26, y);
        y += 24;

        g.setFont(fBody);
        g.setColor(UIUtils.TEXT);
        for (String tip : level.tips) {
            List<String> wrapped = UIUtils.wrapText("• " + tip, fmBody, panel.width - 60);
            for (String line : wrapped) {
                g.drawString(line, panel.x + 30, y);
                y += 21;
            }
            y += 4;
        }

        y += 10;
        g.setFont(fSec);
        g.setColor(UIUtils.ACCENT);
        g.drawString("Key Vocabulary", panel.x + 26, y);
        y += 24;

        g.setFont(fBody);
        g.setColor(UIUtils.MUTED);
        int colW = (panel.width - 56) / 3;
        for (int i = 0; i < level.vocab.size(); i++) {
            int col = i % 3;
            int row = i / 3;
            int vx = panel.x + 28 + col * colW;
            int vy = y + row * 24;
            if (vy < closeBtn.y - 16) {
                String[] kv = level.vocab.get(i);
                g.drawString(kv[0] + " — " + kv[1], vx, vy);
            }
        }

        UIUtils.drawButton(g, closeBtn, "Start Exploring [Enter]", fBtn, UIUtils.ACCENT, UIUtils.ACCENT_HOVER, mouse, UIUtils.TEXT, null);
    }
}

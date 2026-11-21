package com.koreangame;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class LevelSelectOverlay {
    private static final int PER_PAGE = 10;

    private final List<Level> levels;
    private final int currentLevelId;
    private final int unlockedLevelId;
    private final Set<String> completed;
    private final AssetLoader assets;
    private int page;
    private final int totalPages;

    private final List<Rectangle> cardRects = new ArrayList<>();
    private final List<Integer> cardLevelIds = new ArrayList<>();
    private final Rectangle prevBtn = new Rectangle(96, UIUtils.SCREEN_HEIGHT - 86, 110, 38);
    private final Rectangle nextBtn = new Rectangle(UIUtils.SCREEN_WIDTH - 206, UIUtils.SCREEN_HEIGHT - 86, 110, 38);
    private final Rectangle closeBtn = new Rectangle(UIUtils.SCREEN_WIDTH / 2 - 75, UIUtils.SCREEN_HEIGHT - 86, 150, 38);

    public LevelSelectOverlay(List<Level> levels, int currentLevelId, int unlockedLevelId, Set<String> completed, AssetLoader assets) {
        this.levels = levels;
        this.currentLevelId = currentLevelId;
        this.unlockedLevelId = unlockedLevelId;
        this.completed = completed;
        this.assets = assets;
        this.totalPages = (levels.size() + PER_PAGE - 1) / PER_PAGE;
        this.page = Math.max(0, Math.min(totalPages - 1, (currentLevelId - 1) / PER_PAGE));
    }

    public int onKeyPressed(int keyCode) {
        if (keyCode == KeyEvent.VK_ESCAPE || keyCode == KeyEvent.VK_L) {
            return -1;
        }
        if (keyCode == KeyEvent.VK_LEFT && page > 0) {
            page--;
        } else if (keyCode == KeyEvent.VK_RIGHT && page < totalPages - 1) {
            page++;
        }
        return 0;
    }

    public int onMouseClicked(Point p) {
        if (closeBtn.contains(p)) {
            return -1;
        }
        if (prevBtn.contains(p) && page > 0) {
            page--;
            return 0;
        }
        if (nextBtn.contains(p) && page < totalPages - 1) {
            page++;
            return 0;
        }
        for (int i = 0; i < cardRects.size(); i++) {
            int lvId = cardLevelIds.get(i);
            if (lvId <= unlockedLevelId && cardRects.get(i).contains(p)) {
                return lvId;
            }
        }
        return 0;
    }

    public void draw(Graphics2D g, Point mouse) {
        g.setColor(UIUtils.DIM);
        g.fillRect(0, 0, UIUtils.SCREEN_WIDTH, UIUtils.SCREEN_HEIGHT);

        Rectangle panel = new Rectangle(56, 32, UIUtils.SCREEN_WIDTH - 112, UIUtils.SCREEN_HEIGHT - 64);
        g.setColor(UIUtils.PANEL);
        g.fillRoundRect(panel.x, panel.y, panel.width, panel.height, 20, 20);
        g.setColor(UIUtils.ACCENT);
        g.setStroke(new BasicStroke(2f));
        g.drawRoundRect(panel.x, panel.y, panel.width, panel.height, 20, 20);

        Font fHead = assets.getFont(20, true);
        Font fTitle = assets.getFont(13, true);
        Font fSub = assets.getFont(12, false);
        Font fBtn = assets.getFont(14, true);

        int start = page * PER_PAGE;
        int end = Math.min(levels.size(), start + PER_PAGE);

        g.setFont(fHead);
        g.setColor(UIUtils.GOLD);
        g.drawString("Select Level  (" + (start + 1) + "–" + end + " of " + levels.size() + ")", panel.x + 24, panel.y + 38);

        cardRects.clear();
        cardLevelIds.clear();

        int colW = (panel.width - 64) / 2;
        int cardH = 74;
        for (int idx = start; idx < end; idx++) {
            Level lv = levels.get(idx);
            int local = idx - start;
            int col = local % 2;
            int row = local / 2;
            int rx = panel.x + 24 + col * (colW + 16);
            int ry = panel.y + 58 + row * (cardH + 12);
            Rectangle r = new Rectangle(rx, ry, colW, cardH);
            cardRects.add(r);
            cardLevelIds.add(lv.id);

            boolean locked = lv.id > unlockedLevelId;
            int done = 0;
            for (Entity e : lv.entities) {
                if (completed.contains(e.id)) {
                    done++;
                }
            }
            boolean hover = !locked && mouse != null && r.contains(mouse);
            Color bg = locked ? new Color(36, 40, 52)
                : (lv.id == currentLevelId ? UIUtils.ACCENT : (hover ? new Color(62, 74, 104) : UIUtils.PANEL_LIGHT));

            g.setColor(bg);
            g.fillRoundRect(r.x, r.y, r.width, r.height, 12, 12);
            g.setColor(UIUtils.HUD_BORDER);
            g.setStroke(new BasicStroke(1.5f));
            g.drawRoundRect(r.x, r.y, r.width, r.height, 12, 12);

            g.setFont(fTitle);
            g.setColor(locked ? UIUtils.MUTED : UIUtils.TEXT);
            String label = "Lv." + lv.id + "  " + lv.title;
            if (label.length() > 36) {
                label = label.substring(0, 35) + "…";
            }
            g.drawString(label, r.x + 12, r.y + 26);

            g.setFont(fSub);
            g.setColor(locked ? UIUtils.MUTED : (done == lv.entities.size() ? UIUtils.GOLD : UIUtils.MUTED));
            String status = locked ? "LOCKED" : (done + "/" + lv.entities.size() + " Done  •  " + lv.zoneName);
            g.drawString(status, r.x + 12, r.y + 52);
        }

        if (page > 0) {
            UIUtils.drawButton(g, prevBtn, "< Prev", fBtn, UIUtils.PANEL_LIGHT, UIUtils.ACCENT, mouse, UIUtils.TEXT, UIUtils.HUD_BORDER);
        }
        if (page < totalPages - 1) {
            UIUtils.drawButton(g, nextBtn, "Next >", fBtn, UIUtils.PANEL_LIGHT, UIUtils.ACCENT, mouse, UIUtils.TEXT, UIUtils.HUD_BORDER);
        }
        UIUtils.drawButton(g, closeBtn, "Close [Esc]", fBtn, UIUtils.PANEL_LIGHT, UIUtils.ACCENT, mouse, UIUtils.TEXT, UIUtils.HUD_BORDER);
    }
}

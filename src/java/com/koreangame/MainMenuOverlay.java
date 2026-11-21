package com.koreangame;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;

public class MainMenuOverlay {
    private final AssetLoader assets;
    private final SaveManager save;
    private final boolean inGame;
    private boolean showingInfo = false;

    private final Rectangle startBtn = new Rectangle(UIUtils.SCREEN_WIDTH / 2 - 135, 326, 270, 48);
    private final Rectangle infoBtn = new Rectangle(UIUtils.SCREEN_WIDTH / 2 - 135, 386, 270, 48);
    private final Rectangle quitBtn = new Rectangle(UIUtils.SCREEN_WIDTH / 2 - 135, 446, 270, 48);
    private final Rectangle backBtn = new Rectangle(UIUtils.SCREEN_WIDTH / 2 - 105, UIUtils.SCREEN_HEIGHT - 102, 210, 44);

    public MainMenuOverlay(AssetLoader assets, SaveManager save, boolean inGame) {
        this.assets = assets;
        this.save = save;
        this.inGame = inGame;
    }

    public String onKeyPressed(int keyCode) {
        if (showingInfo) {
            if (keyCode == KeyEvent.VK_ESCAPE || keyCode == KeyEvent.VK_ENTER || keyCode == KeyEvent.VK_SPACE) {
                showingInfo = false;
            }
            return null;
        }
        if (keyCode == KeyEvent.VK_ENTER || keyCode == KeyEvent.VK_SPACE) {
            return "start";
        }
        if (keyCode == KeyEvent.VK_ESCAPE && inGame) {
            return "start";
        }
        if (keyCode == KeyEvent.VK_I) {
            showingInfo = true;
        }
        return null;
    }

    public String onMouseClicked(Point p) {
        if (showingInfo) {
            if (backBtn.contains(p)) {
                showingInfo = false;
            }
            return null;
        }
        if (startBtn.contains(p)) {
            return "start";
        }
        if (infoBtn.contains(p)) {
            showingInfo = true;
            return null;
        }
        if (quitBtn.contains(p)) {
            return "quit";
        }
        return null;
    }

    public void draw(Graphics2D g, Point mouse) {
        g.setColor(new Color(10, 14, 22, 215));
        g.fillRect(0, 0, UIUtils.SCREEN_WIDTH, UIUtils.SCREEN_HEIGHT);

        if (showingInfo) {
            drawInfoPanel(g, mouse);
            return;
        }

        Rectangle panel = new Rectangle(144, 54, UIUtils.SCREEN_WIDTH - 288, UIUtils.SCREEN_HEIGHT - 108);
        g.setColor(UIUtils.PANEL);
        g.fillRoundRect(panel.x, panel.y, panel.width, panel.height, 22, 22);
        g.setColor(UIUtils.GOLD);
        g.setStroke(new BasicStroke(2.5f));
        g.drawRoundRect(panel.x, panel.y, panel.width, panel.height, 22, 22);

        drawPixelSprite(g, "sprites/player", panel.x + 34, panel.y + 42, 64);
        drawPixelSprite(g, "sprites/npc_barista", panel.x + panel.width - 98, panel.y + 42, 64);
        drawPixelSprite(g, "objects/coffee", panel.x + 38, panel.y + panel.height - 96, 48);
        drawPixelSprite(g, "objects/bibimbap", panel.x + panel.width - 86, panel.y + panel.height - 96, 48);

        Font fKr = assets.getFont(44, true);
        Font fEn = assets.getFont(22, true);
        Font fTag = assets.getFont(13, true);
        Font fSub = assets.getFont(14, false);
        Font fBtn = assets.getFont(16, true);
        Font fFoot = assets.getFont(12, false);

        g.setFont(fKr);
        String krTitle = "한글몬";
        FontMetrics fmKr = g.getFontMetrics();
        int krX = (UIUtils.SCREEN_WIDTH - fmKr.stringWidth(krTitle)) / 2;
        g.setColor(new Color(14, 18, 28));
        g.drawString(krTitle, krX + 3, panel.y + 82);
        g.setColor(UIUtils.GOLD);
        g.drawString(krTitle, krX, panel.y + 79);

        g.setFont(fEn);
        String enTitle = "HANGEULMON";
        FontMetrics fmEn = g.getFontMetrics();
        int enX = (UIUtils.SCREEN_WIDTH - fmEn.stringWidth(enTitle)) / 2;
        g.setColor(UIUtils.ACCENT);
        g.drawString(enTitle, enX, panel.y + 114);

        g.setFont(fTag);
        String krTag = "단어 마스터들의 부상";
        FontMetrics fmTag = g.getFontMetrics();
        int krTagX = (UIUtils.SCREEN_WIDTH - fmTag.stringWidth(krTag)) / 2;
        g.setColor(UIUtils.TEXT);
        g.drawString(krTag, krTagX, panel.y + 140);

        String enTag = "Rise of the Word Masters";
        int enTagX = (UIUtils.SCREEN_WIDTH - fmTag.stringWidth(enTag)) / 2;
        g.setColor(UIUtils.MUTED);
        g.drawString(enTag, enTagX, panel.y + 159);

        g.setColor(UIUtils.HUD_BORDER);
        g.setStroke(new BasicStroke(2f));
        g.drawLine(panel.x + 64, panel.y + 174, panel.x + panel.width - 64, panel.y + 174);

        Rectangle statBox = new Rectangle(UIUtils.SCREEN_WIDTH / 2 - 190, panel.y + 190, 380, 42);
        g.setColor(UIUtils.PANEL_DARK);
        g.fillRoundRect(statBox.x, statBox.y, statBox.width, statBox.height, 12, 12);
        g.setColor(UIUtils.HUD_BORDER);
        g.setStroke(new BasicStroke(1.5f));
        g.drawRoundRect(statBox.x, statBox.y, statBox.width, statBox.height, 12, 12);

        g.setFont(fSub);
        g.setColor(UIUtils.TEXT);
        String chName = save.currentLevel <= 50 ? "Ch.1" : "Ch.2";
        String stats = chName + " Lv." + save.currentLevel + " / 79   •   " + save.xp + " XP   •   " + save.completedEntities.size() + " / 345 Quests";
        FontMetrics fmStat = g.getFontMetrics();
        g.drawString(stats, statBox.x + (statBox.width - fmStat.stringWidth(stats)) / 2, statBox.y + 26);

        String startLabel = inGame
            ? "Resume Game"
            : (save.xp > 0 || save.currentLevel > 1 ? "Continue  (Lv. " + save.currentLevel + ")" : "Start Game");

        UIUtils.drawButton(g, startBtn, startLabel, fBtn, UIUtils.SUCCESS, UIUtils.SUCCESS_HOVER, mouse, UIUtils.TEXT, null);
        UIUtils.drawButton(g, infoBtn, "Info & Controls", fBtn, UIUtils.PANEL_LIGHT, UIUtils.ACCENT, mouse, UIUtils.TEXT, UIUtils.HUD_BORDER);
        UIUtils.drawButton(g, quitBtn, "Quit Game", fBtn, UIUtils.PANEL_LIGHT, UIUtils.ERROR, mouse, UIUtils.TEXT, UIUtils.HUD_BORDER);

        g.setFont(fFoot);
        g.setColor(UIUtils.MUTED);
        String foot = "Press [Enter] to Play   •   [F11] Toggle Fullscreen";
        FontMetrics fmFoot = g.getFontMetrics();
        g.drawString(foot, (UIUtils.SCREEN_WIDTH - fmFoot.stringWidth(foot)) / 2, panel.y + panel.height - 20);
    }

    private void drawInfoPanel(Graphics2D g, Point mouse) {
        Rectangle panel = new Rectangle(64, 42, UIUtils.SCREEN_WIDTH - 128, UIUtils.SCREEN_HEIGHT - 84);
        g.setColor(UIUtils.PANEL);
        g.fillRoundRect(panel.x, panel.y, panel.width, panel.height, 20, 20);
        g.setColor(UIUtils.ACCENT);
        g.setStroke(new BasicStroke(2f));
        g.drawRoundRect(panel.x, panel.y, panel.width, panel.height, 20, 20);

        Font fHead = assets.getFont(22, true);
        Font fSec = assets.getFont(15, true);
        Font fBody = assets.getFont(14, false);
        Font fBtn = assets.getFont(15, true);

        int x = panel.x + 28;
        int w = panel.width - 56;
        int y = panel.y + 44;

        g.setFont(fHead);
        g.setColor(UIUtils.GOLD);
        g.drawString("한글몬 (Hangeulmon)  —  How to Play", x, y);
        y += 30;

        g.setFont(fBody);
        g.setColor(UIUtils.TEXT);
        FontMetrics fm = g.getFontMetrics();
        for (String line : UIUtils.wrapText(
            "Explore 50 pixel-art zones across Seoul, talk to locals, inspect everyday objects, and build Korean sentences as the grammar gradually scales from core nouns to real-world idioms.",
            fm,
            w
        )) {
            g.drawString(line, x, y);
            y += 22;
        }
        y += 14;

        g.setFont(fSec);
        g.setColor(UIUtils.ACCENT);
        g.drawString("Controls", x, y);
        y += 24;

        String[] controls = {
            "• Move: WASD or Arrow Keys",
            "• Talk / Inspect: E, Space, Enter, or Click directly on any character or object",
            "• Level Guide: Tab (view the level intro, grammar rules, and key vocabulary anytime)",
            "• Level Select: L (jump between unlocked levels across all 50 zones)",
            "• Main Menu: M or Esc   |   Fullscreen: F or F11"
        };
        g.setFont(fBody);
        g.setColor(UIUtils.TEXT);
        for (String c : controls) {
            g.drawString(c, x + 6, y);
            y += 22;
        }
        y += 14;

        g.setFont(fSec);
        g.setColor(UIUtils.ACCENT);
        g.drawString("Learning Features", x, y);
        y += 24;

        String[] features = {
            "• Noun Hover Hints: Hover any gold-underlined noun or word tile to see its English meaning.",
            "• Base Verb Hints: Conjugation and sentence-building tasks always show the dictionary -다 form.",
            "• Auto-Save: Your level, XP, completed quests, and position save automatically to save_data.txt."
        };
        g.setFont(fBody);
        g.setColor(UIUtils.TEXT);
        for (String feat : features) {
            for (String line : UIUtils.wrapText(feat, fm, w - 10)) {
                g.drawString(line, x + 6, y);
                y += 21;
            }
            y += 4;
        }

        UIUtils.drawButton(g, backBtn, "Back to Menu [Esc]", fBtn, UIUtils.ACCENT, UIUtils.ACCENT_HOVER, mouse, UIUtils.TEXT, null);
    }

    private void drawPixelSprite(Graphics2D g, String key, int x, int y, int size) {
        BufferedImage img = assets.getImage(key);
        if (img == null) {
            return;
        }
        Object prev = g.getRenderingHint(RenderingHints.KEY_INTERPOLATION);
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
        g.drawImage(img, x, y, size, size, null);
        if (prev != null) {
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, prev);
        }
    }
}

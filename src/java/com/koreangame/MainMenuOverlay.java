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
import java.util.List;

public class MainMenuOverlay {
    private static final int VIEW_MAIN = 0;
    private static final int VIEW_INFO = 1;
    private static final int VIEW_SLOTS = 2;
    private static final int VIEW_ACHIEVEMENTS = 3;
    private static final int ACH_PER_PAGE = 6;

    private final AssetLoader assets;
    private final SaveManager[] slots;
    private int activeSlot;
    private final boolean inGame;
    private int view = VIEW_MAIN;
    private int confirmingResetSlot = -1;
    private int achPage = 0;

    private final Rectangle startBtn = new Rectangle(UIUtils.SCREEN_WIDTH / 2 - 135, 304, 270, 40);
    private final Rectangle slotsBtn = new Rectangle(UIUtils.SCREEN_WIDTH / 2 - 135, 352, 270, 40);
    private final Rectangle achBtn = new Rectangle(UIUtils.SCREEN_WIDTH / 2 - 135, 400, 270, 40);
    private final Rectangle infoBtn = new Rectangle(UIUtils.SCREEN_WIDTH / 2 - 135, 448, 270, 40);
    private final Rectangle quitBtn = new Rectangle(UIUtils.SCREEN_WIDTH / 2 - 135, 496, 270, 40);
    private final Rectangle backBtn = new Rectangle(UIUtils.SCREEN_WIDTH / 2 - 105, UIUtils.SCREEN_HEIGHT - 92, 210, 42);
    private final Rectangle achPrevBtn = new Rectangle(96, UIUtils.SCREEN_HEIGHT - 92, 110, 42);
    private final Rectangle achNextBtn = new Rectangle(UIUtils.SCREEN_WIDTH - 206, UIUtils.SCREEN_HEIGHT - 92, 110, 42);

    private final Rectangle[] slotPlayRects = new Rectangle[3];
    private final Rectangle[] slotResetRects = new Rectangle[3];
    private final Rectangle[] slotCancelRects = new Rectangle[3];

    public MainMenuOverlay(AssetLoader assets, SaveManager[] slots, int activeSlot, boolean inGame) {
        this.assets = assets;
        this.slots = slots;
        this.activeSlot = activeSlot;
        this.inGame = inGame;
        for (int i = 0; i < 3; i++) {
            int y = 128 + i * 124;
            slotPlayRects[i] = new Rectangle(92, y, 524, 102);
            slotResetRects[i] = new Rectangle(628, y + 12, 144, 36);
            slotCancelRects[i] = new Rectangle(628, y + 54, 144, 36);
        }
    }

    public void setActiveSlot(int idx) {
        this.activeSlot = idx;
    }

    public String onKeyPressed(int keyCode) {
        if (view == VIEW_ACHIEVEMENTS) {
            int totalPages = Math.max(1, (AchievementManager.getAll().size() + ACH_PER_PAGE - 1) / ACH_PER_PAGE);
            if (keyCode == KeyEvent.VK_ESCAPE) {
                view = VIEW_MAIN;
            } else if (keyCode == KeyEvent.VK_LEFT && achPage > 0) {
                achPage--;
            } else if (keyCode == KeyEvent.VK_RIGHT && achPage < totalPages - 1) {
                achPage++;
            }
            return null;
        }
        if (view == VIEW_INFO || view == VIEW_SLOTS) {
            if (keyCode == KeyEvent.VK_ESCAPE) {
                if (confirmingResetSlot >= 0) {
                    confirmingResetSlot = -1;
                } else {
                    view = VIEW_MAIN;
                }
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
            view = VIEW_INFO;
        } else if (keyCode == KeyEvent.VK_S) {
            view = VIEW_SLOTS;
            confirmingResetSlot = -1;
        } else if (keyCode == KeyEvent.VK_A) {
            view = VIEW_ACHIEVEMENTS;
            achPage = 0;
        }
        return null;
    }

    public String onMouseClicked(Point p) {
        if (view == VIEW_INFO) {
            if (backBtn.contains(p)) {
                view = VIEW_MAIN;
            }
            return null;
        }
        if (view == VIEW_ACHIEVEMENTS) {
            int totalPages = Math.max(1, (AchievementManager.getAll().size() + ACH_PER_PAGE - 1) / ACH_PER_PAGE);
            if (backBtn.contains(p)) {
                view = VIEW_MAIN;
                return null;
            }
            if (achPrevBtn.contains(p) && achPage > 0) {
                achPage--;
                return null;
            }
            if (achNextBtn.contains(p) && achPage < totalPages - 1) {
                achPage++;
                return null;
            }
            return null;
        }
        if (view == VIEW_SLOTS) {
            if (backBtn.contains(p)) {
                confirmingResetSlot = -1;
                view = VIEW_MAIN;
                return null;
            }
            for (int i = 0; i < 3; i++) {
                if (confirmingResetSlot == i) {
                    if (slotResetRects[i].contains(p)) {
                        confirmingResetSlot = -1;
                        return "reset:" + i;
                    }
                    if (slotCancelRects[i].contains(p)) {
                        confirmingResetSlot = -1;
                        return null;
                    }
                } else {
                    if (slotResetRects[i].contains(p) && slots[i].hasProgress()) {
                        confirmingResetSlot = i;
                        return null;
                    }
                    if (slotPlayRects[i].contains(p)) {
                        confirmingResetSlot = -1;
                        activeSlot = i;
                        return "load:" + i;
                    }
                }
            }
            return null;
        }
        if (startBtn.contains(p)) {
            return "start";
        }
        if (slotsBtn.contains(p)) {
            view = VIEW_SLOTS;
            confirmingResetSlot = -1;
            return null;
        }
        if (achBtn.contains(p)) {
            view = VIEW_ACHIEVEMENTS;
            achPage = 0;
            return null;
        }
        if (infoBtn.contains(p)) {
            view = VIEW_INFO;
            return null;
        }
        if (quitBtn.contains(p)) {
            return "quit";
        }
        return null;
    }

    private String getPlayerSpriteKeyForSave(SaveManager s) {
        boolean ch1Done = true;
        for (int lv = 1; lv <= 50; lv++) {
            for (int e = 1; e <= 4; e++) {
                if (!s.completedEntities.contains("l" + lv + "_e" + e)) {
                    ch1Done = false;
                    break;
                }
            }
            if (!ch1Done) {
                break;
            }
        }
        boolean ch2Done = true;
        for (int lv = 51; lv <= 79; lv++) {
            for (int e = 1; e <= 5; e++) {
                if (!s.completedEntities.contains("l" + lv + "_e" + e)) {
                    ch2Done = false;
                    break;
                }
            }
            if (!ch2Done) {
                break;
            }
        }
        if (ch2Done) {
            return "sprites/player_crown_big";
        }
        if (ch1Done) {
            return "sprites/player_crown_small";
        }
        return "sprites/player";
    }

    public void draw(Graphics2D g, Point mouse) {
        g.setColor(new Color(10, 14, 22, 215));
        g.fillRect(0, 0, UIUtils.SCREEN_WIDTH, UIUtils.SCREEN_HEIGHT);

        if (view == VIEW_INFO) {
            drawInfoPanel(g, mouse);
            return;
        }
        if (view == VIEW_SLOTS) {
            drawSlotsPanel(g, mouse);
            return;
        }
        if (view == VIEW_ACHIEVEMENTS) {
            drawAchievementsPanel(g, mouse);
            return;
        }

        SaveManager save = slots[activeSlot];

        Rectangle panel = new Rectangle(144, 38, UIUtils.SCREEN_WIDTH - 288, UIUtils.SCREEN_HEIGHT - 76);
        g.setColor(UIUtils.PANEL);
        g.fillRoundRect(panel.x, panel.y, panel.width, panel.height, 22, 22);
        g.setColor(UIUtils.GOLD);
        g.setStroke(new BasicStroke(2.5f));
        g.drawRoundRect(panel.x, panel.y, panel.width, panel.height, 22, 22);

        drawPixelSprite(g, getPlayerSpriteKeyForSave(save), panel.x + 34, panel.y + 42, 64);
        drawPixelSprite(g, "sprites/npc_barista", panel.x + panel.width - 98, panel.y + 42, 64);
        drawPixelSprite(g, "objects/coffee", panel.x + 38, panel.y + panel.height - 96, 48);
        drawPixelSprite(g, "objects/bibimbap", panel.x + panel.width - 86, panel.y + panel.height - 96, 48);

        if (save.unlockedAchievements.contains(AchievementManager.HUNGER_GOD_ID)) {
            drawPixelSprite(g, "objects/hunger_god_badge", panel.x + 48, panel.y + 110, 36);
            Font fHg = assets.getFont(10, true);
            g.setFont(fHg);
            g.setColor(UIUtils.GOLD);
            g.drawString("HUNGER GOD", panel.x + 28, panel.y + 160);
        }

        Font fKr = assets.getFont(44, true);
        Font fEn = assets.getFont(22, true);
        Font fTag = assets.getFont(13, true);
        Font fSub = assets.getFont(13, false);
        Font fBtn = assets.getFont(15, true);
        Font fFoot = assets.getFont(12, false);

        g.setFont(fKr);
        String krTitle = "한글몬";
        FontMetrics fmKr = g.getFontMetrics();
        int krX = (UIUtils.SCREEN_WIDTH - fmKr.stringWidth(krTitle)) / 2;
        g.setColor(new Color(14, 18, 28));
        g.drawString(krTitle, krX + 3, panel.y + 80);
        g.setColor(UIUtils.GOLD);
        g.drawString(krTitle, krX, panel.y + 77);

        g.setFont(fEn);
        String enTitle = "HANGEULMON";
        FontMetrics fmEn = g.getFontMetrics();
        int enX = (UIUtils.SCREEN_WIDTH - fmEn.stringWidth(enTitle)) / 2;
        g.setColor(UIUtils.ACCENT);
        g.drawString(enTitle, enX, panel.y + 112);

        g.setFont(fTag);
        String krTag = "단어 마스터들의 부상";
        FontMetrics fmTag = g.getFontMetrics();
        int krTagX = (UIUtils.SCREEN_WIDTH - fmTag.stringWidth(krTag)) / 2;
        g.setColor(UIUtils.TEXT);
        g.drawString(krTag, krTagX, panel.y + 138);

        String enTag = "Rise of the Word Masters";
        int enTagX = (UIUtils.SCREEN_WIDTH - fmTag.stringWidth(enTag)) / 2;
        g.setColor(UIUtils.MUTED);
        g.drawString(enTag, enTagX, panel.y + 157);

        g.setColor(UIUtils.HUD_BORDER);
        g.setStroke(new BasicStroke(2f));
        g.drawLine(panel.x + 64, panel.y + 172, panel.x + panel.width - 64, panel.y + 172);

        Rectangle statBox = new Rectangle(UIUtils.SCREEN_WIDTH / 2 - 205, panel.y + 184, 410, 40);
        g.setColor(UIUtils.PANEL_DARK);
        g.fillRoundRect(statBox.x, statBox.y, statBox.width, statBox.height, 12, 12);
        g.setColor(UIUtils.HUD_BORDER);
        g.setStroke(new BasicStroke(1.5f));
        g.drawRoundRect(statBox.x, statBox.y, statBox.width, statBox.height, 12, 12);

        g.setFont(fSub);
        g.setColor(UIUtils.TEXT);
        String chName = save.currentLevel == 80 ? "FINAL" : (save.currentLevel <= 50 ? "Ch.1" : "Ch.2");
        String stats = "Slot " + save.slotNumber + "  •  " + chName + " Lv." + save.currentLevel + "/80  •  " + save.xp + " XP  •  Ach: " + save.unlockedAchievements.size() + "/" + AchievementManager.getAll().size();
        FontMetrics fmStat = g.getFontMetrics();
        g.drawString(stats, statBox.x + (statBox.width - fmStat.stringWidth(stats)) / 2, statBox.y + 25);

        String startLabel = inGame
            ? "Resume Game  (Slot " + save.slotNumber + ")"
            : (save.hasProgress() ? "Continue  (Slot " + save.slotNumber + ")" : "Start Game  (Slot " + save.slotNumber + ")");

        UIUtils.drawButton(g, startBtn, startLabel, fBtn, UIUtils.SUCCESS, UIUtils.SUCCESS_HOVER, mouse, UIUtils.TEXT, null);
        UIUtils.drawButton(g, slotsBtn, "Save Files (3 Slots)", fBtn, UIUtils.PANEL_LIGHT, UIUtils.ACCENT, mouse, UIUtils.TEXT, UIUtils.HUD_BORDER);
        UIUtils.drawButton(g, achBtn, "Achievements (" + save.unlockedAchievements.size() + "/" + AchievementManager.getAll().size() + ")", fBtn, UIUtils.PANEL_LIGHT, UIUtils.GOLD, mouse, UIUtils.TEXT, UIUtils.HUD_BORDER);
        UIUtils.drawButton(g, infoBtn, "Info & Controls", fBtn, UIUtils.PANEL_LIGHT, UIUtils.ACCENT, mouse, UIUtils.TEXT, UIUtils.HUD_BORDER);
        UIUtils.drawButton(g, quitBtn, "Quit Game", fBtn, UIUtils.PANEL_LIGHT, UIUtils.ERROR, mouse, UIUtils.TEXT, UIUtils.HUD_BORDER);

        g.setFont(fFoot);
        g.setColor(UIUtils.MUTED);
        String foot = "[Enter] Play   •   [S] Saves   •   [A] Achievements   •   [F11] Fullscreen";
        FontMetrics fmFoot = g.getFontMetrics();
        g.drawString(foot, (UIUtils.SCREEN_WIDTH - fmFoot.stringWidth(foot)) / 2, panel.y + panel.height - 16);
    }

    private void drawAchievementsPanel(Graphics2D g, Point mouse) {
        SaveManager save = slots[activeSlot];
        List<Achievement> all = AchievementManager.getAll();
        int totalPages = Math.max(1, (all.size() + ACH_PER_PAGE - 1) / ACH_PER_PAGE);
        if (achPage >= totalPages) {
            achPage = totalPages - 1;
        }

        Rectangle panel = new Rectangle(56, 36, UIUtils.SCREEN_WIDTH - 112, UIUtils.SCREEN_HEIGHT - 72);
        g.setColor(UIUtils.PANEL);
        g.fillRoundRect(panel.x, panel.y, panel.width, panel.height, 20, 20);
        g.setColor(UIUtils.GOLD);
        g.setStroke(new BasicStroke(2f));
        g.drawRoundRect(panel.x, panel.y, panel.width, panel.height, 20, 20);

        Font fHead = assets.getFont(21, true);
        Font fTitle = assets.getFont(14, true);
        Font fBody = assets.getFont(12, false);
        Font fBtn = assets.getFont(14, true);

        g.setFont(fHead);
        g.setColor(UIUtils.GOLD);
        g.drawString(
            "Achievements  —  " + save.unlockedAchievements.size() + " / " + all.size() + " Unlocked  (Page " + (achPage + 1) + "/" + totalPages + ")",
            panel.x + 26,
            panel.y + 40
        );

        int start = achPage * ACH_PER_PAGE;
        int end = Math.min(all.size(), start + ACH_PER_PAGE);
        int colW = (panel.width - 68) / 2;
        int cardH = 114;

        for (int i = start; i < end; i++) {
            Achievement a = all.get(i);
            int local = i - start;
            int col = local % 2;
            int row = local / 2;
            int rx = panel.x + 26 + col * (colW + 16);
            int ry = panel.y + 62 + row * (cardH + 14);
            Rectangle card = new Rectangle(rx, ry, colW, cardH);

            boolean unlocked = save.unlockedAchievements.contains(a.id);
            g.setColor(unlocked ? (a.specialBadge ? new Color(56, 38, 48) : new Color(38, 52, 68)) : UIUtils.PANEL_DARK);
            g.fillRoundRect(card.x, card.y, card.width, card.height, 14, 14);
            g.setColor(unlocked ? (a.specialBadge ? UIUtils.GOLD : UIUtils.SUCCESS) : UIUtils.HUD_BORDER);
            g.setStroke(new BasicStroke(unlocked ? 2f : 1.5f));
            g.drawRoundRect(card.x, card.y, card.width, card.height, 14, 14);

            if (a.specialBadge && unlocked) {
                drawPixelSprite(g, "objects/hunger_god_badge", card.x + 12, card.y + 14, 36);
            } else {
                g.setColor(unlocked ? UIUtils.SUCCESS : new Color(56, 64, 82));
                g.fillOval(card.x + 14, card.y + 16, 30, 30);
                g.setFont(fTitle);
                g.setColor(unlocked ? new Color(18, 22, 30) : UIUtils.MUTED);
                g.drawString(unlocked ? "OK" : "?", card.x + 24, card.y + 36);
            }

            g.setFont(fTitle);
            g.setColor(unlocked ? (a.specialBadge ? UIUtils.GOLD : UIUtils.TEXT) : UIUtils.MUTED);
            g.drawString(a.title + " (" + a.koreanTitle + ")", card.x + 56, card.y + 30);

            g.setFont(fBody);
            g.setColor(unlocked ? UIUtils.TEXT : UIUtils.MUTED);
            int dy = card.y + 54;
            for (String line : UIUtils.wrapText(a.description, g.getFontMetrics(), card.width - 70)) {
                g.drawString(line, card.x + 56, dy);
                dy += 18;
            }
        }

        if (achPage > 0) {
            UIUtils.drawButton(g, achPrevBtn, "< Prev", fBtn, UIUtils.PANEL_LIGHT, UIUtils.ACCENT, mouse, UIUtils.TEXT, UIUtils.HUD_BORDER);
        }
        if (achPage < totalPages - 1) {
            UIUtils.drawButton(g, achNextBtn, "Next >", fBtn, UIUtils.PANEL_LIGHT, UIUtils.ACCENT, mouse, UIUtils.TEXT, UIUtils.HUD_BORDER);
        }
        UIUtils.drawButton(g, backBtn, "Back to Menu [Esc]", fBtn, UIUtils.ACCENT, UIUtils.ACCENT_HOVER, mouse, UIUtils.TEXT, null);
    }

    private void drawSlotsPanel(Graphics2D g, Point mouse) {
        Rectangle panel = new Rectangle(64, 42, UIUtils.SCREEN_WIDTH - 128, UIUtils.SCREEN_HEIGHT - 84);
        g.setColor(UIUtils.PANEL);
        g.fillRoundRect(panel.x, panel.y, panel.width, panel.height, 20, 20);
        g.setColor(UIUtils.GOLD);
        g.setStroke(new BasicStroke(2f));
        g.drawRoundRect(panel.x, panel.y, panel.width, panel.height, 20, 20);

        Font fHead = assets.getFont(22, true);
        Font fSlot = assets.getFont(17, true);
        Font fBody = assets.getFont(13, false);
        Font fBtn = assets.getFont(14, true);

        g.setFont(fHead);
        g.setColor(UIUtils.GOLD);
        g.drawString("Select Save File (Slots 1–3)", panel.x + 28, panel.y + 42);

        g.setFont(fBody);
        g.setColor(UIUtils.MUTED);
        g.drawString("Click a save slot to play it, or reset any slot to start fresh.", panel.x + 28, panel.y + 66);

        for (int i = 0; i < 3; i++) {
            SaveManager s = slots[i];
            Rectangle card = slotPlayRects[i];
            boolean isActive = (i == activeSlot);
            boolean hoverCard = confirmingResetSlot != i && mouse != null && card.contains(mouse);

            Color cardBg = hoverCard ? new Color(56, 68, 96) : (isActive ? new Color(40, 52, 78) : UIUtils.PANEL_LIGHT);
            g.setColor(cardBg);
            g.fillRoundRect(card.x, card.y, card.width, card.height, 14, 14);
            g.setColor(isActive ? UIUtils.GOLD : UIUtils.HUD_BORDER);
            g.setStroke(new BasicStroke(isActive ? 2.5f : 1.5f));
            g.drawRoundRect(card.x, card.y, card.width, card.height, 14, 14);

            drawPixelSprite(g, getPlayerSpriteKeyForSave(s), card.x + 16, card.y + 19, 64);

            g.setFont(fSlot);
            g.setColor(isActive ? UIUtils.GOLD : UIUtils.TEXT);
            boolean isHungerGod = s.unlockedAchievements.contains(AchievementManager.HUNGER_GOD_ID);
            String title = "Save Slot " + s.slotNumber + (isActive ? " [Active]" : "") + (isHungerGod ? "  ★ HUNGER GOD" : "");
            g.drawString(title, card.x + 96, card.y + 34);

            g.setFont(fBody);
            if (s.hasProgress()) {
                String ch = s.currentLevel == 80 ? "Grand Final" : (s.currentLevel <= 50 ? "Chapter 1" : "Chapter 2");
                g.setColor(UIUtils.TEXT);
                g.drawString(
                    ch + "  •  Level " + s.currentLevel + " / 80  (Unlocked: " + s.unlockedLevel + ")",
                    card.x + 96,
                    card.y + 60
                );
                g.setColor(UIUtils.MUTED);
                g.drawString(
                    "XP: " + s.xp + "   •   Exams: " + s.passedExams.size() + "   •   Ach: " + s.unlockedAchievements.size() + "/" + AchievementManager.getAll().size(),
                    card.x + 96,
                    card.y + 82
                );
            } else {
                g.setColor(UIUtils.MUTED);
                g.drawString("Empty Slot — Click to start a new adventure!", card.x + 96, card.y + 64);
            }

            if (confirmingResetSlot == i) {
                UIUtils.drawButton(g, slotResetRects[i], "Confirm Reset", fBtn, UIUtils.ERROR, new Color(244, 102, 114), mouse, UIUtils.TEXT, null);
                UIUtils.drawButton(g, slotCancelRects[i], "Cancel", fBtn, UIUtils.PANEL_LIGHT, UIUtils.ACCENT, mouse, UIUtils.TEXT, UIUtils.HUD_BORDER);
            } else if (s.hasProgress()) {
                UIUtils.drawButton(g, slotResetRects[i], "Reset Slot", fBtn, UIUtils.PANEL_DARK, UIUtils.ERROR, mouse, UIUtils.TEXT, UIUtils.HUD_BORDER);
            } else {
                g.setColor(new Color(34, 40, 54));
                g.fillRoundRect(slotResetRects[i].x, slotResetRects[i].y, slotResetRects[i].width, slotResetRects[i].height, 14, 14);
                g.setFont(fBody);
                g.setColor(UIUtils.MUTED);
                FontMetrics fm = g.getFontMetrics();
                String emp = "Empty";
                g.drawString(
                    emp,
                    slotResetRects[i].x + (slotResetRects[i].width - fm.stringWidth(emp)) / 2,
                    slotResetRects[i].y + (slotResetRects[i].height - fm.getHeight()) / 2 + fm.getAscent()
                );
            }
        }

        UIUtils.drawButton(g, backBtn, "Back to Menu [Esc]", fBtn, UIUtils.ACCENT, UIUtils.ACCENT_HOVER, mouse, UIUtils.TEXT, null);
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
            "Explore 80 pixel-art zones across 2 chapters and the Grand Final Exam Hall, talk to locals, inspect everyday objects, take revision exams, and unlock achievements.",
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
            "• Talk / Inspect / Enter Exam: E, Space, Enter, or Click directly on any character or icon",
            "• Level Guide: Tab   |   Level Select: L",
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
        g.drawString("Rewards & Achievements", x, y);
        y += 24;

        String[] features = {
            "• Crowns: Finish Chapter 1 for a Small Crown, and Chapter 2 for a Royal Crown.",
            "• Hunger God Badge: Score a perfect 100% on the Level 80 Grand Final Exam!",
            "• 3 Save Slots: Switch or reset up to 3 independent save files anytime from the Main Menu."
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

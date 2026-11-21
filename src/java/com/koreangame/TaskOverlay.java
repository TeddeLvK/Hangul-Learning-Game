package com.koreangame;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Stroke;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class TaskOverlay {
    private static class WordHit {
        final Rectangle rect;
        final String tooltip;

        WordHit(Rectangle rect, String tooltip) {
            this.rect = rect;
            this.tooltip = tooltip;
        }
    }

    public final Entity entity;
    private final AssetLoader assets;
    private final Glossary glossary;
    private int index = 0;

    private List<String> shuffledOptions = new ArrayList<>();
    private final List<Rectangle> optionRects = new ArrayList<>();

    private final List<String> bankTokens = new ArrayList<>();
    private final List<String> selectedTokens = new ArrayList<>();
    private final List<Rectangle> bankRects = new ArrayList<>();
    private final List<Rectangle> selectedRects = new ArrayList<>();
    private final List<WordHit> promptHits = new ArrayList<>();

    private Boolean feedbackOk = null;
    private String feedbackMsg = "";

    private final Rectangle closeBtn = new Rectangle(UIUtils.SCREEN_WIDTH - 102, 50, 36, 36);
    private final Rectangle clearBtn = new Rectangle(UIUtils.SCREEN_WIDTH / 2 - 154, UIUtils.SCREEN_HEIGHT - 100, 142, 42);
    private final Rectangle checkBtn = new Rectangle(UIUtils.SCREEN_WIDTH / 2 + 12, UIUtils.SCREEN_HEIGHT - 100, 142, 42);
    private final Rectangle nextBtn = new Rectangle(UIUtils.SCREEN_WIDTH / 2 - 100, UIUtils.SCREEN_HEIGHT - 100, 200, 42);

    public TaskOverlay(Entity entity, AssetLoader assets, Glossary glossary) {
        this.entity = entity;
        this.assets = assets;
        this.glossary = glossary;
        setupTask();
    }

    private Task currentTask() {
        return entity.tasks.get(index);
    }

    private void setupTask() {
        feedbackOk = null;
        feedbackMsg = "";
        Task task = currentTask();
        if (task.type.equals("build")) {
            bankTokens.clear();
            selectedTokens.clear();
            bankTokens.addAll(task.tokens);
            bankTokens.addAll(task.distractors);
            Collections.shuffle(bankTokens);
        } else {
            shuffledOptions = new ArrayList<>(task.options);
            Collections.shuffle(shuffledOptions);
        }
    }

    private void submitChoice(String chosen) {
        Task t = currentTask();
        feedbackOk = chosen.equals(t.answer);
        feedbackMsg = t.explanation;
    }

    private void submitBuild() {
        Task t = currentTask();
        boolean ok = selectedTokens.equals(t.tokens);
        feedbackOk = ok;
        feedbackMsg = ok ? t.explanation : "Not quite! Hint: " + t.explanation;
    }

    private String advanceAfterFeedback() {
        if (!Boolean.TRUE.equals(feedbackOk)) {
            feedbackOk = null;
            return null;
        }
        index++;
        if (index >= entity.tasks.size()) {
            return "completed";
        }
        setupTask();
        return null;
    }

    public String onKeyPressed(int keyCode) {
        if (keyCode == KeyEvent.VK_ESCAPE) {
            return "close";
        }
        if (feedbackOk != null) {
            if (keyCode == KeyEvent.VK_ENTER || keyCode == KeyEvent.VK_SPACE) {
                return advanceAfterFeedback();
            }
            return null;
        }
        Task t = currentTask();
        if (!t.type.equals("build")) {
            int idx = -1;
            if (keyCode == KeyEvent.VK_1) idx = 0;
            else if (keyCode == KeyEvent.VK_2) idx = 1;
            else if (keyCode == KeyEvent.VK_3) idx = 2;
            else if (keyCode == KeyEvent.VK_4) idx = 3;
            if (idx >= 0 && idx < shuffledOptions.size()) {
                submitChoice(shuffledOptions.get(idx));
            }
        }
        return null;
    }

    public String onMouseClicked(Point p) {
        if (closeBtn.contains(p)) {
            return "close";
        }
        if (feedbackOk != null) {
            if (nextBtn.contains(p)) {
                return advanceAfterFeedback();
            }
            return null;
        }
        Task t = currentTask();
        if (t.type.equals("build")) {
            for (int i = 0; i < bankRects.size(); i++) {
                if (bankRects.get(i).contains(p)) {
                    selectedTokens.add(bankTokens.remove(i));
                    return null;
                }
            }
            for (int i = 0; i < selectedRects.size(); i++) {
                if (selectedRects.get(i).contains(p)) {
                    bankTokens.add(selectedTokens.remove(i));
                    return null;
                }
            }
            if (clearBtn.contains(p)) {
                bankTokens.addAll(selectedTokens);
                selectedTokens.clear();
                return null;
            }
            if (checkBtn.contains(p) && !selectedTokens.isEmpty()) {
                submitBuild();
                return null;
            }
        } else {
            for (int i = 0; i < optionRects.size(); i++) {
                if (optionRects.get(i).contains(p)) {
                    submitChoice(shuffledOptions.get(i));
                    return null;
                }
            }
        }
        return null;
    }

    private int drawHoverablePrompt(Graphics2D g, String prompt, Font font, int startX, int startY, int maxWidth) {
        promptHits.clear();
        g.setFont(font);
        FontMetrics fm = g.getFontMetrics();
        int spaceW = fm.stringWidth(" ");
        int x = startX;
        int y = startY;
        int lineH = 30;

        Stroke prevStroke = g.getStroke();
        Stroke dotted = new BasicStroke(1.5f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0f, new float[]{3f, 3f}, 0f);

        String[] words = prompt.split(" ");
        for (String w : words) {
            int ww = fm.stringWidth(w);
            if (x > startX && x + ww > startX + maxWidth) {
                x = startX;
                y += lineH;
            }
            String tip = glossary != null ? glossary.lookupWord(w) : null;
            g.setColor(tip != null ? new Color(166, 214, 255) : UIUtils.TEXT);
            g.drawString(w, x, y);
            if (tip != null) {
                g.setColor(UIUtils.GOLD);
                g.setStroke(dotted);
                g.drawLine(x, y + 3, x + ww, y + 3);
                g.setStroke(prevStroke);
                promptHits.add(new WordHit(new Rectangle(x - 2, y - fm.getAscent(), ww + 4, fm.getHeight() + 4), tip));
            }
            x += ww + spaceW;
        }
        return y + lineH;
    }

    private String findHoverTooltip(Point mouse, Task task) {
        if (mouse == null || glossary == null) {
            return null;
        }
        for (WordHit wh : promptHits) {
            if (wh.rect.contains(mouse)) {
                return wh.tooltip;
            }
        }
        if (task.type.equals("build")) {
            for (int i = 0; i < bankRects.size(); i++) {
                if (bankRects.get(i).contains(mouse)) {
                    return glossary.lookupPhrase(bankTokens.get(i));
                }
            }
            for (int i = 0; i < selectedRects.size(); i++) {
                if (selectedRects.get(i).contains(mouse)) {
                    return glossary.lookupPhrase(selectedTokens.get(i));
                }
            }
        } else {
            for (int i = 0; i < optionRects.size(); i++) {
                if (optionRects.get(i).contains(mouse)) {
                    return glossary.lookupPhrase(shuffledOptions.get(i));
                }
            }
        }
        return null;
    }

    private void drawTooltip(Graphics2D g, Point mouse, String text) {
        Font fTip = assets.getFont(13, true);
        g.setFont(fTip);
        FontMetrics fm = g.getFontMetrics();
        int padX = 12;
        int tw = fm.stringWidth(text) + padX * 2;
        int th = fm.getHeight() + 12;
        int tx = mouse.x + 14;
        int ty = mouse.y - th - 10;
        if (tx + tw > UIUtils.SCREEN_WIDTH - 12) {
            tx = UIUtils.SCREEN_WIDTH - tw - 12;
        }
        if (ty < 12) {
            ty = mouse.y + 20;
        }
        g.setColor(new Color(16, 20, 32, 245));
        g.fillRoundRect(tx, ty, tw, th, 10, 10);
        g.setColor(UIUtils.GOLD);
        g.setStroke(new BasicStroke(1.5f));
        g.drawRoundRect(tx, ty, tw, th, 10, 10);
        g.setColor(UIUtils.TEXT);
        g.drawString(text, tx + padX, ty + fm.getAscent() + 6);
    }

    public void draw(Graphics2D g, Point mouse) {
        g.setColor(UIUtils.DIM);
        g.fillRect(0, 0, UIUtils.SCREEN_WIDTH, UIUtils.SCREEN_HEIGHT);

        Rectangle panel = new Rectangle(54, 38, UIUtils.SCREEN_WIDTH - 108, UIUtils.SCREEN_HEIGHT - 76);
        g.setColor(UIUtils.PANEL);
        g.fillRoundRect(panel.x, panel.y, panel.width, panel.height, 20, 20);
        g.setColor(UIUtils.ACCENT);
        g.setStroke(new BasicStroke(2f));
        g.drawRoundRect(panel.x, panel.y, panel.width, panel.height, 20, 20);

        Font fName = assets.getFont(20, true);
        Font fGreet = assets.getFont(14, false);
        Font fPrompt = assets.getFont(18, true);
        Font fOpt = assets.getFont(15, true);
        Font fBody = assets.getFont(14, false);
        Font fHint = assets.getFont(12, false);

        BufferedImage portrait = assets.getImage(entity.spriteKey);
        if (portrait != null) {
            Object prevInterp = g.getRenderingHint(RenderingHints.KEY_INTERPOLATION);
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
            g.drawImage(portrait, panel.x + 24, panel.y + 18, 56, 56, null);
            if (prevInterp != null) {
                g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, prevInterp);
            }
        } else {
            g.setColor(UIUtils.ACCENT);
            g.fillRoundRect(panel.x + 24, panel.y + 18, 56, 56, 12, 12);
            g.setFont(fName);
            g.setColor(UIUtils.TEXT);
            g.drawString(entity.name.substring(0, 1), panel.x + 44, panel.y + 54);
        }

        g.setFont(fName);
        g.setColor(UIUtils.GOLD);
        g.drawString(entity.name + "   (" + (index + 1) + "/" + entity.tasks.size() + ")", panel.x + 94, panel.y + 40);

        g.setFont(fGreet);
        g.setColor(UIUtils.MUTED);
        g.drawString(entity.greeting, panel.x + 94, panel.y + 66);

        UIUtils.drawButton(g, closeBtn, "X", fOpt, UIUtils.PANEL_LIGHT, UIUtils.ERROR, mouse, UIUtils.TEXT, null);

        g.setColor(UIUtils.HUD_BORDER);
        g.drawLine(panel.x + 24, panel.y + 88, panel.x + panel.width - 24, panel.y + 88);

        g.setFont(fHint);
        g.setColor(UIUtils.MUTED);
        g.drawString("Tip: Hover underlined nouns or word tiles to see their English meaning.", panel.x + 28, panel.y + 106);

        Task task = currentTask();
        int y = drawHoverablePrompt(g, task.prompt, fPrompt, panel.x + 28, panel.y + 134, panel.width - 56);
        y += 12;

        if (!task.type.equals("build")) {
            optionRects.clear();
            int colW = (panel.width - 76) / 2;
            for (int i = 0; i < shuffledOptions.size(); i++) {
                int col = i % 2;
                int row = i / 2;
                int rx = panel.x + 28 + col * (colW + 20);
                int ry = y + row * 66;
                Rectangle r = new Rectangle(rx, ry, colW, 52);
                optionRects.add(r);
                UIUtils.drawButton(
                    g, r, (i + 1) + ".  " + shuffledOptions.get(i), fOpt,
                    UIUtils.PANEL_LIGHT, UIUtils.ACCENT, mouse, UIUtils.TEXT, UIUtils.HUD_BORDER
                );
            }
        } else {
            Rectangle ansBox = new Rectangle(panel.x + 28, y, panel.width - 56, 74);
            g.setColor(UIUtils.PANEL_DARK);
            g.fillRoundRect(ansBox.x, ansBox.y, ansBox.width, ansBox.height, 14, 14);
            g.setColor(UIUtils.ACCENT);
            g.drawRoundRect(ansBox.x, ansBox.y, ansBox.width, ansBox.height, 14, 14);

            selectedRects.clear();
            g.setFont(fOpt);
            FontMetrics fmOpt = g.getFontMetrics();
            int tx = ansBox.x + 14;
            int ty = ansBox.y + 16;
            if (selectedTokens.isEmpty()) {
                g.setFont(fBody);
                g.setColor(UIUtils.MUTED);
                g.drawString("Click word tiles below in order to build the Korean sentence...", tx, ty + 24);
            } else {
                for (String tok : selectedTokens) {
                    int tw = Math.max(72, fmOpt.stringWidth(tok) + 28);
                    Rectangle r = new Rectangle(tx, ty, tw, 42);
                    selectedRects.add(r);
                    UIUtils.drawButton(g, r, tok, fOpt, UIUtils.ACCENT, UIUtils.ERROR, mouse, UIUtils.TEXT, null);
                    tx += tw + 10;
                }
            }

            int bankY = ansBox.y + ansBox.height + 30;
            g.setFont(fBody);
            g.setColor(UIUtils.MUTED);
            g.drawString("Word Bank (click to add):", panel.x + 28, bankY);
            bankY += 14;

            bankRects.clear();
            int bx = panel.x + 28;
            int by = bankY;
            for (String tok : bankTokens) {
                int tw = Math.max(78, fmOpt.stringWidth(tok) + 30);
                if (bx + tw > panel.x + panel.width - 28) {
                    bx = panel.x + 28;
                    by += 50;
                }
                Rectangle r = new Rectangle(bx, by, tw, 40);
                bankRects.add(r);
                UIUtils.drawButton(g, r, tok, fOpt, UIUtils.PANEL_LIGHT, UIUtils.ACCENT, mouse, UIUtils.TEXT, UIUtils.HUD_BORDER);
                bx += tw + 12;
            }

            if (feedbackOk == null) {
                UIUtils.drawButton(g, clearBtn, "Clear", fOpt, UIUtils.PANEL_LIGHT, UIUtils.ERROR, mouse, UIUtils.TEXT, UIUtils.HUD_BORDER);
                UIUtils.drawButton(g, checkBtn, "Check Answer", fOpt, UIUtils.SUCCESS, UIUtils.SUCCESS_HOVER, mouse, UIUtils.TEXT, null);
            }
        }

        if (feedbackOk != null) {
            Color fbCol = feedbackOk ? UIUtils.SUCCESS : UIUtils.ERROR;
            Rectangle fbBox = new Rectangle(panel.x + 28, nextBtn.y - 84, panel.width - 56, 70);
            g.setColor(UIUtils.PANEL_DARK);
            g.fillRoundRect(fbBox.x, fbBox.y, fbBox.width, fbBox.height, 14, 14);
            g.setColor(fbCol);
            g.drawRoundRect(fbBox.x, fbBox.y, fbBox.width, fbBox.height, 14, 14);

            g.setFont(fOpt);
            g.setColor(fbCol);
            g.drawString(feedbackOk ? "Correct! (+20 XP)" : "Try Again", fbBox.x + 14, fbBox.y + 24);

            g.setFont(fBody);
            g.setColor(UIUtils.TEXT);
            int fy = fbBox.y + 46;
            List<String> lines = UIUtils.wrapText(feedbackMsg, g.getFontMetrics(), fbBox.width - 28);
            for (int i = 0; i < Math.min(2, lines.size()); i++) {
                g.drawString(lines.get(i), fbBox.x + 14, fy);
                fy += 18;
            }

            String btnLbl = feedbackOk ? "Continue [Enter]" : "Try Again [Enter]";
            UIUtils.drawButton(g, nextBtn, btnLbl, fOpt, fbCol, fbCol, mouse, UIUtils.TEXT, null);
        }

        String hoverTip = findHoverTooltip(mouse, task);
        if (hoverTip != null) {
            drawTooltip(g, mouse, hoverTip);
        }
    }
}

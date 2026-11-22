package com.koreangame;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

public class MiniGameOverlay {
    public static final String MODE_LETTERS = "letters";
    public static final String MODE_VOCAB = "vocab";
    public static final String MODE_WRITING = "writing";

    private static final int ROUND_SIZE = 10;

    private static final String[][] HANGUL_LETTERS = {
        {"ㄱ", "g / k (giyeok)"}, {"ㄴ", "n (nieun)"}, {"ㄷ", "d / t (digeut)"},
        {"ㄹ", "r / l (rieul)"}, {"ㅁ", "m (mieum)"}, {"ㅂ", "b / p (bieup)"},
        {"ㅅ", "s (siot)"}, {"ㅇ", "silent / ng (ieung)"}, {"ㅈ", "j (jieut)"},
        {"ㅊ", "ch (chieut)"}, {"ㅋ", "k (kieuk)"}, {"ㅌ", "t (tieut)"},
        {"ㅍ", "p (pieup)"}, {"ㅎ", "h (hieut)"},
        {"ㄲ", "kk (ssang-giyeok)"}, {"ㄸ", "tt (ssang-digeut)"},
        {"ㅃ", "pp (ssang-bieup)"}, {"ㅆ", "ss (ssang-siot)"}, {"ㅉ", "jj (ssang-jieut)"},
        {"ㅏ", "a (as in father)"}, {"ㅑ", "ya"}, {"ㅓ", "eo (as in awe)"},
        {"ㅕ", "yeo"}, {"ㅗ", "o"}, {"ㅛ", "yo"},
        {"ㅜ", "u (as in moon)"}, {"ㅠ", "yu"}, {"ㅡ", "eu"},
        {"ㅣ", "i (as in tree)"}, {"ㅐ", "ae"}, {"ㅔ", "e"},
        {"ㅘ", "wa"}, {"ㅝ", "wo"}, {"ㅢ", "ui / eui"}
    };

    private static final char[] ROW1_NORMAL = {'ㅂ','ㅈ','ㄷ','ㄱ','ㅅ','ㅛ','ㅕ','ㅑ','ㅐ','ㅔ'};
    private static final char[] ROW1_SHIFT  = {'ㅃ','ㅉ','ㄸ','ㄲ','ㅆ','ㅛ','ㅕ','ㅑ','ㅒ','ㅖ'};
    private static final char[] ROW2_KEYS   = {'ㅁ','ㄴ','ㅇ','ㄹ','ㅎ','ㅗ','ㅓ','ㅏ','ㅣ'};
    private static final char[] ROW3_KEYS   = {'ㅋ','ㅌ','ㅊ','ㅍ','ㅠ','ㅜ','ㅡ'};

    private static class Question {
        final String prompt;
        final String targetKorean;
        final String targetEnglish;
        final List<String> options;
        final String answer;

        Question(String prompt, String targetKorean, String targetEnglish, List<String> options, String answer) {
            this.prompt = prompt;
            this.targetKorean = targetKorean;
            this.targetEnglish = targetEnglish;
            this.options = options;
            this.answer = answer;
        }
    }

    private final String mode;
    private final AssetLoader assets;
    private final List<Level> levels;
    private final Glossary glossary;
    private final List<Question> questions = new ArrayList<>();

    private int index = 0;
    private int score = 0;
    private boolean answered = false;
    private boolean lastCorrect = false;
    private String lastFeedback = "";
    private boolean finished = false;
    private boolean hintShown = false;

    private final HangulComposer composer = new HangulComposer();
    private boolean shiftActive = false;

    private final List<Rectangle> optionRects = new ArrayList<>();
    private final List<Rectangle> keyRects = new ArrayList<>();
    private final List<Character> keyChars = new ArrayList<>();

    private final Rectangle closeBtn = new Rectangle(UIUtils.SCREEN_WIDTH - 96, 40, 36, 36);
    private final Rectangle nextBtn = new Rectangle(UIUtils.SCREEN_WIDTH / 2 - 95, UIUtils.SCREEN_HEIGHT - 86, 190, 42);
    private final Rectangle replayBtn = new Rectangle(UIUtils.SCREEN_WIDTH / 2 - 190, UIUtils.SCREEN_HEIGHT - 120, 175, 44);
    private final Rectangle menuBtn = new Rectangle(UIUtils.SCREEN_WIDTH / 2 + 15, UIUtils.SCREEN_HEIGHT - 120, 175, 44);

    private Rectangle shiftRect = new Rectangle();
    private Rectangle backRect = new Rectangle();
    private Rectangle clearRect = new Rectangle();
    private Rectangle spaceRect = new Rectangle();
    private Rectangle hintRect = new Rectangle();
    private Rectangle submitRect = new Rectangle();

    public MiniGameOverlay(String mode, List<Level> levels, Glossary glossary, AssetLoader assets) {
        this.mode = mode;
        this.levels = levels;
        this.glossary = glossary;
        this.assets = assets;
        buildRound();
    }

    public int getScore() {
        return score;
    }

    private void buildRound() {
        questions.clear();
        index = 0;
        score = 0;
        answered = false;
        finished = false;
        hintShown = false;
        shiftActive = false;
        composer.clear();

        Random rng = new Random();
        if (MODE_LETTERS.equals(mode)) {
            buildLetterRound(rng);
        } else if (MODE_VOCAB.equals(mode)) {
            buildVocabRound(rng);
        } else {
            buildWritingRound(rng);
        }
    }

    private void buildLetterRound(Random rng) {
        List<String[]> pool = new ArrayList<>();
        Collections.addAll(pool, HANGUL_LETTERS);
        Collections.shuffle(pool, rng);
        int count = Math.min(ROUND_SIZE, pool.size());
        for (int i = 0; i < count; i++) {
            String[] item = pool.get(i);
            boolean askSound = (i % 2 == 0);
            String correct = askSound ? item[1] : item[0];
            List<String> opts = new ArrayList<>();
            opts.add(correct);
            List<String[]> distractors = new ArrayList<>(pool);
            Collections.shuffle(distractors, rng);
            for (String[] d : distractors) {
                if (opts.size() >= 4) break;
                String val = askSound ? d[1] : d[0];
                if (!opts.contains(val)) opts.add(val);
            }
            Collections.shuffle(opts, rng);
            String prompt = askSound
                ? "What sound does the letter  " + item[0] + "  make?"
                : "Which Hangul letter makes the sound  \"" + item[1] + "\"?";
            questions.add(new Question(prompt, item[0], item[1], opts, correct));
        }
    }

    private List<String[]> collectVocab(boolean writingOnly) {
        List<String[]> out = new ArrayList<>();
        Set<String> seen = new HashSet<>();
        for (Level lv : levels) {
            if (lv.id <= 79) {
                for (String[] kv : lv.vocab) {
                    if (kv.length == 2 && isValidWord(kv[0], writingOnly) && seen.add(kv[0])) {
                        out.add(kv);
                    }
                }
            }
        }
        if (glossary != null) {
            for (Map.Entry<String, String> e : glossary.getAllEntries().entrySet()) {
                if (isValidWord(e.getKey(), writingOnly) && seen.add(e.getKey())) {
                    out.add(new String[]{e.getKey(), e.getValue()});
                }
            }
        }
        return out;
    }

    private boolean isValidWord(String kr, boolean writingOnly) {
        if (kr == null || kr.isEmpty()) return false;
        if (!writingOnly) return true;
        if (kr.length() > 6) return false;
        for (int i = 0; i < kr.length(); i++) {
            char ch = kr.charAt(i);
            boolean isHangul = (ch >= 0xAC00 && ch <= 0xD7A3);
            if (!isHangul && ch != ' ') return false;
        }
        return true;
    }

    private void buildVocabRound(Random rng) {
        List<String[]> pool = collectVocab(false);
        Collections.shuffle(pool, rng);
        int count = Math.min(ROUND_SIZE, pool.size());
        for (int i = 0; i < count; i++) {
            String[] pair = pool.get(i);
            boolean krToEn = (i % 2 == 0);
            String correct = krToEn ? pair[1] : pair[0];
            List<String> opts = new ArrayList<>();
            opts.add(correct);
            List<String[]> copy = new ArrayList<>(pool);
            Collections.shuffle(copy, rng);
            for (String[] c : copy) {
                if (opts.size() >= 4) break;
                String val = krToEn ? c[1] : c[0];
                if (!opts.contains(val)) opts.add(val);
            }
            Collections.shuffle(opts, rng);
            String prompt = krToEn
                ? "What does  \"" + pair[0] + "\"  mean in English?"
                : "Which Korean word means  \"" + pair[1] + "\"?";
            questions.add(new Question(prompt, pair[0], pair[1], opts, correct));
        }
    }

    private void buildWritingRound(Random rng) {
        List<String[]> pool = collectVocab(true);
        Collections.shuffle(pool, rng);
        int count = Math.min(ROUND_SIZE, pool.size());
        for (int i = 0; i < count; i++) {
            String[] pair = pool.get(i);
            String prompt = "Type in Korean:  \"" + pair[1] + "\"";
            questions.add(new Question(prompt, pair[0], pair[1], Collections.emptyList(), pair[0]));
        }
    }

    private Question current() {
        return questions.get(index);
    }

    private void submitChoice(String chosen) {
        Question q = current();
        answered = true;
        lastCorrect = chosen.equals(q.answer);
        if (lastCorrect) {
            score++;
            lastFeedback = "Correct!  " + q.targetKorean + "  =  " + q.targetEnglish;
        } else {
            lastFeedback = "Not quite!  Correct answer: " + q.answer + "  (" + q.targetKorean + " = " + q.targetEnglish + ")";
        }
    }

    private void submitWriting() {
        String typed = composer.getText().trim();
        if (typed.isEmpty()) return;
        Question q = current();
        answered = true;
        lastCorrect = typed.equals(q.answer.trim());
        if (lastCorrect) {
            score++;
            lastFeedback = "Perfect!  " + q.answer + "  =  " + q.targetEnglish;
        } else {
            lastFeedback = "You typed: " + typed + "   |   Correct: " + q.answer;
        }
    }

    private void advance() {
        index++;
        answered = false;
        hintShown = false;
        shiftActive = false;
        composer.clear();
        if (index >= questions.size()) {
            finished = true;
        }
    }

    public String onKeyPressed(int keyCode) {
        if (keyCode == KeyEvent.VK_ESCAPE) {
            return "close";
        }
        if (finished) {
            if (keyCode == KeyEvent.VK_ENTER) return "close";
            return null;
        }
        if (answered) {
            if (keyCode == KeyEvent.VK_ENTER || keyCode == KeyEvent.VK_SPACE) {
                advance();
            }
            return null;
        }
        if (MODE_WRITING.equals(mode)) {
            if (keyCode == KeyEvent.VK_BACK_SPACE) {
                composer.backspace();
            } else if (keyCode == KeyEvent.VK_SPACE) {
                composer.typeSpace();
            } else if (keyCode == KeyEvent.VK_ENTER) {
                submitWriting();
            }
            return null;
        }
        int optIdx = -1;
        if (keyCode == KeyEvent.VK_1) optIdx = 0;
        else if (keyCode == KeyEvent.VK_2) optIdx = 1;
        else if (keyCode == KeyEvent.VK_3) optIdx = 2;
        else if (keyCode == KeyEvent.VK_4) optIdx = 3;
        if (optIdx >= 0 && optIdx < current().options.size()) {
            submitChoice(current().options.get(optIdx));
        }
        return null;
    }

    public String onMouseClicked(Point p) {
        if (closeBtn.contains(p)) {
            return "close";
        }
        if (finished) {
            if (replayBtn.contains(p)) {
                buildRound();
                return null;
            }
            if (menuBtn.contains(p)) {
                return "close";
            }
            return null;
        }
        if (answered) {
            if (nextBtn.contains(p)) {
                advance();
            }
            return null;
        }
        if (MODE_WRITING.equals(mode)) {
            for (int i = 0; i < keyRects.size(); i++) {
                if (keyRects.get(i).contains(p)) {
                    composer.typeJamo(keyChars.get(i));
                    shiftActive = false;
                    return null;
                }
            }
            if (shiftRect.contains(p)) {
                shiftActive = !shiftActive;
                return null;
            }
            if (backRect.contains(p)) {
                composer.backspace();
                return null;
            }
            if (clearRect.contains(p)) {
                composer.clear();
                return null;
            }
            if (spaceRect.contains(p)) {
                composer.typeSpace();
                return null;
            }
            if (hintRect.contains(p)) {
                hintShown = true;
                return null;
            }
            if (submitRect.contains(p)) {
                submitWriting();
                return null;
            }
            return null;
        }

        for (int i = 0; i < optionRects.size(); i++) {
            if (optionRects.get(i).contains(p)) {
                submitChoice(current().options.get(i));
                return null;
            }
        }
        return null;
    }

    private String getTitle() {
        if (MODE_LETTERS.equals(mode)) return "Mini Game 1: Hangul Letters (한글 자모)";
        if (MODE_VOCAB.equals(mode)) return "Mini Game 2: Vocabulary Drill (단어 연습)";
        return "Mini Game 3: Korean Writing (단어 쓰기)";
    }

    public void draw(Graphics2D g, Point mouse) {
        g.setColor(UIUtils.DIM);
        g.fillRect(0, 0, UIUtils.SCREEN_WIDTH, UIUtils.SCREEN_HEIGHT);

        Rectangle panel = new Rectangle(48, 28, UIUtils.SCREEN_WIDTH - 96, UIUtils.SCREEN_HEIGHT - 56);
        g.setColor(UIUtils.PANEL);
        g.fillRoundRect(panel.x, panel.y, panel.width, panel.height, 20, 20);
        g.setColor(UIUtils.GOLD);
        g.setStroke(new BasicStroke(2.5f));
        g.drawRoundRect(panel.x, panel.y, panel.width, panel.height, 20, 20);

        Font fHead = assets.getFont(20, true);
        Font fSub = assets.getFont(13, false);
        Font fPrompt = assets.getFont(20, true);
        Font fOpt = assets.getFont(16, true);
        Font fKey = assets.getFont(18, true);
        Font fBtn = assets.getFont(14, true);

        g.setFont(fHead);
        g.setColor(UIUtils.GOLD);
        g.drawString(getTitle(), panel.x + 24, panel.y + 38);

        UIUtils.drawButton(g, closeBtn, "X", fBtn, UIUtils.PANEL_LIGHT, UIUtils.ERROR, mouse, UIUtils.TEXT, null);

        if (finished) {
            drawSummary(g, panel, mouse, fHead, fPrompt, fBtn);
            return;
        }

        g.setFont(fSub);
        g.setColor(UIUtils.MUTED);
        g.drawString("Question " + (index + 1) + " of " + questions.size() + "   •   Score: " + score, panel.x + 24, panel.y + 62);

        Rectangle barBg = new Rectangle(panel.x + 24, panel.y + 72, panel.width - 48, 8);
        g.setColor(UIUtils.PANEL_DARK);
        g.fillRoundRect(barBg.x, barBg.y, barBg.width, barBg.height, 6, 6);
        int fillW = (int) Math.round(barBg.width * ((double) index / Math.max(1, questions.size())));
        g.setColor(UIUtils.ACCENT);
        g.fillRoundRect(barBg.x, barBg.y, Math.max(8, fillW), barBg.height, 6, 6);

        Question q = current();
        if (MODE_WRITING.equals(mode)) {
            drawWritingMode(g, panel, q, mouse, fPrompt, fKey, fBtn, fSub);
        } else {
            drawChoiceMode(g, panel, q, mouse, fPrompt, fOpt, fBtn);
        }
    }

    private void drawChoiceMode(Graphics2D g, Rectangle panel, Question q, Point mouse, Font fPrompt, Font fOpt, Font fBtn) {
        g.setFont(fPrompt);
        g.setColor(UIUtils.TEXT);
        int y = panel.y + 130;
        for (String line : UIUtils.wrapText(q.prompt, g.getFontMetrics(), panel.width - 48)) {
            g.drawString(line, panel.x + 24, y);
            y += 30;
        }

        y += 20;
        optionRects.clear();
        int colW = (panel.width - 68) / 2;
        for (int i = 0; i < q.options.size(); i++) {
            int col = i % 2;
            int row = i / 2;
            Rectangle r = new Rectangle(panel.x + 24 + col * (colW + 20), y + row * 78, colW, 62);
            optionRects.add(r);
            Color bg = UIUtils.PANEL_LIGHT;
            Color hover = UIUtils.ACCENT;
            if (answered) {
                if (q.options.get(i).equals(q.answer)) {
                    bg = UIUtils.SUCCESS;
                    hover = UIUtils.SUCCESS;
                } else {
                    bg = UIUtils.PANEL_DARK;
                    hover = UIUtils.PANEL_DARK;
                }
            }
            UIUtils.drawButton(g, r, (i + 1) + ".  " + q.options.get(i), fOpt, bg, hover, answered ? null : mouse, UIUtils.TEXT, UIUtils.HUD_BORDER);
        }

        if (answered) {
            Rectangle fb = new Rectangle(panel.x + 24, panel.y + panel.height - 140, panel.width - 48, 52);
            g.setColor(UIUtils.PANEL_DARK);
            g.fillRoundRect(fb.x, fb.y, fb.width, fb.height, 12, 12);
            g.setColor(lastCorrect ? UIUtils.SUCCESS : UIUtils.ERROR);
            g.setStroke(new BasicStroke(2f));
            g.drawRoundRect(fb.x, fb.y, fb.width, fb.height, 12, 12);
            g.setFont(assets.getFont(15, true));
            g.drawString(lastFeedback, fb.x + 16, fb.y + 32);
            UIUtils.drawButton(g, nextBtn, index + 1 < questions.size() ? "Next [Enter]" : "See Results", fBtn, UIUtils.SUCCESS, UIUtils.SUCCESS_HOVER, mouse, UIUtils.TEXT, null);
        }
    }

    private void drawWritingMode(Graphics2D g, Rectangle panel, Question q, Point mouse, Font fPrompt, Font fKey, Font fBtn, Font fSub) {
        g.setFont(fPrompt);
        g.setColor(UIUtils.TEXT);
        g.drawString(q.prompt, panel.x + 24, panel.y + 122);

        if (hintShown && !q.answer.isEmpty()) {
            g.setFont(fSub);
            g.setColor(UIUtils.GOLD);
            g.drawString("Hint: starts with \"" + q.answer.charAt(0) + "\"  (" + q.answer.length() + " syllable" + (q.answer.length() == 1 ? "" : "s") + ")", panel.x + 24, panel.y + 148);
        }

        Rectangle inputBox = new Rectangle(panel.x + 24, panel.y + 162, panel.width - 48, 58);
        g.setColor(UIUtils.PANEL_DARK);
        g.fillRoundRect(inputBox.x, inputBox.y, inputBox.width, inputBox.height, 14, 14);
        g.setColor(answered ? (lastCorrect ? UIUtils.SUCCESS : UIUtils.ERROR) : UIUtils.ACCENT);
        g.setStroke(new BasicStroke(2f));
        g.drawRoundRect(inputBox.x, inputBox.y, inputBox.width, inputBox.height, 14, 14);

        String typed = composer.getText();
        Font fInput = assets.getFont(24, true);
        g.setFont(fInput);
        if (typed.isEmpty() && !answered) {
            g.setFont(fSub);
            g.setColor(UIUtils.MUTED);
            g.drawString("Tap the Korean phone keyboard below to type...", inputBox.x + 18, inputBox.y + 35);
        } else {
            g.setColor(UIUtils.TEXT);
            String display = answered ? typed : typed + "|";
            g.drawString(display, inputBox.x + 18, inputBox.y + 38);
        }

        if (answered) {
            Rectangle fb = new Rectangle(panel.x + 24, panel.y + 240, panel.width - 48, 60);
            g.setColor(UIUtils.PANEL_DARK);
            g.fillRoundRect(fb.x, fb.y, fb.width, fb.height, 12, 12);
            g.setColor(lastCorrect ? UIUtils.SUCCESS : UIUtils.ERROR);
            g.setStroke(new BasicStroke(2f));
            g.drawRoundRect(fb.x, fb.y, fb.width, fb.height, 12, 12);
            g.setFont(assets.getFont(16, true));
            g.drawString(lastFeedback, fb.x + 18, fb.y + 36);
            UIUtils.drawButton(g, nextBtn, index + 1 < questions.size() ? "Next [Enter]" : "See Results", fBtn, UIUtils.SUCCESS, UIUtils.SUCCESS_HOVER, mouse, UIUtils.TEXT, null);
            return;
        }

        keyRects.clear();
        keyChars.clear();
        int kw = 66;
        int kh = 54;
        int gap = 6;
        int kbY = panel.y + 244;

        char[] r1 = shiftActive ? ROW1_SHIFT : ROW1_NORMAL;
        int r1StartX = panel.x + (panel.width - (r1.length * kw + (r1.length - 1) * gap)) / 2;
        for (int i = 0; i < r1.length; i++) {
            Rectangle r = new Rectangle(r1StartX + i * (kw + gap), kbY, kw, kh);
            keyRects.add(r);
            keyChars.add(r1[i]);
            UIUtils.drawButton(g, r, String.valueOf(r1[i]), fKey, UIUtils.PANEL_LIGHT, UIUtils.ACCENT, mouse, UIUtils.TEXT, UIUtils.HUD_BORDER);
        }

        int r2Y = kbY + kh + 10;
        int r2StartX = panel.x + (panel.width - (ROW2_KEYS.length * kw + (ROW2_KEYS.length - 1) * gap)) / 2;
        for (int i = 0; i < ROW2_KEYS.length; i++) {
            Rectangle r = new Rectangle(r2StartX + i * (kw + gap), r2Y, kw, kh);
            keyRects.add(r);
            keyChars.add(ROW2_KEYS[i]);
            UIUtils.drawButton(g, r, String.valueOf(ROW2_KEYS[i]), fKey, UIUtils.PANEL_LIGHT, UIUtils.ACCENT, mouse, UIUtils.TEXT, UIUtils.HUD_BORDER);
        }

        int r3Y = r2Y + kh + 10;
        int specW = 86;
        int r3TotalW = specW * 2 + ROW3_KEYS.length * kw + (ROW3_KEYS.length + 1) * gap;
        int r3X = panel.x + (panel.width - r3TotalW) / 2;
        shiftRect = new Rectangle(r3X, r3Y, specW, kh);
        UIUtils.drawButton(g, shiftRect, "SHIFT", fBtn, shiftActive ? UIUtils.GOLD : UIUtils.PANEL_DARK, UIUtils.GOLD, mouse, shiftActive ? new Color(18, 22, 30) : UIUtils.TEXT, UIUtils.HUD_BORDER);
        int curX = r3X + specW + gap;
        for (int i = 0; i < ROW3_KEYS.length; i++) {
            Rectangle r = new Rectangle(curX, r3Y, kw, kh);
            keyRects.add(r);
            keyChars.add(ROW3_KEYS[i]);
            UIUtils.drawButton(g, r, String.valueOf(ROW3_KEYS[i]), fKey, UIUtils.PANEL_LIGHT, UIUtils.ACCENT, mouse, UIUtils.TEXT, UIUtils.HUD_BORDER);
            curX += kw + gap;
        }
        backRect = new Rectangle(curX, r3Y, specW, kh);
        UIUtils.drawButton(g, backRect, "DEL", fBtn, UIUtils.PANEL_DARK, UIUtils.ERROR, mouse, UIUtils.TEXT, UIUtils.HUD_BORDER);

        int r4Y = r3Y + kh + 12;
        int r4TotalW = 115 + 220 + 115 + 160 + 30;
        int r4X = panel.x + (panel.width - r4TotalW) / 2;
        clearRect = new Rectangle(r4X, r4Y, 115, 48);
        spaceRect = new Rectangle(r4X + 125, r4Y, 220, 48);
        hintRect = new Rectangle(r4X + 355, r4Y, 115, 48);
        submitRect = new Rectangle(r4X + 480, r4Y, 160, 48);

        UIUtils.drawButton(g, clearRect, "Clear", fBtn, UIUtils.PANEL_DARK, UIUtils.ERROR, mouse, UIUtils.TEXT, UIUtils.HUD_BORDER);
        UIUtils.drawButton(g, spaceRect, "Space", fBtn, UIUtils.PANEL_LIGHT, UIUtils.ACCENT, mouse, UIUtils.TEXT, UIUtils.HUD_BORDER);
        UIUtils.drawButton(g, hintRect, "Hint", fBtn, UIUtils.PANEL_DARK, UIUtils.GOLD, mouse, UIUtils.TEXT, UIUtils.HUD_BORDER);
        UIUtils.drawButton(g, submitRect, "Submit", fBtn, UIUtils.SUCCESS, UIUtils.SUCCESS_HOVER, mouse, UIUtils.TEXT, null);
    }

    private void drawSummary(Graphics2D g, Rectangle panel, Point mouse, Font fHead, Font fPrompt, Font fBtn) {
        int pct = questions.isEmpty() ? 100 : (int) Math.round(score * 100.0 / questions.size());
        int xpEarned = score * 15;

        g.setFont(assets.getFont(28, true));
        g.setColor(UIUtils.GOLD);
        String done = "Mini Game Complete!";
        FontMetrics fm = g.getFontMetrics();
        g.drawString(done, panel.x + (panel.width - fm.stringWidth(done)) / 2, panel.y + 180);

        g.setFont(fPrompt);
        g.setColor(UIUtils.TEXT);
        String sc = "Score: " + score + " / " + questions.size() + "  (" + pct + "%)";
        fm = g.getFontMetrics();
        g.drawString(sc, panel.x + (panel.width - fm.stringWidth(sc)) / 2, panel.y + 240);

        g.setFont(assets.getFont(16, true));
        g.setColor(UIUtils.SUCCESS);
        String xpTxt = "+" + xpEarned + " XP earned!";
        fm = g.getFontMetrics();
        g.drawString(xpTxt, panel.x + (panel.width - fm.stringWidth(xpTxt)) / 2, panel.y + 286);

        UIUtils.drawButton(g, replayBtn, "Play Again", fBtn, UIUtils.ACCENT, UIUtils.ACCENT_HOVER, mouse, UIUtils.TEXT, null);
        UIUtils.drawButton(g, menuBtn, "Main Menu", fBtn, UIUtils.SUCCESS, UIUtils.SUCCESS_HOVER, mouse, UIUtils.TEXT, null);
    }
}
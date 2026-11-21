package com.koreangame;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.nio.file.Paths;
import java.util.List;
import javax.swing.JPanel;
import javax.swing.Timer;

public class GamePanel extends JPanel {
    private final List<Level> levels;
    private final AssetLoader assets;
    private final SaveManager[] saveSlots;
    private int activeSlot = 0;
    private SaveManager save;
    private final Glossary glossary;
    private final Runnable onToggleFullscreen;

    private Level currentLevel;
    private MainMenuOverlay mainMenuOverlay;
    private IntroOverlay introOverlay;
    private LevelSelectOverlay levelSelectOverlay;
    private TaskOverlay taskOverlay;

    private boolean keyUp;
    private boolean keyDown;
    private boolean keyLeft;
    private boolean keyRight;

    private Point mousePos = new Point(-1, -1);
    private String toastText = "";
    private float toastTimer = 0.0f;
    private long lastFrameNanos;

    private double renderScale = 1.0;
    private double renderOffsetX = 0.0;
    private double renderOffsetY = 0.0;

    private final Rectangle menuBtn = new Rectangle(UIUtils.SCREEN_WIDTH - 364, 14, 78, 36);
    private final Rectangle fullBtn = new Rectangle(UIUtils.SCREEN_WIDTH - 278, 14, 72, 36);
    private final Rectangle guideBtn = new Rectangle(UIUtils.SCREEN_WIDTH - 198, 14, 92, 36);
    private final Rectangle levelsBtn = new Rectangle(UIUtils.SCREEN_WIDTH - 98, 14, 86, 36);

    public GamePanel(List<Level> levels, SaveManager[] saveSlots, Glossary glossary, Runnable onToggleFullscreen) {
        this.levels = levels;
        this.saveSlots = saveSlots;
        this.activeSlot = 0;
        this.save = saveSlots[0];
        this.glossary = glossary;
        this.onToggleFullscreen = onToggleFullscreen;
        this.assets = new AssetLoader(Paths.get("Assets"));

        setPreferredSize(new Dimension(UIUtils.SCREEN_WIDTH, UIUtils.SCREEN_HEIGHT));
        setBackground(UIUtils.BG);
        setFocusable(true);
        setFocusTraversalKeysEnabled(false);

        NativeEngine.nativeInit();
        loadLevel(save.currentLevel, save.hasSavedPos, false);
        mainMenuOverlay = new MainMenuOverlay(assets, saveSlots, activeSlot, false);

        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                handleKeyPressed(e.getKeyCode());
            }

            @Override
            public void keyReleased(KeyEvent e) {
                handleKeyReleased(e.getKeyCode());
            }
        });

        MouseAdapter ma = new MouseAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                mousePos = toVirtualPoint(e.getPoint());
            }

            @Override
            public void mousePressed(MouseEvent e) {
                mousePos = toVirtualPoint(e.getPoint());
                handleMouseClicked(mousePos);
            }
        };
        addMouseListener(ma);
        addMouseMotionListener(ma);

        lastFrameNanos = System.nanoTime();
        Timer timer = new Timer(16, e -> tick());
        timer.start();
    }

    private void updateRenderTransform() {
        int w = Math.max(1, getWidth());
        int h = Math.max(1, getHeight());
        renderScale = Math.min((double) w / UIUtils.SCREEN_WIDTH, (double) h / UIUtils.SCREEN_HEIGHT);
        if (renderScale <= 0.0) {
            renderScale = 1.0;
        }
        renderOffsetX = (w - UIUtils.SCREEN_WIDTH * renderScale) * 0.5;
        renderOffsetY = (h - UIUtils.SCREEN_HEIGHT * renderScale) * 0.5;
    }

    private Point toVirtualPoint(Point screenPoint) {
        updateRenderTransform();
        int vx = (int) Math.floor((screenPoint.x - renderOffsetX) / renderScale);
        int vy = (int) Math.floor((screenPoint.y - renderOffsetY) / renderScale);
        return new Point(vx, vy);
    }

    private Level findLevel(int id) {
        for (Level lv : levels) {
            if (lv.id == id) {
                return lv;
            }
        }
        return levels.get(0);
    }

    private void loadLevel(int id, boolean useSavedPos, boolean showIntroIfNew) {
        currentLevel = findLevel(id);
        save.currentLevel = currentLevel.id;
        float sx = currentLevel.spawnX * UIUtils.TILE_SIZE + UIUtils.TILE_SIZE * 0.5f;
        float sy = currentLevel.spawnY * UIUtils.TILE_SIZE + UIUtils.TILE_SIZE * 0.5f;
        NativeEngine.nativeClearEntities();
        for (Entity e : currentLevel.entities) {
            NativeEngine.nativeAddEntity(e.tx, e.ty);
        }
        NativeEngine.nativeLoadMap(currentLevel.getFlatMap(), sx, sy);
        if (useSavedPos && save.hasSavedPos) {
            NativeEngine.nativeSetPlayerPos(save.playerX, save.playerY);
        }
        if (showIntroIfNew && !save.seenIntros.contains(currentLevel.id)) {
            introOverlay = new IntroOverlay(currentLevel, assets);
            save.seenIntros.add(currentLevel.id);
        }
        persist();
    }

    public void persist() {
        save.playerX = NativeEngine.nativeGetPlayerX();
        save.playerY = NativeEngine.nativeGetPlayerY();
        save.hasSavedPos = true;
        save.save();
    }

    private void showToast(String msg, float seconds) {
        toastText = msg;
        toastTimer = seconds;
    }

    private boolean hasOverlay() {
        return mainMenuOverlay != null || introOverlay != null || levelSelectOverlay != null || taskOverlay != null;
    }

    private void clearMovementKeys() {
        keyUp = false;
        keyDown = false;
        keyLeft = false;
        keyRight = false;
    }

    private void openMainMenu() {
        clearMovementKeys();
        persist();
        mainMenuOverlay = new MainMenuOverlay(assets, saveSlots, activeSlot, true);
    }

    private void handleMenuAction(String action) {
        if (action == null) {
            return;
        }
        if ("start".equals(action)) {
            mainMenuOverlay = null;
            if (!save.seenIntros.contains(currentLevel.id)) {
                introOverlay = new IntroOverlay(currentLevel, assets);
                save.seenIntros.add(currentLevel.id);
                persist();
            }
        } else if (action.startsWith("load:")) {
            int idx = Integer.parseInt(action.substring(5));
            if (idx >= 0 && idx < saveSlots.length) {
                activeSlot = idx;
                save = saveSlots[activeSlot];
                mainMenuOverlay = null;
                loadLevel(save.currentLevel, save.hasSavedPos, true);
                showToast("Loaded Save Slot " + save.slotNumber + "!", 2.5f);
            }
        } else if (action.startsWith("reset:")) {
            int idx = Integer.parseInt(action.substring(6));
            if (idx >= 0 && idx < saveSlots.length) {
                saveSlots[idx].reset();
                if (idx == activeSlot) {
                    loadLevel(1, false, false);
                }
            }
        } else if ("quit".equals(action)) {
            persist();
            System.exit(0);
        }
    }

    private void onEntityCompleted(Entity ent) {
        boolean isNew = save.completedEntities.add(ent.id);
        if (isNew) {
            save.xp += 20 * ent.tasks.size();
            showToast("Completed " + ent.name + "! Progress saved.", 3.0f);
        } else {
            save.xp += 10;
            showToast("Practiced " + ent.name + "! (+10 XP)", 2.5f);
        }

        int done = countLevelDone();
        if (done == currentLevel.entities.size()) {
            int nextId = currentLevel.id + 1;
            if (nextId <= levels.size() && save.unlockedLevel < nextId) {
                save.unlockedLevel = nextId;
                showToast(currentLevel.id == 50 ? "Chapter 1 Complete! Chapter 2: Downtown Seoul unlocked — press [L]!" : "Level " + currentLevel.id + " Complete! Level " + nextId + " unlocked — press [L]!", 5.0f);
            } else if (nextId > levels.size()) {
                showToast("All 79 levels across Chapters 1 & 2 mastered!", 5.0f);
            }
        }
        persist();
    }

    private String getPlayerSpriteKey() {
        boolean ch1Done = true;
        boolean ch2Done = true;
        for (Level lv : levels) {
            for (Entity e : lv.entities) {
                if (!save.completedEntities.contains(e.id)) {
                    if (lv.id <= 50) {
                        ch1Done = false;
                    } else {
                        ch2Done = false;
                    }
                }
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

    private int countLevelDone() {
        int done = 0;
        for (Entity e : currentLevel.entities) {
            if (save.completedEntities.contains(e.id)) {
                done++;
            }
        }
        return done;
    }

    private void handleKeyPressed(int code) {
        if (code == KeyEvent.VK_F11) {
            clearMovementKeys();
            onToggleFullscreen.run();
            return;
        }
        if (mainMenuOverlay != null) {
            handleMenuAction(mainMenuOverlay.onKeyPressed(code));
            return;
        }
        if (introOverlay != null) {
            if (introOverlay.onKeyPressed(code)) {
                introOverlay = null;
            }
            return;
        }
        if (levelSelectOverlay != null) {
            int res = levelSelectOverlay.onKeyPressed(code);
            if (res == -1) {
                levelSelectOverlay = null;
            }
            return;
        }
        if (taskOverlay != null) {
            String res = taskOverlay.onKeyPressed(code);
            if ("close".equals(res)) {
                taskOverlay = null;
            } else if ("completed".equals(res)) {
                Entity ent = taskOverlay.entity;
                taskOverlay = null;
                onEntityCompleted(ent);
            }
            return;
        }

        if (code == KeyEvent.VK_W || code == KeyEvent.VK_UP) keyUp = true;
        else if (code == KeyEvent.VK_S || code == KeyEvent.VK_DOWN) keyDown = true;
        else if (code == KeyEvent.VK_A || code == KeyEvent.VK_LEFT) keyLeft = true;
        else if (code == KeyEvent.VK_D || code == KeyEvent.VK_RIGHT) keyRight = true;
        else if (code == KeyEvent.VK_M || code == KeyEvent.VK_ESCAPE) {
            openMainMenu();
        } else if (code == KeyEvent.VK_F) {
            clearMovementKeys();
            onToggleFullscreen.run();
        } else if (code == KeyEvent.VK_TAB) {
            clearMovementKeys();
            introOverlay = new IntroOverlay(currentLevel, assets);
        } else if (code == KeyEvent.VK_L) {
            clearMovementKeys();
            levelSelectOverlay = new LevelSelectOverlay(
                levels, currentLevel.id, save.unlockedLevel, save.completedEntities, assets
            );
        } else if (code == KeyEvent.VK_E || code == KeyEvent.VK_SPACE || code == KeyEvent.VK_ENTER) {
            int nearby = NativeEngine.nativeGetNearbyEntity();
            if (nearby >= 0 && nearby < currentLevel.entities.size()) {
                clearMovementKeys();
                taskOverlay = new TaskOverlay(currentLevel.entities.get(nearby), assets, glossary);
            }
        }
    }

    private void handleKeyReleased(int code) {
        if (code == KeyEvent.VK_W || code == KeyEvent.VK_UP) keyUp = false;
        else if (code == KeyEvent.VK_S || code == KeyEvent.VK_DOWN) keyDown = false;
        else if (code == KeyEvent.VK_A || code == KeyEvent.VK_LEFT) keyLeft = false;
        else if (code == KeyEvent.VK_D || code == KeyEvent.VK_RIGHT) keyRight = false;
    }

    private void handleMouseClicked(Point p) {
        if (mainMenuOverlay != null) {
            handleMenuAction(mainMenuOverlay.onMouseClicked(p));
            return;
        }
        if (introOverlay != null) {
            if (introOverlay.onMouseClicked(p)) {
                introOverlay = null;
            }
            return;
        }
        if (levelSelectOverlay != null) {
            int res = levelSelectOverlay.onMouseClicked(p);
            if (res == -1) {
                levelSelectOverlay = null;
            } else if (res > 0) {
                levelSelectOverlay = null;
                if (res != currentLevel.id) {
                    loadLevel(res, false, true);
                }
            }
            return;
        }
        if (taskOverlay != null) {
            String res = taskOverlay.onMouseClicked(p);
            if ("close".equals(res)) {
                taskOverlay = null;
            } else if ("completed".equals(res)) {
                Entity ent = taskOverlay.entity;
                taskOverlay = null;
                onEntityCompleted(ent);
            }
            return;
        }

        if (menuBtn.contains(p)) {
            openMainMenu();
            return;
        }
        if (fullBtn.contains(p)) {
            clearMovementKeys();
            onToggleFullscreen.run();
            return;
        }
        if (guideBtn.contains(p)) {
            clearMovementKeys();
            introOverlay = new IntroOverlay(currentLevel, assets);
            return;
        }
        if (levelsBtn.contains(p)) {
            clearMovementKeys();
            levelSelectOverlay = new LevelSelectOverlay(
                levels, currentLevel.id, save.unlockedLevel, save.completedEntities, assets
            );
            return;
        }
        if (p.y >= UIUtils.HUD_HEIGHT && p.x >= 0 && p.x < UIUtils.SCREEN_WIDTH && p.y < UIUtils.SCREEN_HEIGHT) {
            int picked = NativeEngine.nativePickEntity(p.x, p.y - UIUtils.HUD_HEIGHT);
            if (picked >= 0 && picked < currentLevel.entities.size()) {
                clearMovementKeys();
                taskOverlay = new TaskOverlay(currentLevel.entities.get(picked), assets, glossary);
            }
        }
    }

    private void tick() {
        long now = System.nanoTime();
        float dt = Math.min(0.05f, (now - lastFrameNanos) / 1_000_000_000.0f);
        lastFrameNanos = now;

        if (!hasOverlay()) {
            NativeEngine.nativeUpdate(dt, keyUp, keyDown, keyLeft, keyRight);
        }
        if (toastTimer > 0.0f) {
            toastTimer = Math.max(0.0f, toastTimer - dt);
        }
        repaint();
    }

    private String tileKey(char ch) {
        switch (ch) {
            case 'P': return "tiles/path";
            case 'F': return "tiles/floor";
            case 'W': return "tiles/wall";
            case 'R': return "tiles/roof_red";
            case 'B': return "tiles/roof_blue";
            case '~': return "tiles/water";
            case 'T': return "tiles/tree";
            case 'f': return "tiles/flower";
            case 'C': return "tiles/counter";
            case 'A': return "tiles/road";
            case 'X': return "tiles/crosswalk";
            case 'S': return "tiles/sidewalk";
            case 'L': return "tiles/plaza";
            case 'G': return "tiles/skyscraper";
            case 'N': return "tiles/neon_shop";
            case 'K': return "tiles/tower_roof";
            default: return "tiles/grass";
        }
    }

    @Override
    protected void paintComponent(Graphics g0) {
        super.paintComponent(g0);
        Graphics2D g = (Graphics2D) g0.create();
        g.setColor(UIUtils.BG);
        g.fillRect(0, 0, getWidth(), getHeight());

        updateRenderTransform();
        g.translate(renderOffsetX, renderOffsetY);
        g.scale(renderScale, renderScale);
        g.clipRect(0, 0, UIUtils.SCREEN_WIDTH, UIUtils.SCREEN_HEIGHT);

        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        int s = UIUtils.TILE_SIZE;
        for (int r = 0; r < currentLevel.mapRows.size(); r++) {
            String row = currentLevel.mapRows.get(r);
            for (int c = 0; c < row.length(); c++) {
                char ch = row.charAt(c);
                int tx = c * s;
                int ty = UIUtils.HUD_HEIGHT + r * s;
                BufferedImage img = assets.getImage(tileKey(ch));
                if (img != null) {
                    g.drawImage(img, tx, ty, s, s, null);
                } else {
                    g.setColor(new Color(92, 162, 76));
                    g.fillRect(tx, ty, s, s);
                }
            }
        }

        float anim = NativeEngine.nativeGetAnimTime();
        int bob = (int) Math.round(Math.sin(anim * 5.0) * 3.0);
        int nearbyIdx = NativeEngine.nativeGetNearbyEntity();
        Font fBadge = assets.getFont(14, true);
        Font fPrompt = assets.getFont(12, true);

        for (int i = 0; i < currentLevel.entities.size(); i++) {
            Entity ent = currentLevel.entities.get(i);
            int ex = ent.tx * s;
            int ey = UIUtils.HUD_HEIGHT + ent.ty * s;
            BufferedImage spr = assets.getImage(ent.spriteKey);
            if (spr != null) {
                g.drawImage(spr, ex, ey, s, s, null);
            }

            boolean done = save.completedEntities.contains(ent.id);
            Color bCol = done ? UIUtils.SUCCESS : UIUtils.GOLD;
            String bTxt = done ? "✓" : "!";
            int cx = ex + s / 2;
            int cy = ey - 8 + (done ? 0 : bob);
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setColor(new Color(20, 24, 34));
            g.fillOval(cx - 11, cy - 11, 22, 22);
            g.setColor(bCol);
            g.fillOval(cx - 9, cy - 9, 18, 18);
            g.setFont(fBadge);
            g.setColor(new Color(18, 22, 30));
            FontMetrics fmB = g.getFontMetrics();
            g.drawString(bTxt, cx - fmB.stringWidth(bTxt) / 2, cy + fmB.getAscent() / 2 - 2);
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);

            if (i == nearbyIdx && mainMenuOverlay == null) {
                String label = "[E] " + ent.name;
                g.setFont(fPrompt);
                FontMetrics fmP = g.getFontMetrics();
                int pw = fmP.stringWidth(label);
                int ph = fmP.getHeight();
                Rectangle box = new Rectangle(cx - pw / 2 - 8, ey + s + 2, pw + 16, ph + 6);
                g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g.setColor(UIUtils.PANEL);
                g.fillRoundRect(box.x, box.y, box.width, box.height, 8, 8);
                g.setColor(UIUtils.ACCENT);
                g.setStroke(new BasicStroke(1.5f));
                g.drawRoundRect(box.x, box.y, box.width, box.height, 8, 8);
                g.setColor(UIUtils.TEXT);
                g.drawString(label, box.x + 8, box.y + fmP.getAscent() + 3);
                g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
            }
        }

        float px = NativeEngine.nativeGetPlayerX();
        float py = NativeEngine.nativeGetPlayerY();
        int pDrawX = Math.round(px - s * 0.5f);
        int pDrawY = Math.round(UIUtils.HUD_HEIGHT + py - s * 0.5f);
        BufferedImage pImg = assets.getImage(getPlayerSpriteKey());
        if (pImg != null) {
            g.drawImage(pImg, pDrawX, pDrawY, s, s, null);
        }

        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        drawHud(g);

        if (toastTimer > 0.0f && !hasOverlay()) {
            Font fT = assets.getFont(14, true);
            g.setFont(fT);
            FontMetrics fmT = g.getFontMetrics();
            int tw = fmT.stringWidth(toastText);
            Rectangle box = new Rectangle(UIUtils.SCREEN_WIDTH / 2 - tw / 2 - 18, UIUtils.SCREEN_HEIGHT - 54, tw + 36, 38);
            g.setColor(UIUtils.PANEL);
            g.fillRoundRect(box.x, box.y, box.width, box.height, 12, 12);
            g.setColor(UIUtils.SUCCESS);
            g.setStroke(new BasicStroke(2f));
            g.drawRoundRect(box.x, box.y, box.width, box.height, 12, 12);
            g.setColor(UIUtils.TEXT);
            g.drawString(toastText, box.x + 18, box.y + 24);
        }

        if (mainMenuOverlay != null) {
            mainMenuOverlay.draw(g, mousePos);
        } else if (introOverlay != null) {
            introOverlay.draw(g, mousePos);
        } else if (levelSelectOverlay != null) {
            levelSelectOverlay.draw(g, mousePos);
        } else if (taskOverlay != null) {
            taskOverlay.draw(g, mousePos);
        }

        g.dispose();
    }

    private void drawHud(Graphics2D g) {
        g.setColor(UIUtils.HUD);
        g.fillRect(0, 0, UIUtils.SCREEN_WIDTH, UIUtils.HUD_HEIGHT);
        g.setColor(UIUtils.HUD_BORDER);
        g.setStroke(new BasicStroke(2f));
        g.drawLine(0, UIUtils.HUD_HEIGHT - 1, UIUtils.SCREEN_WIDTH, UIUtils.HUD_HEIGHT - 1);

        g.setFont(assets.getFont(17, true));
        g.setColor(UIUtils.TEXT);
        String chTag = currentLevel.id <= 50 ? "Ch.1 Lv." + currentLevel.id + "/50" : "Ch.2 Lv." + (currentLevel.id - 50) + "/29";
        g.drawString(chTag + "  " + currentLevel.zoneName, 16, 26);

        g.setFont(assets.getFont(13, false));
        g.setColor(UIUtils.MUTED);
        g.drawString(
            "Tasks: " + countLevelDone() + "/" + currentLevel.entities.size()
                + "   |   XP: " + save.xp
                + "   |   WASD: Move   |   E/Click: Talk",
            16,
            49
        );

        Font fBtn = assets.getFont(12, true);
        UIUtils.drawButton(g, menuBtn, "Menu [M]", fBtn, UIUtils.PANEL_LIGHT, UIUtils.ACCENT, mousePos, UIUtils.TEXT, UIUtils.HUD_BORDER);
        UIUtils.drawButton(g, fullBtn, "Full [F]", fBtn, UIUtils.PANEL_LIGHT, UIUtils.ACCENT, mousePos, UIUtils.TEXT, UIUtils.HUD_BORDER);
        UIUtils.drawButton(g, guideBtn, "Guide [Tab]", fBtn, UIUtils.PANEL_LIGHT, UIUtils.ACCENT, mousePos, UIUtils.TEXT, UIUtils.HUD_BORDER);
        UIUtils.drawButton(g, levelsBtn, "Levels [L]", fBtn, UIUtils.PANEL_LIGHT, UIUtils.ACCENT, mousePos, UIUtils.TEXT, UIUtils.HUD_BORDER);
    }
}

# 한글몬 — Hangeulmon
### 단어 마스터들의 부상 · *Rise of the Word Masters*

A 2D pixel-art Korean learning RPG built in **C + Java (JNI)** with **79 progressive levels**, **1,125 interactive tasks**, fullscreen pixel-perfect scaling, and automatic save persistence. Walk around towns and downtown Seoul, talk to locals, inspect everyday objects, and master Korean grammar from core nouns to advanced verb endings.

> **Note:** Assumes you can already read Hangul—jump straight into vocabulary, particles, conjugation, and sentence building.

---

## ✨ Features

* **C + Java (JNI) Engine:** Smooth 60 FPS movement, AABB collision sliding, proximity detection, and click hit-testing written in C11, paired with a zero-dependency Java Swing renderer.
* **Crisp Pixel-Art & Fullscreen Scaling:** Launches in fullscreen with automatic aspect-ratio scaling and nearest-neighbor rendering so 32×32 pixel art stays razor-sharp at any resolution.
* **79 Levels Across 2 Chapters:**
  * **Chapter 1: Foundations (Levels 1–50)** — 4 NPCs/objects × 2 tasks (**400 tasks**) across cozy towns, parks, cafes, markets, and hanok villages.
  * **Chapter 2: Downtown Seoul (Levels 51–79)** — Unlocks after completing Level 50! Set in the heart of Seoul (Gangnam, COEX, Myeongdong, Gwanghwamun, Hongdae, Seongsu, DDP, and more), featuring 29 dedicated verb-ending levels with 5 NPCs × 5 tasks (**725 tasks**).
* **Interactive Learning Mechanics:**
  * **Level Briefings (`Tab`):** Every level opens with a guide explaining the grammar rules, tips, and key vocabulary.
  * **3 Task Types:** Multiple choice, fill-in-the-blank, and click-to-order sentence builders (with distractor tiles and instant grammar explanations).
  * **Noun Hover Tooltips:** Hover any gold-underlined noun or word tile to see its English meaning—particles (`은/는`, `이/가`, `을/를`, `에/에서`, `께서`, etc.) are stripped automatically.
  * **Dictionary `-다` Verb Hints:** Conjugation and sentence-building prompts always display the base `-다` form.
* **Crown Progression:** Complete Chapter 1 to earn a **small gold crown**, and complete Chapter 2 to upgrade to the **royal crown**—displayed both in-game and on the Main Menu.
* **Automatic Save System:** Progress (level, XP, completed quests, seen intros, and player position) auto-saves atomically to `save_data.txt` so you can close and resume anytime.

---

## 🚀 Getting Started

### Prerequisites
* **JDK** (Java 11 or newer; tested on JDK 24)
* **C Compiler** (`clang` on macOS or `gcc` on Linux) and `make`

### Build & Run
Open your terminal in the project root directory and run:

```bash
make run
```

To do a clean rebuild:

```bash
make clean && make run
```

> **JDK 24+ Tip:** If you see a harmless `WARNING: A restricted method in java.lang.System has been called` message from JNI, you can silence it by adding `--enable-native-access=ALL-UNNAMED` to the `java` command in `Makefile`.

---

## 🎮 Controls

| Action | Key / Input |
| :--- | :--- |
| **Move** | `W` `A` `S` `D` or Arrow Keys |
| **Talk / Inspect** | `E`, `Space`, `Enter`, or **Left-Click** on any character/object |
| **Level Guide (Intro & Rules)** | `Tab` (or click **Guide**) |
| **Level Select (Chapters 1 & 2)** | `L` (or click **Levels**, use `< Prev` / `Next >` or Left/Right arrows) |
| **Main Menu / Pause** | `M` or `Esc` (or click **Menu**) |
| **Toggle Fullscreen** | `F` or `F11` (or click **Full**) |
| **Quick-Select Answer (Choice/Fill)** | `1` `2` `3` `4` |

---

## 📚 Curriculum Overview

### Chapter 1: Foundations (Levels 1–50)
* **Lv. 1–10:** Core nouns, `-이에요/-예요`, demonstratives (`이/그/저`), `있다/없다`, particles (`은/는`, `이/가`, `을/를`, `에/에서`), polite present adjectives & verbs, `하다` verbs, native & Sino-Korean numbers, counters, prices, and time.
* **Lv. 11–20:** Negation (`안`, `-지 않아요`, `못`, `-지 못해요`), past (`-았/었어요`), future (`-(으)ㄹ 거예요`), progressive (`-고 있어요`), wants (`-고 싶어요`), requests/prohibition (`-(으)세요`, `-지 마세요`), connectors (`-고`, `-지만`, `-아서/어서`), `ㅂ`/`ㅡ` irregulars.
* **Lv. 21–30:** `ㄷ`/`ㄹ`/`르`/`ㅅ` irregulars, ability (`-(으)ㄹ 수 있다/없다`), obligation (`-아/어야 되다`), suggestions (`-(으)ㄹ까요?`, `-(으)ㅂ시다`), permission/prohibition (`-아/어도 되다`, `-(으)면 안 되다`), sequencing (`-기 전에`, `-(으)ㄴ 후에`, `-고 나서`, `-(으)면서`), purpose (`-(으)러 가다`), trying & experience (`-아/어 보다`, `-(으)ㄴ 적이 있다`).
* **Lv. 31–40:** Present/past/future noun modifiers (`-는`, `-(으)ㄴ`, `-(으)ㄹ`), conditionals (`-(으)면`, `-(으)ㄹ 때`), `-(으)니까`, background `-는데/-(으)ㄴ데`, honorifics (`-(으)시-`, `께서`, `드리다`, `드시다`), casual 반말, formal `-습니다/-ㅂ니다`, comparisons (`보다`, `제일`, `처럼`).
* **Lv. 41–50:** Indirect speech (`-다고`, `-냐고`, `-(으)라고`, `-자고`), supposition (`-(으)ㄴ/는/ㄹ 것 같다`), decisions & promises (`-기로 하다`, `-(으)ㄹ게요`), regret & wishing (`-(으)ㄹ 걸 그랬다`, `-았/었으면 좋겠다`), passives, causatives, concession (`-아/어도`), change of state & habits (`-게 되다`, `-곤 하다`), and real-world Korean idioms.

### Chapter 2: Downtown Seoul (Levels 51–79)
* **Lv. 51–54:** Present, Past, Future, and Progressive/Continuous verb endings across all speech registers.
* **Lv. 55–59:** Sequential actions, simultaneous actions (`-(으)면서`, `-다가`), contrast, conditionals/hypotheticals, and purpose/intention.
* **Lv. 60–63:** All 9 directional movement scenarios with `-(으)러`, ability/possibility, obligation/necessity, and permission/prohibition.
* **Lv. 64–68:** Desire/preference, requests/suggestions, giving/receiving actions, experience/trial, and completion/preparation (`-아/어 버리다`, `-고 말다`, `-아/어 놓다`).
* **Lv. 69–73:** All negative forms, question forms, listing/alternatives (`-거나`, `-든지`), quotation/reporting, and nominal forms (`-기`, `-는 것`, `-(으)ㅁ`).
* **Lv. 74–79:** Relative clause modifiers, emphasis/intensification, supposition/appearance, reason/cause (`-아/어서`, `-(으)니까`, `-느라고`), degree/extent, and background/context (`-는데`, `-거든요`).

---

## 📁 Project Structure

```text
.
├── Makefile
├── README.md
├── save_data.txt                  # Auto-generated save file
├── Assets/
│   ├── tiles/                     # 32x32 pixel-art town & downtown Seoul tiles
│   ├── sprites/                   # Player (base, small crown, big crown) & NPCs
│   └── objects/                   # Interactive pixel-art items
└── src/
    ├── native/                    # C11 engine & JNI bridge
    │   ├── engine.h
    │   ├── engine.c
    │   └── jni_bridge.c
    ├── java/com/koreangame/       # Java Swing UI, renderer, overlays & loaders
    │   ├── Main.java
    │   ├── NativeEngine.java
    │   ├── GameWindow.java
    │   ├── GamePanel.java
    │   ├── AssetLoader.java
    │   ├── CurriculumLoader.java
    │   ├── Glossary.java
    │   ├── SaveManager.java
    │   ├── UIUtils.java
    │   ├── Level.java
    │   ├── Entity.java
    │   ├── Task.java
    │   ├── MainMenuOverlay.java
    │   ├── IntroOverlay.java
    │   ├── LevelSelectOverlay.java
    │   └── TaskOverlay.java
    └── data/
        ├── levels.txt             # All 79 levels, maps, briefings, NPCs & tasks
        └── glossary.txt           # 121-noun Korean-to-English hover dictionary
```

---

## 🛠️ Customization & Resetting Progress

* **Add Vocabulary to Hover Hints:** Append `한국어=English meaning` lines to `src/data/glossary.txt`.
* **Edit or Add Levels:** All maps, briefings, NPCs, and tasks live in `src/data/levels.txt`.
* **Swap Sprites/Tiles:** Replace any 32×32 `.png` inside `Assets/tiles/`, `Assets/sprites/`, or `Assets/objects/`—no recompilation needed.
* **Reset Progress:** Delete `save_data.txt` to start fresh from Level 1.
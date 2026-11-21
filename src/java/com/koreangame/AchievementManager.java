package com.koreangame;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public final class AchievementManager {
    public static final String HUNGER_GOD_ID = "hunger_god";

    private static final List<Achievement> ALL = Collections.unmodifiableList(Arrays.asList(
        new Achievement("first_quest", "First Steps", "첫 걸음", "Complete your very first quest.", false),
        new Achievement("level_1_clear", "Town Square Graduate", "광장 졸업", "Finish every quest in Level 1.", false),
        new Achievement("ten_levels", "Seoul Explorer", "서울 탐험가", "Complete all quests in 10 different levels.", false),
        new Achievement("first_exam", "Exam Ready", "시험 합격", "Pass your first End-of-Level Revision Exam.", false),
        new Achievement("big_exam", "Milestone Crusher", "중간고사 정복", "Pass any 25-question Milestone Big Exam.", false),
        new Achievement("xp_5000", "Word Collector", "단어 수집가", "Reach 5,000 total XP.", false),
        new Achievement("xp_15000", "Grammar Virtuoso", "문법 달인", "Reach 15,000 total XP.", false),
        new Achievement("ch1_complete", "Small Crown Bearer", "작은 왕관", "Complete all 50 levels of Chapter 1.", false),
        new Achievement("ch1_test", "Chapter 1 Scholar", "챕터 1 마스터", "Pass the 40-question Chapter 1 Test.", false),
        new Achievement("ch2_complete", "Royal Crown Bearer", "황금 왕관", "Complete all 29 levels of Chapter 2.", false),
        new Achievement("ch2_test", "Downtown Legend", "서울의 전설", "Pass the 40-question Chapter 2 Test.", false),
        new Achievement("final_pass", "Word Master", "단어 마스터", "Pass the Level 80 Grand Final Exam (70%+).", false),
        new Achievement(HUNGER_GOD_ID, "Hunger God", "헝거 갓", "Score a perfect 100% on the Level 80 Grand Final Exam! Grants the Hunger God badge.", true)
    ));

    private AchievementManager() {}

    public static List<Achievement> getAll() {
        return ALL;
    }

    public static Achievement findById(String id) {
        for (Achievement a : ALL) {
            if (a.id.equals(id)) {
                return a;
            }
        }
        return null;
    }

    public static List<Achievement> evaluate(SaveManager save, List<Level> levels) {
        List<Achievement> newlyUnlocked = new ArrayList<>();

        if (!save.completedEntities.isEmpty()) {
            unlock("first_quest", save, newlyUnlocked);
        }
        if (save.xp >= 5000) {
            unlock("xp_5000", save, newlyUnlocked);
        }
        if (save.xp >= 15000) {
            unlock("xp_15000", save, newlyUnlocked);
        }

        int completedLevels = 0;
        boolean lv1Done = false;
        boolean ch1Done = true;
        boolean ch2Done = true;

        for (Level lv : levels) {
            if (lv.id == 80 || lv.entities.isEmpty()) {
                continue;
            }
            boolean allDone = true;
            for (Entity e : lv.entities) {
                if (!save.completedEntities.contains(e.id)) {
                    allDone = false;
                    break;
                }
            }
            if (allDone) {
                completedLevels++;
                if (lv.id == 1) {
                    lv1Done = true;
                }
            } else {
                if (lv.id <= 50) {
                    ch1Done = false;
                } else {
                    ch2Done = false;
                }
            }
        }

        if (lv1Done) {
            unlock("level_1_clear", save, newlyUnlocked);
        }
        if (completedLevels >= 10) {
            unlock("ten_levels", save, newlyUnlocked);
        }
        if (ch1Done) {
            unlock("ch1_complete", save, newlyUnlocked);
        }
        if (ch2Done) {
            unlock("ch2_complete", save, newlyUnlocked);
        }

        for (String examId : save.passedExams) {
            if (examId.startsWith("exam_lv")) {
                unlock("first_exam", save, newlyUnlocked);
            } else if (examId.startsWith("big_exam_")) {
                unlock("big_exam", save, newlyUnlocked);
            } else if (examId.equals("chapter1_test")) {
                unlock("ch1_test", save, newlyUnlocked);
            } else if (examId.equals("chapter2_test")) {
                unlock("ch2_test", save, newlyUnlocked);
            } else if (examId.equals("final_exam_80")) {
                unlock("final_pass", save, newlyUnlocked);
            }
        }

        return newlyUnlocked;
    }

    public static Achievement unlockExplicit(String id, SaveManager save) {
        if (save.unlockedAchievements.add(id)) {
            return findById(id);
        }
        return null;
    }

    private static void unlock(String id, SaveManager save, List<Achievement> out) {
        if (save.unlockedAchievements.add(id)) {
            Achievement a = findById(id);
            if (a != null) {
                out.add(a);
            }
        }
    }
}

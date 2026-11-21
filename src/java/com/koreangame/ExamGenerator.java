package com.koreangame;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

public final class ExamGenerator {
    private static final String[] DAILY_GENERAL = {
        "친구: 오늘 뭐 해?   |   나: ",
        "민지: 요즘 어떻게 지내요?   |   나: ",
        "현우: 잠깐 시간 있어요?   |   나: ",
        "지수: 오늘 하루 어땠어요?   |   나: ",
        "동료: 퇴근하고 뭐 할 거예요?   |   나: ",
        "친구: 주말에 별일 없어?   |   나: ",
        "소라: 아, 진짜요? 그럼...   |   나: ",
        "룸메이트: 밖에 날씨 어때요?   |   나: "
    };

    private static final String[] DAILY_FOOD = {
        "친구: 배고픈데 점심 뭐 먹을래요?   |   나: ",
        "동료: 여기 맛집이래요! 뭐 시킬까요?   |   나: ",
        "민지: 저녁에 같이 밥 먹을래요?   |   나: ",
        "직원: 주문 도와드릴까요?   |   나: "
    };

    private static final String[] DAILY_CAFE = {
        "바리스타: 아이스 아메리카노 드릴까요?   |   나: ",
        "친구: 카페에서 잠깐 쉴까요?   |   나: ",
        "소라: 여기 라떼가 진짜 맛있어요!   |   나: "
    };

    private static final String[] DAILY_TIME_PAST = {
        "친구: 어제 저녁에 뭐 했어요?   |   나: ",
        "민지: 주말 잘 보냈어요?   |   나: ",
        "현우: 어제 약속 어땠어요?   |   나: "
    };

    private static final String[] DAILY_TIME_FUTURE = {
        "친구: 내일 시간 괜찮아요?   |   나: ",
        "지수: 이번 주말에 어디 갈 거예요?   |   나: ",
        "동료: 다음 주 계획 세웠어요?   |   나: "
    };

    private static final String[] DAILY_TRANSIT = {
        "친구: 지금 어디쯤 왔어요?   |   나: ",
        "현우: 지하철 많이 막혀요?   |   나: ",
        "민지: 거기까지 어떻게 갈 거예요?   |   나: "
    };

    private ExamGenerator() {}

    private static String pickDailyOpener(String text, int idx) {
        if (text.contains("커피") || text.contains("카페") || text.contains("마시")) {
            return DAILY_CAFE[idx % DAILY_CAFE.length];
        }
        if (text.contains("밥") || text.contains("비빔밥") || text.contains("식당") || text.contains("먹") || text.contains("떡볶이") || text.contains("사과")) {
            return DAILY_FOOD[idx % DAILY_FOOD.length];
        }
        if (text.contains("어제") || text.contains("갔") || text.contains("봤") || text.contains("먹었")) {
            return DAILY_TIME_PAST[idx % DAILY_TIME_PAST.length];
        }
        if (text.contains("내일") || text.contains("주말") || text.contains("거예요") || text.contains("게요")) {
            return DAILY_TIME_FUTURE[idx % DAILY_TIME_FUTURE.length];
        }
        if (text.contains("지하철") || text.contains("버스") || text.contains("역") || text.contains("도착")) {
            return DAILY_TRANSIT[idx % DAILY_TRANSIT.length];
        }
        return DAILY_GENERAL[idx % DAILY_GENERAL.length];
    }

    public static List<Task> generate(List<Level> allLevels, Glossary glossary, ExamSpec spec) {
        Random rng = new Random();
        List<Level> range = new ArrayList<>();
        for (Level lv : allLevels) {
            if (lv.id >= spec.startLevel && lv.id <= spec.endLevel && lv.id <= 79) {
                range.add(lv);
            }
        }
        if (range.isEmpty()) {
            for (Level lv : allLevels) {
                if (lv.id <= 79) {
                    range.add(lv);
                }
            }
        }

        List<Task> buildPool = new ArrayList<>();
        List<Task> convPool = new ArrayList<>();
        List<String[]> vocabPool = new ArrayList<>();
        Set<String> seenVocabKeys = new HashSet<>();

        int openerIdx = 0;
        for (Level lv : range) {
            for (String[] kv : lv.vocab) {
                if (kv.length == 2 && seenVocabKeys.add(kv[0])) {
                    vocabPool.add(kv);
                }
            }
            for (Entity e : lv.entities) {
                for (Task t : e.tasks) {
                    if ("build".equals(t.type)) {
                        buildPool.add(new Task(
                            "build",
                            "[Sentence Build] " + t.prompt,
                            t.options,
                            t.answer,
                            t.tokens,
                            t.distractors,
                            t.explanation
                        ));
                    } else if ("fill".equals(t.type) || "choice".equals(t.type)) {
                        String opener = pickDailyOpener(t.prompt + " " + t.answer, openerIdx);
                        openerIdx++;
                        String convPrompt = "[Daily Chat] " + opener + t.prompt;
                        convPool.add(new Task(
                            "fill",
                            convPrompt,
                            t.options,
                            t.answer,
                            t.tokens,
                            t.distractors,
                            t.explanation
                        ));
                    }
                }
            }
        }

        List<String[]> globalVocab = new ArrayList<>();
        Set<String> globalKeys = new HashSet<>();
        for (Level lv : allLevels) {
            if (lv.id <= 79) {
                for (String[] kv : lv.vocab) {
                    if (kv.length == 2 && globalKeys.add(kv[0])) {
                        globalVocab.add(kv);
                    }
                }
            }
        }
        if (glossary != null) {
            for (Map.Entry<String, String> entry : glossary.getAllEntries().entrySet()) {
                if (globalKeys.add(entry.getKey())) {
                    globalVocab.add(new String[]{entry.getKey(), entry.getValue()});
                }
            }
        }

        if (vocabPool.size() < 6) {
            List<String[]> extra = new ArrayList<>(globalVocab);
            Collections.shuffle(extra, rng);
            for (String[] kv : extra) {
                if (seenVocabKeys.add(kv[0])) {
                    vocabPool.add(kv);
                }
                if (vocabPool.size() >= 12) {
                    break;
                }
            }
        }

        List<Task> vocabTasks = new ArrayList<>();
        List<String[]> shuffledVocab = new ArrayList<>(vocabPool);
        Collections.shuffle(shuffledVocab, rng);
        for (int i = 0; i < shuffledVocab.size(); i++) {
            String[] pair = shuffledVocab.get(i);
            boolean krToEn = (i % 2 == 0);
            vocabTasks.add(makeVocabQuestion(pair, globalVocab, krToEn, rng));
            if (spec.questionCount > 20 || vocabTasks.size() < 6) {
                vocabTasks.add(makeVocabQuestion(pair, globalVocab, !krToEn, rng));
            }
        }

        Collections.shuffle(buildPool, rng);
        Collections.shuffle(convPool, rng);
        Collections.shuffle(vocabTasks, rng);

        int total = spec.questionCount;
        int wantBuild = total / 3;
        int wantConv = total / 3;
        int wantVocab = total - wantBuild - wantConv;

        List<Task> result = new ArrayList<>();
        addUpTo(result, buildPool, wantBuild, rng);
        addUpTo(result, convPool, wantConv, rng);
        addUpTo(result, vocabTasks, wantVocab, rng);

        List<Task> fallback = new ArrayList<>();
        fallback.addAll(convPool);
        fallback.addAll(buildPool);
        fallback.addAll(vocabTasks);
        Collections.shuffle(fallback, rng);
        int fi = 0;
        while (result.size() < total && !fallback.isEmpty()) {
            result.add(fallback.get(fi % fallback.size()));
            fi++;
        }

        Collections.shuffle(result, rng);
        return result;
    }

    private static void addUpTo(List<Task> dest, List<Task> pool, int count, Random rng) {
        if (pool.isEmpty() || count <= 0) {
            return;
        }
        int added = 0;
        int idx = 0;
        while (added < count) {
            if (idx >= pool.size()) {
                Collections.shuffle(pool, rng);
                idx = 0;
            }
            dest.add(pool.get(idx));
            idx++;
            added++;
        }
    }

    private static Task makeVocabQuestion(String[] target, List<String[]> allVocab, boolean krToEn, Random rng) {
        String kr = target[0];
        String en = target[1];
        List<String> opts = new ArrayList<>();
        String correct = krToEn ? en : kr;
        opts.add(correct);

        List<String[]> copy = new ArrayList<>(allVocab);
        Collections.shuffle(copy, rng);
        for (String[] cand : copy) {
            if (opts.size() >= 4) {
                break;
            }
            String val = krToEn ? cand[1] : cand[0];
            if (!opts.contains(val)) {
                opts.add(val);
            }
        }
        while (opts.size() < 4) {
            opts.add(krToEn ? "other" : "기타");
        }

        String prompt = krToEn
            ? "[Vocabulary] What does '" + kr + "' mean in English?"
            : "[Vocabulary] Which Korean word means '" + en + "'?";
        String exp = "'" + kr + "' means '" + en + "'.";

        return new Task("choice", prompt, opts, correct, Collections.emptyList(), Collections.emptyList(), exp);
    }
}

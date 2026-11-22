package com.koreangame;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

public final class ExamGenerator {
    private static class ConvEntry {
        final int minLevel;
        final int maxLevel;
        final String prompt;
        final List<String> options;
        final String answer;
        final String explanation;

        ConvEntry(int minLevel, int maxLevel, String prompt, List<String> options, String answer, String explanation) {
            this.minLevel = minLevel;
            this.maxLevel = maxLevel;
            this.prompt = prompt;
            this.options = options;
            this.answer = answer;
            this.explanation = explanation;
        }
    }

    private static final List<ConvEntry> DAILY_CONVERSATIONS = Arrays.asList(
        new ConvEntry(1, 10,
            "민지: 이거 뭐예요? (Holding a coffee)  /  나: 이거는 커피___.",
            Arrays.asList("예요", "이에요", "은", "을"), "예요",
            "커피 ends in a vowel, so the polite copula is -예요."),
        new ConvEntry(1, 10,
            "민지: 저기 테이블에 있는 거 뭐예요?  /  나: 그거는 제 책___.",
            Arrays.asList("이에요", "예요", "가", "를"), "이에요",
            "책 ends in a consonant (ㄱ), so it takes -이에요."),
        new ConvEntry(2, 12,
            "친구: 화장실이 ___? (Asking where it is)  /  나: 저기 오른쪽에 있어요.",
            Arrays.asList("어디예요", "누구예요", "뭐예요", "얼마예요"), "어디예요",
            "어디예요? asks for a location ('Where is it?')."),
        new ConvEntry(3, 15,
            "직원: 우산 ___? (Asking if you have one)  /  나: 네, 가방에 있어요.",
            Arrays.asList("있어요", "없어요", "아니에요", "가요"), "있어요",
            "The reply confirms 'Yes, it's in my bag', so the question is 있어요?"),
        new ConvEntry(3, 15,
            "친구: 지금 어디에 있어요?  /  나: 저는 지금 집___ 있어요.",
            Arrays.asList("에", "에서", "을", "로"), "에",
            "Static location with 있다 always takes 에."),
        new ConvEntry(4, 15,
            "민지: 오늘 날씨 어때요?  /  나: 오늘 날씨___ 정말 좋아요!",
            Arrays.asList("가", "이", "을", "에서"), "가",
            "날씨 ends in a vowel, so the subject particle is 가."),
        new ConvEntry(6, 18,
            "동료: 점심에 뭐 먹을 거예요?  /  나: 저는 비빔밥___ 먹을 거예요.",
            Arrays.asList("을", "를", "이", "에"), "을",
            "비빔밥 ends in a consonant (ㅂ), so the object particle is 을."),
        new ConvEntry(8, 20,
            "친구: 주말에 보통 뭐 해요?  /  나: 저는 도서관___ 책을 읽어요.",
            Arrays.asList("에서", "에", "을", "가"), "에서",
            "Reading is an active action happening at a place, so use 에서."),
        new ConvEntry(8, 20,
            "현우: 지금 어디 가요?  /  나: 친구 만나러 카페___ 가요.",
            Arrays.asList("에", "에서", "를", "은"), "에",
            "Destination with 가다 always uses 에."),
        new ConvEntry(9, 25,
            "바리스타: 몇 잔 드릴까요? (Ordering 2 cups)  /  나: 아메리카노 ___ 잔 주세요.",
            Arrays.asList("두", "둘", "이", "세"), "두",
            "둘 shortens to 두 before the counter 잔."),
        new ConvEntry(10, 25,
            "손님: 이 사과 ___? (Asking the price)  /  나: 오천 원이에요.",
            Arrays.asList("얼마예요", "몇 시예요", "어디예요", "누구예요"), "얼마예요",
            "The reply gives a price (5,000 won), so the question is 얼마예요?"),
        new ConvEntry(11, 30,
            "바리스타: 커피 드실래요? (You choose not to drink coffee)  /  나: 아니요, 저는 커피를 ___ 마셔요.",
            Arrays.asList("안", "못", "없", "아니"), "안",
            "안 expresses choosing not to do something; 못 expresses inability."),
        new ConvEntry(12, 30,
            "친구: 수영장에 같이 갈래요? (You don't know how to swim)  /  나: 미안해요, 저는 수영 ___ 해요.",
            Arrays.asList("못", "안", "않", "없"), "못",
            "Lack of ability uses 못 before 하다 -> 수영 못 해요."),
        new ConvEntry(13, 35,
            "민지: 어제 저녁에 뭐 했어요?  /  나: 어제 친구를 ___.",
            Arrays.asList("만났어요", "만나요", "만날 거예요", "만나고 싶어요"), "만났어요",
            "어제 (yesterday) requires past tense -> 만났어요."),
        new ConvEntry(14, 35,
            "지수: 내일 계획 있어요?  /  나: 네, 내일 부산에 ___ 거예요.",
            Arrays.asList("갈", "갔", "가는", "가을"), "갈",
            "내일 (tomorrow) takes future -(으)ㄹ 거예요; 가다 -> 갈 거예요."),
        new ConvEntry(15, 40,
            "현우: 지금 뭐 하고 있어요? (Listening right now)  /  나: 지금 음악을 ___ 있어요.",
            Arrays.asList("듣고", "들어서", "들을", "들은"), "듣고",
            "Actions happening right now use -고 있어요 -> 듣고 있어요."),
        new ConvEntry(16, 40,
            "친구: 휴가 때 뭐 하고 싶어요?  /  나: 집에서 푹 쉬고 ___.",
            Arrays.asList("싶어요", "있어요", "해요", "돼요"), "싶어요",
            "Expressing a wish ('want to') uses -고 싶어요."),
        new ConvEntry(17, 45,
            "직원: 전시실에서는 사진 찍지 ___. (Polite 'please don't')  /  나: 네, 알겠습니다.",
            Arrays.asList("마세요", "않아요", "못해요", "으세요"), "마세요",
            "Polite prohibition ('please do not') uses -지 마세요."),
        new ConvEntry(18, 45,
            "친구: 그 식당 어때요? (Expensive, BUT delicious)  /  나: 조금 비싸___ 정말 맛있어요.",
            Arrays.asList("지만", "고", "아서", "면"), "지만",
            "Contrasting ideas ('but') use -지만."),
        new ConvEntry(19, 50,
            "동료: 왜 택시를 탔어요? (Because I had no time)  /  나: 시간이 없___ 택시를 탔어요.",
            Arrays.asList("어서", "아서", "지만", "면"), "어서",
            "Neutral cause/reason with 없다 (vowel ㅓ) uses -어서 -> 없어서."),
        new ConvEntry(23, 55,
            "호스트: 한글 읽을 줄 알아요? (Yes, I can read it)  /  나: 네, 한글을 읽___ 수 있어요.",
            Arrays.asList("을", "ㄹ", "는", "은"), "을",
            "Consonant stem 읽다 takes -을 수 있어요."),
        new ConvEntry(24, 55,
            "친구: 왜 벌써 가요? (I have to wake up early tomorrow)  /  나: 내일 일찍 일어나___ 돼요.",
            Arrays.asList("야", "어야", "해야", "면"), "야",
            "Obligation with 일어나다 (vowel ㅏ) is 일어나야 돼요."),
        new ConvEntry(26, 60,
            "손님: 여기에 앉___ 돼요? (Asking permission)  /  직원: 네, 편하게 앉으세요!",
            Arrays.asList("아도", "어도", "으면", "고"), "아도",
            "Asking permission with 앉다 (vowel ㅏ) uses -아도 돼요?"),
        new ConvEntry(27, 65,
            "엄마: 밥 먹___ 전에 손 씻었어?  /  나: 응, 방금 씻었어!",
            Arrays.asList("기", "은", "ㄴ", "고"), "기",
            "'Before doing' always uses stem + -기 전에."),
        new ConvEntry(29, 65,
            "친구: 도서관에 왜 가요? (Going in order to read)  /  나: 책을 읽___ 도서관에 가요.",
            Arrays.asList("으러", "러", "고", "면서"), "으러",
            "Purpose of motion with consonant stem 읽다 takes -으러."),
        new ConvEntry(30, 70,
            "민지: 제주도에 가 본 적 있어요?  /  나: 네, 작년에 ___ 적이 있어요.",
            Arrays.asList("간", "가는", "갈", "가은"), "간",
            "Past life experience uses -(으)ㄴ 적이 있어요 -> 간 적이 있어요."),
        new ConvEntry(35, 79,
            "친구: 밖에 많이 추워요? (Giving a reason before a command)  /  나: 네, 추우___ 코트 입으세요!",
            Arrays.asList("니까", "아서", "어서", "지만"), "니까",
            "Before a command (입으세요), you must use -(으)니까, not -아서/어서."),
        new ConvEntry(38, 79,
            "단짝: 너 지금 어디 가? (Casual reply to best friend)  /  나: 나 카페에 ___.",
            Arrays.asList("가", "가요", "갑니다", "가세요"), "가",
            "Casual 반말 drops 요 -> 가."),
        new ConvEntry(39, 79,
            "면접관: 준비되셨습니까? (Formal interview reply)  /  나: 네, 지금부터 발표를 시작___.",
            Arrays.asList("합니다", "해요", "해", "한다"), "합니다",
            "Formal speech in an interview uses -(스)ㅂ니다 -> 시작합니다."),
        new ConvEntry(43, 79,
            "친구: 내일 비가 올까요? (Making a future guess)  /  나: 하늘을 보니까 내일 비가 ___ 것 같아요.",
            Arrays.asList("올", "온", "오는", "오을"), "올",
            "Future supposition ('I think it will...') uses -(으)ㄹ 것 같아요."),
        new ConvEntry(44, 79,
            "동료: 내일 도착하면 연락해 주세요! (Promising the listener)  /  나: 네, 도착하자마자 전화___!",
            Arrays.asList("할게요", "하기로 했어요", "할까요", "했어요"), "할게요",
            "Making a promise directly to the listener uses -(으)ㄹ게요."),
        new ConvEntry(51, 79,
            "친구: 왜 전화 안 받았어요? (Busy doing homework -> negative result)  /  나: 숙제하___ 못 받았어요.",
            Arrays.asList("느라고", "면서", "거나", "도록"), "느라고",
            "-느라고 explains a negative result caused by being preoccupied with another action."),
        new ConvEntry(51, 79,
            "민지: 어제 그 영화 어땠어요? (Soft background before adding more)  /  나: 어제 ___ 정말 재미있었어요!",
            Arrays.asList("봤는데", "보지만", "보거든", "보려고"), "봤는데",
            "Setting past background context in conversation uses -았/었는데 -> 봤는데."),
        new ConvEntry(51, 79,
            "현우: 왜 이렇게 일찍 가요? (Explaining a reason the listener didn't know)  /  나: 사실 내일 시험이 있___.",
            Arrays.asList("거든요", "는데도", "거나", "자마자"), "거든요",
            "-거든요 gives a new explanation ('you see, because...').")
    );

    private ExamGenerator() {}

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

        for (ConvEntry c : DAILY_CONVERSATIONS) {
            if (c.minLevel <= spec.endLevel && c.maxLevel >= spec.startLevel) {
                convPool.add(new Task(
                    "fill",
                    "[Conversation] " + c.prompt,
                    c.options,
                    c.answer,
                    Collections.emptyList(),
                    Collections.emptyList(),
                    c.explanation
                ));
            }
        }

        if (convPool.size() < 4) {
            for (ConvEntry c : DAILY_CONVERSATIONS) {
                if (c.minLevel <= spec.endLevel) {
                    convPool.add(new Task(
                        "fill",
                        "[Conversation] " + c.prompt,
                        c.options,
                        c.answer,
                        Collections.emptyList(),
                        Collections.emptyList(),
                        c.explanation
                    ));
                }
            }
        }

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
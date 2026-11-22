package com.koreangame;

public class HangulComposer {
    private static final String CHOSEONG = "ㄱㄲㄴㄷㄸㄹㅁㅂㅃㅅㅆㅇㅈㅉㅊㅋㅌㅍㅎ";
    private static final String JUNGSEONG = "ㅏㅐㅑㅒㅓㅔㅕㅖㅗㅘㅙㅚㅛㅜㅝㅞㅟㅠㅡㅢㅣ";
    private static final String JONGSEONG = " ㄱㄲㄳㄴㄵㄶㄷㄹㄺㄻㄼㄽㄾㄿㅀㅁㅂㅄㅅㅆㅇㅈㅊㅋㅌㅍㅎ";

    private final StringBuilder committed = new StringBuilder();
    private int cho = -1;
    private int jung = -1;
    private int jong = 0;

    public void clear() {
        committed.setLength(0);
        cho = -1;
        jung = -1;
        jong = 0;
    }

    public String getText() {
        StringBuilder sb = new StringBuilder(committed);
        if (cho != -1) {
            if (jung != -1) {
                char syl = (char) (0xAC00 + (cho * 21 + jung) * 28 + jong);
                sb.append(syl);
            } else {
                sb.append(CHOSEONG.charAt(cho));
            }
        } else if (jung != -1) {
            sb.append(JUNGSEONG.charAt(jung));
        }
        return sb.toString();
    }

    private void flush() {
        if (cho != -1) {
            if (jung != -1) {
                committed.append((char) (0xAC00 + (cho * 21 + jung) * 28 + jong));
            } else {
                committed.append(CHOSEONG.charAt(cho));
            }
        } else if (jung != -1) {
            committed.append(JUNGSEONG.charAt(jung));
        }
        cho = -1;
        jung = -1;
        jong = 0;
    }

    public void typeSpace() {
        flush();
        committed.append(' ');
    }

    public void typeJamo(char ch) {
        int cIdx = CHOSEONG.indexOf(ch);
        int vIdx = JUNGSEONG.indexOf(ch);
        if (cIdx >= 0) {
            inputConsonant(ch, cIdx);
        } else if (vIdx >= 0) {
            inputVowel(vIdx);
        } else {
            flush();
            committed.append(ch);
        }
    }

    private void inputConsonant(char ch, int cIdx) {
        if (cho == -1 && jung == -1) {
            cho = cIdx;
            return;
        }
        if (cho != -1 && jung == -1) {
            flush();
            cho = cIdx;
            return;
        }
        if (cho != -1 && jung != -1) {
            if (jong == 0) {
                int jIdx = JONGSEONG.indexOf(ch);
                if (jIdx > 0) {
                    jong = jIdx;
                    return;
                }
            } else {
                int combined = combineJong(jong, ch);
                if (combined > 0) {
                    jong = combined;
                    return;
                }
            }
            flush();
            cho = cIdx;
            return;
        }
        flush();
        cho = cIdx;
    }

    private void inputVowel(int vIdx) {
        if (cho == -1 && jung == -1) {
            jung = vIdx;
            return;
        }
        if (cho != -1 && jung == -1) {
            jung = vIdx;
            return;
        }
        if (cho == -1 && jung != -1) {
            int comb = combineJung(jung, vIdx);
            if (comb >= 0) {
                jung = comb;
            } else {
                flush();
                jung = vIdx;
            }
            return;
        }
        if (jong == 0) {
            int comb = combineJung(jung, vIdx);
            if (comb >= 0) {
                jung = comb;
            } else {
                flush();
                jung = vIdx;
            }
        } else {
            int[] split = splitJong(jong);
            if (split != null) {
                jong = split[0];
                int nextCho = split[1];
                flush();
                cho = nextCho;
                jung = vIdx;
            } else {
                char jongChar = JONGSEONG.charAt(jong);
                int nextCho = CHOSEONG.indexOf(jongChar);
                jong = 0;
                flush();
                cho = nextCho;
                jung = vIdx;
            }
        }
    }

    public void backspace() {
        if (jong != 0) {
            int[] split = splitJong(jong);
            jong = split != null ? split[0] : 0;
            return;
        }
        if (jung != -1) {
            int prev = splitJungFirst(jung);
            jung = prev >= 0 ? prev : -1;
            return;
        }
        if (cho != -1) {
            cho = -1;
            return;
        }
        if (committed.length() > 0) {
            committed.deleteCharAt(committed.length() - 1);
        }
    }

    private int combineJung(int a, int b) {
        if (a == 8 && b == 0) return 9;
        if (a == 8 && b == 1) return 10;
        if (a == 8 && b == 20) return 11;
        if (a == 13 && b == 4) return 14;
        if (a == 13 && b == 5) return 15;
        if (a == 13 && b == 20) return 16;
        if (a == 18 && b == 20) return 19;
        return -1;
    }

    private int splitJungFirst(int v) {
        if (v == 9 || v == 10 || v == 11) return 8;
        if (v == 14 || v == 15 || v == 16) return 13;
        if (v == 19) return 18;
        return -1;
    }

    private int combineJong(int j, char ch) {
        char a = JONGSEONG.charAt(j);
        if (a == 'ㄱ' && ch == 'ㅅ') return JONGSEONG.indexOf('ㄳ');
        if (a == 'ㄴ' && ch == 'ㅈ') return JONGSEONG.indexOf('ㄵ');
        if (a == 'ㄴ' && ch == 'ㅎ') return JONGSEONG.indexOf('ㄶ');
        if (a == 'ㄹ' && ch == 'ㄱ') return JONGSEONG.indexOf('ㄺ');
        if (a == 'ㄹ' && ch == 'ㅁ') return JONGSEONG.indexOf('ㄻ');
        if (a == 'ㄹ' && ch == 'ㅂ') return JONGSEONG.indexOf('ㄼ');
        if (a == 'ㄹ' && ch == 'ㅅ') return JONGSEONG.indexOf('ㄽ');
        if (a == 'ㄹ' && ch == 'ㅌ') return JONGSEONG.indexOf('ㄾ');
        if (a == 'ㄹ' && ch == 'ㅍ') return JONGSEONG.indexOf('ㄿ');
        if (a == 'ㄹ' && ch == 'ㅎ') return JONGSEONG.indexOf('ㅀ');
        if (a == 'ㅂ' && ch == 'ㅅ') return JONGSEONG.indexOf('ㅄ');
        return 0;
    }

    private int[] splitJong(int j) {
        char c = JONGSEONG.charAt(j);
        switch (c) {
            case 'ㄳ': return new int[]{JONGSEONG.indexOf('ㄱ'), CHOSEONG.indexOf('ㅅ')};
            case 'ㄵ': return new int[]{JONGSEONG.indexOf('ㄴ'), CHOSEONG.indexOf('ㅈ')};
            case 'ㄶ': return new int[]{JONGSEONG.indexOf('ㄴ'), CHOSEONG.indexOf('ㅎ')};
            case 'ㄺ': return new int[]{JONGSEONG.indexOf('ㄹ'), CHOSEONG.indexOf('ㄱ')};
            case 'ㄻ': return new int[]{JONGSEONG.indexOf('ㄹ'), CHOSEONG.indexOf('ㅁ')};
            case 'ㄼ': return new int[]{JONGSEONG.indexOf('ㄹ'), CHOSEONG.indexOf('ㅂ')};
            case 'ㄽ': return new int[]{JONGSEONG.indexOf('ㄹ'), CHOSEONG.indexOf('ㅅ')};
            case 'ㄾ': return new int[]{JONGSEONG.indexOf('ㄹ'), CHOSEONG.indexOf('ㅌ')};
            case 'ㄿ': return new int[]{JONGSEONG.indexOf('ㄹ'), CHOSEONG.indexOf('ㅍ')};
            case 'ㅀ': return new int[]{JONGSEONG.indexOf('ㄹ'), CHOSEONG.indexOf('ㅎ')};
            case 'ㅄ': return new int[]{JONGSEONG.indexOf('ㅂ'), CHOSEONG.indexOf('ㅅ')};
            default: return null;
        }
    }
}
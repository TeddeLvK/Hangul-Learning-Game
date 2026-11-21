package com.koreangame;

public class Achievement {
    public final String id;
    public final String title;
    public final String koreanTitle;
    public final String description;
    public final boolean specialBadge;

    public Achievement(String id, String title, String koreanTitle, String description, boolean specialBadge) {
        this.id = id;
        this.title = title;
        this.koreanTitle = koreanTitle;
        this.description = description;
        this.specialBadge = specialBadge;
    }
}

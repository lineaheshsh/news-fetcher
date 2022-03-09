package com.zzangho.newsfetcher.news.model;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;

@Getter
@NoArgsConstructor
@Entity(name = "view_news")
@Table
public class News {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private String contents_id;

    private String domain;
    private String category_nm;
    private String title;
    private String contents;
    private String writer;
    private String date;
    private String ampm;
    private String time;
    private String company;
    private String url;
    private String udt_dt;

    @Builder
    News(String contents_id, String domain, String category_nm, String title, String contents,
         String writer, String date, String ampm, String time, String company, String url, String udt_dt) {
        this.contents_id = contents_id;
        this.domain = domain;
        this.category_nm = category_nm;
        this.title = title;
        this.contents = contents;
        this.writer = writer;
        this.date = date;
        this.ampm = ampm;
        this.time = time;
        this.company = company;
        this.url = url;
        this.udt_dt = udt_dt;
    }
}

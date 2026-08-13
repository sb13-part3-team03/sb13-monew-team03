package com.codeit.monew.article.entity;

import com.codeit.monew.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Entity
@Table(name = "articles")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Article extends BaseEntity {

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ArticleSource source;

    @Column(name = "source_url",nullable = false)
    private String sourceUrl;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String summary;

    @Column(name = "published_at", nullable = false)
    private Instant publishedAt;

    @Column(name = "deleted_at")
    private Instant deletedAt;

}

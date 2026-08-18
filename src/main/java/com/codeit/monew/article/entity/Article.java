package com.codeit.monew.article.entity;

import com.codeit.monew.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Entity
@Table(
        name = "articles",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_articles_source_url",
                        columnNames = "source_url"
                )
        })
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Article extends BaseEntity {

    private Article(
            ArticleSource source,
            String sourceUrl,
            String title,
            String summary,
            Instant publishDate
    ) {
        this.source = source;
        this.sourceUrl = sourceUrl;
        this.title = title;
        this.summary = summary;
        this.publishDate = publishDate;
    }

    public static Article create(
            ArticleSource source,
            String sourceUrl,
            String title,
            String summary,
            Instant publishDate
    ) {
        return new Article(
                source,
                sourceUrl,
                title,
                summary,
                publishDate
        );
    }

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
    private Instant publishDate;

    @Column(name = "deleted_at")
    private Instant deletedAt;

    public void delete() {
        if (deletedAt == null) {
            this.deletedAt = Instant.now();
        }
    }

    public boolean isDeleted() {
        return deletedAt != null;
    }
}

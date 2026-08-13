package com.codeit.monew.article.entity;

import com.codeit.monew.global.entity.BaseEntity;
import jakarta.persistence.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "articles")
public class Article extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

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

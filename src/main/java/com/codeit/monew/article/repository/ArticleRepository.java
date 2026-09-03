package com.codeit.monew.article.repository;

import com.codeit.monew.article.entity.Article;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ArticleRepository extends JpaRepository<Article, UUID>, ArticleRepositoryCustom {

    Optional<Article> findBySourceUrl(String sourceUrl);

    Optional<Article> findByIdAndDeletedAtIsNull(UUID articleId);

    List<Article> findByPublishDateGreaterThanEqualAndPublishDateLessThan(Instant from, Instant to);

    @Query("""
    SELECT DISTINCT a.publishDate
    FROM Article a
    WHERE a.publishDate IS NOT NULL
    """)
    List<Instant> findDistinctPublishDates();

    @Modifying
    @Query(value = """
            INSERT INTO articles (
                id,
                source,
                source_url,
                title,
                summary,
                published_at,
                deleted_at,
                created_at,
                updated_at
            )
            VALUES (
                :id,
                :source,
                :sourceUrl,
                :title,
                :summary,
                :publishDate,
                :deletedAt,
                CURRENT_TIMESTAMP,
                CURRENT_TIMESTAMP
            )
            """, nativeQuery = true)
    void insertForRestore(
            @Param("id") UUID id,
            @Param("source") String source,
            @Param("sourceUrl") String sourceUrl,
            @Param("title") String title,
            @Param("summary") String summary,
            @Param("publishDate") Instant publishDate,
            @Param("deletedAt") Instant deletedAt
    );

    List<Article> findAllBySourceUrlIn(List<String> sourceUrls);

}

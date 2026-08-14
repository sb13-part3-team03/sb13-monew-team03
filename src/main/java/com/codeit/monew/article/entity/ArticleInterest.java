package com.codeit.monew.article.entity;

import com.codeit.monew.interest.entity.Interest;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "article_interests")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ArticleInterest {

    @EmbeddedId
    private ArticleInterestId id;

    @MapsId("articleId")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "article_id", nullable = false)
    private Article article;

    @MapsId("interestId")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "interest_id", nullable = false)
    private Interest interest;

    private ArticleInterest(Article article, Interest interest) {
        this.article = article;
        this.interest = interest;
        this.id = new ArticleInterestId(
                article.getId(),
                interest.getId()
        );
    }

    public static ArticleInterest create(
            Article article,
            Interest interest
    ) {
        return new ArticleInterest(article, interest);
    }
}
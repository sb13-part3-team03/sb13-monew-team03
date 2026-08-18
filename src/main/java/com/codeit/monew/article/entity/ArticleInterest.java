package com.codeit.monew.article.entity;

import com.codeit.monew.global.entity.BaseEntity;
import com.codeit.monew.interest.entity.Interest;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "article_interests")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ArticleInterest extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "article_id", nullable = false)
    private Article article;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "interest_id", nullable = false)
    private Interest interest;

}

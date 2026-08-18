package com.codeit.monew.article.entity;

import com.codeit.monew.global.entity.BaseEntity;
import com.codeit.monew.user.entity.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "article_views")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ArticleView extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "article_id", nullable = false)
    private Article article;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

}

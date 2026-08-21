package com.codeit.monew.comment.entity;

import com.codeit.monew.article.entity.Article;
import com.codeit.monew.global.entity.BaseEntity;
import com.codeit.monew.user.entity.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.Instant;

@Entity
@Getter
@Setter
@Table(name = "comments")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Comment extends BaseEntity {

    @ManyToOne(cascade = CascadeType.REMOVE)
    @JoinColumn(name = "article_id", nullable = false)
    private Article article;

    @ManyToOne(cascade = CascadeType.REMOVE)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(length = 500, nullable = false)
    private String content;

    @Column
    private Instant deletedAt;


    public Comment(
            Article article,
            User user,
            String content
    ){
        this.article = article;
        this.user = user;
        this.content = content;
    }


    public void update(
            String content
    ){
        this.content = content;
    }

    // logical delete
    public void delete(){
        this.deletedAt = Instant.now();
    }

}

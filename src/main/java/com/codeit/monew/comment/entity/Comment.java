package com.codeit.monew.comment.entity;


import com.codeit.monew.article.entity.Article;
import com.codeit.monew.user.entity.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Comment {

    // temp fields
    // replace after create base entity
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    @CreatedDate
    private LocalDateTime createdAt;
    @LastModifiedDate
    private LocalDateTime updatedAt;


    @ManyToOne
    @JoinColumn(name = "article_id", nullable = false)
    private Article article;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(length = 500)
    private String content;

    private LocalDateTime deleted_at;


    public Comment(
            Article article,
            User user,
            String content
    ){
        this.article = article;
        this.user = user;
        this.content = content;
    }


    public Comment update(
            String content
    ){
        this.content = content;
        this.updatedAt = LocalDateTime.now();
        return this;
    }

    // logical delete
    public void delete(){
        this.deleted_at = LocalDateTime.now();
    }

}

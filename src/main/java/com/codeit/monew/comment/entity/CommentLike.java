package com.codeit.monew.comment.entity;

import com.codeit.monew.global.entity.BaseEntity;
import com.codeit.monew.user.entity.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "comment_likes")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CommentLike extends BaseEntity {

    @ManyToOne(optional = false, cascade = CascadeType.REMOVE)
    @JoinColumn(name = "comment_id", nullable = false)
    private Comment comment;

    @ManyToOne(optional = false, cascade = CascadeType.REMOVE)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    public CommentLike(
            Comment comment,
            User user
    ){
        this.comment = comment;
        this.user = user;
    }
}

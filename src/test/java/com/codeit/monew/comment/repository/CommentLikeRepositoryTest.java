package com.codeit.monew.comment.repository;


import com.codeit.monew.article.entity.Article;
import com.codeit.monew.comment.entity.Comment;
import com.codeit.monew.comment.entity.CommentLike;
import com.codeit.monew.global.config.JpaAuditingConfig;
import com.codeit.monew.global.config.QuerydslConfig;
import com.codeit.monew.user.entity.User;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

@DataJpaTest
@DisplayName("CommentLike Repository test")
@ActiveProfiles("test")
@Import({
        JpaAuditingConfig.class,
        QuerydslConfig.class
})
@Slf4j
public class CommentLikeRepositoryTest {
    @Autowired
    TestEntityManager entityManager;
    @Autowired
    CommentRepository commentRepository;


    public CommentLike getCommentLike(Comment comment, User user){
        return new CommentLike(comment,user);
    }

    public Comment getComment(Article article, User user, String content){
        return new Comment(article, user, content);
    }






}

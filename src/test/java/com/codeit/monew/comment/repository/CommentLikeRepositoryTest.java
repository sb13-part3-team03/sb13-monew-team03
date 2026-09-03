package com.codeit.monew.comment.repository;


import com.codeit.monew.article.entity.Article;
import com.codeit.monew.article.entity.ArticleSource;
import com.codeit.monew.comment.dto.command.CommentLikeDtoCreateCommand;
import com.codeit.monew.comment.entity.Comment;
import com.codeit.monew.comment.entity.CommentLike;
import com.codeit.monew.global.config.JpaAuditingConfig;
import com.codeit.monew.global.config.QuerydslConfig;
import com.codeit.monew.user.entity.User;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

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
    CommentLikeRepository commentLikeRepository;

    HashMap<String,List<?>> container = new HashMap<>();

    String TEAT_NAME = "CommentLike Test";


    private CommentLike getCommentLike(Comment comment, User user){
        CommentLike commentLike = new CommentLike(comment,user);

        entityManager.persist(commentLike);

        log.debug("{} - commentLike set. id: : {}, user : {}", TEAT_NAME, commentLike.getId(), commentLike.getUser());

        return commentLike;
    }

    private Comment getComment(Article article, User user, String content){
        Comment comment = new Comment(article, user, content);

        entityManager.persist(comment);

        log.debug("{} - comment set. id : {}, contents : {}", TEAT_NAME, comment.getId(), comment.getContent());

        return comment;
    }

    private User getUser(String nickname){
        String email = nickname + "@email.com";

        User user = new User(email, nickname, "password");

        entityManager.persist(user);

        log.debug("{} - user set. id : {}, nickname {}", TEAT_NAME,user.getId(),user.getNickname());

        return user;
    }

    private Article getArticle(String title){
        Article article = Article.create(
                ArticleSource.NAVER,
                "https://source.com",
                title,
                "summery",
                Instant.now()
        );

        entityManager.persist(article);

        log.debug("{} - article set. id : {}, title {}", TEAT_NAME, article.getId(), article.getTitle());

        return article;
    }

    @BeforeEach
    public void setup(){

        Article article = getArticle("a1");

        container.put("article",List.of(article));

        User user1 = getUser("kimsuki");
        User user2 = getUser("parksudang");
        User user3 = getUser("kangdang");

        container.put("user",List.of(user1,user2,user3));

        Comment comment1 = getComment(article,user1,"content");

        container.put("comment",List.of(container));

        CommentLike commentLike = getCommentLike(comment1,user1);
        CommentLike commentLike2 = getCommentLike(comment1,user2);
        CommentLike commentLike3 = getCommentLike(comment1,user3);

        container.put("commentLike",List.of(commentLike,commentLike2,commentLike3));

        entityManager.flush();
        entityManager.clear();
    }

    private <T> List<T> getContentsFromContainer(Class<T> cls, String key){
        List<?> contentLikes = container.get(key);
        if(contentLikes == null) throw new RuntimeException("Test Container Contents getting exception");
        return contentLikes.stream()
                .filter(cls::isInstance)
                .map(cls::cast)
                .toList();
    }


    @Test
    @DisplayName("CommentLike Test 1 - Query one CommentLike to CommentLike Dto Create Command from UUID")
    public void testQuery(){
        // the method return all information on dto at one times.

        List<CommentLike> commentLikes = getContentsFromContainer(CommentLike.class,"commentLike");
        List<User> users = getContentsFromContainer(User.class,"user");

        CommentLike commentlike1 = commentLikes.get(0);
        User user1 = users.get(0);

        CommentLikeDtoCreateCommand command = commentLikeRepository.findCommentLikeByIdToDtoCommand(commentlike1.getId());

        log.debug(
                "{} - CommentLikeDto information \nid - {},\nnickname - {},\ncomment - {},\nlikecount - {} ",
                TEAT_NAME,
                command.id(),
                command.commentUserNickname(),
                command.commentContent(),
                command.commentLikeCount()
        );

        assertThat(command.id()).isEqualTo(commentlike1.getId());
        assertThat(command.commentUserNickname()).isEqualTo(user1.getNickname());
    }




}

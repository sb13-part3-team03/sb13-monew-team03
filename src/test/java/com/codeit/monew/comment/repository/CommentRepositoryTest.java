package com.codeit.monew.comment.repository;

import com.codeit.monew.article.entity.Article;
import com.codeit.monew.article.entity.ArticleSource;
import com.codeit.monew.comment.dto.command.CommentDtoCreateCommand;
import com.codeit.monew.comment.entity.Comment;
import com.codeit.monew.global.config.QueryDslConfig;
import com.codeit.monew.user.entity.User;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.annotation.Transactional;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

@DataJpaTest
@DisplayName("comment repository test")
@ActiveProfiles("test")
@Import(QueryDslConfig.class)
@Slf4j
public class CommentRepositoryTest {
    @Autowired
    TestEntityManager entityManager;
    @Autowired
    CommentRepository commentRepository;

    private ArrayList<Comment> setup(UUID... userIds){
        // 사용될 user, article, comment, commentLike 객체 준비
        try{
            Article article = getArticleMock();
            ArrayList<Comment> comments = new ArrayList<>();
            int counter = 0;
            for(UUID userid : userIds){
                User user = getUserMock(userid,"user" + counter);
                comments.add(getComment(article,user));
                counter++;
            }
            entityManager.flush();
            entityManager.clear();
            return comments;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private Comment getComment(Article article, User user){
        Comment comment = new Comment(article, user, "content");
        ReflectionTestUtils.setField(comment,"createdAt", Instant.now());
        ReflectionTestUtils.setField(comment,"updatedAt", Instant.now());
        log.debug(">> before comment information - {}, {}, {}",comment.getId(),comment.getCreatedAt(),comment.getUpdatedAt());
        entityManager.persist(comment);
        log.debug(">> after comment information - {}, {}, {}",comment.getId(),comment.getCreatedAt(),comment.getUpdatedAt());
        return comment;
    }

    private Article getArticleMock() throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        // no constructor written at 08-14.
        // use reflection api

//        Class<?> articleClass = Article.class;
        Constructor<Article> articleConstructor = Article.class.getDeclaredConstructor();
        articleConstructor.setAccessible(true);
        Article article = articleConstructor.newInstance();

        ReflectionTestUtils.setField(article,"createdAt", Instant.now());
        ReflectionTestUtils.setField(article,"updatedAt", Instant.now());
        ReflectionTestUtils.setField(article,"source", ArticleSource.NAVER);
        ReflectionTestUtils.setField(article,"sourceUrl", "https://localhost");
        ReflectionTestUtils.setField(article,"title", "title");
        ReflectionTestUtils.setField(article,"summary", "summary");
        ReflectionTestUtils.setField(article,"publishDate", Instant.now());


        log.debug(">> before article information - {}, {}, {}",article.getId(),article.getCreatedAt(),article.getUpdatedAt());
        entityManager.persist(article);
        log.debug(">> after article information - {}, {}, {}",article.getId(),article.getCreatedAt(),article.getUpdatedAt());
        return article;
    }

    private User getUserMock(UUID id, String nickname){
        User user = new User(nickname + "@email.com",nickname,"password",Instant.now());
        entityManager.persist(user);
        return user;
    }

    @Test
    @DisplayName("get CommentDtoCreateCommand method test")
    @Transactional
    public void getDtoCommentTest(){
        // 매서드가 지정한 커맨트를 잘 받아오는지 테스트.
        UUID user1Id = UUID.randomUUID();
        UUID user2Id = UUID.randomUUID();

        List<Comment> comments = setup(user1Id,user2Id);

        CommentDtoCreateCommand command = commentRepository
                .getDtoCommandById(comments.get(0).getId())
                .orElseThrow(() -> new RuntimeException("getDtoCommentTest failed. result is null."));

        assertThat(command.id()).isEqualTo(comments.get(0).getId());
        log.debug("created command info - {}",command);
    }
}

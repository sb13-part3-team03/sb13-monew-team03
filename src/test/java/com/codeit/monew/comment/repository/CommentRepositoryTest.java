package com.codeit.monew.comment.repository;

import com.codeit.monew.article.entity.Article;
import com.codeit.monew.article.entity.ArticleSource;
import com.codeit.monew.comment.dto.command.CommentDtoCreateCommand;
import com.codeit.monew.comment.dto.command.CommentQueryCommand;
import com.codeit.monew.comment.entity.Comment;
import com.codeit.monew.comment.entity.CommentLike;
import com.codeit.monew.global.config.QuerydslConfig;
import com.codeit.monew.user.entity.User;
import lombok.extern.slf4j.Slf4j;
import net.bytebuddy.utility.RandomString;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Slice;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.annotation.Transactional;

import java.lang.reflect.Constructor;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;

import static org.assertj.core.api.Assertions.*;

@DataJpaTest
@DisplayName("comment repository test")
@ActiveProfiles("test")
@Import(QuerydslConfig.class)
@Slf4j
public class CommentRepositoryTest {
    @Autowired
    TestEntityManager entityManager;
    @Autowired
    CommentRepository commentRepository;

    private record TestCommentCreateCommand(
            User user,
            Article article,
            List<User> likeUserList,
            Instant createdAt
    ){}

    private List<Comment> setCommentFromCommand(TestCommentCreateCommand... commands){
        List<Comment> commentList = new ArrayList<>();
        Map<UUID,User> userList = new HashMap<>();
        // set Comment and User first
        for (TestCommentCreateCommand command : commands) {
            // set createAt manually
            Comment comment = getComment(command.article(), command.user(), command.createdAt());

            setCommentLikes(comment,command.likeUserList(),comment.getCreatedAt());

            commentList.add(comment);
        }
        // registry db
        entityManager.flush();
        entityManager.clear();

        // return created comment to list
        return commentList;
    }

    private void setCommentLikes(Comment comment, List<User> commentLikeUserList, Instant commentCtime){
        int counter = 15;
        for (User user : commentLikeUserList){
            CommentLike commentLike = new CommentLike(comment,user);
            ReflectionTestUtils.setField(commentLike,"createdAt",commentCtime.plus(counter,ChronoUnit.MINUTES));

            // no updated at in edr but base entity has only c and mtime has.
            ReflectionTestUtils.setField(commentLike,"updatedAt",commentCtime.plus(counter,ChronoUnit.MINUTES));

            entityManager.persist(commentLike);

            counter = counter + 15;
        }
    }



    private ArrayList<Comment> setup(UUID... userIds){
        // 사용될 user, article, comment, commentLike 객체 준비
        try{
            Article article = getArticleMock();
            ArrayList<Comment> comments = new ArrayList<>();
            for(UUID userid : userIds){
                User user = getUserMock(userid);
                comments.add(getComment(article,user,Instant.now()));
            }
            entityManager.flush();
            entityManager.clear();
            return comments;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private Comment getComment(Article article, User user, Instant ctime){
        Comment comment = new Comment(article, user, "content");
        ReflectionTestUtils.setField(comment,"createdAt", ctime);
        ReflectionTestUtils.setField(comment,"updatedAt", ctime);
        log.debug(">> before comment information - {}, {}, {}",comment.getId(),comment.getCreatedAt(),comment.getUpdatedAt());
        entityManager.persist(comment);
        log.debug(">> after comment information - {}, {}, {}",comment.getId(),comment.getCreatedAt(),comment.getUpdatedAt());
        return comment;
    }

    private Article getArticleMock() {
        // no constructor written at 08-14.
        // use reflection api
        try {
            Constructor<Article> articleConstructor = Article.class.getDeclaredConstructor();
            articleConstructor.setAccessible(true);
            Article article = articleConstructor.newInstance();

            // string for unique validation field
            String salt = RandomString.make(10);

            ReflectionTestUtils.setField(article,"createdAt", Instant.now());
            ReflectionTestUtils.setField(article,"updatedAt", Instant.now());
            ReflectionTestUtils.setField(article,"source", ArticleSource.NAVER);
            ReflectionTestUtils.setField(article,"sourceUrl", "https://localhost/" + salt);
            ReflectionTestUtils.setField(article,"title", "title");
            ReflectionTestUtils.setField(article,"summary", "summary");
            ReflectionTestUtils.setField(article,"publishDate", Instant.now());


            log.debug(">> before article information - {}, {}, {}",article.getId(),article.getCreatedAt(),article.getUpdatedAt());
            entityManager.persist(article);
            log.debug(">> after article information - {}, {}, {}",article.getId(),article.getCreatedAt(),article.getUpdatedAt());
            return article;
        } catch (Exception e) {
            log.warn("article mock create error",e);
            throw new RuntimeException(e);
        }
    }

    private User getUserMock(UUID id){
        // set nickname from uuid
        String nickname = id.toString().substring(0,7);

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



    @Nested
    @DisplayName("Repository test - getAllCommentsWithCursor Method")
    class Query {
        /*
        레파지토리 작동에 관련된 책임 코드를 슬라이스 체크 한다.
        체크 사항
        1. command 가 적절한 데이터를 조회하는지
        2. 정렬 및, 기사 조건에 따라 정확한 데이터를 반환하는지
        3. 실패사항에 의도된 에러를 반환하는지.
         */


        private final List<Article> articleList = new ArrayList<>();
        private final List<User> userIdList = new ArrayList<>();
        private final List<Comment> commentList = new ArrayList<>();


        @BeforeEach
        public void setTestEnv(){
            Article article1 = getArticleMock();
            Article article2 = getArticleMock();

            articleList.add(article1);
            articleList.add(article2);

            User user1 = getUserMock(UUID.randomUUID());
            User user2 = getUserMock(UUID.randomUUID());
            User user3 = getUserMock(UUID.randomUUID());
            User user4 = getUserMock(UUID.randomUUID());

            userIdList.add(user1);
            userIdList.add(user2);
            userIdList.add(user3);
            userIdList.add(user4);

            // test comment create at different time and like count.
            // 3 comment to article 1, 1 comment for article 2
            TestCommentCreateCommand command1 = new TestCommentCreateCommand(user1,article1,List.of(user1,user2),Instant.now().minus(30, ChronoUnit.MINUTES));
            TestCommentCreateCommand command2 = new TestCommentCreateCommand(user2,article1,List.of(),Instant.now().minus(10,ChronoUnit.HOURS));
            TestCommentCreateCommand command3 = new TestCommentCreateCommand(user3,article2,List.of(user1),Instant.now().minus(5,ChronoUnit.DAYS));
            TestCommentCreateCommand command4 = new TestCommentCreateCommand(user4,article1,List.of(user1,user2,user3,user4),Instant.now().minus(1,ChronoUnit.HOURS));

            commentList.addAll(setCommentFromCommand(command1,command2,command3,command4));
        }

        @Test
        @DisplayName("query article 1, sort by createdAt desc. no cursor and size is 2")
        @Transactional
        public void getAllCommentWithConditionTest(){
            // given
            // setup comment, user, article ...

            // set test command(request)
            CommentQueryCommand command = new CommentQueryCommand(
                    articleList.get(0).getId(), // article 1
                    "createdAt",
                    "desc",
                    null,
                    null,
                    2L,
                    userIdList.get(2).getId() // userid3
            );

            // query comments
            Slice<CommentDtoCreateCommand> result = commentRepository.getAllCommentsWithCursor(command);

            // query comment count
            // the number of comment will get countByDeletedAtIsEmpty() or countAllByArticleId() method
            Long allElementCount = commentRepository.countAllByDeletedAtIsNullAndArticleId(articleList.get(0).getId());

            log.debug("result - {}", result);

            assertThat(result.getNumberOfElements()).isEqualTo(2);
            assertThat(allElementCount).isEqualTo(3);      // article 1 is contained 3 comments

            // returned first comment equal to comment 1
            assertThat(
                    result.getContent().get(0).id()
            ).isEqualTo(
                    commentList.get(0).getId()
            );

        }

        @Test
        @DisplayName("query no article, sort by createdAt asc. no cursor and size is 2")
        @Transactional
        public void getAllCommentWithConditionTest2(){
            // given
            // setup comment, user, article ...

            // set test command(request)
            CommentQueryCommand command = new CommentQueryCommand(
                    null, // article 1
                    "createdAt",
                    "asc",
                    null,
                    null,
                    2L,
                    userIdList.get(2).getId() // userid3
            );

            // query comments
            Slice<CommentDtoCreateCommand> result = commentRepository.getAllCommentsWithCursor(command);

            // query comment count
            // the number of comment will get countByDeletedAtIsEmpty() or countAllByDeletedAtIsNullAndArticleId()() method
            Long allElementCount = commentRepository.countByDeletedAtIsNull();

            log.debug("Query result - get element count : {}, query result : {}", allElementCount, result.getContent());

            assertThat(result.getNumberOfElements()).isEqualTo(2);
            assertThat(allElementCount).isEqualTo(4);      // all article has contained 3 comments

            // returned oldest comment equal to comment 3
            assertThat(
                    result.getContent().get(0).id()
            ).isEqualTo(
                    commentList.get(2).getId()
            );

        }

        @Test
        @DisplayName("query 1 article, sort by like count asc. no cursor and size is 2")
        @Transactional
        public void getAllCommentWithConditionTest3(){
            // given
            // setup comment, user, article ...

            // set test command(request)
            CommentQueryCommand command = new CommentQueryCommand(
                    articleList.get(0).getId(), // article 1
                    "likeCount",
                    "asc",
                    null,
                    null,
                    2L,
                    userIdList.get(0).getId() // userid1
            );

            // query comments
            Slice<CommentDtoCreateCommand> result = commentRepository.getAllCommentsWithCursor(command);

            // query comment count
            // the number of comment will get countByDeletedAtIsEmpty() or countAllByDeletedAtIsNullAndArticleId()() method
            Long allElementCount = commentRepository.countAllByDeletedAtIsNullAndArticleId(articleList.get(0).getId());

            log.debug("Query result - get element count : {}, query result : {}", allElementCount, result.getContent());

            assertThat(result.getSize()).isEqualTo(2);
            assertThat(allElementCount).isEqualTo(3);      // all article has contained 3 comments

            // returned second like count comment is comment 1
            assertThat(
                    result.getContent().get(1).id()
            ).isEqualTo(
                    commentList.get(0).getId()
            );

            // the comment was liked by self
            assertThat(result.getContent().get(1).likeByMe()).isEqualTo(true);
        }

        @Test
        @DisplayName("query 1 article, sort by like count asc. has cursor and size is 2")
        @Transactional
        public void getAllCommentWithConditionTest4(){
            // given
            // setup comment, user, article ...

            // set test command(request)
            CommentQueryCommand command = new CommentQueryCommand(
                    articleList.get(0).getId(), // article 1
                    "likeCount",
                    "asc",
                    "2",
                    commentList.get(2).getCreatedAt().toString(),
                    2L,
                    userIdList.get(0).getId() // userid1
            );

            // query comments
            Slice<CommentDtoCreateCommand> result = commentRepository.getAllCommentsWithCursor(command);

            // query comment count
            // the number of comment will get countByDeletedAtIsEmpty() or countAllByArticleId() method
            Long allElementCount = commentRepository.countAllByDeletedAtIsNullAndArticleId(articleList.get(0).getId());

            log.debug("Query result - get element count : {}, query result : {}", allElementCount, result.getContent());

            assertThat(result.getSize()).isEqualTo(2);      // page 2 has 2 size
            assertThat(result.getNumberOfElements()).isEqualTo(1);      // page 2 has 1comments
            assertThat(allElementCount).isEqualTo(3);      // all article has contained 3 comments

            // returned first(third comment by article) like count comment is comment 4
            assertThat(
                    result.getContent().get(0).id()
            ).isEqualTo(
                    commentList.get(3).getId()
            );

            // the comment was liked by self
            assertThat(result.getContent().get(0).likeByMe()).isEqualTo(true);


        }

    }




}

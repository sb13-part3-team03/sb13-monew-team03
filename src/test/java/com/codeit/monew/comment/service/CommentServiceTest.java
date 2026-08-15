package com.codeit.monew.comment.service;


import com.codeit.monew.article.entity.Article;
import com.codeit.monew.article.repository.ArticleRepository;
import com.codeit.monew.comment.dto.command.CommentCreateCommand;
import com.codeit.monew.comment.dto.command.CommentDtoCreateCommand;
import com.codeit.monew.comment.entity.Comment;
import com.codeit.monew.comment.mapper.CommentMapper;
import com.codeit.monew.comment.repository.CommentRepository;
import com.codeit.monew.user.entity.User;
import com.codeit.monew.user.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;
import static org.mockito.Mockito.mock;

@ActiveProfiles("test")
@ExtendWith(MockitoExtension.class)
@Slf4j
public class CommentServiceTest {

    @Mock
    CommentRepository commentRepository;
    @Mock
    ArticleRepository articleRepository;
    @Mock
    UserRepository userRepository;

    // none use but used in service bean
    @Mock
    CommentMapper commentMapper;

    @InjectMocks
    CommentServiceImpl commentService;

    private Optional<Article> getArticleMock(UUID articleId){
        Article article = mock(Article.class);
        ReflectionTestUtils.setField(article,"id",articleId);
        return Optional.of(article);
    }

    private Optional<User> getUserMock(UUID userId){
        User user = mock(User.class);
        ReflectionTestUtils.setField(user,"id",userId);
        return Optional.of(user);
    }

    private Optional<CommentDtoCreateCommand> getCommentDtoFromUUID(UUID commentId){
        return Optional.of(new CommentDtoCreateCommand(
                commentId,
                null,
                null,
                "nickname",
                "content",
                0L,
                false,
                Instant.now()
        ));
    }


    @Test
    @DisplayName("comment registry situation with request")
    public void registryTest(){
        // 1. 지정된 정보로 comment 가 생성되었는가.
        // 2. comment 의 정보로 dtoCreateCommand 가 생성 되었는가.

        // set article
        UUID articleId = UUID.randomUUID();
        // set user
        UUID userId = UUID.randomUUID();
        // set content
        String content = "test content text";
        // capture obj for check repository return
        AtomicReference<Comment> createdObject = new AtomicReference<>();
        AtomicReference<CommentDtoCreateCommand> createdCommand = new AtomicReference<>();
        // set user and article has returned.
        given(userRepository.findById(any(UUID.class))).willReturn(getUserMock(userId));
        given(articleRepository.findById(any(UUID.class))).willReturn(getArticleMock(articleId));

        // comment repository will return given comment when save() method
        given(commentRepository.save(any(Comment.class))).willAnswer(
                in -> {
                    Comment repositoryReturn = in.getArgument(0);
                    // set id v- simulate repository saved
                    ReflectionTestUtils.setField(repositoryReturn,"id",UUID.randomUUID());
                    createdObject.set(repositoryReturn);
                    return repositoryReturn;
                }
        );

        given(commentRepository.getDtoCommandById(any(UUID.class))).willAnswer(
                invm -> {
                    Optional<CommentDtoCreateCommand> command = getCommentDtoFromUUID(invm.getArgument(0));
                    command.ifPresent(createdCommand::set);
                    return command;
                }

        );

        commentService.registry(new CommentCreateCommand(articleId, userId, content));

        // check Comment is currently generated.
        assertThat(createdObject.get().getContent()).isEqualTo(content);
        // check Comment is currently mapped to dto object
        assertThat(createdCommand.get().id()).isEqualTo(createdObject.get().getId());
    }


}

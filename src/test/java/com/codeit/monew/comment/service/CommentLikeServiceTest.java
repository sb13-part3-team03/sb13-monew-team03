package com.codeit.monew.comment.service;


import com.codeit.monew.article.entity.Article;
import com.codeit.monew.comment.dto.command.CommentLikeDtoCreateCommand;
import com.codeit.monew.comment.dto.command.like.CommentLikeCancelCommand;
import com.codeit.monew.comment.dto.command.like.CommentLikeRegistryCommand;
import com.codeit.monew.comment.dto.response.CommentLikeDto;
import com.codeit.monew.comment.entity.Comment;
import com.codeit.monew.comment.entity.CommentLike;
import com.codeit.monew.comment.mapper.CommentMapper;
import com.codeit.monew.comment.repository.CommentLikeRepository;
import com.codeit.monew.comment.repository.CommentRepository;
import com.codeit.monew.user.entity.User;
import com.codeit.monew.user.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.platform.commons.util.ReflectionUtils;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

import static org.mockito.Mockito.mock;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

@ActiveProfiles("test")
@DisplayName("CommentLike service test")
@ExtendWith(MockitoExtension.class)
public class CommentLikeServiceTest {
    @Mock
    UserRepository userRepository;
    @Mock
    CommentLikeRepository commentLikeRepository;
    @Mock
    CommentRepository commentRepository;

    @InjectMocks
    CommentLikeServiceImpl commentLikeService;

    @Mock
    CommentMapper commentMapper;


    @Test
    @DisplayName("Comment like registry")
    public void registryTest(){

        // set target commentLike id.
        UUID commentLikeId = UUID.randomUUID();

        // use mock object for getting CommentLike information
        Comment comment = spy(Comment.class);
        User user = spy(User.class);

        when(comment.getArticle()).thenReturn(mock(Article.class));
        when(comment.getUser()).thenReturn(user);



        // checker object
        AtomicReference<CommentLikeDtoCreateCommand> savedCommentLikeInfo = new AtomicReference<>();

        // when
        // get resource
        given(userRepository.findById(any(UUID.class))).willReturn(Optional.of(user));
        given(commentRepository.findByIdAndDeletedAtIsNull(any(UUID.class))).willReturn(Optional.of(comment));

        // create and save commentLike
        given(commentLikeRepository.save(any(CommentLike.class)))
                .willAnswer(invocation -> {
                    CommentLike param = invocation.getArgument(0);

                    // set id, ctime, mtime for simulate repository.save() method set
                    ReflectionTestUtils.setField(param,"id",commentLikeId);
                    ReflectionTestUtils.setField(param,"createdAt", Instant.now());
                    ReflectionTestUtils.setField(param,"updatedAt", Instant.now());

                    return param;
                });

        // map to Dto
        given(commentMapper.toDto(any(CommentLikeDtoCreateCommand.class)))
                .willAnswer( invocation -> {
                    CommentLikeDtoCreateCommand command = invocation.getArgument(0);

                    savedCommentLikeInfo.set(command);

                    return getCommentLikeDtoFrom(command);
                });

        commentLikeService.registry(new CommentLikeRegistryCommand(commentLikeId,UUID.randomUUID()));

        // check comment covert to DTO
        assertThat(savedCommentLikeInfo.get().id()).isEqualTo(commentLikeId);

        // check save method successfully called
        verify(commentLikeRepository,times(1)).save(any(CommentLike.class));

    }


    @Test
    @DisplayName("cancel CommentLike test")
    public void cancelTest(){
        // db 에서 지워지는 것 으로 카운트도 설정되므로, db 에서 잘 지워지는지 확인

        UUID commentLikeId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        Comment comment = spy(Comment.class);

        given(commentRepository.findByIdAndDeletedAtIsNull(any(UUID.class)))
                .willReturn(Optional.of(comment));



        commentLikeService.cancel(new CommentLikeCancelCommand(commentLikeId,userId));

        // verify
//        verify(commentLikeRepository,times(1)).deleteById(commentLikeId);
    }




    private CommentLikeDto getCommentLikeDtoFrom(CommentLikeDtoCreateCommand command){
        return new CommentLikeDto(
                command.id(),
                command.likeBy(),
                command.createdAt(),
                command.commentId(),
                command.articleId(),
                command.commentUserId(),
                command.commentUserNickName(),
                command.commentContent(),
                command.commentLikeCount(),
                command.commentCreateAt()
        );
    }

}

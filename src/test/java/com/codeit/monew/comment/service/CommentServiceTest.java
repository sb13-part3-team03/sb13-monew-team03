package com.codeit.monew.comment.service;


import com.codeit.monew.article.entity.Article;
import com.codeit.monew.article.repository.ArticleRepository;
import com.codeit.monew.comment.dto.command.CommentCreateCommand;
import com.codeit.monew.comment.dto.command.CommentDtoCreateCommand;
import com.codeit.monew.comment.dto.command.CommentQueryCommand;
import com.codeit.monew.comment.dto.command.CursorContainerCreateCommand;
import com.codeit.monew.comment.dto.response.CommentDto;
import com.codeit.monew.comment.dto.response.CursorContainerDto;
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
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
import java.util.List;
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
        return Optional.of(getDtoCreateCommand(commentId,null,null,false));
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

    @Test
    @DisplayName("comment query with pagination")
    public void queryTest(){
        // given
        // set up comment for test
        UUID articleId = UUID.randomUUID();
        UUID user1 = UUID.randomUUID();
        UUID user2 = UUID.randomUUID();

        // order -> createdAt or likeCount
        String order = "createdAt";
        String direction = "DESC";
        String cursor = "";
        String after = "";
        Long size = 2L;
        UUID requestUser = user1;

        CommentQueryCommand command = getQueryCommand(
                articleId,
                order,
                direction,
                cursor,
                after,
                size,
                requestUser
        );

        // comment dto list for return repository.
        List<CommentDtoCreateCommand> dtoCreateCommands = getCommentDtoCommandList();

        // create checker response object
        int commandSize = dtoCreateCommands.size();
        CommentDtoCreateCommand cursorObject = dtoCreateCommands.get(commandSize - 1);

        // convert repository result to service return object
        CursorContainerDto<CommentDtoCreateCommand> response = getCursorResponse(
                dtoCreateCommands,
                cursorObject.createdAt().toString(),    // variable change need
                cursorObject.createdAt().toString(),
                (long) commandSize,
                false
        );

        log.info("TEST - Repository will return query size - {}",commandSize);
        log.info("TEST - the Cursor object is - {}", cursorObject);

        // order by date or commentLike(sub when date) <- repository spec
        // dto transfer test.
        // cursor on and off test

        // when
        given(commentRepository.getAllCommentsWithCursor(any(CommentQueryCommand.class)))
                .willReturn(getSliceFromList(dtoCreateCommands));

        setMapperCommentDtoFromCommand();
        setMapperCursorContainerFromCommand();

        CursorContainerDto<?> result = commentService.query(command);

        // then
        log.info("TEST - result check: answerObject - {}, result - {}",response,result);
        assertThat(response.size()).isEqualTo(result.size());
        assertThat(response.nextCursor()).isEqualTo(result.nextCursor());
        assertThat(response.nextAfter()).isEqualTo(result.nextAfter());
    }


    @Test
    @DisplayName("")










    private void setMapperCursorContainerFromCommand(){
        given(commentMapper.toDto(any(CursorContainerCreateCommand.class)))
                .willAnswer( invocation -> {
                            CursorContainerCreateCommand<?> parameter = invocation.getArgument(0);
                            return getCursorContainerDtoFromCommand(parameter);
                        }
                );
    }

    private void setMapperCommentDtoFromCommand(){
        // set covert CommentDtoCommand to CommentDto
        given(commentMapper.toDto(any(CommentDtoCreateCommand.class)))
                .willAnswer(
                        invocation -> {
                            CommentDtoCreateCommand parameter = invocation.getArgument(0);
                            return getCommentDtoFromCommand(parameter);
                        }
                );
    }

    private CommentDto getCommentDtoFromCommand(CommentDtoCreateCommand command){
        return new CommentDto(
                command.id(),
                command.articleId(),
                command.userId(),
                command.userNickName(),
                command.content(),
                command.likeCount(),
                command.likeByMe(),
                command.createdAt()
        );
    }

    private <T> CursorContainerDto<T> getCursorContainerDtoFromCommand(
            CursorContainerCreateCommand<T> command
    ){
        return new CursorContainerDto<>(
                command.contents(),
                command.nextCursor(),
                command.nextAfter(),
                command.size(),
                command.totalElement(),
                command.hasNext()
        );
    }

    private <T> Slice<T> getSliceFromList(List<T> contents){
        int size = contents.size();
        Pageable pageable = PageRequest.of(0,size);
        return new SliceImpl<>(contents,pageable,false);
    }

    private CursorContainerDto<CommentDtoCreateCommand> getCursorResponse(
            List<CommentDtoCreateCommand> commandList,
            String cursor,
            String after,
            Long totalElement,
            Boolean hasNext
    ){
        return new CursorContainerDto<CommentDtoCreateCommand>(
                commandList,
                cursor,
                after,
                (long) commandList.size(),
                totalElement,
                hasNext
        );
    }

    // get single CommentDtoCreateCommand Object
    private CommentDtoCreateCommand getDtoCreateCommand(
            UUID commentId,
            UUID articleId,
            UUID userId,
            Boolean likeByMe
    ){
        log.info("TEST - create CommentDto Command : comment - {}, article - {}, user - {}", commentId, articleId, userId);
        return new CommentDtoCreateCommand(
                commentId,
                articleId,
                userId,
                "nickname",
                "content",
                0L,
                likeByMe,
                Instant.now()
        );
    }


    // get CommentDtoCreateCommand list
    private List<CommentDtoCreateCommand> getCommentDtoCommandList(){
        List<CommentDtoCreateCommand> result = List.of(
                getDtoCreateCommand(UUID.randomUUID(),null,null,false),
                getDtoCreateCommand(UUID.randomUUID(),null,null,true)
        );
        log.info("TEST - getCommentDtoCommandList() : {}",result);
        return result;
    }


    // get Command for use param service.query()
    private CommentQueryCommand getQueryCommand(
            UUID articleId,
            String orderBy,
            String direction,
            String cursor,
            String after,
            Long size,
            UUID requestUserId
    ){
        return new CommentQueryCommand(articleId, orderBy, direction, cursor, after, size, requestUserId);
    }
}

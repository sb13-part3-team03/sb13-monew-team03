package com.codeit.monew.comment.repository;

import com.codeit.monew.comment.dto.command.CommentLikeDtoCreateCommand;
import com.codeit.monew.comment.entity.Comment;
import com.codeit.monew.comment.entity.CommentLike;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;
import java.util.UUID;

public interface CommentLikeRepository extends JpaRepository<CommentLike, UUID> {

    Boolean existsByComment_IdAndUser_Id(UUID commentId, UUID userId);

    @EntityGraph(attributePaths = {"user","comment"})
    Optional<CommentLike> findByComment_IdAndUser_Id(UUID commentId, UUID userId);

    Long countAllByComment(Comment comment);

    void deleteAllByComment(Comment comment);

    void deleteAllByComment_Article_Id(UUID id);

    @Query(
            """
            select
                        cl.id,
                        u.id,
                        cl.createdAt,
                        c.id,
                        c.article.id,
                        u.id,
                        u.nickname,
                        c.content,
                        (
                                    select count(cll)
                                    from CommentLike cll
                                    where cll.comment = c
                        ),
                        c.createdAt
            from CommentLike cl
                        join cl.comment c
                        join cl.user u
            where c.deletedAt is null
                        and cl.id = :commentLikeId
            """
    )
    CommentLikeDtoCreateCommand findCommentLikeByIdToDtoCommand(UUID commentLikeId);
}

package com.codeit.monew.comment.repository;

import com.codeit.monew.comment.entity.Comment;
import com.codeit.monew.comment.entity.CommentLike;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface CommentLikeRepository extends JpaRepository<CommentLike, UUID> {

    Boolean existsByComment_IdAndUser_Id(UUID commentId, UUID userId);

    Optional<CommentLike> findByComment_IdAndUser_Id(UUID commentId, UUID userId);
    Long countAllByComment(Comment comment);

    void deleteAllByComment_Article_Id(UUID id);

    UUID comment(Comment comment);
}

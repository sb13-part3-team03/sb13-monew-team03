package com.codeit.monew.comment.repository;

import com.codeit.monew.comment.entity.Comment;
import com.codeit.monew.comment.repository.querydsl.CommentRepositoryDsl;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface CommentRepository extends JpaRepository<Comment, UUID>, CommentRepositoryDsl {
    long countByDeletedAtIsNull();

    Long countAllByDeletedAtIsNullAndArticleId(UUID id);

    long countByArticleIdAndDeletedAtIsNull(UUID articleId);

    @EntityGraph(attributePaths = {"user","article"})
    Optional<Comment> findByIdAndDeletedAtIsNull(UUID id);

    long countByArticleId(UUID articleId);

    void deleteAllByArticle_Id(UUID id);
}

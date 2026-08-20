package com.codeit.monew.comment.repository;

import com.codeit.monew.comment.entity.Comment;
import com.codeit.monew.comment.repository.querydsl.CommentRepositoryDsl;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;
import java.util.UUID;

public interface CommentRepository extends JpaRepository<Comment, UUID>, CommentRepositoryDsl {
    long countByDeletedAtIsNull();
    Long countAllByDeletedAtIsNullAndArticleId(UUID id);
    Optional<Comment> findByIdAndDeletedAtIsNull(UUID id);

    void deleteAllByArticle_Id(UUID id);

    long countByArticleId(UUID articleId);
}

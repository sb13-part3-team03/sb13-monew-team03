package com.codeit.monew.useractivity.service;

import com.codeit.monew.useractivity.entity.UserActivity;
import com.codeit.monew.useractivity.event.UserActivityEvent;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.bson.Document;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

/** Field-level updates avoid overwriting unrelated activity from concurrent requests. */
@Service
@RequiredArgsConstructor
public class UserActivityProjectionService {
  private final MongoTemplate mongo;

  public void apply(UserActivityEvent event) {
    if (event instanceof UserActivityEvent.Profile e) {
      ensureProfile(e);
      mongo.updateFirst(user(e.id()), touched().set("email", e.email())
          .set("nickname", e.nickname()), UserActivity.class);
    } else if (event instanceof UserActivityEvent.CommentAdded e) {
      ensureProfile(e.user());
      addRecent(e.user().id(), "comments", "id", e.comment().id(), e.comment());
    } else if (event instanceof UserActivityEvent.LikeAdded e) {
      ensureProfile(e.user());
      addRecent(e.user().id(), "commentLikes", "id", e.like().id(), e.like());
    } else if (event instanceof UserActivityEvent.ArticleViewed e) {
      ensureProfile(e.user());
      // Repeated reads retain the original view timestamp and do not create duplicates.
      addRecent(e.user().id(), "articleViews", "articleId", e.view().articleId(), e.view());
    } else if (event instanceof UserActivityEvent.CommentUpdated e) {
      updateCommentField(e.commentId(), "content", "commentContent", e.content());
    } else if (event instanceof UserActivityEvent.LikeCountChanged e) {
      updateCommentField(e.commentId(), "likeCount", "commentLikeCount", e.count());
    } else if (event instanceof UserActivityEvent.CommentRemoved e) {
      mongo.updateMulti(Query.query(new Criteria().orOperator(
              Criteria.where("comments.id").is(e.commentId()),
              Criteria.where("commentLikes.commentId").is(e.commentId()))),
          touched().pull("comments", new Document("id", e.commentId()))
              .pull("commentLikes", new Document("commentId", e.commentId())), UserActivity.class);
    } else if (event instanceof UserActivityEvent.LikeRemoved e) {
      mongo.updateFirst(user(e.userId()), touched()
          .pull("commentLikes", new Document("id", e.likeId())), UserActivity.class);
    } else if (event instanceof UserActivityEvent.UserRemoved e) {
      mongo.remove(user(e.userId()), UserActivity.class);
    }
  }

  private void ensureProfile(UserActivityEvent.Profile profile) {
    Update insert = new Update().setOnInsert("email", profile.email())
        .setOnInsert("nickname", profile.nickname()).setOnInsert("createdAt", profile.createdAt())
        .setOnInsert("comments", List.of())
        .setOnInsert("commentLikes", List.of()).setOnInsert("articleViews", List.of());
    try {
      mongo.upsert(user(profile.id()), insert, UserActivity.class);
    } catch (DuplicateKeyException e) {
      // Another request may initialize this user's document concurrently.
      if (!mongo.exists(user(profile.id()), UserActivity.class)) {
        throw e;
      }
    }
  }

  private void addRecent(UUID userId, String field, String key, UUID id, Object value) {
    Query query = user(userId).addCriteria(Criteria.where(field + "." + key).ne(id));
    Update update = touched();
    // 이전 기록도 보관하고 최신순으로 정렬한다. 조회할 때만 최근 10개로 제한한다.
    update.push(field).sort(Sort.by(Sort.Order.desc("createdAt"), Sort.Order.desc("id")))
        .each(value);
    mongo.updateFirst(query, update, UserActivity.class);
  }

  private void updateCommentField(UUID id, String commentField, String likeField, Object value) {
    mongo.updateMulti(Query.query(Criteria.where("comments.id").is(id)),
        touched().set("comments.$." + commentField, value), UserActivity.class);
    mongo.updateMulti(Query.query(Criteria.where("commentLikes.commentId").is(id)),
        touched().set("commentLikes.$." + likeField, value), UserActivity.class);
  }

  private Query user(UUID id) {
    return Query.query(Criteria.where("id").is(id));
  }

  private Update touched() {
    return new Update().set("updatedAt", Instant.now());
  }
}

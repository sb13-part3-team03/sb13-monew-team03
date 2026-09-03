package com.codeit.monew.article.service;

import com.codeit.monew.article.dto.response.ArticleRestoreResultDto;
import com.codeit.monew.article.entity.Article;
import com.codeit.monew.article.exception.ArticleRestoreException;
import com.codeit.monew.article.exception.S3StorageException;
import com.codeit.monew.article.repository.ArticleRepository;
import com.codeit.monew.global.exception.ErrorCode;
import com.fasterxml.jackson.core.type.TypeReference;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ArticleRestoreService {

    private final ArticleRepository articleRepository;
    private final S3StorageService s3StorageService;

    // 날짜 범위 복구
    @Transactional
    public List<ArticleRestoreResultDto> restore(LocalDate from, LocalDate to) {

        log.info("날짜 범위 복구 시작. from={}, to={}", from, to);

        List<LocalDate> dates = from
                .datesUntil(to.plusDays(1))
                .toList();

        log.info("복구 대상 날짜: {}", dates);

        return dates.stream()
                .map(this::restore)
                .toList();
    }

    // 하루치 복구
    private ArticleRestoreResultDto restore(LocalDate date) {
        String key = "article-backup/"
                + date
                + "/articles.json";

        log.info("S3 백업 다운로드 시작. key={}", key);

        try {
            // S3에서 해당 날짜 백업 가져오기
            List<Article> backupArticles =
                    s3StorageService.download(
                            key,
                            new TypeReference<List<Article>>() {
                            }
                    );

            log.info("S3 백업 다운로드 완료. date={}, count={}",
                    date, backupArticles.size());

            List<UUID> articleIds = backupArticles.stream()
                    .map(Article::getId)
                    .toList();

            List<String> sourceUrls = backupArticles.stream()
                    .map(Article::getSourceUrl)
                    .toList();

            // 현재 DB에 존재하는 기사 확인
            List<Article> existingArticles = articleRepository.findAllById(articleIds);

            Set<UUID> existingArticleIds = existingArticles.stream()
                    .map(Article::getId)
                    .collect(Collectors.toSet());

            List<Article> existingSourceUrlArticles =
                    articleRepository.findAllBySourceUrlIn(sourceUrls);

            Set<String> existingSourceUrls = existingSourceUrlArticles.stream()
                    .map(Article::getSourceUrl)
                    .collect(Collectors.toSet());

            // ID와 sourceUrl 모두 DB에 존재하지 않는 기사만 복구
            List<Article> lostArticles = backupArticles.stream()
                    .filter(article ->
                            !existingArticleIds.contains(article.getId())
                                    && !existingSourceUrls.contains(article.getSourceUrl())
                    )
                    .toList();

            List<UUID> restoredArticleIds = lostArticles.stream()
                    .map(Article::getId)
                    .toList();

            // 복구용 INSERT
            for (Article article : lostArticles) {

                articleRepository.insertForRestore(
                        article.getId(),
                        article.getSource().name(),
                        article.getSourceUrl(),
                        article.getTitle(),
                        article.getSummary(),
                        article.getPublishDate(),
                        article.getDeletedAt()
                );
            }

            return new ArticleRestoreResultDto(
                    Instant.now(),
                    restoredArticleIds,
                    (long) restoredArticleIds.size()
            );

        } catch (S3StorageException e) {
            throw e;
        } catch (Exception e) {
            log.error("기사 복구 중 오류 발생. date={}", date, e);

            throw new ArticleRestoreException(
                    ErrorCode.ARTICLE_RESTORE_FAILED
            );
        }
    }

}

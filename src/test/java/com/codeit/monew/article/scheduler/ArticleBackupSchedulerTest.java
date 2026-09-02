package com.codeit.monew.article.scheduler;

import com.codeit.monew.article.exception.S3StorageException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.launch.JobLauncher;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ArticleBackupSchedulerTest {

    @Mock
    private JobLauncher jobLauncher;

    @Mock
    private Job backupJob;

    @InjectMocks
    private ArticleBackupScheduler articleBackupScheduler;

    @Test
    @DisplayName("뉴스 기사 백업 Job을 실행한다")
    void backupArticles_success() throws Exception {
        // when
        articleBackupScheduler.backupArticles();

        // then
        verify(jobLauncher)
                .run(
                        eq(backupJob),
                        any(JobParameters.class)
                );
    }

    @Test
    @DisplayName("백업 Job 실행 중 예외가 발생하면 S3StorageException을 던진다")
    void backupArticles_fail() throws Exception {
        // given
        when(jobLauncher.run(
                any(Job.class),
                any(JobParameters.class)
        )).thenThrow(new RuntimeException("Job 실행 실패"));

        // when & then
        assertThatThrownBy(() ->
                articleBackupScheduler.backupArticles()
        )
                .isInstanceOf(S3StorageException.class);

        verify(jobLauncher)
                .run(
                        eq(backupJob),
                        any(JobParameters.class)
                );
    }

}

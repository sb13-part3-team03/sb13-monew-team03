package com.codeit.monew.article.scheduler;

import com.codeit.monew.article.exception.S3StorageException;
import com.codeit.monew.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class ArticleBackupScheduler {

    private final JobLauncher jobLauncher;
    private final Job backupJob;

    // 매일 새벽 1시에 백업 Job 실행
    @Scheduled(cron = "0 * * * * *")
    public void backupArticles() {
        log.info("뉴스 기사 백업 배치 시작");

        try {
            JobParameters jobParameters = new JobParametersBuilder()
                    .addLong(
                            "timestamp",
                            System.currentTimeMillis()
                    )
                    .toJobParameters();

            jobLauncher.run(backupJob, jobParameters);

            log.info("뉴스 기사 백업 배치 완료");

        } catch (Exception e) {
            log.error("뉴스 기사 백업 배치 실패", e);

            throw new S3StorageException(
                    ErrorCode.BACKUP_JOB_FAILED,
                    e
            );
        }
    }
}

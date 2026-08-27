package com.codeit.monew.article.config;

import com.codeit.monew.article.service.ArticleBackupService;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
public class BackupStepConfig {

    @Bean
    public Step backupStep(
            JobRepository jobRepository,
            PlatformTransactionManager transactionManager,
            ArticleBackupService backupService
    ) {
        return new StepBuilder("backupStep", jobRepository)
                .tasklet((contribution, chunkContext) -> {
                    backupService.backup();
                    return RepeatStatus.FINISHED;
                }, transactionManager)
                .build();
    }
}

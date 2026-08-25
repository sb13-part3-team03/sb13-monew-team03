package com.codeit.monew.article.config;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class BackupJobConfig {

    @Bean
    public Job backupJob(
            JobRepository jobRepository,
            Step backupStep
    ) {
        return new JobBuilder("backupJob", jobRepository)
                .start(backupStep)
                .build();
    }
}

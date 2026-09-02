package com.codeit.monew.article.service;

import com.codeit.monew.article.exception.S3StorageException;
import com.codeit.monew.global.exception.ErrorCode;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.core.exception.SdkClientException;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class S3StorageService {

    private final S3Client s3Client;
    private final ObjectMapper objectMapper;

    @Value("${cloud.aws.s3.bucket}")
    private String bucket;

    /**
     * S3에 백업 파일 업로드
     */
    public void upload(String key, String content) {
        PutObjectRequest request = PutObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .contentType("application/json")
                .build();

        try {
            s3Client.putObject(
                    request,
                    RequestBody.fromString(content)
            );

            log.info("S3 백업 업로드 완료: {}", key);

        } catch (S3Exception | SdkClientException e) {
            throw new S3StorageException(ErrorCode.S3_BACKUP_UPLOAD_FAILED);
        }
    }

    /**
     * S3에서 백업 파일 다운로드
     */
    public <T> T download(
            String key,
            TypeReference<T> typeReference
    ) {
        try {
            GetObjectRequest request = GetObjectRequest.builder()
                    .bucket(bucket)
                    .key(key)
                    .build();

            ResponseBytes<GetObjectResponse> response =
                    s3Client.getObjectAsBytes(request);

            return objectMapper.readValue(
                    response.asUtf8String(),
                    typeReference
            );

        } catch (S3Exception | SdkClientException | JsonProcessingException e) {
            log.error(
                    "S3 백업 업로드 실패. bucket={}, key={}",
                    bucket,
                    key,
                    e
            );

            throw new S3StorageException(ErrorCode.S3_BACKUP_UPLOAD_FAILED);
        }
    }

    // S3에 지정된 key의 백업 파일이 존재하는지 확인
    public boolean exists(String key) {
        try {
            s3Client.headObject(
                    HeadObjectRequest.builder()
                            .bucket(bucket)
                            .key(key)
                            .build()
            );

            return true;

        } catch (NoSuchKeyException e) {
            // 백업 파일이 존재하지 않는 경우
            return false;

        } catch (S3Exception e) {
            // 404 응답은 파일이 존재하지 않는 것으로 처리
            if (e.statusCode() == 404) { return false; }

            // 그 외 S3 오류는 백업 파일 확인 실패로 처리
            throw new S3StorageException(ErrorCode.S3_BACKUP_CHECK_FAILED);

        } catch (SdkClientException e) {
        throw new S3StorageException(ErrorCode.S3_BACKUP_FAILED);

        }
    }

}
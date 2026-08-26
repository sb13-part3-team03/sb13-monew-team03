package com.codeit.monew.article.service;

import com.codeit.monew.article.exception.S3StorageException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.util.ReflectionTestUtils;
import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.HeadObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;

import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
public class S3StorageServiceTest {

    @Mock
    private S3Client s3Client;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private S3StorageService s3StorageService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(
                s3StorageService,
                "bucket",
                "test-bucket"
        );
    }

    @Test
    @DisplayName("S3에 백업 파일을 업로드한다")
    void upload() {
        // given
        String key = "backup/2026-08-25.json";
        String content = "{\"id\":\"test\"}";

        // when
        s3StorageService.upload(key, content);

        // then
        ArgumentCaptor<PutObjectRequest> captor =
                ArgumentCaptor.forClass(PutObjectRequest.class);

        verify(s3Client).putObject(
                captor.capture(),
                any(RequestBody.class)
        );

        PutObjectRequest request = captor.getValue();

        assertThat(request.bucket()).isEqualTo("test-bucket");
        assertThat(request.key()).isEqualTo(key);
        assertThat(request.contentType()).isEqualTo("application/json");
    }

    @Test
    @DisplayName("S3 업로드 실패 시 S3StorageException을 발생시킨다")
    void upload_throwsException() {
        // given
        String key = "backup/2026-08-25.json";
        String content = "{}";

        doThrow(
                S3Exception.builder()
                        .statusCode(500)
                        .message("S3 upload failed")
                        .build()
        )
                .when(s3Client)
                .putObject(
                        (PutObjectRequest) any(PutObjectRequest.class),
                        any(RequestBody.class)
                );

        // when & then
        assertThatThrownBy(() ->
                s3StorageService.upload(key, content)
        )
                .isInstanceOf(S3StorageException.class);
    }

    @Test
    @DisplayName("S3 백업 파일을 다운로드하여 객체로 변환한다")
    void download() throws Exception {
        // given
        String key = "backup/2026-08-25.json";

        String json = """
            [
                {
                    "id": "550e8400-e29b-41d4-a716-446655440000"
                }
            ]
            """;

        List<String> expected = List.of("test");

        ResponseBytes<GetObjectResponse> response =
                ResponseBytes.fromByteArray(
                        GetObjectResponse.builder().build(),
                        json.getBytes(StandardCharsets.UTF_8)
                );

        when(s3Client.getObjectAsBytes(any(GetObjectRequest.class)))
                .thenReturn(response);

        when(objectMapper.readValue(
                eq(json),
                any(TypeReference.class)
        )).thenReturn(expected);

        // when
        List<String> result = s3StorageService.download(
                key,
                new TypeReference<List<String>>() {}
        );

        // then
        assertThat(result).isEqualTo(expected);

        ArgumentCaptor<GetObjectRequest> captor =
                ArgumentCaptor.forClass(GetObjectRequest.class);

        verify(s3Client).getObjectAsBytes(captor.capture());

        GetObjectRequest request = captor.getValue();

        assertThat(request.bucket()).isEqualTo("test-bucket");
        assertThat(request.key()).isEqualTo(key);
    }

    @Test
    @DisplayName("S3 다운로드 실패 시 S3StorageException을 발생시킨다")
    void download_throwsS3Exception() {
        // given
        when(s3Client.getObjectAsBytes(
                (GetObjectRequest) any()
        ))
                .thenThrow(
                        S3Exception.builder()
                                .statusCode(500)
                                .message("S3 download failed")
                                .build()
                );

        // when & then
        assertThatThrownBy(() ->
                s3StorageService.download(
                        "backup/test.json",
                        new TypeReference<List<String>>() {}
                )
        )
                .isInstanceOf(S3StorageException.class);
    }

}

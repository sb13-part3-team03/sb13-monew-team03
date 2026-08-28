package com.codeit.monew.article.entity;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@ExtendWith(SpringExtension.class)
public class ArticleSourceTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    @DisplayName("기존 YONHAP 값을 YEONHAP으로 역직렬화한다")
    void deserializeYonhap() throws Exception {
        ArticleSource result =
                objectMapper.readValue("\"YONHAP\"", ArticleSource.class);

        assertThat(result).isEqualTo(ArticleSource.YEONHAP);
    }

}

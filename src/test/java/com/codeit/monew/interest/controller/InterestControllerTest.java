package com.codeit.monew.interest.controller;

import com.codeit.monew.interest.dto.request.InterestRegisterRequest;
import com.codeit.monew.interest.dto.request.InterestUpdateRequest;
import com.codeit.monew.interest.dto.response.CursorPageResponseInterestDto;
import com.codeit.monew.interest.dto.response.InterestDto;
import com.codeit.monew.interest.dto.response.SubscriptionDto;
import com.codeit.monew.interest.service.InterestService;
import com.codeit.monew.interest.service.command.*;
import com.codeit.monew.interest.service.condition.InterestSearchCondition;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(InterestController.class)
class InterestControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private InterestService interestService;

    @Nested
    @DisplayName("관심사 등록")
    class CreateInterest {

        @Test
        @DisplayName("성공")
        void success() throws Exception {
            UUID interestId = UUID.randomUUID();

            InterestRegisterRequest request = new InterestRegisterRequest(
                    "스포츠",
                    List.of("축구", "야구")
            );

            InterestDto response = new InterestDto(
                    interestId,
                    "스포츠",
                    List.of("축구", "야구"),
                    0L,
                    null
            );

            given(interestService.createInterest(any(InterestRegisterCommand.class)))
                    .willReturn(response);

            mockMvc.perform(post("/api/interests")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").value(interestId.toString()))
                    .andExpect(jsonPath("$.name").value("스포츠"))
                    .andExpect(jsonPath("$.keywords[0]").value("축구"))
                    .andExpect(jsonPath("$.keywords[1]").value("야구"))
                    .andExpect(jsonPath("$.subscriberCount").value(0));

            then(interestService)
                    .should()
                    .createInterest(any(InterestRegisterCommand.class));
        }

        @Test
        @DisplayName("이름이 비어 있으면 실패")
        void fail_whenNameIsBlank() throws Exception {
            InterestRegisterRequest request = new InterestRegisterRequest(
                    "",
                    List.of("축구")
            );

            mockMvc.perform(post("/api/interests")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());

            then(interestService).shouldHaveNoInteractions();
        }

        @Test
        @DisplayName("키워드가 비어 있으면 실패")
        void fail_whenKeywordsAreEmpty() throws Exception {
            InterestRegisterRequest request = new InterestRegisterRequest(
                    "스포츠",
                    List.of()
            );

            mockMvc.perform(post("/api/interests")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());

            then(interestService).shouldHaveNoInteractions();
        }

        @Test
        @DisplayName("키워드가 null이면 실패")
        void fail_whenKeywordsAreNull() throws Exception {
            InterestRegisterRequest request = new InterestRegisterRequest(
                    "스포츠",
                    null
            );

            mockMvc.perform(post("/api/interests")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());

            then(interestService).shouldHaveNoInteractions();
        }
    }

    @Nested
    @DisplayName("관심사 구독")
    class Subscribe {

        @Test
        @DisplayName("성공")
        void success() throws Exception {
            UUID interestId = UUID.randomUUID();
            UUID userId = UUID.randomUUID();
            UUID subscriptionId = UUID.randomUUID();

            SubscriptionDto response = new SubscriptionDto(
                    subscriptionId,
                    interestId,
                    "스포츠",
                    List.of("축구", "야구"),
                    1L,
                    Instant.now()
            );

            given(interestService.subscribe(any(InterestSubscribeCommand.class)))
                    .willReturn(response);

            mockMvc.perform(post("/api/interests/{interestId}/subscriptions", interestId)
                            .header("Monew-Request-User-ID", userId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(subscriptionId.toString()))
                    .andExpect(jsonPath("$.interestId").value(interestId.toString()))
                    .andExpect(jsonPath("$.interestName").value("스포츠"))
                    .andExpect(jsonPath("$.interestSubscriberCount").value(1));

            then(interestService)
                    .should()
                    .subscribe(any(InterestSubscribeCommand.class));
        }

        @Test
        @DisplayName("요청자 헤더가 없으면 실패")
        void fail_whenUserHeaderIsMissing() throws Exception {
            UUID interestId = UUID.randomUUID();

            mockMvc.perform(post("/api/interests/{interestId}/subscriptions", interestId))
                    .andExpect(status().isBadRequest());

            then(interestService).shouldHaveNoInteractions();
        }
    }

    @Nested
    @DisplayName("관심사 목록 조회")
    class FindInterests {

        @Test
        @DisplayName("성공")
        void success() throws Exception {
            // given
            UUID userId = UUID.randomUUID();
            UUID interestId1 = UUID.randomUUID();
            UUID interestId2 = UUID.randomUUID();

            CursorPageResponseInterestDto response =
                    new CursorPageResponseInterestDto(
                            List.of(
                                    new InterestDto(
                                            interestId1,
                                            "스포츠",
                                            List.of("축구", "야구"),
                                            10L,
                                            true
                                    ),
                                    new InterestDto(
                                            interestId2,
                                            "스포츠 뉴스",
                                            List.of("농구", "배구"),
                                            5L,
                                            false
                                    )
                            ),
                            "스포츠 뉴스",
                            Instant.parse("2026-08-20T10:00:00Z"),
                            2,
                            10L,
                            true
                    );

            given(interestService.findInterests(
                    any(InterestSearchCondition.class)
            )).willReturn(response);

            // when & then
            mockMvc.perform(get("/api/interests")
                            .header(
                                    "Monew-Request-User-ID",
                                    userId.toString()
                            )
                            .param("keyword", "스포츠")
                            .param("orderBy", "name")
                            .param("direction", "ASC")
                            .param("limit", "2"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content").isArray())
                    .andExpect(jsonPath("$.content.length()").value(2))
                    .andExpect(jsonPath("$.content[0].id")
                            .value(interestId1.toString()))
                    .andExpect(jsonPath("$.content[0].name")
                            .value("스포츠"))
                    .andExpect(jsonPath("$.content[0].subscriberCount")
                            .value(10))
                    .andExpect(jsonPath("$.content[0].subscribedByMe")
                            .value(true))
                    .andExpect(jsonPath("$.content[1].name")
                            .value("스포츠 뉴스"))
                    .andExpect(jsonPath("$.nextCursor")
                            .value("스포츠 뉴스"))
                    .andExpect(jsonPath("$.nextAfter")
                            .value("2026-08-20T10:00:00Z"))
                    .andExpect(jsonPath("$.size").value(2))
                    .andExpect(jsonPath("$.totalElements").value(10))
                    .andExpect(jsonPath("$.hasNext").value(true));

            then(interestService)
                    .should()
                    .findInterests(any(InterestSearchCondition.class));
        }

        @Test
        @DisplayName("구독자 수 정렬에서 커서가 숫자가 아니면 실패")
        void fail_whenSubscriberCountCursorIsNotNumber() throws Exception {
            // given
            UUID userId = UUID.randomUUID();

            // when & then
            mockMvc.perform(get("/api/interests")
                            .header(
                                    "Monew-Request-User-ID",
                                    userId.toString()
                            )
                            .param("orderBy", "subscriberCount")
                            .param("direction", "ASC")
                            .param("cursor", "abc")
                            .param("limit", "10"))
                    .andExpect(status().isBadRequest());

            then(interestService)
                    .shouldHaveNoInteractions();
        }

        @Test
        @DisplayName("구독자 수 정렬에서 커서가 음수이면 실패")
        void fail_whenSubscriberCountCursorIsNegative() throws Exception {
            UUID userId = UUID.randomUUID();

            mockMvc.perform(get("/api/interests")
                            .header(
                                    "Monew-Request-User-ID",
                                    userId.toString()
                            )
                            .param("orderBy", "subscriberCount")
                            .param("direction", "ASC")
                            .param("cursor", "-1")
                            .param("limit", "10"))
                    .andExpect(status().isBadRequest());

            then(interestService)
                    .shouldHaveNoInteractions();
        }

        @Test
        @DisplayName("요청자 ID 헤더가 없으면 실패")
        void fail_whenUserIdHeaderIsMissing() throws Exception {
            // when & then
            mockMvc.perform(get("/api/interests")
                            .param("orderBy", "name")
                            .param("direction", "ASC")
                            .param("limit", "10"))
                    .andExpect(status().isBadRequest());

            then(interestService)
                    .shouldHaveNoInteractions();
        }
    }

    @Nested
    @DisplayName("관심사 수정")
    class UpdateInterest {

        @Test
        @DisplayName("성공")
        void success() throws Exception {
            UUID interestId = UUID.randomUUID();

            InterestUpdateRequest request = new InterestUpdateRequest(
                    List.of("축구", "농구")
            );

            InterestDto response = new InterestDto(
                    interestId,
                    "스포츠",
                    List.of("축구", "농구"),
                    3L,
                    null
            );

            given(interestService.updateInterest(any(InterestUpdateCommand.class)))
                    .willReturn(response);

            mockMvc.perform(patch("/api/interests/{interestId}", interestId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(interestId.toString()))
                    .andExpect(jsonPath("$.name").value("스포츠"))
                    .andExpect(jsonPath("$.keywords[0]").value("축구"))
                    .andExpect(jsonPath("$.keywords[1]").value("농구"))
                    .andExpect(jsonPath("$.subscriberCount").value(3));

            then(interestService)
                    .should()
                    .updateInterest(any(InterestUpdateCommand.class));
        }

        @Test
        @DisplayName("키워드가 비어 있으면 실패")
        void fail_whenKeywordsAreEmpty() throws Exception {
            UUID interestId = UUID.randomUUID();

            InterestUpdateRequest request = new InterestUpdateRequest(
                    List.of()
            );

            mockMvc.perform(patch("/api/interests/{interestId}", interestId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());

            then(interestService).shouldHaveNoInteractions();
        }
    }

    @Nested
    @DisplayName("관심사 삭제")
    class DeleteInterest {

        @Test
        @DisplayName("성공")
        void success() throws Exception {
            UUID interestId = UUID.randomUUID();

            mockMvc.perform(delete("/api/interests/{interestId}", interestId))
                    .andExpect(status().isNoContent());

            then(interestService)
                    .should()
                    .deleteInterest(any(InterestDeleteCommand.class));
        }
    }



    @Nested
    @DisplayName("관심사 구독 취소")
    class Unsubscribe {

        @Test
        @DisplayName("성공")
        void success() throws Exception {
            UUID interestId = UUID.randomUUID();
            UUID userId = UUID.randomUUID();

            mockMvc.perform(delete("/api/interests/{interestId}/subscriptions", interestId)
                            .header("Monew-Request-User-ID", userId))
                    .andExpect(status().isOk());

            then(interestService)
                    .should()
                    .unsubscribe(any(InterestUnsubscribeCommand.class));
        }

        @Test
        @DisplayName("요청자 헤더가 없으면 실패")
        void fail_whenUserHeaderIsMissing() throws Exception {
            UUID interestId = UUID.randomUUID();

            mockMvc.perform(delete("/api/interests/{interestId}/subscriptions", interestId))
                    .andExpect(status().isBadRequest());

            then(interestService).shouldHaveNoInteractions();
        }
    }
}
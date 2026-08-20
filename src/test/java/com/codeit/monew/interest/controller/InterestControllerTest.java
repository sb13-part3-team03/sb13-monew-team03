package com.codeit.monew.interest.controller;

import com.codeit.monew.interest.dto.request.InterestRegisterRequest;
import com.codeit.monew.interest.dto.request.InterestUpdateRequest;
import com.codeit.monew.interest.dto.response.InterestDto;
import com.codeit.monew.interest.dto.response.SubscriptionDto;
import com.codeit.monew.interest.service.InterestService;
import com.codeit.monew.interest.service.command.*;
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

//        @Test
//        @DisplayName("요청자 헤더가 없으면 실패")
//        void fail_whenUserHeaderIsMissing() throws Exception {
//            UUID interestId = UUID.randomUUID();
//
//            mockMvc.perform(post("/api/interests/{interestId}/subscriptions", interestId))
//                    .andExpect(status().isBadRequest());
//
//            then(interestService).shouldHaveNoInteractions();
//        }
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

//        @Test
//        @DisplayName("요청자 헤더가 없으면 실패")
//        void fail_whenUserHeaderIsMissing() throws Exception {
//            UUID interestId = UUID.randomUUID();
//
//            mockMvc.perform(delete("/api/interests/{interestId}/subscriptions", interestId))
//                    .andExpect(status().isBadRequest());
//
//            then(interestService).shouldHaveNoInteractions();
//        }
    }
}
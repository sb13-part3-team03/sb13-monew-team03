package com.codeit.monew.interest.service;

import com.codeit.monew.interest.dto.response.CursorPageResponseInterestDto;
import com.codeit.monew.interest.dto.response.InterestDto;
import com.codeit.monew.interest.dto.response.SubscriptionDto;
import com.codeit.monew.interest.service.command.*;
import com.codeit.monew.interest.service.condition.InterestSearchCondition;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class InterestService {

    public InterestDto createInterest(InterestRegisterCommand command) {
        return null;
    }

    public SubscriptionDto subscribe(InterestSubscribeCommand command) {
        return null;
    }

    public CursorPageResponseInterestDto findInterests(InterestSearchCondition condition) {
        return null;
    }

    public InterestDto updateInterest(InterestUpdateCommand command) {
        return null;
    }

    public void deleteInterest(InterestDeleteCommand command) {

    }

    public void unsubscribe(InterestUnsubscribeCommand command) {

    }
}

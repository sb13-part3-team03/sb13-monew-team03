package com.codeit.monew.interest.repository;

import com.codeit.monew.interest.repository.projection.InterestSearchResult;
import com.codeit.monew.interest.service.condition.InterestSearchCondition;

import java.util.List;

public interface InterestRepositoryCustom {

    List<InterestSearchResult> search(InterestSearchCondition condition, int limit);
    long countByCondition(InterestSearchCondition condition);

}

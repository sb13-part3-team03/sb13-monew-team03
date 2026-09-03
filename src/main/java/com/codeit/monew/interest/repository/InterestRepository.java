package com.codeit.monew.interest.repository;

import com.codeit.monew.interest.entity.Interest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface InterestRepository extends JpaRepository<Interest, UUID>, InterestRepositoryCustom {

    @Query("""
            SELECT DISTINCT i
            FROM Interest i
            LEFT JOIN FETCH i.keywords
            """)
    List<Interest> findAllWithKeywords();

}

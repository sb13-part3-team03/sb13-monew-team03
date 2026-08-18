package com.codeit.monew.interest.repository;

import com.codeit.monew.interest.entity.Interest;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface InterestRepository extends JpaRepository<Interest, UUID> {

}

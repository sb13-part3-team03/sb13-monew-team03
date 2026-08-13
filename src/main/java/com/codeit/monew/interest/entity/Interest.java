package com.codeit.monew.interest.entity;

import com.codeit.monew.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "interests")
public class Interest extends BaseEntity {

    @Column(nullable = false, length = 50)
    private String name;

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(
            name = "interest_keywords",
            joinColumns = @JoinColumn(name = "interest_id")
    )
    @Column(name = "keyword", nullable = false)
    private List<String> keywords = new ArrayList<>();

    public Interest(String name, List<String> keywords) {
        this.name = name;
        this.keywords.addAll(keywords);
    }

    public void updateKeywords(List<String> keywords) {
        this.keywords.clear();
        this.keywords.addAll(keywords);
    }

}

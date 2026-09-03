package com.codeit.monew.user.entity;

import com.codeit.monew.global.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Entity
@Table(name = "users")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User extends BaseEntity {

    @Column(nullable = false, unique = true, length = 255)
    private String email;

    @Column(nullable = false, length = 20)
    private String nickname;

    @Column(nullable = false, length = 255)
    private String password;

    @Column(name = "deleted_at")
    private Instant deletedAt;

    public User(
            String email,
            String nickname,
            String password
    ) {
        this.email = email;
        this.nickname = nickname;
        this.password = password;
    }

    public void updateNickname(String nickname) {
        this.nickname = nickname;
    }

    public void delete() {
        this.deletedAt = Instant.now();
    }
}
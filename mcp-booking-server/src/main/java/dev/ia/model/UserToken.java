package dev.ia.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "user_tokens")
public class UserToken {

    @Id
    @Column(name = "user_id")
    public String userId;

    @Column(name = "access_token")
    public String accessToken;

    @Column(name = "refresh_token")
    public String refreshToken;

    @Column(name = "expires_at")
    public Instant expiresAt;

    public UserToken() {}

    public UserToken(String userId) {
        this.userId = userId;
    }
}

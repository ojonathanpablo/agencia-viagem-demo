package dev.ia.repository;

import dev.ia.model.UserToken;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class UserTokenRepository implements PanacheRepositoryBase<UserToken, String> {}

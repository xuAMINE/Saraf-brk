package com.saraf.security.user;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface VerTokenRepository extends JpaRepository<VerificationToken, Integer> {

    Optional<VerificationToken> findByToken(String token);

    @Query("SELECT t FROM VerificationToken t WHERE t.user.id = :userId AND t.validatedAt IS NULL AND t.expires > CURRENT_TIMESTAMP")
    List<VerificationToken> findAllActiveTokensByUser(@Param("userId") Integer userId);

}

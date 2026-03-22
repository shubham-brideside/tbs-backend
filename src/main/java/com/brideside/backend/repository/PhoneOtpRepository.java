package com.brideside.backend.repository;

import com.brideside.backend.entity.PhoneOtpChallenge;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Optional;

public interface PhoneOtpRepository extends JpaRepository<PhoneOtpChallenge, Long> {

    Optional<PhoneOtpChallenge> findTopByPhoneDigitsOrderByCreatedAtDesc(String phoneDigits);

    Optional<PhoneOtpChallenge> findTopByPhoneDigitsAndConsumedFalseAndExpiresAtAfterOrderByCreatedAtDesc(
            String phoneDigits, LocalDateTime now);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE PhoneOtpChallenge c SET c.consumed = true WHERE c.phoneDigits = :phone AND c.consumed = false")
    int markConsumedPendingForPhone(@Param("phone") String phoneDigits);
}

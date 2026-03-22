package com.brideside.backend.service;

import com.brideside.backend.entity.PhoneOtpChallenge;
import com.brideside.backend.repository.PhoneOtpRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class PhoneOtpPersistenceHelper {

    private final PhoneOtpRepository phoneOtpRepository;

    public PhoneOtpPersistenceHelper(PhoneOtpRepository phoneOtpRepository) {
        this.phoneOtpRepository = phoneOtpRepository;
    }

    @Transactional
    public PhoneOtpChallenge createNewChallenge(PhoneOtpChallenge challenge) {
        phoneOtpRepository.markConsumedPendingForPhone(challenge.getPhoneDigits());
        return phoneOtpRepository.save(challenge);
    }

    @Transactional
    public void deleteChallenge(Long id) {
        phoneOtpRepository.deleteById(id);
    }

    @Transactional
    public void saveChallenge(PhoneOtpChallenge challenge) {
        phoneOtpRepository.save(challenge);
    }
}

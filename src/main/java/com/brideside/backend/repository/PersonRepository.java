package com.brideside.backend.repository;

import com.brideside.backend.entity.Person;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PersonRepository extends JpaRepository<Person, Long> {
    
    /**
     * Find person by phone number
     * @param phone the phone number
     * @return optional person
     */
    Optional<Person> findByPhone(String phone);
    
    /**
     * Find person by phone number and not deleted
     * @param phone the phone number
     * @param isDeleted deletion status
     * @return optional person
     */
    Optional<Person> findByPhoneAndIsDeleted(String phone, Boolean isDeleted);
    
    /**
     * Find all persons by phone number
     * @param phone the phone number
     * @return list of persons
     */
    List<Person> findAllByPhone(String phone);
}


package com.brideside.backend.repository;

import com.brideside.backend.entity.Contact;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ContactRepository extends JpaRepository<Contact, Integer> {
    
    /**
     * Find contact by contact name
     * @param contactName the contact name to search for
     * @return optional contact
     */
    Optional<Contact> findByContactName(String contactName);
    
    /**
     * Find contact by Pipedrive contact ID
     * @param pipedriveContactId the Pipedrive contact ID to search for
     * @return optional contact
     */
    Optional<Contact> findByPipedriveContactId(String pipedriveContactId);
    
    /**
     * Check if contact exists by contact name
     * @param contactName the contact name to check
     * @return true if exists, false otherwise
     */
    boolean existsByContactName(String contactName);
    
    /**
     * Check if contact exists by Pipedrive contact ID
     * @param pipedriveContactId the Pipedrive contact ID to check
     * @return true if exists, false otherwise
     */
    boolean existsByPipedriveContactId(String pipedriveContactId);
}

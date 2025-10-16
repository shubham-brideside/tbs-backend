package com.brideside.backend.repository;

import com.brideside.backend.entity.Deal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DealRepository extends JpaRepository<Deal, Integer> {
    
    /**
     * Find all deals by user name
     * @param userName the user name to search for
     * @return list of deals for the given user
     */
    List<Deal> findByUserName(String userName);
    
    /**
     * Find all deals by contact number
     * @param contactNumber the contact number to search for
     * @return list of deals for the given contact number
     */
    List<Deal> findByContactNumber(String contactNumber);
    
    /**
     * Find all deals by category
     * @param category the category to search for
     * @return list of deals for the given category
     */
    List<Deal> findByCategory(String category);
    
    /**
     * Find all deals by user name and contact number
     * @param userName the user name to search for
     * @param contactNumber the contact number to search for
     * @return list of deals for the given user and contact number
     */
    List<Deal> findByUserNameAndContactNumber(String userName, String contactNumber);
}

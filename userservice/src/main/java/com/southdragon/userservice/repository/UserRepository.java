package com.southdragon.userservice.repository;

import com.southdragon.userservice.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository  extends JpaRepository<User,String> {
    boolean existsByEmail(String email);

    Boolean existsByKeycloakId(String keycloakId);

    User findByEmail(String email);
}

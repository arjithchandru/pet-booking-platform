package com.petcare.booking.repository;

import com.petcare.booking.domain.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {

    @Query("SELECT u FROM User u JOIN FETCH u.tenant WHERE u.oktaSubject = :oktaSubject")
    Optional<User> findByOktaSubject(@Param("oktaSubject") String oktaSubject);
}
package com.tigo.shop.user.domain;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmailIgnoreCase(String email);

    boolean existsByEmailIgnoreCase(String email);

    /** Consulta liviana (una columna) que se ejecuta en cada petición autenticada. */
    @Query("select u.tokenVersion from User u where u.id = :id")
    Optional<Long> findTokenVersionById(Long id);
}

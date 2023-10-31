package com.angelo.demo.repository;

import com.angelo.demo.entity.User;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

public interface UserRepository extends CrudRepository<User, Long> {
    @Query("SELECT u FROM User u WHERE u.id = :userId")
    User findUserByUserId(@Param("userId") String userId);

    boolean existsByEmail(String email);

    boolean existsByUsername(String username);
}

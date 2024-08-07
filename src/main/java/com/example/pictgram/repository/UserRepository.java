package com.example.pictgram.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.pictgram.entity.User;

public interface UserRepository extends JpaRepository<User, Long> {
	
	User findByUsername(String username);

}

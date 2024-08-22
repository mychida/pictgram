package com.example.pictgram.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.pictgram.entity.Comment;

public interface CommentRepository extends JpaRepository<Comment, Long> {

}

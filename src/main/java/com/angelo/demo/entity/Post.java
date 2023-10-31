package com.angelo.demo.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Entity
@Data
@Table(name = "posts")
public class Post implements Serializable {

    @Serial
    private static final long serialVersionUID = -4008243236108827324L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String title;
    private String body;
    private Long userId;
}


package com.angelo.demo.user.entity;

import com.angelo.demo.common.model.Address;
import com.angelo.demo.common.model.Company;
import jakarta.persistence.*;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import java.io.Serial;
import java.io.Serializable;

@Entity
@Data
@Table(name = "users")
public class User implements Serializable {

    @Serial
    private static final long serialVersionUID = 8569863186213551204L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long id;

    @NotBlank(message = "Name is required")
    @Column(name = "full_name")
    private String name;

    @NotBlank(message = "Username is required")
    @Column(unique = true)
    private String username;

    @NotBlank(message = "Email is required")
    @Column(unique = true)
    private String email;

    @Embedded
    private Address address;

    private String phone;

    private String website;

    @Embedded
    private Company company;
}

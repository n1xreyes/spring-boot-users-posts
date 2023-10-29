package com.angelo.demo.entity;

import com.angelo.demo.model.Address;
import com.angelo.demo.model.Company;
import jakarta.persistence.*;
import lombok.Data;
import org.springframework.lang.Nullable;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

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
    @Column(name = "full_name")
    private String name;
    @Column(unique = true)
    private String username;
    @Column(unique = true)
    private String email;
    @Embedded
    private Address address;
    private String phone;
    private String website;
    @Embedded
    private Company company;
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Nullable
    private List<Post> posts;
}

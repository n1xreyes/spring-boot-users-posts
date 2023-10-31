package com.angelo.demo.dto;

import com.angelo.demo.entity.Post;
import com.angelo.demo.model.Address;
import com.angelo.demo.model.Company;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;
import java.util.List;

@Data
public class UserAndPostsDto implements Serializable {

    private static final long serialVersionUID = -869792601381149662L;

    private Long id;
    @NotBlank(message = "fullName is required")
    private String fullName;
    @NotBlank(message = "userName is required")
    private String userName;
    @NotBlank(message = "email is required")
    private String email;
    private Address address;
    private String phone;
    private String website;
    private Company company;
    private List<Post> posts;

}

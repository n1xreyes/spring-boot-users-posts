package com.angelo.demo.model;

import jakarta.persistence.*;
import lombok.Data;

import java.io.Serializable;

@Embeddable
@Data
public class Address implements Serializable {

    private static final long serialVersionUID = 331680390620492298L;

    private String street;
    private String suite;
    private String city;
    private String zipcode;
}

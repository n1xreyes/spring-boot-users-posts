package com.angelo.demo.model;

import jakarta.persistence.*;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Embeddable
@Data
public class Address {

    private String street;
    private String suite;
    private String city;
    private String zipcode;
}

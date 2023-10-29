package com.angelo.demo.model;

import jakarta.persistence.*;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Embeddable
@Data
public class Company {
    private String name;
    private String catchPhrase;
    private String bs;
}

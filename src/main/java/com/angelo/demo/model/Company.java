package com.angelo.demo.model;

import jakarta.persistence.*;
import lombok.Data;

import java.io.Serializable;

@Embeddable
@Data
public class Company implements Serializable {

    private static final long serialVersionUID = 5016425414270816537L;

    @Column(name = "company_name")
    private String name;
    private String catchPhrase;
    private String bs;
}

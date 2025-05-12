package com.example.demo.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@Table(name="people")
public class PersonEntity {
    @Id
    private String email;
    private String name;
    private Integer age;

    public PersonEntity() {}

    // All-args constructor helps with testing
    public PersonEntity(String email, String name, Integer age) {
        this.email = email;
        this.name = name;
        this.age = age;
    }
}

package com.terragis.appeloffre.terragis_project.entity;

import com.fasterxml.jackson.annotation.JsonIgnore; // Added import
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Contact {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private String email;
    private String phone;
    private String position;

    @ManyToOne
    @JoinColumn(name = "client_id") // Ensure this matches the foreign key column name in your DB
    @JsonIgnore // Added to break the circular reference
    private MaitreOeuvrage client;
}

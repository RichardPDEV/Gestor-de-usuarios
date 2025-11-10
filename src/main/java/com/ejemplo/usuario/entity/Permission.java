package com.ejemplo.usuario.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "permissions")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Permission {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    @Column(name = "description")
    private String description;

    @Column(name = "resource")
    private String resource; // Ej: "user", "product", etc.

    @Column(name = "action")
    private String action; // Ej: "read", "write", "delete", etc.

    @Column(name = "http_method")
    private String httpMethod; // Ej: "GET", "POST", "PUT", "DELETE"

}


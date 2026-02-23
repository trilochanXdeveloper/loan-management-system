package com.loanmanagement.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "audit_logs")
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String action;

    @Column(nullable = false)
    private String performedBy;

    @Column(nullable = false)
    private String entityName;

    @Column(nullable = false)
    private Long entityId;

    @Column(nullable = false, length = 1000)
    private String details;

    @CreationTimestamp
    private LocalDateTime createdAt;
}
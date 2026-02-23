package com.loanmanagement.entity;

import com.loanmanagement.enums.EmiStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "emi_schedule")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmiSchedule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "loan_id")
    private Loan loan;

    @Column(nullable = false,precision = 15,scale = 2)
    private BigDecimal emiAmount;

    @Column(nullable = false)
    private LocalDate dueDate;

    @Column(nullable = true)
    private LocalDate paidDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EmiStatus status;

    @OneToOne(mappedBy = "emiSchedule",cascade = CascadeType.ALL)
    private Payment payment;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;


}
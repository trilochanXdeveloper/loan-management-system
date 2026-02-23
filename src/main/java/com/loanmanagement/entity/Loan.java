package com.loanmanagement.entity;

import com.loanmanagement.enums.LoanStatus;
import com.loanmanagement.enums.LoanType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;


@Entity
@Data
@Table(name = "loans")
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Loan {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id",nullable = false)
    private User user;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal loanAmount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private LoanType loanType;

    @Column(nullable = false, precision = 5, scale = 2)
    private BigDecimal interestRate;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal processingFee;

    @Column(nullable = false)
    private Integer tenureMonths;

    @Column(nullable = true)
    private String collateralDetails;

    @Enumerated(EnumType.STRING)
    private LoanStatus status;

    @OneToOne(mappedBy = "loan",cascade = CascadeType.ALL)
    private Approval approval;

    @OneToMany(mappedBy = "loan", cascade = CascadeType.ALL)
    private List<EmiSchedule> emiSchedules;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
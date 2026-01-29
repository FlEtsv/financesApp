package com.finances.main.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;

/**
 * Cuenta financiera principal donde se registran transacciones.
 */
@Entity
@Table(name = "accounts")
public class Account {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String currency;

    /**
     * Saldo inicial declarado manualmente para la cuenta.
     */
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal initialBalance = BigDecimal.ZERO;

    @Column(nullable = false)
    private Instant createdAt = Instant.now();

    public Account() {
    }

    public Account(String name, String currency) {
        this.name = name;
        this.currency = currency;
        this.initialBalance = BigDecimal.ZERO;
    }

    public Account(String name, String currency, BigDecimal initialBalance) {
        this.name = name;
        this.currency = currency;
        this.initialBalance = initialBalance == null ? BigDecimal.ZERO : initialBalance;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public BigDecimal getInitialBalance() {
        return initialBalance;
    }

    public void setInitialBalance(BigDecimal initialBalance) {
        this.initialBalance = initialBalance == null ? BigDecimal.ZERO : initialBalance;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}

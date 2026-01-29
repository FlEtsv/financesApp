package com.finances.main.model;

import jakarta.persistence.*;
import org.springframework.data.annotation.Id;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
public class GastoFijo {

    public Long getId1() {
        return id1;
    }

    public void setId1(Long id1) {
        this.id1 = id1;
    }

    @jakarta.persistence.Id
    private Long id1;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nombre;

    private BigDecimal importe;

    @Enumerated(EnumType.STRING)
    private Periodicidad periodicidad;

    private LocalDate fechaInicio;

    private boolean activo = true;

    @ManyToOne
    private Account cuenta;
}

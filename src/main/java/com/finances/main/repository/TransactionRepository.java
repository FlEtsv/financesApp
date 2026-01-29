package com.finances.main.repository;

import com.finances.main.model.CategoryType;
import com.finances.main.model.Transaction;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * Acceso a datos de transacciones con consultas optimizadas.
 */
public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    /**
     * Lista transacciones por cuenta ordenadas por fecha descendente.
     */
    List<Transaction> findByAccountIdOrderByTransactionDateDesc(Long accountId);

    /**
     * Obtiene transacciones en un rango de fechas para una cuenta.
     */
    @Query("""
        select t
        from Transaction t
        where t.account.id = :accountId
          and t.transactionDate between :startDate and :endDate
        order by t.transactionDate asc
        """)
    List<Transaction> findByAccountAndDateRange(
        @Param("accountId") Long accountId,
        @Param("startDate") LocalDate startDate,
        @Param("endDate") LocalDate endDate
    );

    /**
     * Suma de montos por tipo de categoría (ingreso o gasto).
     */
    @Query("""
        select coalesce(sum(t.amount), 0)
        from Transaction t
        where t.account.id = :accountId
          and t.category.type = :type
        """)
    BigDecimal sumByAccountAndCategoryType(
        @Param("accountId") Long accountId,
        @Param("type") CategoryType type
    );

    /**
     * Totales agrupados por categoría para una cuenta.
     */
    @Query("""
        select t.category.name, coalesce(sum(t.amount), 0)
        from Transaction t
        where t.account.id = :accountId
          and t.category.type = :type
        group by t.category.name
        """)
    List<Object[]> totalsByCategory(
        @Param("accountId") Long accountId,
        @Param("type") CategoryType type
    );

    /**
     * Obtiene transacciones en un rango de fechas para una cuenta por nombre.
     */
    @Query("""
        select t
        from Transaction t
        where lower(t.account.name) = lower(:accountName)
          and t.transactionDate between :startDate and :endDate
        order by t.transactionDate asc
        """)
    List<Transaction> findByAccountNameAndDateRange(
        @Param("accountName") String accountName,
        @Param("startDate") LocalDate startDate,
        @Param("endDate") LocalDate endDate
    );

    /**
     * Suma de montos por tipo de categoría usando el nombre de cuenta.
     */
    @Query("""
        select coalesce(sum(t.amount), 0)
        from Transaction t
        where lower(t.account.name) = lower(:accountName)
          and t.category.type = :type
        """)
    BigDecimal sumByAccountNameAndCategoryType(
        @Param("accountName") String accountName,
        @Param("type") CategoryType type
    );

    /**
     * Totales agrupados por categoría usando el nombre de cuenta.
     */
    @Query("""
        select t.category.name, coalesce(sum(t.amount), 0)
        from Transaction t
        where lower(t.account.name) = lower(:accountName)
          and t.category.type = :type
        group by t.category.name
        """)
    List<Object[]> totalsByCategoryAndAccountName(
        @Param("accountName") String accountName,
        @Param("type") CategoryType type
    );
}

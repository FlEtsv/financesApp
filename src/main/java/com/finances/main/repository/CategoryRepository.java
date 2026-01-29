package com.finances.main.repository;

import com.finances.main.model.Category;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Acceso a datos de categorías.
 */
public interface CategoryRepository extends JpaRepository<Category, Long> {
}

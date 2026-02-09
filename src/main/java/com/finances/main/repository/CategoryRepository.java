package com.finances.main.repository;

import com.finances.main.model.Category;
import com.finances.main.model.CategoryType;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Acceso a datos de categorías.
 */
public interface CategoryRepository extends JpaRepository<Category, Long> {
    /**
     * Busca una categoría por nombre y tipo.
     */
    Optional<Category> findByNameIgnoreCaseAndType(String name, CategoryType type);

    /**
     * Lista categorías por tipo ordenadas alfabéticamente.
     */
    List<Category> findByTypeOrderByNameAsc(CategoryType type);
}

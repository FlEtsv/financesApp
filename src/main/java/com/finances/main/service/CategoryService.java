package com.finances.main.service;

import com.finances.main.model.Category;
import com.finances.main.model.CategoryType;
import com.finances.main.repository.CategoryRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Servicio para gestionar categorías de ingresos y gastos.
 */
@Service
@Transactional
public class CategoryService {
    private final CategoryRepository categoryRepository;

    public CategoryService(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    /**
     * Obtiene una categoría por nombre y tipo o la crea si no existe.
     */
    public Category getOrCreate(String name, CategoryType type) {
        return categoryRepository.findByNameIgnoreCaseAndType(name, type)
            .orElseGet(() -> categoryRepository.save(new Category(name, type)));
    }

    /**
     * Lista categorías disponibles por tipo.
     */
    @Transactional(readOnly = true)
    public List<Category> listByType(CategoryType type) {
        return categoryRepository.findByTypeOrderByNameAsc(type);
    }
}

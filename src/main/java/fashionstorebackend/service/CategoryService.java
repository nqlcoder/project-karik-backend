package fashionstorebackend.service;

import fashionstorebackend.dto.category.CategoryRequest;
import fashionstorebackend.dto.category.CategoryResponse;
import fashionstorebackend.entity.Category;
import fashionstorebackend.exception.CategoryNotFoundException;
import fashionstorebackend.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;

    public List<CategoryResponse> getAllCategories() {

        return categoryRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public CategoryResponse getCategoryById(Long id) {

        Category category = categoryRepository.findById(id)
                .orElseThrow(() ->
                        new CategoryNotFoundException(id)
                );

        return toResponse(category);
    }

    public CategoryResponse createCategory(CategoryRequest request) {

        if (categoryRepository.existsBySlug(request.getSlug())) {
            throw new IllegalArgumentException(
                    "Category slug already exists: " + request.getSlug()
            );
        }

        Category parent = null;

        if (request.getParentId() != null) {
            parent = categoryRepository.findById(request.getParentId())
                    .orElseThrow(() ->
                            new CategoryNotFoundException(request.getParentId())
                    );
        }

        Category category = Category.builder()
                .parent(parent)
                .name(request.getName())
                .slug(request.getSlug())
                .description(request.getDescription())
                .status(request.getStatus())
                .build();

        Category savedCategory = categoryRepository.save(category);

        return toResponse(savedCategory);
    }

    public CategoryResponse updateCategory(
            Long id,
            CategoryRequest request
    ) {

        Category category = categoryRepository.findById(id)
                .orElseThrow(() ->
                        new CategoryNotFoundException(id)
                );

        if (categoryRepository.existsBySlugAndIdNot(
                request.getSlug(),
                id
        )) {
            throw new IllegalArgumentException(
                    "Category slug already exists: " + request.getSlug()
            );
        }

        Category parent = null;

        if (request.getParentId() != null) {

            if (request.getParentId().equals(id)) {
                throw new IllegalArgumentException(
                        "Category cannot be its own parent"
                );
            }

            parent = categoryRepository.findById(request.getParentId())
                    .orElseThrow(() ->
                            new CategoryNotFoundException(
                                    request.getParentId()
                            )
                    );
        }

        category.setParent(parent);
        category.setName(request.getName());
        category.setSlug(request.getSlug());
        category.setDescription(request.getDescription());
        category.setStatus(request.getStatus());

        Category updatedCategory = categoryRepository.save(category);

        return toResponse(updatedCategory);
    }

    public void deleteCategory(Long id) {

        Category category = categoryRepository.findById(id)
                .orElseThrow(() ->
                        new CategoryNotFoundException(id)
                );

        categoryRepository.delete(category);
    }

    private CategoryResponse toResponse(Category category) {

        return CategoryResponse.builder()
                .id(category.getId())
                .parentId(
                        category.getParent() != null
                                ? category.getParent().getId()
                                : null
                )
                .name(category.getName())
                .slug(category.getSlug())
                .description(category.getDescription())
                .status(category.getStatus())
                .createdAt(category.getCreatedAt())
                .updatedAt(category.getUpdatedAt())
                .build();
    }
}
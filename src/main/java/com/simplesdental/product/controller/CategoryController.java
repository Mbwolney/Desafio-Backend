package com.simplesdental.product.controller;

import com.simplesdental.product.model.Category;
import com.simplesdental.product.service.CategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;


@Tag(name = "Categorias", description = "Operações das Categorias")
@RestController
@RequestMapping("/api/categories")
public class CategoryController {

    private final CategoryService categoryService;

    private static Logger logger = LoggerFactory.getLogger(CategoryController.class);

    @Autowired
    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @Operation(summary = "Listar todas as Categorias", description = "Este endpoint lista todas as categorias disponíveis.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Listagem das categorias com sucesso",
                    content = @Content(mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(implementation = Category.class)))),
            @ApiResponse(responseCode = "400", description = "Parâmetros de requisição inválidos",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(example = "{\"message\": \"Parâmetros de paginação inválidos\"}"))),
            @ApiResponse(responseCode = "500", description = "Erro interno no servidor",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(example = "{\"message\": \"Erro interno no servidor\"}")))
    })
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    @GetMapping
    public ResponseEntity<Page<Category>> getAllCategories(
            @Parameter(description = "Número da página (0 = primeira página)", example = "0")
            @RequestParam(defaultValue = "0") int page,

            @Parameter(description = "Tamanho da página", example = "10")
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        logger.info("[CategoryController:getAllCategories] Buscando todas as categorias - Página: {}, Tamanho: {}", pageable.getPageNumber(), pageable.getPageSize());
        Page<Category> categories = categoryService.findAll(pageable);
        logger.debug("[CategoryController:getAllCategories] Total de categorias encontradas: {}", categories.getTotalElements());
        return ResponseEntity.ok(categories);
    }

    @Operation(summary = "Buscar categoria por ID", description = "Retorna a categoria correspondente ao ID fornecido.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Categoria encontrada com sucesso",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = Category.class))),
            @ApiResponse(
                    responseCode = "404",
                    description = "Categoria não encontrada",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(),
                            examples = @ExampleObject(value = "")
                    )
            )
    })
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    @GetMapping("/{id}")
    public ResponseEntity<Category> getCategoryById(@PathVariable Long id) {
        logger.info("[CategoryController:getCategoryById] Buscando categoria com ID: {}", id);
        return categoryService.findById(id)
                .map(category -> {
                    logger.debug("[CategoryController:getCategoryById] Categoria encontrada: {}", category.getName());
                    return ResponseEntity.ok(category);
                })
                .orElseGet(() -> {
                    logger.warn("[CategoryController:getCategoryById] Categoria com ID {} não encontrada", id);
                    return ResponseEntity.notFound().build();
                });
    }

    @Operation(summary = "Criar uma nova categoria", description = "Adiciona uma nova categoria ao sistema. Requer role ADMIN.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Categoria criada com sucesso",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = Category.class))),
            @ApiResponse(responseCode = "400", description = "Dados inválidos enviados",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(example = "{\"name\": \"não deve estar em branco\"}")))
    })
    @PreAuthorize("hasAnyRole('ADMIN')")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Category createCategory(@Valid @RequestBody Category category) {
        logger.info("[CategoryController:createCategory] Criando nova categoria: {}", category.getName());
        Category savedCategory = categoryService.save(category);
        logger.debug("[CategoryController:createCategory] Categoria criada com ID: {}", savedCategory.getId());
        return savedCategory;
    }

    @Operation(summary = "Atualizar uma categoria", description = "Atualiza os dados de uma categoria existente pelo ID. Requer role ADMIN.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Categoria atualizada com sucesso",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = Category.class))),
            @ApiResponse(responseCode = "400", description = "Dados inválidos enviados",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(example = "{\"name\": \"não deve estar em branco\"}"))),
            @ApiResponse(
                    responseCode = "404",
                    description = "Categoria não encontrada para atualização",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(),
                            examples = @ExampleObject(value = "")
                    )
            )
    })
    @PreAuthorize("hasAnyRole('ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<Category> updateCategory(@PathVariable Long id, @Valid @RequestBody Category category) {
        logger.info("[CategoryController:updateCategory] Atualizando categoria com ID: {}", id);
        return categoryService.findById(id)
                .map(existingCategory -> {
                    category.setId(id);
                    Category updated = categoryService.save(category);
                    logger.debug("[CategoryController:updateCategory] Categoria atualizada: {}", updated.getName());
                    return ResponseEntity.ok(updated);
                })
                .orElseGet(() -> {
                    logger.warn("[CategoryController:updateCategory] Categoria com ID {} não encontrada para atualização", id);
                    return ResponseEntity.notFound().build();
                });
    }

    @Operation(summary = "Excluir uma categoria", description = "Remove uma categoria existente pelo ID. Requer role ADMIN.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Categoria excluída com sucesso"),
            @ApiResponse(
                    responseCode = "404",
                    description = "Categoria não encontrada para exclusão",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(),
                            examples = @ExampleObject(value = "")
                    )
            )
    })
    @PreAuthorize("hasAnyRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCategory(@PathVariable Long id) {
        logger.info("[CategoryController:deleteCategory] Excluindo categoria com ID: {}", id);
        return categoryService.findById(id)
            .map(category -> {
                categoryService.deleteById(id);
                logger.debug("[CategoryController:deleteCategory] Categoria com ID {} excluída", id);
                return ResponseEntity.noContent().<Void>build();
            })
            .orElseGet(() -> {
                logger.warn("[CategoryController:deleteCategory] Categoria com ID {} não encontrada para exclusão", id);
                return ResponseEntity.notFound().build();
            });
    }
}
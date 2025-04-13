package com.simplesdental.product.controller;

import com.simplesdental.product.model.Product;
import com.simplesdental.product.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import org.hibernate.Hibernate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    private static final Logger logger = LoggerFactory.getLogger(ProductController.class);

    private final ProductService productService;

    @Autowired
    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @Operation(summary = "Listar todos os produtos", description = "Lista paginada de todos os produtos.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de produtos retornada com sucesso",
                    content = @Content(mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(implementation = Product.class)))),
            @ApiResponse(responseCode = "403", description = "Token JWT ausente ou inválido", content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(),
                    examples = @ExampleObject(value = "")
            ))
    })
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    @GetMapping
    @Transactional
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<Page<Product>> getAllProducts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        logger.info("[ProductController:getAllProducts] Listando produtos - page={}, size={}", page, size);

        Pageable pageable = PageRequest.of(page, size);
        Page<Product> products = productService.findAll(pageable);

        products.forEach(product -> {
            if (product.getCategory() != null) {
                Hibernate.initialize(product.getCategory());
            }
        });

        logger.debug("[ProductController:getAllProducts] Produtos encontrados: {}", products.getContent().size());

        return ResponseEntity.ok(products);
    }

    @Operation(summary = "Buscar produto por ID", description = "Retorna um produto pelo ID fornecido.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Produto encontrado",
                    content = @Content(schema = @Schema(implementation = Product.class))),
            @ApiResponse(
                    responseCode = "403",
                    description = "Token JWT ausente ou inválido",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(),
                            examples = @ExampleObject(value = "")
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Produto não encontrado",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(),
                            examples = @ExampleObject(value = "")
                    )
            )
    })
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    @GetMapping("/{id}")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<Product> getProductById(@PathVariable Long id) {
        logger.info("[ProductController:getProductById] Buscando produto com id={}", id);

        return productService.findById(id)
                .map(product -> {
                    if (product.getCategory() != null) {
                        Hibernate.initialize(product.getCategory());
                    }
                    logger.info("[ProductController:getProductById] Produto encontrado: {}", product.getName());
                    return ResponseEntity.ok(product);
                })
                .orElseGet(() -> {
                    logger.error("[ProductController:getProductById] Produto com id={} não encontrado", id);
                    return ResponseEntity.notFound().build();
                });
    }

    @Operation(summary = "Criar novo produto", description = "Cria um novo produto. Requer autenticação e perfil ADMIN.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Produto criado com sucesso",
                    content = @Content(schema = @Schema(implementation = Product.class))),
            @ApiResponse(responseCode = "400", description = "Dados inválidos enviados",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(example = "{\"name\": \"não deve estar em branco\"}"))),
            @ApiResponse(responseCode = "403", description = "Token JWT ausente ou inválido",
                    content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(),
                    examples = @ExampleObject(value = "")
            ))
    })
    @PreAuthorize("hasAnyRole('ADMIN')")
    @PostMapping
    @SecurityRequirement(name = "bearerAuth")
    @ResponseStatus(HttpStatus.CREATED)
    public Product createProduct(@Valid @RequestBody Product product) {
        logger.info("[ProductController:createProduct] Criando novo produto: {}", product.getName());
        return productService.save(product);
    }

    @Operation(summary = "Atualizar produto", description = "Atualiza os dados de um produto existente.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Produto atualizado com sucesso",
                    content = @Content(schema = @Schema(implementation = Product.class))),
            @ApiResponse(responseCode = "400", description = "Dados inválidos enviados",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(example = "{\"name\": \"não deve estar em branco\"}"))),
            @ApiResponse(responseCode = "403", description = "Token JWT ausente ou inválido",
                    content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(),
                    examples = @ExampleObject(value = "")
            )),
            @ApiResponse(
                    responseCode = "404",
                    description = "Produto não encontrado",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(),
                            examples = @ExampleObject(value = "")
                    )
            )
    })
    @PreAuthorize("hasAnyRole('ADMIN')")
    @PutMapping("/{id}")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<Product> updateProduct(@PathVariable Long id, @Valid @RequestBody Product product) {
        logger.info("[ProductController:updateProduct] Atualizando produto id={}", id);

        return productService.findById(id)
            .map(existingProduct -> {
                product.setId(id);
                logger.debug("[ProductController:updateProduct] Dados atualizados: {}", product);
                return ResponseEntity.ok(productService.save(product));
            })
            .orElseGet(() -> {
                logger.error("[ProductController:updateProduct] Produto com id={} não encontrado para atualização", id);
                return ResponseEntity.notFound().build();
            });
    }

    @Operation(summary = "Deletar produto", description = "Remove um produto existente com base no ID.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Produto removido com sucesso"),
            @ApiResponse(responseCode = "403", description = "Token JWT ausente ou inválido",
                    content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(),
                    examples = @ExampleObject(value = "")
            )),
            @ApiResponse(
                    responseCode = "404",
                    description = "Produto não encontrado",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(),
                            examples = @ExampleObject(value = "")
                    )
            )
    })
    @PreAuthorize("hasAnyRole('ADMIN')")
    @DeleteMapping("/{id}")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long id) {
        logger.info("[ProductController:deleteProduct] Removendo produto com id={}", id);
        return productService.findById(id)
            .map(product -> {
                productService.deleteById(id);
                logger.info("[ProductController:deleteProduct] Produto removido com sucesso");
                return ResponseEntity.noContent().<Void>build();
            })
            .orElseGet(() -> {
                logger.error("[ProductController:deleteProduct] Produto com id={} não encontrado para exclusão", id);
                return ResponseEntity.notFound().build();
            });
    }
}
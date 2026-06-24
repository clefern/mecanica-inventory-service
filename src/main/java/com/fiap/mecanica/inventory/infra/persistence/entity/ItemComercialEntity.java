package com.fiap.mecanica.inventory.infra.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "itens_comerciais")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ItemComercialEntity {

  @Id
  private UUID id;

  @Column(name = "tipo_item", nullable = false)
  private String tipoItem;

  @Column(name = "nome", nullable = false)
  private String nome;

  @Column(name = "descricao")
  private String descricao;

  @Column(name = "preco_base", nullable = false)
  private BigDecimal precoBase;

  @Column(name = "ativo", nullable = false)
  private boolean ativo = true;

  @Column(name = "quantidade_estoque", nullable = false)
  private int quantidadeEstoque = 0;

  @Column(name = "estoque_minimo", nullable = false)
  private int estoqueMinimo = 0;

  @Column(name = "estoque_maximo", nullable = false)
  private int estoqueMaximo = 0;

  @Column(name = "fabricante")
  private String fabricante;

  @Column(name = "codigo_fabricante")
  private String codigoFabricante;

  @Column(name = "modelo_peca")
  private String modeloPeca;

  @Column(name = "unidade_medida")
  private String unidadeMedida;

  @Column(name = "created_at")
  private LocalDateTime createdAt;

  @Column(name = "updated_at")
  private LocalDateTime updatedAt;

  @PrePersist
  void prePersist() {
    if (id == null) id = UUID.randomUUID();
    createdAt = LocalDateTime.now();
    updatedAt = LocalDateTime.now();
  }

  @PreUpdate
  void preUpdate() {
    updatedAt = LocalDateTime.now();
  }
}

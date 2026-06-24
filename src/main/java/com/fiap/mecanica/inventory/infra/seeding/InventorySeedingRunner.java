package com.fiap.mecanica.inventory.infra.seeding;

import com.fiap.mecanica.inventory.infra.persistence.entity.ItemComercialEntity;
import com.fiap.mecanica.inventory.infra.persistence.repository.ItemComercialJpaRepository;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class InventorySeedingRunner implements CommandLineRunner {

  // UUIDs fixos para referenciar nas OSs de teste
  public static final UUID PECA_FILTRO_OLEO_ID =
      UUID.fromString("10000000-0000-0000-0000-000000000001");
  public static final UUID PECA_PASTILHA_FREIO_ID =
      UUID.fromString("10000000-0000-0000-0000-000000000002");
  public static final UUID PECA_CORREIA_DENTADA_ID =
      UUID.fromString("10000000-0000-0000-0000-000000000003");
  public static final UUID INSUMO_OLEO_MOTOR_ID =
      UUID.fromString("10000000-0000-0000-0000-000000000011");
  public static final UUID INSUMO_FLUIDO_FREIO_ID =
      UUID.fromString("10000000-0000-0000-0000-000000000012");

  private final ItemComercialJpaRepository itemRepository;

  @Value("${seeding.enabled:true}")
  private boolean seedingEnabled;

  @Override
  public void run(String... args) {
    if (!seedingEnabled) {
      log.info("Seeding desabilitado.");
      return;
    }
    if (itemRepository.count() > 0) {
      log.info("Seeding de inventory já executado — pulando.");
      return;
    }

    log.info("[SEED] Iniciando seeding do inventory-service...");
    LocalDateTime now = LocalDateTime.now();

    List<ItemComercialEntity> items = List.of(
        peca(PECA_FILTRO_OLEO_ID, "Filtro de Óleo", "Filtro de óleo para motores 1.0-2.0",
            new BigDecimal("45.90"), 50, 5, 100, "Mann Filter", "W712/95", now),
        peca(PECA_PASTILHA_FREIO_ID, "Pastilha de Freio Dianteira",
            "Pastilha de freio cerâmica dianteira", new BigDecimal("189.90"),
            30, 4, 60, "Bosch", "BP897", now),
        peca(PECA_CORREIA_DENTADA_ID, "Correia Dentada",
            "Correia dentada para motores 1.6 Flex", new BigDecimal("320.00"),
            20, 2, 40, "Gates", "5687XS", now),
        insumo(INSUMO_OLEO_MOTOR_ID, "Óleo de Motor 5W30",
            "Óleo sintético 5W30 API SP", new BigDecimal("65.00"),
            100, 10, 200, "LITRO", now),
        insumo(INSUMO_FLUIDO_FREIO_ID, "Fluido de Freio DOT4",
            "Fluido de freio DOT4 500ml", new BigDecimal("28.50"),
            80, 8, 160, "FRASCO", now)
    );

    itemRepository.saveAll(items);
    log.info("[SEED] {} itens de estoque criados", items.size());
  }

  private ItemComercialEntity peca(UUID id, String nome, String descricao, BigDecimal preco,
      int qtd, int min, int max, String fabricante, String codigo, LocalDateTime now) {
    return ItemComercialEntity.builder()
        .id(id).tipoItem("PECA").nome(nome).descricao(descricao).precoBase(preco).ativo(true)
        .quantidadeEstoque(qtd).estoqueMinimo(min).estoqueMaximo(max)
        .fabricante(fabricante).codigoFabricante(codigo)
        .createdAt(now).updatedAt(now).build();
  }

  private ItemComercialEntity insumo(UUID id, String nome, String descricao, BigDecimal preco,
      int qtd, int min, int max, String unidade, LocalDateTime now) {
    return ItemComercialEntity.builder()
        .id(id).tipoItem("INSUMO").nome(nome).descricao(descricao).precoBase(preco).ativo(true)
        .quantidadeEstoque(qtd).estoqueMinimo(min).estoqueMaximo(max)
        .unidadeMedida(unidade)
        .createdAt(now).updatedAt(now).build();
  }
}

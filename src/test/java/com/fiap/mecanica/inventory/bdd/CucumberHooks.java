package com.fiap.mecanica.inventory.bdd;

import com.fiap.mecanica.inventory.infra.persistence.repository.ProcessedCommandJpaRepository;
import io.cucumber.java.Before;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;

@RequiredArgsConstructor
public class CucumberHooks {

  private final ProcessedCommandJpaRepository processedRepo;
  private final JdbcTemplate jdbcTemplate;

  @Before
  public void limparComandos() {
    processedRepo.deleteAll();
    // Restaura estoque dos seeds fixos para valores iniciais
    jdbcTemplate.update("UPDATE itens_comerciais SET quantidade_estoque = 50 WHERE id = '10000000-0000-0000-0000-000000000001'");
    jdbcTemplate.update("UPDATE itens_comerciais SET quantidade_estoque = 30 WHERE id = '10000000-0000-0000-0000-000000000002'");
    jdbcTemplate.update("UPDATE itens_comerciais SET quantidade_estoque = 20 WHERE id = '10000000-0000-0000-0000-000000000003'");
    jdbcTemplate.update("UPDATE itens_comerciais SET quantidade_estoque = 100 WHERE id = '10000000-0000-0000-0000-000000000011'");
    jdbcTemplate.update("UPDATE itens_comerciais SET quantidade_estoque = 80 WHERE id = '10000000-0000-0000-0000-000000000012'");
  }
}

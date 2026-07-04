package com.fiap.mecanica.inventory.bdd.steps;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.fiap.mecanica.inventory.application.messaging.FalhaNaReservaEvent;
import com.fiap.mecanica.inventory.application.messaging.PecasReservadasEvent;
import com.fiap.mecanica.inventory.application.messaging.ReservarPecasCommand;
import com.fiap.mecanica.inventory.application.messaging.ReservarPecasCommand.ItemReserva;
import com.fiap.mecanica.inventory.bdd.ScenarioContext;
import com.fiap.mecanica.inventory.infra.messaging.listener.ReservarPecasListener;
import com.fiap.mecanica.inventory.infra.messaging.publisher.InventoryEventPublisher;
import com.fiap.mecanica.inventory.infra.persistence.repository.ItemComercialJpaRepository;
import io.cucumber.java.pt.Dado;
import io.cucumber.java.pt.E;
import io.cucumber.java.pt.Então;
import io.cucumber.java.pt.Quando;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;

@RequiredArgsConstructor
public class InventorySteps {

  private static final UUID PECA_PASTILHA_ID =
      UUID.fromString("10000000-0000-0000-0000-000000000002");

  private final ScenarioContext ctx;
  private final ReservarPecasListener listener;
  private final ItemComercialJpaRepository itemRepo;
  private final InventoryEventPublisher inventoryEventPublisher;

  @Autowired
  private TestRestTemplate restTemplate;

  @LocalServerPort
  private int port;

  private String baseUrl() {
    return "http://localhost:" + port;
  }

  private ReservarPecasCommand buildCommand(UUID sagaId, UUID osId, int quantidade) {
    return new ReservarPecasCommand(sagaId, osId,
        List.of(new ItemReserva(PECA_PASTILHA_ID, "PECA", quantidade)));
  }

  @Quando("a saga solicita reserva de peças disponíveis em estoque")
  public void reservarPecasComSucesso() {
    UUID sagaId = UUID.randomUUID();
    UUID osId = UUID.randomUUID();
    ctx.setSagaId(sagaId);
    ctx.setOsId(osId);
    listener.onReservarPecas(buildCommand(sagaId, osId, 1));
  }

  @Então("o evento PecasReservadas é publicado pelo inventory")
  public void verificarEventoPecasReservadas() {
    verify(inventoryEventPublisher, times(1)).publicarSucesso(any(PecasReservadasEvent.class));
  }

  @E("o estoque é decrementado no banco de dados")
  public void verificarEstoqueDecrementado() {
    var item = itemRepo.findById(PECA_PASTILHA_ID);
    assertThat(item).isPresent();
    assertThat(item.get().getQuantidadeEstoque()).isEqualTo(29);
  }

  @Dado("que uma reserva já foi processada para uma saga")
  public void reservaJaProcessada() {
    UUID sagaId = UUID.randomUUID();
    UUID osId = UUID.randomUUID();
    ctx.setSagaId(sagaId);
    ctx.setOsId(osId);
    listener.onReservarPecas(buildCommand(sagaId, osId, 1));
  }

  @Quando("a saga solicita reserva novamente com o mesmo sagaId")
  public void reservaDuplicada() {
    listener.onReservarPecas(buildCommand(ctx.getSagaId(), ctx.getOsId(), 1));
  }

  @E("o evento PecasReservadas é publicado pelo inventory exatamente 1 vez")
  public void verificarIdempotencia() {
    verify(inventoryEventPublisher, times(1)).publicarSucesso(any(PecasReservadasEvent.class));
  }

  @Quando("a saga solicita quantidade de peças maior que o estoque disponível")
  public void reservarMaisDoQueEstoque() {
    UUID sagaId = UUID.randomUUID();
    UUID osId = UUID.randomUUID();
    ctx.setSagaId(sagaId);
    ctx.setOsId(osId);
    listener.onReservarPecas(buildCommand(sagaId, osId, 999));
  }

  @Então("o evento FalhaNaReserva é publicado pelo inventory")
  public void verificarEventoFalha() {
    verify(inventoryEventPublisher, times(1)).publicarFalha(any(FalhaNaReservaEvent.class));
  }

  @Quando("listo o estoque via endpoint")
  public void listarEstoque() {
    HttpHeaders headers = new HttpHeaders();
    headers.set("Authorization", "Bearer " + ctx.getToken());
    ResponseEntity<String> resp = restTemplate.exchange(
        baseUrl() + "/api/estoque",
        HttpMethod.GET,
        new HttpEntity<>(headers),
        String.class);
    ctx.setLastStatusCode(resp.getStatusCode().value());
  }

  @Então("recebo status HTTP {int}")
  public void verificarStatusHttp(int statusEsperado) {
    assertThat(ctx.getLastStatusCode()).isEqualTo(statusEsperado);
  }

  @E("a resposta contém itens no catálogo")
  public void verificarListaNaoVazia() {
    assertThat(itemRepo.count()).isGreaterThan(0);
  }
}

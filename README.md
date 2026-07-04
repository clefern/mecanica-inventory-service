# mecanica-inventory-service

> Gerencia o estoque de peças e insumos, executando reserva e estorno como etapa compensável da Saga (Fase 4, Grupo 14SOAT).

## Responsabilidade na Saga

Este serviço atua como **participante compensável** da Saga. Ao receber `ReservarPecasCommand`, decrementa o estoque dos itens solicitados (peças e insumos) de forma idempotente. Em caso de estoque insuficiente ou erro, publica `FalhaNaReservaEvent` para que o orquestrador inicie a compensação. O estorno é disparado pelo orquestrador quando a Saga é cancelada.

```
os-service → [ReservarPecasCommand] → inventory-service
inventory-service → [PecasReservadasEvent | FalhaNaReservaEvent] → os-service
```

## Endpoints REST

| Método | Path | Descrição |
|--------|------|-----------|
| `GET` | `/api/estoque` | Listar todos os itens do catálogo |
| `GET` | `/api/estoque/{id}` | Buscar item por ID |

Swagger: `http://localhost:8082/swagger-ui.html`

## Mensagens RabbitMQ

### Consome
| Queue | Tipo | Ação |
|-------|------|------|
| `mecanica.inventory.reservar-pecas` | `ReservarPecasCommand` | Baixa estoque de peças/insumos |

### Publica
| Routing Key | Tipo | Condição |
|-------------|------|----------|
| `os.pecas-reservadas` | `PecasReservadasEvent` | Reserva bem-sucedida |
| `os.falha-reserva` | `FalhaNaReservaEvent` | Estoque insuficiente ou item não encontrado |

Idempotência garantida por `processed_commands` (deduplicação por `sagaId`).

## Itens de catálogo (seed)

| ID | Nome | Tipo | Estoque |
|----|------|------|---------|
| `10000000-…-0001` | Filtro de Óleo | PECA | 50 |
| `10000000-…-0002` | Pastilha de Freio Dianteira | PECA | 30 |
| `10000000-…-0003` | Correia Dentada | PECA | 20 |
| `10000000-…-0011` | Óleo de Motor 5W30 | INSUMO | 100 |
| `10000000-…-0012` | Fluido de Freio DOT4 | INSUMO | 80 |

## Como rodar localmente

```bash
# Stack completa (todos os MS + infra)
cd ms-infra-ms/mecanica-fiap
docker compose -f docker-compose.full.yml up --build

# Listar estoque
curl -s http://localhost/api/estoque \
  -H "Authorization: Bearer {token}"
```

## Testes

```bash
./mvnw test                       # unitários + BDD
./mvnw test -Dtest="CucumberTest" # apenas BDD
```

O BDD usa **Testcontainers** (PostgreSQL real) + `@MockBean` no `InventoryEventPublisher` — requer Docker em execução.

## Tech stack

| | |
|-|-|
| **Java** | 21 |
| **Framework** | Spring Boot 3.5.x |
| **Banco** | PostgreSQL 16 (porta 5434) |
| **Mensageria** | RabbitMQ 3.13 |
| **Migrations** | Flyway |
| **Segurança** | JWT (JJWT 0.12) |
| **Porta** | 8082 |
| **Cobertura** | JaCoCo ≥ 80% |
| **BDD** | Cucumber 7.21 + JUnit Platform Suite |

# language: pt
Funcionalidade: Inventory — reserva e estoque de peças
  Como sistema de estoque da mecânica
  Quero gerenciar reservas de peças para as ordens de serviço
  Para coordenar a etapa de materiais na Saga

  Contexto:
    Dado que tenho um token de autenticação válido

  Cenário: Reservar peças com sucesso
    Quando a saga solicita reserva de peças disponíveis em estoque
    Então o evento PecasReservadas é publicado pelo inventory
    E o estoque é decrementado no banco de dados

  Cenário: Comando duplicado é ignorado por idempotência
    Dado que uma reserva já foi processada para uma saga
    Quando a saga solicita reserva novamente com o mesmo sagaId
    Então o evento PecasReservadas é publicado pelo inventory exatamente 1 vez

  Cenário: Estoque insuficiente publica evento de falha
    Quando a saga solicita quantidade de peças maior que o estoque disponível
    Então o evento FalhaNaReserva é publicado pelo inventory

  Cenário: Listar estoque retorna itens autenticado
    Quando listo o estoque via endpoint
    Então recebo status HTTP 200
    E a resposta contém itens no catálogo

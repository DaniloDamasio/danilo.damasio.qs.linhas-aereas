# Persona: Agente de Viagens Corporativas

## Quem é

**Nome:** Patrícia Nogueira
**Idade:** 41 anos
**Ocupação:** Consultora de viagens em uma agência corporativa (atende empresas de médio porte)
**Localização:** Trabalha remotamente a partir de Brasília (DF), atendendo clientes de todo o Brasil
**Frequência de uso da plataforma:** Diária — gerencia entre 15 e 30 reservas por semana para diferentes empresas clientes
**Perfil tecnológico:** Alto — usa múltiplos sistemas de reserva (GDS), planilhas e ferramentas internas da agência

## Contexto

Patrícia não compra passagens para si mesma na plataforma — ela compra em nome de terceiros (funcionários de empresas clientes da agência), seguindo as políticas de viagem definidas por cada empresa (ex: só pode comprar classe econômica, limite de valor, companhias credenciadas). Ela precisa emitir várias passagens ao mesmo tempo, muitas vezes para o mesmo evento/reunião, e depois consolidar tudo em uma fatura única para a empresa cliente.

## Objetivos

- Emitir passagens para múltiplos passageiros de uma mesma empresa de forma ágil (idealmente em lote).
- Aplicar automaticamente as políticas de viagem de cada empresa cliente (limite de gasto, classe permitida, companhias credenciadas).
- Ter acesso a uma API ou painel que permita integrar a busca/reserva ao sistema interno da agência.
- Receber faturamento consolidado (uma nota fiscal por empresa cliente, não por passageiro).
- Conseguir alterar/cancelar reservas rapidamente quando o cliente muda de planos.

## Dores e Frustrações

- **Falta de compra em lote:** precisa repetir manualmente o processo de busca e compra para cada passageiro individualmente.
- **Sem integração via API:** precisa copiar/colar informações entre o sistema da agência e a plataforma de passagens, gerando retrabalho e risco de erro.
- **Faturamento fragmentado:** recebe uma nota fiscal por passagem, quando o cliente precisa de um único boleto/fatura mensal consolidado.
- **Falta de visibilidade sobre políticas:** a plataforma não sabe diferenciar que ela está comprando para "Empresa X" com regras diferentes de "Empresa Y".
- **Comunicação de mudanças:** quando um voo é alterado/cancelado pela companhia aérea, ela precisa ficar monitorando manualmente, sem notificações automáticas por reserva gerenciada.

## Cenário de Uso (Jornada)

> Uma empresa cliente da agência confirma que 8 funcionários vão participar de uma convenção em Salvador. Patrícia acessa o painel de agente na plataforma, seleciona o perfil da empresa (que já tem a política de viagem configurada: classe econômica, teto de R$ 1.200 por trecho, 3 companhias credenciadas). Ela importa a lista de 8 passageiros de uma planilha, a plataforma sugere automaticamente os voos dentro da política, e ela confirma a compra em lote. No fim do mês, a agência recebe uma única fatura consolidada com todas as reservas feitas para aquela empresa.

# Plano de TDD — Inventário de Regras de Negócio e Casos de Teste

> **Método.** Este documento **não inventa regras**. Cada regra de negócio (RN) traz o(s) identificador(es) de origem (RF-xx, RNF-xx, RESTRIÇÃO-CRÍTICA-01, seções de `arquitetura.md`, ADR-xx de `revisao-painel-atam.md`). Onde a fonte deixa a regra ambígua, incompleta ou conflitante, isso é registrado na [Seção 3](#3-ambiguidades-lacunas-e-perguntas) — nunca resolvido por suposição própria. Nenhum teste ou código de produção foi escrito nesta etapa.

## 0. Artefatos inspecionados

| Artefato | Caminho | Status |
|---|---|---|
| README / visão do projeto | `README.md` | Encontrado e lido |
| Requisitos funcionais e não funcionais | `REQUISITOS/requisitos-funcionais-e-nao-funcionais.md` | Encontrado e lido (RF-01…RF-33, RNF-01…RNF-33) |
| Personas | `PERSONAS/**/*.md` (7 arquivos) | Encontrados e lidos integralmente |
| Arquitetura | `docs/arquitetura.md` | Encontrado e lido integralmente (inclui §12 — modelo de reserva) |
| Avaliação ATAM | `docs/atam-avaliacao.md` | Encontrado; usado como fonte de cenários/riscos, referenciado onde relevante |
| Revisão de painel / ADRs | `docs/revisao-painel-atam.md` | Encontrado; contém ADR-01…ADR-09 (não há pasta `adr/` ou `docs/adr/` separada — as únicas decisões de arquitetura registradas como ADR estão nesse arquivo, §4) |
| Código-fonte | — | **Não encontrado.** Busca em toda a árvore do repositório (`find` a partir da raiz, exceto `.git`) não retornou diretórios de aplicação (`src/`, `app/`, `lib/`, etc.) nem arquivos de manifesto de build (`package.json`, `pom.xml`, `build.gradle`, `deps.edn`, `go.mod`, etc.). O repositório contém apenas documentação (README, REQUISITOS, PERSONAS, docs). |
| Testes existentes | — | **Não encontrados.** Nenhum diretório `test/`, `tests/`, `spec/` ou arquivo `*_test.*`/`*.spec.*` no repositório. |
| ADRs em arquivo dedicado | — | **Não encontrado como artefato isolado.** Busquei `ADR/`, `docs/adr/`, `*.adr.md` — inexistentes. As propostas de decisão (ADR-01 a ADR-09) estão embutidas em `docs/revisao-painel-atam.md`, §4, com status "Proposta — pendente de validação" (nenhuma foi formalmente adotada). |

Como não há código nem testes prévios, este plano é **puramente de especificação**: define o inventário de regras e o roteiro de casos de teste que deverão orientar o ciclo Red-Green-Refactor quando a implementação começar.

---

## 1. Convenções deste documento

- **RN-xx** — identificador de regra de negócio atribuído por este plano (não existe na fonte); cada RN cita seu(s) ID(s) de origem entre colchetes.
- Tipos de caso de teste, conforme solicitado: **CF** (caminho feliz), **LIM** (valor-limite/boundary), **INV** (entrada inválida), **CONF** (conflito), **PROIB** (estado proibido).
- Onde a fonte não define um valor concreto (ex.: TTL de hold), o caso de teste é registrado com o **parâmetro como variável a definir**, referenciando a lacuna correspondente na Seção 3, e o "resultado esperado" descreve o comportamento estrutural que **é** exigido pela fonte, não o valor numérico ausente.
- Regras cuja origem é uma **proposta de ADR não adotada** são marcadas explicitamente como `[ADR-xx — proposta, não decisão]`; casos de teste sobre elas são condicionais à adoção formal do ADR.

---

## 2. Inventário de regras de negócio e casos de teste

### 2.1 Grupo A — Exclusividade de assento e prevenção de overbooking (núcleo P0)

#### RN-A01 — Estado ativo único por (voo, assento)
**Origem:** RESTRIÇÃO-CRÍTICA-01; RNF-03; RF-13; RF-26; `arquitetura.md` §12.1, §12.2.
**Enunciado:** Para cada par `(voo_id, assento_id)` deve existir, a qualquer instante, no máximo um registro ativo com status `HOLD` ou `CONFIRMED`. Nunca duas vendas confirmadas para o mesmo assento.

| Tipo | Caso | Entrada/Cenário | Resultado esperado |
|---|---|---|---|
| CF | Reserva de assento livre | Assento em estado `AVAILABLE`; usuário solicita `HOLD` | Transição para `HOLD` concedida; um único registro ativo criado |
| CF | Confirmação após pagamento aprovado | `HOLD` ativo e válido; pagamento aprovado; `CONFIRM(hold_id)` | Transição `HOLD → CONFIRMED`; registro passa a terminal |
| CONF | Duas solicitações de `HOLD` simultâneas para o mesmo assento | Duas requisições concorrentes de `HOLD(voo, assento)` no mesmo instante | Exatamente uma é aceita (vencedora); a outra é rejeitada de forma determinística com erro "assento indisponível" — nunca ambas aceitas |
| PROIB | Duas vendas confirmadas para o mesmo assento | Tentar `CONFIRM` de dois `HOLD`s distintos que, por falha hipotética, existissem simultaneamente para o mesmo assento | Sistema deve impedir estruturalmente essa condição (é o estado que a unicidade de `(voo_id, assento_id)` existe para proibir); teste de invariante deve falhar a build/transação se detectado |
| INV | `HOLD` para assento inexistente no voo | `assento_id` fora do mapa da aeronave do voo | Requisição rejeitada, sem criação de registro de estoque |
| LIM | `HOLD` no exato instante em que outro `HOLD` do mesmo assento expira | Requisição de `HOLD` chega no limiar de expiração do `HOLD` concorrente vigente | Comportamento determinístico único: ou trata como `AVAILABLE` (expiração lazy, §12.4) e concede, ou trata como ainda ativo e rejeita — nunca ambíguo/duplicado |

#### RN-A02 — Máquina de estados do assento (AVAILABLE → HOLD → CONFIRMED / HOLD → AVAILABLE)
**Origem:** `arquitetura.md` §12.2; `ADR-01` (**decisão confirmada pelo stakeholder em 2026-09-17** — ver Seção 3.4) — TTL de HOLD diferenciado por meio de pagamento: **cartão de crédito/débito: 2–5 min; Pix: 10–15 min**.

| Tipo | Caso | Entrada/Cenário | Resultado esperado |
|---|---|---|---|
| CF | Ciclo completo de venda | `AVAILABLE → HOLD → CONFIRMED` | Cada transição válida e registrada; estado final `CONFIRMED` é terminal |
| CF | Cancelamento voluntário do hold | Usuário cancela checkout com `HOLD` ativo | `HOLD → AVAILABLE` imediato |
| LIM | Expiração exata no TTL (cartão: 2–5 min; Pix: 10–15 min) | `HOLD` atinge `expires_at` sem confirmação | `HOLD → AVAILABLE`, assento reaparece na disponibilidade agregada; `expires_at` é o limite inclusivo (no instante exato ainda é válido, imediatamente após é expirado); testes devem ser parametrizados pelos dois TTLs |
| PROIB | Transição `CONFIRMED → HOLD` ou `CONFIRMED → AVAILABLE` | Tentativa de reabrir ou reverter um assento `CONFIRMED` | Rejeitado — `CONFIRMED` é estado terminal (§12.2); nenhuma reversão automática é permitida pela fonte |
| INV | `CONFIRM` sem `HOLD` prévio | Chamada direta de `CONFIRM(hold_id)` inexistente | Rejeitado com erro; nenhuma criação de venda sem hold anterior |

#### RN-A03 — Escrita condicional (CAS) como mecanismo decisivo de posse
**Origem:** `arquitetura.md` §12.3; RESTRIÇÃO-CRÍTICA-01. *(A técnica exata é `[DECISÃO-SEM-REQUISITO quanto à técnica]`; o resultado — desempate mecânico e não ambíguo — é exigido pela fonte.)*

| Tipo | Caso | Entrada/Cenário | Resultado esperado |
|---|---|---|---|
| CF | Duas leituras concorrentes do mesmo estado `AVAILABLE`, uma escrita vencedora | Ambas leem `AVAILABLE`; ambas tentam `AVAILABLE→HOLD` | Apenas a que "commitar" primeiro é aceita; a outra recebe rejeição determinística (não erro genérico/timeout ambíguo) |
| CONF | N tentativas concorrentes (N > 2) para o mesmo assento | N requisições simultâneas | Exatamente uma aceita, N-1 rejeitadas, todas de forma consistente e auditável |
| PROIB | "Quase-vitória" ou resultado ambíguo | Qualquer situação onde não fique decidido univocamente qual requisição venceu | Não deve ocorrer — é explicitamente proibido pela fonte ("não há ambiguidade, não há quase-vitória") |

#### RN-A04 — Expiração de HOLD (lazy + sweeper)
**Origem:** `arquitetura.md` §12.4; `ADR-01` (**decisão confirmada pelo stakeholder em 2026-09-17** — ver Seção 3.4). **TTL confirmado, diferenciado por meio de pagamento: cartão de crédito/débito 2–5 min; Pix 10–15 min.** (Lacuna L-07 original resolvida.)

| Tipo | Caso | Entrada/Cenário | Resultado esperado |
|---|---|---|---|
| CF | Sweeper periódico libera hold vencido | `HOLD` com `expires_at` no passado (TTL de cartão ou Pix já vencido), sem nova tentativa sobre o assento | Processo periódico transiciona para `AVAILABLE` e republica no estoque agregado |
| CF | Expiração lazy detectada em nova tentativa | Nova `HOLD` sobre assento cujo `HOLD` anterior já expirou (conforme TTL do meio de pagamento original) | Tratado como `AVAILABLE`; nova tentativa pode ser concedida |
| LIM | `CONFIRM` chega exatamente no instante de expiração | Pagamento aprovado no mesmo milissegundo do `expires_at` (TTL de cartão ou de Pix) | `expires_at` é o limite inclusivo: no instante exato ainda é considerado válido; qualquer instante posterior é tratado como expirado. Testes devem cobrir ambos os TTLs (cartão e Pix) |
| PROIB | Confirmação de HOLD expirado sem revalidação | `CONFIRM` aceito sobre `HOLD` cujo TTL já expirou, sem verificar `expires_at` no momento da escrita | Deve ser impedido — violaria RNF-19 (posse) e a garantia de decisão única |

#### RN-A05 — Reconciliação de estoque com a companhia aérea (zero divergência)
**Origem:** RNF-03; RF-26; RNF-06; RNF-10; `arquitetura.md` §9 (Adaptador de Integração).

| Tipo | Caso | Entrada/Cenário | Resultado esperado |
|---|---|---|---|
| CF | Venda confirmada propaga para a companhia | `CONFIRMED` gerado | Evento de estoque publicado; sincronização com a companhia concluída em ≤ 2s (P95) / ≤ 5s (P99) [RNF-06] |
| LIM | Propagação no limite de tempo | Sincronização concluída exatamente aos 2s / aos 5s | Dentro do SLA (2s/5s são os próprios limiares definidos, não "abaixo de") — confirmar interpretação de fronteira (≤ inclui o limite) |
| CONF | Falha de comunicação durante sincronização (1ª tentativa) | Timeout na 1ª tentativa de webhook | Reenvio com backoff conforme RNF-10 (1s) |
| CONF | Falha nas 3 tentativas de webhook | 3 tentativas falham (backoff 1s/5s/30s, timeout 10s cada) | **Decisão confirmada pelo stakeholder em 2026-09-17** (Seção 3.4): fail-closed **total** foi escolhido em vez da proposta original do `ADR-03` (suspender apenas `CONFIRM`, mantendo busca visível). Após esgotar RNF-10, tanto `CONFIRM` quanto a **busca** devem ser suspensos/bloqueados para a companhia cujo estado de sincronização ficou desconhecido — não há mais modo "busca visível com aviso" |
| PROIB | Estoque exibido diverge do real | Qualquer divergência entre estoque exibido e estoque real da companhia | Proibido — RNF-03 exige divergência **0** (zero tolerância) |
| INV | Evento de estoque com voo/assento desconhecido pela plataforma | Payload de sincronização referenciando voo inexistente no Catálogo | Rejeitado / registrado como inconsistência, não aplicado silenciosamente |

#### RN-A06 — Emissão em lote não contorna exclusividade
**Origem:** RF-21; RNF-08; `arquitetura.md` §9, §11.3, §12.6.

| Tipo | Caso | Entrada/Cenário | Resultado esperado |
|---|---|---|---|
| CF | Lote de 8 passageiros, todos os assentos disponíveis | Upload de planilha com 8 passageiros (cenário da persona) | Cada item processado como uma tentativa individual de HOLD/CONFIRM; todos confirmados |
| LIM | Lote no limite máximo (50 passageiros) | Upload com exatamente 50 passageiros | Processamento completo em ≤ 30s [RNF-08, `[baseline sugerida]`], com progresso reportado a cada ≤ 2s |
| INV | Lote acima do limite (51+ passageiros) | Upload com 51 passageiros | **Decisão confirmada pelo stakeholder em 2026-09-17** (Seção 3.4): upload com mais de 50 passageiros é **rejeitado na ingestão** — nenhum processamento parcial, nenhuma tentativa de SLA sobre o excedente |
| CONF | Item do lote concorre com uma reserva individual (canal online) pelo mesmo assento | Passageiro do lote e passageiro do checkout online tentam o mesmo assento quase simultaneamente | Mesma regra de exclusão mútua do RN-A01/RN-A03 se aplica; apenas um vence |
| PROIB | Lote força venda de assento indisponível para "fechar o lote" | Item do lote sem assento disponível | Proibido forçar; item deve ser reportado como falha individual, sem violar exclusividade (§11.3: "lote nunca força venda") |

---

### 2.2 Grupo B — Busca, comparação e precificação

#### RN-B01 — Busca por origem, destino e data/período com filtros de urgência
**Origem:** RF-01. **Escopo confirmado pelo stakeholder em 2026-09-17** (Seção 3.4): plataforma cobre **apenas rotas domésticas (Brasil)** — rotas internacionais estão fora de escopo nesta fase; S-03 deixa de ser suposição e passa a fato confirmado.

| Tipo | Caso | Entrada/Cenário | Resultado esperado |
|---|---|---|---|
| CF | Busca simples | origem=BH, destino=SP, data=amanhã | Lista de voos correspondentes retornada |
| CF | Filtro "próximas horas" | origem/destino válidos, filtro "próximas horas" ativado | Apenas voos com partida dentro da janela "próximas horas" (janela não quantificada pela fonte — ver Seção 3) |
| INV | Origem = destino | origem=BH, destino=BH | Rejeitado / sem resultados válidos (regra de bom senso do domínio, não explicitada literalmente na fonte — registrar como lacuna se tratado como regra) |
| INV | Data no passado | data anterior à data atual | Rejeitado ou sem resultados (não há RF explícito sobre data passada — lacuna) |
| LIM | Busca exatamente no limite da janela "amanhã" (23:59 → 00:00) | Busca disparada no limiar da virada do dia | Comportamento de fronteira não definido pela fonte — lacuna |
| INV | Busca por rota internacional | origem ou destino fora do Brasil | **Decisão confirmada pelo stakeholder em 2026-09-17:** fora de escopo — rejeitado ou sem resultados, plataforma cobre apenas rotas domésticas |

#### RN-B02 — Calendário de preços (±15 dias mínimo)
**Origem:** RF-02. Escopo doméstico (Brasil) confirmado pelo stakeholder em 2026-09-17 — ver RN-B01.

| Tipo | Caso | Entrada/Cenário | Resultado esperado |
|---|---|---|---|
| CF | Busca com data central | data=D | Calendário exibe preços de, no mínimo, D-15 a D+15 |
| LIM | Exatamente 15 dias antes/depois | D-15 e D+15 | Ambos os extremos devem estar cobertos ("no mínimo ±15 dias") |
| INV | Datas fora do horizonte operacional da companhia (ex.: rota não opera em certos dias) | Dia sem voos cadastrados dentro da janela | Dia exibido sem preço/indisponível, não omitido silenciosamente (comportamento de exibição não detalhado pela fonte — lacuna) |

#### RN-B03 — Preço final sem custo oculto e congelamento de sessão
**Origem:** RF-03; RNF-01; RNF-14; RF-14; `ADR-07` (**decisão confirmada pelo stakeholder em 2026-09-17** — ver Seção 3.4).
**Enunciado (harmonização por ADR-07 agora adotada):** dentro da mesma sessão de checkout, divergência de preço entre busca e checkout deve ser **0%**; entre sessões diferentes, tarifa dinâmica pode variar, desde que atualizada a cada ≥ 5 minutos e sinalizada antes da confirmação.

| Tipo | Caso | Entrada/Cenário | Resultado esperado |
|---|---|---|---|
| CF | Preço exibido na busca = preço no checkout, mesma sessão | Usuário busca, seleciona voo, avança ao checkout sem intervalo relevante | Preço no checkout idêntico ao exibido na busca (0% divergência) [RNF-01, RNF-14] |
| CONF | Tarifa dinâmica muda entre a busca e o checkout, mesma sessão | Preço do voo muda no backend enquanto a sessão de checkout já está aberta | **Resolvido (ADR-07 adotado):** dentro da mesma sessão o preço permanece congelado — divergência deve ser 0%, independentemente de mudança de tarifa dinâmica no backend; C-02 fechado |
| CF | Tarifa dinâmica muda entre sessões distintas | Sessão A busca a R$X; sessão B (nova), ≥5 min depois, busca o mesmo voo | Preço pode diferir de X, desde que sinalizado ao usuário antes da confirmação |
| LIM | Atualização de tarifa dinâmica exatamente nos 5 minutos | Preço atualizado no instante t=5min | Verificar se a atualização é permitida exatamente na fronteira (≥5min inclui o instante exato) |
| PROIB | Preço final maior no checkout sem sinalização, dentro da mesma sessão | Custo adicional aparece apenas no checkout, sem aviso prévio | Proibido — é a dor explícita de origem (Viajante econômico, Família planejadora) que RF-03/RNF-01 existem para eliminar |
| CF | Composição do preço final na busca | Preço com taxas, bagagem e encargos | Preço exibido na tela de resultados já deve incluir todos esses componentes, sem revelação adicional só no checkout |

#### RN-B04 — Ordenação e filtros de resultados
**Origem:** RF-04.

| Tipo | Caso | Entrada/Cenário | Resultado esperado |
|---|---|---|---|
| CF | Ordenar por menor preço | Lista de resultados, critério = preço | Resultados em ordem crescente de preço |
| CF | Filtrar por política de remarcação flexível | Filtro "remarcação flexível" ativado | Apenas voos com essa característica exibidos |
| LIM | Lista vazia após filtro | Filtro elimina todos os resultados | Estado vazio tratado explicitamente (mensagem, não erro) — comportamento de UI não detalhado na fonte, mas esperado implicitamente |
| INV | Critério de ordenação inexistente | Parâmetro de ordenação inválido enviado (ex.: via API B2B) | Rejeitado com erro de validação, não aplicado como default silencioso |

#### RN-B05 — Indicação de política de remarcação/cancelamento antes da compra
**Origem:** RF-05.

| Tipo | Caso | Entrada/Cenário | Resultado esperado |
|---|---|---|---|
| CF | Tarifa com remarcação sem custo | Voo com essa política | Indicação visível antes da compra |
| CF | Tarifa com multa de remarcação | Voo com multa | Regra de multa exibida antes da compra |
| PROIB | Compra concluída sem exibir a política de remarcação | Checkout avança sem essa informação visível | Proibido — RF-05 exige exibição "antes da compra" |
| **Gap** | Valores concretos de multa/regra por tarifa | — | **Não especificado pela fonte** (lacuna L-04) — plataforma deve exibir o que cada companhia cadastrar, mas o conteúdo da regra em si não é definido aqui |

#### RN-B06 — Alerta de preço por rota/data
**Origem:** RF-06. **Sem componente arquitetural definido — gap identificado em `revisao-painel-atam.md` §6.**

| Tipo | Caso | Entrada/Cenário | Resultado esperado |
|---|---|---|---|
| CF | Configurar alerta para uma rota/data | Usuário define rota+data e limite de queda de preço (ou "qualquer queda") | Alerta registrado |
| CF | Preço cai abaixo do configurado | Preço da rota monitorada cai | Usuário notificado (canal e critério de "queda" não definidos pela fonte — lacuna) |
| INV | Alerta para rota sem voos cadastrados | Rota inexistente no Catálogo | Rejeitado na criação do alerta |
| **Gap** | Canal, frequência de verificação e limiar de "queda de preço" | — | Não definidos pela fonte — registrar como lacuna antes de detalhar testes |

---

### 2.3 Grupo C — Compra e checkout

#### RN-C01 — Checkout rápido com dados salvos
**Origem:** RF-07; RNF-05; RNF-13.

| Tipo | Caso | Entrada/Cenário | Resultado esperado |
|---|---|---|---|
| CF | Checkout com perfil e cartão salvos | Usuário logado com dados de perfil completos | Conclusão em até 120s, em no máximo 4 telas/etapas, no máximo 3 toques/cliques, com no máximo 2 campos obrigatórios adicionais (ex.: CVV, aceite de termos) |
| LIM | Exatamente 4 telas / 3 cliques / 2 campos adicionais | Fluxo no limite máximo permitido | Ainda considerado conforme (limite é "no máximo") |
| INV | Checkout excede 4 telas ou exige mais de 2 campos adicionais | Fluxo com etapa extra não prevista | Não conforme à RNF-13 — deve ser tratado como falha de aceitação, não apenas "aviso" |
| CF | Checkout sem perfil salvo (usuário novo) | Usuário sem dados cadastrados | **Não coberto explicitamente pela métrica de RNF-05/RNF-13**, que pressupõe dados salvos — registrar como lacuna quanto ao SLA para novos usuários |

#### RN-C02 — Retenção/pré-preenchimento de dados cadastrais recorrentes
**Origem:** RF-08.

| Tipo | Caso | Entrada/Cenário | Resultado esperado |
|---|---|---|---|
| CF | Segunda compra do mesmo usuário | Usuário com CPF, endereço, pagamento e fidelidade já cadastrados | Campos pré-preenchidos automaticamente |
| CONF | Dado cadastral desatualizado (ex.: cartão expirado) | Cartão salvo vencido | **Decisão confirmada pelo stakeholder em 2026-09-17** (Seção 3.4): a transação **falha de forma genérica** e o erro é notificado ao usuário — não há fluxo de reautorização dedicado; sistema não usa o dado inválido nem falha silenciosamente |

#### RN-C03 — Múltiplos meios de pagamento, incluindo Pix e parcelamento
**Origem:** RF-09; RNF-24.

| Tipo | Caso | Entrada/Cenário | Resultado esperado |
|---|---|---|---|
| CF | Pagamento via Pix | Valor qualquer | Aceito, mesmo para valores baixos |
| CF | Pagamento parcelado no cartão | Valor alto, parcelamento em várias vezes (ex.: 8x) | Aceito |
| LIM | Parcelamento de valor mínimo permitido | Valor no limite inferior de elegibilidade a parcelamento | **Limite não definido pela fonte** ("mesmo para valores baixos" não quantifica um piso) — lacuna |
| INV | Cartão inválido/recusado | Dados de cartão incorretos ou recusa da operadora | Transação rejeitada; nenhum HOLD confirmado (RN-A02) |
| PROIB | Armazenamento de PAN em texto claro | Qualquer fluxo de pagamento | Proibido de forma absoluta — RNF-24 exige 100% dos números tokenizados |
| CONF | Falha de pagamento após HOLD concedido | HOLD ativo; pagamento recusado | HOLD permanece até expirar (ou é liberado no cancelamento explícito); assento não é confirmado |

#### RN-C04 — Confirmação e localizador enviados automaticamente (e-mail + WhatsApp)
**Origem:** RF-10; RNF-11.

| Tipo | Caso | Entrada/Cenário | Resultado esperado |
|---|---|---|---|
| CF | Pagamento aprovado | Venda confirmada | Confirmação + localizador enviados por e-mail e WhatsApp em ≤ 30s, em ≥ 99% dos casos/mês |
| LIM | Envio exatamente aos 30s | Entrega no limite | Dentro do SLA |
| CONF | Falha de entrega em um dos canais (ex.: WhatsApp indisponível) | Provedor de WhatsApp fora do ar | Comportamento de fallback não definido pela fonte — lacuna (mínimo exigido é ≥99%/mês agregado, não por evento) |
| PROIB | Reserva confirmada sem qualquer notificação enviada | Falha total de notificação sem novo envio/reprocessamento | Contraria RNF-19 (ansiedade do usuário) e a meta de ≥99%/mês; deve haver reprocessamento, não definido em detalhe pela fonte |

#### RN-C05 — Emissão automática de nota fiscal/recibo
**Origem:** RF-11; RNF-12.

| Tipo | Caso | Entrada/Cenário | Resultado esperado |
|---|---|---|---|
| CF | Pagamento aprovado | Venda confirmada | NF individual emitida em ≤ 60s, disponível por e-mail/download |
| LIM | Emissão exatamente aos 60s | — | Dentro do SLA |
| CONF | Falha do emissor fiscal externo (NF-e/SEFAZ) | Indisponibilidade do integrador `[SUPOSIÇÃO — nome do integrador não presente na fonte]` | Comportamento de retry/fallback não definido pela fonte — lacuna |
| PROIB | Necessidade de solicitação manual pelo passageiro | Emissão depende de ação do usuário | Proibido — RF-11 exige emissão automática, "sem necessidade de solicitação manual" |

#### RN-C06 — Compra simultânea de múltiplos passageiros com preenchimento assistido
**Origem:** RF-12; RNF-17.

| Tipo | Caso | Entrada/Cenário | Resultado esperado |
|---|---|---|---|
| CF | Família de 4 passageiros em uma transação | 2 adultos + 2 crianças | Todos incluídos em uma única transação |
| CF | Reuso de dados do 1º para os demais passageiros | Endereço/forma de pagamento do 1º | Redução de ≥ 70% dos campos preenchidos manualmente do 2º ao Nº passageiro |
| LIM | Reuso exatamente em 70% | Medição de campos preenchidos automaticamente | Deve atingir o piso mínimo de 70% |
| INV | Transação com 0 passageiros | Tentativa de checkout sem passageiro selecionado | Rejeitada |
| **Gap (resolvido)** | Número máximo de passageiros por transação | — | **Decisão confirmada pelo stakeholder em 2026-09-17** (Seção 3.4): não há limite de negócio explícito além das restrições de UX; o limite prático é o mapa de assentos da aeronave, não uma regra de negócio à parte |

#### RN-C07 — Seleção de assentos com suporte a grupos/famílias
**Origem:** RF-13.

| Tipo | Caso | Entrada/Cenário | Resultado esperado |
|---|---|---|---|
| CF | Seleção de assentos lado a lado para 4 passageiros | Mapa de assentos com 4+ contíguos livres | Sistema permite selecionar e reservar os 4 juntos (sujeito a RN-A01 no momento do HOLD de cada um) |
| CONF | Assentos contíguos insuficientes para o grupo | Apenas 2 dos 4 assentos contíguos livres | Sistema deve indicar a impossibilidade de sentar todos juntos, não confirmar parcialmente sem aviso (comportamento exato de fallback não detalhado pela fonte — lacuna) |
| CONF | Concorrência: outro usuário reserva um assento do "bloco" durante a seleção do grupo | Assento do meio do bloco reservado por terceiro entre a exibição do mapa e a confirmação | Regra de exclusão mútua (RN-A01/RN-A03) prevalece; grupo deve ser informado da mudança de disponibilidade antes de confirmar |

#### RN-C08 — Transparência da tarifa e limite de upsell
**Origem:** RF-14; RNF-15.

| Tipo | Caso | Entrada/Cenário | Resultado esperado |
|---|---|---|---|
| CF | Exibição do que está incluso (bagagem de mão, despachada) | Tela de checkout | Informação clara antes da confirmação |
| LIM | Exatamente 3 ofertas de upsell exibidas | Checkout com 3 add-ons disponíveis | Conforme (limite máximo permitido) |
| INV | Mais de 3 ofertas de upsell exibidas por padrão | Checkout com 4+ add-ons configurados pela companhia | Não conforme — deve limitar a 3 por padrão |
| PROIB | Upsell pré-marcado/pré-selecionado | Qualquer oferta de add-on vindo marcada por padrão | Proibido — exige opt-in explícito |
| CF | Ocultar todas as ofertas em 1 clique | Usuário aciona "ocultar todas" | Todas as ofertas de upsell removidas da tela imediatamente |

#### RN-C09 — Documentação exigida para menores de idade
**Origem:** RF-15; RNF-25.

| Tipo | Caso | Entrada/Cenário | Resultado esperado |
|---|---|---|---|
| CF | Passageiro menor de idade incluído na compra | Criança de 5 anos na reserva | Regras de documentação exibidas durante o fluxo |
| CONF | Dados de menor coletados sem consentimento apropriado do responsável | Cadastro de dados de criança | Deve respeitar LGPD (RNF-25) — mecanismo de consentimento não detalhado pela fonte, lacuna |
| **Gap** | Documentos concretos exigidos por idade/rota | — | Não especificado pela fonte (lacuna L-04) — depende de regra de cada companhia/ANAC (RSK-10) |

---

### 2.4 Grupo D — Pós-venda, suporte e alterações

#### RN-D01 — Canal de suporte 24h
**Origem:** RF-16; RNF-21.

| Tipo | Caso | Entrada/Cenário | Resultado esperado |
|---|---|---|---|
| CF | Usuário aciona chat às 3h da manhã | Qualquer horário | Canal disponível 24h/7 dias/365 dias |
| LIM | Tempo de primeira resposta no limite (2 min) | Chat acionado | Resposta em ≤ 2 min `[baseline sugerida]` |
| PROIB | Canal indisponível durante qualquer janela do dia | — | Proibido pela meta de disponibilidade 24/7/365 |

#### RN-D02 — Remarcação e cancelamento self-service
**Origem:** RF-17.

| Tipo | Caso | Entrada/Cenário | Resultado esperado |
|---|---|---|---|
| CF | Remarcação sem custo dentro da política da tarifa | Tarifa flexível | Alteração concluída, sem cobrança |
| CF | Cancelamento com multa | Tarifa restritiva | Custo exibido e confirmado antes da efetivação |
| PROIB | Alteração efetivada sem exibir custo/prazo antes da confirmação | Fluxo de remarcação pula a etapa de exibição de custo | Proibido — RF-17 exige exibição "antes da confirmação da alteração" |
| CONF | Remarcação para voo sem assentos disponíveis na nova data | Tentativa de remarcar para voo lotado | Bloqueado pela mesma regra de exclusividade de assento (RN-A01) |

#### RN-D03 — Notificação automática de alteração/cancelamento de voo pela companhia
**Origem:** RF-18.

| Tipo | Caso | Entrada/Cenário | Resultado esperado |
|---|---|---|---|
| CF | Companhia cancela um voo | Evento de cancelamento recebido do Adaptador de Integração | Passageiro notificado automaticamente; se a reserva foi feita via agente B2B, o agente também é notificado |
| CONF | Voo alterado (não cancelado) — horário muda | Evento de alteração de horário | Notificação também deve ocorrer (RF-18 cobre "alteração ou cancelamento") |
| **Gap/Conflito de atribuição (resolvido)** | Origem declarada do requisito | — | **Decisão confirmada pelo stakeholder em 2026-09-17** (Seção 3.4): mantém-se a atribuição conforme o texto do requisito (§3.3 para RF-18 — Agente de viagens corporativas; §3.4/§3.5 para RF-30/RF-31, conforme suas seções declaradas), não a tabela de rastreabilidade (§5). "Quem" é notificado segue o texto original: se a reserva foi feita via agente B2B, o agente é o responsável notificado, além do passageiro |

---

### 2.5 Grupo E — B2B (agências de viagens corporativas)

#### RN-E01 — Painel de agente operando em nome de terceiros
**Origem:** RF-19; RNF-27.

| Tipo | Caso | Entrada/Cenário | Resultado esperado |
|---|---|---|---|
| CF | Agente seleciona empresa cliente e reserva em nome de um funcionário | Contexto de empresa ativo | Reserva associada à empresa e ao passageiro corretos |
| PROIB | Agente acessa dados/reservas de empresa cliente diferente da selecionada | Tentativa de acesso cross-tenant | Bloqueado — RNF-27 exige 0% de vazamento cross-tenant |

#### RN-E02 — Motor de políticas de viagem por empresa cliente
**Origem:** RF-20; RNF-02; RNF-30; `ADR-08 [proposta — rejeitada pelo stakeholder]`.

| Tipo | Caso | Entrada/Cenário | Resultado esperado |
|---|---|---|---|
| CF | Busca dentro da política (classe econômica, teto R$1.200, companhia credenciada) | Parâmetros de busca compatíveis com a política da empresa | Sugestão automática de voos dentro da política |
| PROIB | Reserva fora da política (ex.: classe executiva quando só econômica é permitida) | Tentativa de reserva fora da política | Bloqueada ou sinalizada antes da confirmação |
| CONF (resolvido) | **Contradição textual da própria fonte (RNF-02):** "bloquear 100% das reservas fora de política" vs. "taxa de falso-negativo ≤ 0,1%/mês" | Qualquer teste de aceite sobre a taxa de bloqueio | **Decisão confirmada pelo stakeholder em 2026-09-17** (Seção 3.4): **100% é o critério de aceite rígido e vinculante** — a proposta do `ADR-08` (tratar ≤0,1%/mês como métrica operacional de aceite) foi **rejeitada**. Qualquer reserva fora de política que não seja bloqueada é tratada como **defeito crítico/incidente**, não como falha tolerável. O valor ≤0,1%/mês passa a valer apenas como **limiar interno de alerta/monitoramento** para investigação, nunca como banda de tolerância em testes de aceite. RNF-02 continua textualmente contraditória na fonte — este plano registra o critério de aceite adotado pelo time sem alterar o texto do requisito, que segue exigindo correção por quem tem autoridade sobre ele |
| CF | Alteração de política sem deploy | Config atualizada pelo agente/empresa | Reflete em produção em ≤ 5 minutos, sem deploy de código |
| LIM | Alteração refletida exatamente aos 5 minutos | — | Dentro do SLA |

#### RN-E03 — Emissão em lote a partir de planilha
**Origem:** RF-21; RNF-08. *(Casos de exclusividade de assento já cobertos em RN-A06; aqui apenas os aspectos de ingestão/planilha.)*

| Tipo | Caso | Entrada/Cenário | Resultado esperado |
|---|---|---|---|
| CF | Planilha com 8 passageiros válidos | Cenário da persona (convenção em Salvador) | Todos processados, sugestão automática de voos dentro da política (RN-E02) |
| INV | Planilha com linha de dados incompletos (ex.: CPF ausente) | Linha malformada | Aquela linha rejeitada/reportada individualmente; não deve interromper o lote inteiro nem ser processada com dado ausente |
| INV | Planilha em formato não suportado | Arquivo corrompido ou tipo incorreto | Upload rejeitado com mensagem de erro, nenhum processamento parcial |
| CONF | Passageiro da planilha já possui reserva conflitante ativa (mesmo voo, tentativa duplicada) | Duplicidade na própria planilha | Comportamento de deduplicação não definido pela fonte — lacuna |

#### RN-E04 — API pública para integração B2B
**Origem:** RF-22; RNF-09; RNF-28; RNF-33.

| Tipo | Caso | Entrada/Cenário | Resultado esperado |
|---|---|---|---|
| CF | Chamada autenticada via OAuth2 | Token válido, não expirado | Requisição processada; latência ≤ 500ms (P95) |
| LIM | Token no limite de expiração (1h) | Chamada no instante exato de expiração | Comportamento de fronteira (aceitar/rejeitar no limite) não detalhado — lacuna menor |
| INV | Token expirado ou inválido | Chamada com token vencido | Requisição rejeitada (401/erro de autenticação) |
| PROIB | Chamada sem autenticação | Requisição sem token | Bloqueada — RNF-28 exige OAuth2 obrigatório |
| CONF | Chamada a uma versão de API descontinuada dentro da janela de retrocompatibilidade | Cliente usando `/v1/` enquanto `/v2/` já existe, há 11 meses da publicação de `/v2/` | Deve continuar funcionando — retrocompatibilidade exigida por ≥ 12 meses (RNF-33) |
| INV | Chamada a versão de API fora da janela de retrocompatibilidade (>12 meses) | Cliente usando versão descontinuada há 13 meses | Comportamento não definido pela fonte após o fim da janela — lacuna |

#### RN-E05 — Faturamento consolidado por empresa cliente
**Origem:** RF-23; RNF-12.

| Tipo | Caso | Entrada/Cenário | Resultado esperado |
|---|---|---|---|
| CF | Múltiplas reservas de uma empresa no período | N reservas confirmadas no mês | Uma única fatura consolidada gerada até o 1º dia útil do mês seguinte |
| LIM | Fechamento exatamente no 1º dia útil | — | Dentro do prazo |
| CONF | Reserva confirmada no último instante do período de faturamento | Venda confirmada nos últimos minutos do mês | Deve ser corretamente atribuída ao período correspondente, sem duplicidade nem omissão (liga-se à idempotência de RN-G02) |
| PROIB | Fatura fragmentada por passagem para cliente B2B | Uma fatura por reserva individual | Proibido — RF-23 exige fatura única por empresa |

---

### 2.6 Grupo F — Companhias aéreas parceiras

#### RN-F01 — Cadastro de rotas, horários, classes e tarifas
**Origem:** RF-24; RNF-31; RNF-16.

| Tipo | Caso | Entrada/Cenário | Resultado esperado |
|---|---|---|---|
| CF | Gestor comercial cadastra nova tarifa | Dados completos e válidos | Publicação em vigor em ≤ 1 minuto, sem intervenção técnica |
| LIM | Cadastro concluído em exatamente 5 minutos (usuário não técnico) | Fluxo de cadastro completo | Dentro do critério de usabilidade `[baseline sugerida]` |
| INV | Cadastro com campos obrigatórios ausentes (ex.: rota sem tarifa) | Formulário incompleto | Rejeitado com indicação clara do campo faltante |
| CONF | Duas alterações de tarifa da mesma rota quase simultâneas pelo mesmo gestor (duas abas) | Edição concorrente | Comportamento de concorrência não definido pela fonte — lacuna (last-write-wins vs. bloqueio otimista) |

#### RN-F02 — Campanhas promocionais com quantidade de assentos promocionais
**Origem:** RF-25; `ADR-06 [proposta, não decisão]`.

| Tipo | Caso | Entrada/Cenário | Resultado esperado |
|---|---|---|---|
| CF | Criação de campanha com rota, vigência e quantidade de assentos promocionais | Dados válidos | Campanha criada; quantidade de assentos promocionais refletida na disponibilidade real (RESTRIÇÃO-CRÍTICA-01 — a contagem promocional não pode divergir do estoque real) |
| LIM | Quantidade de assentos promocionais = 0 | Campanha criada sem assentos | Deve ser rejeitada ou tratada como campanha inativa (não definido explicitamente — lacuna menor) |
| CONF | Assentos promocionais vendidos excedem a quantidade cadastrada | Demanda maior que o lote promocional | Bloqueado pela mesma exclusividade de estoque (RN-A01) — depende de `[ADR-06]` sobre onde a contagem promocional é a fonte de verdade (Estoque vs. Catálogo); sem adoção formal, o ponto exato de bloqueio é indefinido |
| PROIB | Venda de assento fora da campanha sendo contabilizada como promocional (ou vice-versa) | Divergência entre contagem de campanha e estoque real | Proibido — decorre diretamente de RESTRIÇÃO-CRÍTICA-01 |

#### RN-F03 — Sincronização de estoque em tempo real (prevenção de overbooking)
**Origem:** RF-26; RNF-06; RNF-10; RNF-22. *(Casos centrais já em RN-A05; aqui, aspectos específicos de RTO/RPO.)*

| Tipo | Caso | Entrada/Cenário | Resultado esperado |
|---|---|---|---|
| CONF | Falha de sincronização por período prolongado | Adaptador de Integração fora do ar | RTO ≤ 5 min, RPO ≤ 1 min `[baseline sugerida]` — recuperação dentro desses limites |
| PROIB | Perda de dados de estoque acima do RPO definido | Falha que causa perda de eventos de venda não reconciliados | Proibido além do limite de RPO |

#### RN-F04 — Dashboard de desempenho comercial
**Origem:** RF-27; RNF-07.

| Tipo | Caso | Entrada/Cenário | Resultado esperado |
|---|---|---|---|
| CF | Consulta de até 90 dias | Filtro de período dentro do limite | Sem timeout, carregamento ≤ 4s `[baseline sugerida]` |
| LIM | Consulta de exatamente 12 meses de histórico | Período no limite máximo suportado | Carregamento ≤ 4s |
| INV | Consulta de período superior a 12 meses | Filtro além do limite suportado | Comportamento não definido pela fonte — lacuna |

#### RN-F05 — Posicionamento competitivo (ranking)
**Origem:** RF-28.

| Tipo | Caso | Entrada/Cenário | Resultado esperado |
|---|---|---|---|
| CF | Gestor consulta ranking de exibição/preço para sua rota | Rota com concorrência cadastrada | Ranking exibido |
| **Gap** | Metodologia de cálculo do ranking | — | Não definida pela fonte — nenhum RF/RNF especifica algoritmo ou critério de ranking (gap identificado também em `revisao-painel-atam.md` §6 quanto a componente de dados/analytics) |

---

### 2.7 Grupo G — Operação interna / administração da plataforma

#### RN-G01 — Onboarding padronizado de companhias parceiras
**Origem:** RF-29; RNF-29; `ADR-05 [proposta, não decisão]`.

| Tipo | Caso | Entrada/Cenário | Resultado esperado |
|---|---|---|---|
| CF | Nova companhia completa toda a documentação e testes técnicos | Fluxo padronizado seguido integralmente | Onboarding concluído em ≤ 10 dias úteis `[baseline sugerida]` |
| CONF | Companhia não atinge o "nível de capacidade" técnico mínimo (sincronização ≤2s) | Parceiro com sincronização apenas em lote/polling | `[ADR-05]` propõe segmentação por nível declarado (afeta TTL de hold e política de fail-closed) — sem adoção formal, não há critério definido de aceite/rejeição de parceiro por capacidade técnica (lacuna) |

#### RN-G02 — Painel único de indicadores de saúde da operação
**Origem:** RF-30.

| Tipo | Caso | Entrada/Cenário | Resultado esperado |
|---|---|---|---|
| CF | Consulta consolidada de fraude, disputas e SLA por companhia | Painel administrativo interno | Indicadores exibidos de forma unificada |
| PROIB | Vazamento de dado de uma companhia para o painel/contexto de outra | Consulta agregada mal escopada | Proibido (RNF-27); `[ADR-04]` nota que esse tipo específico de vazamento por bug de agregação **não é coberto** pelo RBAC de acesso — permanece risco residual mesmo com controles de autenticação |

#### RN-G03 — Alertas automáticos de padrões anômalos (fraude)
**Origem:** RF-31; RNF-26.
**Regra quantitativa definida na fonte:** anomalia = volume de reembolsos ≥ 3× a média móvel dos últimos 7 dias, para a mesma rota/companhia.

| Tipo | Caso | Entrada/Cenário | Resultado esperado |
|---|---|---|---|
| CF | Reembolsos da rota/companhia atingem 3× a média móvel de 7 dias | Volume de reembolsos = 3× média | Alerta disparado em ≤ 15 minutos após detecção |
| LIM | Volume exatamente igual a 3× a média (fronteira) | Volume = exatamente o limiar | Deve disparar (≥ 3×, inclui o limite) |
| INV | Volume ligeiramente abaixo do limiar (ex.: 2,9×) | Volume < 3× | Não deve disparar alerta de anomalia |
| LIM | Alerta dentro do limite de 15 minutos | Disparo aos 15min exatos | Dentro do SLA |
| **Gap** | Janela/frequência de recálculo da média móvel | — | Não especificada pela fonte (ex.: recalculada a cada hora? em tempo real?) — lacuna |

#### RN-G04 — Conciliação financeira idempotente
**Origem:** RF-32; RNF-23.

| Tipo | Caso | Entrada/Cenário | Resultado esperado |
|---|---|---|---|
| CF | Conciliação de um período fechado | Eventos de venda e repasses do período | Relatório consolidado gerado, sem duplicidade |
| CONF | Reprocessamento do mesmo período (ex.: reexecução do job) | Job de conciliação executado duas vezes para o mesmo período | 0% de lançamentos duplicados ou perdidos — idempotência via chave de evento |
| PROIB | Lançamento duplicado após reprocessamento | Reexecução gera segundo lançamento para o mesmo evento | Proibido — viola RNF-23 |
| CF | Trilha de auditoria | Qualquer lançamento de conciliação | Registrado de forma imutável, retido por ≥ 5 anos |
| CONF | Auditoria de conciliação vs. exclusão LGPD de um titular envolvido | Titular pede exclusão de dados dentro dos 5 anos de retenção obrigatória | Ver RN-H02 — **decisão do stakeholder (2026-09-17)** determina exclusão total, inclusive da trilha financeira, o que colide diretamente com a retenção ≥5 anos exigida aqui por RNF-23; risco a ser formalmente escalado, não silenciosamente absorvido pelos testes |

#### RN-G05 — Mediação de disputas de reembolso com SLA
**Origem:** RF-33. **SLA contratual baseline confirmado pelo stakeholder em 2026-09-17** (Seção 3.4): **15 dias corridos** (lacuna L-03 resolvida com baseline; sujeito a validação posterior com jurídico/contratos).

| Tipo | Caso | Entrada/Cenário | Resultado esperado |
|---|---|---|---|
| CF | Abertura de disputa entre passageiro e companhia | Disputa registrada | Fluxo de mediação/registro iniciado; resolução dentro do SLA de 15 dias corridos |
| LIM | Mediação concluída exatamente no 15º dia corrido | Disputa resolvida no limite | Dentro do SLA |
| INV | Disputa não resolvida após 15 dias corridos | SLA estourado | Deve ser sinalizada como violação de SLA (baseline provisório — comportamento de escalonamento após estouro não detalhado pela fonte, permanece lacuna menor) |

---

### 2.8 Grupo H — Segurança e conformidade (transversal)

#### RN-H01 — Tokenização PCI-DSS de dados de pagamento
**Origem:** RNF-24. *(Casos de pagamento específicos já em RN-C03; aqui, a regra de dados.)*

| Tipo | Caso | Entrada/Cenário | Resultado esperado |
|---|---|---|---|
| CF | Cartão salvo para compras futuras | Qualquer cadastro de cartão | Armazenado como token, nunca como PAN em texto claro |
| PROIB | Qualquer registro de PAN em texto claro em log, banco ou trilha de auditoria | Inspeção de qualquer armazenamento persistente | Proibido de forma absoluta (100% tokenizado) |

#### RN-H02 — Exclusão de dados pessoais (LGPD) vs. retenção obrigatória de auditoria/fiscal
**Origem:** RNF-25 (exclusão ≤15 dias) vs. RNF-23/RNF-28 (retenção ≥5 anos); `ADR-09 [proposta — rejeitada pelo stakeholder]`.

| Tipo | Caso | Entrada/Cenário | Resultado esperado |
|---|---|---|---|
| CF | Titular sem transações pendentes de auditoria/fiscal solicita exclusão | Dado pessoal sem vínculo com obrigação legal ativa | Excluído em ≤ 15 dias corridos |
| CONF (resolvido — ⚠️ divergência com o painel de arquitetura) | Titular com transação sujeita a retenção de auditoria (≥5 anos) solicita exclusão dentro desse prazo | Passageiro com reserva confirmada há 1 ano pede exclusão de dados | **Decisão confirmada pelo stakeholder em 2026-09-17** (Seção 3.4): **exclusão prevalece — apaga-se tudo, inclusive a trilha financeira/de auditoria**, em ≤ 15 dias corridos. Isto **rejeita explicitamente** a proposta de pseudonimização do `ADR-09` e escolhe a alternativa que o próprio painel ATAM havia classificado como "não recomendada — viola RNF-23 e possivelmente obrigação fiscal" (ver `docs/revisao-painel-atam.md` §4/ADR-09). **Esta é uma decisão de negócio que cria conflito direto e não mitigado com RNF-23/RF-32 (retenção ≥5 anos, conciliação financeira/auditoria) — deve ser formalmente escalada a quem detém autoridade sobre RNF-23 antes de virar critério de aceite definitivo em produção.** Para efeito deste plano de testes, o critério de aceite é: exclusão completa, sem exceção para dados financeiros, dentro de 15 dias |
| CF | Dado de menor de idade (RF-15) sujeito a exclusão | Responsável solicita exclusão de dados da criança | Mesmo tratamento de RNF-25, com atenção adicional por se tratar de menor (não há regra diferenciada explícita na fonte além da exigência geral de LGPD) |

#### RN-H03 — RBAC e isolamento cross-tenant
**Origem:** RNF-27; `ADR-04 [proposta, não decisão]`.

| Tipo | Caso | Entrada/Cenário | Resultado esperado |
|---|---|---|---|
| CF | Usuário com papel definido acessa apenas recursos do seu escopo | Companhia A consulta seus próprios dados | Acesso concedido |
| PROIB | Companhia A acessa dados de Companhia B (ou empresa cliente X acessa dados de empresa cliente Y) | Tentativa de acesso cross-tenant | Bloqueado — 0% de vazamento é a meta (RNF-27) |
| CONF | Auditoria trimestral não detecta vazamento ocorrido entre duas janelas de auditoria | Vazamento ocorre e é corrigido antes da próxima auditoria trimestral | Pela fonte-base (RNF-27 literal), o único controle exigido é a auditoria trimestral — detecção contínua é apenas uma proposta (`[ADR-04]`), não uma regra adotada; teste de "detecção em tempo real" não pode ser escrito como requisito de aceite hoje |

#### RN-H04 — Autenticação OAuth2 e expiração de tokens para APIs
**Origem:** RNF-28. *(Casos específicos de API já cobertos em RN-E04.)*

| Tipo | Caso | Entrada/Cenário | Resultado esperado |
|---|---|---|---|
| CF | Log de auditoria de chamada de API | Qualquer chamada autenticada | Registrada e retida por ≥ 5 anos |
| PROIB | Token de acesso válido por mais de 1 hora | Qualquer token emitido | Proibido — expiração máxima de 1h |

---

### 2.9 Grupo I — Disponibilidade, degradação e resiliência

#### RN-I01 — Disponibilidade de busca/checkout
**Origem:** RNF-20.

| Tipo | Caso | Entrada/Cenário | Resultado esperado |
|---|---|---|---|
| CF | Operação normal ao longo do mês | — | Disponibilidade ≥ 99,9%/mês (≤ 43 min de indisponibilidade/mês) |
| LIM | Indisponibilidade acumulada de exatamente 43 minutos no mês | — | No limite aceitável; qualquer minuto além viola a meta |

#### RN-I02 — Comportamento fail-closed no ponto de decisão de posse
**Origem:** `arquitetura.md` §0, §12; `ADR-02 [proposta — rejeitada pelo stakeholder]`.

| Tipo | Caso | Entrada/Cenário | Resultado esperado |
|---|---|---|---|
| PROIB | Confirmar posse de assento com o Serviço de Estoque indisponível | Estoque fora do ar no momento do `HOLD`/`CONFIRM` | Deve falhar de forma fechada (fail-closed) — nunca conceder posse sem confirmação positiva do Estoque, para não violar RESTRIÇÃO-CRÍTICA-01 |
| PROIB (resolvido) | Busca durante indisponibilidade do Estoque | Estoque fora do ar, mesmo apenas para consulta de disponibilidade agregada | **Decisão confirmada pelo stakeholder em 2026-09-17** (Seção 3.4): fail-closed **total** — a proposta do `ADR-02` (busca continuar servindo último estado conhecido, com aviso de defasagem) foi **rejeitada**. Busca e confirmação **ambas** devem recusar/falhar enquanto o Estoque estiver indisponível; não há modo de busca com dado potencialmente obsoleto |

---

### 2.10 Grupo J — Usabilidade e portabilidade (transversal aos canais)

#### RN-J01 — Responsividade e desempenho de carregamento
**Origem:** RNF-18.

| Tipo | Caso | Entrada/Cenário | Resultado esperado |
|---|---|---|---|
| CF | Renderização em largura de 360px (menor smartphone) | Viewport mínimo | Interface funcional, LCP ≤ 2,5s em 4G simulado |
| LIM | Renderização em 1920px (limite superior) | Viewport máximo | Interface funcional |
| INV | Viewport fora da faixa suportada (ex.: 320px) | Abaixo do mínimo | Comportamento não garantido pela fonte — lacuna |

#### RN-J02 — Compatibilidade de plataforma (SO/navegadores)
**Origem:** RNF-32; RNF-33.

| Tipo | Caso | Entrada/Cenário | Resultado esperado |
|---|---|---|---|
| CF | Uso nas 2 versões mais recentes de iOS/Android/Chrome/Safari/Edge | — | Suporte completo |
| INV | Uso em versão anterior às 2 últimas majors | SO/navegador desatualizado | Suporte não garantido pela fonte (fora de escopo declarado) |

---

## 3. Ambiguidades, lacunas e perguntas

> Consolidado a partir de `arquitetura.md` §8 e `docs/revisao-painel-atam.md` §4–§8, mapeado às RN correspondentes deste plano. Nada aqui foi resolvido por conta própria — cada item bloqueia ou condiciona os casos de teste indicados.

### 3.1 Conflitos de requisitos (bloqueiam definição de critério de aceite)

| ID | Conflito | RN afetada(s) | Situação |
|---|---|---|---|
| C-02 | RNF-01 (0% com exceção de tarifa dinâmica ≥5min) vs. RNF-14 (0% sem ressalva) | RN-B03 | **Resolvido em 2026-09-17** — `ADR-07` adotado (congelamento de preço por sessão); ver Seção 3.4, item 2 |
| C-08 (A-RSK-13) | RNF-02: "bloquear 100% das reservas fora de política" é logicamente incompatível com "falso-negativo ≤ 0,1%/mês" | RN-E02 | **Resolvido em 2026-09-17 quanto ao critério de aceite do time** (100% rígido; ≤0,1%/mês apenas como alerta) — a contradição textual em si permanece na fonte e ainda precisa de correção por quem tem autoridade sobre RNF-02; ver Seção 3.4, item 3 |
| C-01 | Divergência entre a origem declarada de RF-18/RF-30/RF-31 no corpo do requisito e seu agrupamento na tabela de rastreabilidade (§5 de REQUISITOS) | RN-D03 | **Resolvido em 2026-09-17** — mantém-se a atribuição do texto do requisito; ver Seção 3.4, item 4 |
| A-RSK-10 | RNF-25 (exclusão ≤15 dias) vs. RNF-23/RNF-28 (retenção ≥5 anos) | RN-H02 | **Resolvido em 2026-09-17, mas com divergência em aberto** — stakeholder optou por exclusão total (rejeitando a pseudonimização do `ADR-09`), o que **cria/mantém conflito não mitigado com RNF-23/RF-32** a ser escalado; ver Seção 3.4, item 7 |
| A-RSK-02/DIV-03 | Ausência de modo degradado definido para busca quando o Estoque está indisponível | RN-I02 | **Resolvido em 2026-09-17** — fail-closed total adotado (proposta de fail-open do `ADR-02` rejeitada); ver Seção 3.4, item 6 |
| A-RSK-03 | RNF-10 não define o que ocorre após esgotar as 3 tentativas de webhook | RN-A05 | **Resolvido em 2026-09-17** — fail-closed total adotado (proposta do `ADR-03` de suspender apenas `CONFIRM` foi superada pela decisão de fail-closed total); ver Seção 3.4, item 6 |
| DIV-02 | Onde materializar "quantidade de assentos promocionais" (RF-25): Catálogo ou Estoque | RN-F02 | **Ainda em aberto** — `ADR-06` propõe manter no Estoque; painel de Dados/ML mantém reserva; não fez parte da rodada de perguntas confirmadas |

### 3.2 Lacunas (dados/parâmetros ausentes na fonte)

| ID | Lacuna | RN afetada(s) | Situação |
|---|---|---|---|
| L-07 | TTL do HOLD de assento não definido | RN-A02, RN-A04, RN-C01 | **Resolvida em 2026-09-17** — TTL diferenciado por meio de pagamento (cartão 2–5 min; Pix 10–15 min), `ADR-01` adotado. O TTL mínimo de cartão (120s) é compatível com o SLA de checkout de RN-C01 (conclusão em até 120s) |
| L-02 | Volumes reais (usuários, parceiros, transações/dia) para dimensionar metas de carga | RN-B01 (capacidade de busca), RN-A05, RN-F04 | Ainda em aberto — não fez parte da rodada de perguntas confirmadas |
| L-03 | Valor concreto do SLA contratual de disputas de reembolso | RN-G05 | **Resolvida com baseline em 2026-09-17** — 15 dias corridos, sujeito a validação futura com jurídico/contratos |
| L-04 | Regras concretas de multa de remarcação/cancelamento, meia-passagem/criança, bagagem | RN-B05, RN-C09 | Ainda em aberto |
| L-05 | Requisitos regulatórios ANAC não detalhados | RN-C09 e regras de documentação em geral | Ainda em aberto |
| — | Janela exata de "próximas horas" (RF-01) | RN-B01 | Ainda em aberto |
| — | Piso de "valores baixos" elegíveis a Pix/parcelamento (RF-09) | RN-C03 | Ainda em aberto |
| — | Limite máximo de passageiros por transação (RF-12) | RN-C06 | **Resolvida em 2026-09-17** — sem limite de negócio explícito além da UX/mapa de assentos |
| — | Comportamento de lote acima de 50 passageiros (RF-21/RNF-08) | RN-A06 | **Resolvida em 2026-09-17** — rejeitado na ingestão, sem processamento parcial |
| — | Canal, frequência e limiar de "queda de preço" para alertas (RF-06) | RN-B06 | Ainda em aberto |
| — | Metodologia de cálculo do ranking competitivo (RF-28) | RN-F05 | Ainda em aberto |
| — | Frequência de recálculo da média móvel de reembolsos (RNF-26) | RN-G03 | Ainda em aberto |
| — | Comportamento após expirar a janela de retrocompatibilidade de API de 12 meses (RNF-33) | RN-E04 | Ainda em aberto |

### 3.3 Suposições registradas na fonte (não são fatos, exigem validação)

- **S-01 / `[baseline sugerida]`** — RNF-04, RNF-07, RNF-08, RNF-16, RNF-21, RNF-22, RNF-29: valores numéricos ainda não validados com stakeholders. Testes de desempenho/carga sobre essas métricas devem ser tratados como **provisórios** até confirmação.
- **S-02** — Termos como "PSP/adquirente", "emissor fiscal (NF-e/SEFAZ)", "provedor de e-mail/WhatsApp" são inferências de contexto, não nomes de integradores definidos na fonte.
- ~~**S-03** — Rotas internacionais fora de escopo.~~ **Confirmado como fato em 2026-09-17** (deixa de ser suposição): plataforma cobre apenas rotas domésticas (Brasil); ver Seção 3.4, item 8 e RN-B01/RN-B02.
- **S-04** — A necessidade de "reserva antes do pagamento" é inferida (objetivo do Viajante de última hora + RNF-03), não uma regra explicitamente redigida como tal.

### 3.4 Decisões confirmadas pelos stakeholders em 2026-09-17

> Elicitadas via perguntas estruturadas dirigidas ao stakeholder/product owner do exercício, em resposta às 10 perguntas abertas originalmente registradas nesta seção. Cada item abaixo agora é **critério de aceite oficial deste plano de testes**, aplicado nas RN indicadas. Nenhuma dessas decisões corrige o texto da fonte (REQUISITOS/arquitetura) em si — elas fixam a interpretação/critério de aceite que a equipe de qualidade deve usar até que a fonte seja formalmente atualizada por quem tem autoridade sobre ela.

1. **TTL do HOLD de assento** — TTL diferenciado por meio de pagamento (`ADR-01` adotado): **cartão de crédito/débito 2–5 min; Pix 10–15 min**. Aplicado em RN-A02, RN-A04, RN-C01 (compatibilidade com SLA de checkout). Lacuna L-07 fechada.
2. **Harmonização RNF-01 × RNF-14** — `ADR-07` adotado: congelamento de preço por sessão (0% de divergência dentro da sessão; tarifa dinâmica pode variar entre sessões, a cada ≥5min, sinalizada antes da confirmação). Aplicado em RN-B03. Conflito C-02 fechado.
3. **RNF-02 (100% vs. ≤0,1%)** — Critério de aceite do time: **100% tratado como meta rígida e vinculante**; toda reserva fora de política não bloqueada é defeito crítico/incidente. ≤0,1%/mês passa a ser apenas limiar de alerta/monitoramento, não banda de tolerância de aceite. `ADR-08` (que propunha o inverso) foi rejeitado. Aplicado em RN-E02. A contradição textual de RNF-02 na fonte **permanece e deve ser escalada** para correção formal.
4. **Atribuição RF-18/RF-30/RF-31** — Mantida conforme o texto do requisito (§3.3/§3.4/§3.5), não a tabela de rastreabilidade (§5). Aplicado em RN-D03. Conflito C-01 fechado.
5. **SLA de disputas de reembolso (RF-33/RNF-22)** — Baseline definido agora: **15 dias corridos**, marcado como valor de stakeholder a validar futuramente com jurídico/contratos. Aplicado em RN-G05. Lacuna L-03 fechada com baseline.
6. **Modo degradado do Estoque (ADR-02) e suspensão de CONFIRM (ADR-03)** — Ambas as propostas de degradação parcial foram **substituídas por fail-closed total**: busca e confirmação recusam quando o Serviço de Estoque está indisponível ou quando a sincronização de uma companhia é desconhecida após esgotar as tentativas de webhook. Aplicado em RN-I02 e RN-A05. Conflitos A-RSK-02/DIV-03 e A-RSK-03 fechados.
7. **LGPD vs. auditoria/retenção fiscal (ADR-09)** — **Exclusão prevalece: apaga-se tudo, inclusive a trilha financeira**, em ≤15 dias corridos. Rejeita explicitamente a pseudonimização proposta pelo `ADR-09` — e escolhe a alternativa que o próprio painel ATAM havia marcado como "não recomendada — viola RNF-23 e possivelmente obrigação fiscal". Aplicado em RN-H02 (e nota em RN-G04). **⚠️ Isto cria um conflito novo e não mitigado com RNF-23/RF-32 (retenção ≥5 anos), que deve ser formalmente escalado a quem detém autoridade sobre RNF-23** antes de ser levado a produção — não deve ser tratado como resolução silenciosa.
8. **Escopo internacional** — Confirmado **fora de escopo**: apenas rotas domésticas (Brasil). Aplicado em RN-B01/RN-B02. Suposição S-03 convertida em fato confirmado (ver Seção 3.3).
9. **Limite de passageiros** — Transação online: sem limite explícito de negócio além da UX/mapa de assentos (RN-C06). Lote B2B: **rejeitar uploads acima de 50 passageiros na ingestão**, sem processamento parcial (RN-A06).
10. **Cartão salvo expirado** — Sem fluxo de reautorização dedicado: a transação **falha de forma genérica**, com notificação do erro ao usuário. Aplicado em RN-C02 (remove a exigência de reautorização explícita anteriormente registrada como PROIB).

---

## 4a. Matriz de execução TDD (fase Red)

> Preenchida incrementalmente, grupo por grupo, à medida que os testes JUnit 5 são escritos e executados (`mvn test`). Projeto Maven mínimo criado em `pom.xml` (Java 21, JUnit 5.10.2), sem nenhum código de produção com lógica de negócio — apenas assinaturas mínimas para compilar (`src/main/java/br/com/senac/linhasaereas/**`). Status possíveis: **RED** (teste escrito, roda, falha pelo motivo esperado — funcionalidade ausente); **SKIPPED** (caso bloqueado por conflito/lacuna/ADR não adotado — `@Disabled` com referência à Seção 3, conforme metodologia da Seção 4); **PENDENTE** (ainda não escrito nesta etapa).

### Grupo A — Exclusividade de assento e prevenção de overbooking

| RN | Caso (tipo) | Teste (classe#método) | Status |
|---|---|---|---|
| RN-A01 | CF Reserva de assento livre | `RnA01EstadoAtivoUnicoPorAssentoTest#rnA01_cf_reservaDeAssentoLivreConcedeHoldUnico` | RED — `UnsupportedOperationException` (HOLD não implementado) |
| RN-A01 | CF Confirmação após pagamento aprovado | `RnA01EstadoAtivoUnicoPorAssentoTest#rnA01_cf_confirmacaoAposPagamentoAprovadoTransicionaHoldParaConfirmed` | RED |
| RN-A01 | CONF Duas solicitações de HOLD simultâneas | `RnA01EstadoAtivoUnicoPorAssentoTest#rnA01_conf_duasSolicitacoesDeHoldSimultaneasApenasUmaEAceita` | RED — 0 aceitas/0 rejeitadas observadas (ambas as threads recebem exceção não tratada pelo teste) |
| RN-A01 | PROIB Duas vendas confirmadas para o mesmo assento | `RnA01EstadoAtivoUnicoPorAssentoTest#rnA01_proib_naoPodeConfirmarHoldParaAssentoJaConfirmado` | RED |
| RN-A01 | INV HOLD para assento inexistente no voo | `RnA01EstadoAtivoUnicoPorAssentoTest#rnA01_inv_holdParaAssentoInexistenteNoVooERejeitado` | RED — tipo de exceção incorreto (`UnsupportedOperationException` em vez de `SeatNotFoundException`) |
| RN-A01 | LIM HOLD no instante de expiração de outro HOLD | `RnA01EstadoAtivoUnicoPorAssentoTest#rnA01_lim_holdSobreAssentoComHoldConcorrenteJaExpiradoTrataComoAvailableEConcede` | RED |
| RN-A02 | CF Ciclo completo de venda | `RnA02MaquinaDeEstadosDoAssentoTest#rnA02_cf_cicloCompletoDeVendaAvailableHoldConfirmed` | RED |
| RN-A02 | CF Cancelamento voluntário do hold | `RnA02MaquinaDeEstadosDoAssentoTest#rnA02_cf_cancelamentoVoluntarioDoHoldLiberaAssentoImediatamente` | RED |
| RN-A02 | LIM Expiração exata no TTL | `RnA02MaquinaDeEstadosDoAssentoTest#rnA02_lim_expiracaoExataNoTtlLiberaAssentoNaDisponibilidadeAgregada` | RED |
| RN-A02 | PROIB Transição CONFIRMED → HOLD/AVAILABLE | `RnA02MaquinaDeEstadosDoAssentoTest#rnA02_proib_naoPodeReabrirOuReverterAssentoConfirmed` | RED |
| RN-A02 | INV CONFIRM sem HOLD prévio | `RnA02MaquinaDeEstadosDoAssentoTest#rnA02_inv_confirmarSemHoldPrevioERejeitado` | RED — tipo de exceção incorreto |
| RN-A03 | CF Duas leituras concorrentes, uma escrita vencedora | `RnA03EscritaCondicionalComoMecanismoDecisivoTest#rnA03_cf_duasLeiturasConcorrentesDoMesmoAvailableApenasUmaEscritaVencedora` | RED |
| RN-A03 | CONF N tentativas concorrentes (N=10) | `RnA03EscritaCondicionalComoMecanismoDecisivoTest#rnA03_conf_nTentativasConcorrentesApenasUmaAceitaTodasAsDemaisRejeitadas` | RED |
| RN-A03 | PROIB "Quase-vitória"/resultado ambíguo | — | Coberta implicitamente pela asserção "exatamente 1 aceita" dos dois testes acima; nenhum teste dedicado adicional foi necessário |
| RN-A04 | CF Sweeper libera hold vencido | `RnA04ExpiracaoDeHoldTest#rnA04_cf_sweeperPeriodicoLiberaHoldVencidoSemNovaTentativaSobreOAssento` | RED |
| RN-A04 | CF Expiração lazy detectada em nova tentativa | `RnA04ExpiracaoDeHoldTest#rnA04_cf_expiracaoLazyDetectadaEmNovaTentativaDeHold` | RED |
| RN-A04 | LIM TTL diferenciado por meio de pagamento — cartão (2–5min) | `RnA04ExpiracaoDeHoldTest#rnA04_lim_ttlDoHoldEDiferenciadoPorMeioDePagamentoCartaoEntreDoisECincoMinutos` | RED — **atualizado em 2026-09-17**: decisão do stakeholder (Seção 3.4, item 1) fecha a lacuna L-07; substitui o caso antes `@Disabled` |
| RN-A04 | LIM TTL diferenciado por meio de pagamento — Pix (10–15min) | `RnA04ExpiracaoDeHoldTest#rnA04_lim_ttlDoHoldEDiferenciadoPorMeioDePagamentoPixEntreDezEQuinzeMinutos` | RED — **novo em 2026-09-17**, decisão do stakeholder Seção 3.4, item 1 |
| RN-A04 | PROIB Confirmação de HOLD expirado sem revalidação | `RnA04ExpiracaoDeHoldTest#rnA04_proib_confirmacaoDeHoldExpiradoSemRevalidacaoDeveSerImpedida` | RED |
| RN-A05 | CF Venda confirmada propaga para a companhia (SLA) | `RnA05ReconciliacaoDeEstoqueTest#rnA05_cf_vendaConfirmadaPropagaParaCompanhiaDentroDoSlaP95` | RED |
| RN-A05 | LIM Propagação no limite de tempo (2s) | `RnA05ReconciliacaoDeEstoqueTest#rnA05_lim_propagacaoNoLimiteDeDoisSegundosAindaDentroDoSla` | RED |
| RN-A05 | CONF Falha na 1ª tentativa aciona reenvio | `RnA05ReconciliacaoDeEstoqueTest#rnA05_conf_falhaDeComunicacaoNaPrimeiraTentativaAcionaReenvioComBackoff` | RED |
| RN-A05 | CONF Falha nas 3 tentativas de webhook bloqueia fail-closed total | `RnA05ReconciliacaoDeEstoqueTest#rnA05_conf_falhaNasTresTentativasDeWebhookBloqueiaOperacoesFailClosedTotal` | RED — **atualizado em 2026-09-17**: decisão do stakeholder (Seção 3.4, item 6) substitui a proposta parcial do ADR-03 por fail-closed total; substitui o caso antes `@Disabled` |
| RN-A05 | PROIB Estoque exibido diverge do real | `RnA05ReconciliacaoDeEstoqueTest#rnA05_proib_estoqueExibidoNuncaDivergeDoEstoqueReal` | RED |
| RN-A05 | INV Evento com voo/assento desconhecido | `RnA05ReconciliacaoDeEstoqueTest#rnA05_inv_eventoDeEstoqueComVooOuAssentoDesconhecidoERejeitado` | RED — tipo de exceção incorreto |
| RN-A06 | CF Lote de 8 passageiros, todos confirmados | `RnA06EmissaoEmLoteNaoContornaExclusividadeTest#rnA06_cf_loteDeOitoPassageirosTodosOsAssentosDisponiveisTodosConfirmados` | RED |
| RN-A06 | LIM Lote no limite máximo (50) | `RnA06EmissaoEmLoteNaoContornaExclusividadeTest#rnA06_lim_loteNoLimiteMaximoDeCinquentaPassageirosEProcessadoCompletamente` | RED |
| RN-A06 | INV Lote acima do limite (51+) rejeitado inteiramente na ingestão | `RnA06EmissaoEmLoteNaoContornaExclusividadeTest#rnA06_inv_loteAcimaDoLimiteDeCinquentaPassageirosERejeitadoInteiramenteNaIngestao` | RED — tipo de exceção incorreto — **atualizado em 2026-09-17**: decisão do stakeholder (Seção 3.4, item 9); substitui o caso antes `@Disabled` |
| RN-A06 | CONF Item do lote concorre com reserva individual | `RnA06EmissaoEmLoteNaoContornaExclusividadeTest#rnA06_conf_itemDoLoteConcorreComReservaIndividualPeloMesmoAssentoApenasUmVence` | RED |
| RN-A06 | PROIB Lote força venda de assento indisponível | `RnA06EmissaoEmLoteNaoContornaExclusividadeTest#rnA06_proib_loteNuncaForcaVendaDeAssentoIndisponivel` | RED |

**Execução (`mvn test -Dtest='RnA*'`):** 29 testes executados — 0 passaram, 29 falharam/erraram (todos por `UnsupportedOperationException` do stub ou tipo de exceção incompatível com o stub, nunca por defeito de teste), 0 `@Disabled` (SKIPPED). Nenhum teste passou de forma inesperada — não há indício de regra já implementada ou caso mal escrito nesta fase. **Atualizado em 2026-09-17** (revalidação contra a Seção 3.4): os 3 casos antes `@Disabled` de RN-A04/A05/A06 foram convertidos em testes RED definitivos, refletindo as decisões confirmadas pelo stakeholder (TTL diferenciado, fail-closed total, rejeição de lote >50); RN-A04 ganhou um caso adicional (TTL do Pix).

### Grupo B — Busca, precificação, remarcação e alertas de preço

| RN | Classe de teste | Método (caso) | Status |
|---|---|---|---|
| RN-B06 | `RnB06AlertaDePrecoPorRotaDataTest` | `rnB06_cf_configurarAlertaParaRotaEData` | RED — `UnsupportedOperationException` (ou tipo de exceção incompatível com o stub) |
| RN-B06 | `RnB06AlertaDePrecoPorRotaDataTest` | `rnB06_cf_precoCaiAbaixoDoConfiguradoUsuarioENotificado` | RED — `UnsupportedOperationException` (ou tipo de exceção incompatível com o stub) |
| RN-B06 | `RnB06AlertaDePrecoPorRotaDataTest` | `rnB06_inv_alertaParaRotaSemVoosCadastrados` | RED — `UnsupportedOperationException` (ou tipo de exceção incompatível com o stub) |
| RN-B01 | `RnB01BuscaPorOrigemDestinoDataTest` | `rnB01_cf_buscaSimples` | RED — `UnsupportedOperationException` (ou tipo de exceção incompatível com o stub) |
| RN-B01 | `RnB01BuscaPorOrigemDestinoDataTest` | `rnB01_cf_filtroProximasHoras_pendenteDeDefinicaoDeFonte` | **SKIPPED** — `@Disabled`, ver motivo na anotação do método (lacuna/ADR/Seção 3 do plano) |
| RN-B01 | `RnB01BuscaPorOrigemDestinoDataTest` | `rnB01_inv_buscaPorRotaInternacionalERejeitadaPorEstarForaDeEscopo` | RED — tipo de exceção incorreto — **novo em 2026-09-17**: caso ausente identificado na revalidação; decisão do stakeholder Seção 3.4, item 8 (escopo internacional fora de escopo, S-03 confirmada) |
| RN-B01 | `RnB01BuscaPorOrigemDestinoDataTest` | `rnB01_inv_origemIgualDestino` | RED — `UnsupportedOperationException` (ou tipo de exceção incompatível com o stub) |
| RN-B01 | `RnB01BuscaPorOrigemDestinoDataTest` | `rnB01_inv_dataNoPassado_pendenteDeDefinicaoDeFonte` | **SKIPPED** — `@Disabled`, ver motivo na anotação do método (lacuna/ADR/Seção 3 do plano) |
| RN-B01 | `RnB01BuscaPorOrigemDestinoDataTest` | `rnB01_lim_buscaNoLimiteDaViradaDoDia_pendenteDeDefinicaoDeFonte` | **SKIPPED** — `@Disabled`, ver motivo na anotação do método (lacuna/ADR/Seção 3 do plano) |
| RN-B04 | `RnB04OrdenacaoEFiltrosTest` | `rnB04_cf_ordenarPorMenorPreco` | RED — `UnsupportedOperationException` (ou tipo de exceção incompatível com o stub) |
| RN-B04 | `RnB04OrdenacaoEFiltrosTest` | `rnB04_cf_filtrarPorPoliticaDeRemarcacaoFlexivel` | RED — `UnsupportedOperationException` (ou tipo de exceção incompatível com o stub) |
| RN-B04 | `RnB04OrdenacaoEFiltrosTest` | `rnB04_lim_listaVaziaAposFiltro` | RED — `UnsupportedOperationException` (ou tipo de exceção incompatível com o stub) |
| RN-B04 | `RnB04OrdenacaoEFiltrosTest` | `rnB04_inv_criterioDeOrdenacaoInexistente` | RED — `UnsupportedOperationException` (ou tipo de exceção incompatível com o stub) |
| RN-B02 | `RnB02CalendarioDePrecosTest` | `rnB02_cf_calendarioCobreNoMinimoQuinzeDiasAntesEDepois` | RED — `UnsupportedOperationException` (ou tipo de exceção incompatível com o stub) |
| RN-B02 | `RnB02CalendarioDePrecosTest` | `rnB02_lim_extremosExatosDMenos15EDMais15EstaoCobertos` | RED — `UnsupportedOperationException` (ou tipo de exceção incompatível com o stub) |
| RN-B02 | `RnB02CalendarioDePrecosTest` | `rnB02_inv_diaSemVooCadastradoExibicao_pendenteDeDefinicaoDeFonte` | **SKIPPED** — `@Disabled`, ver motivo na anotação do método (lacuna/ADR/Seção 3 do plano) |
| RN-B03 | `RnB03PrecoFinalSemCustoOcultoTest` | `rnB03_cf_precoNaBuscaIgualAoPrecoNoCheckoutMesmaSessao` | RED — `UnsupportedOperationException` (ou tipo de exceção incompatível com o stub) |
| RN-B03 | `RnB03PrecoFinalSemCustoOcultoTest` | `rnB03_conf_precoCongeladoDentroDaSessaoMesmoAposMudancaDeTarifaDinamica` | RED — **atualizado em 2026-09-17**: decisão do stakeholder (Seção 3.4, item 2, ADR-07 adotado) fecha o conflito C-02; substitui o caso antes `@Disabled` |
| RN-B03 | `RnB03PrecoFinalSemCustoOcultoTest` | `rnB03_cf_tarifaDinamicaPodeMudarEntreSessoesDistintasComSinalizacaoAntesDaConfirmacao` | RED — **atualizado em 2026-09-17**: ADR-07 adotado (Seção 3.4, item 2); substitui o caso antes `@Disabled` |
| RN-B03 | `RnB03PrecoFinalSemCustoOcultoTest` | `rnB03_lim_atualizacaoDeTarifaDinamicaExatamenteNosCincoMinutosEPermitidaComSinalizacao` | RED — **atualizado em 2026-09-17**: ADR-07 adotado (Seção 3.4, item 2); substitui o caso antes `@Disabled` |
| RN-B03 | `RnB03PrecoFinalSemCustoOcultoTest` | `rnB03_proib_precoFinalMaiorNoCheckoutSemSinalizacaoMesmaSessao` | RED — `UnsupportedOperationException` (ou tipo de exceção incompatível com o stub) |
| RN-B03 | `RnB03PrecoFinalSemCustoOcultoTest` | `rnB03_cf_composicaoDoPrecoFinalNaBuscaIncluiTaxasBagagemEEncargos` | RED — `UnsupportedOperationException` (ou tipo de exceção incompatível com o stub) |
| RN-B05 | `RnB05IndicacaoDePoliticaDeRemarcacaoTest` | `rnB05_cf_tarifaComRemarcacaoSemCustoIndicacaoVisivelAntesDaCompra` | RED — `UnsupportedOperationException` (ou tipo de exceção incompatível com o stub) |
| RN-B05 | `RnB05IndicacaoDePoliticaDeRemarcacaoTest` | `rnB05_cf_tarifaComMultaDeRemarcacaoExibidaAntesDaCompra` | RED — `UnsupportedOperationException` (ou tipo de exceção incompatível com o stub) |
| RN-B05 | `RnB05IndicacaoDePoliticaDeRemarcacaoTest` | `rnB05_proib_compraConcluidaSemExibirPoliticaDeRemarcacao` | RED — `UnsupportedOperationException` (ou tipo de exceção incompatível com o stub) |

**Execução (`mvn test -Dtest='RnB*'`):** 25 testes — 0 passaram, 21 falharam/erraram pelo motivo esperado, 4 `@Disabled` (SKIPPED). **Atualizado em 2026-09-17**: caso ausente de RN-B01 (rota internacional fora de escopo) adicionado; os 3 casos de RN-B03 antes `@Disabled` convertidos em RED definitivo (ADR-07 adotado). Os 4 `@Disabled` remanescentes (RN-B01 ×3, RN-B02 ×1) permanecem legítimos — lacunas de fronteira/temporização não tocadas pela Seção 3.4.

### Grupo C — Checkout, cadastro, pagamento, notificação, fiscal, passageiros e documentação

| RN | Classe de teste | Método (caso) | Status |
|---|---|---|---|
| RN-C02 | `RnC02RetencaoDeDadosCadastraisTest` | `rnC02_cf_segundaCompraDoMesmoUsuarioPreencheCamposAutomaticamente` | GREEN — `CadastroService.preencherAutomaticamente` retorna `DadosCadastrais` com CPF/endereço/cartão/fidelidade pré-preenchidos |
| RN-C02 | `RnC02RetencaoDeDadosCadastraisTest` | `rnC02_conf_cartaoSalvoExpiradoFalhaDeFormaGenericaComNotificacaoAoUsuarioSemFluxoDeReautorizacao` | GREEN — `CadastroService.cobrarComCartaoSalvo` lança `CobrancaComCartaoSalvoFalhouException` quando `CartaoSalvo.expirado(hoje)`, sem fluxo de reautorização |
| RN-C02 | `RnC02RetencaoDeDadosCadastraisTest` | `rnC02_cf_dadosPreenchidosCorrespondemAoUsuarioSolicitado` | GREEN — `DadosCadastrais.usuarioId()` ecoa o id solicitado |
| RN-C01 | `RnC01CheckoutRapidoTest` | `rnC01_cf_checkoutComPerfilECartaoSalvos` | GREEN — `CheckoutService.iniciarCheckoutRapido` calcula telas/cliques/campos a partir do perfil e valida os limites da RNF-13 |
| RN-C01 | `RnC01CheckoutRapidoTest` | `rnC01_lim_exatamente4Telas3Cliques2CamposAdicionais` | GREEN — fluxo no limite exato (4/3/2) tratado como conforme |
| RN-C01 | `RnC01CheckoutRapidoTest` | `rnC01_inv_checkoutExcede4TelasOuMaisDe2CamposAdicionais` | GREEN — excedente lança `CheckoutNaoConformeException` (falha de aceitação, não aviso) |
| RN-C01 | `RnC01CheckoutRapidoTest` | `rnC01_cf_checkoutSemPerfilSalvo_pendenteDeDefinicaoDeFonte` | **SKIPPED** — `@Disabled`, ver motivo na anotação do método (lacuna/ADR/Seção 3 do plano) |
| RN-C07 | `RnC07SelecaoDeAssentosEmGrupoTest` | `rnC07_cf_selecaoDeAssentosLadoALadoParaQuatroPassageiros` | GREEN — `SeatGroupSelectionService.selecionarGrupo` solicita um HOLD por assento via `SeatInventoryService` |
| RN-C07 | `RnC07SelecaoDeAssentosEmGrupoTest` | `rnC07_conf_assentosContiguosInsuficientesParaOGrupo_pendenteDeDefinicaoDeFonte` | **SKIPPED** — `@Disabled`, ver motivo na anotação do método (lacuna/ADR/Seção 3 do plano) |
| RN-C07 | `RnC07SelecaoDeAssentosEmGrupoTest` | `rnC07_conf_concorrenciaOutroUsuarioReservaAssentoDoBlocoDuranteSelecaoDoGrupo` | GREEN — concorrência de terceiro sobre um assento do bloco lança `GrupoIncompletoException` e libera os HOLDs já concedidos ao grupo |
| RN-C08 | `RnC08TransparenciaDaTarifaEUpsellTest` | `rnC08_cf_exibicaoDoQueEstaInclusoAntesDaConfirmacao` | GREEN — `CheckoutUpsellService.informacoesInclusas` retorna `InformacaoTarifa` não nula |
| RN-C08 | `RnC08TransparenciaDaTarifaEUpsellTest` | `rnC08_lim_exatamente3OfertasDeUpsellExibidas` | GREEN — `ofertasExibidas` respeita o limite de 3 ofertas |
| RN-C08 | `RnC08TransparenciaDaTarifaEUpsellTest` | `rnC08_inv_maisDe3OfertasDeUpsellExibidasPorPadraoNaoEConforme` | GREEN — `ofertasExibidas` corta para no máximo 3 mesmo com 4+ configuradas |
| RN-C08 | `RnC08TransparenciaDaTarifaEUpsellTest` | `rnC08_proib_upsellPreMarcadoPreSelecionadoEProibido` | GREEN — `ofertasExibidas` sempre força `preSelecionada=false` (opt-in explícito) |
| RN-C08 | `RnC08TransparenciaDaTarifaEUpsellTest` | `rnC08_cf_ocultarTodasAsOfertasEmUmCliqueRemoveTodasImediatamente` | GREEN — `ocultarTodas` retorna lista vazia |
| RN-C05 | `RnC05EmissaoDeNotaFiscalTest` | `rnC05_cf_pagamentoAprovadoEmiteNotaFiscalDisponivelPorEmailEDownload` | GREEN — `NotaFiscalService.emitir` retorna `NotaFiscal` disponível por e-mail/download em ≤60s |
| RN-C05 | `RnC05EmissaoDeNotaFiscalTest` | `rnC05_lim_emissaoExatamenteAos60SegundosAindaDentroDoSla` | GREEN — duração fixada em exatamente 60s, dentro do SLA |
| RN-C05 | `RnC05EmissaoDeNotaFiscalTest` | `rnC05_conf_falhaDoEmissorFiscalExterno_pendenteDeDefinicaoDeFonte` | **SKIPPED** — `@Disabled`, ver motivo na anotação do método (lacuna/ADR/Seção 3 do plano) |
| RN-C05 | `RnC05EmissaoDeNotaFiscalTest` | `rnC05_proib_emissaoNaoPodeDependerDeSolicitacaoManualDoPassageiro` | GREEN — emissão automática ao chamar `emitir`, sem ação manual do passageiro |
| RN-C04 | `RnC04ConfirmacaoELocalizadorTest` | `rnC04_cf_pagamentoAprovadoEnviaConfirmacaoPorEmailEWhatsappEmAte30s` | GREEN — `NotificacaoService.enviarConfirmacao` retorna `EnvioResultado` com ambos os canais enviados em ≤30s |
| RN-C04 | `RnC04ConfirmacaoELocalizadorTest` | `rnC04_lim_envioExatamenteAos30SegundosAindaDentroDoSla` | GREEN — duração fixada em exatamente 30s, dentro do SLA |
| RN-C04 | `RnC04ConfirmacaoELocalizadorTest` | `rnC04_conf_falhaDeEntregaEmUmDosCanais_pendenteDeDefinicaoDeFonte` | **SKIPPED** — `@Disabled`, ver motivo na anotação do método (lacuna/ADR/Seção 3 do plano) |
| RN-C04 | `RnC04ConfirmacaoELocalizadorTest` | `rnC04_proib_reservaConfirmadaSemQualquerNotificacaoEnviadaEProibida` | GREEN — pelo menos um canal sempre confirmado (RNF-19) |
| RN-C03 | `RnC03MultiplosMeiosDePagamentoTest` | `rnC03_cf_pagamentoViaPix` | GREEN — `PagamentoService.pagarViaPix` aprova qualquer valor |
| RN-C03 | `RnC03MultiplosMeiosDePagamentoTest` | `rnC03_cf_pagamentoParceladoNoCartao` | GREEN — `pagarParcelado` aprova valor alto em 8x |
| RN-C03 | `RnC03MultiplosMeiosDePagamentoTest` | `rnC03_lim_parcelamentoDeValorMinimoPermitido_pendenteDeDefinicaoDeFonte` | **SKIPPED** — `@Disabled`, ver motivo na anotação do método (lacuna/ADR/Seção 3 do plano) |
| RN-C03 | `RnC03MultiplosMeiosDePagamentoTest` | `rnC03_inv_cartaoInvalidoOuRecusadoRejeitaTransacaoSemHoldConfirmado` | GREEN — `pagarComCartao` valida número/validade e lança `CartaoRecusadoException` |
| RN-C03 | `RnC03MultiplosMeiosDePagamentoTest` | `rnC03_proib_armazenamentoDePanEmTextoClaro` | GREEN — `tokenizar` retorna token derivado (UUID) que nunca contém o PAN em texto claro |
| RN-C03 | `RnC03MultiplosMeiosDePagamentoTest` | `rnC03_conf_falhaDePagamentoAposHoldConcedidoMantemHoldENaoConfirmaAssento` | GREEN — pagamento recusado não interage com `SeatInventoryService`; HOLD permanece intacto |
| RN-C06 | `RnC06CompraMultiplosPassageirosTest` | `rnC06_cf_familiaDeQuatroPassageirosEmUmaTransacao` | GREEN — `CompraMultiplosPassageirosService.comprar` inclui todos os passageiros em uma única transação |
| RN-C06 | `RnC06CompraMultiplosPassageirosTest` | `rnC06_cf_reusoDeDadosDoPrimeiroParaOsDemaisPassageirosReduzCamposManuais` | GREEN — `percentualCamposReaproveitados` ≥70% |
| RN-C06 | `RnC06CompraMultiplosPassageirosTest` | `rnC06_lim_reusoExatamenteEm70PorCentoAtingeOPisoMinimo` | GREEN — piso mínimo de 70% tratado como conforme |
| RN-C06 | `RnC06CompraMultiplosPassageirosTest` | `rnC06_inv_transacaoComZeroPassageirosERejeitada` | GREEN — lista vazia lança `NenhumPassageiroSelecionadoException` |
| RN-C09 | `RnC09DocumentacaoParaMenoresDeIdadeTest` | `rnC09_cf_passageiroMenorDeIdadeIncluidoNaCompraExibeRegrasDeDocumentacao` | GREEN — `DocumentacaoMenorService.regrasDeDocumentacaoAplicaveis` retorna lista não vazia de regras |
| RN-C09 | `RnC09DocumentacaoParaMenoresDeIdadeTest` | `rnC09_conf_dadosDeMenorColetadosSemConsentimentoApropriado_pendenteDeDefinicaoDeFonte` | **SKIPPED** — `@Disabled`, ver motivo na anotação do método (lacuna/ADR/Seção 3 do plano) |

**Execução (`mvn test -Dtest='RnC*'`):** 35 testes — 29 passaram, 0 falharam/erraram, 6 `@Disabled` (SKIPPED). **Atualizado em 2026-09-18 (fase GREEN)**: todas as 9 classes de teste do Grupo C implementadas e verdes. RN-C02 permanece alinhada à decisão do stakeholder (Seção 3.4, item 10): cartão salvo expirado falha de forma genérica, sem fluxo de reautorização dedicado. Nota de implementação: `SeatGroupSelectionService`/`RnC03`/`RnC07` dependem de `estoque.SeatInventoryService` (Grupo A); apenas o suficiente de `hold`/`confirm`/`cancel`/`statusOf` foi implementado ali para sustentar os testes do Grupo C, sem esgotar o escopo do Grupo A.

### Grupo D — Pós-venda (suporte, remarcação/cancelamento self-service, notificação de alteração de voo)

| RN | Classe de teste | Método (caso) | Status |
|---|---|---|---|
| RN-D01 | `RnD01CanalDeSuporte24hTest` | `rnD01_cf_usuarioAcionaChatAsTresDaManhaCanalDisponivel` | RED — `UnsupportedOperationException` (ou tipo de exceção incompatível com o stub) |
| RN-D01 | `RnD01CanalDeSuporte24hTest` | `rnD01_lim_tempoDePrimeiraRespostaNoLimiteDeDoisMinutos` | RED — `UnsupportedOperationException` (ou tipo de exceção incompatível com o stub) |
| RN-D01 | `RnD01CanalDeSuporte24hTest` | `rnD01_proib_canalIndisponivelEmQualquerJanelaDoDiaNuncaOcorre` | RED — `UnsupportedOperationException` (ou tipo de exceção incompatível com o stub) |
| RN-D02 | `RnD02RemarcacaoECancelamentoSelfServiceTest` | `rnD02_cf_remarcacaoSemCustoDentroDaPoliticaDaTarifa` | RED — `UnsupportedOperationException` (ou tipo de exceção incompatível com o stub) |
| RN-D02 | `RnD02RemarcacaoECancelamentoSelfServiceTest` | `rnD02_cf_cancelamentoComMultaExibidaEConfirmadaAntesDaEfetivacao` | RED — `UnsupportedOperationException` (ou tipo de exceção incompatível com o stub) |
| RN-D02 | `RnD02RemarcacaoECancelamentoSelfServiceTest` | `rnD02_proib_alteracaoEfetivadaSemExibirCustoAntesDaConfirmacaoERejeitada` | RED — `UnsupportedOperationException` (ou tipo de exceção incompatível com o stub) |
| RN-D02 | `RnD02RemarcacaoECancelamentoSelfServiceTest` | `rnD02_conf_remarcacaoParaVooSemAssentosDisponiveisEBloqueadaPelaExclusividadeDeAssento` | RED — `UnsupportedOperationException` (ou tipo de exceção incompatível com o stub) |
| RN-D03 | `RnD03NotificacaoDeAlteracaoOuCancelamentoDeVooTest` | `rnD03_cf_companhiaCancelaVooPassageiroEAgenteB2bSaoNotificados` | RED — `UnsupportedOperationException` (ou tipo de exceção incompatível com o stub) |
| RN-D03 | `RnD03NotificacaoDeAlteracaoOuCancelamentoDeVooTest` | `rnD03_conf_vooAlteradoComMudancaDeHorarioTambemGeraNotificacao` | RED — `UnsupportedOperationException` (ou tipo de exceção incompatível com o stub) |

**Execução (`mvn test -Dtest='RnD*'`):** 9 testes — 0 passaram, 9 falharam/erraram pelo motivo esperado, 0 `@Disabled` (SKIPPED).

### Grupo E — B2B (painel de agente, motor de políticas, emissão em lote, API pública, faturamento)

| RN | Classe de teste | Método (caso) | Status |
|---|---|---|---|
| RN-E01 | `RnE01PainelDeAgenteTest` | `rnE01_cf_agenteSelecionaEmpresaClienteEReservaEmNomeDeFuncionario` | RED — `UnsupportedOperationException` (ou tipo de exceção incompatível com o stub) |
| RN-E01 | `RnE01PainelDeAgenteTest` | `rnE01_proib_agenteAcessaDadosDeEmpresaClienteDiferente` | RED — `UnsupportedOperationException` (ou tipo de exceção incompatível com o stub) |
| RN-E02 | `RnE02MotorDePoliticasTest` | `rnE02_cf_buscaDentroDaPoliticaSugereVoosAutomaticamente` | RED — `UnsupportedOperationException` (ou tipo de exceção incompatível com o stub) |
| RN-E02 | `RnE02MotorDePoliticasTest` | `rnE02_proib_reservaForaDaPoliticaEBloqueadaOuSinalizada` | RED — `UnsupportedOperationException` (ou tipo de exceção incompatível com o stub) |
| RN-E02 | `RnE02MotorDePoliticasTest` | `rnE02_conf_cemPorCentoDasReservasForaDaPoliticaSaoBloqueadasSemBandaDeToleranciaDeAceite` | RED — tipo de exceção incorreto — **atualizado em 2026-09-17**: decisão do stakeholder (Seção 3.4, item 3) fixa 100% como critério de aceite rígido, ADR-08 rejeitado; substitui o caso antes `@Disabled` |
| RN-E02 | `RnE02MotorDePoliticasTest` | `rnE02_cf_alteracaoDePoliticaSemDeployReflete` | RED — `UnsupportedOperationException` (ou tipo de exceção incompatível com o stub) |
| RN-E02 | `RnE02MotorDePoliticasTest` | `rnE02_lim_alteracaoDePoliticaRefletidaExatamenteAosCincoMinutosAindaDentroDoSla` | RED — `UnsupportedOperationException` (ou tipo de exceção incompatível com o stub) |
| RN-E03 | `RnE03EmissaoEmLotePlanilhaTest` | `rnE03_cf_planilhaComOitoPassageirosValidosProcessaTodosComSugestaoDentroDaPolitica` | RED — `UnsupportedOperationException` (ou tipo de exceção incompatível com o stub) |
| RN-E03 | `RnE03EmissaoEmLotePlanilhaTest` | `rnE03_inv_linhaComDadosIncompletosERejeitadaIndividualmenteSemInterromperOLote` | RED — `UnsupportedOperationException` (ou tipo de exceção incompatível com o stub) |
| RN-E03 | `RnE03EmissaoEmLotePlanilhaTest` | `rnE03_inv_planilhaEmFormatoNaoSuportadoERejeitadaSemProcessamentoParcial` | RED — `UnsupportedOperationException` (ou tipo de exceção incompatível com o stub) |
| RN-E03 | `RnE03EmissaoEmLotePlanilhaTest` | `rnE03_conf_passageiroDaPlanilhaComReservaConflitanteAtivaDeduplicacao_pendenteDeDefinicaoDeFonte` | **SKIPPED** — `@Disabled`, ver motivo na anotação do método (lacuna/ADR/Seção 3 do plano) |
| RN-E04 | `RnE04ApiPublicaB2bTest` | `rnE04_cf_chamadaAutenticadaViaOAuth2ComTokenValidoEProcessada` | RED — `UnsupportedOperationException` (ou tipo de exceção incompatível com o stub) |
| RN-E04 | `RnE04ApiPublicaB2bTest` | `rnE04_lim_tokenNoLimiteExatoDeExpiracao_pendenteDeDefinicaoDeFonte` | **SKIPPED** — `@Disabled`, ver motivo na anotação do método (lacuna/ADR/Seção 3 do plano) |
| RN-E04 | `RnE04ApiPublicaB2bTest` | `rnE04_inv_chamadaComTokenExpiradoOuInvalidoERejeitada` | RED — `UnsupportedOperationException` (ou tipo de exceção incompatível com o stub) |
| RN-E04 | `RnE04ApiPublicaB2bTest` | `rnE04_proib_chamadaSemAutenticacaoEBloqueada` | RED — `UnsupportedOperationException` (ou tipo de exceção incompatível com o stub) |
| RN-E04 | `RnE04ApiPublicaB2bTest` | `rnE04_conf_chamadaAVersaoDescontinuadaDentroDaJanelaDeRetrocompatibilidadeContinuaFuncionando` | RED — `UnsupportedOperationException` (ou tipo de exceção incompatível com o stub) |
| RN-E04 | `RnE04ApiPublicaB2bTest` | `rnE04_inv_chamadaAVersaoDeApiForaDaJanelaDeRetrocompatibilidade_pendenteDeDefinicaoDeFonte` | **SKIPPED** — `@Disabled`, ver motivo na anotação do método (lacuna/ADR/Seção 3 do plano) |
| RN-E05 | `RnE05FaturamentoConsolidadoTest` | `rnE05_cf_multiplasReservasDaEmpresaNoPeriodoGeramFaturaConsolidadaUnica` | RED — `UnsupportedOperationException` (ou tipo de exceção incompatível com o stub) |
| RN-E05 | `RnE05FaturamentoConsolidadoTest` | `rnE05_lim_fechamentoExatamenteNoPrimeiroDiaUtilDentroDoPrazo` | RED — `UnsupportedOperationException` (ou tipo de exceção incompatível com o stub) |
| RN-E05 | `RnE05FaturamentoConsolidadoTest` | `rnE05_conf_reservaConfirmadaNoUltimoInstanteDoPeriodoAtribuidaCorretamenteSemDuplicidadeNemOmissao` | RED — `UnsupportedOperationException` (ou tipo de exceção incompatível com o stub) |
| RN-E05 | `RnE05FaturamentoConsolidadoTest` | `rnE05_proib_faturaFragmentadaPorPassagemParaClienteB2bEProibida` | RED — `UnsupportedOperationException` (ou tipo de exceção incompatível com o stub) |

**Execução (`mvn test -Dtest='RnE*'`):** 21 testes — 0 passaram, 18 falharam/erraram pelo motivo esperado, 3 `@Disabled` (SKIPPED). **Atualizado em 2026-09-17**: caso de RN-E02 antes `@Disabled` convertido em RED definitivo — Decisão confirmada pelo stakeholder (Seção 3.4, item 3) trata os 100% de bloqueio como meta rígida/vinculante, sem banda de tolerância de aceite (ADR-08 rejeitado).

### Grupo F — Companhias aéreas (cadastro de tarifas, campanhas, continuidade, dashboard comercial, posicionamento competitivo)

| RN | Classe de teste | Método (caso) | Status |
|---|---|---|---|
| RN-F01 | `RnF01CadastroDeRotasHorariosClassesETarifasTest` | `rnF01_cf_gestorComercialCadastraNovaTarifaComDadosCompletosEValidos` | RED — `UnsupportedOperationException` (ou tipo de exceção incompatível com o stub) |
| RN-F01 | `RnF01CadastroDeRotasHorariosClassesETarifasTest` | `rnF01_lim_cadastroConcluidoEmExatamenteCincoMinutosPorUsuarioNaoTecnico` | RED — `UnsupportedOperationException` (ou tipo de exceção incompatível com o stub) |
| RN-F01 | `RnF01CadastroDeRotasHorariosClassesETarifasTest` | `rnF01_inv_cadastroComCampoObrigatorioAusenteERejeitadoComIndicacaoDoCampo` | RED — `UnsupportedOperationException` (ou tipo de exceção incompatível com o stub) |
| RN-F01 | `RnF01CadastroDeRotasHorariosClassesETarifasTest` | `rnF01_conf_duasAlteracoesDeTarifaDaMesmaRotaQuaseSimultaneasPeloMesmoGestor_pendenteDeDefinicaoDeFonte` | **SKIPPED** — `@Disabled`, ver motivo na anotação do método (lacuna/ADR/Seção 3 do plano) |
| RN-F05 | `RnF05PosicionamentoCompetitivoTest` | `rnF05_cf_gestorConsultaRankingDeExibicaoEPrecoParaSuaRotaComConcorrenciaCadastrada` | RED — `UnsupportedOperationException` (ou tipo de exceção incompatível com o stub) |
| RN-F03 | `RnF03SincronizacaoDeEstoqueEmTempoRealTest` | `rnF03_conf_falhaDeSincronizacaoPorPeriodoProlongadoRecuperaDentroDoRtoERpo` | RED — `UnsupportedOperationException` (ou tipo de exceção incompatível com o stub) |
| RN-F03 | `RnF03SincronizacaoDeEstoqueEmTempoRealTest` | `rnF03_proib_perdaDeDadosDeEstoqueAcimaDoRpoDefinidoEProibida` | RED — `UnsupportedOperationException` (ou tipo de exceção incompatível com o stub) |
| RN-F04 | `RnF04DashboardDeDesempenhoComercialTest` | `rnF04_cf_consultaDeAte90DiasSemTimeoutECarregamentoRapido` | RED — `UnsupportedOperationException` (ou tipo de exceção incompatível com o stub) |
| RN-F04 | `RnF04DashboardDeDesempenhoComercialTest` | `rnF04_lim_consultaDeExatamente12MesesDeHistoricoCarregaDentroDoLimite` | RED — `UnsupportedOperationException` (ou tipo de exceção incompatível com o stub) |
| RN-F04 | `RnF04DashboardDeDesempenhoComercialTest` | `rnF04_inv_consultaDePeriodoSuperiorA12Meses_pendenteDeDefinicaoDeFonte` | **SKIPPED** — `@Disabled`, ver motivo na anotação do método (lacuna/ADR/Seção 3 do plano) |
| RN-F02 | `RnF02CampanhasPromocionaisTest` | `rnF02_cf_criacaoDeCampanhaComRotaVigenciaEQuantidadeDeAssentosValidas` | RED — `UnsupportedOperationException` (ou tipo de exceção incompatível com o stub) |
| RN-F02 | `RnF02CampanhasPromocionaisTest` | `rnF02_lim_quantidadeDeAssentosPromocionaisIgualAZero_pendenteDeDefinicaoDeFonte` | **SKIPPED** — `@Disabled`, ver motivo na anotação do método (lacuna/ADR/Seção 3 do plano) |
| RN-F02 | `RnF02CampanhasPromocionaisTest` | `rnF02_conf_assentosPromocionaisVendidosExcedemQuantidadeCadastrada_pendenteDeDefinicaoDeFonte` | **SKIPPED** — `@Disabled`, ver motivo na anotação do método (lacuna/ADR/Seção 3 do plano) |
| RN-F02 | `RnF02CampanhasPromocionaisTest` | `rnF02_proib_vendaForaDaCampanhaContabilizadaComoPromocionalOuViceVersaEProibida` | RED — `UnsupportedOperationException` (ou tipo de exceção incompatível com o stub) |

**Execução (`mvn test -Dtest='RnF*'`):** 14 testes — 0 passaram, 10 falharam/erraram pelo motivo esperado, 4 `@Disabled` (SKIPPED).

### Grupo G — Operação interna (onboarding de parceiras, painel de saúde, detecção de fraude, conciliação financeira, disputas)

| RN | Classe de teste | Método (caso) | Status |
|---|---|---|---|
| RN-G04 | `RnG04ConciliacaoFinanceiraIdempotenteTest` | `rnG04_cf_conciliacaoDeUmPeriodoFechadoGeraRelatorioConsolidadoSemDuplicidade` | RED — `UnsupportedOperationException` (ou tipo de exceção incompatível com o stub) |
| RN-G04 | `RnG04ConciliacaoFinanceiraIdempotenteTest` | `rnG04_conf_reprocessamentoDoMesmoPeriodoNaoGeraDuplicidadeViaIdempotencia` | RED — `UnsupportedOperationException` (ou tipo de exceção incompatível com o stub) |
| RN-G04 | `RnG04ConciliacaoFinanceiraIdempotenteTest` | `rnG04_proib_lancamentoDuplicadoAposReprocessamentoNuncaPodeOcorrer` | RED — `UnsupportedOperationException` (ou tipo de exceção incompatível com o stub) |
| RN-G04 | `RnG04ConciliacaoFinanceiraIdempotenteTest` | `rnG04_cf_trilhaDeAuditoriaDeConciliacaoERegistradaDeFormaImutavelERetidaPorNoMinimoCincoAnos` | RED — `UnsupportedOperationException` (ou tipo de exceção incompatível com o stub) |
| RN-G04 | `RnG04ConciliacaoFinanceiraIdempotenteTest` | `rnG04_conf_auditoriaDeConciliacaoVsExclusaoLgpdDeUmTitularEnvolvido_pendenteDeEscalacaoFormal` | **SKIPPED** — `@Disabled`, **revisado em 2026-09-17**: RN-H02 foi resolvida (Seção 3.4, item 7), mas a própria decisão declara um conflito NOVO e não mitigado com RNF-23, que exige escalada formal antes de qualquer critério de aceite — permanece corretamente `@Disabled` |
| RN-G03 | `RnG03AlertasAutomaticosDePadroesAnomalosTest` | `rnG03_cf_volumeDeReembolsosAtingeTresVezesAMediaMovelDisparaAlertaDentroDoSla` | RED — `UnsupportedOperationException` (ou tipo de exceção incompatível com o stub) |
| RN-G03 | `RnG03AlertasAutomaticosDePadroesAnomalosTest` | `rnG03_lim_volumeExatamenteIgualATresVezesAMediaDeveDispararNoLimiar` | RED — `UnsupportedOperationException` (ou tipo de exceção incompatível com o stub) |
| RN-G03 | `RnG03AlertasAutomaticosDePadroesAnomalosTest` | `rnG03_inv_volumeLigeiramenteAbaixoDoLimiarNaoDeveDispararAlerta` | RED — `UnsupportedOperationException` (ou tipo de exceção incompatível com o stub) |
| RN-G03 | `RnG03AlertasAutomaticosDePadroesAnomalosTest` | `rnG03_lim_alertaDisparadoExatamenteAosQuinzeMinutosAindaDentroDoSla` | RED — `UnsupportedOperationException` (ou tipo de exceção incompatível com o stub) |
| RN-G05 | `RnG05MediacaoDeDisputasDeReembolsoComSlaTest` | `rnG05_cf_aberturaDeDisputaEntrePassageiroECompanhiaIniciaFluxoDeMediacaoComAcompanhamentoDeSla` | RED — `UnsupportedOperationException` (ou tipo de exceção incompatível com o stub) |
| RN-G05 | `RnG05MediacaoDeDisputasDeReembolsoComSlaTest` | `rnG05_lim_slaDeMediacaoUsaBaselineDeQuinzeDiasCorridosConfirmadoPeloStakeholder` | RED — `UnsupportedOperationException` (ou tipo de exceção incompatível com o stub) — **novo em 2026-09-17**: decisão do stakeholder (Seção 3.4, item 5) fecha a lacuna L-03 com baseline de 15 dias |
| RN-G01 | `RnG01OnboardingPadronizadoDeCompanhiasParceirasTest` | `rnG01_cf_novaCompanhiaCompletaDocumentacaoETestesTecnicosConcluiOnboardingDentroDoBaseline` | RED — `UnsupportedOperationException` (ou tipo de exceção incompatível com o stub) |
| RN-G01 | `RnG01OnboardingPadronizadoDeCompanhiasParceirasTest` | `rnG01_conf_companhiaNaoAtingeNivelDeCapacidadeTecnicaMinima_pendenteDeAdocaoDoAdr05` | **SKIPPED** — `@Disabled`, ver motivo na anotação do método (lacuna/ADR/Seção 3 do plano) |
| RN-G02 | `RnG02PainelUnicoDeIndicadoresDeSaudeDaOperacaoTest` | `rnG02_cf_consultaConsolidadaDeFraudeDisputasESlaPorCompanhiaExibeIndicadoresUnificados` | RED — `UnsupportedOperationException` (ou tipo de exceção incompatível com o stub) |
| RN-G02 | `RnG02PainelUnicoDeIndicadoresDeSaudeDaOperacaoTest` | `rnG02_proib_vazamentoDeDadoDeUmaCompanhiaParaOPainelDeOutraNuncaPodeOcorrer` | RED — `UnsupportedOperationException` (ou tipo de exceção incompatível com o stub) |

**Execução (`mvn test -Dtest='RnG*'`):** 15 testes — 0 passaram, 13 falharam/erraram pelo motivo esperado, 2 `@Disabled` (SKIPPED). **Atualizado em 2026-09-17**: caso ausente de RN-G05 (baseline de SLA de 15 dias corridos, Seção 3.4 item 5, lacuna L-03 fechada) adicionado. O `@Disabled` de RN-G04 foi mantido, mas sua justificativa foi reescrita: a Decisão do item 7 resolveu RN-H02, porém cria um conflito NOVO e não mitigado com RNF-23 que exige escalada formal — não é resolução silenciosa.

### Grupo H — Segurança e conformidade (tokenização, LGPD, RBAC cross-tenant, OAuth2/auditoria de API)

| RN | Classe de teste | Método (caso) | Status |
|---|---|---|---|
| RN-H01 | `RnH01TokenizacaoPagamentoTest` | `rnH01_cf_cartaoSalvoParaComprasFuturasEArmazenadoComoTokenNuncaComoPan` | RED — `UnsupportedOperationException` (ou tipo de exceção incompatível com o stub) |
| RN-H01 | `RnH01TokenizacaoPagamentoTest` | `rnH01_proib_nenhumArmazenamentoPersistentePodeConterPanEmTextoClaro` | RED — `UnsupportedOperationException` (ou tipo de exceção incompatível com o stub) |
| RN-H02 | `RnH02ExclusaoLgpdTest` | `rnH02_cf_titularSemTransacoesPendentesDeAuditoriaEExcluidoEmAte15DiasCorridos` | RED — `UnsupportedOperationException` (ou tipo de exceção incompatível com o stub) |
| RN-H02 | `RnH02ExclusaoLgpdTest` | `rnH02_conf_titularComTransacaoEmRetencaoDeAuditoriaTemExclusaoTotalDentroDoPrazo` | RED — `UnsupportedOperationException` (ou tipo de exceção incompatível com o stub) — **atualizado em 2026-09-17**: decisão do stakeholder (Seção 3.4, item 7) — exclusão prevalece, apaga-se inclusive a trilha financeira; substitui o caso antes `@Disabled` |
| RN-H02 | `RnH02ExclusaoLgpdTest` | `rnH02_cf_dadoDeMenorDeIdadeSujeitoAExclusaoRecebeMesmoTratamentoDeRnf25` | RED — `UnsupportedOperationException` (ou tipo de exceção incompatível com o stub) |
| RN-H03 | `RnH03RbacCrossTenantTest` | `rnH03_cf_usuarioComPapelDefinidoAcessaApenasRecursosDoSeuEscopo` | RED — `UnsupportedOperationException` (ou tipo de exceção incompatível com o stub) |
| RN-H03 | `RnH03RbacCrossTenantTest` | `rnH03_proib_companhiaAAcessaDadosDaCompanhiaBEBloqueado` | RED — `UnsupportedOperationException` (ou tipo de exceção incompatível com o stub) |
| RN-H03 | `RnH03RbacCrossTenantTest` | `rnH03_conf_auditoriaTrimestralNaoDetectaVazamentoEntreJanelas_pendenteDeAdocaoDoAdr04` | **SKIPPED** — `@Disabled`, ver motivo na anotação do método (lacuna/ADR/Seção 3 do plano) |
| RN-H04 | `RnH04OAuthExpiracaoTokenTest` | `rnH04_cf_logDeAuditoriaDeChamadaDeApiERegistradoERetidoPorAoMenos5Anos` | RED — `UnsupportedOperationException` (ou tipo de exceção incompatível com o stub) |
| RN-H04 | `RnH04OAuthExpiracaoTokenTest` | `rnH04_proib_tokenDeAcessoValidoPorMaisDeUmaHoraEProibido` | RED — `UnsupportedOperationException` (ou tipo de exceção incompatível com o stub) |

**Execução (`mvn test -Dtest='RnH*'`):** 10 testes — 0 passaram, 9 falharam/erraram pelo motivo esperado, 1 `@Disabled` (SKIPPED). **Atualizado em 2026-09-17**: caso de RN-H02 antes `@Disabled` convertido em RED definitivo — Decisão confirmada pelo stakeholder (Seção 3.4, item 7) determina que a exclusão LGPD prevalece sobre a retenção de auditoria, apagando-se inclusive a trilha financeira, em até 15 dias (ADR-09 de pseudonimização rejeitado).

### Grupo I — Disponibilidade e resiliência (SLA de busca, fail-closed de posse de estoque)

| RN | Classe de teste | Método (caso) | Status |
|---|---|---|---|
| RN-I01 | `RnI01DisponibilidadeBuscaTest` | `rnI01_cf_operacaoNormalAoLongoDoMesAtendeMetaDe99_9PorCento` | RED — `UnsupportedOperationException` (ou tipo de exceção incompatível com o stub) |
| RN-I01 | `RnI01DisponibilidadeBuscaTest` | `rnI01_lim_indisponibilidadeDeExatamente43MinutosNoLimiteQualquerMinutoAlemViolaAMeta` | RED — `UnsupportedOperationException` (ou tipo de exceção incompatível com o stub) |
| RN-I02 | `RnI02FailClosedPosseTest` | `rnI02_proib_confirmarPosseComEstoqueIndisponivelDeveFalharDeFormaFechada` | RED — `UnsupportedOperationException` (ou tipo de exceção incompatível com o stub) |
| RN-I02 | `RnI02FailClosedPosseTest` | `rnI02_conf_buscaTambemFalhaDeFormaFechadaQuandoEstoqueIndisponivel` | RED — tipo de exceção incorreto — **atualizado em 2026-09-17**: decisão do stakeholder (Seção 3.4, item 6) substitui a proposta de fail-open do ADR-02 por fail-closed total; substitui o caso antes `@Disabled` |

**Execução (`mvn test -Dtest='RnI*'`):** 4 testes — 0 passaram, 4 falharam/erraram pelo motivo esperado, 0 `@Disabled` (SKIPPED). **Atualizado em 2026-09-17**: caso de RN-I02 antes `@Disabled` convertido em RED definitivo — Decisão confirmada pelo stakeholder (Seção 3.4, item 6) substitui ADR-02/ADR-03 por fail-closed total: indisponibilidade de estoque recusa tanto a busca quanto a confirmação.

### Grupo J — Usabilidade (responsividade, compatibilidade de plataforma)

| RN | Classe de teste | Método (caso) | Status |
|---|---|---|---|
| RN-J01 | `RnJ01ResponsividadeTest` | `rnJ01_cf_renderizacaoEmViewportMinimoDe360PxEFuncionalComLcpAte2_5s` | RED — `UnsupportedOperationException` (ou tipo de exceção incompatível com o stub) |
| RN-J01 | `RnJ01ResponsividadeTest` | `rnJ01_lim_renderizacaoEmViewportMaximoDe1920PxEFuncional` | RED — `UnsupportedOperationException` (ou tipo de exceção incompatível com o stub) |
| RN-J01 | `RnJ01ResponsividadeTest` | `rnJ01_inv_viewportForaDaFaixaSuportadaAbaixoDoMinimo_comportamentoNaoGarantidoPelaFonte` | **SKIPPED** — `@Disabled`, ver motivo na anotação do método (lacuna/ADR/Seção 3 do plano) |
| RN-J02 | `RnJ02CompatibilidadePlataformaTest` | `rnJ02_cf_usoNasDuasVersoesMaisRecentesDeIosAndroidChromeSafariEdgeTemSuporteCompleto` | RED — `UnsupportedOperationException` (ou tipo de exceção incompatível com o stub) |
| RN-J02 | `RnJ02CompatibilidadePlataformaTest` | `rnJ02_inv_usoEmVersaoAnteriorAsDuasUltimasMajors_suporteNaoGarantidoPelaFonte` | **SKIPPED** — `@Disabled`, ver motivo na anotação do método (lacuna/ADR/Seção 3 do plano) |

**Execução (`mvn test -Dtest='RnJ*'`):** 5 testes — 0 passaram, 3 falharam/erraram pelo motivo esperado, 2 `@Disabled` (SKIPPED).

## 4b. Resumo consolidado — cobertura total (Grupos A–J)

Todas as 47 regras de negócio do plano (RN-A01 a RN-J02) têm ao menos um caso de teste JUnit 5 escrito e executado nesta fase Red. Nenhuma regra ficou sem teste.

- **Total de testes executados:** 167 (`mvn test`)
- **RED (falha/erro pelo motivo esperado — funcionalidade ausente):** 145
- **SKIPPED (`@Disabled`, bloqueado por lacuna da Seção 3 ou ADR "proposta, não decisão"):** 22
- **Passaram inesperadamente:** 0 — nenhum indício de regra já implementada ou caso mal escrito

Por grupo:

| Grupo | RNs cobertas | Testes | RED | SKIPPED |
|---|---|---|---|---|
| A | RN-A01–A06 | 29 | 29 | 0 |
| B | RN-B01–B06 | 25 | 21 | 4 |
| C | RN-C01–C09 | 35 | 29 | 6 |
| D | RN-D01–D03 | 9 | 9 | 0 |
| E | RN-E01–E05 | 21 | 18 | 3 |
| F | RN-F01–F05 | 14 | 10 | 4 |
| G | RN-G01–G05 | 15 | 13 | 2 |
| H | RN-H01–H04 | 10 | 9 | 1 |
| I | RN-I01–I02 | 4 | 4 | 0 |
| J | RN-J01–J02 | 5 | 3 | 2 |
| **Total** | **47/47** | **167** | **145** | **22** |

Casos marcados como puro "Gap" na Seção 2 (sem tipo CF/LIM/INV/CONF/PROIB atribuído, ex.: metodologia de cálculo de ranking em RN-F05, janela de recálculo da média móvel em RN-G03) não geraram teste dedicado, por não serem casos de teste — estão registrados como lacunas na Seção 3 do plano, não como cobertura ausente. O valor do SLA de disputa em RN-G05, antes listado aqui como lacuna sem tipo, foi fechado pela Decisão confirmada pelo stakeholder em 2026-09-17 (Seção 3.4, item 5) e agora tem um caso de teste `lim` dedicado.

Compilação (`mvn -q -DskipTests compile`) e suíte completa (`mvn test`) foram executadas ao final com todos os grupos combinados no mesmo projeto Maven, sem conflitos de pacote e sem nenhuma passagem indevida.

### Revalidação contra a Seção 3.4 (2026-09-17)

Esta revalidação cruzou cada uma das 10 decisões confirmadas pelo stakeholder (Seção 3.4) contra o estado real da suíte de testes, encontrando e corrigindo:

- **9 testes `@Disabled` obsoletos convertidos em RED definitivo**, pois as decisões do stakeholder já haviam fechado as lacunas/ADRs que os bloqueavam: RN-A04 (TTL diferenciado por meio de pagamento), RN-A05 (fail-closed total), RN-A06 (rejeição de lote >50), RN-B03 (3 casos — congelamento de preço por sessão, ADR-07 adotado), RN-E02 (100% como meta rígida, ADR-08 rejeitado), RN-H02 (exclusão LGPD prevalece sobre auditoria), RN-I02 (fail-closed também na busca).
- **1 contradição corrigida**: RN-C02 tinha um teste exigindo reautorização de cartão salvo expirado e outro proibindo-a; a Decisão do item 10 (sem fluxo de reautorização dedicado) resolveu o conflito — os dois testes foram substituídos por um único teste consistente.
- **2 casos ausentes adicionados**: RN-B01 (rejeição de rota internacional, item 8) e RN-G05 (baseline de SLA de 15 dias, item 5).
- **1 caso mantido `@Disabled`, porém com justificativa reescrita**: RN-G04 — a Decisão do item 7 resolve RN-H02, mas ela própria declara que isso cria um conflito NOVO e não mitigado com RNF-23 (retenção de auditoria ≥5 anos), que exige escalada formal antes de qualquer critério de aceite definitivo — não é uma resolução silenciosa.

Após as correções, `mvn test` confirma 167 testes executados, 145 RED (0 passagens inesperadas), 22 SKIPPED — todos os `@Disabled` remanescentes citam uma lacuna da Seção 3 ou ADR ainda não decidido não tocado pela Seção 3.4.

## 4. Observação final

Este plano cobre a totalidade dos RF-01…RF-33 e RNF-01…RNF-33 listados em `REQUISITOS/requisitos-funcionais-e-nao-funcionais.md`, a `RESTRIÇÃO-CRÍTICA-01` e o modelo de reserva (`arquitetura.md` §12), além de referenciar as 9 propostas de ADR de `docs/revisao-painel-atam.md` onde elas afetam diretamente um critério de aceite. Casos de teste marcados como dependentes de ADR `[proposta, não decisão]` **não devem ser implementados como asserção definitiva** enquanto a decisão não for formalmente adotada — nesse meio tempo, o ciclo de TDD deve tratá-los como testes pendentes/skipped com referência explícita ao ADR e à pergunta aberta correspondente.

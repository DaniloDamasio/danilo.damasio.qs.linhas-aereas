# Elicitação de Requisitos — Plataforma Nacional de Venda de Passagens Aéreas

## 1. Sobre este documento

Este documento consolida os **Requisitos Funcionais (RF)** e **Requisitos Não Funcionais (RNF)** da plataforma descrita no [README.md](../README.md), derivados diretamente das dores, objetivos e cenários de uso registrados nas personas em [PERSONAS/](../PERSONAS/).

Cada requisito traz:
- **Origem:** qual(is) persona(s) motivou(aram) o requisito.
- **Justificativa:** a dor ou objetivo que o requisito resolve.

Os RNF estão organizados segundo os **8 eixos de qualidade da ISO/IEC 25010**:
1. Adequação Funcional
2. Eficiência de Desempenho
3. Compatibilidade
4. Usabilidade
5. Confiabilidade
6. Segurança
7. Manutenibilidade
8. Portabilidade

---

## 2. Atores considerados

| Ator | Personas mapeadas |
|---|---|
| Passageiro | Viajante de última hora, Viajante econômico, Viajante a negócios, Família/turista planejadora |
| Operação interna | Administradora da plataforma |
| Companhia aérea | Gestor comercial da companhia aérea |
| Agência B2B | Agente de viagens corporativas |

---

## 3. Requisitos Funcionais (RF)

### 3.1 Busca, comparação e precificação

| ID | Requisito | Origem | Justificativa |
|---|---|---|---|
| RF-01 | O sistema deve permitir busca de voos por origem, destino e data/período, com filtro específico para "próximas horas" e "amanhã". | Viajante de última hora | Precisa achar rapidamente voos para viagens não planejadas. |
| RF-02 | O sistema deve exibir um calendário de preços mostrando valores de, no mínimo, ±15 dias em torno da data pesquisada. | Viajante econômico, Família planejadora | Ambas as personas comparam preços entre datas próximas antes de decidir. |
| RF-03 | O sistema deve exibir o preço final (com taxas, bagagem e encargos) já na tela de resultados de busca, sem custos ocultos revelados apenas no checkout. | Viajante econômico, Família planejadora | Dor recorrente de "taxa escondida" que só aparece no checkout. |
| RF-04 | O sistema deve permitir ordenar/filtrar resultados por critérios como menor preço, horário de saída/chegada, número de escalas e política de remarcação. | Viajante a negócios, Viajante de última hora | Necessidade de ordenar por chegada mais cedo/flexibilidade sem comparar manualmente múltiplas abas. |
| RF-05 | O sistema deve indicar de forma visível, antes da compra, se a tarifa permite remarcação/cancelamento sem custo e as regras de multa aplicáveis. | Viajante a negócios | Políticas de remarcação hoje são confusas e não ficam claras antes da compra. |
| RF-06 | O sistema deve permitir configurar alertas de preço por rota/data, notificando o usuário quando houver queda de preço. | Viajante econômico | Precisa ser avisada de promoções sem monitorar manualmente todos os dias. |

### 3.2 Compra e checkout

| ID | Requisito | Origem | Justificativa |
|---|---|---|---|
| RF-07 | O sistema deve permitir concluir uma compra com poucos passos (dados salvos de perfil, cartão salvo), viabilizando checkout em menos de 2–3 minutos. | Viajante de última hora, Viajante a negócios | Ambos exigem velocidade extrema no fechamento da compra. |
| RF-08 | O sistema deve reter/pré-preencher dados cadastrais recorrentes do passageiro (CPF, endereço, forma de pagamento, programa de fidelidade) para compras futuras. | Viajante a negócios | Frustração com formulários repetitivos a cada compra. |
| RF-09 | O sistema deve suportar múltiplos meios de pagamento, incluindo Pix e parcelamento em cartão de crédito, mesmo para valores baixos. | Viajante econômico, Família planejadora | Pagamento restrito é dor explícita; família precisa parcelar em várias vezes. |
| RF-10 | O sistema deve emitir e enviar automaticamente a confirmação da compra e o localizador por e-mail e WhatsApp imediatamente após o pagamento. | Viajante de última hora | Necessidade de confirmação imediata para reduzir ansiedade e permitir embarque. |
| RF-11 | O sistema deve emitir automaticamente a nota fiscal/recibo da compra e disponibilizá-la por e-mail/download, sem necessidade de solicitação manual. | Viajante a negócios | Precisa de nota fiscal para prestação de contas na empresa sem retrabalho. |
| RF-12 | O sistema deve permitir a compra simultânea de múltiplos passageiros (ex: família) em uma única transação, com preenchimento assistido de dados repetidos. | Família planejadora | Preenchimento de dados de 4 passageiros é repetitivo e cansativo hoje. |
| RF-13 | O sistema deve permitir a seleção de assentos no mapa da aeronave, indicando a possibilidade de reservar assentos lado a lado para grupos/famílias. | Família planejadora | Medo de a família ficar espalhada pelo avião. |
| RF-14 | O sistema deve exibir de forma clara, antes da confirmação da compra, o que está incluso na tarifa (bagagem de mão, despachada) e opções de add-ons sem indução a upsell excessivo. | Viajante econômico, Família planejadora | Dor com interface poluída de upsell e taxas de bagagem não explicitadas. |
| RF-15 | O sistema deve apresentar regras de documentação exigida para crianças/menores de idade durante o fluxo de compra. | Família planejadora | Insegurança sobre documentos necessários para os filhos. |

### 3.3 Pós-venda, suporte e alterações

| ID | Requisito | Origem | Justificativa |
|---|---|---|---|
| RF-16 | O sistema deve disponibilizar canal de suporte (chat/telefone) acessível 24h a partir de qualquer etapa da compra ou pós-venda. | Viajante de última hora | Falta de canal de suporte imediato em caso de erro/dúvida. |
| RF-17 | O sistema deve permitir remarcação e cancelamento de passagens de forma self-service, exibindo custos e prazos antes da confirmação da alteração. | Viajante a negócios | Necessidade de remarcar/cancelar rapidamente quando a reunião muda de data. |
| RF-18 | O sistema deve notificar automaticamente o passageiro (e o agente responsável, quando aplicável) em caso de alteração ou cancelamento de voo pela companhia aérea. | Agente de viagens corporativas | Hoje precisa monitorar manualmente mudanças de voo sem notificação automática. |

### 3.4 B2B — Agências de viagens corporativas

| ID | Requisito | Origem | Justificativa |
|---|---|---|---|
| RF-19 | O sistema deve prover um painel/perfil de agente que permita operar em nome de terceiros (funcionários de empresas clientes). | Agente de viagens corporativas | Compra sempre em nome de terceiros, com necessidade de contexto por empresa cliente. |
| RF-20 | O sistema deve permitir o cadastro de políticas de viagem por empresa cliente (classe permitida, teto de gasto por trecho, companhias credenciadas) e aplicá-las automaticamente na busca/sugestão de voos. | Agente de viagens corporativas | Plataformas hoje não diferenciam regras entre empresas clientes distintas. |
| RF-21 | O sistema deve permitir a emissão de passagens em lote, a partir de uma lista/planilha de passageiros, para múltiplos beneficiários de uma mesma empresa. | Agente de viagens corporativas | Falta de compra em lote obriga repetição manual do processo por passageiro. |
| RF-22 | O sistema deve expor uma API para integração com sistemas internos das agências (busca e reserva programáticas). | Agente de viagens corporativas | Falta de integração via API gera retrabalho de copiar/colar dados. |
| RF-23 | O sistema deve gerar faturamento consolidado (uma única fatura/nota por empresa cliente, agregando múltiplas reservas). | Agente de viagens corporativas | Faturamento fragmentado por passagem não atende ao fluxo financeiro da agência. |

### 3.5 Companhias aéreas parceiras

| ID | Requisito | Origem | Justificativa |
|---|---|---|---|
| RF-24 | O sistema deve prover painel administrativo para companhias aéreas cadastrarem e atualizarem rotas, horários, classes de assento e tarifas. | Gestor comercial da companhia aérea | Necessidade de manter cadastro atualizado sem processos manuais múltiplos. |
| RF-25 | O sistema deve permitir a criação e configuração de campanhas promocionais (rota, período de vigência, quantidade de assentos promocionais). | Gestor comercial da companhia aérea | Precisa lançar promoções específicas de forma simples e rápida. |
| RF-26 | O sistema deve prover API/integração para sincronização em tempo real do estoque de assentos entre o sistema da companhia e a plataforma, prevenindo overbooking. | Gestor comercial da companhia aérea | Falta de sincronização em tempo real é dor explícita e risco de overbooking. |
| RF-27 | O sistema deve disponibilizar dashboard de desempenho comercial (volume de vendas, receita, ticket médio, taxa de conversão) por rota e período. | Gestor comercial da companhia aérea | Dashboards limitados hoje impedem entender queda de vendas por rota. |
| RF-28 | O sistema deve exibir ao gestor comercial informações de posicionamento competitivo (ex.: ranking de exibição/preço frente a concorrentes) dentro do marketplace. | Gestor comercial da companhia aérea | Falta de visibilidade sobre posicionamento frente a concorrentes. |

### 3.6 Operação interna / administração da plataforma

| ID | Requisito | Origem | Justificativa |
|---|---|---|---|
| RF-29 | O sistema deve prover um fluxo padronizado de onboarding de novas companhias aéreas parceiras (documentação, integração técnica, homologação). | Administradora da plataforma, Gestor comercial da companhia aérea | Onboarding hoje não é padronizado e é lento para ambos os lados. |
| RF-30 | O sistema deve consolidar em um painel único indicadores de saúde da operação por companhia parceira: taxa de fraude, disputas/chargebacks, SLA de atendimento. | Administradora da plataforma | Falta de visão consolidada obriga acessar sistemas separados. |
| RF-31 | O sistema deve gerar alertas automáticos de padrões anômalos (ex.: pico de reembolsos, indícios de fraude) por companhia/rota. | Administradora da plataforma | Detecção de fraude hoje é reativa, só percebida após o prejuízo. |
| RF-32 | O sistema deve automatizar a conciliação financeira (repasses e comissões) entre a plataforma e cada companhia aérea parceira, com relatório consolidado por período. | Administradora da plataforma | Conciliação manual via cruzamento de planilhas é dor central da persona. |
| RF-33 | O sistema deve prover um fluxo de mediação/registro de disputas de reembolso entre passageiro e companhia aérea, com acompanhamento de SLA contratual. | Administradora da plataforma | Falta de fluxo claro de mediação gera atritos e escalonamento de reclamações. |

---

## 4. Requisitos Não Funcionais (RNF) — por eixo da ISO/IEC 25010

> Todos os requisitos abaixo trazem uma **métrica exata e verificável**. Onde o valor depende de definição de negócio ainda não formalizada nas personas, ele é marcado como **[baseline sugerida]** — um ponto de partida razoável para validação com stakeholders, não um dado extraído literalmente das personas.

### 4.1 Adequação Funcional (*Functional Suitability*)

| ID | Requisito (com métrica) | Origem/Justificativa |
|---|---|---|
| RNF-01 | O preço exibido na tela de busca/resultados deve ter **divergência de 0%** em relação ao valor cobrado no checkout, exceto variação de tarifa dinâmica, limitada a **atualizações a cada ≥5 minutos** e sinalizada ao usuário antes da confirmação. | Viajante econômico, Família planejadora — taxas escondidas quebram a confiança no resultado da busca. |
| RNF-02 | O motor de política de viagem B2B deve bloquear ou sinalizar **100% das reservas fora de política** antes da confirmação, com taxa de falso-negativo (reserva fora de política que passa despercebida) **≤ 0,1% das transações/mês**. | Agente de viagens corporativas — erro aqui gera reserva fora de política e retrabalho financeiro. |
| RNF-03 | O estoque de assentos exibido ao passageiro deve ter **divergência máxima de 0 assentos** (zero tolerância) em relação ao estoque real da companhia aérea, com reconciliação event-driven em **≤ 2 segundos** após qualquer venda. | Gestor comercial da companhia aérea — overbooking por dessincronização é risco de negócio crítico. |

### 4.2 Eficiência de Desempenho (*Performance Efficiency*)

| ID | Requisito (com métrica) | Origem/Justificativa |
|---|---|---|
| RNF-04 | A busca de voos deve responder em **≤ 3 segundos (P95)** e **≤ 5 segundos (P99)**, suportando ao menos **500 buscas simultâneas** sem degradação. **[baseline sugerida]** | Viajante de última hora — decisão sob estresse exige resposta quase instantânea. |
| RNF-05 | O checkout completo (da seleção do voo à confirmação de pagamento), com perfil e cartão salvos, deve ser concluído em **até 120 segundos**, em **no máximo 4 telas/etapas**. | Viajante de última hora, Viajante a negócios — meta explícita de "menos de 2–3 minutos". |
| RNF-06 | Atualizações de estoque de assentos entre a companhia aérea e a plataforma devem propagar em **≤ 2 segundos (P95)** e **≤ 5 segundos (P99)**. | Gestor comercial da companhia aérea — evitar overbooking por atraso de sincronização. |
| RNF-07 | Dashboards (vendas, conciliação, fraude) devem carregar em **≤ 4 segundos** para períodos de até **12 meses** de dados históricos, sem timeout para consultas de até **90 dias** por página. **[baseline sugerida]** | Administradora da plataforma, Gestor comercial da companhia aérea. |
| RNF-08 | A emissão em lote deve processar **até 50 passageiros em ≤ 30 segundos**, com atualização de progresso a cada **≤ 2 segundos**. **[baseline sugerida, com base no cenário de 8 passageiros da persona]** | Agente de viagens corporativas. |

### 4.3 Compatibilidade (*Compatibility*)

| ID | Requisito (com métrica) | Origem/Justificativa |
|---|---|---|
| RNF-09 | A API para agências B2B deve ter disponibilidade **≥ 99,9% ao mês** (≤ 43 min de indisponibilidade/mês), latência **≤ 500ms (P95)** e documentação OpenAPI 3.0 publicada. | Agente de viagens corporativas — falta de integração via API é dor explícita. |
| RNF-10 | Webhooks de sincronização de estoque com companhias aéreas devem realizar **no máximo 3 tentativas de reenvio** em falha, com backoff de **1s / 5s / 30s** e timeout de **10 segundos** por tentativa. | Gestor comercial da companhia aérea. |
| RNF-11 | Notificações multicanal (e-mail + WhatsApp) devem ser entregues em **≤ 30 segundos** após o evento gerador, em **≥ 99% dos casos/mês**. | Viajante de última hora, Agente de viagens corporativas. |
| RNF-12 | A nota fiscal individual deve ser emitida em **≤ 60 segundos** após confirmação do pagamento; a fatura consolidada B2B deve ser gerada até o **1º dia útil do mês seguinte** ao período faturado. | Viajante a negócios, Agente de viagens corporativas. |

### 4.4 Usabilidade (*Usability*)

| ID | Requisito (com métrica) | Origem/Justificativa |
|---|---|---|
| RNF-13 | O checkout de usuário com perfil salvo deve exigir **no máximo 3 toques/cliques** entre a seleção do voo e a confirmação do pagamento, com **no máximo 2 campos obrigatórios adicionais** (ex.: CVV e aceite de termos). | Viajante de última hora — dor com "cadastros completos" que atrasam quem só quer comprar rápido. |
| RNF-14 | O preço total (com taxas e bagagem incluída) deve ser exibido na listagem de resultados com **fonte de tamanho ≥ 90% da fonte usada no preço base**, sem exigir rolagem ou clique adicional para ser visto. Diferença entre preço anunciado na busca e preço final no checkout: **0%**. | Viajante econômico, Família planejadora. |
| RNF-15 | A tela de checkout deve exibir **no máximo 3 ofertas de upsell** (ex.: seguro-viagem, bagagem extra, assento preferencial) por padrão, **nenhuma pré-marcada/pré-selecionada** (rege opt-in explícito), com opção de ocultar todas em **1 clique**. | Viajante econômico — interface poluída com excesso de ofertas e upsells. |
| RNF-16 | Um usuário não técnico deve conseguir cadastrar uma nova tarifa/promoção no painel comercial em **≤ 5 minutos**, após treinamento de **≤ 30 minutos**, com pontuação **SUS (System Usability Scale) ≥ 70** em testes de usabilidade. **[baseline sugerida]** | Gestor comercial da companhia aérea — "não é técnico, depende de painéis simples". |
| RNF-17 | O cadastro de passageiros adicionais (2º ao Nº passageiro de uma mesma compra) deve reduzir em **≥ 70% o número de campos preenchidos manualmente** em relação ao 1º passageiro (via cópia de endereço/forma de pagamento). | Família planejadora, Agente de viagens corporativas. |
| RNF-18 | A interface do passageiro deve ser responsiva de **360px a 1920px** de largura, com **LCP (Largest Contentful Paint) ≤ 2,5s** em conexão 4G simulada. | Todas as personas de passageiro (uso majoritariamente por celular). |

### 4.5 Confiabilidade (*Reliability*)

| ID | Requisito (com métrica) | Origem/Justificativa |
|---|---|---|
| RNF-19 | Toda reserva com pagamento aprovado deve ser persistida de forma durável, com **0 casos de perda de reserva paga por falha técnica** e confirmação positiva exibida/enviada em **≤ 5 segundos** após aprovação do pagamento. | Viajante de última hora — ansiedade explícita sobre "se a compra foi realmente confirmada". |
| RNF-20 | Os fluxos de busca e checkout devem ter disponibilidade **≥ 99,9% ao mês** (≤ 43 min de indisponibilidade/mês). | Viajante de última hora — viagens de emergência não podem esperar indisponibilidade do sistema. |
| RNF-21 | O canal de suporte deve operar **24 horas/dia, 7 dias/semana, 365 dias/ano**, com tempo médio de primeira resposta no chat **≤ 2 minutos**. **[baseline sugerida para o tempo de resposta]** | Viajante de última hora — necessidade explícita de suporte a qualquer hora. |
| RNF-22 | Falhas de sincronização de estoque devem ter **RTO (tempo de recuperação) ≤ 5 minutos** e **RPO (perda de dados) ≤ 1 minuto**. **[baseline sugerida]** | Gestor comercial da companhia aérea. |
| RNF-23 | O reprocessamento da conciliação financeira deve ser idempotente, garantindo **0% de lançamentos duplicados ou perdidos**, com trilha de auditoria imutável retida por **≥ 5 anos**. | Administradora da plataforma. |

### 4.6 Segurança (*Security*)

| ID | Requisito (com métrica) | Origem/Justificativa |
|---|---|---|
| RNF-24 | Dados de pagamento devem seguir **PCI-DSS**, com **100% dos números de cartão tokenizados** (nenhum PAN armazenado em texto claro). | Viajante a negócios, Viajante de última hora — armazenamento de cartão salvo para compra rápida. |
| RNF-25 | Dados pessoais (incluindo de menores de idade) devem seguir a **LGPD**, com exclusão de dados a pedido do titular em **≤ 15 dias corridos** e resposta a solicitações de titular em **≤ 15 dias** (prazo legal). | Família planejadora — dados de documentação de crianças. |
| RNF-26 | Alertas de fraude devem ser disparados em **≤ 15 minutos** após detecção de anomalia, definida como volume de reembolsos **≥ 3x a média móvel dos últimos 7 dias** para a mesma rota/companhia. | Administradora da plataforma — detecção hoje é reativa. |
| RNF-27 | O controle de acesso (RBAC) deve garantir **0% de vazamento de dados cross-tenant** (um agente/companhia não acessa dados de terceiros), validado por auditoria de segurança **trimestral**. | Agente de viagens corporativas, Gestor comercial da companhia aérea. |
| RNF-28 | APIs para parceiros devem exigir autenticação **OAuth2**, com tokens de acesso expirando em **≤ 1 hora**, e logs de auditoria retidos por **≥ 5 anos**. | Agente de viagens corporativas, Gestor comercial da companhia aérea. |

### 4.7 Manutenibilidade (*Maintainability*)

| ID | Requisito (com métrica) | Origem/Justificativa |
|---|---|---|
| RNF-29 | O onboarding técnico completo de uma nova companhia aérea (documentação, integração, homologação) deve ser concluído em **≤ 10 dias úteis**, via fluxo padronizado. **[baseline sugerida]** | Administradora da plataforma, Gestor comercial da companhia aérea — onboarding hoje não padronizado e lento. |
| RNF-30 | A criação/alteração de política de viagem de uma empresa cliente B2B deve ser feita via configuração (sem deploy de código), refletindo em produção em **≤ 5 minutos**. | Agente de viagens corporativas. |
| RNF-31 | A publicação de uma nova tarifa/promoção pelo gestor comercial deve entrar em vigor em **≤ 1 minuto** após confirmação, sem intervenção do time técnico da plataforma. | Gestor comercial da companhia aérea — hoje depende de múltiplos passos manuais. |

### 4.8 Portabilidade (*Portability*)

| ID | Requisito (com métrica) | Origem/Justificativa |
|---|---|---|
| RNF-32 | A aplicação do passageiro deve suportar as **últimas 2 versões majors** de iOS e Android, e as **últimas 2 versões** dos navegadores Chrome, Safari e Edge. | Todas as personas de passageiro — uso predominante via celular no dia a dia. |
| RNF-33 | As APIs de integração (companhias aéreas e agências B2B) devem ser versionadas explicitamente (ex.: `/v1/`), mantendo compatibilidade retroativa por **≥ 12 meses** após publicação de uma nova versão. | Gestor comercial da companhia aérea, Agente de viagens corporativas — parceiros usam sistemas próprios variados (GDS, sistemas internos). |

---

## 5. Rastreabilidade — resumo por persona

| Persona | RF relacionados | RNF relacionados |
|---|---|---|
| Viajante de última hora | RF-01, RF-04, RF-07, RF-10, RF-16 | RNF-04, RNF-05, RNF-13, RNF-19, RNF-20, RNF-21, RNF-24 |
| Viajante econômico | RF-02, RF-03, RF-06, RF-09, RF-14 | RNF-01, RNF-14, RNF-15 |
| Viajante a negócios | RF-04, RF-05, RF-07, RF-08, RF-09, RF-11, RF-17 | RNF-05, RNF-24 |
| Família/turista planejadora | RF-02, RF-03, RF-09, RF-12, RF-13, RF-14, RF-15 | RNF-01, RNF-14, RNF-17, RNF-25 |
| Administradora da plataforma | RF-29, RF-30, RF-31, RF-32, RF-33 | RNF-23, RNF-26, RNF-29 |
| Gestor comercial da companhia aérea | RF-24, RF-25, RF-26, RF-27, RF-28, RF-29 | RNF-03, RNF-06, RNF-07, RNF-10, RNF-16, RNF-22, RNF-27, RNF-31, RNF-33 |
| Agente de viagens corporativas | RF-18, RF-19, RF-20, RF-21, RF-22, RF-23 | RNF-02, RNF-08, RNF-09, RNF-17, RNF-27, RNF-28, RNF-30, RNF-33 |

---

## 6. Observações

- Este documento é uma **elicitação inicial**, servindo de base para priorização (ex.: MoSCoW) e detalhamento posterior em histórias de usuário/casos de uso.
- Requisitos de conformidade regulatória de aviação civil (ANAC) não foram detalhados aqui por não constarem explicitamente nas personas, mas devem ser validados como restrição adicional em fase de refinamento.

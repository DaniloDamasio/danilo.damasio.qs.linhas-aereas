# Avaliação ATAM — Plataforma Nacional de Venda de Passagens Aéreas

> **Natureza deste documento.** Relatório de **equipe avaliadora ATAM independente** (Architecture Tradeoff Analysis Method). Ele **acrescenta** análise ao conjunto de evidências existente — [README.md](../README.md), [REQUISITOS/requisitos-funcionais-e-nao-funcionais.md](../REQUISITOS/requisitos-funcionais-e-nao-funcionais.md), [PERSONAS/](../PERSONAS/) e [docs/arquitetura.md](arquitetura.md) — **sem alterar, corrigir ou apagar** nenhum deles. Conflitos e lacunas já registrados em [arquitetura.md §8](arquitetura.md#8-fatos-lacunas-conflitos-suposições-e-perguntas-abertas) permanecem como estão; quando esta avaliação os toca, ela os **referencia**, não os resolve.
>
> **Regra de evidência adotada pela equipe avaliadora.**
> - Um número só é tratado como **requisito** se estiver literalmente em RF-xx/RNF-xx.
> - Todo valor marcado `[baseline sugerida]` na fonte é **hipótese pendente**, não meta acordada (a própria fonte diz isso em [REQUISITOS §4](../REQUISITOS/requisitos-funcionais-e-nao-funcionais.md), nota introdutória; e [arquitetura.md §8.3 S-01](arquitetura.md#83-suposicoes)).
> - Qualquer número **necessário para avaliar um cenário e ausente da fonte** é registrado como **hipótese `A-H-xx`** (§8), nunca como fato. Nenhuma hipótese foi usada para declarar um cenário "atendido".
> - **Nenhuma implementação foi feita ou proposta como pronta.** As abordagens de §3 são leituras do que a arquitetura documentada já decidiu, mais alternativas para discussão.
>
> **Estado da avaliação.** Esta é uma ATAM de **Fase 1 sem workshop com stakeholders**: a priorização de cenários (§5) foi feita pela equipe avaliadora a partir da linguagem de criticidade dos artefatos, e **não substitui a votação de stakeholders** prevista no método. Ver [A-RSK-14](#7-riscos).

---

## 1. Escopo, entradas e limites da avaliação

| Item | Conteúdo |
|---|---|
| **Artefatos avaliados** | README.md; REQUISITOS (RF-01…RF-33, RNF-01…RNF-33); 7 personas; docs/arquitetura.md §0–§13 (incl. C4, sequências e o modelo de reserva §12). |
| **Artefatos ausentes** (limitam a profundidade) | Não há código, ADRs, protótipo, dados de produção, contratos com companhias, nem escolha de stack ([arquitetura.md L-01, L-06](arquitetura.md#81-lacunas)). |
| **Consequência metodológica** | A avaliação é sobre **arquitetura documentada**, não sobre sistema construído. Nenhuma afirmação de conformidade é feita — apenas identificação de sensibilidade, trade-off e risco. |
| **Fora de escopo** | Regulação ANAC (L-05), rotas internacionais (S-03), modelo de "pacotes" (L-08), escolha de tecnologia (L-01). |

**Observação da equipe avaliadora sobre um vazio de cobertura.** O enunciado desta avaliação exige examinar comportamentos de **operação contínua** — execução diária, chegada de dados novos, falha parcial, jobs sobrepostos, degradação de dados, candidato pior, rollback, indisponibilidade e vazamento. Confrontando isso com a fonte: os RNF cobrem bem **latência, disponibilidade e integridade pontual**, mas **não há um único requisito que enderece explicitamente sobreposição de execução de jobs, rollback de publicação, ou detecção de degradação silenciosa de dados**. Isso não é falha desta avaliação nem da arquitetura — é um **vazio de elicitação** que a avaliação expõe e registra como tema de risco [A-TR-03](#10-temas-de-risco).

---

## 2. Drivers de negócio (reconstruídos pela equipe avaliadora)

Reconstruídos a partir do README, das dores/objetivos das personas e da restrição de [arquitetura.md §0](arquitetura.md#0-restrição-inegociável-registro-obrigatório). Numerados `A-DR-xx` para não colidir com a numeração de [arquitetura.md §5](arquitetura.md#5-drivers-priorizados), da qual são uma leitura independente.

| ID | Driver de negócio | Evidência | Leitura da equipe avaliadora |
|---|---|---|---|
| **A-DR-01** | **Confiança transacional absoluta:** um assento vendido é um assento existente e único. | RESTRIÇÃO-CRÍTICA-01; RNF-03 (0 assentos); RF-26; [gestor-comercial](../PERSONAS/companhias-aereas/gestor-comercial-companhia-aerea.md) | É o único driver declarado como **inviolável**. Tudo o mais é negociável contra ele — inclusive desempenho. Dita a arquitetura mais do que qualquer outro item. |
| **A-DR-02** | **Confiança comercial:** o preço visto é o preço pago. | RNF-01, RNF-14, RF-03; [viajante-econômico](../PERSONAS/passageiros/viajante-economico.md), [família](../PERSONAS/passageiros/familia-turista-planejadora.md) | Dor central de 2 das 4 personas de passageiro. A fonte contém uma contradição aqui (C-02), que a avaliação trata como **sensibilidade não resolvida**. |
| **A-DR-03** | **Viabilidade de marketplace multi-parceiro:** N companhias heterogêneas integradas sem que a variabilidade delas contamine a plataforma. | RF-24…RF-29, RNF-10, RNF-22, RNF-29, RNF-33; [administradora](../PERSONAS/operacao-interna/administrador-da-plataforma.md) ("cada nova companhia exige um processo diferente") | O modelo de negócio **é** a integração. A qualidade da plataforma é limitada pela pior companhia integrada — ponto pouco endereçado na fonte. |
| **A-DR-04** | **Confiança institucional B2B:** agência e empresa cliente confiam dados e regras a um terceiro. | RNF-27 (0% cross-tenant), RNF-02, RF-19, RF-20, RNF-28 | Falha aqui é evento de perda de contrato e exposição legal, não bug. |
| **A-DR-05** | **Integridade financeira:** dinheiro conferido, repassado e comprovado. | RF-32, RNF-23 (0% duplicado/perdido, auditoria ≥ 5 anos), RF-11, RF-23, RNF-12 | Único domínio da fonte com exigência explícita de **idempotência**. |
| **A-DR-06** | **Velocidade percebida sob estresse:** urgência é o contexto de compra de duas personas. | RNF-05, RNF-13, RNF-04*, RNF-20; [última-hora](../PERSONAS/passageiros/viajante-ultima-hora.md), [negócios](../PERSONAS/passageiros/viajante-a-negocios.md) | Driver que **compete diretamente** com A-DR-01 e A-DR-02. Fonte principal de trade-off. |
| **A-DR-07** | **Autonomia operacional do parceiro:** publicar tarifa/política sem time técnico. | RNF-30 (≤ 5 min), RNF-31 (≤ 1 min), RF-24, RF-25 | Autonomia de publicação sem rollback declarado é o vazio mais evidente da fonte (ver A-CN-07). |

`*` contém valor `[baseline sugerida]`.

---

## 3. Atributos de qualidade prioritários e abordagens arquiteturais identificadas

### 3.1 Ordem de prioridade adotada nesta avaliação

A equipe avaliadora adota a seguinte ordem, **derivada** (não elicitada — ver [A-RSK-14](#7-riscos)):

1. **Integridade de estoque / não-overbooking** (única restrição declarada absoluta)
2. **Integridade financeira e de reserva paga** (metas de zero)
3. **Segurança e isolamento multi-tenant** (meta de zero + risco legal)
4. **Correção de preço e de política** (metas de 0% e 100%)
5. **Disponibilidade** (99,9%)
6. **Desempenho** (P95/P99)
7. **Manutenibilidade / operabilidade** (publicação, onboarding, rollback)
8. **Usabilidade e portabilidade**

Manutenibilidade aparece em 7º pela **firmeza dos requisitos**, não pela importância real: a avaliação conclui em §10 que ela é, de fato, o tema de risco mais desatendido.

### 3.2 Abordagens arquiteturais identificadas na arquitetura documentada

Cada abordagem `A-AB-xx` é uma decisão **já presente** em [arquitetura.md](arquitetura.md); a coluna "Estado" diz se a fonte a exige ou se é escolha de projeto.

| ID | Abordagem | Onde está | Estado | Atributos que promove | Atributos que tensiona |
|---|---|---|---|---|---|
| **A-AB-01** | **Ponto único de decisão de disponibilidade** — o Serviço de Estoque é a única escrita autorizada sobre assento. | [§9](arquitetura.md#9-responsabilidades-limites-e-interfaces-dos-componentes), [§12.1](arquitetura.md#121-entidades) | Derivada de RESTRIÇÃO-CRÍTICA-01 | Integridade, auditabilidade, verificabilidade | Disponibilidade (ponto de concentração), desempenho |
| **A-AB-02** | **Exclusão mútua por escrita condicional (CAS) / unique constraint em `(voo_id, assento_id)`**, com rejeição determinística do perdedor. | [§12.3](arquitetura.md#123-regra-decisiva-de-posse-mecanismo-de-exclusão-mútua) | **[DECISÃO-SEM-REQUISITO]** quanto à técnica | Integridade sob concorrência | Latência sob contenção; acoplamento ao armazenamento escolhido (L-01) |
| **A-AB-03** | **HOLD com TTL antes do pagamento** + expiração lazy + sweeper ativo. | [§12.2](arquitetura.md#122-máquina-de-estados-do-assento), [§12.4](arquitetura.md#124-expiração-do-hold) | Mecanismo derivado; **TTL indefinido (L-07)** | Certeza de assento antes do pagamento | Estoque percebido (holds abandonados), checkout ≤120s |
| **A-AB-04** | **Reconciliação event-driven** com barramento de eventos. | [§9](arquitetura.md#9-responsabilidades-limites-e-interfaces-dos-componentes), [§10.2](arquitetura.md#102-nível-2--contêineres) | "event-driven" é **literal** em RNF-03; tecnologia não | Propagação ≤2s, desacoplamento | Consistência eventual, ordenação, duplicação de eventos |
| **A-AB-05** | **Adaptador por companhia + webhooks com retry/backoff** (3 tentativas, 1s/5s/30s, timeout 10s). | [§9](arquitetura.md#9-responsabilidades-limites-e-interfaces-dos-componentes), RNF-10 | **Requisito literal** | Isolamento da variabilidade do parceiro | Perda silenciosa após a 3ª tentativa (ver A-CN-03) |
| **A-AB-06** | **Congelamento de preço em sessão** com janela de tarifa dinâmica ≥5 min sinalizada. | [§9](arquitetura.md#9-responsabilidades-limites-e-interfaces-dos-componentes) (Busca & Precificação), RNF-01 | Requisito com **exceção conflitante** (C-02) | Transparência de preço | Receita/paridade tarifária do parceiro |
| **A-AB-07** | **Motor de políticas B2B configurável sem deploy**, aplicado em busca e no commit. | [§9](arquitetura.md#9-responsabilidades-limites-e-interfaces-dos-componentes), RF-20, RNF-02, RNF-30 | Requisito literal | Correção B2B, agilidade de mudança | Superfície de erro de configuração sem gate técnico |
| **A-AB-08** | **Armazenamento por tenant + RBAC**, meta de 0% cross-tenant, auditoria trimestral. | [§9](arquitetura.md#9-responsabilidades-limites-e-interfaces-dos-componentes), RNF-27 | Requisito literal | Isolamento | Custo de agregação cross-tenant para painéis internos (RF-30) |
| **A-AB-09** | **Reuso do mecanismo de reserva no lote B2B** (1 item = 1 tentativa de HOLD/CONFIRM; falha individual reportada). | [§9](arquitetura.md#9-responsabilidades-limites-e-interfaces-dos-componentes), [§11.3](arquitetura.md#113-fluxo-batch--emissão-em-lote-b2b) | Derivada; protege RESTRIÇÃO-CRÍTICA-01 | Integridade uniforme entre canais | Latência do lote (RNF-08*), atomicidade parcial do lote |
| **A-AB-10** | **Conciliação em batch idempotente** por chave de idempotência, trilha imutável ≥5 anos. | [§11.4](arquitetura.md#114-fluxo-batch--conciliação-financeira), RF-32, RNF-23 | Requisito literal | Integridade financeira, auditabilidade | Custo de retenção; janela de reprocessamento |
| **A-AB-11** | **APIs versionadas com retrocompatibilidade ≥12 meses**; OAuth2 com token ≤1h. | RNF-33, RNF-28, RNF-09 | Requisito literal | Evolução sem quebrar parceiro | Manutenção de N versões simultâneas |

### 3.3 Abordagens **ausentes** que a avaliação esperava encontrar

Registradas como ausência, não como recomendação de implementação:

| Ausência | Por que a avaliação a esperava | Vira |
|---|---|---|
| Controle de **execução única / exclusão mútua entre jobs** (conciliação, sweeper, lote) | RNF-23 exige idempotência de reprocessamento, mas nada impede duas execuções concorrentes do mesmo job | [A-CN-04](#41-cenários-de-caso-de-uso-e-de-operação), [A-RSK-04](#7-riscos) |
| **Rollback** de publicação de tarifa/política | RNF-30/RNF-31 exigem publicação rápida sem time técnico; nada define desfazer | [A-CN-07](#41-cenários-de-caso-de-uso-e-de-operação), [A-RSK-07](#7-riscos) |
| **Detecção de degradação silenciosa** (dado stale mas presente) | RNF-03/RNF-06 endereçam atraso e divergência, não dado plausível-porém-velho | [A-CN-05](#41-cenários-de-caso-de-uso-e-de-operação), [A-RSK-05](#7-riscos) |
| **Critério de aceitação de "candidato"** (tarifa/política/ranking novo pior que o vigente) | RF-25/RF-28/RNF-31 permitem publicar; nada define avaliar antes ou reverter depois | [A-CN-06](#41-cenários-de-caso-de-uso-e-de-operação), [A-RSK-06](#7-riscos) |
| **Modo degradado explícito** de busca/checkout | RNF-20 exige 99,9%, mas não diz o que o sistema faz nos 0,1% | [A-CN-08](#41-cenários-de-caso-de-uso-e-de-operação), [A-RSK-08](#7-riscos) |

---

## 4. Árvore de utilidade (utility tree)

Formato: `(Importância para o negócio, Dificuldade arquitetural)`, cada uma **A** (alta), **M** (média), **B** (baixa) — convenção ATAM. Importância derivada dos drivers §2; dificuldade estimada pela equipe avaliadora. Cenários detalhados em §4.1.

```
UTILIDADE
│
├── Integridade de estoque (não-overbooking) ─── A-DR-01
│   ├── Exclusão mútua sob concorrência ................ A-CN-01  (A, A)
│   ├── Sincronização com sistema da companhia ......... A-CN-02  (A, A)
│   ├── Resiliência a falha parcial de parceiro ........ A-CN-03  (A, A)
│   └── Estoque não degradar silenciosamente ........... A-CN-05  (A, A)
│
├── Integridade financeira e de reserva ─── A-DR-05
│   ├── Execução diária de conciliação ................. A-CN-04a (A, M)
│   ├── Jobs sobrepostos / reexecução .................. A-CN-04  (A, A)
│   └── Reserva paga nunca perdida ..................... A-CN-11  (A, M)
│
├── Segurança e isolamento ─── A-DR-04
│   ├── Vazamento cross-tenant ......................... A-CN-09  (A, A)
│   └── Dado de menor / LGPD / PCI ..................... A-CN-10  (A, M)
│
├── Correção comercial ─── A-DR-02, A-DR-04
│   ├── Preço busca ↔ checkout ......................... A-CN-12  (A, M)
│   └── Bloqueio de reserva fora de política ........... A-CN-13  (A, M)
│
├── Disponibilidade ─── A-DR-06
│   ├── Indisponibilidade de busca/checkout ............ A-CN-08  (A, A)
│   └── Indisponibilidade da API B2B ................... A-CN-08b (M, M)
│
├── Desempenho ─── A-DR-06
│   ├── Busca sob carga ................................ A-CN-14  (M, M)*
│   └── Checkout ≤120s sob contenção de assento ........ A-CN-15  (A, A)
│
├── Operabilidade e evolução ─── A-DR-03, A-DR-07
│   ├── Entrada de dados novos (nova companhia/rota) ... A-CN-16  (A, A)
│   ├── Candidato pior que o vigente ................... A-CN-06  (M, A)
│   ├── Rollback de publicação ......................... A-CN-07  (A, A)
│   └── Evolução de API sem quebrar parceiro ........... A-CN-17  (M, M)
│
└── Usabilidade e portabilidade
    ├── Checkout ≤3 toques ............................. A-CN-18  (M, B)
    └── Faixa de dispositivos/navegadores .............. A-CN-19  (B, B)
```

`*` A-CN-14 depende de RNF-04 `[baseline sugerida]` — importância M **por firmeza de evidência**, não por irrelevância.

### 4.1 Cenários de caso de uso e de operação

Formato ATAM de 6 partes. A coluna **Métrica** distingue: `[RNF-xx]` = meta da fonte; `[baseline]` = valor marcado como sugerido na fonte; `[A-H-xx]` = **hipótese da avaliação, pendente de validação** — nenhuma delas é meta acordada.

---

#### A-CN-01 — Concorrência por assento *(Importância A / Dificuldade A)*

| Parte | Conteúdo |
|---|---|
| **Fonte** | Dois ou mais passageiros (ou um passageiro e um item de lote B2B) |
| **Estímulo** | Tentativas de HOLD do **mesmo** `(voo, assento)` dentro da mesma janela de commit |
| **Ambiente** | Operação normal; voo de alta demanda; canais mistos (app, web, API B2B) |
| **Artefato** | Serviço de Estoque (Seat Repo + CAS Guard), Reserva & Checkout ([§12.3](arquitetura.md#123-regra-decisiva-de-posse-mecanismo-de-exclusão-mútua)) |
| **Resposta** | Exatamente uma tentativa obtém HOLD; as demais recebem rejeição **determinística** ("assento indisponível") e reseleção; nenhuma venda dupla é confirmada em nenhuma ordem de chegada |
| **Métrica** | Divergência de estoque **0 assentos** `[RNF-03]`; vendas confirmadas duplicadas **0** `[RESTRIÇÃO-CRÍTICA-01]`; latência adicional da rejeição sob contenção **não definida na fonte** `[A-H-01]` |

---

#### A-CN-02 — Propagação de venda externa da companhia *(A / A)*

| Parte | Conteúdo |
|---|---|
| **Fonte** | Sistema interno da companhia aérea (venda pelo canal próprio da companhia) |
| **Estímulo** | Assento vendido **fora** da plataforma; evento de estoque chega via webhook |
| **Ambiente** | Operação normal, N companhias com maturidade técnica heterogênea |
| **Artefato** | Adaptador de Integração, Stock Sync API, Serviço de Estoque, Barramento de Eventos |
| **Resposta** | Estoque exibido reflete a venda externa antes que outro passageiro consiga confirmar o mesmo assento |
| **Métrica** | Propagação **≤2s P95 / ≤5s P99** `[RNF-06]`; reconciliação **≤2s** após venda `[RNF-03]`. **A fonte não define o comportamento na janela entre a venda externa e a propagação** — ver [A-SP-02](#6-pontos-de-sensibilidade) |

---

#### A-CN-03 — Falha parcial de parceiro *(A / A)*

| Parte | Conteúdo |
|---|---|
| **Fonte** | Sistema da companhia aérea (indisponível, lento ou respondendo com erro) |
| **Estímulo** | Webhook de estoque falha nas **3 tentativas** (1s/5s/30s, timeout 10s); demais companhias operam normalmente |
| **Ambiente** | Falha parcial — 1 de N parceiros degradado; plataforma saudável |
| **Artefato** | Adaptador de Integração, Serviço de Estoque, Busca & Precificação |
| **Resposta esperada pela avaliação** | A oferta daquela companhia **não pode continuar sendo vendida como se o estoque fosse confiável**; a degradação deve ser contida ao tenant afetado, sem derrubar busca das demais |
| **Métrica** | Retry conforme `[RNF-10]`; RTO **≤5min** / RPO **≤1min** `[RNF-22 — baseline]`. **Comportamento após esgotar as 3 tentativas não está definido na fonte** `[A-H-02]` — esta é a lacuna mais grave encontrada nesta avaliação (ver [A-RSK-03](#7-riscos)) |

---

#### A-CN-04 — Jobs sobrepostos *(A / A)*

| Parte | Conteúdo |
|---|---|
| **Fonte** | Agendador de jobs (conciliação RF-32), operador humano (reprocessamento manual), sweeper de holds, lote B2B |
| **Estímulo** | Uma execução de conciliação do período T ainda em curso quando a execução seguinte é disparada — por atraso, por retry do agendador ou por acionamento manual concorrente |
| **Ambiente** | Fechamento de período (1º dia útil do mês, RNF-12) sob volume máximo; ou reprocessamento após incidente |
| **Artefato** | Serviço de Conciliação Financeira, trilha de auditoria, Barramento de Eventos (leitura histórica) |
| **Resposta esperada** | O resultado final é idêntico ao de uma execução única: nenhum lançamento duplicado, nenhum perdido, trilha coerente |
| **Métrica** | **0% de lançamentos duplicados ou perdidos** `[RNF-23]` — a fonte garante isso por **idempotência de reprocessamento**, mas **não exige exclusão mútua entre execuções concorrentes** `[A-H-03]`. Idempotência protege reexecução **sequencial**; não é equivalente a proteção contra **concorrência**. Ver [A-SP-04](#6-pontos-de-sensibilidade) e [A-RSK-04](#7-riscos) |

**A-CN-04a — Execução diária normal** *(A / M)* — variante de regime permanente: **Fonte** agendador; **Estímulo** execução diária no horário previsto; **Ambiente** operação normal; **Artefato** conciliação + faturamento + sweeper; **Resposta** conclusão dentro da janela, relatório consolidado disponível, trilha gravada; **Métrica** fatura consolidada até o **1º dia útil do mês seguinte** `[RNF-12]`; NF individual **≤60s** após pagamento `[RNF-12]`; **duração aceitável da janela diária e horário de corte não definidos na fonte** `[A-H-04]`.

---

#### A-CN-05 — Degradação silenciosa dos dados *(A / A)*

| Parte | Conteúdo |
|---|---|
| **Fonte** | Adaptador de companhia entregando dados **sintaticamente válidos porém desatualizados ou parcialmente corretos** (ex.: snapshot antigo reenviado; contador de estoque congelado; tarifa de campanha expirada ainda vigente) |
| **Estímulo** | Ausência de erro explícito — nenhum retry é disparado, nenhum alerta de indisponibilidade ocorre; o dado simplesmente deixa de refletir a realidade |
| **Ambiente** | Operação normal, sem incidente declarado; pode persistir por horas/dias |
| **Artefato** | Serviço de Estoque, Catálogo, Busca & Precificação, Antifraude/Observabilidade |
| **Resposta esperada** | O sistema detecta que o dado envelheceu além do tolerável e trata a oferta como não confiável antes de vendê-la |
| **Métrica** | `[RNF-03]` mede **divergência**, o que só é verificável se houver uma **referência viva** para comparar — se o snapshot da companhia é a própria referência e ele está velho, a divergência medida é 0 e o sistema está errado. **Nenhum requisito da fonte define limiar de frescor (staleness) nem verificação de vivacidade** `[A-H-05]`. Ver [A-RSK-05](#7-riscos) |

---

#### A-CN-06 — Candidato pior que o vigente *(M / A)*

| Parte | Conteúdo |
|---|---|
| **Fonte** | Gestor comercial (nova faixa tarifária/campanha, RF-25); agente B2B (nova política, RF-20/RNF-30); plataforma (novo critério de ranking/ordenação, RF-04/RF-28) |
| **Estímulo** | Publicação de uma configuração **candidata** que, em produção, se mostra **pior** que a vigente — conversão cai, bloqueio indevido de reservas em política, ranking degrada receita do parceiro, ou preço promocional consome estoque abaixo do intencionado |
| **Ambiente** | Produção, tráfego real, sem time técnico envolvido (RNF-31 exige explicitamente isso) |
| **Artefato** | Catálogo, Motor de Políticas B2B, Busca & Precificação, dashboards (RF-27, RF-28) |
| **Resposta esperada** | O candidato pior é detectado a partir de sinal objetivo e sua vigência é interrompida antes de dano acumulado relevante |
| **Métrica** | **A fonte não define nenhum critério de aceitação, janela de observação, métrica-guarda nem gatilho de interrupção** para publicações. RF-27 fornece os **indicadores** (volume, receita, ticket médio, conversão) mas nada os liga a uma decisão de manter/reverter. Janela de canário e limiar de regressão: `[A-H-06]`. Ver [A-RSK-06](#7-riscos) e [A-TO-05](#9-trade-offs) |

---

#### A-CN-07 — Rollback de publicação *(A / A)*

| Parte | Conteúdo |
|---|---|
| **Fonte** | Gestor comercial ou administradora da plataforma, após identificar erro de publicação (tarifa com valor errado, política que bloqueia tudo, promoção com estoque mal dimensionado) |
| **Estímulo** | Pedido de reverter a publicação ao estado imediatamente anterior |
| **Ambiente** | Produção; **vendas já podem ter ocorrido sob a configuração errada** |
| **Artefato** | Catálogo, Motor de Políticas B2B, Serviço de Estoque, Faturamento, reservas já confirmadas |
| **Resposta esperada** | A configuração volta ao estado anterior de forma verificável, **e** o tratamento das vendas já realizadas sob a configuração errada é explicitamente decidido — reverter a configuração **não** reverte a venda: RNF-19 (0 perda de reserva paga) e a RESTRIÇÃO-CRÍTICA-01 impedem desfazer uma confirmação |
| **Métrica** | RNF-31 garante que publicar leva **≤1 min**; **não existe requisito simétrico para despublicar/reverter** `[A-H-07]`. A avaliação registra que **rollback de configuração e rollback de efeito comercial são problemas distintos**, e a fonte não trata nenhum dos dois. Ver [A-RSK-07](#7-riscos) e [A-TO-04](#9-trade-offs) |

---

#### A-CN-08 — Indisponibilidade de busca/checkout *(A / A)*

| Parte | Conteúdo |
|---|---|
| **Fonte** | Falha interna da plataforma (Estoque, Pagamentos, Barramento) ou dependência externa (PSP, provedor de notificação) |
| **Estímulo** | Componente do caminho crítico indisponível durante a janela dos 0,1% permitidos (≤43 min/mês) |
| **Ambiente** | Pico de demanda; persona de urgência ([última-hora](../PERSONAS/passageiros/viajante-ultima-hora.md): viagem em <24h) |
| **Artefato** | BFF/Gateway, Busca, Estoque, Reserva & Checkout, Pagamentos |
| **Resposta esperada** | Nenhuma venda é confirmada sem garantia de assento; nenhum pagamento é capturado sem reserva correspondente; o usuário recebe estado inequívoco (não "talvez comprado") |
| **Métrica** | Disponibilidade **≥99,9%/mês** `[RNF-20]`; **0 perda de reserva paga** `[RNF-19]`; confirmação **≤5s** após aprovação `[RNF-19]`. **A fonte não define modo degradado**: não diz se, com Estoque indisponível, a busca deve continuar servindo resultados (arriscando A-DR-01) ou parar (custando disponibilidade) `[A-H-08]`. Ver [A-SP-05](#6-pontos-de-sensibilidade) — este é o ponto de sensibilidade mais agudo da arquitetura |

**A-CN-08b — Indisponibilidade da API B2B** *(M / M)* — **Fonte** agência integrada; **Estímulo** API fora do ar durante emissão em lote em curso; **Ambiente** fechamento de evento corporativo (cenário das 8 passagens da [persona B2B](../PERSONAS/b2b-agencias/agente-de-viagens-corporativas.md)); **Artefato** API B2B, Emissão em Lote; **Resposta** o lote parcialmente processado tem estado recuperável e não emite passagem duplicada ao ser retomado; **Métrica** disponibilidade **≥99,9%/mês**, latência **≤500ms P95** `[RNF-09]`; **retomada de lote interrompido não é tratada na fonte** `[A-H-09]`.

---

#### A-CN-09 — Vazamento cross-tenant *(A / A)*

| Parte | Conteúdo |
|---|---|
| **Fonte** | Agente B2B autenticado, gestor de uma companhia, ou consumidor da API com token válido |
| **Estímulo** | Requisição que referencia identificador pertencente a **outro** tenant (empresa cliente ou companhia) — por erro de aplicação, enumeração de IDs, ou consulta a painel agregado |
| **Ambiente** | Operação normal, credencial legítima, sem invasão — o caminho mais provável de vazamento é **autorização faltante**, não invasão |
| **Artefato** | RBAC, Armazenamento por tenant, API B2B, Painel Admin Interno (que **legitimamente** agrega dados de todos os tenants — RF-30) |
| **Resposta esperada** | Acesso negado e registrado; agregação cross-tenant permitida **apenas** ao papel de operação interna |
| **Métrica** | **0% de vazamento cross-tenant** `[RNF-27]`, verificado por auditoria **trimestral** `[RNF-27]`; logs **≥5 anos** `[RNF-28]`; token **≤1h** `[RNF-28]`. **Auditoria trimestral é detecção com latência de até 90 dias para uma meta de zero** — ver [A-SP-06](#6-pontos-de-sensibilidade) e [A-TO-06](#9-trade-offs) |

---

#### A-CN-10 — Dado sensível e conformidade *(A / M)*

| Parte | Conteúdo |
|---|---|
| **Fonte** | Titular de dados (incluindo responsável por menor); auditoria PCI |
| **Estímulo** | Pedido de exclusão LGPD sobre um titular com reservas ativas e lançamentos financeiros retidos; ou auditoria de armazenamento de cartão |
| **Ambiente** | Operação normal |
| **Artefato** | Pagamentos (tokenização), Armazenamento por tenant, Conciliação (trilha imutável), Faturamento |
| **Resposta esperada** | Exclusão atendida sem violar a retenção obrigatória |
| **Métrica** | Exclusão **≤15 dias corridos** `[RNF-25]`; **100% de PAN tokenizado** `[RNF-24]`; trilha de auditoria **≥5 anos** `[RNF-23]`; logs **≥5 anos** `[RNF-28]`. **Conflito não registrado na fonte:** RNF-25 (exclusão em 15 dias) versus RNF-23/RNF-28 (retenção ≥5 anos) — a fonte não define o que prevalece nem quais campos são anonimizáveis `[A-H-10]`. Ver [A-RSK-10](#7-riscos) |

---

#### A-CN-11 — Reserva paga sob falha pós-pagamento *(A / M)*

| Parte | Conteúdo |
|---|---|
| **Fonte** | Passageiro; PSP |
| **Estímulo** | Pagamento aprovado no PSP, mas falha ocorre **entre** a aprovação e o CONFIRM do assento (queda do serviço, expiração do HOLD durante a autorização, timeout de rede) |
| **Ambiente** | Operação normal ou degradada; Pix e cartão têm tempos de aprovação diferentes |
| **Artefato** | Pagamentos, Reserva & Checkout, Estoque (CAS), Emissão de Bilhete, Barramento |
| **Resposta esperada** | Ou a reserva é confirmada de forma durável, ou o pagamento não permanece capturado sem assento — sem estado intermediário visível ao passageiro como ambíguo |
| **Métrica** | **0 casos de perda de reserva paga** `[RNF-19]`; confirmação **≤5s** `[RNF-19]`. **A fonte não define compensação para pagamento aprovado com HOLD expirado** — cenário fisicamente possível se o TTL (L-07) for menor que o tempo de aprovação de Pix/cartão `[A-H-11]`. Ver [A-RSK-11](#7-riscos) |

---

#### A-CN-12 — Divergência de preço busca ↔ checkout *(A / M)*

| Parte | Conteúdo |
|---|---|
| **Fonte** | Gestor comercial (atualização de tarifa dinâmica durante a sessão do passageiro) |
| **Estímulo** | Preço da tarifa muda enquanto o passageiro está no funil |
| **Ambiente** | Operação normal; janela de atualização ≥5 min |
| **Artefato** | Busca & Precificação (congelamento de sessão), Catálogo, Reserva & Checkout |
| **Resposta esperada** | O passageiro paga o preço congelado, **ou** é explicitamente informado da variação antes de confirmar |
| **Métrica** | Divergência **0%** `[RNF-01, RNF-14]`; atualizações **≥5 min** e sinalizadas `[RNF-01]`. **Conflito C-02 já registrado na fonte permanece aberto** — RNF-14 não admite a exceção que RNF-01 admite; sem harmonização, o cenário **não é testável** `[A-SP-03]` |

---

#### A-CN-13 — Reserva fora de política B2B *(A / M)*

| Parte | Conteúdo |
|---|---|
| **Fonte** | Agente B2B, ou sistema da agência via API |
| **Estímulo** | Tentativa de reserva que viola a política da empresa cliente (classe, teto por trecho, companhia não credenciada) — inclusive via **lote**, onde a validação por item pode ser omitida por desempenho |
| **Ambiente** | Operação diária (15–30 reservas/semana por agente); política podendo ter sido alterada há <5 min (RNF-30) |
| **Artefato** | Motor de Políticas B2B, API B2B, Emissão em Lote, Reserva & Checkout |
| **Resposta esperada** | Bloqueio ou sinalização antes da confirmação, com a mesma regra aplicada em todos os canais |
| **Métrica** | **100% das reservas fora de política** bloqueadas/sinalizadas; falso-negativo **≤0,1% das transações/mês** `[RNF-02]`. **Observação da avaliação:** "100%" e "falso-negativo ≤0,1%" são metas **mutuamente inconsistentes** no mesmo requisito — 100% implica falso-negativo 0. Conflito **não registrado** na fonte; ver [A-RSK-13](#7-riscos) |

---

#### A-CN-14 — Busca sob carga *(M* / M)*

| Parte | Conteúdo |
|---|---|
| **Fonte** | Passageiros e integrações B2B simultâneos |
| **Estímulo** | Volume concorrente de buscas em pico (promoção publicada, feriado) |
| **Ambiente** | Produção, N companhias consultadas por busca |
| **Artefato** | BFF, Busca & Precificação, Catálogo, Estoque (disponibilidade agregada) |
| **Resposta** | Latência dentro da meta sem degradação de correção de preço/estoque |
| **Métrica** | **≤3s P95 / ≤5s P99**, **≥500 buscas simultâneas** `[RNF-04 — baseline sugerida]`. Volumes reais **ausentes** (L-02): a avaliação **não pode julgar suficiência** deste número. Ver [A-RSK-09](#7-riscos) |

---

#### A-CN-15 — Checkout ≤120s sob contenção *(A / A)*

| Parte | Conteúdo |
|---|---|
| **Fonte** | Passageiro de urgência ([última-hora](../PERSONAS/passageiros/viajante-ultima-hora.md)) |
| **Estímulo** | Checkout completo com assento disputado e pagamento via Pix (latência de confirmação fora do controle da plataforma) |
| **Ambiente** | Voo quase esgotado, alta concorrência |
| **Artefato** | Reserva & Checkout, Estoque (HOLD/TTL), Pagamentos |
| **Resposta esperada** | Compra concluída sem que o HOLD expire durante a aprovação do pagamento |
| **Métrica** | **≤120s**, **≤4 telas** `[RNF-05]`, **≤3 toques** `[RNF-13]`. **TTL do HOLD indefinido (L-07)** torna este cenário indeterminado: se TTL < tempo de aprovação de Pix, o cenário falha por construção. Ver [A-SP-01](#6-pontos-de-sensibilidade) — **este é o parâmetro mais sensível de toda a arquitetura** |

---

#### A-CN-16 — Dados novos: nova companhia parceira *(A / A)*

| Parte | Conteúdo |
|---|---|
| **Fonte** | Administradora da plataforma + TI da nova companhia |
| **Estímulo** | Onboarding de uma companhia cujo sistema interno tem semântica diferente das existentes (granularidade de assento vs. contador de classe, fuso, códigos de tarifa próprios, ausência de webhook) |
| **Ambiente** | Homologação e, depois, produção com tráfego real |
| **Artefato** | Adaptador de Integração, Catálogo, Estoque, Onboarding, APIs versionadas |
| **Resposta esperada** | A nova companhia entra sem exigir mudança nos componentes centrais, e sua eventual imaturidade técnica não degrada as demais |
| **Métrica** | Onboarding técnico **≤10 dias úteis** `[RNF-29 — baseline]`; retrocompatibilidade **≥12 meses** `[RNF-33]`. **A fonte não define um contrato canônico de estoque** nem o mínimo técnico exigido do parceiro — nada garante que uma companhia sem capacidade de sincronização em ≤2s (RNF-06) seja recusada ou operada em regime distinto `[A-H-12]`. Ver [A-RSK-12](#7-riscos) e [A-TR-02](#10-temas-de-risco) |

**A-CN-17 — Evolução de API sem quebrar parceiro** *(M / M)* — **Fonte** plataforma; **Estímulo** publicação de nova versão de API com mudança de contrato; **Ambiente** N agências e companhias em versões distintas; **Artefato** API B2B, Adaptador, Catálogo; **Resposta** parceiros em versão anterior seguem operando; **Métrica** versionamento explícito e retrocompatibilidade **≥12 meses** `[RNF-33]`; **custo de manter N versões e critério de sunset não definidos** `[A-H-13]`.

**A-CN-18 — Checkout de baixa fricção** *(M / B)* — **Métrica**: ≤3 toques, ≤2 campos obrigatórios `[RNF-13]`; ≤3 upsells, nenhum pré-marcado `[RNF-15]`; reuso ≥70% de campos do 2º passageiro em diante `[RNF-17]`.

**A-CN-19 — Faixa de dispositivos** *(B / B)* — **Métrica**: 360–1920px, LCP ≤2,5s em 4G `[RNF-18]`; 2 últimas majors iOS/Android e 2 últimas versões Chrome/Safari/Edge `[RNF-32]`.

---

## 5. Priorização dos cenários

Ordenação da equipe avaliadora (**pendente de votação de stakeholders** — [A-RSK-14](#7-riscos)). Critério: `(Importância, Dificuldade)` — cenários `(A,A)` concentram o esforço de análise, conforme o método.

| Ordem | Cenário | (I, D) | Razão da posição |
|---|---|---|---|
| 1 | [A-CN-15](#a-cn-15--checkout-120s-sob-contenção-a--a) — Checkout sob contenção | (A, A) | Único cenário em que **dois requisitos de zero-tolerância e um de tempo colidem em um parâmetro indefinido** (TTL, L-07). |
| 2 | [A-CN-08](#a-cn-08--indisponibilidade-de-buscacheckout-a--a) — Indisponibilidade | (A, A) | Define se a plataforma prefere vender errado ou não vender. Ausente da fonte. |
| 3 | [A-CN-03](#a-cn-03--falha-parcial-de-parceiro-a--a) — Falha parcial de parceiro | (A, A) | Fim da cadeia de retry é um vazio que aponta diretamente para overbooking. |
| 4 | [A-CN-05](#a-cn-05--degradação-silenciosa-dos-dados-a--a) — Degradação silenciosa | (A, A) | A única classe de falha que **as métricas atuais não conseguem enxergar**. |
| 5 | [A-CN-01](#a-cn-01--concorrência-por-assento-importância-a--dificuldade-a) — Concorrência por assento | (A, A) | Crítico, porém **bem endereçado** pela arquitetura (candidato a não-risco). |
| 6 | [A-CN-09](#a-cn-09--vazamento-cross-tenant-a--a) — Vazamento cross-tenant | (A, A) | Meta de zero com detecção trimestral. |
| 7 | [A-CN-04](#a-cn-04--jobs-sobrepostos-a--a) — Jobs sobrepostos | (A, A) | Idempotência ≠ proteção contra concorrência. |
| 8 | [A-CN-07](#a-cn-07--rollback-de-publicação-a--a) — Rollback | (A, A) | Publicação em ≤1 min sem caminho de volta. |
| 9 | [A-CN-16](#a-cn-16--dados-novos-nova-companhia-parceira-a--a) — Dados novos / nova companhia | (A, A) | O modelo de negócio depende disso; contrato canônico ausente. |
| 10 | [A-CN-02](#a-cn-02--propagação-de-venda-externa-da-companhia-a--a) — Venda externa da companhia | (A, A) | Janela de propagação é risco residual inerente. |
| 11 | [A-CN-11](#a-cn-11--reserva-paga-sob-falha-pós-pagamento-a--m) — Reserva paga sob falha | (A, M) | Coberto por RNF-19, mas sem compensação definida. |
| 12 | [A-CN-12](#a-cn-12--divergência-de-preço-busca--checkout-a--m) — Preço busca↔checkout | (A, M) | Bloqueado pelo conflito C-02. |
| 13 | [A-CN-13](#a-cn-13--reserva-fora-de-política-b2b-a--m) — Política B2B | (A, M) | Meta internamente inconsistente. |
| 14 | [A-CN-10](#a-cn-10--dado-sensível-e-conformidade-a--m) — LGPD/PCI | (A, M) | Conflito retenção × exclusão. |
| 15 | [A-CN-06](#a-cn-06--candidato-pior-que-o-vigente-m--a) — Candidato pior | (M, A) | Alto custo arquitetural, importância declarada menor na fonte. |
| 16 | [A-CN-04a](#a-cn-04--jobs-sobrepostos-a--a) — Execução diária | (A, M) | Regime permanente; metas existem, janela não. |
| 17 | [A-CN-08b](#a-cn-08--indisponibilidade-de-buscacheckout-a--a) — API B2B indisponível | (M, M) | Retomada de lote indefinida. |
| 18 | [A-CN-14](#a-cn-14--busca-sob-carga-m--m) — Busca sob carga | (M, M) | Não avaliável sem volumes reais (L-02). |
| 19 | [A-CN-17](#a-cn-16--dados-novos-nova-companhia-parceira-a--a) — Evolução de API | (M, M) | Requisito claro, custo não dimensionado. |
| 20–21 | A-CN-18, A-CN-19 | (M,B) / (B,B) | Bem especificados e de baixo risco arquitetural. |

---

## 6. Pontos de sensibilidade

Parâmetro de decisão cuja variação **move mensuravelmente** uma resposta de atributo de qualidade. Sensibilidade **não é** defeito.

| ID | Ponto de sensibilidade | Decisão/parâmetro | Atributo afetado | Cenários | Evidência |
|---|---|---|---|---|---|
| **A-SP-01** | **Valor do TTL do HOLD** | [A-AB-03](#32-abordagens-arquiteturais-identificadas-na-arquitetura-documentada); L-07; faixas em [§12.4](arquitetura.md#124-expiração-do-hold) | TTL curto → sobe risco de perder reserva de pagador legítimo (RNF-19) e de estourar RNF-05 com Pix; TTL longo → estoque percebido cai em pico, tensionando RNF-03/RF-26 | A-CN-15, A-CN-11, A-CN-01 | **Único parâmetro que toca simultaneamente três metas de zero-tolerância.** Valor não definido na fonte. |
| **A-SP-02** | **Janela de propagação de estoque** (≤2s P95 / ≤5s P99) | RNF-06, [A-AB-04](#32-abordagens-arquiteturais-identificadas-na-arquitetura-documentada) | Janela menor → mais custo/acoplamento de sincronização; janela maior → cresce a janela em que RESTRIÇÃO-CRÍTICA-01 depende de sorte | A-CN-02, A-CN-05 | Enquanto a venda externa não propaga, a exclusão mútua local (A-AB-02) **não protege** contra o estoque da companhia. |
| **A-SP-03** | **Definição da exceção de tarifa dinâmica** | RNF-01 vs. RNF-14 (conflito **C-02**, já registrado) | Admitir exceção → protege receita, arranha A-DR-02; não admitir → protege confiança, transfere risco de preço à plataforma | A-CN-12 | Sem decisão, o cenário não tem critério de aprovação. |
| **A-SP-04** | **Granularidade da chave de idempotência** da conciliação | RNF-23, [A-AB-10](#32-abordagens-arquiteturais-identificadas-na-arquitetura-documentada) | Chave por evento protege reexecução; **não protege** duas execuções lendo a mesma janela simultaneamente sem exclusão mútua | A-CN-04, A-CN-04a | [§11.4](arquitetura.md#114-fluxo-batch--conciliação-financeira) aplica "chave de idempotência por evento"; a concorrência entre jobs não é tratada. |
| **A-SP-05** | **Política de modo degradado quando o Estoque está indisponível** | Ausente da fonte | Servir busca sem Estoque → disponibilidade preservada, RESTRIÇÃO-CRÍTICA-01 ameaçada; recusar → integridade preservada, RNF-20 consumido | A-CN-08, A-CN-03 | **A restrição de §0 prevalece sobre desempenho e conveniência** — logo a leitura da avaliação é que recusar é a única leitura compatível, mas **isso não está escrito em lugar nenhum**. |
| **A-SP-06** | **Frequência de verificação de isolamento** (auditoria trimestral) | RNF-27 | Trimestral → baixo custo, detecção lenta para uma meta de 0% | A-CN-09 | Um vazamento pode durar até 90 dias antes da verificação prevista. |
| **A-SP-07** | **Ponto de aplicação da política B2B** (busca, commit, ou ambos) | RF-20, RNF-02, [A-AB-07](#32-abordagens-arquiteturais-identificadas-na-arquitetura-documentada) | Só na busca → falso-negativo se a política mudar durante a sessão (RNF-30 permite mudança em 5 min); nos dois → custo de latência no lote | A-CN-13 | [§9](arquitetura.md#9-responsabilidades-limites-e-interfaces-dos-componentes) diz "na busca/reserva"; a fonte não fixa o ponto decisivo. |
| **A-SP-08** | **Tamanho do lote B2B** (até 50) | RNF-08 `[baseline]`, [A-AB-09](#32-abordagens-arquiteturais-identificadas-na-arquitetura-documentada) | Lote maior → mais contenção de assento e mais tempo sob HOLD simultâneo; menor → mais chamadas | A-CN-08b, A-CN-01 | Cenário real da persona é 8; 50 é hipótese da própria fonte. |

---

## 7. Riscos

Risco = decisão (ou ausência de decisão) que **pode impedir** o alcance de um atributo prioritário. Cada risco cita cenário e decisão.

| ID | Risco | Cenário | Decisão/ausência que o origina | Atributo/driver ameaçado | Severidade |
|---|---|---|---|---|---|
| **A-RSK-01** | **TTL do HOLD indefinido torna indecidível o encontro entre RNF-05, RNF-19 e RNF-03.** Não é "detalhe a definir depois": é o parâmetro que decide qual das três metas de zero cede. | A-CN-15, A-CN-11 | L-07 + [A-SP-01](#6-pontos-de-sensibilidade) | A-DR-01, A-DR-05, A-DR-06 | **Crítica** |
| **A-RSK-02** | **Ausência de política de modo degradado.** Com o Estoque indisponível, nada na fonte impede que a busca continue ofertando — caminho direto para violar a restrição inviolável durante um incidente. | A-CN-08, A-CN-03 | [A-SP-05](#6-pontos-de-sensibilidade) | A-DR-01 vs. A-DR-06 | **Crítica** |
| **A-RSK-03** | **Fim de cadeia de retry não especificado.** RNF-10 define 3 tentativas e para. Após a 3ª falha o estoque daquela companhia está desatualizado e **nenhum requisito diz o que acontece** — o sistema continua vendendo com dado antigo. | A-CN-03 | RNF-10 sem cláusula terminal; [A-AB-05](#32-abordagens-arquiteturais-identificadas-na-arquitetura-documentada) | A-DR-01, A-DR-03 | **Crítica** |
| **A-RSK-04** | **Idempotência confundida com controle de concorrência.** RNF-23 garante reexecução segura, não execução simultânea segura; sem exclusão mútua ou janela particionada, duas execuções podem produzir contagem dupla apesar da chave de idempotência, dependendo de ordem de leitura/escrita. | A-CN-04, A-CN-04a | [A-SP-04](#6-pontos-de-sensibilidade); [§11.4](arquitetura.md#114-fluxo-batch--conciliação-financeira) | A-DR-05 | **Alta** |
| **A-RSK-05** | **Degradação silenciosa é invisível às métricas atuais.** Todas as métricas de estoque medem divergência contra a fonte do parceiro; se a própria fonte estiver congelada, a divergência medida é zero e o sistema reporta saúde perfeita enquanto vende assentos inexistentes. | A-CN-05 | Ausência de requisito de frescor/vivacidade | A-DR-01, A-DR-03 | **Alta** |
| **A-RSK-06** | **Publicação sem critério de aceitação.** RNF-31 exige publicar em ≤1 min sem time técnico; nada define métrica-guarda, janela de observação ou gatilho de interrupção para um candidato pior. Autonomia sem instrumentação de decisão. | A-CN-06 | [A-AB-07](#32-abordagens-arquiteturais-identificadas-na-arquitetura-documentada), RNF-30/RNF-31 | A-DR-07, A-DR-03 | **Média-alta** |
| **A-RSK-07** | **Rollback inexistente e assimétrico.** Não há requisito de reversão; e mesmo que houvesse, reverter configuração não reverte vendas já confirmadas — que RNF-19 e a RESTRIÇÃO-CRÍTICA-01 proíbem desfazer. O dano de uma publicação errada é, hoje, **irreversível por construção**. | A-CN-07 | Ausência de requisito + [A-TO-04](#9-trade-offs) | A-DR-05, A-DR-07 | **Alta** |
| **A-RSK-08** | **0,1% de indisponibilidade sem comportamento definido.** ≤43 min/mês é aceito, mas o que o sistema faz nesses 43 min não é especificado — justamente para a persona cuja viagem é em <24h. | A-CN-08 | RNF-20 sem contraparte de degradação | A-DR-06, A-DR-01 | **Média-alta** |
| **A-RSK-09** | **Dimensionamento impossível.** Sem volumes globais (L-02), RNF-04/RNF-07/RNF-08 não são verificáveis como suficientes; qualquer arquitetura pode parecer adequada. | A-CN-14, A-CN-04a | L-02, S-01 | Desempenho, capacidade | **Média** |
| **A-RSK-10** | **Colisão LGPD × retenção.** RNF-25 (exclusão ≤15 dias) versus RNF-23/RNF-28 (retenção ≥5 anos) não é resolvida na fonte, e envolve dado de menores. Conflito **não catalogado** em §8.2 da arquitetura. | A-CN-10 | RNF-25 vs. RNF-23/RNF-28 | A-DR-04, conformidade | **Média-alta** |
| **A-RSK-11** | **Pagamento aprovado com HOLD expirado sem compensação definida.** Cenário fisicamente alcançável (Pix + TTL curto), e a resposta correta — estornar, ou honrar realocando assento — não está na fonte e **não pode ser inventada** porque toca RNF-19 e a restrição de §0 ao mesmo tempo. | A-CN-11 | L-07, RNF-19 | A-DR-01, A-DR-05 | **Alta** |
| **A-RSK-12** | **Nenhum contrato canônico nem piso técnico de parceiro.** O marketplace herda a qualidade da pior integração; RNF-06 é exigido da plataforma, não do parceiro. | A-CN-16, A-CN-03 | Ausência; RF-29/RNF-29 tratam prazo, não critério | A-DR-03, A-DR-01 | **Alta** |
| **A-RSK-13** | **RNF-02 é internamente inconsistente:** bloquear "100% das reservas fora de política" e admitir falso-negativo "≤0,1%/mês" não podem ser ambos verdadeiros. Conflito **não registrado** na fonte. | A-CN-13 | RNF-02 | A-DR-04 | **Média** |
| **A-RSK-14** | **Priorização sem stakeholders.** A árvore de utilidade e a ordem de §5 foram derivadas de texto, não votadas. Em ATAM, a votação é o que legitima a priorização; sem ela, todo este relatório é uma hipótese estruturada. | Todos | Método (ausência de workshop) | Validade da própria avaliação | **Média** |
| **A-RSK-15** | **Suporte 24/7 é um compromisso operacional tratado como requisito de sistema.** RNF-21 (≤2 min de primeira resposta, `[baseline]`) e RF-16 implicam custo e escala de pessoas não modelados em nenhum lugar da arquitetura. | A-CN-08 | RNF-21, RF-16 | Confiabilidade percebida | **Baixa-média** |

---

## 8. Hipóteses da avaliação (pendentes — **não são fatos**)

Nenhuma foi usada para afirmar que um cenário é atendido. Todas requerem decisão de stakeholder.

| ID | Hipótese pendente | Origem | Cenário |
|---|---|---|---|
| **A-H-01** | Latência adicional aceitável para a rejeição determinística sob contenção | Não definida | A-CN-01 |
| **A-H-02** | Comportamento após esgotar as 3 tentativas de webhook (RNF-10) | Não definido | A-CN-03 |
| **A-H-03** | Se execuções concorrentes do mesmo job são permitidas, e sob qual controle | Não definido | A-CN-04 |
| **A-H-04** | Janela e horário de corte da execução diária de conciliação/faturamento | Não definidos | A-CN-04a |
| **A-H-05** | Limiar de frescor (staleness) do estoque e do catálogo | Não definido | A-CN-05 |
| **A-H-06** | Métrica-guarda, janela de observação e gatilho de interrupção para publicação candidata | Não definidos | A-CN-06 |
| **A-H-07** | Existência, prazo e escopo de rollback de publicação | Não definidos | A-CN-07 |
| **A-H-08** | Modo degradado de busca/checkout | Não definido | A-CN-08 |
| **A-H-09** | Retomada de lote B2B interrompido | Não definida | A-CN-08b |
| **A-H-10** | Precedência entre exclusão LGPD e retenção de auditoria; campos anonimizáveis | Não definida | A-CN-10 |
| **A-H-11** | Compensação para pagamento aprovado com HOLD expirado | Não definida | A-CN-11 |
| **A-H-12** | Contrato canônico de estoque e piso técnico exigido do parceiro | Não definidos | A-CN-16 |
| **A-H-13** | Critério de sunset de versão de API e custo de N versões | Não definidos | A-CN-17 |
| **A-H-14** | Todos os valores `[baseline sugerida]` da fonte (RNF-04, 07, 08, 16, 21, 22, 29) | Marcados pela própria fonte | Diversos |
| **A-H-15** | TTL do HOLD (L-07) — faixas propostas em [§12.4](arquitetura.md#124-expiração-do-hold) são opções, não escolha | L-07 | A-CN-15, A-CN-11 |

---

## 9. Trade-offs

Trade-off = decisão em que **melhorar um atributo piora outro de forma mensurável**. Cada um cita cenários e decisões.

| ID | Trade-off | Eixo A | Eixo B | Decisão que o materializa | Cenários | Leitura da equipe avaliadora |
|---|---|---|---|---|---|---|
| **A-TO-01** | **Certeza de assento × velocidade de checkout** | Integridade (RESTRIÇÃO-CRÍTICA-01, RNF-03, RNF-19) | Desempenho/UX (RNF-05 ≤120s, RNF-13 ≤3 toques) | HOLD antes do pagamento + TTL ([A-AB-03](#32-abordagens-arquiteturais-identificadas-na-arquitetura-documentada)) | A-CN-15, A-CN-11 | Já registrado como RSK-02/C-03 na fonte. A avaliação acrescenta: o trade-off é **resolvido pelo valor do TTL**, e como ele é indefinido, o trade-off está **não resolvido**, não apenas conhecido. |
| **A-TO-02** | **Consistência forte de assento × disponibilidade e escala** | Integridade (ponto único de decisão, A-AB-01/02) | Disponibilidade (RNF-20), desempenho (RNF-04*) | Serviço de Estoque como único escritor com CAS | A-CN-01, A-CN-08, A-CN-14 | A arquitetura escolhe deliberadamente consistência sobre disponibilidade — coerente com §0. O custo: **o Estoque é ponto de concentração**; sua indisponibilidade para o checkout inteiro. A fonte não reconhece esse custo. |
| **A-TO-03** | **Estoque percebido × proteção do comprador de boa-fé** | Disponibilidade comercial de assentos (RNF-03, RF-26) | Não perder reserva de pagador legítimo (RNF-19) | Duração do TTL e política de expiração | A-CN-15, A-CN-01 | Holds longos protegem o pagador e "somem" com estoque em pico; curtos liberam estoque e punem pagamento lento. Não há posição neutra. |
| **A-TO-04** | **Autonomia de publicação × reversibilidade** | Manutenibilidade/agilidade (RNF-30 ≤5 min, RNF-31 ≤1 min, sem time técnico) | Segurança operacional (conter dano de configuração errada) | Publicação self-service sem gate técnico ([A-AB-07](#32-abordagens-arquiteturais-identificadas-na-arquitetura-documentada)) | A-CN-06, A-CN-07 | Quanto mais rápido publicar sem revisão, maior o alcance de um erro antes de ser notado. A fonte otimizou só um lado do eixo. |
| **A-TO-05** | **Validar candidato antes × publicar rápido** | Correção comercial (evitar candidato pior) | RNF-31 (≤1 min) | Ausência de etapa de canário/observação | A-CN-06 | Qualquer janela de observação **viola literalmente** RNF-31 se aplicada como bloqueio. Este trade-off é **estrutural**, não de implementação. |
| **A-TO-06** | **Custo de verificação de isolamento × latência de detecção** | Custo operacional (auditoria trimestral, RNF-27) | Segurança (meta de 0% de vazamento) | Cadência trimestral | A-CN-09 | Meta de zero com verificação a cada 90 dias é uma **meta de resultado com instrumento de baixa frequência**. |
| **A-TO-07** | **Isolamento por tenant × agregação para operação interna** | Segurança (RNF-27, A-AB-08) | Observabilidade (RF-30, RF-31, RNF-07*) | Armazenamento segregado + painel unificado | A-CN-09 | O painel interno é, por definição, o **único caminho legítimo de leitura cross-tenant** — e portanto a maior superfície de vazamento acidental. |
| **A-TO-08** | **Transparência de preço × tarifa dinâmica do parceiro** | Confiança do passageiro (RNF-14: 0%) | Receita/paridade da companhia (RNF-01: exceção ≥5 min) | Congelamento de sessão ([A-AB-06](#32-abordagens-arquiteturais-identificadas-na-arquitetura-documentada)) | A-CN-12 | É a expressão econômica do conflito C-02: alguém absorve a variação — passageiro, plataforma ou companhia. A fonte não diz quem. |
| **A-TO-09** | **Uniformidade de integração × velocidade de onboarding** | Integridade do marketplace (contrato canônico) | RNF-29 (≤10 dias úteis*) | Adaptador por companhia ([A-AB-05](#32-abordagens-arquiteturais-identificadas-na-arquitetura-documentada)) | A-CN-16 | Adaptadores permissivos aceleram onboarding e importam a fragilidade do parceiro para dentro. |
| **A-TO-10** | **Throughput do lote × contenção de assento** | RNF-08 (50 passageiros / 30s*) | RESTRIÇÃO-CRÍTICA-01 e latência do checkout online | Reuso do mecanismo de HOLD no lote ([A-AB-09](#32-abordagens-arquiteturais-identificadas-na-arquitetura-documentada)) | A-CN-08b, A-CN-01 | A decisão de **não** dar caminho privilegiado ao lote é correta frente a §0, e cobra o preço em latência e em holds simultâneos. |

---

## 10. Não-riscos

Decisões que a avaliação considera **seguras dadas as premissas declaradas** — cada uma com a premissa que, se cair, transforma o não-risco em risco.

| ID | Não-risco | Cenário | Por que é não-risco | **Premissa que o sustenta** |
|---|---|---|---|---|
| **A-NR-01** | **Exclusão mútua por escrita condicional em `(voo_id, assento_id)`** resolve a disputa de assento sem ambiguidade e sem lógica de desempate. | A-CN-01 | Desempate mecânico na camada de dados; rejeição determinística; sem "quase-vitória". Alinhado literalmente à RESTRIÇÃO-CRÍTICA-01. | Que o armazenamento escolhido (**L-01 — ainda indefinido**) ofereça escrita condicional/unicidade com garantia atômica real. Se não oferecer, [§12.5](arquitetura.md#125-alternativas-consideradas-para-a-exclusão-mútua-e-por-que-a-proposta-acima-foi-preferida) já mantém single-writer como alternativa. |
| **A-NR-02** | **Rejeição da reserva otimista com reconciliação posterior** ([§12.5](arquitetura.md#125-alternativas-consideradas-para-a-exclusão-mútua-e-por-que-a-proposta-acima-foi-preferida)). | A-CN-01 | Vender e cancelar depois viola §0 e RNF-19 simultaneamente. Descarte correto e bem justificado. | Que RESTRIÇÃO-CRÍTICA-01 permaneça inviolável. |
| **A-NR-03** | **Lote B2B reusar o mesmo mecanismo de reserva, sem exceção.** | A-CN-08b, A-CN-01 | Elimina a classe inteira de bug em que um canal secundário fura a regra central. | Que RNF-08 (50/30s `[baseline]`) não seja endurecido a ponto de forçar um atalho. |
| **A-NR-04** | **Estoque como fonte única de verdade**, sem escrita por outros serviços. | A-CN-01, A-CN-02 | Torna a restrição §0 **verificável** — há um só lugar para auditar. | Que nenhum painel ou adaptador receba escrita direta "por desempenho" em fase posterior. |
| **A-NR-05** | **Idempotência da conciliação com trilha imutável.** | A-CN-04a | Correto e explícito para **reexecução**; raro em elicitações nesse nível de maturidade. | Que execuções sejam **sequenciais** — premissa que [A-RSK-04](#7-riscos) questiona. Este não-risco vale para A-CN-04a, **não** para A-CN-04. |
| **A-NR-06** | **Versionamento de API com retrocompatibilidade ≥12 meses.** | A-CN-17 | Endereça diretamente a heterogeneidade de parceiros (GDS, sistemas internos) declarada nas personas. | Que o custo de manter N versões seja dimensionado (A-H-13). |
| **A-NR-07** | **Portabilidade e usabilidade (RNF-13, 15, 17, 18, 32).** | A-CN-18, A-CN-19 | Metas objetivas, verificáveis e sem tensão estrutural com os drivers de integridade. | Nenhuma relevante. |
| **A-NR-08** | **Separação explícita entre Catálogo (oferta) e Estoque (assento).** | A-CN-02, A-CN-16 | Impede que publicação de tarifa (rápida, self-service) toque disponibilidade (crítica, transacional). Limite bem colocado. | Que campanhas com "quantidade de assentos promocionais" (RF-25) sejam materializadas **no Estoque**, não no Catálogo — ponto que a fonte **não** deixa claro. |

---

## 11. Temas de risco

Agrupamento dos riscos em padrões sistêmicos, com o driver de negócio ameaçado — a saída de maior nível da ATAM.

### A-TR-01 — Comportamento sob falha não é requisito de primeira classe
**Riscos:** A-RSK-02, A-RSK-03, A-RSK-08, A-RSK-11, A-RSK-05
**Cenários:** A-CN-03, A-CN-05, A-CN-08, A-CN-08b, A-CN-11
**Driver ameaçado:** A-DR-01, A-DR-06

Os 33 RNF descrevem com precisão o **sistema funcionando** — latências, percentis, disponibilidade. Praticamente nenhum descreve o **sistema falhando**: o que acontece após o terceiro retry, o que a busca faz sem o Estoque, o que ocorre com um pagamento aprovado cujo HOLD venceu, como o lote retoma. Como a arquitetura protege a restrição §0 por um caminho feliz muito bem desenhado, **o risco de overbooking migrou inteiramente para os caminhos de falha** — que são exatamente os não especificados. É o tema de maior severidade desta avaliação.

### A-TR-02 — A qualidade da plataforma é refém do parceiro menos maduro
**Riscos:** A-RSK-03, A-RSK-05, A-RSK-12
**Cenários:** A-CN-02, A-CN-03, A-CN-05, A-CN-16
**Driver ameaçado:** A-DR-03, A-DR-01

RNF-03 e RNF-06 impõem à **plataforma** metas cuja realização depende do **sistema da companhia**. Não há contrato canônico, piso técnico de admissão, nem regime diferenciado para parceiros incapazes de sincronizar em ≤2s — apenas um prazo de onboarding (RNF-29, `[baseline]`). O marketplace assume um compromisso que não controla. As personas confirmam a heterogeneidade ([administradora](../PERSONAS/operacao-interna/administrador-da-plataforma.md): "cada nova companhia aérea exige um processo diferente").

### A-TR-03 — Agilidade operacional sem contrapartida de reversão e verificação
**Riscos:** A-RSK-06, A-RSK-07, A-RSK-04
**Cenários:** A-CN-04, A-CN-06, A-CN-07
**Driver ameaçado:** A-DR-07, A-DR-05

RNF-30 e RNF-31 otimizam agressivamente a velocidade de mudança (≤5 min, ≤1 min, sem time técnico), sem nenhum requisito simétrico de **verificar antes** (critério de aceitação de candidato) ou **desfazer depois** (rollback). Some-se a ausência de controle de execução de jobs e o padrão fica claro: **capacidade de mudar rápido sem capacidade de conter o erro da mudança.** O agravante é [A-RSK-07](#7-riscos): parte do dano é irreversível por construção, porque desfazer vendas confirmadas colide com RNF-19 e com §0.

### A-TR-04 — Metas de zero com instrumentos de baixa resolução
**Riscos:** A-RSK-05, A-RSK-13, A-RSK-10; ponto A-SP-06
**Cenários:** A-CN-05, A-CN-09, A-CN-10, A-CN-13
**Driver ameaçado:** A-DR-04, A-DR-01, A-DR-02

A fonte usa "zero" e "100%" com frequência (RNF-01, 02, 03, 14, 19, 23, 24, 27), mas os instrumentos de verificação associados são de baixa frequência ou indiretos: auditoria **trimestral** para 0% de vazamento; divergência medida contra uma fonte que pode estar congelada; e ao menos uma meta internamente contraditória (RNF-02). Metas absolutas exigem detecção contínua; sem isso, o zero é aspiracional e não verificável.

### A-TR-05 — Base quantitativa e de método insuficiente para conclusões definitivas
**Riscos:** A-RSK-09, A-RSK-14, A-RSK-15; hipóteses A-H-01…A-H-15
**Cenários:** A-CN-14, A-CN-04a, todos
**Driver ameaçado:** validade da própria avaliação

Sete RNF carregam `[baseline sugerida]`, não há volumes globais (L-02), o TTL do HOLD é indefinido (L-07), e não houve votação de stakeholders. **A consequência é explícita: nenhum cenário desta avaliação pode ser declarado "atendido".** O que este documento entrega é o mapa do que precisa ser decidido — não um veredito de conformidade.

---

## 12. Síntese para os stakeholders

**O que a arquitetura documentada faz bem.** O núcleo de integridade é sólido e raro nesse estágio: ponto único de decisão de disponibilidade, exclusão mútua com desempate mecânico, alternativas comparadas com trade-offs explícitos ([§12.5](arquitetura.md#125-alternativas-consideradas-para-a-exclusão-mútua-e-por-que-a-proposta-acima-foi-preferida)), recusa fundamentada da reserva otimista, e reuso do mesmo mecanismo no lote B2B. Oito não-riscos foram confirmados (§10). A disciplina de marcar `[SUPOSIÇÃO]`/`[DECISÃO-SEM-REQUISITO]` tornou esta avaliação possível.

**Onde a avaliação encontra o risco real.** Não no caminho feliz — nos **caminhos de falha, na dependência de parceiros e na ausência de reversibilidade** (A-TR-01, A-TR-02, A-TR-03). O caminho feliz protege a restrição §0 com rigor; as bordas não estão especificadas, e é nelas que a restrição será testada.

**As três decisões que mais destravam análise, em ordem:**

1. **TTL do HOLD** (A-H-15 / L-07) — resolve simultaneamente A-CN-15, A-CN-11, A-TO-01 e A-TO-03. Nenhuma outra decisão isolada move tantos cenários.
2. **Política de modo degradado e fim de cadeia de retry** (A-H-08, A-H-02) — decide se a plataforma prefere não vender a vender errado. A restrição §0 sugere fortemente a resposta; ela precisa estar **escrita** para ser testável.
3. **Rollback e critério de aceitação de publicação** (A-H-07, A-H-06) — a contrapartida ausente de RNF-30/RNF-31.

**Três inconsistências que esta avaliação encontrou e que ainda não constavam de [§8.2 da arquitetura](arquitetura.md#82-conflitos)**, registradas para acréscimo àquele catálogo pelos responsáveis (não alteradas aqui):
- **RNF-02**: "100% bloqueadas" × "falso-negativo ≤0,1%/mês" ([A-RSK-13](#7-riscos)).
- **RNF-25 × RNF-23/RNF-28**: exclusão em ≤15 dias × retenção ≥5 anos ([A-RSK-10](#7-riscos)).
- **RF-25 × separação Catálogo/Estoque**: "quantidade de assentos promocionais" é dado de estoque publicado por um fluxo de catálogo ([A-NR-08](#10-não-riscos), premissa).

**Status.** Avaliação de Fase 1, sem workshop de stakeholders. Nenhum cenário declarado atendido; nenhuma hipótese tratada como fato; nenhuma implementação realizada; nenhuma evidência anterior alterada.

---

## 13. Nota de método

- **Método:** ATAM (Architecture Tradeoff Analysis Method), passos 1–6 executados sobre documentação; passos 7–9 (brainstorming e votação de cenários com stakeholders, reanálise) **não executados** — ver [A-RSK-14](#7-riscos).
- **Independência:** a avaliação não participou da produção de [arquitetura.md](arquitetura.md) e a trata como objeto de análise, incluindo suas próprias marcações de suposição.
- **Integridade da evidência:** nenhum arquivo pré-existente foi modificado ou removido. Este documento é aditivo. Conflitos e lacunas da fonte foram referenciados pelos IDs originais (L-xx, C-xx, S-xx, RSK-xx) e nunca reescritos.
- **Rastreabilidade:** todo cenário cita artefato e requisito; todo risco cita cenário e decisão; todo trade-off cita os dois atributos em tensão e a decisão que os coloca em tensão; todo não-risco declara a premissa que o sustenta.
- **Separação fato × hipótese:** métricas de RF/RNF aparecem como `[RNF-xx]`; valores marcados na fonte, como `[baseline]`; e tudo o mais como `[A-H-xx]` — hipótese pendente, jamais meta.

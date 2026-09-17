# Revisão de Painel — Arquiteto, Dados/ML, Segurança/LGPD, Operações/SRE, Custos e Facilitador ATAM

> **Natureza deste documento.** Relatório de **painel revisor multidisciplinar**, convocado para examinar [docs/arquitetura.md](arquitetura.md) à luz de [REQUISITOS/requisitos-funcionais-e-nao-funcionais.md](../REQUISITOS/requisitos-funcionais-e-nao-funcionais.md) e das [PERSONAS](../PERSONAS/), com a [avaliação ATAM independente](atam-avaliacao.md) já registrada como insumo de entrada. Este painel **não altera nenhum artefato-fonte** (README, REQUISITOS, PERSONAS, arquitetura.md, atam-avaliacao.md). Achados anteriores (L-xx, C-xx, S-xx, RSK-xx da arquitetura; A-DR/A-CN/A-SP/A-RSK/A-TO/A-NR/A-TR/A-H da ATAM) são **referenciados**, não reescritos.
>
> **Regra de evidência do painel** (idêntica à já adotada nos dois documentos-fonte): número é requisito só se estiver literal em RF-xx/RNF-xx; `[baseline sugerida]` é hipótese, não meta; toda afirmação sem lastro na fonte é marcada **[SUPOSIÇÃO]**, **[DECISÃO-SEM-REQUISITO]** ou **[HIPÓTESE-PAINEL]**. Nenhum ADR aqui é uma decisão final — é uma **recomendação de decisão** submetida à validação dos stakeholders (mesma ressalva de [atam-avaliacao.md §12](atam-avaliacao.md#12-síntese-para-os-stakeholders)).
> **Não há código nesta revisão.**

---

## 1. Composição do painel e mandato

| Papel | Lente | Pergunta que guia as objeções |
|---|---|---|
| **Arquiteto** | Coerência estrutural, limites de componente, decomposição | A decomposição em [§9](arquitetura.md#9-responsabilidades-limites-e-interfaces-dos-componentes) é suficiente e internamente consistente? |
| **Dados/ML** | Modelo de dados, pipelines analíticos, qualidade de dado, aprendizado de máquina | Onde há decisão orientada a dados/anomalia/ranking sem pipeline de dados definido? |
| **Segurança/LGPD** | Conformidade, isolamento, minimização de dados | O isolamento multi-tenant e o ciclo de vida de dados pessoais resistem a um cenário adversarial ou de auditoria? |
| **Operações/SRE** | Operabilidade, degradação, recuperação, runbooks | O que a equipe de operação faz quando o caminho feliz falha? |
| **Custos** | Custo de construir, operar e manter as garantias exigidas | Qual garantia declarada tem custo não reconhecido na fonte? |
| **Facilitador ATAM** | Processo, coerência entre artefatos, consolidação sem apagar divergência | Os artefatos (C4, sequência, pipelines batch, utility tree, riscos) contam a mesma história? |

---

## 2. Objeções fundamentadas por papel

Cada objeção cita o ponto exato da arquitetura, o requisito em tensão e por que a objeção é fundamentada (não é preferência estética).

### 2.1 Arquiteto

| ID | Objeção | Evidência | Fundamento |
|---|---|---|---|
| **OBJ-ARQ-01** | O "Barramento de Eventos" é `ContainerDb` no C4 ([§10.2](arquitetura.md#102-nível-2--contêineres)) mas a tabela de componentes ([§9](arquitetura.md#9-responsabilidades-limites-e-interfaces-dos-componentes)) diz explicitamente "não é fonte de verdade de nenhum dado — apenas propaga". `ContainerDb` no Mermaid C4 denota armazenamento persistente como responsabilidade primária; o texto diz o oposto. | [§9](arquitetura.md#9-responsabilidades-limites-e-interfaces-dos-componentes) vs. [§10.2](arquitetura.md#102-nível-2--contêineres) | Notação inconsistente com a própria narrativa — risco de leitor concluir que o barramento é durável quando a garantia de durabilidade declarada (RNF-19: 0 perda de reserva paga) está no Serviço de Estoque/Emissão, não no barramento. |
| **OBJ-ARQ-02** | O Catálogo "não guarda contagem de assentos vendidos" ([§9](arquitetura.md#9-responsabilidades-limites-e-interfaces-dos-componentes)), mas RF-25 permite ao gestor comercial definir "quantidade de assentos promocionais" por campanha — isso **é** uma contagem de assentos. A ATAM já flagou isso como premissa não confirmada em [A-NR-08](atam-avaliacao.md#10-não-riscos), mas a arquitetura em si nunca resolve onde esse número é materializado. | RF-25; [§9](arquitetura.md#9-responsabilidades-limites-e-interfaces-dos-componentes) linha Catálogo; [A-NR-08](atam-avaliacao.md#10-não-riscos) | Se "quantidade de assentos promocionais" vive no Catálogo, o limite "Catálogo não decide disponibilidade" ([§9](arquitetura.md#9-responsabilidades-limites-e-interfaces-dos-componentes)) é violado por construção — dois serviços passam a ter opinião sobre quantos assentos existem. Isso não é uma preferência: é a mesma classe de erro que a RESTRIÇÃO-CRÍTICA-01 existe para impedir. |
| **OBJ-ARQ-03** | O componente "BFF/API Gateway" ([§9](arquitetura.md#9-responsabilidades-limites-e-interfaces-dos-componentes)) recebe tráfego do canal do passageiro **e** da API B2B pública no C4 ([§10.2](arquitetura.md#102-nível-2--contêineres): `Rel(api_b2b, bff, "chamadas")`), mas a tabela de componentes descreve a API B2B como algo que "delega a Busca, Políticas e Reserva" diretamente, sem citar o BFF como intermediário. Os dois artefatos descrevem topologias diferentes para o mesmo tráfego. | [§9](arquitetura.md#9-responsabilidades-limites-e-interfaces-dos-componentes) (linha API B2B) vs. [§10.2](arquitetura.md#102-nível-2--contêineres) (relação `api_b2b -> bff`) | Se a API B2B passa pelo BFF, o BFF herda requisitos de RNF-09 (latência ≤500ms P95, disponibilidade ≥99,9%) que não estão na sua linha de requisitos-base (`[DECISÃO-SEM-REQUISITO]`, sem RNF citado). Se não passa, o C4 está errado. Qualquer leitura tem uma inconsistência para resolver. |

### 2.2 Dados/ML

| ID | Objeção | Evidência | Fundamento |
|---|---|---|---|
| **OBJ-DADOS-01** | Não existe, em nenhum artefato-fonte nem em [arquitetura.md](arquitetura.md), um componente de plataforma de dados/analytics que sustente RF-27 (dashboard de desempenho comercial), RF-28 (posicionamento competitivo/ranking) e RF-31 (detecção de padrões anômalos). O "Serviço de Antifraude/Observabilidade" ([§9](arquitetura.md#9-responsabilidades-limites-e-interfaces-dos-componentes)) é descrito só como consumidor de eventos, sem modelo de dados, sem definição de retenção analítica, sem separação entre dado transacional e dado analítico. | RF-27, RF-28, RF-31, RNF-07, RNF-26; [§9](arquitetura.md#9-responsabilidades-limites-e-interfaces-dos-componentes) | RNF-26 define anomalia como "volume de reembolsos ≥ 3x a média móvel dos últimos 7 dias" — isso é uma regra estatística simples, não exige ML, mas **exige uma série temporal armazenada e agregada por rota/companhia**, o que não tem componente nem armazenamento designado. RF-28 (ranking competitivo) sugere um modelo de comparação de preços entre companhias que não existe em lugar nenhum do C4. Nenhuma tecnologia é inventada aqui — apenas se registra a ausência de um componente lógico de dados analíticos, no mesmo espírito de [§9](arquitetura.md#9-responsabilidades-limites-e-interfaces-dos-componentes) marcar decisões sem requisito. |
| **OBJ-DADOS-02** | RNF-07 (dashboards ≤4s para até 12 meses de histórico, `[baseline sugerida]`) tensiona diretamente com RNF-27/RNF-28 (isolamento por tenant, 0% cross-tenant): consultas analíticas rápidas sobre 12 meses de dados **de todas as companhias** normalmente exigem um armazenamento agregado/desnormalizado — o oposto do isolamento estrito por tenant que RNF-27 exige para o caminho transacional. A ATAM já registrou o trade-off em termos de segurança ([A-TO-07](atam-avaliacao.md#9-trade-offs)), mas não em termos de **modelo de dados**: qual pipeline extrai, agrega e expõe esse dado sem recriar, na camada analítica, a superfície de vazamento que a camada transacional evita? | RNF-07, RNF-27; [A-TO-07](atam-avaliacao.md#9-trade-offs) | Sem um pipeline de ETL/agregação explicitamente desenhado com controle de acesso próprio, a arquitetura corre o risco de resolver o isolamento no banco transacional e reabri-lo no data warehouse/painel — exatamente o ponto que RNF-27 exige fechar. |
| **OBJ-DADOS-03** | O "limiar de frescor (staleness)" apontado pela ATAM como ausente ([A-H-05](atam-avaliacao.md#8-hipóteses-da-avaliação-pendentes--não-são-fatos), [A-RSK-05](atam-avaliacao.md#7-riscos)) é, na prática, um problema de **qualidade de dado de entrada** — não apenas de sincronização. Nenhum componente tem a responsabilidade de validar que o dado recebido do Adaptador de Integração é plausível antes de propagá-lo (ex.: um contador de estoque que nunca muda por 6 horas em uma rota de alta demanda é suspeito, mesmo sem erro de protocolo). | [A-H-05](atam-avaliacao.md#8-hipóteses-da-avaliação-pendentes--não-são-fatos); Adaptador de Integração em [§9](arquitetura.md#9-responsabilidades-limites-e-interfaces-dos-componentes) | Data quality/anomaly-on-ingestion é uma responsabilidade distinta de "sincronizar" e de "detectar fraude financeira" (RF-31 é sobre reembolsos, não sobre integridade do dado de estoque). Não há dono declarado para essa responsabilidade. |

### 2.3 Segurança/LGPD

| ID | Objeção | Evidência | Fundamento |
|---|---|---|---|
| **OBJ-SEG-01** | O conflito RNF-25 (exclusão de dados em ≤15 dias corridos) × RNF-23/RNF-28 (retenção de auditoria ≥5 anos) já identificado pela ATAM ([A-RSK-10](atam-avaliacao.md#7-riscos)) **envolve dados de menores** (RF-15, [família planejadora](../PERSONAS/passageiros/familia-turista-planejadora.md)) — o que sob a LGPD é dado de titular vulnerável, com escrutínio mais rígido, não menor. A arquitetura não distingue, na trilha de auditoria (RF-32, RNF-23), quais campos são identificáveis (nome, documento) e quais são necessários apenas para fins fiscais/financeiros (valor, data, ID de transação). | RNF-25, RNF-23, RNF-28, RF-15; [A-RSK-10](atam-avaliacao.md#7-riscos) | Sem uma decisão de **quais campos são anonimizáveis mantendo a trilha financeira íntegra**, qualquer implementação será obrigada a escolher entre violar a LGPD (não excluir) ou violar RNF-23 (perder rastreabilidade de auditoria) — as duas leituras possíveis hoje são não-conformidade. |
| **OBJ-SEG-02** | RNF-27 exige 0% de vazamento cross-tenant validado por **auditoria trimestral**. Detecção trimestral para uma meta de zero absoluto significa que um vazamento pode se acumular por até 90 dias antes de ser formalmente constatado — a ATAM já registrou isso como ponto de sensibilidade ([A-SP-06](atam-avaliacao.md#6-pontos-de-sensibilidade)) e trade-off ([A-TO-06](atam-avaliacao.md#9-trade-offs)). O painel de Segurança acrescenta: a fonte não define **detecção contínua** (ex.: alertas em tempo real de acesso cross-tenant) como complemento à auditoria periódica — e RF-30/RF-31 já provam que a plataforma tem apetite para alertas de anomalia em outros domínios (fraude), só não neste. | RNF-27; [A-SP-06](atam-avaliacao.md#6-pontos-de-sensibilidade); RF-31 | Inconsistência de rigor: a plataforma detecta anomalia de reembolso em ≤15 min (RNF-26) mas aceita detecção de vazamento de dados em até 90 dias — para uma meta declarada como mais crítica (0% vs. ≤0,1%). Não há requisito que force o mesmo padrão de vigilância para os dois riscos. |
| **OBJ-SEG-03** | O Serviço de Estoque de Assentos declara explicitamente "não conhece dados pessoais do passageiro" ([§9](arquitetura.md#9-responsabilidades-limites-e-interfaces-dos-componentes)) — um bom limite de minimização de dados — mas o HOLD é associado a "sessão" ([§12.1](arquitetura.md#121-entidades), [§12.2](arquitetura.md#122-máquina-de-estados-do-assento)). Não está definido se "sessão" é um identificador anônimo (token de checkout) ou se carrega, ainda que indiretamente, dado que permita reidentificação (ex.: se o `session_id` for derivado do CPF ou reutilizado entre compras do mesmo usuário). | [§9](arquitetura.md#9-responsabilidades-limites-e-interfaces-dos-componentes), [§12.1](arquitetura.md#121-entidades) | A afirmação "não conhece dados pessoais" só é verificável se o identificador de sessão for comprovadamente não-reidentificável a partir do próprio Serviço de Estoque. A fonte não define a natureza do identificador — o limite declarado pode estar certo na intenção e indefinido na prática. |

### 2.4 Operações/SRE

| ID | Objeção | Evidência | Fundamento |
|---|---|---|---|
| **OBJ-SRE-01** | Concordância plena com o achado mais grave da ATAM ([A-TR-01](atam-avaliacao.md#a-tr-01--comportamento-sob-falha-não-é-requisito-de-primeira-classe)): não há modo degradado definido para busca/checkout com o Estoque indisponível. O painel de Operações acrescenta a pergunta operacional concreta que falta: **quem decide, em tempo real de incidente, se o sistema entra em modo degradado, e com que autoridade/automação?** Nenhum runbook, alçada de decisão ou gatilho automático é mencionado — nem na arquitetura, nem nos RF/RNF de suporte (RF-16, RNF-21). | [A-RSK-02](atam-avaliacao.md#7-riscos), [A-SP-05](atam-avaliacao.md#6-pontos-de-sensibilidade) | Mesmo que a decisão de "recusar vender antes de arriscar overbooking" seja tomada (consistente com §0), falta o mecanismo operacional que a executa sob pressão de incidente real — sem isso, a decisão arquitetural correta pode não ser aplicada a tempo. |
| **OBJ-SRE-02** | O Hold Expiration Sweeper ([§12](arquitetura.md#12-modelo-de-reserva-de-passagem--exclusão-mútua-expiração-e-decisão-de-posse)) é um processo periódico crítico para RNF-03/RNF-06 (propagação de estoque), mas não há requisito nem seção que trate da **falha do próprio sweeper** — se ele parar de rodar, holds vencidos deixam de ser liberados e o estoque percebido cai silenciosamente, sem qualquer erro visível (nenhum retry, nenhum webhook falhando). Este é um segundo ponto de degradação silenciosa, adicional ao já registrado pela ATAM em [A-CN-05](atam-avaliacao.md#a-cn-05--degradação-silenciosa-dos-dados-a--a) (que fala do dado do parceiro, não do processo interno). | [§12.4](arquitetura.md#124-expiração-do-hold); [A-CN-05](atam-avaliacao.md#a-cn-05--degradação-silenciosa-dos-dados-a--a) | A expiração *lazy* (verificação no momento do próximo HOLD/CONFIRM) mitiga parcialmente — um assento nunca é vendido incorretamente por essa via —, mas a **disponibilidade percebida** (quantos assentos aparecem livres) fica sub-relatada indefinidamente sem o sweeper, o que pressiona RF-26 ("prevenindo overbooking" por transparência de estoque) por outro ângulo: falso indisponível, não overbooking, mas ainda uma falha de negócio sem alarme. |
| **OBJ-SRE-03** | RF-33/RNF-22 mencionam "SLA acordado em contrato" e RTO/RPO, mas nenhum artefato define **quem monitora o cumprimento desse SLA em tempo real** nem qual ação operacional segue uma violação (RSK-03 na arquitetura já registra a lacuna de valor; o painel de Operações registra a lacuna de **processo**, que é distinta). | L-03 (arquitetura); RF-33, RNF-22 | Um SLA sem processo de monitoramento e escalonamento operacional é um número contratual sem consequência operacional — a mediação de disputas (RF-33) fica sem gatilho de acionamento proativo, dependendo de reclamação do passageiro. |

### 2.5 Custos

| ID | Objeção | Evidência | Fundamento |
|---|---|---|---|
| **OBJ-CUSTO-01** | A retenção de auditoria ≥5 anos (RNF-23, RNF-28) aplicada a **todas** as trilhas de conciliação e logs de acesso, combinada com N companhias parceiras e potencialmente alto volume de transações, é um custo de armazenamento de longo prazo crescente e não dimensionado — a fonte não define volumes (L-02 da arquitetura), então o painel de Custos não pode nem estimar ordem de grandeza, apenas registrar que a decisão de "gravar tudo, sempre, por 5 anos" tem custo monotonicamente crescente sem política de tiering (dado recente em armazenamento rápido/caro, dado antigo em armazenamento frio/barato) mencionada em nenhum lugar. | RNF-23, RNF-28; L-02 (arquitetura) | Duas metas de retenção de "zero perda por 5 anos" sem estratégia de tiering custam significativamente mais do que a mesma garantia com tiering — e a garantia de auditoria (RNF-23) não exige *velocidade* de acesso aos dados antigos, apenas *existência* e *imutabilidade*, o que é compatível com armazenamento frio. |
| **OBJ-CUSTO-02** | O padrão de "ponto único de decisão de disponibilidade" (Serviço de Estoque, [A-AB-01](atam-avaliacao.md#32-abordagens-arquiteturais-identificadas-na-arquitetura-documentada)) que a ATAM corretamente identifica como necessário para RESTRIÇÃO-CRÍTICA-01 tem um custo de escala vertical: se a técnica de exclusão mútua escolhida for CAS/unique constraint sobre um único armazenamento lógico ([§12.3](arquitetura.md#123-regra-decisiva-de-posse-mecanismo-de-exclusão-mútua)), o custo de manter esse armazenamento com a disponibilidade de RNF-20 (≥99,9%) sob picos de contenção (voos quase esgotados, promoções) tende a crescer mais rápido que linearmente com o volume — e a fonte não define volumes (L-02) nem orçamento de infraestrutura. | [§12.3](arquitetura.md#123-regra-decisiva-de-posse-mecanismo-de-exclusão-mútua); [A-TO-02](atam-avaliacao.md#9-trade-offs); L-02 | Não é uma objeção contra a decisão (que o painel de Arquitetura e a ATAM já validaram como correta frente a §0), mas um alerta de que "consistência forte" tem uma curva de custo de infraestrutura sob contenção que ninguém dimensionou — e que pode ser o item de maior custo de operação de toda a plataforma, sem que a fonte o reconheça como driver de custo em lugar nenhum. |
| **OBJ-CUSTO-03** | O modelo de N adaptadores por companhia ([A-AB-05](atam-avaliacao.md#32-abordagens-arquiteturais-identificadas-na-arquitetura-documentada)) tem custo de manutenção proporcional ao número de parceiros e à heterogeneidade deles (já registrada pela persona da administradora: "cada nova companhia exige um processo diferente"). RNF-29 (`[baseline sugerida]`, onboarding ≤10 dias úteis) mede velocidade de entrada, não custo de manutenção contínua por adaptador — a fonte não tem nenhum requisito de **custo de manutenção por parceiro integrado**, o que significa que o crescimento do marketplace (mais companhias) cresce o custo de engenharia sem meta declarada de eficiência. | RNF-29; [A-TO-09](atam-avaliacao.md#9-trade-offs); [administradora](../PERSONAS/operacao-interna/administrador-da-plataforma.md) | Sucesso comercial (mais companhias parceiras) e custo de engenharia (mais adaptadores heterogêneos para manter) crescem juntos sem que a fonte reconheça esse acoplamento como risco de custo — só como risco de qualidade ([A-TR-02](atam-avaliacao.md#a-tr-02--a-qualidade-da-plataforma-é-refém-do-parceiro-menos-maduro)). |

### 2.6 Facilitador ATAM

| ID | Objeção (de processo/coerência) | Evidência | Fundamento |
|---|---|---|---|
| **OBJ-FAC-01** | A avaliação ATAM já registra explicitamente ([A-RSK-14](atam-avaliacao.md#7-riscos)) que sua própria priorização não foi votada por stakeholders. Este painel, ao produzir ADRs de recomendação (§4 abaixo), corre o **mesmo risco em segunda ordem**: recomendações de um painel sem representação direta de stakeholders de negócio (comercial das companhias, jurídico/LGPD da empresa, financeiro) têm o mesmo status de hipótese estruturada, não de decisão legítima. | [A-RSK-14](atam-avaliacao.md#7-riscos) | Coerência de método: se a ATAM já se autodeclarou "Fase 1 sem workshop", este painel deve se autodeclarar igualmente provisório — ver §7. |
| **OBJ-FAC-02** | Os diagramas de sequência batch ([§11.3](arquitetura.md#113-fluxo-batch--emissão-em-lote-b2b), [§11.4](arquitetura.md#114-fluxo-batch--conciliação-financeira)) — os "pipelines" mais próximos de um pipeline de dados na fonte — não aparecem na *utility tree* da ATAM como artefato citado, apenas como texto de risco (A-CN-04, A-CN-04a). A árvore de utilidade ([§4](atam-avaliacao.md#4-árvore-de-utilidade-utility-tree)) organiza cenários por driver de negócio, não por artefato, então essa omissão é esperada — mas nenhum documento faz o cruzamento explícito "este cenário de risco corresponde a este trecho exato do diagrama de sequência batch". O painel adiciona essa rastreabilidade no §3.3. | [§11.3](arquitetura.md#113-fluxo-batch--emissão-em-lote-b2b), [§11.4](arquitetura.md#114-fluxo-batch--conciliação-financeira); [§4](atam-avaliacao.md#4-árvore-de-utilidade-utility-tree) | Sem esse cruzamento, um leitor dos riscos A-RSK-04 não sabe, sem grep manual, exatamente qual seta do diagrama de sequência é a etapa vulnerável (`Con->>Con: aplicar chave de idempotência por evento` em [§11.4](arquitetura.md#114-fluxo-batch--conciliação-financeira) — não há trava de execução única antes dessa etapa). |

---

## 3. Divergências entre papéis (consolidadas, não resolvidas)

O mandato pede para não esconder divergência. As três abaixo são tensões reais entre papéis do painel, não apenas entre requisitos.

| ID | Divergência | Posição A | Posição B | Estado |
|---|---|---|---|---|
| **DIV-01** | **Frequência de verificação de isolamento cross-tenant (RNF-27).** | *Segurança* (OBJ-SEG-02): auditoria trimestral é insuficiente para uma meta de zero; propõe detecção contínua equivalente à de fraude (RNF-26, ≤15 min). | *Custos* (implícito em OBJ-CUSTO-01/03): monitoramento contínuo cross-tenant em tempo real tem custo de engenharia e de processamento proporcional ao volume de acessos, não apenas ao volume de vendas — pode ser ordens de magnitude mais caro que auditoria trimestral. | **Não resolvida.** Nenhum papel tem informação de volume (L-02) para arbitrar o trade-off custo×risco. Registrada como pergunta aberta PA-04. |
| **DIV-02** | **Onde materializar "quantidade de assentos promocionais" (RF-25).** | *Arquiteto* (OBJ-ARQ-02): deve estar exclusivamente no Serviço de Estoque, para não violar a fonte única de verdade (§0). | *Dados/ML* (OBJ-DADOS-01, leitura complementar): se estiver só no Estoque, o Catálogo/dashboard de campanhas (RF-25, RF-27) precisa consultar o Estoque para saber quantas unidades promocionais restam — criando acoplamento de leitura cross-serviço que a separação Catálogo/Estoque ([A-NR-08](atam-avaliacao.md#10-não-riscos)) tentava evitar. | **Não resolvida.** Ambos os papéis concordam que a separação atual está subespecificada; divergem em qual lado deveria cair a dependência. Endereçada como recomendação em ADR-06 (§4.6), não como consenso do painel. |
| **DIV-03** | **Modo degradado: recusar vender vs. vender com risco controlado.** | *Operações/SRE* (OBJ-SRE-01) e a leitura da ATAM ([A-SP-05](atam-avaliacao.md#6-pontos-de-sensibilidade)): a restrição §0 é absoluta, logo a única leitura compatível é recusar vender (fail-closed) quando o Estoque está indisponível. | *Arquiteto*, em objeção não formalizada como ID mas levantada em discussão de painel: fail-closed total em busca (não apenas em confirmação de venda) pode ser desnecessariamente conservador — a busca **exibe** disponibilidade agregada, não confirma venda; poderia continuar servindo resultados "como consultado por último", com aviso explícito de que a disponibilidade pode ter mudado, desde que a confirmação (HOLD/CONFIRM) permaneça fail-closed. | **Não resolvida — divergência genuína, não apenas de opinião.** Ambas as leituras são compatíveis com §0 se a fail-closed for aplicada apenas no ponto de decisão de posse (HOLD/CONFIRM) e não na exibição de busca. A ATAM não distinguiu "busca" de "confirmação" ao registrar A-SP-05/A-RSK-02; este painel torna essa distinção explícita e a devolve como pergunta aberta (PA-05) em vez de resolver por conta própria. |

---

## 3.3 Verificação de coerência entre C4, sequência, pipelines (batch) e utility tree/riscos

| Verificação | Resultado |
|---|---|
| Todo componente do C4 nível 2 ([§10.2](arquitetura.md#102-nível-2--contêineres)) aparece em pelo menos uma responsabilidade em [§9](arquitetura.md#9-responsabilidades-limites-e-interfaces-dos-componentes)? | **Sim**, com a ressalva de OBJ-ARQ-01 (notação `ContainerDb` do barramento). |
| Toda seta crítica do diagrama de sequência do caminho feliz ([§11.1](arquitetura.md#111-fluxo-online--busca-reserva-e-checkout-feliz)) corresponde a um componente do C4? | **Sim.** |
| O diagrama de concorrência ([§11.2](arquitetura.md#112-fluxo-online--concorrência-por-assento-garantia-de-exclusividade)) é consistente com a máquina de estados ([§12.2](arquitetura.md#122-máquina-de-estados-do-assento)) e a regra de posse ([§12.3](arquitetura.md#123-regra-decisiva-de-posse-mecanismo-de-exclusão-mútua))? | **Sim** — os três artefatos descrevem a mesma escrita condicional sem contradição. Este é o núcleo mais coerente de toda a arquitetura, confirmando [A-NR-01](atam-avaliacao.md#10-não-riscos). |
| Os "pipelines" batch ([§11.3](arquitetura.md#113-fluxo-batch--emissão-em-lote-b2b) lote B2B; [§11.4](arquitetura.md#114-fluxo-batch--conciliação-financeira) conciliação) aparecem refletidos na utility tree/riscos da ATAM? | **Parcialmente.** O lote aparece como A-CN-08b/A-AB-09/A-TO-10. A conciliação aparece como A-CN-04/A-CN-04a/A-RSK-04. Mas **nenhum dos dois diagramas de sequência contém uma trava visual de exclusão mútua entre execuções** — a ausência apontada por A-RSK-04 é visível diretamente no próprio Mermaid de [§11.4](arquitetura.md#114-fluxo-batch--conciliação-financeira) (não há guarda antes de `Job->>Con: conciliar(...)`), o que confirma, no nível do diagrama e não apenas do texto, que o risco é real e não apenas teórico. |
| O utility tree ([§4](atam-avaliacao.md#4-árvore-de-utilidade-utility-tree)) cobre algum componente do C4 que não aparece em nenhum cenário `A-CN-xx`? | **Sim — gap encontrado.** O "Painel Admin Interno" e o "Serviço de Antifraude/Observabilidade" aparecem no C4 e em [§9](arquitetura.md#9-responsabilidades-limites-e-interfaces-dos-componentes), mas o cenário mais próximo (A-CN-09, vazamento cross-tenant) trata do painel apenas como superfície de risco de acesso, nunca como objeto de cenário de **desempenho** (RNF-07, dashboards ≤4s) nem de **qualidade de dado** (OBJ-DADOS-01/02) — confirma a lacuna já registrada por Dados/ML no §2.2. |
| Os riscos da arquitetura (RSK-01…RSK-11, [§7](arquitetura.md#7-riscos-iniciais)) e os da ATAM (A-RSK-01…15) se sobrepõem sem contradição? | **Sim, sem contradição** — a ATAM refina e aprofunda os riscos da arquitetura (ex.: RSK-01 → A-RSK-01/02/03), nunca os contradiz. Isso é coerência de segunda ordem: dois documentos independentes convergem. |

---

## 4. ADRs (recomendações do painel, pendentes de decisão dos stakeholders)

> Cada ADR aqui é uma **proposta**, não uma decisão adotada — nenhuma altera [arquitetura.md](arquitetura.md). Onde a arquitetura já apresentou alternativas (ex. [§12.5](arquitetura.md#125-alternativas-consideradas-para-a-exclusão-mútua-e-por-que-a-proposta-acima-foi-preferida)), o painel referencia-as em vez de duplicá-las.

### ADR-01 — TTL do HOLD de assento

- **Status:** Proposta — pendente de validação com Gestor Comercial e Operações.
- **Contexto:** L-07 (arquitetura) e A-SP-01/A-RSK-01 (ATAM) identificam o TTL do HOLD como o parâmetro que decide simultaneamente três metas de zero-tolerância: RESTRIÇÃO-CRÍTICA-01/RNF-03 (não perder exclusividade), RNF-19 (0 perda de reserva paga) e RNF-05 (checkout ≤120s).
- **Forças:** TTL curto favorece giro de estoque em pico (protege RF-26/RNF-03); TTL longo protege pagador legítimo com meio de pagamento lento, especialmente Pix (protege RNF-19); nenhum valor é fato elicitado.
- **Alternativas:** as três faixas já propostas em [§12.4](arquitetura.md#124-expiração-do-hold) (curto 2–5 min, médio 10–15 min, longo 20–30 min); e uma quarta não considerada na fonte — **TTL diferenciado por meio de pagamento** (TTL curto para cartão com aprovação quase instantânea; TTL mais longo apenas para Pix, cujo tempo de confirmação é externo à plataforma).
- **Decisão recomendada pelo painel:** adotar **TTL diferenciado por meio de pagamento** como hipótese de trabalho a validar — resolve o trade-off de A-TO-01/A-TO-03 sem escolher um único valor médio que penaliza todos os meios de pagamento pelo pior caso. Cartão: faixa curta (2–5 min). Pix: faixa média-longa (10–15 min), a validar contra o tempo real de confirmação dos provedores de Pix considerados `[HIPÓTESE-PAINEL]`.
- **Consequências:** exige que o HOLD carregue o meio de pagamento pretendido no momento da criação (dado adicional não modelado em [§12.1](arquitetura.md#121-entidades)); reduz, mas não elimina, a chance do cenário A-CN-11 (pagamento aprovado com HOLD já expirado).
- **Riscos que permanecem:** A-RSK-01, A-RSK-11 — a compensação para o caso-limite (Pix aprovado exatamente após expiração) ainda não tem resposta e não pode ser inventada aqui.
- **Requisitos vinculados:** RESTRIÇÃO-CRÍTICA-01, RNF-03, RNF-05, RNF-19; L-07.

### ADR-02 — Comportamento de fail-closed diferenciado entre busca e confirmação

- **Status:** Proposta — depende da resolução de DIV-03.
- **Contexto:** A-SP-05/A-RSK-02 (ATAM) identificam a ausência de modo degradado quando o Estoque está indisponível.
- **Forças:** fail-closed total protege §0 ao custo de disponibilidade (RNF-20); fail-open na busca (exibir última disponibilidade conhecida) preserva parte da experiência sob incidente sem violar §0, desde que a confirmação de posse permaneça bloqueada.
- **Alternativas:** (a) fail-closed total (busca e confirmação recusam); (b) fail-closed apenas na confirmação, busca degrada para "disponibilidade pode estar desatualizada"; (c) fail-open total (rejeitada de imediato — viola §0 diretamente, não é alternativa viável).
- **Decisão recomendada pelo painel:** (b) — fail-closed **apenas no ponto de decisão de posse** (HOLD/CONFIRM), com a busca podendo continuar servindo o último estado conhecido acompanhado de aviso explícito de possível defasagem, nunca de uma promessa de disponibilidade garantida.
- **Consequências:** exige que a UI/BFF trate e sinalize esse estado degradado — não modelado em nenhum requisito de usabilidade atual (RNF-13, RNF-14, RNF-18 tratam do caminho normal). É uma **decisão sem requisito de UX correspondente**, a registrar como gap (§6).
- **Riscos que permanecem:** A-RSK-02, A-RSK-08 — o painel reduz o escopo do risco (de "toda a busca" para "apenas a exibição sob incidente"), não o elimina.
- **Requisitos vinculados:** RESTRIÇÃO-CRÍTICA-01, RNF-20, RNF-03.

### ADR-03 — Fim de cadeia de retry do webhook de sincronização (RNF-10)

- **Status:** Proposta — pendente de definição de contrato com companhias parceiras.
- **Contexto:** A-RSK-03 (ATAM): RNF-10 define 3 tentativas (1s/5s/30s, timeout 10s) e não define o que ocorre após a 3ª falha.
- **Forças:** continuar vendendo com dado desatualizado arrisca overbooking (§0); suspender vendas da companhia afetada protege §0 ao custo de receita e de experiência para aquele parceiro específico.
- **Alternativas:** (a) manter vendendo com o último dado conhecido (rejeitada — viola §0 sob a ótica deste painel, mesma lógica de ADR-02); (b) suspender novas confirmações de venda para a companhia afetada até a sincronização ser restabelecida, mantendo a busca visível com aviso; (c) suspender também a exibição da oferta daquela companhia na busca.
- **Decisão recomendada pelo painel:** (b) — suspender **confirmação** (CONFIRM), não necessariamente a exibição em busca, replicando a mesma distinção de ADR-02 e mantendo consistência entre as duas decisões de degradação.
- **Consequências:** exige um estado "tenant degradado" no Serviço de Estoque/Adaptador, não modelado em [§12.1](arquitetura.md#121-entidades); precisa de um sinal de alerta ao Painel Admin Interno (RF-30/RF-31) para visibilidade operacional — reforça OBJ-SRE-01.
- **Riscos que permanecem:** A-RSK-03, A-RSK-12 (parceiro sem contrato canônico) — este ADR não resolve a causa raiz (heterogeneidade de parceiros), apenas contém o sintoma.
- **Requisitos vinculados:** RNF-10, RNF-06, RNF-22, RF-26; A-RSK-03, A-RSK-12.

### ADR-04 — Detecção contínua de isolamento cross-tenant como complemento à auditoria trimestral

- **Status:** Proposta — depende de DIV-01 (custo × risco, sem dados de volume para arbitrar).
- **Contexto:** OBJ-SEG-02, A-SP-06/A-TO-06 (ATAM): meta de 0% de vazamento verificada apenas trimestralmente.
- **Forças:** detecção contínua reduz a janela de exposição de até 90 dias para próxima de tempo real; tem custo de engenharia e processamento (OBJ-CUSTO-01) não dimensionável sem volumes (L-02).
- **Alternativas:** (a) manter apenas auditoria trimestral (aceita o risco, não recomendada pelo painel de Segurança); (b) adicionar checagem automatizada de autorização em cada acesso cross-recurso, com alerta imediato em qualquer tentativa fora de escopo do papel (RBAC), complementando — não substituindo — a auditoria trimestral, que passaria a servir como verificação de completude e não como único mecanismo de detecção; (c) auditoria contínua completa equivalente à detecção de fraude (RNF-26).
- **Decisão recomendada pelo painel:** (b) — é a opção de menor custo incremental porque reaproveita o RBAC já exigido por RNF-27, adicionando apenas alerta em vez de um novo pipeline analítico (evitaria a divergência de custo apontada em DIV-01).
- **Consequências:** não elimina vazamento por lógica de negócio incorreta (ex.: um agregador do Painel Admin Interno mal implementado) — cobre acesso indevido por credencial, não por bug de agregação legítima mal escopada.
- **Riscos que permanecem:** parte de A-RSK-cross-tenant permanece — bug de agregação (RF-30) não é coberto por controle de acesso.
- **Requisitos vinculados:** RNF-27, RNF-28, RF-30.

### ADR-05 — Contrato canônico mínimo de integração com companhias parceiras

- **Status:** Proposta — pendente de definição comercial/jurídica com companhias.
- **Contexto:** A-RSK-12/A-TR-02 (ATAM) e OBJ-CUSTO-03: não há piso técnico exigido do parceiro nem contrato canônico de estoque.
- **Forças:** exigir piso técnico alto filtra parceiros incapazes de sincronizar em ≤2s (RNF-06), reduzindo risco de A-TR-02, mas tensiona RNF-29 (onboarding ≤10 dias úteis) e o modelo de negócio de marketplace (mais parceiros = mais receita).
- **Alternativas:** (a) piso técnico único e rígido para todo parceiro (rejeitada — inviabiliza onboarding rápido de parceiros menores); (b) contrato canônico de dados (formato de evento de estoque) obrigatório, com **níveis de capacidade declarados** (ex.: "Nível 1: sincronização ≤2s" vs. "Nível 2: sincronização em lote/polling"), e o nível declarado determina o TTL de HOLD e a política de fail-closed (ADR-03) aplicada àquele parceiro especificamente; (c) sem contrato canônico (mantém status quo, não recomendada).
- **Decisão recomendada pelo painel:** (b) — segmentar parceiros por capacidade declarada em vez de aplicar uma regra única, alinhando-se ao próprio espírito de "marketplace" (README) sem sacrificar §0: um parceiro Nível 2 simplesmente teria TTL de HOLD mais conservador e talvez menor limite de vendas simultâneas por rota, não uma regra de negócio diferente.
- **Consequências:** aumenta a complexidade do Adaptador de Integração (precisa tratar por nível) — custo de engenharia adicional reconhecido explicitamente aqui (liga-se a OBJ-CUSTO-03).
- **Riscos que permanecem:** A-RSK-12 é mitigado, não eliminado — parceiros ainda podem se degradar dentro do próprio nível declarado.
- **Requisitos vinculados:** RNF-06, RNF-10, RNF-22, RNF-29, RF-26, RF-29.

### ADR-06 — Materialização de "quantidade de assentos promocionais" (RF-25) no Serviço de Estoque

- **Status:** Proposta — resolve DIV-02 com recomendação, não consenso pleno do painel (Dados/ML mantém reserva sobre acoplamento de leitura).
- **Contexto:** OBJ-ARQ-02, DIV-02: RF-25 exige que a promoção carregue "quantidade de assentos promocionais", que é dado de disponibilidade.
- **Forças:** manter a contagem no Estoque preserva a fonte única de verdade (§0); manter no Catálogo simplifica a publicação self-service (RNF-31, ≤1 min) mas duplica a noção de "quantos assentos existem".
- **Alternativas:** (a) contagem no Catálogo, sincronizada por evento para o Estoque (risco de divergência, rejeitada); (b) contagem exclusivamente no Estoque, com o Catálogo apenas referenciando um identificador de campanha e delegando a decisão de "quantos assentos restam nesta campanha" ao Estoque via consulta; (c) sem separação — fundir Catálogo e Estoque (rejeitada, pois anula o limite já validado em [A-NR-08](atam-avaliacao.md#10-não-riscos)).
- **Decisão recomendada pelo painel:** (b) — o Estoque passa a reconhecer "classe promocional" como uma dimensão adicional de `(voo_id, assento_id)` ou de `InventoryCounter` ([§12.1](arquitetura.md#121-entidades)), e o Catálogo consulta (não escreve) essa contagem para exibição em RF-27/RF-28.
- **Consequências:** a publicação de campanha (RNF-31, ≤1 min) passa a depender de uma escrita no Estoque, não apenas no Catálogo — pode tensionar o SLA de publicação sem gate técnico se o Estoque estiver sob alta contenção no momento da publicação.
- **Riscos que permanecem:** nenhum risco novo eliminado; risco de acoplamento reconhecido, não removido — Dados/ML mantém objeção registrada em DIV-02.
- **Requisitos vinculados:** RF-25, RF-27, RF-28, RNF-31; RESTRIÇÃO-CRÍTICA-01.

### ADR-07 — Harmonização RNF-01 × RNF-14 (exceção de tarifa dinâmica)

- **Status:** Proposta — pendente de decisão comercial sobre quem absorve a variação de preço.
- **Contexto:** C-02 (arquitetura), A-SP-03/A-TO-08 (ATAM): RNF-01 admite exceção de tarifa dinâmica (≥5 min, sinalizada); RNF-14 exige 0% sem ressalva.
- **Forças:** admitir a exceção protege receita/paridade tarifária da companhia; não admitir protege a confiança do passageiro de forma absoluta, transferindo o risco de variação para a plataforma ou companhia.
- **Alternativas:** (a) RNF-14 prevalece sem exceção — preço é congelado no instante da busca e a plataforma/companhia absorve qualquer variação até a confirmação; (b) RNF-01 prevalece — variação é permitida e sinalizada, RNF-14 é lido como aplicável apenas dentro da mesma janela de tarifa; (c) híbrido — preço é congelado por sessão (já proposto em [§9](arquitetura.md#9-responsabilidades-limites-e-interfaces-dos-componentes), Busca & Precificação) e a exceção de RNF-01 só se aplica **entre sessões diferentes**, nunca dentro da mesma sessão de checkout.
- **Decisão recomendada pelo painel:** (c) — é a única leitura que satisfaz literalmente as duas métricas ao mesmo tempo: dentro de uma sessão, divergência é 0% (satisfaz RNF-14); entre sessões, a tarifa dinâmica pode ter mudado (satisfaz RNF-01). Isso já é implícito no desenho de "congelamento de preço de sessão" ([§9](arquitetura.md#9-responsabilidades-limites-e-interfaces-dos-componentes)), mas nunca foi declarado como a harmonização formal do conflito C-02.
- **Consequências:** exige definir precisamente o que encerra uma "sessão" para fins de preço (abertura do checkout? expiração do HOLD? fechamento da aba?) — novo parâmetro não modelado, análogo ao TTL do HOLD (ADR-01), mas não necessariamente do mesmo valor.
- **Riscos que permanecem:** RSK-03 (arquitetura) passa de "não harmonizado" para "harmonizado, mas com novo parâmetro indefinido (duração da sessão de preço)".
- **Requisitos vinculados:** RNF-01, RNF-14, RF-03; C-02.

### ADR-08 — Resolução da inconsistência interna de RNF-02 (100% × falso-negativo ≤0,1%)

- **Status:** Proposta — pendente de esclarecimento com quem redigiu o requisito original (fora do escopo deste painel alterar a fonte).
- **Contexto:** A-RSK-13 (ATAM): "bloquear 100% das reservas fora de política" e "falso-negativo ≤0,1%/mês" não podem ser ambos verdadeiros.
- **Forças:** nenhuma alternativa é neutra — é uma contradição lógica, não um trade-off de engenharia.
- **Alternativas:** (a) ler "100%" como aspiração/meta de processo e "≤0,1%" como a métrica operacional real de aceitação (interpretação recomendada); (b) ler "100%" como meta rígida e tratar qualquer falso-negativo como incidente crítico, tornando "≤0,1%" apenas um limite de alerta, não de aceitação; (c) devolver à origem para reformulação textual (não é uma decisão de arquitetura, é uma correção de requisito — fora do mandato deste painel).
- **Decisão recomendada pelo painel:** (a) para fins de arquitetura de curto prazo — o Motor de Políticas B2B ([§9](arquitetura.md#9-responsabilidades-limites-e-interfaces-dos-componentes)) é dimensionado para a métrica operacional verificável (≤0,1%), e "100%" é tratada como a meta que orienta a evolução do motor, não como critério de aceite binário — **mas o painel recomenda formalmente que REQUISITOS §4.1/RNF-02 seja revisado por quem tem autoridade sobre a fonte**, já que nenhuma leitura de arquitetura resolve uma contradição textual.
- **Consequências:** nenhuma mudança de componente; é uma decisão de **interpretação**, não de projeto.
- **Riscos que permanecem:** A-RSK-13 permanece até a fonte ser corrigida — este ADR não altera REQUISITOS.
- **Requisitos vinculados:** RNF-02, RF-20; A-RSK-13.

### ADR-09 — Precedência entre exclusão LGPD (RNF-25) e retenção de auditoria (RNF-23/RNF-28)

- **Status:** Proposta — pendente de parecer jurídico/DPO, fora do mandato técnico deste painel para decisão final.
- **Contexto:** A-RSK-10 (ATAM), OBJ-SEG-01: exclusão em ≤15 dias × retenção ≥5 anos, envolvendo dados de menores.
- **Forças:** LGPD permite retenção de dados necessários ao cumprimento de obrigação legal/regulatória mesmo após pedido de exclusão (isto é entendimento jurídico geral, **não extraído da fonte** — `[HIPÓTESE-PAINEL]`, a validar com DPO/jurídico); a fonte não faz essa distinção nem define quais campos são "necessários à obrigação legal" vs. "dado pessoal excluível".
- **Alternativas:** (a) excluir todo dado pessoal identificável do titular em 15 dias, mantendo na trilha de auditoria apenas identificadores pseudonimizados e os campos financeiros exigidos por obrigação fiscal/regulatória; (b) reter tudo por 5 anos e não atender pedidos de exclusão de titulares com transação registrada (não recomendada — contraria RNF-25 e a LGPD); (c) excluir tudo, inclusive de trilhas financeiras (não recomendada — viola RNF-23 e possivelmente obrigação fiscal).
- **Decisão recomendada pelo painel:** (a) — pseudonimização dos campos de identificação pessoal na trilha de auditoria após o prazo de exclusão, preservando os campos financeiros/transacionais necessários à conciliação (RF-32) e à obrigação fiscal (RF-11).
- **Consequências:** exige decisão jurídica sobre **qual conjunto de campos** é obrigação legal (fora do mandato deste painel); exige um mecanismo de pseudonimização não modelado em nenhum componente atual.
- **Riscos que permanecem:** A-RSK-10 — a decisão de quais campos entram em cada categoria continua pendente de parecer jurídico, não resolvida por este ADR.
- **Requisitos vinculados:** RNF-25, RNF-23, RNF-28, RF-15, RF-32; A-RSK-10.

---

## 5. Matriz requisito → decisão → componente → evidência

> Cobertura focada nos ASRs ([§3](arquitetura.md#3-requisitos-arquiteturalmente-significativos-asr)) e nos requisitos de maior criticidade (P0/P1 em [§5](arquitetura.md#5-drivers-priorizados)). Não é exaustiva para os 33+33 RF/RNF — os não listados aqui estão cobertos apenas descritivamente em [§9](arquitetura.md#9-responsabilidades-limites-e-interfaces-dos-componentes) sem decisão de projeto adicional deste painel.

| Requisito | Decisão arquitetural | Componente(s) | Evidência (arquitetura / ATAM / painel) |
|---|---|---|---|
| RESTRIÇÃO-CRÍTICA-01 | Escrita condicional atômica (CAS) sobre `(voo_id, assento_id)`; estado único ativo | Serviço de Estoque (Seat Repo, CAS Guard) | [§12.3](arquitetura.md#123-regra-decisiva-de-posse-mecanismo-de-exclusão-mútua); [A-NR-01](atam-avaliacao.md#10-não-riscos) |
| RNF-03 / RF-26 | Reconciliação event-driven via Barramento de Eventos + Adaptador por companhia | Estoque, Adaptador de Integração, Barramento de Eventos | [§9](arquitetura.md#9-responsabilidades-limites-e-interfaces-dos-componentes), [§12.4](arquitetura.md#124-expiração-do-hold); OBJ-ARQ-01 (notação a corrigir) |
| RNF-06 / RNF-10 | Webhooks com retry/backoff (3 tentativas); fim de cadeia não definido | Adaptador de Integração | RNF-10 literal; **ADR-03** (recomendação de suspensão de CONFIRM após esgotar retries) |
| RNF-05 / RNF-19 / RNF-13 | HOLD com TTL antes do pagamento; TTL indefinido | Reserva & Checkout, Estoque | [§12.4](arquitetura.md#124-expiração-do-hold); **ADR-01** (TTL diferenciado por meio de pagamento) |
| RNF-01 / RNF-14 (C-02) | Congelamento de preço de sessão | Busca & Precificação | [§9](arquitetura.md#9-responsabilidades-limites-e-interfaces-dos-componentes); **ADR-07** (harmonização proposta) |
| RNF-27 / RF-19 / RF-20 | Armazenamento por tenant + RBAC, auditoria trimestral | Armazenamento por tenant, RBAC | RNF-27 literal; **ADR-04** (detecção contínua complementar recomendada) |
| RNF-24 | Tokenização PCI-DSS | Serviço de Pagamentos | RNF-24 literal; nenhuma objeção do painel — considerado bem coberto |
| RNF-25 vs. RNF-23/RNF-28 | Não resolvido na fonte | Armazenamento por tenant, Conciliação Financeira | A-RSK-10; **ADR-09** (pseudonimização proposta, pendente de parecer jurídico) |
| RNF-02 / RF-20 | Motor de políticas B2B configurável sem deploy | Motor de Políticas B2B | A-RSK-13 (inconsistência textual); **ADR-08** |
| RF-32 / RNF-23 | Conciliação batch idempotente por chave de evento; sem exclusão mútua entre execuções | Conciliação Financeira | [§11.4](arquitetura.md#114-fluxo-batch--conciliação-financeira); A-RSK-04 — **sem ADR do painel**: recomenda-se apenas registrar como experimento (§9, EXP-04) |
| RF-30 / RF-31 / RNF-26 | Consolidação de indicadores + alertas de anomalia (regra estatística simples) | Serviço de Antifraude/Observabilidade | [§9](arquitetura.md#9-responsabilidades-limites-e-interfaces-dos-componentes); OBJ-DADOS-01 (componente de dados/plataforma analítica não definido) |
| RF-27 / RF-28 / RNF-07 | Dashboards (`[baseline sugerida]`) sem pipeline analítico definido | — (**gap**, ver §6) | OBJ-DADOS-01/02 |
| RF-21 / RNF-08 | Reuso do mecanismo de HOLD/CONFIRM no lote, sem atalho | Emissão em Lote B2B, Reserva & Checkout | [§11.3](arquitetura.md#113-fluxo-batch--emissão-em-lote-b2b); [A-NR-03](atam-avaliacao.md#10-não-riscos) |
| RNF-29 / RF-29 | Onboarding padronizado; sem contrato canônico de estoque por nível de parceiro | Onboarding (processo, sem componente de sistema dedicado) | A-RSK-12; **ADR-05** (segmentação por nível de capacidade) |
| RF-25 | Campanha com "quantidade de assentos promocionais" | Catálogo — **e** Estoque (não resolvido na fonte) | OBJ-ARQ-02, DIV-02; **ADR-06** |
| RNF-33 / RNF-28 / RNF-09 | APIs versionadas, OAuth2, retrocompatibilidade ≥12 meses | API B2B pública | Requisitos literais; nenhuma objeção crítica do painel |

---

## 6. Requisitos sem cobertura arquitetural

- **RF-27, RF-28, RNF-07** — dashboards e posicionamento competitivo não têm componente de dados/analytics designado (OBJ-DADOS-01/02). Não há decisão de arquitetura, apenas o requisito funcional.
- **RF-06** (alertas de preço por rota/data) — citado em REQUISITOS §3.1, mas **nenhum componente em [§9](arquitetura.md#9-responsabilidades-limites-e-interfaces-dos-componentes) o menciona**. Nem a arquitetura nem a ATAM o tratam. Gap novo, encontrado por este painel.
- **RF-05, RF-17** (regras de remarcação/cancelamento visíveis e self-service) — citados na tabela de personas ([§2](arquitetura.md#2-stakeholders--personas)) mas sem componente dedicado em [§9](arquitetura.md#9-responsabilidades-limites-e-interfaces-dos-componentes); presume-se cobertos implicitamente pela Busca & Precificação e por um fluxo pós-venda não modelado — **gap de modelagem, não necessariamente de requisito atendido**.
- **RNF-16** (SUS ≥70, `[baseline sugerida]`) — depende de teste de usabilidade com usuários reais; nenhuma decisão de arquitetura pode cobri-lo, é um requisito de processo de produto, não de sistema.
- **Modo degradado de UX** para os estados propostos em ADR-02/ADR-03 — nenhum requisito de usabilidade (RNF-13/14/18) cobre como comunicar disponibilidade incerta ao usuário.

## 6.2 Decisões sem requisito (consolidado)

Já marcadas individualmente na fonte como `[DECISÃO-SEM-REQUISITO]`; consolidadas aqui para visão única: BFF/API Gateway; técnica exata de exclusão mútua (CAS); mecanismo/tecnologia do Barramento de Eventos; mecanismo do Hold Expiration Sweeper; provedor de PSP/adquirente; provedor de notificação (e-mail/WhatsApp); emissor fiscal (NF-e/SEFAZ). Este painel adiciona: **componente de plataforma de dados/analytics** (necessário para RF-27/RF-28/RNF-07, mas nunca declarado nem como requisito nem como decisão) — é uma ausência de decisão, não uma decisão sem requisito, distinção relevante para quem for atuar sobre este documento.

---

## 7. Suposições (do painel, adicionais às já registradas em [arquitetura.md §8.3](arquitetura.md#83-suposicoes) e [atam-avaliacao.md §8](atam-avaliacao.md#8-hipóteses-da-avaliação-pendentes--não-são-fatos))

- **SUP-PAINEL-01** — LGPD permite retenção de dado necessário a obrigação legal mesmo após pedido de exclusão do titular (base para ADR-09); é entendimento jurídico geral, não extraído da fonte, e não substitui parecer de DPO.
- **SUP-PAINEL-02** — Pix e cartão de crédito têm tempos de confirmação estruturalmente diferentes (base para ADR-01 — TTL diferenciado); plausível por conhecimento de domínio de pagamentos, não afirmado nas personas nem nos requisitos.
- **SUP-PAINEL-03** — RNF-26 (anomalia de reembolso via média móvel) é implementável como regra estatística simples, sem exigir aprendizado de máquina; a fonte não usa o termo "ML" em lugar nenhum, e este painel não o introduz como requisito, apenas descarta a necessidade dele para RNF-26 especificamente — RF-28 (ranking competitivo) permanece aberto quanto à necessidade de um modelo mais sofisticado.
- **SUP-PAINEL-04** — O papel de Custos assume que armazenamento frio/tiering é tecnicamente viável para dados de auditoria imutáveis sem violar a garantia de existência/imutabilidade de RNF-23 — não há requisito de latência de leitura para dados de auditoria antigos na fonte que contradiga essa suposição, mas também não há confirmação explícita.

---

## 8. Perguntas abertas (do painel, adicionais às de [arquitetura.md §8.5](arquitetura.md#85-perguntas-abertas))

1. **PA-01.** A API B2B pública passa pelo BFF/Gateway ou o acessa diretamente os serviços internos? (OBJ-ARQ-03 — o C4 e a tabela de componentes divergem.)
2. **PA-02.** "Quantidade de assentos promocionais" (RF-25) deve ser escrita no Estoque ou no Catálogo? (DIV-02/ADR-06.)
3. **PA-03.** Existe, ou deveria existir, um componente de plataforma de dados/analytics para RF-27/RF-28/RNF-07, e quem o patrocina (produto, dados, ou é responsabilidade de cada serviço individualmente)? (OBJ-DADOS-01.)
4. **PA-04.** Qual o orçamento/tolerância de custo para detecção contínua de isolamento cross-tenant, versus manter apenas auditoria trimestral? (DIV-01 — não arbitrável sem volumes, L-02.)
5. **PA-05.** O modo degradado de indisponibilidade do Estoque deve bloquear também a **exibição** de busca, ou apenas a **confirmação** de posse? (DIV-03.)
6. **PA-06.** Quais campos da trilha de auditoria financeira (RF-32/RNF-23) são estritamente necessários por obrigação legal/fiscal, e quais são dado pessoal excluível sob LGPD? (ADR-09 — requer parecer jurídico/DPO, fora do escopo deste painel.)
7. **PA-07.** RF-06 (alerta de preço por rota/data) tem algum componente de arquitetura previsto, ou é um requisito ainda não decompuesto? (Gap novo, §6.)
8. **PA-08.** Existe apetite de negócio para segmentar parceiros por "nível de capacidade técnica" (ADR-05), mesmo que isso signifique tratamento comercial/contratual diferenciado entre companhias parceiras?

---

## 9. Riscos aceitos (pelo painel, nesta fase, para fins de progresso — não para fins de implementação sem revisão)

> "Aceito" aqui significa: o painel não recomenda bloquear o avanço da arquitetura por causa deste risco especificamente, desde que ele permaneça visível e monitorado — não significa que o risco esteja mitigado.

| Risco aceito | Por quê | Condição de revisão |
|---|---|---|
| A-RSK-09 (dimensionamento sem volumes, L-02) | Nenhuma arquitetura pode resolver ausência de dado de negócio; é um risco de elicitação, não de projeto. | Revisar assim que volumes reais forem fornecidos pelos stakeholders. |
| A-RSK-15 (suporte 24/7 como compromisso operacional, não de sistema) | Fora do escopo de decisão arquitetural — é dimensionamento de equipe/operação. | Revisar quando o modelo de operação de suporte for definido. |
| OBJ-CUSTO-02 (custo de escala do Estoque sob contenção) | A decisão de consistência forte sobre disponibilidade já é a correta frente a §0; o custo é uma consequência aceita da restrição inegociável, não um erro de projeto. | Revisar com dados reais de contenção em produção/homologação. |
| A-RSK-14 (priorização sem stakeholders, incluindo a deste próprio painel) | Estrutural desta fase de documentação; não é um defeito a corrigir agora, é uma limitação de método a ser suprida pelo workshop de votação. | Revisar após o workshop com stakeholders. |

**Riscos explicitamente NÃO aceitos como está** — recomenda-se resolução antes de avançar para implementação: A-RSK-01/A-RSK-11 (TTL indefinido — ADR-01 é só recomendação, decisão final pendente), A-RSK-02 (modo degradado — ADR-02 pendente de DIV-03), A-RSK-03 (fim de retry — ADR-03 pendente), A-RSK-10 (LGPD × retenção, envolve menores — ADR-09 pendente de jurídico), A-RSK-13 (RNF-02 contraditório — requer correção da fonte, fora do mandato deste painel).

---

## 10. Próximos experimentos recomendados

1. **EXP-01 — Teste de carga de contenção de assento.** Simular concorrência real sobre o mesmo `(voo_id, assento_id)` sob diferentes volumes, para dimensionar a latência de rejeição (A-H-01) e validar o custo de escala apontado em OBJ-CUSTO-02, antes de fixar tecnologia de armazenamento (L-01).
2. **EXP-02 — Simulação de TTL diferenciado por meio de pagamento (ADR-01).** Medir, com dados reais ou de piloto, o tempo de confirmação de Pix vs. cartão para calibrar as faixas propostas em vez de adotar um valor único por intuição.
3. **EXP-03 — Chaos test de esgotamento de retry de webhook (ADR-03).** Forçar falha das 3 tentativas de um adaptador simulado e verificar se o estado "tenant degradado" proposto é suficiente para impedir CONFIRM sem impedir busca.
4. **EXP-04 — Teste de execução concorrente do job de conciliação (A-RSK-04).** Disparar duas execuções do mesmo período deliberadamente e verificar se a chave de idempotência por evento realmente produz resultado idêntico ao de uma execução única, ou se há janela de corrida na leitura agregada.
5. **EXP-05 — Spike de pseudonimização de trilha de auditoria (ADR-09).** Protótipo (documentação de esquema, não implementação de produção) de quais campos poderiam ser pseudonimizados sem quebrar a capacidade de conciliação (RF-32), para levar ao parecer jurídico com uma proposta concreta em vez de uma pergunta aberta.
6. **EXP-06 — Modelo de custo de retenção com tiering (OBJ-CUSTO-01).** Estimativa de custo comparando "tudo em armazenamento único por 5 anos" vs. "tiering por idade do dado", mesmo sem volumes reais (usar cenários de sensibilidade: baixo/médio/alto volume) para dar ao negócio uma faixa de decisão em vez de nenhuma informação.
7. **EXP-07 — Prototipagem do painel único de saúde (RF-30) com controle de acesso por linha.** Validar se é possível ter agregação cross-tenant legítima (RF-30) sem reabrir a superfície de vazamento que RNF-27 fecha no lado transacional (OBJ-DADOS-02) — testar com um subconjunto de dados sintéticos multi-tenant.

---

## 11. Confirmação final — pontos de restrição, promoção/rollback, observabilidade, LGPD e custo

| Ponto | Está confirmado/endereçado? | Como |
|---|---|---|
| **Restrição crítica (não-overbooking, §0)** | **Sim, no caminho feliz e na concorrência direta.** Não, nos caminhos de falha (fim de retry, indisponibilidade do Estoque, degradação silenciosa). | Núcleo validado por Arquitetura, ATAM e este painel (OBJ-ARQ, [A-NR-01](atam-avaliacao.md#10-não-riscos)). Bordas endereçadas apenas como recomendação (ADR-02, ADR-03), não como decisão fechada. |
| **Promoção/rollback** de configuração (tarifas, políticas, campanhas) | **Não confirmado — lacuna confirmada por este painel.** | Nenhum ADR deste painel resolve A-RSK-06/A-RSK-07 (candidato pior, rollback) porque nenhuma alternativa é sugerida nem pela fonte nem pela ATAM com forças suficientes para recomendação — registrado como pergunta aberta implícita em PA-08 e como risco não aceito em §9. |
| **Observabilidade** | **Parcialmente.** Bem endereçada para fraude financeira (RF-30/RF-31/RNF-26); **não** endereçada para qualidade/frescor de dado de estoque (A-H-05/OBJ-DADOS-03) nem para desempenho de dashboards com isolamento (OBJ-DADOS-02). | ADR-04 cobre apenas a fatia de segurança (acesso cross-tenant); as fatias de dados/qualidade permanecem como gap em §6. |
| **LGPD** | **Parcialmente — conflito identificado e com recomendação, decisão final fora do mandato deste painel.** | ADR-09 propõe caminho técnico (pseudonimização), mas a precedência legal exige parecer jurídico/DPO (PA-06) que este painel não tem autoridade para emitir. |
| **Custo** | **Nunca era requisito de primeira classe na fonte — este painel o torna explícito pela primeira vez.** | OBJ-CUSTO-01/02/03 e EXP-06 dão visibilidade a três custos estruturais (retenção sem tiering, escala do Estoque sob contenção, manutenção de N adaptadores) que nenhum RF/RNF menciona. Nenhum é resolvido; todos são nomeados. |

---

## 12. Nota de método

- Este painel é **aditivo**: não altera README, REQUISITOS, PERSONAS, [arquitetura.md](arquitetura.md) nem [atam-avaliacao.md](atam-avaliacao.md).
- Nenhum código foi escrito ou proposto.
- Toda ADR é rotulada como **proposta pendente de validação de stakeholders** — nenhuma é tratada como decisão adotada pela arquitetura.
- Divergências entre papéis do painel (§3) foram preservadas como tais, não convertidas em falso consenso.
- Rastreabilidade: toda objeção cita artefato-fonte e/ou achado prévio (RSK-xx, A-RSK-xx, A-CN-xx); toda ADR cita requisito de origem; a matriz (§5) e a confirmação final (§11) fecham o ciclo pedido pelo mandato do painel.

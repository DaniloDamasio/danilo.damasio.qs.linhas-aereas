# danilo.damasio.qs.linhas-aereas

## Projeto
O objetivo é criar uma plataforma nacional integrada com várias companhias aereas parceiras que vão fazer oferta e venda de passagens, podendo oferecer pacotes, ofertas, compatibilidade com empresas para serviço B2B, entre outros fatores relevantes para otimização de compra e recursos para diversas personas de cliente final.

## Personas para a aplicação

### Passageiros
- Viajante de ultima hora
- Viajante economico
- Viajante a negócio
- Familia turista planejadora

### Operação interna
- Admin da Plataforma

### Companhias aereas
- Gestor comercial da companhia aerea

### Agencias B2B
- Agente de viagens corporativas

## CI (GitHub Actions)
O workflow [.github/workflows/ci.yml](.github/workflows/ci.yml) roda `./mvnw -B verify` em todo `pull_request` e em `push` na `main`, com Java 21 (Temurin), cache de dependências Maven, `concurrency` cancelando execuções obsoletas e `timeout-minutes: 15`. Os relatórios do Surefire e do Failsafe são publicados como artifacts (`if: always()`, retenção de 5 dias).

### Proteção da branch `main`
A branch `main` exige:
- Pull Request obrigatório para qualquer mudança (`required_pull_request_reviews`, `required_approving_review_count: 0` — não exige aprovação de terceiros por ser projeto solo, mas bloqueia push direto na `main`);
- o check `verify` do workflow CI passando (`required_status_checks`, `strict: true`);
- aplicação da regra também para administradores (`enforce_admins: true`).

Isso foi configurado via API do GitHub (`gh api repos/.../branches/main/protection`) — a proteção de branch requer repositório público ou plano GitHub Pro para repositórios privados.

### Runs de referência (PR #1 — ci/github-actions-maven-verify)
| Run ID | Resultado | Observação |
|---|---|---|
| [35352582967](https://github.com/DaniloDamasio/danilo.damasio.qs.linhas-aereas/actions/runs/35352582967) | ✅ verde | Primeira execução do workflow, `./mvnw -B verify` com 167 testes, 0 falhas |
| [35354990635](https://github.com/DaniloDamasio/danilo.damasio.qs.linhas-aereas/actions/runs/35354990635) | ❌ vermelho | Asserção quebrada de propósito em `RnA03EscritaCondicionalComoMecanismoDecisivoTest` (`AssertionFailedError: expected: <2> but was: <1>`) |
| [35370052065](https://github.com/DaniloDamasio/danilo.damasio.qs.linhas-aereas/actions/runs/35370052065) | ✅ verde | Revert do commit que quebrou o teste, suíte voltou a passar |

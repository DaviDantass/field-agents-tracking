# Field Agents Tracking — Fullstack

Sistema de rastreamento de equipes externas. Backend Spring Boot com sincronização GPS automática, 4 schedulers independentes e circuit breaker. Frontend Next.js consumindo a API REST.

---

## Estrutura

```
field-agents-tracking-full/
├── field-agents-tracking/   backend — Spring Boot 3 + Java 21
├── frontend/                frontend — Next.js 16 + Tailwind CSS
└── README.md
```

---

## Backend

**Stack:** Java 21 · Spring Boot 3.5 · MySQL · Resilience4j · MapStruct · Swagger/OpenAPI

O backend cobre todos os requisitos obrigatórios e a maioria dos diferenciais:

| Requisito | Status |
|---|---|
| CRUD completo de agentes | ✓ |
| Atualização automática de posições (sync GPS) | ✓ |
| Registro de check-ins manuais | ✓ |
| Histórico de rota | ✓ |
| 4 schedulers independentes | ✓ |
| Idempotência e tratamento de conflitos | ✓ |
| Histórico de sincronização persistido | ✓ |
| WebClient (reativo) | ✓ |
| Tratamento de rate limit (429) e instabilidade (503) | ✓ |
| Sincronização incremental com syncToken | ✓ |
| Swagger / OpenAPI | ✓ (diferencial) |
| Circuit Breaker + Retry (Resilience4j) | ✓ (diferencial) |

### Rodando o backend

Requer Java 21 e MySQL com banco `tracking` criado.

```bash
cd field-agents-tracking
./mvnw spring-boot:run
```

API em `http://localhost:8080` · Swagger em `http://localhost:8080/swagger-ui.html`

Variáveis de ambiente (defaults de dev já configurados):

| Variável | Default |
|---|---|
| `DB_URL` | `jdbc:mysql://localhost:3306/tracking` |
| `DB_USERNAME` | `root` |
| `DB_PASSWORD` | —          |
| `GPS_API_BASE_URL` | `https://desafio-media.onrender.com` |
| `GPS_API_KEY` | — |

---

## Frontend

**Stack:** Next.js 16 · TypeScript · Tailwind CSS

Interface simples para consumo da API. Uma página com listagem de agentes e criação via formulário inline.

### Rodando o frontend

```bash
cd frontend
npm install
npm run dev
```

App em `http://localhost:3000`

O arquivo `frontend/.env.local` já aponta para `http://localhost:8080`.

---

## Decisões técnicas

**Sync incremental com syncToken**
A cada ciclo o scheduler busca o `nextSyncToken` da última sincronização persistida e envia na chamada à API GPS. A API devolve só o delta — evita reprocessar tudo a cada 15 segundos.

**Idempotência**
Antes de salvar uma localização, verifica `agent_id + timestamp`. Retries não geram duplicatas.

**Circuit Breaker (Resilience4j)**
O `GpsClient` abre o circuito com 60% de falhas, aguarda 60s e tenta reabrir. Retry em 3 tentativas para `ConnectException`, `503` e `429`. Com o circuito aberto, fallback devolve lista vazia — o scheduler segue rodando.

**Soft delete**
`DELETE /agents/{id}` seta `active = false`. Histórico de localizações e check-ins preservado com integridade referencial.

**Schedulers isolados**
Cada scheduler é um componente Spring independente com `@ConditionalOnProperty`. Falha de um não afeta os outros e cada um pode ser desabilitado individualmente via `application.yaml`.
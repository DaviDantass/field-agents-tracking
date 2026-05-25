mu# Field Agents Tracking

Sistema fullstack de rastreamento de agentes de campo. O backend sincroniza posições automaticamente com uma API GPS externa, persiste histórico de localizações e check-ins, e expõe uma API REST completa. O frontend consome essa API.

## Estrutura

```
├── backend/    Spring Boot 3 + Java 21
└── frontend/   Next.js 16 + TypeScript + Tailwind
```

## Rodando o projeto

**Pré-requisitos:** Java 21, MySQL rodando com o banco `tracking` criado.

```bash
# Backend
cd backend
./mvnw spring-boot:run
```

Variáveis de ambiente necessárias (o restante já tem default):

```
DB_PASSWORD=sua_senha
GPS_API_KEY=sua_chave
```

A API sobe em `http://localhost:8080`. Swagger disponível em `/swagger-ui.html`.

```bash
# Frontend
cd frontend
npm install
npm run dev
```

Frontend em `http://localhost:3000`. O arquivo `frontend/.env.local` já aponta para o backend local.

## Backend

O núcleo do projeto. Quatro schedulers rodam de forma independente — um sincroniza posições com a API GPS a cada 5 minutos, outro faz limpeza de histórico antigo, um terceiro monitora estatísticas de sincronização, e o quarto está reservado para validação de geofencing. Cada um pode ser desabilitado individualmente no `application.yaml`.

A sincronização com a API GPS usa `syncToken` incremental: a cada ciclo, o sistema busca o token da última sincronização bem-sucedida e envia junto na requisição, recebendo apenas o delta. Isso evita reprocessar todo o histórico a cada 15 segundos em produção.

O `GpsClient` tem circuit breaker configurado pelo Resilience4j — abre com 60% de falhas, aguarda 60s antes de tentar reabrir. Retry automático em até 3 tentativas para `ConnectException`, `503` e `429`. Com o circuito aberto, o scheduler continua rodando normalmente com fallback de lista vazia.

Localizações têm verificação de idempotência por `agent_id + timestamp` antes de salvar, então retries não geram duplicatas. Deleção de agente é soft delete (`active = false`), preservando integridade do histórico.

**Stack:** Java 21 · Spring Boot 3.5 · MySQL · WebClient · Resilience4j · MapStruct · Swagger/OpenAPI

## Frontend

Uma página com listagem de agentes e formulário de criação. Sem dependências além do Next.js e Tailwind — `fetch` nativo, `useState` e `useEffect`.

**Stack:** Next.js 16 · TypeScript · Tailwind CSS
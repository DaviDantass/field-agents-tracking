# field-agents-tracking

Backend em Spring Boot para rastreamento de agentes de campo. Consome uma API GPS externa, sincroniza localizações automaticamente e expõe uma REST API documentada com Swagger.

---

## Stack

Java 21 · Spring Boot 3.5 · MySQL · Resilience4j · MapStruct · Swagger/OpenAPI

---

## Rodando localmente

Requer Java 21 e MySQL. Crie o banco antes:

```sql
CREATE DATABASE tracking;
```

```bash
./mvnw spring-boot:run
```

Swagger UI em `http://localhost:8080/swagger-ui.html`.

Variáveis de ambiente — os defaults já funcionam em dev:

| Variável           | Default                                |
|--------------------|----------------------------------------|
| `DB_URL`           | `jdbc:mysql://localhost:3306/tracking` |
| `DB_USERNAME`      | `root`                                 |
| `DB_PASSWORD`      | —                                      |
| `GPS_API_BASE_URL` | `https://desafio-media.onrender.com`   |
| `GPS_API_KEY`      | —                                      |

---

## API

### Agentes

```
GET    /agents               lista agentes (paginado, ?name=filtro)
GET    /agents/{id}          detalhe
POST   /agents               criar
PUT    /agents/{id}          atualizar
DELETE /agents/{id}          desativar (soft delete)
```

### Localizações

```
GET    /locations/agents/{id}/history     histórico de posições GPS
GET    /locations/agents/{id}/last        última posição conhecida
```

### Check-ins manuais

```
POST   /locations/agents/{id}/check-ins           registrar
GET    /locations/agents/{id}/check-ins           listar (paginado)
GET    /locations/agents/{id}/check-ins/range     filtrar por período
```

### Sincronização

```
GET    /locations/sync-history            histórico de sync (paginado)
GET    /locations/sync-history/last       última sincronização
GET    /locations/sync-history/stats      totais: syncs, sucessos, falhas, localizações processadas
```

---

## Schedulers

Quatro schedulers independentes. Cada um pode ser desabilitado individualmente via `application.yaml` com `@ConditionalOnProperty`.

| Scheduler                 | Intervalo              | Função                                          |
|---------------------------|------------------------|-------------------------------------------------|
| `LocationScheduler`       | 15s (dev) / 5min (prod)| Sincroniza agentes e posições com a API GPS     |
| `GeofencingScheduler`     | 10 min                 | Validação de geofencing                         |
| `HistoryCleanupScheduler` | Toda meia-noite        | Remove localizações com mais de 30 dias         |
| `MonitoringScheduler`     | 15 min                 | Loga estatísticas de sincronização              |

---

## Decisões de implementação

**Sync incremental com syncToken**
Em vez de buscar todos os agentes a cada ciclo, a sincronização guarda o `nextSyncToken` da resposta da API GPS e usa ele na próxima chamada — a API devolve só o delta. Implementado em `LocationService.syncLocationsFromGps()`.

**Idempotência na gravação de localizações**
Antes de salvar qualquer posição, verifica `agent_id + timestamp` no banco. Ciclos de retry não geram duplicatas.

**Circuit Breaker + Retry via Resilience4j**
O `GpsClient` abre o circuito após 60% de falhas, espera 60s e tenta reabrir. Retry configurado para 3 tentativas em `ConnectException`, `503` e `429`. Com o circuito aberto, o fallback devolve lista vazia e o scheduler continua rodando normalmente.

**Soft delete**
`DELETE /agents/{id}` marca `active = false`. O histórico de localizações e check-ins permanece intacto — integridade referencial preservada.
# redis-game-hub

ДЗ 1 по Redis — бэкенд GameHub: профили игроков, лидерборд, достижения, уведомления.

Kotlin, Spring Boot 4, Spring Data Redis (Lettuce), Redis 8.

## Архитектура

- `redis-master` (6379) — мастер, RDB + AOF, `maxmemory 256mb`, `volatile-lru`, `min-replicas-to-write 1`
- `redis-replica-1` (6380), `redis-replica-2` (6381) — реплики, read-only
- `sentinel-1..3` (26379, 26380, 26381) — Sentinel, кворум 2
- `redis-insight` (5540) — UI для Redis
- `app` (8080) — REST API

Приложение подключается к Redis через Sentinel, читает с реплик, пишет в мастер.
После failover Lettuce сам переключается на новый мастер.

Конфиги Redis лежат в `redis/`.

## Запуск

Всё в докере:

```bash
./gradlew bootJar
docker compose --profile app up -d --build
```

Только Redis (приложение запускать из IDE с профилем `local`, порт 8081):

```bash
docker compose up -d
SPRING_PROFILES_ACTIVE=local ./gradlew bootRun
```

Sentinel отдаёт адреса узлов по именам контейнеров, поэтому в профиле `local` они подменяются на `localhost`.

Swagger: http://localhost:8080/swagger-ui.html

Остановить: `docker compose --profile app down -v`

## API

| Метод | Путь | Redis |
|---|---|---|
| POST | `/api/players/{id}` | `HSET player:{id}`, `DEL cache:player:{id}`, `XADD notifications` |
| GET | `/api/players/{id}` | `GET cache:player:{id}`, при промахе `HGETALL` + `SET ... EX 60` |
| PATCH | `/api/players/{id}/level` | `HINCRBY player:{id} level`, `DEL cache:player:{id}`, `XADD notifications` |
| POST | `/api/players/{id}/login` | Lua: `INCR logins:{id}` + `EXPIRE 86400` |
| POST | `/api/players/batch` | pipeline из `HSET` |
| POST | `/api/leaderboard/score` | `ZINCRBY tournament:main` |
| GET | `/api/leaderboard/top?limit=10` | `ZREVRANGE tournament:main 0 9 WITHSCORES` |
| GET | `/api/leaderboard/rank/{playerId}` | `ZREVRANK`, `ZSCORE` |
| POST | `/api/players/{id}/achievements` | `SADD achievements:{id}` |
| GET | `/api/players/{id}/achievements/{name}` | `SISMEMBER achievements:{id}` |
| GET | `/api/players/{id1}/achievements/common/{id2}` | `SINTER` |

Пример:

```bash
curl -X POST localhost:8080/api/players/1001 -H 'Content-Type: application/json' \
  -d '{"name":"Alice","level":5,"region":"EU"}'
curl -i localhost:8080/api/players/1001   # заголовок X-Cache: MISS / HIT
```

Место в лидерборде считается через `ZREVRANK`, так как очки сортируются по убыванию.

## Данные в Redis

| Ключ | Тип | Что хранит | TTL |
|---|---|---|---|
| `player:{id}` | Hash | name, level, region, created_at | нет |
| `logins:{id}` | String | число входов | 24 ч |
| `cache:player:{id}` | String | JSON профиля | 60 сек |
| `tournament:main` | Sorted Set | player_id → очки | нет |
| `achievements:{id}` | Set | названия достижений | нет |
| `notifications` | Stream | player_id, type, message, timestamp | 7 дней (`XTRIM MINID`) |

Уведомления читает `NotificationConsumer` из группы `notifications-group` (`XREADGROUP` → лог → `XACK`).

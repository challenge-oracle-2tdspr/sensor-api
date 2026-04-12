# Sensor API — Deploy Guide

## Arquitetura

```
Internet
   │
   └── sensor-api VM
          ├── sensor-api (Spring Boot :8081)
          └── sensor-timescaledb (PostgreSQL/TimescaleDB :5432)
                    │
                    └── RabbitMQ VM
                              └── vhost: sensor_vhost
```

---

## Pré-requisitos

- Ubuntu 24.04 LTS
- Docker + Docker Compose Plugin
- Acesso SSH à VM
- RabbitMQ rodando na mesma vnet com vhost `sensor_vhost` configurado

---

## 1. Instalar Docker

```bash
sudo apt update && sudo apt upgrade -y
sudo apt install -y apt-transport-https ca-certificates curl gnupg lsb-release

sudo install -m 0755 -d /etc/apt/keyrings
curl -fsSL https://download.docker.com/linux/ubuntu/gpg | sudo gpg --dearmor -o /etc/apt/keyrings/docker.gpg
sudo chmod a+r /etc/apt/keyrings/docker.gpg

echo "deb [arch=$(dpkg --print-architecture) signed-by=/etc/apt/keyrings/docker.gpg] https://download.docker.com/linux/ubuntu $(lsb_release -cs) stable" | sudo tee /etc/apt/sources.list.d/docker.list > /dev/null

sudo apt update
sudo apt install -y docker-ce docker-ce-cli containerd.io docker-compose-plugin
sudo usermod -aG docker $USER
newgrp docker
```

---

## 2. Clonar o repositório

```bash
git clone https://github.com/challenge-oracle-2tdspr/sensor-api.git
cd sensor-api
```

---

## 3. Configurar variáveis de ambiente

```bash
cp .env.example .env
nano .env
```

### Variáveis do `.env`

| Variável | Descrição | Default |
|---|---|---|
| `POSTGRES_DB` | Nome do banco | `sensor_db` |
| `POSTGRES_USER` | Usuário do banco | `sensor_user` |
| `POSTGRES_PASSWORD` | Senha do banco | — |
| `RABBITMQ_HOST` | IP do servidor RabbitMQ | — |
| `RABBITMQ_PORT` | Porta AMQP | `5672` |
| `RABBITMQ_USER` | Usuário do RabbitMQ | `sensor_user` |
| `RABBITMQ_PASS` | Senha do RabbitMQ | — |
| `RABBITMQ_VHOST` | Virtual host | `sensor_vhost` |
| `DDL_AUTO` | Estratégia DDL do Hibernate | `update` |

### Exemplo de `.env`

```env
POSTGRES_DB=sensor_db
POSTGRES_USER=sensor_user
POSTGRES_PASSWORD=senha-forte-aqui

RABBITMQ_HOST=10.0.0.4
RABBITMQ_PORT=5672
RABBITMQ_USER=sensor_user
RABBITMQ_PASS=senha-rabbit-aqui
RABBITMQ_VHOST=sensor_vhost

DDL_AUTO=update
```

---

## 4. Configurar RabbitMQ (na VM do RabbitMQ)

Certifique-se de que o vhost e as permissões existem:

```bash
docker exec rabbitmq rabbitmqctl add_vhost sensor_vhost
docker exec rabbitmq rabbitmqctl set_permissions -p sensor_vhost sensor_user ".*" ".*" ".*"

# Verificar
docker exec rabbitmq rabbitmqctl list_vhosts
docker exec rabbitmq rabbitmqctl list_permissions -p sensor_vhost
```

---

## 5. Subir os containers

```bash
docker compose up -d --build
```

Aguarde o build e acompanhe os logs:

```bash
docker logs -f sensor-api
```

A API está pronta quando aparecer:

```
Started ApiApplication in XX seconds
```

---

## 6. Verificar saúde dos containers

```bash
docker ps
```

| Container | Status esperado |
|---|---|
| `sensor-api` | `healthy` |
| `sensor-timescaledb` | `healthy` |

---

## 7. Verificar conexão com RabbitMQ

```bash
# Na VM do RabbitMQ — confirmar conexão ativa
docker exec rabbitmq rabbitmqctl list_connections -p sensor_vhost user peer_host state

# Confirmar fila criada
docker exec rabbitmq rabbitmqctl list_queues -p sensor_vhost name messages consumers
```

---

## Troubleshooting

### Container `unhealthy` ou sem logs

A VM pode estar sem memória suficiente. Verifique:

```bash
free -h
dmesg | grep -i "killed\\|oom"
```

> A sensor-api requer no mínimo **2GB de RAM** na VM (Spring Boot + TimescaleDB).

O `docker-compose.yml` já inclui flags de otimização de memória JVM:

```
-XX:InitialRAMPercentage=30 -XX:MaxRAMPercentage=60 -XX:+UseG1GC
```

### Erro `vhost not found`

```bash
docker exec rabbitmq rabbitmqctl add_vhost sensor_vhost
docker exec rabbitmq rabbitmqctl set_permissions -p sensor_vhost sensor_user ".*" ".*" ".*"
```

### Erro `Address seems to contain an unquoted IPv6`

O `RABBITMQ_HOST` está com `http://` na frente. Corrija no `.env`:

```env
# Errado
RABBITMQ_HOST=http://10.0.0.4

# Correto
RABBITMQ_HOST=10.0.0.4
```

### Variável de ambiente não atualizada após editar `.env`

O `docker compose restart` não recarrega variáveis. Use:

```bash
docker compose down && docker compose up -d
```

### Host RabbitMQ hardcoded no compose

Se a variável do `.env` não surtir efeito, verifique se o `docker-compose.yml` não tem o host fixo:

```yaml
# Errado — valor hardcoded
SPRING_RABBITMQ_HOST: rabbitmq

# Correto — lê do .env
SPRING_RABBITMQ_HOST: ${RABBITMQ_HOST}
```

---

## Acesso ao banco via TablePlus (sem IP público)

A VM não possui IP público. Use um túnel SSH via jump host:

```bash
# No terminal local — manter rodando em background
ssh -i ~/.ssh/sua-chave.pem \\
  -J agrotech@<IP-PUBLICO-AGROTECH> \\
  -L 5433:localhost:5432 \\
  sensor@<IP-PRIVADO-SENSOR> -N
```

No TablePlus, conecte com as seguintes configurações:

| Campo | Valor |
|---|---|
| Host | `localhost` |
| Port | `5433` |
| User | `sensor_user` |
| Password | `<POSTGRES_PASSWORD>` |
| Database | `sensor_db` |
| SSH Tunnel | desligado |

---

## Inserir dados manualmente no banco

```bash
docker exec -it sensor-timescaledb psql -U sensor_user -d sensor_db
```

```sql
-- Listar tabelas
\\dt

-- Ver estrutura de uma tabela
\\d sensors

-- Inserir sensor
INSERT INTO sensors (name, type, location)
VALUES ('Sensor 01', 'temperature', 'Field A');

-- Consultar
SELECT * FROM sensors;
```

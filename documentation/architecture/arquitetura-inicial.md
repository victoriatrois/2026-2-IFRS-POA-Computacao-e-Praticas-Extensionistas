# MVP — Plataforma de Avaliação para Competição de Robótica

## 1. Stack

### Frontend

* React
* Next.js
* PWA

### Backend

* Spring Boot 4.1.x
* Java 26
* API REST
* Spring Security
* JWT

### Banco de dados

**Recomendação: PostgreSQL**

MongoDB também é uma possibilidade, mas PostgreSQL é mais adequado ao domínio inicial devido à quantidade de relacionamentos entre as entidades.

---

# 2. Escopo do MVP

O MVP terá inicialmente quatro grandes módulos:

1. **Autenticação e Gestão de Usuários**
2. **Gestão de Eventos e Projetos**
3. **Configuração de Avaliações**
4. **Avaliação dos Projetos**

---

# 3. Perfis de Usuário

Inicialmente existirão apenas dois roles:

### ADMIN

Responsável por:

* Gerenciar usuários
* Criar/importar eventos
* Importar projetos via CSV
* Criar os quesitos de avaliação
* Configurar as regras de avaliação
* Gerenciar avaliadores

### EVALUATOR

Responsável por:

* Fazer login
* Visualizar eventos disponíveis
* Visualizar projetos
* Avaliar os projetos
* Preencher notas dos quesitos
* Adicionar comentários
* Finalizar uma avaliação

---

# 4. Modelo Conceitual

```text
User
 └── Role (ADMIN | EVALUATOR)

Event
 ├── Projects
 └── Evaluation Criteria

Project
 └── Evaluations

EvaluationCriteria
 └── Evaluation Scores

Evaluation
 ├── Evaluator (User)
 ├── Project
 └── Scores
```

O fluxo principal será:

```text
ADMIN
  │
  ├── Login
  │
  ├── Criar/Importar Evento
  │      └── Importar Projetos via CSV
  │
  ├── Criar Quesitos
  │      ├── Quesito 1
  │      ├── Quesito 2
  │      └── Quesito N
  │
  └── Gerenciar Avaliadores


EVALUATOR
  │
  ├── Login
  │
  ├── Selecionar Evento
  │
  ├── Selecionar Projeto
  │
  └── Avaliar
         ├── Quesito 1 → Nota
         ├── Quesito 2 → Nota
         └── Quesito N → Nota
```

---

# 5. Entidades Principais

## User

```text
users
----------------
uuid
name
surname
email
password_hash
role
cpf
created_at
updated_at
```

Roles:

```text
ADMIN
EVALUATOR
```

---

## Event

```text
events
----------------
id
name
description
date
status
created_at
```

Status sugeridos:

```text
DRAFT
OPEN
CLOSED
```

### Fluxo

```text
DRAFT
  ↓
OPEN
  ↓
CLOSED
```

Enquanto o evento estiver `CLOSED`, novas avaliações ou alterações nas avaliações podem ser bloqueadas.

---

## Project

Cada projeto pertence a um evento.

```text
projects
----------------
id
event_id
name
team
registration_number
created_at
```

Relacionamento:

```text
Event 1 ─────── N Project
```

---

## Evaluation Criteria

Os quesitos devem pertencer ao evento.

```text
evaluation_criteria
----------------
id
event_id
name
description
max_score
weight
order
```

Exemplo:

```text
Evento: Competição 2026

Critérios:

1. Design
   Nota máxima: 10

2. Inovação
   Nota máxima: 10

3. Autonomia
   Nota máxima: 10

4. Apresentação
   Nota máxima: 10
```

Isso permite que diferentes eventos tenham diferentes critérios.

---

# 6. Evaluation

Uma avaliação representa a avaliação de um projeto por um avaliador.

```text
evaluations
----------------
id
project_id
evaluator_id
status
created_at
updated_at
```

Status sugeridos:

```text
IN_PROGRESS
SUBMITTED
```

Relacionamentos:

```text
User (Evaluator)
       │
       │
       ▼
Evaluation
       │
       │
       ▼
Project
```

---

# 7. Evaluation Score

As notas individuais dos quesitos ficam separadas da avaliação.

```text
evaluation_scores
----------------
id
evaluation_id
criterion_id
score
comment
```

Exemplo:

```text
Evaluation #123

Avaliador: João
Projeto: Robot Alpha

Scores:

Design          → 8
Inovação        → 9
Autonomia       → 7
Apresentação    → 10
```

Essa abordagem permite que uma avaliação tenha vários quesitos.

---

# 8. Relacionamentos do Banco

```text
┌──────────────┐
│    Event     │
└──────┬───────┘
       │
       │ 1:N
       ▼
┌──────────────┐
│   Project    │
└──────┬───────┘
       │
       │ 1:N
       ▼
┌──────────────┐
│  Evaluation  │
└──────┬───────┘
       │
       │ 1:N
       ▼
┌──────────────────┐
│ EvaluationScore  │
└────────┬─────────┘
         │
         │ N:1
         ▼
┌──────────────────────┐
│ EvaluationCriterion  │
└──────────────────────┘


┌──────────────┐
│     User     │
└──────┬───────┘
       │
       │ 1:N
       ▼
┌──────────────┐
│  Evaluation  │
└──────────────┘
```

---

# 9. Por que PostgreSQL?

Para esse MVP, **PostgreSQL é a recomendação**.

O domínio possui diversos relacionamentos:

```text
Event
  ↓
Projects
  ↓
Evaluations
  ↓
Evaluator
  ↓
Criteria
```

Além disso, provavelmente serão necessárias consultas como:

* Qual projeto teve a maior pontuação?
* Qual foi a média de cada projeto?
* Qual foi a nota dada por cada avaliador?
* Qual quesito teve maior impacto?
* Qual é o ranking dos projetos?
* Quais projetos pertencem a determinado evento?
* Quais avaliações ainda estão pendentes?

Essas operações são muito naturais em um banco relacional.

---

# 10. Arquitetura do Backend

A recomendação é utilizar um **Modular Monolith**, em vez de microservices.

Estrutura sugerida:

```text
backend/
└── src/main/java/com/seuprojeto/
    │
    ├── auth/
    │   ├── controller/
    │   ├── service/
    │   ├── repository/
    │   ├── entity/
    │   └── dto/
    │
    ├── user/
    │   ├── controller/
    │   ├── service/
    │   ├── repository/
    │   ├── entity/
    │   └── dto/
    │
    ├── event/
    │   ├── controller/
    │   ├── service/
    │   ├── repository/
    │   ├── entity/
    │   └── dto/
    │
    ├── project/
    │   ├── controller/
    │   ├── service/
    │   ├── repository/
    │   ├── entity/
    │   └── dto/
    │
    ├── evaluation/
    │   ├── controller/
    │   ├── service/
    │   ├── repository/
    │   ├── entity/
    │   └── dto/
    │
    └── common/
        ├── exception/
        ├── security/
        └── config/
```

A aplicação continua sendo um único backend Spring Boot, mas seus módulos são separados por domínio.

---

# 11. Arquitetura Geral

```text
                    ┌─────────────────────┐
                    │      Next.js        │
                    │       React         │
                    │        PWA          │
                    └──────────┬──────────┘
                               │
                              HTTPS
                               │
                               ▼
                    ┌─────────────────────┐
                    │    Spring Boot      │
                    │      REST API       │
                    └──────────┬──────────┘
                               │
                    ┌──────────┴──────────┐
                    │                     │
                    ▼                     ▼
             ┌──────────────┐      ┌──────────────┐
             │  PostgreSQL  │      │    Storage   │
             └──────────────┘      └──────────────┘
```

Para o MVP, o storage separado nem necessariamente será necessário. O CSV pode ser processado diretamente pelo backend.

---

# 12. Autenticação

Fluxo sugerido:

```text
Frontend
   │
   │ POST /auth/login
   ▼
Spring Boot
   │
   ├── Busca usuário
   ├── Verifica password hash
   └── Gera JWT
          │
          ▼
       Frontend
```

Nas requisições autenticadas:

```http
Authorization: Bearer <JWT>
```

O JWT pode conter informações como:

```json
{
  "sub": "123",
  "email": "avaliador@email.com",
  "role": "EVALUATOR"
}
```

A autorização deve ser feita **no backend**.

O frontend não deve ser considerado uma fonte confiável para determinar o role do usuário.

---

# 13. Permissões

## ADMIN

```text
/auth/*
/users/*
/events/*
/projects/import
/criteria/*
```

## EVALUATOR

```text
/auth/*
/events
/projects
/evaluations/*
```

Exemplo:

```text
ADMIN
 ├── Criar evento
 ├── Importar projetos
 ├── Criar quesitos
 └── Gerenciar avaliadores

EVALUATOR
 ├── Visualizar evento
 ├── Visualizar projetos
 └── Realizar avaliações
```

---

# 14. Importação CSV

A importação pode ser feita através de:

```http
POST /api/events/{eventId}/projects/import
Content-Type: multipart/form-data
```

Exemplo de CSV:

```csv
registration_number,name,team
001,Robot Alpha,Equipe A
002,Robot Beta,Equipe B
003,Robot Gamma,Equipe C
```

Fluxo:

```text
CSV
 ↓
Upload
 ↓
Spring Boot
 ↓
CSV Parser
 ↓
Validation
 ↓
Project DTO
 ↓
Project Service
 ↓
Repository
 ↓
PostgreSQL
```

O frontend apenas envia o arquivo.

A validação e persistência devem ocorrer no backend.

---

# 15. API de Avaliação

Exemplo de endpoint:

```http
GET /api/events/{eventId}/projects
```

Para obter a avaliação:

```http
GET /api/projects/{projectId}/evaluation
```

Para salvar uma avaliação:

```http
POST /api/projects/{projectId}/evaluations
```

Exemplo de payload:

```json
{
  "scores": [
    {
      "criterionId": 1,
      "score": 8,
      "comment": "Boa execução"
    },
    {
      "criterionId": 2,
      "score": 9,
      "comment": "Excelente autonomia"
    },
    {
      "criterionId": 3,
      "score": 7,
      "comment": "Pode melhorar o design"
    }
  ]
}
```

---

# 16. Validações da Avaliação

O backend deve validar:

```text
score >= 0

score <= criterion.maxScore

criterion pertence ao evento

project pertence ao evento

evaluator está autenticado

evaluator possui permissão para avaliar

event está OPEN

evaluation ainda não foi SUBMITTED
```

Isso evita que regras importantes fiquem apenas no frontend.

---

# 17. Interface do Avaliador

Uma interface inicial poderia ser:

```text
Projeto: Robot Alpha

┌─────────────────────────────────────┐
│ Design                       8 / 10 │
│ [______________________________]    │
│ Comentário:                         │
│ [______________________________]    │
└─────────────────────────────────────┘

┌─────────────────────────────────────┐
│ Inovação                     9 / 10 │
│ [______________________________]    │
│ Comentário:                         │
│ [______________________________]    │
└─────────────────────────────────────┘

┌─────────────────────────────────────┐
│ Autonomia                    7 / 10 │
│ [______________________________]    │
│ Comentário:                         │
│ [______________________________]    │
└─────────────────────────────────────┘

Progresso: 3/3

[ Finalizar avaliação ]
```

---

# 18. PWA

A utilização de PWA faz sentido principalmente porque os avaliadores podem utilizar:

* Tablets
* Smartphones
* Notebooks

Fluxo:

```text
Tablet
   ↓
PWA
   ↓
Selecionar evento
   ↓
Selecionar projeto
   ↓
Avaliar
   ↓
Enviar
```

## Offline

Uma evolução futura poderia ser:

```text
             Network
                │
       ┌────────┴────────┐
       │                 │
     Online            Offline
       │                 │
       ▼                 ▼
 Spring Boot         IndexedDB
                         │
                         │
                         ▼
                    Sync depois
```

Porém, **não implementaria o modo offline no primeiro MVP**.

A arquitetura pode ser preparada para isso posteriormente.

---

# 19. Modular Monolith vs Microservices

Para esse projeto, a recomendação é **não utilizar microservices**.

Não seria necessário criar:

```text
Auth Service
Event Service
Evaluation Service
User Service
Project Service
```

Em vez disso:

```text
                 Spring Boot
                     │
        ┌────────────┼────────────┐
        │            │            │
       Auth        Events     Evaluation
        │            │            │
       User       Projects     Criteria
                                  │
                              Evaluation
```

Tudo pode ser executado como uma única aplicação.

Isso reduz:

* Complexidade
* Infraestrutura
* Número de deploys
* Configuração
* Comunicação entre serviços
* Pontos de falha

E continua permitindo uma boa separação de responsabilidades.

---

# 20. Módulos do MVP

## Authentication

```text
Login
JWT
Roles
Authorization
```

## Event Management

```text
Event
Project
CSV Import
```

## Evaluation Configuration

```text
Criteria
Questions
Scoring rules
```

## Evaluation

```text
Evaluator
Project selection
Scores
Comments
Submit evaluation
```

---

# 21. Stack Recomendada

| Componente   | Tecnologia                      |
| ------------ | ------------------------------- |
| Frontend     | Next.js + React                 |
| PWA          | Sim                             |
| Backend      | Spring Boot 4.1.x               |
| Linguagem    | Java 26                         |
| API          | REST                            |
| Autenticação | JWT                             |
| Segurança    | Spring Security                 |
| Banco        | PostgreSQL                      |
| ORM          | JPA/Hibernate / Spring Data JPA |
| Importação   | CSV via Multipart               |
| Arquitetura  | Modular Monolith                |
| Offline      | Futuro                          |
| Deploy       | Frontend + Backend + PostgreSQL |

---

# 22. Decisão Arquitetural Principal

O núcleo do domínio deve ser:

```text
                EVENT
                  │
          ┌───────┴───────┐
          │               │
       PROJECT         CRITERIA
          │               │
          │               │
          └───────┬───────┘
                  │
             EVALUATION
                  │
                  │
            EVALUATOR
```

A ideia central é que **um evento possui projetos e critérios de avaliação, e os avaliadores geram avaliações dos projetos utilizando esses critérios**.

Isso permite que o sistema seja reutilizado para diferentes competições sem precisar alterar o código para cada novo evento.

---

# 23. Escopo recomendado do MVP

### Fase 1 — Autenticação

* [ ] Cadastro de usuários pelo Admin
* [ ] Login
* [ ] JWT
* [ ] Roles
* [ ] Autorização

### Fase 2 — Eventos

* [ ] Criar evento
* [ ] Editar evento
* [ ] Alterar status
* [ ] Visualizar evento

### Fase 3 — Projetos

* [ ] Importar CSV
* [ ] Validar CSV
* [ ] Listar projetos
* [ ] Visualizar projeto

### Fase 4 — Quesitos

* [ ] Criar quesito
* [ ] Editar quesito
* [ ] Definir nota máxima
* [ ] Definir peso
* [ ] Definir ordem
* [ ] Associar quesitos ao evento

### Fase 5 — Avaliações

* [ ] Listar projetos para avaliação
* [ ] Abrir avaliação
* [ ] Preencher notas
* [ ] Adicionar comentários
* [ ] Salvar avaliação
* [ ] Finalizar avaliação
* [ ] Bloquear alterações após submissão

### Fase 6 — PWA

* [ ] Instalação como PWA
* [ ] Layout responsivo
* [ ] Interface otimizada para tablet
* [ ] Cache básico
* [ ] Offline/sincronização como evolução futura

---

# 24. Visão Final do MVP

```text
┌─────────────────────────────────────────────┐
│                  NEXT.JS                    │
│              React + PWA                    │
│                                             │
│  ┌───────────┐ ┌──────────┐ ┌────────────┐ │
│  │   Login   │ │  Events  │ │ Evaluation │ │
│  └───────────┘ └──────────┘ └────────────┘ │
└──────────────────────┬──────────────────────┘
                       │
                       │ REST / HTTPS
                       ▼
┌─────────────────────────────────────────────┐
│                SPRING BOOT                  │
│                                             │
│ ┌──────┐ ┌───────┐ ┌────────┐ ┌─────────┐ │
│ │ Auth │ │ Event │ │Project │ │Evaluation│ │
│ └──────┘ └───────┘ └────────┘ └─────────┘ │
│                                             │
│              Spring Security                │
│                   + JWT                     │
└──────────────────────┬──────────────────────┘
                       │
                       ▼
              ┌─────────────────┐
              │   PostgreSQL    │
              │                 │
              │ Users           │
              │ Events          │
              │ Projects        │
              │ Criteria        │
              │ Evaluations     │
              │ Scores          │
              └─────────────────┘
```

**Resumo da recomendação:** para o MVP, manteria **Next.js + React + PWA no frontend, Spring Boot + Java no backend, PostgreSQL como banco e um Modular Monolith**, deixando offline, rankings avançados e outras funcionalidades mais complexas para uma segunda etapa.

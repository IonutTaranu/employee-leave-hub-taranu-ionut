# Arhitectura aplicației

## Componente

```mermaid
flowchart TB
    subgraph Client
        UI[Angular standalone components]
        AUTH[Auth service + route guards]
        API[API service + JWT interceptor]
    end

    subgraph Server[Spring Boot]
        SEC[Spring Security + JWT filter]
        CTRL[REST controllers]
        BUS[Business services]
        REP[Spring Data repositories]
        PDF[PDF report service]
        FILE[Attachment service]
    end

    DB[(PostgreSQL / H2)]
    FS[(File storage)]

    UI --> AUTH --> API --> SEC --> CTRL --> BUS --> REP --> DB
    BUS --> PDF
    BUS --> FILE --> FS
```

Frontendul este împărțit pe funcționalități (`login`, `dashboard`, `requests`, `calendar`, `reports`, `administration`) și încarcă paginile la nevoie. Backendul separă controllerele HTTP de servicii, entități și repository-uri.

## Model relațional

```mermaid
erDiagram
    DEPARTMENT ||--o{ EMPLOYEE : conține
    EMPLOYEE ||--o{ LEAVE_REQUEST : depune
    LEAVE_TYPE ||--o{ LEAVE_REQUEST : clasifică
    LEAVE_REQUEST ||--|{ LEAVE_WORKFLOW : păstrează
    EMPLOYEE ||--o{ LEAVE_WORKFLOW : efectuează
    LEAVE_REQUEST ||--o{ ATTACHMENT : include

    DEPARTMENT {
        bigint id PK
        varchar name UK
        bigint manager_id FK
        int max_absent_employees
    }
    EMPLOYEE {
        bigint id PK
        varchar name
        varchar email UK
        varchar password_hash
        varchar role
        bigint department_id FK
        int annual_leave_days
        int available_leave_days
    }
    LEAVE_TYPE {
        bigint id PK
        varchar name
        varchar code UK
        boolean requires_attachment
        boolean paid
    }
    LEAVE_REQUEST {
        bigint id PK
        bigint employee_id FK
        bigint leave_type_id FK
        date start_date
        date end_date
        int working_days
        varchar status
        varchar reason
        datetime created_at
    }
    LEAVE_WORKFLOW {
        bigint id PK
        bigint leave_request_id FK
        bigint employee_id FK
        varchar old_status
        varchar current_status
        datetime changed_at
        varchar comment
    }
    ATTACHMENT {
        bigint id PK
        bigint leave_request_id FK
        varchar file_name
        varchar file_path
        varchar content_type
        bigint size_bytes
        datetime uploaded_at
    }
```

## Ciclul de viață

```mermaid
stateDiagram-v2
    [*] --> DRAFT: creare
    DRAFT --> PENDING: trimitere
    DRAFT --> CANCELLED: anulare
    PENDING --> APPROVED: aprobare
    PENDING --> REJECTED: respingere
    PENDING --> CANCELLED: anulare de către angajat
```

Nu există tranziții directe între `APPROVED`, `REJECTED` și `CANCELLED`. Regulile sunt verificate în backend, nu doar în interfață.

## Securitate

1. Utilizatorul trimite e-mailul și parola la `/api/auth/login`.
2. Parola este verificată cu BCrypt.
3. Serverul returnează un JWT semnat, cu identificatorul și rolul utilizatorului.
4. Frontendul adaugă tokenul în antetul `Authorization: Bearer ...`.
5. Filtrul JWT autentifică apelul, iar serviciile verifică rolul, proprietarul și departamentul.

## Date și tranzacții

Flyway creează schema prin migrarea `V1__create_leave_hub_schema.sql`. Hibernate validează că entitățile Java corespund schemei. Operațiile de aprobare/respingere sunt tranzacționale pentru ca starea, soldul și istoricul să rămână consistente chiar dacă apare o eroare.

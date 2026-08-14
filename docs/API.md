# Referință API REST

Adresa implicită este `http://localhost:8080/api`. Cu excepția loginului, endpointurile cer `Authorization: Bearer <token>`.

## Autentificare

| Metodă | Rută | Descriere |
|---|---|---|
| POST | `/auth/login` | autentificare și emitere JWT |
| GET | `/auth/me` | utilizatorul autentificat |

Exemplu login:

```json
{
  "email": "ana.popescu@leavehub.ro",
  "password": "Demo123!"
}
```

## Cereri și dashboard

| Metodă | Rută | Descriere |
|---|---|---|
| GET | `/dashboard` | sold și statistici pentru utilizator |
| GET | `/leave-requests` | listă filtrabilă după stare, departament, tip, angajat și perioadă |
| GET | `/leave-requests/{id}` | detalii, istoric și atașamente |
| POST | `/leave-requests` | creare draft |
| PUT | `/leave-requests/{id}` | editare draft |
| POST | `/leave-requests/{id}/submit` | trimitere spre aprobare |
| POST | `/leave-requests/{id}/cancel` | anulare înainte de aprobare |
| POST | `/leave-requests/{id}/decision` | aprobare/respingere de manager/admin |
| DELETE | `/leave-requests/{id}` | ștergere draft |
| GET | `/calendar` | concedii pentru calendar și avertizări de suprapunere |

## Atașamente

| Metodă | Rută | Descriere |
|---|---|---|
| POST | `/leave-requests/{id}/attachments` | upload multipart, câmpul `file` |
| GET | `/attachments/{id}` | descărcarea fișierului |
| DELETE | `/attachments/{id}` | ștergerea atașamentului permis |

Sunt acceptate PDF, JPEG și PNG, cu limită de 10 MB per fișier.

## Administrare

| Metodă | Rută | Acces |
|---|---|---|
| GET/POST | `/departments` | citire autentificat / creare admin |
| PUT/DELETE | `/departments/{id}` | admin |
| GET/POST | `/employees` | manager/admin pentru citire / admin pentru creare |
| PUT/DELETE | `/employees/{id}` | admin |
| GET/POST | `/leave-types` | citire autentificat / creare admin |
| PUT/DELETE | `/leave-types/{id}` | admin |

## Rapoarte și PDF

| Metodă | Rută | Descriere |
|---|---|---|
| GET | `/reports/summary` | indicatori și distribuții |
| GET | `/leave-requests/{id}/pdf` | cerere individuală PDF |
| GET | `/reports/pending.pdf` | cereri în așteptare |
| GET | `/reports/balances.pdf` | situația soldurilor |

Erorile sunt returnate în format `application/problem+json`, cu status HTTP potrivit și mesaj sigur pentru interfață.

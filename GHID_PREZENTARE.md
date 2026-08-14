# Ghid de prezentare — Employee Leave Hub

Acest document te ajută să înțelegi și să prezinți proiectul fără să memorezi codul.

## Explicația în 30 de secunde

Employee Leave Hub este o aplicație prin care angajații își depun cererile de concediu, managerii le aprobă sau le resping, iar administratorii gestionează utilizatorii și regulile companiei. Sistemul calculează zilele lucrătoare, verifică soldul, păstrează istoricul deciziilor, arată concediile într-un calendar comun și poate genera documente PDF.

## Ce problemă rezolvă

Înlocuiește formularele pe hârtie, e-mailurile și fișierele Excel. Astfel, cererile nu se pierd, soldurile sunt clare, fiecare aprobare este trasabilă, iar managerii pot vedea din timp suprapunerile de concedii.

## Cum este construit

- **Angular** afișează interfața din browser.
- **Angular Material** oferă componentele vizuale: tabele, dialoguri, formulare și meniuri.
- **Spring Boot** primește cererile de la interfață, verifică drepturile și aplică regulile.
- **JPA/Hibernate** transformă obiectele Java în înregistrări din baza de date.
- **Flyway** creează aceeași structură a bazei de date pe orice calculator.
- **PostgreSQL** păstrează datele; H2 permite o demonstrație imediată.
- **JWT** este dovada de autentificare trimisă la fiecare apel după login.

## Scenariu recomandat pentru demonstrație (5–7 minute)

1. Intră cu `ana.popescu@leavehub.ro` / `Demo123!`.
2. Arată dashboardul și soldul de zile disponibil.
3. Creează o cerere de concediu de odihnă, salveaz-o ca draft, apoi trimite-o spre aprobare.
4. Arată detaliile cererii, istoricul și butonul pentru PDF.
5. Ieși și intră cu `manager@leavehub.ro` / `Demo123!`.
6. Deschide „Aprobări”, verifică cererea și aprob-o. Menționează că respingerea cere obligatoriu un motiv.
7. Arată calendarul echipei și avertizarea de suprapunere.
8. Intră cu `admin@leavehub.ro` / `Demo123!` și arată rapoartele plus administrarea angajaților/departamentelor/tipurilor.

## Reguli importante implementate

- O cerere aprobată nu poate fi anulată sau editată.
- Angajatul poate anula doar un draft sau o cerere încă neaprobată.
- Managerul vede și aprobă doar cererile departamentului său.
- Managerul nu își poate aproba propria cerere.
- La respingere, comentariul este obligatoriu.
- Soldul este verificat la trimitere și scăzut doar la aprobare.
- Zilele lucrătoare exclud weekendurile și sărbătorile legale românești.
- Pentru concediul medical este necesar un document atașat.
- Fiecare tranziție este înregistrată în `LEAVE_WORKFLOW`.
- Numărul maxim de colegi absenți simultan este configurabil per departament.

## Întrebări probabile și răspunsuri scurte

**De ce Spring Boot?**

Oferă rapid API REST, securitate, validare, acces la baza de date și testare într-un ecosistem Java matur.

**De ce Angular?**

Este cerut în temă și este potrivit pentru interfețe mari, structurate pe componente și roluri.

**De ce există H2 și PostgreSQL?**

H2 face demonstrația simplă, fără configurare. PostgreSQL este opțiunea persistentă și realistă pentru utilizare normală.

**Cum sunt protejate parolele?**

Nu sunt stocate în clar; sunt transformate cu BCrypt. După login, accesul se face printr-un token JWT cu expirare.

**Cum sunt evitate accesările nepermise?**

Backendul verifică rolul și departamentul la fiecare operație sensibilă. Ascunderea butoanelor în frontend este doar o măsură suplimentară, nu securitatea principală.

**Ce se întâmplă la aprobare?**

Backendul verifică starea, soldul și dreptul managerului, actualizează cererea, scade soldul dacă este cazul și adaugă o înregistrare în istoric, toate în aceeași tranzacție.

**Cum ai extinde proiectul?**

Notificări prin e-mail, resetarea parolei, integrare SSO, stocare cloud a documentelor, audit extins și teste end-to-end automate.

## Entitățile bazei de date

- `EMPLOYEE`: angajat, rol, departament și sold.
- `DEPARTMENT`: departament, responsabil și limită de absențe simultane.
- `LEAVE_TYPE`: tipul concediului și regulile lui.
- `LEAVE_REQUEST`: cererea propriu-zisă.
- `LEAVE_WORKFLOW`: istoricul schimbărilor de stare.
- `ATTACHMENT`: metadatele documentelor atașate.

## Dacă demonstrația nu pornește

1. Verifică Java cu `java -version` — trebuie Java 21.
2. Verifică Node cu `node -v` — trebuie 20.19+ sau 22.12+.
3. Backendul trebuie să afișeze că rulează pe portul `8080`.
4. Frontendul trebuie să afișeze adresa `http://localhost:4200`.
5. Dacă un port este ocupat, închide instanța mai veche și pornește din nou.

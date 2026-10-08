# Component and Deployment Diagrams

> **คืออะไร (What is this?)**
> - **Component Diagram**: แสดงโครงสร้างของระบบที่ถูกแบ่งออกเป็นส่วนย่อยๆ (Components) และการสื่อสารระหว่างกัน (เช่น Frontend เรียก API Backend, Backend คุยกับ Database)
> - **Deployment Diagram**: แสดงสถาปัตยกรรมระดับ Hardware/Network ว่าซอฟต์แวร์ถูกนำไปติดตั้งและรันบนสภาพแวดล้อมจริงอย่างไร (เช่น Cloud, Docker)
> 
> **ใช้ทำไมและเพื่ออะไร (Why & Purpose)**
> - เพื่อวางแผนสถาปัตยกรรมระบบ (System Architecture) ให้ชัดเจนก่อนพัฒนาจริง
> - เป็นภาพประกอบในการทำ DevOps (CI/CD) ให้เห็นว่าต้องนำ Spring Boot ไปบรรจุลง Docker Container แล้ว Deploy ขึ้น Render Cloud และใช้ Database บน Supabase

## Component Diagram

```mermaid
flowchart TD
    %% Define components
    subgraph Frontend [Frontend (Vanilla JS/HTML/CSS)]
        UI[User Interface App.js]
        APIClient[API Client api.js]
    end

    subgraph Backend [Backend (Spring Boot)]
        Controller[Controllers (Game, Turn, Log)]
        Service[Business Services (Game, City, Army)]
        Repository[Data Repositories (Spring Data JPA)]
        Domain[Domain Models & Entities]
    end

    subgraph DatabaseLayer [Database]
        DB[(PostgreSQL)]
    end

    %% Define connections
    UI -->|Calls| APIClient
    APIClient -->|REST / JSON| Controller
    Controller -->|Delegates to| Service
    Service -->|Uses| Domain
    Service -->|Uses| Repository
    Repository -->|JPA/Hibernate| DB
```

---

## Deployment Diagram

```mermaid
flowchart TD
    %% Nodes
    subgraph Client [Client Device (Browser)]
        Browser[Web Browser]
    end

    subgraph Cloud [Render Cloud Platform]
        subgraph WebService [Spring Boot Web Service]
            Docker[Docker Container]
            SpringBoot[Spring Boot Embedded Tomcat :8080]
            Docker --- SpringBoot
        end
    end

    subgraph DatabaseServer [Supabase Cloud]
        PostgreSQL[(PostgreSQL 15+)]
    end

    %% Connections
    Browser -->|HTTPS Request| Docker
    SpringBoot -->|Static Files (index.html, JS, CSS)| Browser
    SpringBoot -->|TCP/IP (JDBC)| PostgreSQL
```

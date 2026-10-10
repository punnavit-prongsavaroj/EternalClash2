# EternalClash2

เกมวางแผนกลยุทธ์ Turn-based ระดับตำนาน

## สมาชิกกลุ่ม
| ลำดับ | ชื่อ-นามสกุล | รหัสนักศึกษา | Section | Branch | หน้าที่รับผิดชอบ |
|---|---|---|---|---|---|
| 1 | นายธนภูมิ แทนทุมมา | 673380271-3 | 1 | thanaphumi_673380271-3_01 | Unit Testing, API Documentation |
| 2 | นายปุณณวิชญ์ พงษ์สวโรจน์ | 673380281-0 | 2 | punnavit_673380281-0_02 | Backend, Game Engine, Frontend |
| 3 | นายพงศ์อนันต์ วงศ์ศรี | 673380284-4 | 2 | phonganan_673380284-4_02 | Frontend Integration, Database Design |
| 4 | นายกิตติพจน์ ทิพย์นางรอง | 633020384-3 | 1 | kittipot_633020384-3_01 | System Architecture, Deployment (CI/CD) |

## Tech Stack
- **Backend**: Java 17, Spring Boot 3.2.4
- **Database**: PostgreSQL (Supabase Cloud DB)
- **ORM**: Spring Data JPA (Hibernate)
- **Frontend**: Vanilla HTML/CSS/JS (served via Spring Boot static)
- **Deployment**: Render, Docker

## System Architecture
Layered Architecture (Controller -> Service -> Repository -> Database) 
- มีการประยุกต์ใช้ Domain-Driven Design (แบ่ง Domain Service ออกจาก Application Service)

## Database Design (ER Diagram)
- **ER Diagram**: ดูภาพในไฟล์ doc/diagrams/ErDiagram.svg หรือ doc/diagrams/Database.png
- **Data Dictionary**: คำอธิบายตารางอยู่ในไฟล์ doc/data_dictionary.md

## Installation & Setup
1. Clone repository
2. ตั้งค่า Database credentials ใน code/src/main/resources/application.properties (ปัจจุบันชี้ไปที่ Supabase Cloud แล้ว สามารถรันได้เลย)

## How to Run
`ash
cd code
./mvnw clean spring-boot:run
`
หรือรันผ่าน Docker:
`ash
docker-compose up --build
`

## API Documentation
เมื่อรันโปรเจกต์ สามารถเข้าดู Swagger UI ได้ที่:
http://localhost:8080/swagger-ui.html

## How to Run Tests
`ash
cd code
./mvnw test
`

## Deployment URL
eternal-clash2.vercel.app
## Project Structure
- code/: Source code และ Configuration (Spring Boot)
- code/src/test/: ไฟล์ Unit Testing
- doc/: เอกสารทั้งหมด (Diagrams, SOLID, Design Patterns, Data Dictionary)
- img/: ไฟล์รูปภาพ

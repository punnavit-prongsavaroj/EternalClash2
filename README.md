# EternalClash2

เกมวางแผนกลยุทธ์ Turn-based ระดับตำนาน

## สมาชิกกลุ่ม
| ลำดับ | ชื่อ-นามสกุล | รหัสนักศึกษา | Section | Branch | หน้าที่รับผิดชอบ |
|---|---|---|---|---|---|
| 1 | ปุณณวิช ปรงสวโรจน์ | 6733802810 | 02 | punnavit_6733802810_02 | Backend, Game Engine, Frontend |

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
[https://eternalclash2.onrender.com](https://eternalclash2.onrender.com)

## Project Structure
- code/: Source code และ Configuration (Spring Boot)
- code/src/test/: ไฟล์ Unit Testing
- doc/: เอกสารทั้งหมด (Diagrams, SOLID, Design Patterns, Data Dictionary)
- img/: ไฟล์รูปภาพ

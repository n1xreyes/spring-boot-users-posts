# spring-boot-users-posts

A simple Spring Boot application that manages users and user posts. It utilizes REST APIs, Spring Security, an H2 database, and containerization

### Run the application locally with Docker
1. Build and package the project with Maven. Ensure Maven is installed on your machine and fire up a terminal on the project root:  
`mvn clean package`
2. Ensure Docker (and docker-compose) is installed
3. Build and start up the auth server and the app containers by running  `docker compose up --build`

# spring-boot-users-posts

A simple Spring Boot application that manages users and user posts. It utilizes REST APIs, Spring Security, an H2 database, and containerization

### Run the application locally with Docker
1. Build and package the project with Maven. Ensure Maven is installed on your machine and fire up a terminal on the project root:  
`mvn clean package`
2. Ensure Docker (and docker-compose) is installed
3. Build and start up the auth server and the app containers by running  `docker compose up`

### Initial Auth Setup
1. Follow these steps for local [KeyCloak setup](https://www.keycloak.org/getting-started/getting-started-docker#_create_a_realm)
2. 
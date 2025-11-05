# Maintenance Service
This service is responsible for managing maintenance schedules and records for military assets. It provides functionalities to create, read, update, and delete maintenance records, as well as schedule upcoming maintenance tasks.

## Features
- Maintenance Management: Create, read, update, and delete maintenance records.
- Scheduling: Schedule upcoming maintenance tasks for assets.
- Search and Filter: Search and filter maintenance records based on various criteria.
- Notifications: Send notifications for upcoming maintenance tasks.
- Integration with Asset Service: Seamless integration with the Asset Service for asset information.
- Integration with User Service: Seamless integration with the User Service for user information.
- Dockerized: Easily deployable using Docker.
- Kubernetes Ready: Can be deployed in a Kubernetes cluster.

## API Documentation
The OpenAPI Specification for the Maintenance Service can be found at `/api-docs`. For swagger ui, visit `/swagger-ui.html`.

## Technologies Used
- Java 21
- Spring Boot
- Hibernate
- PostgreSQL
- Docker
- Kubernetes
- OpenAPI/Swagger

## Environment Variables
```
DB_USER=
DB_PASSWORD=
DB_NAME=
DB_URL=
API_GATEWAY_URL=
ASSET_SERVICE_URL=
USER_SERVICE_URL=
```
# RDFtoCSVW API - Docker Setup

This guide explains how to run the RDFtoCSVW API service using Docker.

## Prerequisites

- Docker Desktop installed and running
- Docker Compose (included with Docker Desktop)

## Running the Service

### Recommended: Using Docker Compose (Starts Everything Together)

This method automatically sets up both the API and Redis together:

```powershell
# Build and start all services (Redis + API)
docker-compose up --build

# Or run in detached mode (background)
docker-compose up --build -d
```

**The API will be available at: `http://localhost:8080`**  
**Swagger UI at: `http://localhost:8080/swagger-ui/index.html`**

To stop the services:
```powershell
# If running in foreground, press Ctrl+C, then:
docker-compose down

# If running in background:
docker-compose down
```

### Rebuilding After Code Changes

When you make changes to your code:

```powershell
# Stop current containers
docker-compose down

# Rebuild and restart
docker-compose up --build -d
```

### Viewing Logs

```powershell
# View all logs
docker-compose logs -f

# View only API logs
docker-compose logs -f app

# View only Redis logs
docker-compose logs -f redis
```

### Viewing Logs

```powershell
# View all logs
docker-compose logs -f

# View only API logs
docker-compose logs -f app

# View only Redis logs
docker-compose logs -f redis
```

## Testing the Service

Once running, test the service:

```powershell
# Simple health check
curl http://localhost:8080/

# Check Redis connection
curl http://localhost:8080/health/redis

# Or open Swagger UI in browser
# http://localhost:8080/swagger-ui/index.html
```

## Stopping and Cleaning Up

```powershell
# Stop services
docker-compose down

# Stop and remove volumes (clears Redis data)
docker-compose down -v

# Remove all containers and images
docker-compose down --rmi all -v
```

## Configuration

The application uses the following default ports:
- **API**: 8080
- **Redis**: 6379

To change ports, edit the [docker-compose.yml](docker-compose.yml) file.

The API container automatically connects to Redis using the service name `redis` through Docker's internal network.

## Troubleshooting

### Services won't start
- Ensure Docker Desktop is running
- Check if ports 8080 and 6379 are not already in use:
  ```powershell
  netstat -ano | findstr :8080
  netstat -ano | findstr :6379
  ```
- View logs: `docker-compose logs`

### Redis connection issues
- Verify Redis is running: `docker-compose ps`
- Check Redis health: `curl http://localhost:8080/health/redis`
- Verify Redis directly: `docker exec rdftocsvw-redis redis-cli ping`

### Build issues
- Clean Docker cache: `docker-compose build --no-cache`
- Ensure all dependencies in `lib/` folder are present
- Check for sufficient disk space

### Application not reflecting code changes
- Rebuild the containers: `docker-compose up --build -d`
- Or force rebuild: `docker-compose build --no-cache && docker-compose up -d`

## Development Workflow

For active development, you may want to run the app locally (with hot reload) and only Redis in Docker:

```powershell
# Start only Redis
docker-compose up redis -d

# Then run the app locally
mvn spring-boot:run
```

This gives you faster feedback during development while still using Docker for dependencies.


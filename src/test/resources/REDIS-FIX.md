# Redis Health Check Fix for Render.com Deployment

## Problem
The `/health/redis` endpoint is returning a 7.5-second response time because:
1. Redis is not properly configured on Render.com
2. The application is timing out trying to connect to Redis
3. Render.com doesn't use docker-compose, so the Redis service defined locally isn't deployed

## Solution

### Option 1: Use Render's Managed Redis (Recommended)

#### Step 1: Update your repository
The following files have been updated:
- `src/main/java/org/rdftocsvconverter/RDFtoCSVW/api/controller/RDFtoCSVWController.java` - Optimized health check
- `application.properties` - Reduced timeouts from 60s to 5s for faster failure detection
- `render.yaml` - New file for easy Render deployment

#### Step 2: Deploy using render.yaml

**Option A: Deploy from Dashboard**
1. Go to your Render dashboard
2. Click "New" → "Blueprint"
3. Connect your GitHub repository
4. Render will automatically detect `render.yaml` and create both services:
   - Redis service (free tier)
   - Web service with Redis connection

**Option B: Manual Setup**
1. Create Redis service:
   - Go to Render Dashboard → "New" → "Redis"
   - Name: `rdftocsvw-redis`
   - Plan: Free (or Starter for production)
   - Click "Create Redis"
   - Copy the "Internal Redis URL"

2. Update your Web Service:
   - Go to your web service settings
   - Environment Variables → Add:
     ```
     REDIS_URL = [paste the Internal Redis URL]
     ```
   - Click "Save Changes"

The application will automatically restart and connect to Redis.

### Option 2: Use External Redis Service

If you prefer an external Redis provider:

#### Using Upstash (Free Tier Available)
1. Go to [upstash.com](https://upstash.com)
2. Create a free Redis database
3. Get the connection URL
4. In Render.com, set environment variables:
   ```
   REDIS_URL = your-upstash-redis-url
   ```

#### Using Redis Cloud
1. Go to [redis.com/try-free](https://redis.com/try-free)
2. Create a free database
3. Get connection details
4. Set in Render.com:
   ```
   SPRING_REDIS_HOST = your-redis-host
   SPRING_REDIS_PORT = your-redis-port
   SPRING_REDIS_PASSWORD = your-redis-password
   ```

### Option 3: Disable Redis (Not Recommended)

If you don't need Redis in production, you can make it optional. However, this will disable session management and task tracking.

## Verification

After deploying, test the health check:
```bash
curl https://rdf-to-csvw.onrender.com/health/redis
```

Expected response (should be fast, <1 second):
```json
{
  "status": "UP",
  "message": "Redis is connected and accessible"
}
```

## Changes Made

### 1. Optimized Health Check
Changed from creating and fetching a test key to using Redis PING command for faster response:
```java
// Before: taskService.getTask(testKey) - slow
// After: redisTemplate.getConnectionFactory().getConnection().ping() - fast
```

### 2. Reduced Timeouts
- Connection timeout: 10s → 3s
- Operation timeout: 60s → 5s

This ensures faster failure detection instead of long waits.

### 3. Created render.yaml
Automated deployment configuration that sets up:
- Redis service (internal, secure)
- Web service with proper environment variables
- Automatic connection between services

## Troubleshooting

### Still seeing slow response?
1. Check Redis service status in Render dashboard
2. Verify `REDIS_URL` environment variable is set
3. Check application logs for connection errors

### Connection refused error?
1. Ensure using **Internal Redis URL** (not external)
2. Verify both services are in the same Render account
3. Check Redis service is running (not suspended)

### Out of memory errors?
1. Upgrade Redis plan from Free to Starter
2. Adjust `maxmemoryPolicy` in render.yaml
3. Set shorter TTL for cached data

## Next Steps

1. Commit the changes:
   ```bash
   git add .
   git commit -m "Fix Redis health check and add Render deployment config"
   git push
   ```

2. Deploy using render.yaml or manually configure Redis

3. Test the health endpoint

4. Monitor your application logs for any Redis-related errors

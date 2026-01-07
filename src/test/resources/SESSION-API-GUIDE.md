# Session-Based Async Computation API

This document describes the session-based asynchronous computation feature for RDF to CSVW conversion.

## Overview

The API now supports asynchronous processing of RDF to CSVW conversions with session tracking. This allows you to:
1. Submit a conversion request and receive a session ID immediately
2. Poll for the computation status using the session ID
3. Retrieve the result when computation is complete

## Endpoints

### 1. Start Async Conversion

**POST** `/rdftocsvw/async`

Initiates an asynchronous conversion and returns a session ID.

**Parameters:**
- `file` (semi-optional): The RDF file to convert (multipart/form-data) (either choose this input or fileURL)
- `fileURL` (semi-optional): URL to the RDF file (either choose this input or file)
- `conversionMethod` (optional, default: RDF4J): Parsing method (RDF4J/STREAMING/BIGFILESTREAMING)
- `table` (optional, default: ONE): Table choice (ONE/MORE)
- `firstNormalForm` (optional, default: false): Enable first normal form
- `preferredLanguages` (optional): Comma-separated language codes (e.g., "en,cs,de")
- `namingConvention` (optional): Column naming convention

**Response (202 Accepted):**
```json
{
  "sessionId": "550e8400-e29b-41d4-a716-446655440000",
  "message": "Computation started. Use the sessionId to check status.",
  "statusUrl": "/status/550e8400-e29b-41d4-a716-446655440000"
}
```

**Example:**
```bash
curl -X POST "http://localhost:8080/rdftocsvw/async" \
  -F "file=@yourfile.ttl" \
  -F "conversionMethod=RDF4J" \
  -F "table=ONE"
```

### 2. Check Computation Status

**GET** `/status/{sessionId}`

Retrieves the current status of a computation.

**Path Parameters:**
- `sessionId`: The session ID returned from the async endpoint

**Response Codes:**
- **202 Accepted** - Computation still in progress
- **200 OK** - Computation completed (returns ZIP file)
- **404 Not Found** - Session ID not found
- **500 Internal Server Error** - Computation failed

**Response Examples:**

**Still Computing (202):**
```json
{
  "status": "COMPUTING",
  "sessionId": "550e8400-e29b-41d4-a716-446655440000",
  "message": "Computation is still in progress"
}
```

**Completed (200):**
Returns the ZIP file containing CSV and metadata files as `application/octet-stream`.

**Failed (500):**
```json
{
  "status": "FAILED",
  "sessionId": "550e8400-e29b-41d4-a716-446655440000",
  "error": "Error message describing what went wrong"
}
```

**Not Found (404):**
```json
{
  "error": "Session not found",
  "sessionId": "550e8400-e29b-41d4-a716-446655440000"
}
```

**Example:**
```bash
# Check status
curl "http://localhost:8080/status/550e8400-e29b-41d4-a716-446655440000"

# Download result when done
curl "http://localhost:8080/status/550e8400-e29b-41d4-a716-446655440000" \
  --output result.zip
```

## Usage Workflow

1. **Submit Request:**
   ```bash
   curl -X POST "http://localhost:8080/rdftocsvw/async" \
     -F "fileURL=https://example.com/data.ttl" \
     -F "table=MORE"
   ```
   
   Response:
   ```json
   {
     "sessionId": "abc-123-def-456",
     "message": "Computation started...",
     "statusUrl": "/status/abc-123-def-456"
   }
   ```

2. **Poll for Status:**
   ```bash
   curl "http://localhost:8080/status/abc-123-def-456"
   ```
   
   While computing:
   ```json
   {
     "status": "COMPUTING",
     "message": "Computation is still in progress"
   }
   ```

3. **Retrieve Result:**
   Once status returns 200 OK, the response body contains the ZIP file:
   ```bash
   curl "http://localhost:8080/status/abc-123-def-456" -o result.zip
   ```

## Implementation Details

### Storage
- Task status and results are stored in Redis
- Tasks expire after 24 hours
- Results are stored as byte arrays in Redis

### Status States
- **COMPUTING**: Conversion is in progress
- **DONE**: Conversion completed successfully, result available
- **FAILED**: Conversion failed, error message available

### Components

1. **ComputationStatus** (enum): Defines the three possible states
2. **ComputationTask** (model): Stores task metadata and result
3. **TaskService**: Manages task lifecycle in Redis
4. **RDFtoCSVWService.computeAsyncAndStore()**: Async method that performs computation
5. **Controller endpoints**: `/rdftocsvw/async` and `/status/{sessionId}`

## Error Handling

- Invalid session IDs return 404
- Failed computations return 500 with error details
- Missing required parameters return 400

## Notes

- Session IDs are UUIDs generated server-side
- Tasks automatically expire after 24 hours
- Results are available immediately upon completion
- The original synchronous endpoints remain available

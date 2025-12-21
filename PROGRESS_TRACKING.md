# Progress Tracking Implementation Guide

## Overview

This implementation provides real-time progress updates during RDF to CSV conversion using **Server-Sent Events (SSE)**. The library outputs progress messages like:

```
[PROGRESS] stage=PARSING progress=100% message=Completed Parsing RDF file
[PROGRESS] stage=CONVERTING progress=0% message=Starting Converting to CSV structure
[PROGRESS] stage=CONVERTING progress=25% message=Executing SPARQL queries
```

These are captured and streamed to the client in real-time.

## Architecture

### Components Created

1. **ProgressEvent.java** - Model class for progress events
2. **ProgressEventPublisher.java** - Manages SSE connections and publishes events
3. **ProgressCaptureService.java** - Captures System.out and parses progress messages
4. **ProgressController.java** - SSE endpoint for client subscriptions

### How It Works

```
┌─────────┐      ┌──────────────┐      ┌─────────────────┐      ┌────────┐
│ Client  │──1──>│ SSE Endpoint │──2──>│ ProgressPublisher│      │ Client │
│         │      │ /progress/   │      │ (registers      │      │        │
│         │      │ stream/{id}  │      │  emitter)       │      │        │
└─────────┘      └──────────────┘      └─────────────────┘      └────────┘
     │                                           │                    ▲
     │                                           │                    │
     │           ┌──────────────┐                │                    │
     └────3─────>│ Conversion   │                │                    │
                 │ Endpoint     │                │                    │
                 │ /csv?sessionId│               │                    │
                 └──────┬───────┘                │                    │
                        │                        │                    │
                        ▼                        │                    │
                ┌──────────────┐                 │                    │
                │ RDFtoCSV lib │─────4──────────>│                    │
                │ (prints to   │  [PROGRESS]     │                    │
                │  System.out) │   messages      │                    │
                └──────────────┘                 │                    │
                                                 │                    │
                                                 5                    │
                                         Parse & Publish              │
                                                 │                    │
                                                 └────────────────────┘
                                                    SSE Events
```

## Client Usage

### Step 1: Generate a Session ID

```javascript
const sessionId = crypto.randomUUID(); // or any unique identifier
```

### Step 2: Establish SSE Connection

```javascript
const eventSource = new EventSource(`http://localhost:8081/api/progress/stream/${sessionId}`);

eventSource.addEventListener('progress', (event) => {
    const progress = JSON.parse(event.data);
    console.log(`Stage: ${progress.stage}, Progress: ${progress.progress}%, Message: ${progress.message}`);
    
    // Update UI progress bar
    updateProgressBar(progress.progress);
    updateProgressMessage(progress.message);
});

eventSource.onerror = (error) => {
    console.error('SSE connection error:', error);
    eventSource.close();
};
```

### Step 3: Start Conversion with Session ID

```javascript
const formData = new FormData();
formData.append('file', fileInput.files[0]);
formData.append('sessionId', sessionId); // Pass the session ID

fetch(`http://localhost:8081/csv/string?sessionId=${sessionId}`, {
    method: 'POST',
    body: formData
})
.then(response => response.text())
.then(result => {
    console.log('Conversion complete!', result);
    eventSource.close(); // Close SSE connection when done
})
.catch(error => {
    console.error('Conversion error:', error);
    eventSource.close();
});
```

### Complete Example (React)

```javascript
import React, { useState, useEffect } from 'react';

function FileConverter() {
    const [progress, setProgress] = useState(0);
    const [message, setMessage] = useState('');
    const [sessionId, setSessionId] = useState(null);
    const [eventSource, setEventSource] = useState(null);

    const handleFileUpload = async (file) => {
        // Generate session ID
        const id = crypto.randomUUID();
        setSessionId(id);

        // Establish SSE connection
        const es = new EventSource(`http://localhost:8081/api/progress/stream/${id}`);
        
        es.addEventListener('progress', (event) => {
            const progressData = JSON.parse(event.data);
            setProgress(progressData.progress);
            setMessage(progressData.message);
        });

        es.onerror = (error) => {
            console.error('SSE error:', error);
            es.close();
        };

        setEventSource(es);

        // Start conversion
        const formData = new FormData();
        formData.append('file', file);
        formData.append('sessionId', id);

        try {
            const response = await fetch(`http://localhost:8081/csv/string?sessionId=${id}`, {
                method: 'POST',
                body: formData
            });
            const result = await response.text();
            console.log('Result:', result);
            es.close();
        } catch (error) {
            console.error('Conversion failed:', error);
            es.close();
        }
    };

    useEffect(() => {
        return () => {
            // Cleanup on unmount
            if (eventSource) {
                eventSource.close();
            }
        };
    }, [eventSource]);

    return (
        <div>
            <h2>RDF to CSV Converter</h2>
            <input type="file" onChange={(e) => handleFileUpload(e.target.files[0])} />
            
            {sessionId && (
                <div>
                    <progress value={progress} max="100">{progress}%</progress>
                    <p>{message}</p>
                </div>
            )}
        </div>
    );
}
```

## Next Steps: Integrate with Service

You need to modify your service methods to use `ProgressCaptureService`. Here's an example:

```java
@Autowired
private ProgressCaptureService progressCaptureService;

@Autowired
private ProgressEventPublisher progressEventPublisher;

public String getCSVStringFromFile(AppConfig config, String sessionId) throws IOException {
    try {
        return progressCaptureService.executeWithProgressCapture(sessionId, () -> {
            RDFtoCSV rdFtoCSV = new RDFtoCSV(config);
            return rdFtoCSV.getCSVTableAsString();
        });
    } catch (Exception e) {
        progressEventPublisher.errorSession(sessionId, e);
        throw new IOException("Conversion failed", e);
    } finally {
        progressEventPublisher.completeSession(sessionId);
    }
}
```

## API Endpoints

### SSE Endpoint
- **GET** `/api/progress/stream/{sessionId}`
- **Description**: Establishes SSE connection for progress updates
- **Response**: Stream of progress events

### Progress Event Format
```json
{
    "stage": "PARSING",
    "progress": 100,
    "message": "Completed Parsing RDF file",
    "sessionId": "123e4567-e89b-12d3-a456-426614174000"
}
```

## Testing with curl

```bash
# Terminal 1: Subscribe to progress updates
curl -N http://localhost:8081/api/progress/stream/test-session-123

# Terminal 2: Start conversion (needs implementation of sessionId parameter)
curl -X POST http://localhost:8081/csv/string?sessionId=test-session-123 \
  -F "file=@test.ttl" \
  -F "table=ONE" \
  -F "conversionMethod=RDF4J"
```

## Browser Testing

```html
<!DOCTYPE html>
<html>
<head>
    <title>Progress Test</title>
</head>
<body>
    <h1>RDF to CSV Progress</h1>
    <div id="progress">
        <progress id="bar" value="0" max="100"></progress>
        <span id="percent">0%</span>
        <p id="message"></p>
    </div>

    <script>
        const sessionId = 'test-' + Date.now();
        const eventSource = new EventSource(`http://localhost:8081/api/progress/stream/${sessionId}`);
        
        eventSource.addEventListener('progress', (e) => {
            const data = JSON.parse(e.data);
            document.getElementById('bar').value = data.progress;
            document.getElementById('percent').textContent = data.progress + '%';
            document.getElementById('message').textContent = `${data.stage}: ${data.message}`;
        });
        
        console.log('Listening for progress on session:', sessionId);
    </script>
</body>
</html>
```

## Important Notes

1. **Session ID Management**: The client must generate and manage session IDs
2. **Connection Timeout**: SSE connections timeout after 30 minutes
3. **Thread Safety**: The implementation uses thread-safe collections (ConcurrentHashMap, CopyOnWriteArrayList)
4. **Cleanup**: SSE connections auto-cleanup on completion, timeout, or error
5. **CORS**: Update CORS origins in ProgressController as needed

## Limitations

1. **System.out Capture**: Currently intercepts System.out which may affect other logging
2. **Pattern Matching**: Assumes a specific log format from the library
3. **No Persistence**: Session data is not persisted (in-memory only)

## Alternative Approach (If Library Supports Callbacks)

If the RDFtoCSV library supports progress callbacks, you can implement a ProgressListener:

```java
public class ProgressListenerImpl implements ProgressListener {
    private final String sessionId;
    private final ProgressEventPublisher publisher;
    
    @Override
    public void onProgress(String stage, int progress, String message) {
        ProgressEvent event = new ProgressEvent(stage, progress, message, sessionId);
        publisher.publishProgress(event);
    }
}
```

This would be cleaner than capturing System.out.

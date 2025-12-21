# Testing SSE (Server-Sent Events) Guide

## Overview

This guide explains how to test SSE functionality and observe network activity in tests.

## Testing Approaches

### 1. Unit Testing with Mocks

**ProgressEventPublisherTest** demonstrates:
- Capturing data sent through SSE emitters
- Verifying event isolation between sessions
- Testing completion and error handling

```java
// Custom test emitter captures what would be sent over network
private static class TestSseEmitter extends SseEmitter {
    private final List<Object> capturedData;
    
    public void captureData(Object data) {
        capturedData.add(data);  // Store for verification
    }
}
```

### 2. Integration Testing with MockMvc

**ProgressControllerTest** demonstrates:
- Testing SSE endpoints with async support
- Verifying content types and CORS headers
- Observing the full request/response flow

```java
MvcResult result = mockMvc.perform(get("/api/progress/stream/{sessionId}", sessionId))
    .andExpect(request().asyncStarted())  // Verify async handling
    .andReturn();

assertNotNull(result.getAsyncResult());  // Get the SSE emitter
```

### 3. Testing Progress Capture

**ProgressCaptureServiceTest** demonstrates:
- Mocking the publisher to verify events
- Using ArgumentCaptor to inspect published events
- Testing regex pattern matching

```java
ArgumentCaptor<ProgressEvent> eventCaptor = ArgumentCaptor.forClass(ProgressEvent.class);
verify(mockPublisher, times(3)).publishProgress(eventCaptor.capture());

List<ProgressEvent> events = eventCaptor.getAllValues();
// Verify each event
```

## Observing Network Activity

### Method 1: Using MockMvc (Integration Tests)

MockMvc provides several ways to observe SSE activity:

```java
@Test
void testSseNetworkActivity() throws Exception {
    MvcResult result = mockMvc.perform(get("/api/progress/stream/test-id"))
        .andExpect(status().isOk())
        .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_EVENT_STREAM))
        .andDo(print())  // Prints request/response details
        .andReturn();

    // Get async result (the SSE emitter)
    SseEmitter emitter = (SseEmitter) result.getAsyncResult();
    
    // The emitter is what would send data over network
    // In tests, we can't directly observe the stream,
    // but we can verify the emitter was created
}
```

### Method 2: Using Argument Captors

Capture what's being published to emitters:

```java
@Mock
private ProgressEventPublisher mockPublisher;

@Test
void observePublishedEvents() {
    ArgumentCaptor<ProgressEvent> captor = ArgumentCaptor.forClass(ProgressEvent.class);
    
    // Execute code that publishes events
    service.doSomething();
    
    // Capture all published events
    verify(mockPublisher, times(3)).publishProgress(captor.capture());
    
    // Inspect captured events
    List<ProgressEvent> events = captor.getAllValues();
    events.forEach(event -> {
        System.out.println("Stage: " + event.getStage());
        System.out.println("Progress: " + event.getProgress() + "%");
        System.out.println("Message: " + event.getMessage());
    });
}
```

### Method 3: Manual Testing with Browser/curl

For observing actual network traffic:

#### Browser Testing

```html
<!DOCTYPE html>
<html>
<body>
    <h1>SSE Test</h1>
    <div id="events"></div>
    
    <script>
        const eventSource = new EventSource('http://localhost:8081/api/progress/stream/test-123');
        
        eventSource.addEventListener('progress', (e) => {
            const data = JSON.parse(e.data);
            const div = document.getElementById('events');
            div.innerHTML += `<p>${data.stage}: ${data.progress}% - ${data.message}</p>`;
        });
        
        // Open browser DevTools > Network tab to see SSE activity
    </script>
</body>
</html>
```

#### curl Testing

```bash
# Terminal 1: Start SSE listener
curl -N http://localhost:8081/api/progress/stream/test-session-123

# Terminal 2: Trigger conversion with same session ID
curl -X POST http://localhost:8081/csv/string?sessionId=test-session-123 \
  -F "file=@test.ttl"

# Terminal 1 will show SSE events as they arrive
```

### Method 4: Using TestRestTemplate (Spring Boot)

For actual HTTP connections in tests:

```java
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
class SseNetworkTest {
    
    @Autowired
    private TestRestTemplate restTemplate;
    
    @Test
    void testSseWithRealConnection() {
        String sessionId = "network-test-123";
        
        // Use a separate thread to connect
        CompletableFuture<Void> sseConnection = CompletableFuture.runAsync(() -> {
            restTemplate.execute(
                "/api/progress/stream/" + sessionId,
                HttpMethod.GET,
                null,
                response -> {
                    // Read SSE stream
                    BufferedReader reader = new BufferedReader(
                        new InputStreamReader(response.getBody())
                    );
                    
                    String line;
                    while ((line = reader.readLine()) != null) {
                        System.out.println("SSE: " + line);
                        // Parse and verify SSE data
                    }
                    return null;
                }
            );
        });
        
        // Give SSE time to connect
        Thread.sleep(500);
        
        // Trigger events
        progressEventPublisher.publishProgress(
            new ProgressEvent("PARSING", 50, "Test", sessionId)
        );
    }
}
```

### Method 5: Using WireMock for Network Stubs

If you need to verify SSE client behavior:

```java
@Test
void testSseClientWithWireMock() {
    // Set up WireMock to simulate SSE server
    wireMockServer.stubFor(get("/progress/stream/test")
        .willReturn(aResponse()
            .withStatus(200)
            .withHeader("Content-Type", "text/event-stream")
            .withBody("event: progress\ndata: {\"progress\":50}\n\n")));
    
    // Your client code connects to WireMock URL
    // Verify it handles SSE correctly
}
```

## Debugging SSE in Tests

### Enable Detailed Logging

```properties
# application-test.properties
logging.level.org.springframework.web=DEBUG
logging.level.org.rdftocsvconverter=DEBUG
```

### Add Debug Output

```java
@Test
void debugSseFlow() throws Exception {
    MvcResult result = mockMvc.perform(get("/api/progress/stream/test"))
        .andDo(print())  // Prints full request/response
        .andExpect(status().isOk())
        .andReturn();
    
    System.out.println("Async started: " + result.getRequest().isAsyncStarted());
    System.out.println("Async result: " + result.getAsyncResult());
}
```

### Use Breakpoints

Set breakpoints in:
- `ProgressController.streamProgress()` - When SSE connection is established
- `ProgressEventPublisher.publishProgress()` - When events are published
- `ProgressCaptureService.parseAndPublishProgress()` - When parsing progress logs

## Verification Strategies

### 1. Verify Event Count

```java
verify(mockPublisher, times(expectedCount)).publishProgress(any());
```

### 2. Verify Event Content

```java
ArgumentCaptor<ProgressEvent> captor = ArgumentCaptor.forClass(ProgressEvent.class);
verify(mockPublisher).publishProgress(captor.capture());

ProgressEvent event = captor.getValue();
assertEquals("PARSING", event.getStage());
assertEquals(100, event.getProgress());
```

### 3. Verify Event Order

```java
InOrder inOrder = inOrder(mockPublisher);
inOrder.verify(mockPublisher).publishProgress(argThat(e -> e.getProgress() == 0));
inOrder.verify(mockPublisher).publishProgress(argThat(e -> e.getProgress() == 50));
inOrder.verify(mockPublisher).publishProgress(argThat(e -> e.getProgress() == 100));
```

### 4. Verify Session Isolation

```java
// Publish to session1
progressEventPublisher.publishProgress(event1);

// Verify session2 didn't receive it
verify(session2Emitter, never()).send(any());
```

## Common Testing Patterns

### Pattern 1: Test SSE Endpoint

```java
@Test
void testEndpoint() throws Exception {
    mockMvc.perform(get("/api/progress/stream/{id}", "test"))
        .andExpect(status().isOk())
        .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_EVENT_STREAM))
        .andExpect(request().asyncStarted());
}
```

### Pattern 2: Test Event Publishing

```java
@Test
void testPublishing() {
    List<Object> captured = new ArrayList<>();
    CustomEmitter emitter = new CustomEmitter(captured);
    
    publisher.addEmitter("session1", emitter);
    publisher.publishProgress(event);
    
    assertEquals(1, captured.size());
}
```

### Pattern 3: Test Progress Capture

```java
@Test
void testCapture() throws Exception {
    service.executeWithProgressCapture("session1", () -> {
        System.out.println("[PROGRESS] stage=TEST progress=100% message=Done");
        return "result";
    });
    
    verify(mockPublisher).publishProgress(argThat(e -> 
        e.getStage().equals("TEST") && e.getProgress() == 100
    ));
}
```

## Performance Testing

### Test SSE Under Load

```java
@Test
void testMultipleClients() throws Exception {
    int clientCount = 100;
    CountDownLatch latch = new CountDownLatch(clientCount);
    
    for (int i = 0; i < clientCount; i++) {
        String sessionId = "session-" + i;
        new Thread(() -> {
            try {
                mockMvc.perform(get("/api/progress/stream/" + sessionId))
                    .andExpect(status().isOk());
                latch.countDown();
            } catch (Exception e) {
                fail("Client failed: " + e);
            }
        }).start();
    }
    
    assertTrue(latch.await(10, TimeUnit.SECONDS), "All clients should connect");
}
```

## Troubleshooting

### Issue: Events Not Being Captured

**Solution**: Ensure the emitter is registered before publishing events

```java
publisher.addEmitter(sessionId, emitter);
Thread.sleep(100);  // Give it time to register
publisher.publishProgress(event);
```

### Issue: Async Tests Failing

**Solution**: Use proper async handling in tests

```java
MvcResult result = mockMvc.perform(get("/endpoint"))
    .andExpect(request().asyncStarted())
    .andReturn();

mockMvc.perform(asyncDispatch(result))
    .andExpect(status().isOk());
```

### Issue: SSE Connection Timing Out

**Solution**: Increase timeout in tests

```java
SseEmitter emitter = new SseEmitter(60_000L);  // 60 second timeout
```

## Summary

The test files demonstrate multiple approaches to testing SSE:

1. **ProgressEventPublisherTest** - Unit tests with custom emitters
2. **ProgressCaptureServiceTest** - Mocking and argument capture
3. **ProgressControllerTest** - Integration tests with MockMvc

For observing network activity:
- Use ArgumentCaptor to inspect events
- Use MockMvc with `.andDo(print())`
- Use browser DevTools Network tab
- Use curl with `-N` flag
- Add debug logging

Each approach provides different insights into the SSE behavior.

package org.rdftocsvconverter.RDFtoCSVW.redis;

import org.junit.jupiter.api.Test;
import org.rdftocsvconverter.RDFtoCSVW.service.ExpensiveComputationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.*;
import org.rdftocsvconverter.RDFtoCSVW.RDFToCSVWApiApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;

@SpringBootTest(classes = RDFToCSVWApiApplication.class)
@AutoConfigureMockMvc
class ComputationControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ExpensiveComputationService computationService;

    @Test
    void compute_ShouldReturnServiceResult() throws Exception {
        String input = "integrationTest";
        String expectedResult = "Integration test result";

        when(computationService.expensiveComputation(input)).thenReturn(expectedResult);

        mockMvc.perform(get("/api/compute/{input}", input))
                .andExpect(status().isOk())
                .andExpect(content().string(expectedResult));
    }

    @Test
    void clearCache_ShouldReturnSuccessMessage() throws Exception {
        String input = "cacheToClear";

        mockMvc.perform(post("/api/compute/clear/{input}", input))
                .andExpect(status().isOk())
                .andExpect(content().string("Cache cleared for " + input));

        verify(computationService).clearCache(input);
    }

    @Test
    void compute_ShouldHandleEmptyInput() throws Exception {
        when(computationService.expensiveComputation("")).thenReturn("Empty input result");

        mockMvc.perform(get("/api/compute/"))
                .andExpect(status().isOk())
                .andExpect(content().string("Empty input result"));
    }

    @Test
    void compute_ShouldCacheResults() throws Exception {
        String input = "cacheableInput";
        String firstResult = "First computation";
        String secondResult = "First computation"; // Same as first because cached

        when(computationService.expensiveComputation(input))
                .thenReturn(firstResult)
                .thenThrow(new RuntimeException("Should use cache on second call"));

        // First call - should hit service
        mockMvc.perform(get("/api/compute/{input}", input))
                .andExpect(status().isOk())
                .andExpect(content().string(firstResult));

        // Second call - should return cached value
        mockMvc.perform(get("/api/compute/{input}", input))
                .andExpect(status().isOk())
                .andExpect(content().string(secondResult));
    }

    @Test
    void clearCache_ShouldActuallyClearCache() throws Exception {
        String input = "clearableInput";
        String firstResult = "First computation";
        String secondResult = "Second computation";

        when(computationService.expensiveComputation(input))
                .thenReturn(firstResult)
                .thenReturn(secondResult);

        // First call
        mockMvc.perform(get("/api/compute/{input}", input));

        // Clear cache
        mockMvc.perform(post("/api/compute/clear/{input}", input));

        // Second call - should hit service again
        mockMvc.perform(get("/api/compute/{input}", input))
                .andExpect(content().string(secondResult));
    }
}

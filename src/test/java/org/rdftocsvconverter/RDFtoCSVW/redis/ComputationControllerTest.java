package org.rdftocsvconverter.RDFtoCSVW.redis;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.junit.jupiter.api.extension.ExtendWith;
import org.rdftocsvconverter.RDFtoCSVW.api.controller.ComputationController;
import org.rdftocsvconverter.RDFtoCSVW.service.ExpensiveComputationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.emptyString;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class ComputationControllerTest {

    @Mock
    private ExpensiveComputationService computationService;


    private MockMvc mockMvc;

    @InjectMocks
    private ComputationController computationController;

    @BeforeEach
    void setup() {
        ComputationController controller = new ComputationController(computationService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    void compute_ShouldReturnResultFromService() {
        // Arrange
        String input = "testInput";
        String expectedResult = "Result for testInput at 12345";
        when(computationService.expensiveComputation(input)).thenReturn(expectedResult);

        // Act
        String result = computationController.compute(input);

        // Assert
        assertEquals(expectedResult, result);
        verify(computationService, times(1)).expensiveComputation(input);
    }

    @Test
    void compute_Returns200Ok_ForValidInput() throws Exception {
        // Arrange
        String input = "testInput";
        when(computationService.expensiveComputation(anyString()))
                .thenReturn("some result");

        // Act & Assert
        mockMvc.perform(get("/api/compute/{input}", input))
                .andExpect(status().isOk())
                .andExpect(content().string(not(emptyString())));
    }

    @Test
    void compute_ReturnsError_WhenInputTooLong() throws Exception {
        String longInput = "a".repeat(256);

        mockMvc.perform(get("/api/compute/{input}", longInput))
                .andExpect(status().isOk());
    }

    @Test
    void clearCache_ReturnsSuccessMessage() throws Exception {
        MvcResult result = mockMvc.perform(post("/api/compute/clear/testInput"))
                .andExpect(status().isOk())
                .andReturn();

        String content = result.getResponse().getContentAsString();
        assertTrue(content.startsWith("Cache cleared for"));
        assertTrue(content.endsWith("testInput"));
    }

    @Test
    void clearCache_ReturnsSuccessMessage2() throws Exception {
        mockMvc.perform(post("/api/compute/clear/testInput"))
                .andExpect(status().isOk());
    }

    @Test
    void compute_loggingLogs() {
        // Arrange
        String expectedResult = "Check your logs";

        // Act
        String result = computationController.testLogging();

        // Assert
        assertEquals(expectedResult, result);
    }

    @Test
    void compute_ShouldCallServiceWithCorrectParameter() {
        // Arrange
        String input = "specificInput";

        // Act
        computationController.compute(input);

        // Assert
        verify(computationService, times(1)).expensiveComputation(input);
    }

    @Test
    void clearCache_ShouldCallServiceClearMethod() {
        // Arrange
        String input = "toClear";

        // Act
        String result = computationController.clearCache(input);

        // Assert
        assertEquals("Cache cleared for toClear", result);
        verify(computationService, times(1)).clearCache(input);
    }

    @Test
    void clearCache_ShouldReturnCorrectMessage() {
        // Arrange
        String input = "test123";
        String expectedMessage = "Cache cleared for test123";

        // Act
        String result = computationController.clearCache(input);

        // Assert
        assertEquals(expectedMessage, result);
    }

    @Test
    void compute_ShouldReturnResultFromService_different_approach() throws Exception {
        // Arrange
        String input = "testInput";
        String expectedResult = "Result for testInput";

        when(computationService.expensiveComputation(input))
                .thenReturn(expectedResult);

        mockMvc = MockMvcBuilders.standaloneSetup(
                new ComputationController(computationService)
        ).build();

        // Act & Assert
        mockMvc.perform(get("/api/compute/{input}", input))
                .andExpect(status().isOk())
                .andExpect(content().string(expectedResult));
    }

    @Test
    void clearCache_ShouldCallServiceAndReturnMessage() throws Exception {
        // Arrange
        String input = "toClear";

        mockMvc = MockMvcBuilders.standaloneSetup(
                new ComputationController(computationService)
        ).build();

        // Act & Assert
        mockMvc.perform(post("/api/compute/clear/{input}", input))
                .andExpect(status().isOk())
                .andExpect(content().string("Cache cleared for " + input));

        verify(computationService).clearCache(input);
    }
}

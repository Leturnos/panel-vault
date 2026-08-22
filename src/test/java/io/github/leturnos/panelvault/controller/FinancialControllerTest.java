package io.github.leturnos.panelvault.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.leturnos.panelvault.config.TokenService;
import io.github.leturnos.panelvault.dto.FinancialSummaryResponseDTO;
import io.github.leturnos.panelvault.dto.MonthlyExpenseResponseDTO;
import io.github.leturnos.panelvault.dto.WorkFinancialResponseDTO;
import io.github.leturnos.panelvault.exception.ResourceNotFoundException;
import io.github.leturnos.panelvault.repository.UserRepository;
import io.github.leturnos.panelvault.service.FinancialService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(FinancialController.class)
@WithMockUser
class FinancialControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private FinancialService service;

    @MockitoBean
    private TokenService tokenService;

    @MockitoBean
    private UserRepository userRepository;

    @MockitoBean
    private UserDetailsService userDetailsService;

    @Nested
    @DisplayName("GET /finances/summary tests")
    class SummaryTests {

        @Test
        @DisplayName("Should return 200 OK with financial summary")
        void summary_shouldReturn200AndSummary() throws Exception {
            FinancialSummaryResponseDTO response = new FinancialSummaryResponseDTO(
                    new BigDecimal("350.00"),
                    10,
                    new BigDecimal("35.00"),
                    new BigDecimal("45.00"),
                    new BigDecimal("25.00"),
                    new BigDecimal("1085.00")
            );

            Mockito.when(service.getSummary(any())).thenReturn(response);

            mockMvc.perform(get("/finances/summary")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.totalSpent", is(350.00)))
                    .andExpect(jsonPath("$.totalVolumesWithPrice", is(10)))
                    .andExpect(jsonPath("$.averageVolumePrice", is(35.00)))
                    .andExpect(jsonPath("$.highestPrice", is(45.00)))
                    .andExpect(jsonPath("$.lowestPrice", is(25.00)))
                    .andExpect(jsonPath("$.estimatedToCompleteCollection", is(1085.00)));
        }
    }

    @Nested
    @DisplayName("GET /finances/history tests")
    class HistoryTests {

        @Test
        @DisplayName("Should return 200 OK with monthly expenses list")
        void history_shouldReturn200AndHistoryList() throws Exception {
            MonthlyExpenseResponseDTO item = new MonthlyExpenseResponseDTO(
                    2026,
                    5,
                    new BigDecimal("120.50"),
                    3L
            );

            Mockito.when(service.getSummaryByDate(any(), any(), any(), any()))
                    .thenReturn(List.of(item));

            mockMvc.perform(get("/finances/history")
                            .param("year", "2026")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(1)))
                    .andExpect(jsonPath("$[0].year", is(2026)))
                    .andExpect(jsonPath("$[0].month", is(5)))
                    .andExpect(jsonPath("$[0].totalSpent", is(120.50)))
                    .andExpect(jsonPath("$[0].volumeCount", is(3)));
        }
    }

    @Nested
    @DisplayName("GET /finances/works tests")
    class SummaryWorksTests {

        @Test
        @DisplayName("Should return 200 OK with paginated works financial summary")
        void summaryWorks_shouldReturn200AndPage() throws Exception {
            WorkFinancialResponseDTO dto = new WorkFinancialResponseDTO(
                    10L,
                    "Berserk",
                    new BigDecimal("350.00"),
                    10,
                    41,
                    new BigDecimal("35.00"),
                    new BigDecimal("1085.00")
            );

            Page<WorkFinancialResponseDTO> page = new PageImpl<>(List.of(dto), PageRequest.of(0, 10), 1);
            Mockito.when(service.getSummaryByUser(any(), any())).thenReturn(page);

            mockMvc.perform(get("/finances/works")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content", hasSize(1)))
                    .andExpect(jsonPath("$.content[0].workId", is(10)))
                    .andExpect(jsonPath("$.content[0].workTitle", is("Berserk")))
                    .andExpect(jsonPath("$.content[0].totalSpent", is(350.00)));
        }
    }

    @Nested
    @DisplayName("GET /finances/works/{workId} tests")
    class SummaryByWorkTests {

        @Test
        @DisplayName("Should return 200 OK with work financial detail when work exists")
        void summaryByWork_shouldReturn200AndWorkDetail() throws Exception {
            WorkFinancialResponseDTO dto = new WorkFinancialResponseDTO(
                    10L,
                    "Berserk",
                    new BigDecimal("350.00"),
                    10,
                    41,
                    new BigDecimal("35.00"),
                    new BigDecimal("1085.00")
            );

            Mockito.when(service.getSummaryByWork(any(), eq(10L))).thenReturn(dto);

            mockMvc.perform(get("/finances/works/10")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.workId", is(10)))
                    .andExpect(jsonPath("$.workTitle", is("Berserk")))
                    .andExpect(jsonPath("$.totalSpent", is(350.00)))
                    .andExpect(jsonPath("$.ownedVolumesCount", is(10)))
                    .andExpect(jsonPath("$.totalVolumes", is(41)))
                    .andExpect(jsonPath("$.averagePricePaid", is(35.00)))
                    .andExpect(jsonPath("$.estimatedRemainingCost", is(1085.00)));
        }

        @Test
        @DisplayName("Should return 404 Not Found when work does not exist")
        void summaryByWork_shouldReturn404_whenWorkNotFound() throws Exception {
            Mockito.when(service.getSummaryByWork(any(), eq(999L)))
                    .thenThrow(new ResourceNotFoundException("Obra não encontrada"));

            mockMvc.perform(get("/finances/works/999")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.title", is("Recurso Não Encontrado")));
        }
    }
}

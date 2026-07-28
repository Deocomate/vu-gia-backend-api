package vn.springboot.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import vn.springboot.dto.request.altar.AltarDesignRenameRequest;
import vn.springboot.dto.request.altar.AltarDesignRequest;
import vn.springboot.dto.response.PageResponse;
import vn.springboot.dto.response.altar.AltarDesignResponse;
import vn.springboot.dto.response.altar.AltarDesignSummaryResponse;
import vn.springboot.security.CustomAccessDeniedHandler;
import vn.springboot.security.JwtAuthenticationEntryPoint;
import vn.springboot.security.SecurityConfig;
import vn.springboot.security.jwt.JwtAuthenticationFilter;
import vn.springboot.security.jwt.JwtService;
import vn.springboot.service.AltarDesignService;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Confirms {@code /api/altar-designs/**} requires authentication by default (no
 * {@code SecurityConfig} entry needed — see the phase-05 spec's "no security config change"
 * decision) and that the controller wires through to the service correctly. Ownership isolation
 * itself is exercised at the service layer (see {@code AltarDesignServiceImplTest}), where the
 * caller is resolved from the security context.
 */
@WebMvcTest(AltarDesignController.class)
@Import({SecurityConfig.class, JwtAuthenticationFilter.class,
        JwtAuthenticationEntryPoint.class, CustomAccessDeniedHandler.class})
class AltarDesignControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AltarDesignService altarDesignService;

    // Collaborators the security filter chain needs.
    @MockitoBean
    private JwtService jwtService;
    @MockitoBean
    private UserDetailsService userDetailsService;

    private AltarDesignRequest validCreateRequest() {
        return AltarDesignRequest.builder()
                .name("My design")
                .thumb("/files/thumb.png")
                .altarModelSizeId(5L)
                .items("[]")
                .accessories("[]")
                .totalPrice(100_000L)
                .build();
    }

    @Test
    void list_returns401_whenUnauthenticated() throws Exception {
        mockMvc.perform(get("/api/altar-designs"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(4010));
    }

    @Test
    void getById_returns401_whenUnauthenticated() throws Exception {
        mockMvc.perform(get("/api/altar-designs/1"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(4010));
    }

    @Test
    void create_returns401_whenUnauthenticated() throws Exception {
        mockMvc.perform(post("/api/altar-designs")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validCreateRequest())))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(4010));
    }

    @Test
    void rename_returns401_whenUnauthenticated() throws Exception {
        mockMvc.perform(patch("/api/altar-designs/1")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                AltarDesignRenameRequest.builder().name("New name").build())))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(4010));
    }

    @Test
    void delete_returns401_whenUnauthenticated() throws Exception {
        mockMvc.perform(delete("/api/altar-designs/1").with(csrf()))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(4010));
    }

    @Test
    @WithMockUser
    void list_returns200_forAuthenticatedUser() throws Exception {
        when(altarDesignService.list(any())).thenReturn(PageResponse.<AltarDesignSummaryResponse>builder()
                .content(List.of(AltarDesignSummaryResponse.builder().id(1L).name("My design").build()))
                .pageNumber(1).pageSize(20).totalElements(1).totalPages(1).first(true).last(true)
                .build());

        mockMvc.perform(get("/api/altar-designs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1000))
                .andExpect(jsonPath("$.data.content[0].name").value("My design"));
    }

    @Test
    @WithMockUser
    void create_returns200_forValidBody() throws Exception {
        when(altarDesignService.create(any()))
                .thenReturn(AltarDesignResponse.builder().id(1L).name("My design").totalPrice(100_000L).build());

        mockMvc.perform(post("/api/altar-designs")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validCreateRequest())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1000))
                .andExpect(jsonPath("$.data.name").value("My design"));
    }

    @Test
    @WithMockUser
    void create_returns4001_whenNameMissing() throws Exception {
        AltarDesignRequest invalid = AltarDesignRequest.builder()
                .thumb("/files/thumb.png").altarModelSizeId(5L).items("[]").accessories("[]").totalPrice(1L)
                .build();

        mockMvc.perform(post("/api/altar-designs")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalid)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(4001));
    }

    @Test
    @WithMockUser
    void rename_returns200_forAuthenticatedUser() throws Exception {
        when(altarDesignService.rename(eq(1L), any()))
                .thenReturn(AltarDesignResponse.builder().id(1L).name("New name").build());

        mockMvc.perform(patch("/api/altar-designs/1")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                AltarDesignRenameRequest.builder().name("New name").build())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.name").value("New name"));
    }

    @Test
    @WithMockUser
    void delete_returns200_forAuthenticatedUser() throws Exception {
        mockMvc.perform(delete("/api/altar-designs/1").with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1000));
    }
}

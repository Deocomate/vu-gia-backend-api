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
import vn.springboot.dto.request.altar.AltarStyleCreateRequest;
import vn.springboot.dto.response.PageResponse;
import vn.springboot.dto.response.altar.AltarStyleResponse;
import vn.springboot.security.CustomAccessDeniedHandler;
import vn.springboot.security.JwtAuthenticationEntryPoint;
import vn.springboot.security.SecurityConfig;
import vn.springboot.security.jwt.JwtAuthenticationFilter;
import vn.springboot.security.jwt.JwtService;
import vn.springboot.service.AltarStyleService;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AltarStyleController.class)
@Import({SecurityConfig.class, JwtAuthenticationFilter.class,
        JwtAuthenticationEntryPoint.class, CustomAccessDeniedHandler.class})
class AltarStyleControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AltarStyleService altarStyleService;

    // Collaborators the security filter chain needs.
    @MockitoBean
    private JwtService jwtService;
    @MockitoBean
    private UserDetailsService userDetailsService;

    private PageResponse<AltarStyleResponse> onePage() {
        return PageResponse.<AltarStyleResponse>builder()
                .content(List.of(AltarStyleResponse.builder().id(1L).name("Men lam").isActive(true).build()))
                .pageNumber(1).pageSize(10).totalElements(1).totalPages(1).first(true).last(true)
                .build();
    }

    private AltarStyleCreateRequest validCreateRequest() {
        return AltarStyleCreateRequest.builder()
                .name("Men lam")
                .thumb("/files/x.jpg")
                .description("desc")
                .build();
    }

    @Test
    void search_isPublic_returns200() throws Exception {
        when(altarStyleService.search(any())).thenReturn(onePage());

        mockMvc.perform(get("/api/altar-styles"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1000))
                .andExpect(jsonPath("$.data.content[0].name").value("Men lam"));
    }

    @Test
    void getById_isPublic_returns200() throws Exception {
        when(altarStyleService.getById(1L)).thenReturn(AltarStyleResponse.builder().id(1L).name("Men lam").build());

        mockMvc.perform(get("/api/altar-styles/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.name").value("Men lam"));
    }

    @Test
    void create_returns401_whenUnauthenticated() throws Exception {
        mockMvc.perform(post("/api/altar-styles")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validCreateRequest())))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(4010));
    }

    @Test
    @WithMockUser(roles = "CUSTOMER")
    void create_returns403_forInsufficientRole() throws Exception {
        mockMvc.perform(post("/api/altar-styles")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validCreateRequest())))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value(4030));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void create_returns200_forAdmin() throws Exception {
        when(altarStyleService.create(any()))
                .thenReturn(AltarStyleResponse.builder().id(1L).name("Men lam").build());

        mockMvc.perform(post("/api/altar-styles")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validCreateRequest())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.name").value("Men lam"));
    }

    @Test
    @WithMockUser(roles = "CUSTOMER")
    void delete_returns403_forInsufficientRole() throws Exception {
        mockMvc.perform(delete("/api/altar-styles/1").with(csrf()))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value(4030));
    }

    @Test
    @WithMockUser(roles = "SUPERADMIN")
    void delete_returns200_forSuperadmin() throws Exception {
        mockMvc.perform(delete("/api/altar-styles/1").with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1000));
    }
}

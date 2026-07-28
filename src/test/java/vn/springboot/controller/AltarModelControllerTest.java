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
import vn.springboot.dto.request.altar.AltarModelCreateRequest;
import vn.springboot.dto.request.altar.AltarModelSizeCreateRequest;
import vn.springboot.dto.response.PageResponse;
import vn.springboot.dto.response.altar.AltarModelResponse;
import vn.springboot.dto.response.altar.AltarModelSizeResponse;
import vn.springboot.security.CustomAccessDeniedHandler;
import vn.springboot.security.JwtAuthenticationEntryPoint;
import vn.springboot.security.SecurityConfig;
import vn.springboot.security.jwt.JwtAuthenticationFilter;
import vn.springboot.security.jwt.JwtService;
import vn.springboot.service.AltarModelService;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AltarModelController.class)
@Import({SecurityConfig.class, JwtAuthenticationFilter.class,
        JwtAuthenticationEntryPoint.class, CustomAccessDeniedHandler.class})
class AltarModelControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AltarModelService altarModelService;

    // Collaborators the security filter chain needs.
    @MockitoBean
    private JwtService jwtService;
    @MockitoBean
    private UserDetailsService userDetailsService;

    private PageResponse<AltarModelResponse> onePage() {
        return PageResponse.<AltarModelResponse>builder()
                .content(List.of(AltarModelResponse.builder()
                        .id(1L).name("Bàn thờ gia tiên").isActive(true).sizes(List.of()).build()))
                .pageNumber(1).pageSize(10).totalElements(1).totalPages(1).first(true).last(true)
                .build();
    }

    private AltarModelCreateRequest validCreateRequest() {
        return AltarModelCreateRequest.builder()
                .name("Bàn thờ gia tiên")
                .thumb("/files/x.jpg")
                .description("desc")
                .build();
    }

    private AltarModelSizeCreateRequest validSizeCreateRequest() {
        return AltarModelSizeCreateRequest.builder()
                .label("127 x 61 cm")
                .widthCm(127)
                .depthCm(61)
                .backgroundImage("/files/bg.jpg")
                .surfaceLeft(0.1).surfaceTop(0.1).surfaceRight(0.9).surfaceBottom(0.9)
                .surfaceWidthCm(120)
                .build();
    }

    @Test
    void search_isPublic_returns200() throws Exception {
        when(altarModelService.search(any())).thenReturn(onePage());

        mockMvc.perform(get("/api/altar-models"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1000))
                .andExpect(jsonPath("$.data.content[0].name").value("Bàn thờ gia tiên"));
    }

    @Test
    void getById_isPublic_returns200() throws Exception {
        when(altarModelService.getById(1L)).thenReturn(
                AltarModelResponse.builder().id(1L).name("Bàn thờ gia tiên").sizes(List.of()).build());

        mockMvc.perform(get("/api/altar-models/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.name").value("Bàn thờ gia tiên"));
    }

    @Test
    void create_returns401_whenUnauthenticated() throws Exception {
        mockMvc.perform(post("/api/altar-models")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validCreateRequest())))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(4010));
    }

    @Test
    @WithMockUser(roles = "CUSTOMER")
    void create_returns403_forInsufficientRole() throws Exception {
        mockMvc.perform(post("/api/altar-models")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validCreateRequest())))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value(4030));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void create_returns200_forAdmin() throws Exception {
        when(altarModelService.create(any())).thenReturn(
                AltarModelResponse.builder().id(1L).name("Bàn thờ gia tiên").sizes(List.of()).build());

        mockMvc.perform(post("/api/altar-models")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validCreateRequest())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.name").value("Bàn thờ gia tiên"));
    }

    @Test
    @WithMockUser(roles = "CUSTOMER")
    void delete_returns403_forInsufficientRole() throws Exception {
        mockMvc.perform(delete("/api/altar-models/1").with(csrf()))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value(4030));
    }

    @Test
    @WithMockUser(roles = "SUPERADMIN")
    void delete_returns200_forSuperadmin() throws Exception {
        mockMvc.perform(delete("/api/altar-models/1").with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1000));
    }

    @Test
    void createSize_returns401_whenUnauthenticated() throws Exception {
        mockMvc.perform(post("/api/altar-models/1/sizes")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validSizeCreateRequest())))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(4010));
    }

    @Test
    @WithMockUser(roles = "CUSTOMER")
    void createSize_returns403_forInsufficientRole() throws Exception {
        mockMvc.perform(post("/api/altar-models/1/sizes")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validSizeCreateRequest())))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value(4030));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createSize_returns200_forAdmin() throws Exception {
        when(altarModelService.createSize(anyLong(), any()))
                .thenReturn(AltarModelSizeResponse.builder().id(10L).altarModelId(1L).build());

        mockMvc.perform(post("/api/altar-models/1/sizes")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validSizeCreateRequest())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(10));
    }

    @Test
    @WithMockUser(roles = "SUPERADMIN")
    void deleteSize_returns200_forSuperadmin() throws Exception {
        mockMvc.perform(delete("/api/altar-models/1/sizes/10").with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1000));
    }
}

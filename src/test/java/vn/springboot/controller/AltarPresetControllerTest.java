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
import vn.springboot.dto.request.altar.AltarPresetItemRequest;
import vn.springboot.dto.request.altar.AltarPresetRequest;
import vn.springboot.dto.response.PageResponse;
import vn.springboot.dto.response.altar.AltarPresetResponse;
import vn.springboot.security.CustomAccessDeniedHandler;
import vn.springboot.security.JwtAuthenticationEntryPoint;
import vn.springboot.security.SecurityConfig;
import vn.springboot.security.jwt.JwtAuthenticationFilter;
import vn.springboot.security.jwt.JwtService;
import vn.springboot.service.AltarPresetService;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AltarPresetController.class)
@Import({SecurityConfig.class, JwtAuthenticationFilter.class,
        JwtAuthenticationEntryPoint.class, CustomAccessDeniedHandler.class})
class AltarPresetControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AltarPresetService altarPresetService;

    // Collaborators the security filter chain needs.
    @MockitoBean
    private JwtService jwtService;
    @MockitoBean
    private UserDetailsService userDetailsService;

    private PageResponse<AltarPresetResponse> onePage() {
        return PageResponse.<AltarPresetResponse>builder()
                .content(List.of(AltarPresetResponse.builder()
                        .id(1L).name("Bộ gợi ý A").isActive(true).items(List.of()).build()))
                .pageNumber(1).pageSize(10).totalElements(1).totalPages(1).first(true).last(true)
                .build();
    }

    private AltarPresetRequest validCreateRequest() {
        return AltarPresetRequest.builder()
                .name("Bộ gợi ý A")
                .thumb("/files/thumb.jpg")
                .description("desc")
                .altarModelSizeId(1L)
                .items(List.of(AltarPresetItemRequest.builder().productId(5L).quantity(1).build()))
                .build();
    }

    @Test
    void search_isPublic_returns200() throws Exception {
        when(altarPresetService.search(any())).thenReturn(onePage());

        mockMvc.perform(get("/api/altar-presets"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1000))
                .andExpect(jsonPath("$.data.content[0].name").value("Bộ gợi ý A"));
    }

    @Test
    void getById_isPublic_returns200() throws Exception {
        when(altarPresetService.getById(1L)).thenReturn(
                AltarPresetResponse.builder().id(1L).name("Bộ gợi ý A").items(List.of()).build());

        mockMvc.perform(get("/api/altar-presets/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.name").value("Bộ gợi ý A"));
    }

    @Test
    void create_returns401_whenUnauthenticated() throws Exception {
        mockMvc.perform(post("/api/altar-presets")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validCreateRequest())))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(4010));
    }

    @Test
    @WithMockUser(roles = "CUSTOMER")
    void create_returns403_forInsufficientRole() throws Exception {
        mockMvc.perform(post("/api/altar-presets")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validCreateRequest())))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value(4030));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void create_returns200_forAdmin() throws Exception {
        when(altarPresetService.create(any())).thenReturn(
                AltarPresetResponse.builder().id(1L).name("Bộ gợi ý A").items(List.of()).build());

        mockMvc.perform(post("/api/altar-presets")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validCreateRequest())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.name").value("Bộ gợi ý A"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void create_returns4001_whenNameBlank() throws Exception {
        AltarPresetRequest request = validCreateRequest();
        request.setName(" ");

        mockMvc.perform(post("/api/altar-presets")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(4001));
    }

    @Test
    @WithMockUser(roles = "CUSTOMER")
    void delete_returns403_forInsufficientRole() throws Exception {
        mockMvc.perform(delete("/api/altar-presets/1").with(csrf()))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value(4030));
    }

    @Test
    @WithMockUser(roles = "SUPERADMIN")
    void delete_returns200_forSuperadmin() throws Exception {
        mockMvc.perform(delete("/api/altar-presets/1").with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1000));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void update_returns200_forAdmin() throws Exception {
        when(altarPresetService.update(anyLong(), any())).thenReturn(
                AltarPresetResponse.builder().id(1L).name("Bộ gợi ý A").items(List.of()).build());

        mockMvc.perform(put("/api/altar-presets/1")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validCreateRequest())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(1));
    }
}

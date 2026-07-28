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
import vn.springboot.dto.request.altar.AltarPlacementRequest;
import vn.springboot.dto.response.altar.AltarPlacementResponse;
import vn.springboot.security.CustomAccessDeniedHandler;
import vn.springboot.security.JwtAuthenticationEntryPoint;
import vn.springboot.security.SecurityConfig;
import vn.springboot.security.jwt.JwtAuthenticationFilter;
import vn.springboot.security.jwt.JwtService;
import vn.springboot.service.AltarPlacementService;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AltarPlacementController.class)
@Import({SecurityConfig.class, JwtAuthenticationFilter.class,
        JwtAuthenticationEntryPoint.class, CustomAccessDeniedHandler.class})
class AltarPlacementControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AltarPlacementService altarPlacementService;

    // Collaborators the security filter chain needs.
    @MockitoBean
    private JwtService jwtService;
    @MockitoBean
    private UserDetailsService userDetailsService;

    private AltarPlacementRequest validRequest() {
        return AltarPlacementRequest.builder()
                .overlayImage("/files/overlay.png")
                .defaultX(0.5)
                .defaultY(0.8)
                .widthCm(30)
                .scaleAdjust(1.0)
                .flippable(true)
                .build();
    }

    @Test
    void get_isPublic_returns200() throws Exception {
        when(altarPlacementService.getByProductImage(1L, 10L))
                .thenReturn(AltarPlacementResponse.builder().id(5L).productImageId(10L).build());

        mockMvc.perform(get("/api/products/1/images/10/placement"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1000))
                .andExpect(jsonPath("$.data.productImageId").value(10));
    }

    @Test
    void upsert_returns401_whenUnauthenticated() throws Exception {
        mockMvc.perform(put("/api/products/1/images/10/placement")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest())))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(4010));
    }

    @Test
    @WithMockUser(roles = "CUSTOMER")
    void upsert_returns403_forInsufficientRole() throws Exception {
        mockMvc.perform(put("/api/products/1/images/10/placement")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest())))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value(4030));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void upsert_returns200_forAdmin() throws Exception {
        when(altarPlacementService.upsert(anyLong(), anyLong(), any()))
                .thenReturn(AltarPlacementResponse.builder().id(5L).productImageId(10L).build());

        mockMvc.perform(put("/api/products/1/images/10/placement")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(5));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void upsert_returns200_forDefaultXOutsideOldUnitRange() throws Exception {
        // x/y are no longer limited to [0,1] (strictly inside the tabletop) — an item may be
        // placed anywhere on the full backdrop image, which in surface-relative terms can be
        // outside [0,1]. 1.5 is still well within the new generous [-2,3] bound.
        when(altarPlacementService.upsert(anyLong(), anyLong(), any()))
                .thenReturn(AltarPlacementResponse.builder().id(5L).productImageId(10L).build());
        AltarPlacementRequest request = validRequest();
        request.setDefaultX(1.5);

        mockMvc.perform(put("/api/products/1/images/10/placement")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void upsert_returns4001_whenDefaultXOutOfRange() throws Exception {
        AltarPlacementRequest request = validRequest();
        request.setDefaultX(5.0);

        mockMvc.perform(put("/api/products/1/images/10/placement")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(4001));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void upsert_returns4001_whenDefaultYOutOfRange() throws Exception {
        AltarPlacementRequest request = validRequest();
        request.setDefaultY(-5.0);

        mockMvc.perform(put("/api/products/1/images/10/placement")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(4001));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void upsert_returns4001_whenWidthCmNotPositive() throws Exception {
        AltarPlacementRequest request = validRequest();
        request.setWidthCm(0);

        mockMvc.perform(put("/api/products/1/images/10/placement")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(4001));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void upsert_returns4001_whenScaleAdjustAboveMax() throws Exception {
        AltarPlacementRequest request = validRequest();
        request.setScaleAdjust(5.1);

        mockMvc.perform(put("/api/products/1/images/10/placement")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(4001));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void upsert_returns4001_whenScaleAdjustBelowMin() throws Exception {
        AltarPlacementRequest request = validRequest();
        request.setScaleAdjust(0.05);

        mockMvc.perform(put("/api/products/1/images/10/placement")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(4001));
    }

    @Test
    @WithMockUser(roles = "CUSTOMER")
    void delete_returns403_forInsufficientRole() throws Exception {
        mockMvc.perform(delete("/api/products/1/images/10/placement").with(csrf()))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value(4030));
    }

    @Test
    @WithMockUser(roles = "SUPERADMIN")
    void delete_returns200_forSuperadmin() throws Exception {
        mockMvc.perform(delete("/api/products/1/images/10/placement").with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1000));
    }
}

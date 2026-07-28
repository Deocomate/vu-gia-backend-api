package vn.springboot.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import vn.springboot.dto.response.altar.AltarCustomizerItemResponse;
import vn.springboot.dto.response.altar.AltarPlacementResponse;
import vn.springboot.security.CustomAccessDeniedHandler;
import vn.springboot.security.JwtAuthenticationEntryPoint;
import vn.springboot.security.SecurityConfig;
import vn.springboot.security.jwt.JwtAuthenticationFilter;
import vn.springboot.security.jwt.JwtService;
import vn.springboot.service.AltarCustomizerService;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AltarCustomizerController.class)
@Import({SecurityConfig.class, JwtAuthenticationFilter.class,
        JwtAuthenticationEntryPoint.class, CustomAccessDeniedHandler.class})
class AltarCustomizerControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AltarCustomizerService altarCustomizerService;

    // Collaborators the security filter chain needs.
    @MockitoBean
    private JwtService jwtService;
    @MockitoBean
    private UserDetailsService userDetailsService;

    @Test
    void items_isPublic_noFilters_returns200WithPlacementInline() throws Exception {
        when(altarCustomizerService.getItems(null, null)).thenReturn(List.of(
                AltarCustomizerItemResponse.builder()
                        .productId(1L).name("Bát hương").slug("bat-huong").price(500_000L)
                        .thumb("/files/thumb.jpg").groupId(9L).styleId(3L).renderOnAltar(true)
                        .placement(AltarPlacementResponse.builder()
                                .productImageId(10L).overlayImage("/files/overlay.png")
                                .defaultX(0.5).defaultY(0.8).widthCm(30).scaleAdjust(1.0).flippable(true)
                                .build())
                        .build()));

        mockMvc.perform(get("/api/altar-customizer/items"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1000))
                .andExpect(jsonPath("$.data[0].productId").value(1))
                .andExpect(jsonPath("$.data[0].renderOnAltar").value(true))
                .andExpect(jsonPath("$.data[0].placement.productImageId").value(10))
                .andExpect(jsonPath("$.data[0].placement.overlayImage").exists());
    }

    @Test
    void items_withFilters_passesThroughToService() throws Exception {
        when(altarCustomizerService.getItems(eq(9L), eq(3L))).thenReturn(List.of());

        mockMvc.perform(get("/api/altar-customizer/items?altarItemGroupId=9&altarStyleId=3"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray());

        verify(altarCustomizerService).getItems(9L, 3L);
    }

    @Test
    void items_placementNull_returnsNullPlacementField() throws Exception {
        when(altarCustomizerService.getItems(any(), any())).thenReturn(List.of(
                AltarCustomizerItemResponse.builder()
                        .productId(2L).name("Không có placement").slug("no-placement")
                        .price(100_000L).thumb("/files/thumb2.jpg").renderOnAltar(false).placement(null)
                        .build()));

        mockMvc.perform(get("/api/altar-customizer/items"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].placement").doesNotExist());
    }
}

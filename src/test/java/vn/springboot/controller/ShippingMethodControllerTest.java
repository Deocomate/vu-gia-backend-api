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
import vn.springboot.dto.request.shipping.ShippingMethodCreateRequest;
import vn.springboot.dto.response.PageResponse;
import vn.springboot.dto.response.shipping.ShippingMethodResponse;
import vn.springboot.security.CustomAccessDeniedHandler;
import vn.springboot.security.JwtAuthenticationEntryPoint;
import vn.springboot.security.SecurityConfig;
import vn.springboot.security.jwt.JwtAuthenticationFilter;
import vn.springboot.security.jwt.JwtService;
import vn.springboot.service.ShippingMethodService;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ShippingMethodController.class)
@Import({SecurityConfig.class, JwtAuthenticationFilter.class,
        JwtAuthenticationEntryPoint.class, CustomAccessDeniedHandler.class})
class ShippingMethodControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ShippingMethodService shippingMethodService;

    // Collaborators the security filter chain needs.
    @MockitoBean
    private JwtService jwtService;
    @MockitoBean
    private UserDetailsService userDetailsService;

    private PageResponse<ShippingMethodResponse> onePage() {
        return PageResponse.<ShippingMethodResponse>builder()
                .content(List.of(ShippingMethodResponse.builder()
                        .id(1L).name("Standard").fee(30_000L).isActive(true).build()))
                .pageNumber(1).pageSize(10).totalElements(1).totalPages(1).first(true).last(true)
                .build();
    }

    private ShippingMethodCreateRequest validCreateRequest() {
        return ShippingMethodCreateRequest.builder()
                .name("Standard")
                .fee(30_000L)
                .build();
    }

    @Test
    void search_isPublic_returns200() throws Exception {
        when(shippingMethodService.search(any())).thenReturn(onePage());

        mockMvc.perform(get("/api/shipping-methods"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1000))
                .andExpect(jsonPath("$.data.content[0].name").value("Standard"))
                .andExpect(jsonPath("$.data.totalElements").value(1))
                .andExpect(jsonPath("$.data.first").value(true))
                .andExpect(jsonPath("$.data.last").value(true));
    }

    @Test
    void getById_isPublic_returns200() throws Exception {
        when(shippingMethodService.getById(1L)).thenReturn(
                ShippingMethodResponse.builder().id(1L).name("Standard").fee(30_000L).build());

        mockMvc.perform(get("/api/shipping-methods/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1000))
                .andExpect(jsonPath("$.data.name").value("Standard"));
    }

    @Test
    void create_returns401_whenUnauthenticated() throws Exception {
        mockMvc.perform(post("/api/shipping-methods")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validCreateRequest())))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(4010));
    }

    @Test
    @WithMockUser(roles = "CUSTOMER")
    void create_returns403_forInsufficientRole() throws Exception {
        mockMvc.perform(post("/api/shipping-methods")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validCreateRequest())))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value(4030));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void create_returns200_forAdmin() throws Exception {
        when(shippingMethodService.create(any()))
                .thenReturn(ShippingMethodResponse.builder().id(1L).name("Standard").fee(30_000L).build());

        mockMvc.perform(post("/api/shipping-methods")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validCreateRequest())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1000))
                .andExpect(jsonPath("$.data.name").value("Standard"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void create_returns4001_whenNameMissing() throws Exception {
        mockMvc.perform(post("/api/shipping-methods")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                ShippingMethodCreateRequest.builder().fee(30_000L).build())))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(4001));
    }

    @Test
    @WithMockUser(roles = "CUSTOMER")
    void delete_returns403_forInsufficientRole() throws Exception {
        mockMvc.perform(delete("/api/shipping-methods/1").with(csrf()))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value(4030));
    }

    @Test
    @WithMockUser(roles = "SUPERADMIN")
    void delete_returns200_forSuperadmin() throws Exception {
        mockMvc.perform(delete("/api/shipping-methods/1").with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1000));
    }
}

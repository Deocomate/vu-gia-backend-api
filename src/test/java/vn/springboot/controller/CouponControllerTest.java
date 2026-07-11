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
import vn.springboot.dto.request.coupon.CouponCreateRequest;
import vn.springboot.dto.request.coupon.CouponValidateRequest;
import vn.springboot.dto.response.PageResponse;
import vn.springboot.dto.response.coupon.CouponResponse;
import vn.springboot.dto.response.coupon.CouponValidationResponse;
import vn.springboot.entity.enums.DiscountType;
import vn.springboot.security.CustomAccessDeniedHandler;
import vn.springboot.security.JwtAuthenticationEntryPoint;
import vn.springboot.security.SecurityConfig;
import vn.springboot.security.jwt.JwtAuthenticationFilter;
import vn.springboot.security.jwt.JwtService;
import vn.springboot.service.CouponService;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CouponController.class)
@Import({SecurityConfig.class, JwtAuthenticationFilter.class,
        JwtAuthenticationEntryPoint.class, CustomAccessDeniedHandler.class})
class CouponControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private CouponService couponService;
    @MockitoBean
    private JwtService jwtService;
    @MockitoBean
    private UserDetailsService userDetailsService;

    @Test
    void list_requiresAuth_returns401WhenAnonymous() throws Exception {
        // Coupon listing is admin-only (codes must not leak).
        mockMvc.perform(get("/api/coupons"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(4010));
    }

    @Test
    @WithMockUser(roles = "CUSTOMER")
    void list_forbiddenForNonStaff() throws Exception {
        mockMvc.perform(get("/api/coupons"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value(4030));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void list_okForAdmin() throws Exception {
        when(couponService.search(any())).thenReturn(PageResponse.<CouponResponse>builder()
                .content(List.of(CouponResponse.builder().id(1L).code("SALE10").build()))
                .pageNumber(1).pageSize(10).totalElements(1).totalPages(1).first(true).last(true).build());

        mockMvc.perform(get("/api/coupons"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1000))
                .andExpect(jsonPath("$.data.content[0].code").value("SALE10"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void create_okForAdmin() throws Exception {
        when(couponService.create(any()))
                .thenReturn(CouponResponse.builder().id(1L).code("SALE10").discountType(DiscountType.PERCENT).build());
        CouponCreateRequest req = CouponCreateRequest.builder()
                .code("SALE10").discountType(DiscountType.PERCENT).discountValue(10L).build();

        mockMvc.perform(post("/api/coupons").with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1000))
                .andExpect(jsonPath("$.data.code").value("SALE10"));
    }

    @Test
    void validate_isPublic() throws Exception {
        when(couponService.validate(any())).thenReturn(CouponValidationResponse.builder()
                .valid(true).code("SALE10").discountType(DiscountType.PERCENT).discountAmount(30_000L).build());
        CouponValidateRequest req = new CouponValidateRequest("SALE10", 300_000L);

        mockMvc.perform(post("/api/coupons/validate").with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1000))
                .andExpect(jsonPath("$.data.valid").value(true))
                .andExpect(jsonPath("$.data.discountAmount").value(30000));
    }
}

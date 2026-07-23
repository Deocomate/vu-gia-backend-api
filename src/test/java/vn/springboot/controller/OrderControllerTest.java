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
import vn.springboot.dto.request.order.OrderItemRequest;
import vn.springboot.dto.request.order.OrderPlaceRequest;
import vn.springboot.dto.request.order.OrderStatusUpdateRequest;
import vn.springboot.dto.response.PageResponse;
import vn.springboot.dto.response.order.OrderResponse;
import vn.springboot.dto.response.order.PaymentInfoResponse;
import vn.springboot.entity.enums.OrderStatus;
import vn.springboot.entity.enums.PaymentMethod;
import vn.springboot.security.CustomAccessDeniedHandler;
import vn.springboot.security.JwtAuthenticationEntryPoint;
import vn.springboot.security.SecurityConfig;
import vn.springboot.security.jwt.JwtAuthenticationFilter;
import vn.springboot.security.jwt.JwtService;
import vn.springboot.service.OrderService;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(OrderController.class)
@Import({SecurityConfig.class, JwtAuthenticationFilter.class,
        JwtAuthenticationEntryPoint.class, CustomAccessDeniedHandler.class})
class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private OrderService orderService;

    // Collaborators the security filter chain needs.
    @MockitoBean
    private JwtService jwtService;
    @MockitoBean
    private UserDetailsService userDetailsService;

    private OrderPlaceRequest validPlaceRequest() {
        return OrderPlaceRequest.builder()
                .idempotencyKey("key-1")
                .items(List.of(OrderItemRequest.builder().productId(10L).quantity(2).build()))
                .receiverName("Alice").receiverPhone("0900123456").receiverAddress("Hà Nội")
                .build();
    }

    @Test
    void place_returns401_whenUnauthenticated() throws Exception {
        mockMvc.perform(post("/api/orders")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validPlaceRequest())))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(4010));
    }

    @Test
    @WithMockUser
    void place_returns200_forValidBody() throws Exception {
        when(orderService.placeOrder(any()))
                .thenReturn(OrderResponse.builder().id(1L).orderCode("ODABC").totalAmount(200).build());

        mockMvc.perform(post("/api/orders")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validPlaceRequest())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1000))
                .andExpect(jsonPath("$.data.orderCode").value("ODABC"))
                .andExpect(jsonPath("$.data.totalAmount").value(200));
    }

    @Test
    @WithMockUser
    void place_onl_returnsQrPaymentInResponse() throws Exception {
        when(orderService.placeOrder(any())).thenReturn(OrderResponse.builder()
                .id(1L).orderCode("ODX").paymentMethod(PaymentMethod.ONL)
                .payment(PaymentInfoResponse.builder()
                        .qrImageUrl("https://vietqr.app/img?amount=200&des=ODX")
                        .amount(200).transferContent("ODX").build())
                .build());

        mockMvc.perform(post("/api/orders")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validPlaceRequest())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1000))
                .andExpect(jsonPath("$.data.paymentMethod").value("ONL"))
                .andExpect(jsonPath("$.data.payment.qrImageUrl").value("https://vietqr.app/img?amount=200&des=ODX"))
                .andExpect(jsonPath("$.data.payment.transferContent").value("ODX"));
    }

    @Test
    @WithMockUser
    void place_returns4001_whenItemsMissing() throws Exception {
        mockMvc.perform(post("/api/orders")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                OrderPlaceRequest.builder().idempotencyKey("k")
                                        .receiverName("A").receiverPhone("1").receiverAddress("x").build())))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(4001));
    }

    @Test
    @WithMockUser(roles = "CUSTOMER")
    void searchAll_returns403_forNonStaff() throws Exception {
        mockMvc.perform(get("/api/orders/admin"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value(4030));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void searchAll_returns200_forAdmin() throws Exception {
        when(orderService.searchOrders(any())).thenReturn(PageResponse.<OrderResponse>builder()
                .content(List.of(OrderResponse.builder().id(1L).orderCode("ODX").build()))
                .pageNumber(1).pageSize(10).totalElements(1).totalPages(1).first(true).last(true)
                .build());

        mockMvc.perform(get("/api/orders/admin"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1000))
                .andExpect(jsonPath("$.data.content[0].orderCode").value("ODX"))
                .andExpect(jsonPath("$.data.totalElements").value(1));
    }

    @Test
    @WithMockUser(roles = "CUSTOMER")
    void updateStatus_returns403_forNonStaff() throws Exception {
        mockMvc.perform(patch("/api/orders/1/status")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                OrderStatusUpdateRequest.builder().status(OrderStatus.PROCESSING).build())))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value(4030));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void updateStatus_returns200_forAdmin() throws Exception {
        when(orderService.updateStatus(eq(1L), any()))
                .thenReturn(OrderResponse.builder().id(1L).status(OrderStatus.PROCESSING).build());

        mockMvc.perform(patch("/api/orders/1/status")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                OrderStatusUpdateRequest.builder().status(OrderStatus.PROCESSING).build())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1000))
                .andExpect(jsonPath("$.data.status").value("PROCESSING"));
    }

    @Test
    @WithMockUser(roles = "CUSTOMER")
    void cancel_returns200_forAuthenticatedCustomer() throws Exception {
        when(orderService.cancel(1L))
                .thenReturn(OrderResponse.builder().id(1L).status(OrderStatus.CANCELLED).build());

        mockMvc.perform(post("/api/orders/1/cancel").with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1000))
                .andExpect(jsonPath("$.data.status").value("CANCELLED"));
    }

    @Test
    void cancel_returns401_whenUnauthenticated() throws Exception {
        mockMvc.perform(post("/api/orders/1/cancel").with(csrf()))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(4010));
    }
}

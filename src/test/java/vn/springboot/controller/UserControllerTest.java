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
import vn.springboot.dto.request.user.ResetPasswordRequest;
import vn.springboot.dto.request.user.UserCreateRequest;
import vn.springboot.dto.request.user.UserRoleUpdateRequest;
import vn.springboot.dto.response.PageResponse;
import vn.springboot.dto.response.user.UserResponse;
import vn.springboot.entity.enums.Role;
import vn.springboot.security.CustomAccessDeniedHandler;
import vn.springboot.security.JwtAuthenticationEntryPoint;
import vn.springboot.security.SecurityConfig;
import vn.springboot.security.jwt.JwtAuthenticationFilter;
import vn.springboot.security.jwt.JwtService;
import vn.springboot.service.UserService;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
@Import({SecurityConfig.class, JwtAuthenticationFilter.class,
        JwtAuthenticationEntryPoint.class, CustomAccessDeniedHandler.class})
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private UserService userService;

    // Collaborators the security filter chain needs.
    @MockitoBean
    private JwtService jwtService;
    @MockitoBean
    private UserDetailsService userDetailsService;

    private PageResponse<UserResponse> onePage() {
        return PageResponse.<UserResponse>builder()
                .content(List.of(UserResponse.builder().id(1L).username("john").role(Role.CUSTOMER).build()))
                .pageNumber(0).pageSize(10).totalElements(1).totalPages(1).first(true).last(true)
                .build();
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void search_returns200_forAdmin() throws Exception {
        when(userService.search(any())).thenReturn(onePage());

        mockMvc.perform(get("/api/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1000))
                .andExpect(jsonPath("$.data.content[0].username").value("john"))
                .andExpect(jsonPath("$.data.totalElements").value(1))
                .andExpect(jsonPath("$.data.first").value(true))
                .andExpect(jsonPath("$.data.last").value(true));
    }

    @Test
    void search_returns401_whenUnauthenticated() throws Exception {
        mockMvc.perform(get("/api/users"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(4010));
    }

    @Test
    @WithMockUser(roles = "CUSTOMER")
    void search_returns403_forInsufficientRole() throws Exception {
        mockMvc.perform(get("/api/users"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value(4030));
    }

    // ---------- create (SUPERADMIN-only) ----------

    @Test
    @WithMockUser(roles = "SUPERADMIN")
    void create_ok_forSuperadmin() throws Exception {
        when(userService.create(any()))
                .thenReturn(UserResponse.builder().id(2L).username("boss").role(Role.ADMIN).build());
        UserCreateRequest req = UserCreateRequest.builder()
                .username("boss").email("boss@example.com").password("secret123").role(Role.ADMIN).build();

        mockMvc.perform(post("/api/users").with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1000))
                .andExpect(jsonPath("$.data.role").value("ADMIN"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void create_forbidden_forAdmin() throws Exception {
        UserCreateRequest req = UserCreateRequest.builder()
                .username("boss").email("boss@example.com").password("secret123").role(Role.ADMIN).build();

        mockMvc.perform(post("/api/users").with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value(4030));
    }

    @Test
    @WithMockUser(roles = "SUPERADMIN")
    void changeRole_ok_forSuperadmin() throws Exception {
        when(userService.changeRole(any(), any()))
                .thenReturn(UserResponse.builder().id(1L).username("john").role(Role.ADMIN).build());

        mockMvc.perform(patch("/api/users/1/role").with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new UserRoleUpdateRequest(Role.ADMIN))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.role").value("ADMIN"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void resetPassword_ok_forStaff() throws Exception {
        mockMvc.perform(patch("/api/users/1/password").with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new ResetPasswordRequest("newpass123"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1000));
    }
}

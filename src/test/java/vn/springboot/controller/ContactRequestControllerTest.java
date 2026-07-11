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
import vn.springboot.dto.request.contact.ContactRequestCreateRequest;
import vn.springboot.dto.response.PageResponse;
import vn.springboot.dto.response.contact.ContactRequestResponse;
import vn.springboot.entity.enums.ContactStatus;
import vn.springboot.security.CustomAccessDeniedHandler;
import vn.springboot.security.JwtAuthenticationEntryPoint;
import vn.springboot.security.SecurityConfig;
import vn.springboot.security.jwt.JwtAuthenticationFilter;
import vn.springboot.security.jwt.JwtService;
import vn.springboot.service.ContactRequestService;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ContactRequestController.class)
@Import({SecurityConfig.class, JwtAuthenticationFilter.class,
        JwtAuthenticationEntryPoint.class, CustomAccessDeniedHandler.class})
class ContactRequestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ContactRequestService contactRequestService;

    // Collaborators the security filter chain needs.
    @MockitoBean
    private JwtService jwtService;
    @MockitoBean
    private UserDetailsService userDetailsService;

    private PageResponse<ContactRequestResponse> onePage() {
        return PageResponse.<ContactRequestResponse>builder()
                .content(List.of(ContactRequestResponse.builder()
                        .id(1L).name("Alice").content("hello").status(ContactStatus.NEW).build()))
                .pageNumber(1).pageSize(10).totalElements(1).totalPages(1).first(true).last(true)
                .build();
    }

    private ContactRequestCreateRequest validCreateRequest() {
        return ContactRequestCreateRequest.builder()
                .name("Alice")
                .email("alice@example.com")
                .content("I have a question")
                .build();
    }

    @Test
    void create_isPublic_returns200() throws Exception {
        when(contactRequestService.create(any()))
                .thenReturn(ContactRequestResponse.builder()
                        .id(1L).name("Alice").content("I have a question").status(ContactStatus.NEW).build());

        mockMvc.perform(post("/api/contact-requests")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validCreateRequest())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1000))
                .andExpect(jsonPath("$.data.name").value("Alice"))
                .andExpect(jsonPath("$.data.status").value("NEW"));
    }

    @Test
    void create_returns4001_forBlankName() throws Exception {
        mockMvc.perform(post("/api/contact-requests")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                ContactRequestCreateRequest.builder().name("").content("hi").build())))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(4001));
    }

    @Test
    void search_returns401_whenUnauthenticated() throws Exception {
        mockMvc.perform(get("/api/contact-requests"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(4010));
    }

    @Test
    @WithMockUser(roles = "CUSTOMER")
    void search_returns403_forInsufficientRole() throws Exception {
        mockMvc.perform(get("/api/contact-requests"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value(4030));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void search_returns200_forAdmin() throws Exception {
        when(contactRequestService.search(any())).thenReturn(onePage());

        mockMvc.perform(get("/api/contact-requests"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1000))
                .andExpect(jsonPath("$.data.content[0].name").value("Alice"))
                .andExpect(jsonPath("$.data.totalElements").value(1))
                .andExpect(jsonPath("$.data.first").value(true))
                .andExpect(jsonPath("$.data.last").value(true));
    }
}

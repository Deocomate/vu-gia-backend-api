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
import vn.springboot.dto.request.newsletter.NewsletterSubscribeRequest;
import vn.springboot.dto.response.PageResponse;
import vn.springboot.dto.response.newsletter.NewsletterSubscriberResponse;
import vn.springboot.security.CustomAccessDeniedHandler;
import vn.springboot.security.JwtAuthenticationEntryPoint;
import vn.springboot.security.SecurityConfig;
import vn.springboot.security.jwt.JwtAuthenticationFilter;
import vn.springboot.security.jwt.JwtService;
import vn.springboot.service.NewsletterSubscriberService;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(NewsletterSubscriberController.class)
@Import({SecurityConfig.class, JwtAuthenticationFilter.class,
        JwtAuthenticationEntryPoint.class, CustomAccessDeniedHandler.class})
class NewsletterSubscriberControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private NewsletterSubscriberService subscriberService;

    // Collaborators the security filter chain needs.
    @MockitoBean
    private JwtService jwtService;
    @MockitoBean
    private UserDetailsService userDetailsService;

    private PageResponse<NewsletterSubscriberResponse> onePage() {
        return PageResponse.<NewsletterSubscriberResponse>builder()
                .content(List.of(NewsletterSubscriberResponse.builder().id(1L).email("a@b.com").isActive(true).build()))
                .pageNumber(1).pageSize(10).totalElements(1).totalPages(1).first(true).last(true)
                .build();
    }

    private NewsletterSubscribeRequest validSubscribeRequest() {
        return NewsletterSubscribeRequest.builder().email("new@b.com").build();
    }

    @Test
    void subscribe_isPublic_returns200() throws Exception {
        when(subscriberService.subscribe(any()))
                .thenReturn(NewsletterSubscriberResponse.builder().id(1L).email("new@b.com").isActive(true).build());

        mockMvc.perform(post("/api/newsletter-subscribers")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validSubscribeRequest())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1000))
                .andExpect(jsonPath("$.data.email").value("new@b.com"));
    }

    @Test
    void subscribe_returns4001_forInvalidEmail() throws Exception {
        mockMvc.perform(post("/api/newsletter-subscribers")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                NewsletterSubscribeRequest.builder().email("not-an-email").build())))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(4001));
    }

    @Test
    void search_returns401_whenUnauthenticated() throws Exception {
        mockMvc.perform(get("/api/newsletter-subscribers"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(4010));
    }

    @Test
    @WithMockUser(roles = "CUSTOMER")
    void search_returns403_forInsufficientRole() throws Exception {
        mockMvc.perform(get("/api/newsletter-subscribers"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value(4030));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void search_returns200_forAdmin() throws Exception {
        when(subscriberService.search(any())).thenReturn(onePage());

        mockMvc.perform(get("/api/newsletter-subscribers"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1000))
                .andExpect(jsonPath("$.data.content[0].email").value("a@b.com"))
                .andExpect(jsonPath("$.data.totalElements").value(1))
                .andExpect(jsonPath("$.data.first").value(true))
                .andExpect(jsonPath("$.data.last").value(true));
    }
}

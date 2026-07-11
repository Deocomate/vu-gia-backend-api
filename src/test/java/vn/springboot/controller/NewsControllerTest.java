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
import vn.springboot.dto.request.news.NewsCreateRequest;
import vn.springboot.dto.request.news.NewsStatusUpdateRequest;
import vn.springboot.dto.response.PageResponse;
import vn.springboot.dto.response.news.NewsResponse;
import vn.springboot.entity.enums.ContentStatus;
import vn.springboot.security.CustomAccessDeniedHandler;
import vn.springboot.security.JwtAuthenticationEntryPoint;
import vn.springboot.security.SecurityConfig;
import vn.springboot.security.jwt.JwtAuthenticationFilter;
import vn.springboot.security.jwt.JwtService;
import vn.springboot.service.NewsService;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(NewsController.class)
@Import({SecurityConfig.class, JwtAuthenticationFilter.class,
        JwtAuthenticationEntryPoint.class, CustomAccessDeniedHandler.class})
class NewsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private NewsService newsService;

    // Collaborators the security filter chain needs.
    @MockitoBean
    private JwtService jwtService;
    @MockitoBean
    private UserDetailsService userDetailsService;

    private PageResponse<NewsResponse> onePage() {
        return PageResponse.<NewsResponse>builder()
                .content(List.of(NewsResponse.builder().id(1L).title("Hello World").slug("hello-world").build()))
                .pageNumber(1).pageSize(10).totalElements(1).totalPages(1).first(true).last(true)
                .build();
    }

    private NewsCreateRequest validCreateRequest() {
        return NewsCreateRequest.builder()
                .title("Hello World")
                .thumb("http://img/x.png")
                .shortContent("short")
                .des("{}")
                .newsCategoryId(5L)
                .build();
    }

    @Test
    void search_isPublic_returns200() throws Exception {
        when(newsService.search(any())).thenReturn(onePage());

        mockMvc.perform(get("/api/news"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1000))
                .andExpect(jsonPath("$.data.content[0].title").value("Hello World"))
                .andExpect(jsonPath("$.data.totalElements").value(1))
                .andExpect(jsonPath("$.data.first").value(true))
                .andExpect(jsonPath("$.data.last").value(true));
    }

    @Test
    void create_returns401_whenUnauthenticated() throws Exception {
        mockMvc.perform(post("/api/news")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validCreateRequest())))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(4010));
    }

    @Test
    @WithMockUser(roles = "CUSTOMER")
    void create_returns403_forInsufficientRole() throws Exception {
        mockMvc.perform(post("/api/news")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validCreateRequest())))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value(4030));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void create_returns200_forAdmin() throws Exception {
        when(newsService.create(any()))
                .thenReturn(NewsResponse.builder().id(1L).title("Hello World").slug("hello-world").build());

        mockMvc.perform(post("/api/news")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validCreateRequest())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1000))
                .andExpect(jsonPath("$.data.title").value("Hello World"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void create_returns4001_whenBodyInvalid() throws Exception {
        NewsCreateRequest invalid = validCreateRequest();
        invalid.setTitle(null);

        mockMvc.perform(post("/api/news")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalid)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(4001));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void updateStatus_returns200_forAdmin() throws Exception {
        when(newsService.updateStatus(anyLong(), any()))
                .thenReturn(NewsResponse.builder().id(1L).title("Hello World").status(ContentStatus.PUBLISHED).build());

        NewsStatusUpdateRequest body = NewsStatusUpdateRequest.builder()
                .status(ContentStatus.PUBLISHED)
                .build();

        mockMvc.perform(patch("/api/news/1/status")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1000))
                .andExpect(jsonPath("$.data.status").value("PUBLISHED"));
    }
}

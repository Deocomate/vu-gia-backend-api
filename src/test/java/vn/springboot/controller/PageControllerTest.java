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
import vn.springboot.dto.request.page.PageCreateRequest;
import vn.springboot.dto.response.PageResponse;
import vn.springboot.dto.response.page.PageDetailResponse;
import vn.springboot.entity.enums.ContentStatus;
import vn.springboot.security.CustomAccessDeniedHandler;
import vn.springboot.security.JwtAuthenticationEntryPoint;
import vn.springboot.security.SecurityConfig;
import vn.springboot.security.jwt.JwtAuthenticationFilter;
import vn.springboot.security.jwt.JwtService;
import vn.springboot.service.PageService;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PageController.class)
@Import({SecurityConfig.class, JwtAuthenticationFilter.class,
        JwtAuthenticationEntryPoint.class, CustomAccessDeniedHandler.class})
class PageControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private PageService pageService;

    // Collaborators the security filter chain needs.
    @MockitoBean
    private JwtService jwtService;
    @MockitoBean
    private UserDetailsService userDetailsService;

    private PageResponse<PageDetailResponse> onePage() {
        return PageResponse.<PageDetailResponse>builder()
                .content(List.of(PageDetailResponse.builder()
                        .id(1L).key("about-us").title("About us").status(ContentStatus.DRAFT).build()))
                .pageNumber(1).pageSize(10).totalElements(1).totalPages(1).first(true).last(true)
                .build();
    }

    private PageCreateRequest validCreateRequest() {
        return PageCreateRequest.builder().key("about-us").title("About us").build();
    }

    @Test
    void search_isPublic_returns200() throws Exception {
        when(pageService.search(any())).thenReturn(onePage());

        mockMvc.perform(get("/api/pages"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1000))
                .andExpect(jsonPath("$.data.content[0].key").value("about-us"))
                .andExpect(jsonPath("$.data.totalElements").value(1))
                .andExpect(jsonPath("$.data.first").value(true))
                .andExpect(jsonPath("$.data.last").value(true));
    }

    @Test
    void getByKey_isPublic_returns200() throws Exception {
        when(pageService.getByKey(any()))
                .thenReturn(PageDetailResponse.builder()
                        .id(1L).key("about-us").title("About us").status(ContentStatus.PUBLISHED).build());

        mockMvc.perform(get("/api/pages/key/about-us"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1000))
                .andExpect(jsonPath("$.data.key").value("about-us"))
                .andExpect(jsonPath("$.data.status").value("PUBLISHED"));
    }

    @Test
    void create_returns401_whenUnauthenticated() throws Exception {
        mockMvc.perform(post("/api/pages")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validCreateRequest())))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(4010));
    }

    @Test
    @WithMockUser(roles = "CUSTOMER")
    void create_returns403_forInsufficientRole() throws Exception {
        mockMvc.perform(post("/api/pages")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validCreateRequest())))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value(4030));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void create_returns200_forAdmin() throws Exception {
        when(pageService.create(any()))
                .thenReturn(PageDetailResponse.builder()
                        .id(1L).key("about-us").title("About us").status(ContentStatus.DRAFT).build());

        mockMvc.perform(post("/api/pages")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validCreateRequest())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1000))
                .andExpect(jsonPath("$.data.key").value("about-us"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void create_returns4001_forBlankKey() throws Exception {
        mockMvc.perform(post("/api/pages")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                PageCreateRequest.builder().key("").title("x").build())))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(4001));
    }
}

package vn.springboot.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import vn.springboot.security.CustomAccessDeniedHandler;
import vn.springboot.security.JwtAuthenticationEntryPoint;
import vn.springboot.security.SecurityConfig;
import vn.springboot.security.jwt.JwtAuthenticationFilter;
import vn.springboot.security.jwt.JwtService;
import vn.springboot.service.FileStorageService;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// StorageProperties.class defaults (public-url=http://localhost:8080, url-prefix=/files) are
// what the assertions below expect; the @StorageUrl serializer resolves UploadResponse.url to
// an absolute URL using those. The bean itself is registered by WebStorageConfig's
// @EnableConfigurationProperties (always pulled into this slice as a WebMvcConfigurer).
@WebMvcTest(MediaController.class)
@Import({SecurityConfig.class, JwtAuthenticationFilter.class,
        JwtAuthenticationEntryPoint.class, CustomAccessDeniedHandler.class})
class MediaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private FileStorageService fileStorageService;
    @MockitoBean
    private JwtService jwtService;
    @MockitoBean
    private UserDetailsService userDetailsService;

    private MockMultipartFile image(String name) {
        return new MockMultipartFile("files", name, "image/jpeg", new byte[]{1, 2, 3});
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void uploadMultiple_returnsUrls_forAdmin() throws Exception {
        // Storage returns relative paths; the @StorageUrl serializer must resolve
        // them to absolute URLs in the JSON response.
        when(fileStorageService.uploadImages(any(), anyString()))
                .thenReturn(List.of("/files/products/a.jpg", "/files/products/b.jpg"));

        mockMvc.perform(multipart("/api/media/upload-multiple")
                        .file(image("a.jpg")).file(image("b.jpg"))
                        .param("folder", "products").with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1000))
                .andExpect(jsonPath("$.data.length()").value(2))
                .andExpect(jsonPath("$.data[0].url").value("http://localhost:8080/files/products/a.jpg"))
                .andExpect(jsonPath("$.data[1].url").value("http://localhost:8080/files/products/b.jpg"));
    }

    @Test
    void uploadMultiple_requiresAuth() throws Exception {
        mockMvc.perform(multipart("/api/media/upload-multiple").file(image("a.jpg")).with(csrf()))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(4010));
    }

    @Test
    @WithMockUser(roles = "CUSTOMER")
    void uploadMultiple_forbiddenForNonStaff() throws Exception {
        mockMvc.perform(multipart("/api/media/upload-multiple").file(image("a.jpg")).with(csrf()))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value(4030));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void uploadSingle_returnsUrl_forAdmin() throws Exception {
        when(fileStorageService.uploadImage(any(), anyString())).thenReturn("/files/misc/x.jpg");

        mockMvc.perform(multipart("/api/media/upload")
                        .file(new MockMultipartFile("file", "x.jpg", "image/jpeg", new byte[]{1}))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1000))
                .andExpect(jsonPath("$.data.url").value("http://localhost:8080/files/misc/x.jpg"));
    }
}

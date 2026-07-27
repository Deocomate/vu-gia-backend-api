package vn.springboot;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

// app.jwt.secret / app.security.cors.allowed-origins have no default in application.yaml
// (Phase 1 security hardening — fail-closed, required env vars in real deployments), so a
// full-context test must supply test-only values for the context to boot.
@SpringBootTest
@TestPropertySource(properties = {
        "app.jwt.secret=YcgS5HZmw4ViK7gfWyIpDHORS5Mytm96TnSWhtH7zGh5efncqiVoqOXgFLybSnbH+mDUMGtZYF8FIHMkmA4L+g==",
        "app.security.cors.allowed-origins=http://localhost:5173"
})
class ApplicationTests {

    @Test
    void contextLoads() {
    }

}

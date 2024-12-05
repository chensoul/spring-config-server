package com.chensoul.configserver;

import java.util.Map;
import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit.jupiter.SpringExtension;

/**
 * TODO Comment
 *
 * @author <a href="mailto:ichensoul@gmail.com">chensoul</a>
 * @since TODO
 */
@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = ConfigServerApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {
                "spring.config.import: file:samples/config-repo.yml"
        })
public class ConfigServerApplicationTest {
    @Value("${local.server.port}")
    private int port = 0;

    @Test
    public void configurationAvailable() {
        ResponseEntity<Map> entity = new TestRestTemplate("user", "password")
                .getForEntity("http://localhost:" + port + "/app/cloud", Map.class);
        assertEquals(HttpStatus.OK, entity.getStatusCode());
    }

    @Test
    public void envPostAvailable() {
        @SuppressWarnings("rawtypes")
        ResponseEntity<Map> entity = new TestRestTemplate("user", "password")
                .getForEntity("http://localhost:" + port + "/admin/env", Map.class);
        assertEquals(HttpStatus.OK, entity.getStatusCode());
    }

}
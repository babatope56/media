package com.media.media;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = {
        "spring.autoconfigure.exclude=org.springframework.ai.model.anthropic.autoconfigure.AnthropicChatAutoConfiguration"
})
class MediaApplicationTests {

    @Test
    void contextLoads() {
    }

}

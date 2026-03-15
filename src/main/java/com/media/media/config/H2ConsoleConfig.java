package com.media.media.config;

import org.h2.server.web.JakartaWebServlet;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnProperty(prefix = "app.security", name = "enable-h2-console", havingValue = "true")
public class H2ConsoleConfig {

    @Bean
    public ServletRegistrationBean<JakartaWebServlet> h2ConsoleServletRegistration() {
        ServletRegistrationBean<JakartaWebServlet> registration =
                new ServletRegistrationBean<>(new JakartaWebServlet(), "/h2-console/*");

        registration.addInitParameter("-webAllowOthers", "false");
        registration.addInitParameter("trace", "false");

        return registration;
    }
}

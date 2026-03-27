package com.eventcart.orderservice;

import com.eventcart.orderservice.cache.RedisOrderCacheService;
import com.eventcart.orderservice.messaging.OrderEventPublisher;
import jakarta.servlet.Filter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.setup.ConfigurableMockMvcBuilder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.Optional;

import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@Import(OrderControllerPostgresIntegrationTest.MockConfig.class)
@ActiveProfiles("postgres")
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:h2:mem:orderdb;MODE=PostgreSQL;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.jpa.hibernate.ddl-auto=update",
        "spring.kafka.listener.auto-startup=false"
})
class OrderControllerPostgresIntegrationTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired(required = false)
    private Filter[] filters;

    private MockMvc mockMvc;

    @Autowired
    private OrderEventPublisher orderEventPublisher;

    @Autowired
    private RedisOrderCacheService redisOrderCacheService;

    @BeforeEach
    void setUp() {
        ConfigurableMockMvcBuilder<?> builder = MockMvcBuilders.webAppContextSetup(webApplicationContext);
        if (filters != null && filters.length > 0) {
            builder.addFilters(filters);
        }
        this.mockMvc = builder.build();
    }

    @Test
    void postThenGet_returnsCreatedOrderWithStableContract() throws Exception {
        when(redisOrderCacheService.getOrderStatus(anyString())).thenReturn(Optional.empty());

        MvcResult postResult = mockMvc.perform(post("/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"items\":[],\"totalAmount\":120.0}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orderId", notNullValue()))
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andReturn();

        String postBody = postResult.getResponse().getContentAsString();
        String orderId = postBody.replaceAll(".*\"orderId\":\"([^\"]+)\".*", "$1");

        mockMvc.perform(get("/orders/{orderId}", orderId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orderId").value(orderId))
                .andExpect(jsonPath("$.status").value("PENDING"));
    }

    @TestConfiguration
    static class MockConfig {
        @Bean
        @Primary
        OrderEventPublisher orderEventPublisher() {
            return Mockito.mock(OrderEventPublisher.class);
        }

        @Bean
        @Primary
        RedisOrderCacheService redisOrderCacheService() {
            return Mockito.mock(RedisOrderCacheService.class);
        }
    }
}

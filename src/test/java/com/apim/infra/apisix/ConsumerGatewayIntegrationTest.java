package com.apim.infra.apisix;

import com.apim.domain.ApisixException;
import com.apim.domain.gateway.ConsumerGateway;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest(classes = ConsumerGatewayIntegrationTest.TestConfig.class)
class ConsumerGatewayIntegrationTest {

    @Configuration
    @Import({ApisixWebClientConfig.class, ApisixConsumerGateway.class})
    @ImportAutoConfiguration(JacksonAutoConfiguration.class)
    static class TestConfig {
    }

    @org.junit.jupiter.api.extension.RegisterExtension
    static WireMockExtension wireMock = WireMockExtension.newInstance()
            .options(WireMockConfiguration.wireMockConfig().dynamicPort())
            .build();

    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
        registry.add("apisix.base-url", wireMock::baseUrl);
        registry.add("apisix.api-key", () -> "test-api-key");
    }

    @Autowired
    private ConsumerGateway consumerGateway;

    @BeforeEach
    void setUp() {
        wireMock.resetAll();
    }

    @Test
    void listConsumers_returnsConsumers() {
        wireMock.stubFor(get(urlEqualTo("/apisix/admin/consumers"))
                .willReturn(okJson("""
                        {
                          "list": [
                            {
                              "key": "/apisix/consumers/jack",
                              "value": {
                                "username": "jack",
                                "desc": "Test consumer",
                                "plugins": {
                                  "key-auth": {"key": "auth-jack"}
                                }
                              }
                            }
                          ],
                          "total": 1
                        }
                        """)));

        var result = consumerGateway.list().block();

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().id()).isEqualTo("/apisix/consumers/jack");
        assertThat(result.getFirst().value().username()).isEqualTo("jack");
        assertThat(result.getFirst().value().desc()).isEqualTo("Test consumer");

        wireMock.verify(getRequestedFor(urlEqualTo("/apisix/admin/consumers"))
                .withHeader("X-API-KEY", equalTo("test-api-key")));
    }

    @Test
    void listConsumers_emptyList() {
        wireMock.stubFor(get(urlEqualTo("/apisix/admin/consumers"))
                .willReturn(okJson("""
                        {
                          "list": [],
                          "total": 0
                        }
                        """)));

        var result = consumerGateway.list().block();

        assertThat(result).isEmpty();
    }

    @Test
    void findById_returnsConsumer() {
        wireMock.stubFor(get(urlEqualTo("/apisix/admin/consumers/jack"))
                .willReturn(okJson("""
                        {
                          "key": "/apisix/consumers/jack",
                          "value": {
                            "username": "jack",
                            "desc": "Test consumer",
                            "plugins": {
                              "key-auth": {"key": "auth-jack"}
                            }
                          }
                        }
                        """)));

        var result = consumerGateway.findById("jack").block();

        assertThat(result).isNotNull();
        assertThat(result.username()).isEqualTo("jack");
        assertThat(result.desc()).isEqualTo("Test consumer");
        assertThat(result.plugins()).containsKey("key-auth");
    }

    @Test
    void findById_notFound_throwsApisixException() {
        wireMock.stubFor(get(urlEqualTo("/apisix/admin/consumers/unknown"))
                .willReturn(aResponse().withStatus(404).withBody("{\"error_msg\":\"not found\"}")));

        assertThrows(ApisixException.class,
                () -> consumerGateway.findById("unknown").block());
    }
}

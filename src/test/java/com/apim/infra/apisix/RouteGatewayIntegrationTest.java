package com.apim.infra.apisix;

import com.apim.domain.ApisixException;
import com.apim.domain.gateway.RouteGateway;
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

@SpringBootTest(classes = RouteGatewayIntegrationTest.TestConfig.class)
class RouteGatewayIntegrationTest {

    @Configuration
    @Import({ApisixWebClientConfig.class, ApisixRouteGateway.class})
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
    private RouteGateway routeGateway;

    @BeforeEach
    void setUp() {
        wireMock.resetAll();
    }

    @Test
    void listRoutes_returnsRoutes() {
        wireMock.stubFor(get(urlEqualTo("/apisix/admin/routes"))
                .willReturn(okJson("""
                        {
                          "list": [
                            {
                              "key": "/apisix/routes/1",
                              "value": {
                                "id": "1",
                                "name": "test-route",
                                "uri": "/api/test",
                                "methods": ["GET", "POST"],
                                "status": 1
                              }
                            }
                          ],
                          "total": 1
                        }
                        """)));

        var result = routeGateway.list().block();

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().id()).isEqualTo("/apisix/routes/1");
        assertThat(result.getFirst().value().name()).isEqualTo("test-route");
        assertThat(result.getFirst().value().uri()).isEqualTo("/api/test");
        assertThat(result.getFirst().value().methods()).containsExactly("GET", "POST");

        wireMock.verify(getRequestedFor(urlEqualTo("/apisix/admin/routes"))
                .withHeader("X-API-KEY", equalTo("test-api-key")));
    }

    @Test
    void listRoutes_emptyList() {
        wireMock.stubFor(get(urlEqualTo("/apisix/admin/routes"))
                .willReturn(okJson("""
                        {
                          "list": [],
                          "total": 0
                        }
                        """)));

        var result = routeGateway.list().block();

        assertThat(result).isEmpty();
    }

    @Test
    void findById_returnsRoute() {
        wireMock.stubFor(get(urlEqualTo("/apisix/admin/routes/1"))
                .willReturn(okJson("""
                        {
                          "key": "/apisix/routes/1",
                          "value": {
                            "id": "1",
                            "name": "test-route",
                            "uri": "/api/test",
                            "methods": ["GET"],
                            "host": "example.com",
                            "priority": 0,
                            "status": 1
                          }
                        }
                        """)));

        var result = routeGateway.findById("1").block();

        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo("1");
        assertThat(result.name()).isEqualTo("test-route");
        assertThat(result.uri()).isEqualTo("/api/test");
        assertThat(result.host()).isEqualTo("example.com");
    }

    @Test
    void findById_notFound_throwsApisixException() {
        wireMock.stubFor(get(urlEqualTo("/apisix/admin/routes/999"))
                .willReturn(aResponse().withStatus(404).withBody("{\"error_msg\":\"not found\"}")));

        assertThrows(ApisixException.class,
                () -> routeGateway.findById("999").block());
    }
}

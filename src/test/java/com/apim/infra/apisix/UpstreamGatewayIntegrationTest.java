package com.apim.infra.apisix;

import com.apim.domain.ApisixException;
import com.apim.domain.gateway.UpstreamGateway;
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

@SpringBootTest(classes = UpstreamGatewayIntegrationTest.TestConfig.class)
class UpstreamGatewayIntegrationTest {

    @Configuration
    @Import({ApisixWebClientConfig.class, ApisixUpstreamGateway.class})
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
    private UpstreamGateway upstreamGateway;

    @BeforeEach
    void setUp() {
        wireMock.resetAll();
    }

    @Test
    void listUpstreams_returnsUpstreams() {
        wireMock.stubFor(get(urlEqualTo("/apisix/admin/upstreams"))
                .willReturn(okJson("""
                        {
                          "list": [
                            {
                              "key": "/apisix/upstreams/1",
                              "value": {
                                "id": "1",
                                "name": "test-upstream",
                                "type": "roundrobin",
                                "nodes": [
                                  {"host": "127.0.0.1", "port": 8080, "weight": 1}
                                ]
                              }
                            }
                          ],
                          "total": 1
                        }
                        """)));

        var result = upstreamGateway.list().block();

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().id()).isEqualTo("/apisix/upstreams/1");
        assertThat(result.getFirst().value().name()).isEqualTo("test-upstream");
        assertThat(result.getFirst().value().type()).isEqualTo("roundrobin");

        wireMock.verify(getRequestedFor(urlEqualTo("/apisix/admin/upstreams"))
                .withHeader("X-API-KEY", equalTo("test-api-key")));
    }

    @Test
    void listUpstreams_emptyList() {
        wireMock.stubFor(get(urlEqualTo("/apisix/admin/upstreams"))
                .willReturn(okJson("""
                        {
                          "list": [],
                          "total": 0
                        }
                        """)));

        var result = upstreamGateway.list().block();

        assertThat(result).isEmpty();
    }

    @Test
    void findById_returnsUpstream() {
        wireMock.stubFor(get(urlEqualTo("/apisix/admin/upstreams/1"))
                .willReturn(okJson("""
                        {
                          "key": "/apisix/upstreams/1",
                          "value": {
                            "id": "1",
                            "name": "test-upstream",
                            "type": "roundrobin",
                            "nodes": [
                              {"host": "127.0.0.1", "port": 8080, "weight": 1}
                            ]
                          }
                        }
                        """)));

        var result = upstreamGateway.findById("1").block();

        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo("1");
        assertThat(result.name()).isEqualTo("test-upstream");
        assertThat(result.type()).isEqualTo("roundrobin");
    }

    @Test
    void findById_notFound_throwsApisixException() {
        wireMock.stubFor(get(urlEqualTo("/apisix/admin/upstreams/999"))
                .willReturn(aResponse().withStatus(404).withBody("{\"error_msg\":\"not found\"}")));

        assertThrows(ApisixException.class,
                () -> upstreamGateway.findById("999").block());
    }
}

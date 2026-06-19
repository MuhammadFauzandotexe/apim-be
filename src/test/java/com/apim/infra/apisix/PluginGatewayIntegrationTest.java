package com.apim.infra.apisix;

import com.apim.domain.ApisixException;
import com.apim.domain.gateway.PluginGateway;
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

@SpringBootTest(classes = PluginGatewayIntegrationTest.TestConfig.class)
class PluginGatewayIntegrationTest {

    @Configuration
    @Import({ApisixWebClientConfig.class, ApisixPluginGateway.class})
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
    private PluginGateway pluginGateway;

    @BeforeEach
    void setUp() {
        wireMock.resetAll();
    }

    @Test
    void listSchemas_returnsPluginSchemas() {
        wireMock.stubFor(get(urlEqualTo("/apisix/admin/schema/plugins"))
                .willReturn(okJson("""
                        {
                          "jwt-auth": {
                            "type": "object",
                            "properties": {
                              "key": {"type": "string"}
                            }
                          },
                          "key-auth": {
                            "type": "object",
                            "properties": {
                              "key": {"type": "string"}
                            }
                          }
                        }
                        """)));

        var result = pluginGateway.listSchemas().block();

        assertThat(result).hasSize(2);
        assertThat(result).containsKeys("jwt-auth", "key-auth");
        assertThat(result.get("jwt-auth").name()).isEqualTo("jwt-auth");

        wireMock.verify(getRequestedFor(urlEqualTo("/apisix/admin/schema/plugins"))
                .withHeader("X-API-KEY", equalTo("test-api-key")));
    }

    @Test
    void getSchema_returnsPluginSchema() {
        wireMock.stubFor(get(urlEqualTo("/apisix/admin/schema/plugins/jwt-auth"))
                .willReturn(okJson("""
                        {
                          "type": "object",
                          "properties": {
                            "key": {"type": "string"},
                            "secret": {"type": "string"}
                          },
                          "required": ["key"]
                        }
                        """)));

        var result = pluginGateway.getSchema("jwt-auth").block();

        assertThat(result).isNotNull();
        assertThat(result.name()).isEqualTo("jwt-auth");
        assertThat(result.schema()).containsKey("type");
        assertThat(result.schema()).containsKey("properties");
    }

    @Test
    void getSchema_notFound_throwsApisixException() {
        wireMock.stubFor(get(urlEqualTo("/apisix/admin/schema/plugins/nonexistent"))
                .willReturn(aResponse().withStatus(404).withBody("{\"error_msg\":\"not found\"}")));

        assertThrows(ApisixException.class,
                () -> pluginGateway.getSchema("nonexistent").block());
    }
}

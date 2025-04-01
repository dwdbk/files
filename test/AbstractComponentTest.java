package com.futurmaster.demandplanning.componenttest;

import com.c4_soft.springaddons.security.oauth2.test.annotations.OpenIdClaims;
import com.c4_soft.springaddons.security.oauth2.test.annotations.keycloak.WithMockKeycloakAuth;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.futurmaster.demandplanning.model.DPConfig;
import com.futurmaster.demandplanning.model.Node;
import com.futurmaster.demandplanning.model.SearchNode;
import com.futurmaster.demandplanning.service.CacheService;
import com.github.tomakehurst.wiremock.client.WireMock;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.http.HttpHeaders;
import org.junit.jupiter.api.BeforeEach;
import org.keycloak.adapters.springboot.KeycloakSpringBootConfigResolver;
import org.keycloak.adapters.springsecurity.KeycloakSecurityComponents;
import org.keycloak.representations.AccessTokenResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.AutoConfigureDataMongo;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.stream.Collectors;

import static com.futurmaster.demandplanning.utils.MapperUtils.OBJECT_MAPPER;
import static com.github.tomakehurst.wiremock.client.WireMock.okJson;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

@SpringBootTest(webEnvironment = RANDOM_PORT)
@AutoConfigureWireMock(port = 0)
@AutoConfigureDataMongo
@AutoConfigureMockMvc(addFilters = false)
@ComponentScan(basePackageClasses = {KeycloakSecurityComponents.class, KeycloakSpringBootConfigResolver.class})
@WithMockKeycloakAuth(claims = @OpenIdClaims(exp = "2025-01-01T00:00:00.00Z"))
@ContextConfiguration(classes = TestConfig.class)
public abstract class AbstractComponentTest {

    protected static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("dd/MM/yyyy");

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected ObjectMapper objectMapper;

    @Autowired
    protected MongoTemplate mongoTemplate;

    @Autowired
    protected CacheService cacheService;

    @BeforeEach
    final void init() throws JsonProcessingException {
        // Useful for calling HeaderRequestInterceptor when saving a node in the database.
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(new MockHttpServletRequest()));

        mongoTemplate.getDb().drop();
        WireMock.reset();
        cacheService.clearAllCaches();

        stubKeycloak();
    }

    private void stubKeycloak() throws JsonProcessingException {
        AccessTokenResponse token = new AccessTokenResponse();
        token.setToken("token");
        stubFor(WireMock.post("/realms/XXX/protocol/openid-connect/token")
                .willReturn(okJson(OBJECT_MAPPER.writeValueAsString(token))
                        .withHeader(HttpHeaders.CONNECTION, "close"))
        );
    }

    protected void insertNodes(List<Node> nodes, DPConfig dpConfig) {
        List<SearchNode> searchNodes = nodes.stream()
                .map(node -> Pair.of(node.getNodeId(), node.getCode()))
                .distinct()
                .map(p -> SearchNode.builder()
                        .code(p.getRight())
                        .nodeId(p.getLeft())
                        .configId(dpConfig.getId())
                        .build())
                .collect(Collectors.toList());

        mongoTemplate.insertAll(searchNodes);
        mongoTemplate.insertAll(nodes);
    }
}

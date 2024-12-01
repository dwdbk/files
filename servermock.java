package com.cacib.loanscape.config;

import com.cacib.loanscape.common.external.CPY;
import com.cacib.loanscape.oidc.TokenSupplier;
import com.jayway.jsonpath.TypeRef;
import com.netflix.graphql.dgs.client.GraphQLResponse;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class ThirdPartyLoaderBeanTest {

    private MockWebServer mockWebServer;

    private ThirdPartyLoaderBean thirdPartyLoaderBean;

    private TokenSupplier tokenSupplier;

    /*
    <dependency>
    <groupId>com.squareup.okhttp3</groupId>
    <artifactId>mockwebserver</artifactId>
    <scope>test</scope>
</dependency>
    */

    @BeforeEach
    void setUp() throws Exception {
        // Start MockWebServer
        mockWebServer = new MockWebServer();
        mockWebServer.start();

        // Mock TokenSupplier
        tokenSupplier = mock(TokenSupplier.class);
        when(tokenSupplier.get()).thenReturn(() -> "mock-token");

        // Initialize ThirdPartyLoaderBean with WebClient pointing to MockWebServer
        thirdPartyLoaderBean = new ThirdPartyLoaderBean(tokenSupplier);
        thirdPartyLoaderBean.setGatewayUrl(mockWebServer.url("/graphql").toString());
    }

    @AfterEach
    void tearDown() throws Exception {
        // Shut down MockWebServer
        mockWebServer.shutdown();
    }

    @Test
    void testExecuteQueryWithWebClient() {
        // Arrange
        String graphqlResponse = """
                {
                  "data": {
                    "ALL_CACIB_THIRDPARTIES": [
                      {
                        "ricosId": "testRicosId",
                        "isCacibEntity": "YES"
                      }
                    ]
                  }
                }
                """;
        mockWebServer.enqueue(new MockResponse()
                .setBody(graphqlResponse)
                .addHeader("Content-Type", "application/json"));

        String query = "query { ALL_CACIB_THIRDPARTIES { ricosId isCacibEntity } }";

        // Act
        Mono<GraphQLResponse> responseMono = thirdPartyLoaderBean.executeQuery(query, null);
        GraphQLResponse response = responseMono.block();

        // Assert
        assertThat(response).isNotNull();
        List<CPY> thirdParties = response.extractValueAsObject("ALL_CACIB_THIRDPARTIES", new TypeRef<>() {});
        assertThat(thirdParties).isNotNull().hasSize(1);
        assertThat(thirdParties.get(0).getRicosId()).isEqualTo("testRicosId");
        assertThat(thirdParties.get(0).getIsCacibEntity()).isEqualTo("YES");

        // Verify that the WebClient sent the request
        var recordedRequest = mockWebServer.takeRequest();
        assertThat(recordedRequest.getPath()).isEqualTo("/graphql");
        assertThat(recordedRequest.getHeader("Authorization")).isEqualTo("Bearer mock-token");
    }
     @Test
    void testDoQueryWithValidResponse() {
        // Arrange
        String graphqlResponse = """
                {
                  "data": {
                    "ALL_CACIB_THIRDPARTIES": [
                      {
                        "ricosId": "testRicosId",
                        "isCacibEntity": "YES"
                      }
                    ]
                  }
                }
                """;
        mockWebServer.enqueue(new MockResponse()
                .setBody(graphqlResponse)
                .addHeader("Content-Type", "application/json"));

        String query = "query { ALL_CACIB_THIRDPARTIES { ricosId isCacibEntity } }";

        // Act
        List<CPY> result = thirdPartyLoaderBean.doQuery(query, "ALL_CACIB_THIRDPARTIES", new TypeRef<>() {});

        // Assert
        assertThat(result).isNotNull().hasSize(1);
        assertThat(result.get(0).getRicosId()).isEqualTo("testRicosId");
        assertThat(result.get(0).getIsCacibEntity()).isEqualTo("YES");

        // Verify that the WebClient sent the request
        var recordedRequest = mockWebServer.takeRequest();
        assertThat(recordedRequest.getPath()).isEqualTo("/graphql");
        assertThat(recordedRequest.getHeader("Authorization")).isEqualTo("Bearer mock-token");
    }

    @Test
    void testDoAliasQueryWithValidResponse() {
        // Arrange
        String graphqlResponse = """
                {
                  "data": {
                    "alias1": [
                      {
                        "ricosId": "testRicosId1",
                        "isCacibEntity": "YES"
                      }
                    ],
                    "alias2": [
                      {
                        "ricosId": "testRicosId2",
                        "isCacibEntity": "NO"
                      }
                    ]
                  }
                }
                """;
        mockWebServer.enqueue(new MockResponse()
                .setBody(graphqlResponse)
                .addHeader("Content-Type", "application/json"));

        String query = "query { alias1 { ricosId isCacibEntity } alias2 { ricosId isCacibEntity } }";
        List<String> aliases = List.of("alias1", "alias2");

        // Act
        Map<String, List<CPY>> result = thirdPartyLoaderBean.doAliasQuery(query, "testQueryName", "", new TypeRef<>() {}, aliases);

        // Assert
        assertThat(result).isNotNull().hasSize(2);
        assertThat(result.get("alias1")).isNotNull().hasSize(1);
        assertThat(result.get("alias1").get(0).getRicosId()).isEqualTo("testRicosId1");
        assertThat(result.get("alias1").get(0).getIsCacibEntity()).isEqualTo("YES");
        assertThat(result.get("alias2")).isNotNull().hasSize(1);
        assertThat(result.get("alias2").get(0).getRicosId()).isEqualTo("testRicosId2");
        assertThat(result.get("alias2").get(0).getIsCacibEntity()).isEqualTo("NO");

        // Verify that the WebClient sent the request
        var recordedRequest = mockWebServer.takeRequest();
        assertThat(recordedRequest.getPath()).isEqualTo("/graphql");
        assertThat(recordedRequest.getHeader("Authorization")).isEqualTo("Bearer mock-token");
    }
    
     @Test
    void testInitAllCacibThirdPartiesWithWebClient() {
        // Arrange
        String graphqlResponse = """
                {
                  "data": {
                    "ALL_CACIB_THIRDPARTIES": [
                      {
                        "ricosId": "testRicosId",
                        "isCacibEntity": "YES"
                      }
                    ]
                  }
                }
                """;
        mockWebServer.enqueue(new MockResponse()
                .setBody(graphqlResponse)
                .addHeader("Content-Type", "application/json"));

        // Act
        thirdPartyLoaderBean.initAllCacibThirdParties();

        // Assert
        List<CPY> thirdParties = thirdPartyLoaderBean.getCacibThirdParties();
        assertThat(thirdParties).isNotNull().hasSize(1);
        assertThat(thirdParties.get(0).getRicosId()).isEqualTo("testRicosId");
        assertThat(thirdParties.get(0).getIsCacibEntity()).isEqualTo("YES");

        // Verify that the WebClient sent the request
        var recordedRequest = mockWebServer.takeRequest();
        assertThat(recordedRequest.getPath()).isEqualTo("/graphql");
        assertThat(recordedRequest.getHeader("Authorization")).isEqualTo("Bearer mock-token");
    }

}

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
}

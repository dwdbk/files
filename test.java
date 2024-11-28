import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.cacib.loanscape.config.gateway.GatewayClient;
import com.netflix.graphql.dgs.client.GraphQLResponse;
import com.netflix.graphql.dgs.client.MonoGraphQLClient;
import reactor.core.publisher.Mono;

@ExtendWith(MockitoExtension.class)
public class GatewayClientTest {

    @InjectMocks
    private GatewayClient gatewayClient;

    @Mock
    private MonoGraphQLClient graphqlClient;

    @Test
    public void testPerform() {
        String query = "some query";
        GraphQLResponse expectedResponse = new GraphQLResponse("{\"data\":{\"someField\":\"someValue\"}}");

        when(graphqlClient.reactiveExecuteQuery(anyString())).thenReturn(Mono.just(expectedResponse));

        GraphQLResponse actualResponse = gatewayClient.perform(query);

        assertNotNull(actualResponse);
        assertEquals(expectedResponse.getJson(), actualResponse.getJson());
    }
}

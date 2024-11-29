import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class ThirdPartyLoaderBeanTest {

    @Mock
    private WebClient webClient;

    @InjectMocks
    private ThirdPartyLoaderBean thirdPartyLoaderBean;

    @Test
    public void testDoQuerySuccess() {
        // Mock successful gateway response
        Mockito.when(webClient.post()
                .uri(Mockito.anyString())
                .bodyValue(Mockito.anyString())
                .retrieve()
                .bodyToMono(GraphQLResponse.class))
                .thenReturn(Mono.just(new GraphQLResponse("{\"data\": {\"queryName\": \"expectedResult\"}}")));

        // Execute the query
        String result = thirdPartyLoaderBean.doQuery("query", "queryName", TypeRef.of(String.class));

        // Assert the result
        assertEquals("expectedResult", result);
    }

    @Test
    public void testDoQueryGatewayFailure() {
        // Mock failed gateway response
        Mockito.when(webClient.post()
                .uri(Mockito.anyString())
                .bodyValue(Mockito.anyString())
                .retrieve()
                .bodyToMono(GraphQLResponse.class))
                .thenReturn(Mono.error(new RuntimeException("Gateway error")));

        // Execute the query
        String result = thirdPartyLoaderBean.doQuery("query", "queryName", TypeRef.of(String.class));

        // Assert the result is null
        assertNull(result);
    }

    // ... more test cases for `doQuery()` with different input scenarios

    @Test
    public void testDoAliasQuerySuccess() {
        // Mock successful gateway response
        Mockito.when(webClient.post()
                .uri(Mockito.anyString())
                .bodyValue(Mockito.anyString())
                .retrieve()
                .bodyToMono(GraphQLResponse.class))
                .thenReturn(Mono.just(new GraphQLResponse("{\"data\": {\"alias1\": \"value1\", \"alias2\": \"value2\"}}")));

        // Execute the alias query
        Map<String, String> result = thirdPartyLoaderBean.doAliasQuery("query", "queryName", "", TypeRef.of(String.class), List.of("alias1", "alias2"));

        // Assert the result
        assertEquals("value1", result.get("alias1"));
        assertEquals("value2", result.get("alias2"));
    }

    // ... more test cases for `doAliasQuery()` with different alias configurations and error scenarios

    @Test
    public void testDoQueryEmptyQueryName() {
        // Mock gateway response
        Mockito.when(webClient.post()
                .uri(Mockito.anyString())
                .bodyValue(Mockito.anyString())
                .retrieve()
                .bodyToMono(GraphQLResponse.class))
                .thenReturn(Mono.just(new GraphQLResponse("{\"data\": {\"queryName\": \"expectedResult\"}}")));

        // Execute the query with an empty query name
        String result = thirdPartyLoaderBean.doQuery("query", "", TypeRef.of(String.class));

        // Assert that an exception is thrown
        assertThrows(IllegalArgumentException.class, () -> result);
    }

    @Test
    public void testDoQueryInvalidTypeRef() {
        // Execute the query with an invalid TypeRef
        assertThrows(IllegalArgumentException.class, () -> thirdPartyLoaderBean.doQuery("query", "queryName", null));
    }

    // ... more test cases for `doQuery()` with different input scenarios

    @Test
    public void testDoAliasQuerySuccess() {
        // ... (same as before)
    }

    @Test
    public void testDoAliasQueryEmptyAliases() {
        // Mock gateway response
        Mockito.when(webClient.post()
                .uri(Mockito.anyString())
                .bodyValue(Mockito.anyString())
                .retrieve()
                .bodyToMono(GraphQLResponse.class))
                .thenReturn(Mono.just(new GraphQLResponse("{\"data\": {\"alias1\": \"value1\", \"alias2\": \"value2\"}}")));

        // Execute the alias query with empty aliases
        Map<String, String> result = thirdPartyLoaderBean.doAliasQuery("query", "queryName", "", TypeRef.of(String.class), List.of());

        // Assert that the result is an empty map
        assertTrue(result.isEmpty());
    }

    @Test
    public void testDoAliasQueryInvalidSuffix() {
        // Execute the alias query with an invalid suffix
        assertThrows(IllegalArgumentException.class, () -> thirdPartyLoaderBean.doAliasQuery("query", "queryName", null, TypeRef.of(String.class), List.of("alias1", "alias2")));
    }

    // ... more test cases for `doAliasQuery()` with different alias configurations and error scenarios

    // Test for `executeQuery()` method (if accessible)
    @Test
    public void testExecuteQuerySuccess() {
        // ... (similar to the `doQuery()` success test, but without the result extraction)
    }

    @Test
    public void testExecuteQueryGatewayFailure() {
        // ... (similar to the `doQuery()` failure test, but without the result extraction)
    }
      @Test
    public void testDoQuerySuccess() {
        // ... (same as before)
    }

    @Test
    public void testDoQueryGatewayFailure() {
        // ... (same as before)
    }

    @Test
    public void testDoQueryEmptyQueryName() {
        // Mock gateway response
        Mockito.when(webClient.post()
                .uri(Mockito.anyString())
                .bodyValue(Mockito.anyString())
                .retrieve()
                .bodyToMono(GraphQLResponse.class))
                .thenReturn(Mono.just(new GraphQLResponse("{\"data\": {\"queryName\": \"expectedResult\"}}")));

        // Execute the query with an empty query name
        String result = thirdPartyLoaderBean.doQuery("query", "", TypeRef.of(String.class));

        // Assert that an exception is thrown
        assertThrows(IllegalArgumentException.class, () -> result);
    }

    @Test
    public void testDoQueryInvalidTypeRef() {
        // Execute the query with an invalid TypeRef
        assertThrows(IllegalArgumentException.class, () -> thirdPartyLoaderBean.doQuery("query", "queryName", null));
    }

    // ... more test cases for `doQuery()` with different input scenarios

    @Test
    public void testDoAliasQuerySuccess() {
        // ... (same as before)
    }

    @Test
    public void testDoAliasQueryEmptyAliases() {
        // Mock gateway response
        Mockito.when(webClient.post()
                .uri(Mockito.anyString())
                .bodyValue(Mockito.anyString())
                .retrieve()
                .bodyToMono(GraphQLResponse.class))
                .thenReturn(Mono.just(new GraphQLResponse("{\"data\": {\"alias1\": \"value1\", \"alias2\": \"value2\"}}")));

        // Execute the alias query with empty aliases
        Map<String, String> result = thirdPartyLoaderBean.doAliasQuery("query", "queryName", "", TypeRef.of(String.class), List.of());

        // Assert that the result is an empty map
        assertTrue(result.isEmpty());
    }

    @Test
    public void testDoAliasQueryInvalidSuffix() {
        // Execute the alias query with an invalid suffix
        assertThrows(IllegalArgumentException.class, () -> thirdPartyLoaderBean.doAliasQuery("query", "queryName", null, TypeRef.of(String.class), List.of("alias1", "alias2")));
    }

    // ... more test cases for `doAliasQuery()` with different alias configurations and error scenarios

    // Test for `executeQuery()` method (if accessible)
    @Test
    public void testExecuteQuerySuccess() {
        // ... (similar to the `doQuery()` success test, but without the result extraction)
    }

    @Test
    public void testExecuteQueryGatewayFailure() {
        // ... (similar to the `doQuery()` failure test, but without the result extraction)
    }

    // Additional tests based on the new code:

    @Test
    public void testInitAllCacibThirdParties() {
        // Mock successful gateway response
        Mockito.when(webClient.post()
                .uri(Mockito.anyString())
                .bodyValue(Mockito.anyString())
                .retrieve()
                .bodyToMono(GraphQLResponse.class))
                .thenReturn(Mono.just(new GraphQLResponse("{\"data\": {\"ALL_CACIB_THIRDPARTIES\": [{\"ricosId\": \"123\", \"isCacibEntity\": true}]}}")));

        // Call the method
        thirdPartyLoaderBean.initAllCacibThirdParties();

        // Assert that the map is populated correctly
        assertEquals(1, thirdPartyLoaderBean.getIsCacibEntityForRicosId().size());
        assertTrue(thirdPartyLoaderBean.getIsCacibEntityForRicosId().containsKey("123"));
    }

    @Test
    public void testInitAllCacibThirdPartiesGatewayFailure() {
        // Mock failed gateway response
        Mockito.when(webClient.post()
                .uri(Mockito.anyString())
                .bodyValue(Mockito.anyString())
                .retrieve()
                .bodyToMono(GraphQLResponse.class))
                .thenReturn(Mono.error(new RuntimeException("Gateway error")));

        // Call the method
        thirdPartyLoaderBean.initAllCacibThirdParties();

        // Assert that the map is empty
        assertTrue(thirdPartyLoaderBean.getIsCacibEntityForRicosId().isEmpty());
    }
}

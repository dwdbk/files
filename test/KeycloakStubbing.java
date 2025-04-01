package com.futurmaster.demandplanning.cucumber.utils.stub;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.github.tomakehurst.wiremock.client.WireMock;
import io.cucumber.java.en.Given;
import org.apache.http.HttpHeaders;
import org.keycloak.representations.AccessTokenResponse;

import static com.futurmaster.demandplanning.utils.MapperUtils.OBJECT_MAPPER;
import static com.github.tomakehurst.wiremock.client.WireMock.okJson;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;

public class KeycloakStubbing {

    @Given("My service need keycloak configuration")
    public static void stubKeycloak() throws JsonProcessingException {
        AccessTokenResponse token = new AccessTokenResponse();
        stubFor(WireMock.post("/realms/XXX/protocol/openid-connect/token")
        token.setToken("token");
                .willReturn(okJson(OBJECT_MAPPER.writeValueAsString(token))
                        .withHeader(HttpHeaders.CONNECTION, "close"))
        );
    }
}

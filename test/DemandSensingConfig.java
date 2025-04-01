package com.futurmaster.demandplanning.componenttest.utils;

import com.futurmaster.demandplanning.model.demandsensing.KPI;
import wiremock.org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Objects;

import static com.github.tomakehurst.wiremock.client.WireMock.*;

public class DemandSensingConfig {

    public static String getModels() throws IOException {
        InputStream modelsInputStream = DemandSensingConfig.class.getClassLoader().getResourceAsStream("models.json");
        return IOUtils.toString(Objects.requireNonNull(modelsInputStream), StandardCharsets.UTF_8);
    }

    public static List<KPI> getKpis() {
        return List.of(
                new KPI("mae", "Label 1", "Description 1", "602.2901969378613", "%", true),
                new KPI("rmse", "Label 2", "Description 2", "846.9085017350354", "%", true),
                new KPI("smape", "Label 3", "Description 3", "1.0858959371950214", "%", true),
                new KPI("maape", "Label 4", "Description 4", "NaN", "%", true)
        );
    }

    public static void stubEffectProposal() {
        stubFor(post("/demandsensing/mass-dynamiceffectproposal/")
                .willReturn(okJson("{\"id\": \"batchId\"," +
                        "\"nodes\": [" +
                        "{\"key\": \"{{jsonPath request.body '$.nodes[0].key'}}\"," +
                        "\"eeReplacementValues\": { {{#each (jsonPath request.body '$.nodes[0].eeDates.keys()') as |eventId|}}" +
                        "\"{{{eventId}}}\": [1.0, 2.0]" +
                        "{{/each}}" +
                        " }}]}")
        .withTransformers("response-template")));
    }
}

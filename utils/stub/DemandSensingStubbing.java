package com.futurmaster.demandplanning.cucumber.utils.stub;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.futurmaster.demandplanning.cucumber.utils.CommonUtils;
import com.futurmaster.demandplanning.model.demandsensing.KPI;
import com.github.tomakehurst.wiremock.client.WireMock;
import io.cucumber.java.en.Given;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.List;

import static com.futurmaster.demandplanning.utils.MapperUtils.OBJECT_MAPPER;
import static com.github.tomakehurst.wiremock.client.WireMock.*;

public class DemandSensingStubbing extends CommonUtils {

    @Given("The demand sensing chronology for {} present in {}")
    public static void stubDemandSensingChronology(String chronologies, String modelsFileUri) throws IOException {
        genericChronologies = chronologies;
        genericModelsFileUri = modelsFileUri;
        File f = new File("src/test/resources/" + modelsFileUri);
        stubFor(get("/demandsensing/models/?chronologies=" + chronologies)
                .willReturn(okJson(FileUtils.readFileToString(f, Charset.defaultCharset()))));
    }

    @Given("The demand sensing indicators:")
    public void stubDemandSensignIndicators(List<KPI> kpis) throws JsonProcessingException {
        genericListKPI = kpis;
        stubFor(get("/demandsensing/indicators/")
                .willReturn(okJson(OBJECT_MAPPER.writeValueAsString(kpis))));

    }
    @Given("The demand Sensing CalcPerimeter")
    public static void stubDemandSensingCalcPerimeter() throws IOException {

        stubFor(WireMock.post("/demandsensing/calcperim/")
                .willReturn(okJson(OBJECT_MAPPER.writeValueAsString(new Double[]{20d}))));
    }

}

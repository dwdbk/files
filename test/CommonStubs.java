package com.futurmaster.demandplanning.componenttest.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.futurmaster.generated.FMProtoFMConfigOuterClass.FMProtoFMConfig;
import com.futurmaster.legacyclient.aggregate.AggregateResponse;
import com.futurmaster.legacyclient.node.NodeResponse;
import com.futurmaster.legacyclient.node.TimeSeriesType;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.matching.RequestPattern;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import static com.futurmaster.demandplanning.utils.MapperUtils.OBJECT_MAPPER;
import static com.futurmaster.legacyclient.node.TimeSeriesType.*;
import static com.github.tomakehurst.wiremock.client.WireMock.*;

public class CommonStubs {

    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("dd/MM/yyyy");
    public static RequestPattern SAVE_NODES_STUB_REQUEST;
    public static RequestPattern LOAD_NODE_STUB_REQUEST;

    private CommonStubs() {
    }

    public static void stubLegacyConfig() throws Exception {
        stubLegacyConfig(null, null, null, null);
    }

    public static void stubLegacyConfig(LocalDate startHistory, LocalDate endHistory, LocalDate startForecast, LocalDate endForecast)
            throws ParseException, JsonProcessingException {
        FMProtoFMConfig legacyConfig = LegacyConfig.getFmConfig(format(startHistory), format(endHistory), format(startForecast), format(endForecast));

        stubFor(get("/fmlegacywrapper/api/fmfacade/Common_GetFMConfig")
                .willReturn(aResponse().withBody(legacyConfig.toByteArray())));
        stubFor(get("/fmlegacywrapper/api/fmfacade/getAggregateRules")
                .willReturn(okJson(OBJECT_MAPPER.writeValueAsString(LegacyConfig.getAggregateRules()))));
        stubFor(get("/fmlegacywrapper/api/fmfacade/getSelections")
                .willReturn(okJson(OBJECT_MAPPER.writeValueAsString(LegacyConfig.getSelectionRules()))));
    }

    private static String format(LocalDate localDate) {
        return Optional.ofNullable(localDate).map(LegacyConfig.DATE_FORMATTER::format).orElse(null);
    }

    public static void stubDemandSensingConfig() throws IOException {
        stubFor(get("/demandsensing/models/?chronologies=DAILY%2CWEEKLY%2CMONTHLY")
                .willReturn(okJson(DemandSensingConfig.getModels())));

        stubFor(get("/demandsensing/indicators/")
                .willReturn(okJson(OBJECT_MAPPER.writeValueAsString(DemandSensingConfig.getKpis()))));
    }

    public static void stubNode() throws Exception {
        stubNode(DATE_FORMAT.parse("01/01/2021"), DATE_FORMAT.parse("10/01/2021"), null);
    }

    public static void stubNode(Date startDate, Date endDate, List<NodeResponse.TimeSeries> timeSeries) throws Exception {
        NodeResponse nodeResponse = new NodeResponse();
        nodeResponse.setId("NODE_ID");
        nodeResponse.setCode("NODE_CODE");
        Double[] values = new Double[]{10.0, 20.0, null};
        List<NodeResponse.TimeSeries> ts = timeSeries != null ? timeSeries : List.of(
                generateTimeSeries(HISTORY, startDate, endDate, values),
                generateTimeSeries(CLEANSED_HISTORY, startDate, endDate, values),
                generateTimeSeries(EE_EFFECT, startDate, endDate, new Double[]{null, null, null}),
                generateTimeSeries(EXTERNAL_VARIABLES_1, startDate, endDate, values),
                generateTimeSeries(EXTERNAL_VARIABLES_EFFECTS_1, startDate, endDate, values),
                generateTimeSeries(INDISTINCT_DATA_STORAGE_1, startDate, endDate, new Double[]{10.10, null, 20.20})
        );
        nodeResponse.setTimeSeries(ts);

        LOAD_NODE_STUB_REQUEST = stubFor(WireMock.post("/fmlegacywrapper/api/fmfacade/loadNode")
                .willReturn(okJson(OBJECT_MAPPER.writeValueAsString(nodeResponse)))).getRequest();

        stubFor(WireMock.post("/fmlegacywrapper/api/fmfacade/saveNode")
                .willReturn(okJson(OBJECT_MAPPER.writeValueAsString(nodeResponse))));

        SAVE_NODES_STUB_REQUEST = stubFor(post("/fmlegacywrapper/api/fmfacade/saveNodes")
                .willReturn(okJson(OBJECT_MAPPER.writeValueAsString(List.of(nodeResponse))))).getRequest();
    }

    public static void stubAggregate() throws IOException {
        // New nodes created in legacy
        AggregateResponse aggregateResponse1 = new AggregateResponse();
        aggregateResponse1.setId("NODE_ID1");
        aggregateResponse1.setKey("CODE1");
        aggregateResponse1.setDescription("DESCRIPTION 1");
        AggregateResponse aggregateResponse2 = new AggregateResponse();
        aggregateResponse2.setId("NODE_ID2");
        aggregateResponse2.setKey("CODE2");
        aggregateResponse2.setDescription("DESCRIPTION 2");
        // Node already exist
        AggregateResponse aggregateResponse3 = new AggregateResponse();
        aggregateResponse3.setId("NODE_ID");
        aggregateResponse3.setKey("NODE_CODE");
        aggregateResponse3.setDescription("DESCRIPTION");

        stubFor(WireMock.post("/fmlegacywrapper/api/fmfacade/getNodes")
                .willReturn(okJson(OBJECT_MAPPER.writeValueAsString(List.of(aggregateResponse1, aggregateResponse2, aggregateResponse3)))));
    }

    public static NodeResponse.TimeSeries generateTimeSeries(TimeSeriesType timeSeriesType, Date startDate, Date endDate, Double[] values) {
        NodeResponse.TimeSeries timeSeries = new NodeResponse.TimeSeries();
        timeSeries.setId(timeSeriesType);
        timeSeries.setVersion(1);
        timeSeries.setDateStart(startDate);
        timeSeries.setDateEnd(endDate);
        timeSeries.setValues(values);
        return timeSeries;
    }
}

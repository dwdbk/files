package com.futurmaster.demandplanning.cucumber.utils.generalPrecondition;

import com.futurmaster.demandplanning.cucumber.utils.FormatUtils;
import com.futurmaster.demandplanning.model.*;
import com.futurmaster.demandplanning.model.externalvariables.ExternalVariable;
import com.futurmaster.generated.FMProtoFMConfigOuterClass;
import com.futurmaster.legacyclient.node.NodeResponse;
import com.futurmaster.legacyclient.node.Rule;
import com.futurmaster.legacyclient.node.TimeSeriesType;
import io.cucumber.datatable.DataTable;
import io.cucumber.java.DataTableType;

import java.text.ParseException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;


public class GeneralPreconditionDataTableConfigures extends FormatUtils {

    @DataTableType
    public Rule entryRule(final Map<String, String> datatable) {
        return new Rule(datatable.get("key"), datatable.get("description"));
    }

    @DataTableType
    public FMProtoFMConfigOuterClass.FMProtoFMConfig entryFMProtoFMConfig(final Map<String, String> datatable) throws ParseException {
        FMProtoFMConfigOuterClass.FMProtoFMConfig.Builder configBuilder = FMProtoFMConfigOuterClass.FMProtoFMConfig.newBuilder()
                .setChronology(FMProtoFMConfigOuterClass.FMProtoFMConfig.DBChronologyType.ProtoDAYWEEK);
        FMProtoFMConfigOuterClass.FMProtoDBHorizonConfig.Builder dbHorizonConfigBuilder = FMProtoFMConfigOuterClass.FMProtoDBHorizonConfig.newBuilder()
                .setStartDateHistDay(getTimestampValueOrNull(datatable.get("startDateHistDay")))
                .setEndDateHistDay(getTimestampValueOrNull(datatable.get("endDateHistDay")))
                .setStartDateForecastDay(getTimestampValueOrNull(datatable.get("startDateForecastDay")))
                .setEndDateForecastDay(getTimestampValueOrNull(datatable.get("endDateForecastDay")))
                .setStartDateHistWeek(getTimestampValueOrNull(datatable.get("startDateHistDay")))
                .setEndDateHistWeek(getTimestampValueOrNull(datatable.get("endDateHistDay")))
                .setStartDateForecastWeek(getTimestampValueOrNull(datatable.get("startDateForecastDay")))
                .setEndDateForecastWeek(getTimestampValueOrNull(datatable.get("endDateForecastDay")));
        return configBuilder.setDbHorizonConfig(dbHorizonConfigBuilder).build();
    }

    @DataTableType
    public Alternative entryAlternative(final Map<String, String> datatable) {
        return Alternative.builder()
                .id(datatable.get("id"))
                .name(datatable.get("name"))
                .originHistory(datatable.get("originHistory"))
                .version(getLongValueOrNull(datatable.get("version")))
                .ver(getLongValueOrNull(datatable.get("ver")))
                .isDefault(getBooleanValueOrFalse(datatable.get("isDefault")))
                .build();
    }

    @DataTableType
    public Map<String, DPConfig> entryMapDPConfigByAlternativeName(final DataTable datatable) {
        Map<String, DPConfig> mapResult = new HashMap<>();
        datatable.asMaps().forEach(map -> {
            DPConfig config = DPConfig.builder()
                    .id(map.get("id"))
                    .level(ConfigLevel.builder()
                            .name(map.get("level.name"))
                            .priority(getIntegerValueOrNull(map.get("level.priority")))
                            .aggregate(map.get("level.aggregate"))
                            .chronologies(getListChronoTypeValueOrNull(map.get("level.chronologies")))
                            .build())
                    .build();
            mapResult.put(map.get("alternative"), config);
        });
        return mapResult;
    }

    @DataTableType
    public DPConfig entryDPConfig(final Map<String, String> datatable) {
        return DPConfig.builder()
                .id(datatable.get("id"))
                .level(ConfigLevel.builder()
                        .name(datatable.get("level.name"))
                        .priority(getIntegerValueOrNull(datatable.get("level.priority")))
                        .aggregate(datatable.get("level.aggregate"))
                        .chronologies(getListChronoTypeValueOrNull(datatable.get("level.chronologies")))
                        .build())
                .alternativesConfig(Map.of(
                        datatable.get("alternative.key"),
                        AlternativeConfig.builder()
                                .externalVariables(
                                        List.of(ExternalVariable.builder()
                                                .title(datatable.get("alternative.AlternativeConfig.title"))
                                                .timeSeriesType(TimeSeriesType.valueOf(datatable.get("alternative.AlternativeConfig.timeSeriesType")))
                                                .version(getIntegerValueOrNull(datatable.get("alternative.AlternativeConfig.version"))).build())).build()))
                .build();
    }

    @DataTableType
    public SearchNode entrySearchNode(final Map<String, String> datatable) {
        return SearchNode.builder()
                .id(datatable.get("id"))
                .code(datatable.get("code"))
                .nodeId(datatable.get("nodeId"))
                .configId(getStringValueOrNull(datatable.get("configId")))
                .build();
    }

    @DataTableType
    public Node entryNode(final Map<String, String> datatable) {
        return Node.builder()
                .id(datatable.get("id"))
                .nodeId(datatable.get("nodeId"))
                .alternativeId(datatable.get("alternativeId"))
                .code(datatable.get("code"))
                .chronology(ChronoType.valueOf(datatable.get("chronology")))
                .modelByChrono(getModelByChronoValueOrNull(datatable.get("modelByChrono")))
                .configId(datatable.get("configId"))
                //.perimetersByChrono(getPerimeterByChronoValueOrNull(datatable.get("perimeterByChrono")))
                //.notUpToDate(getBooleanValueOrFalse(datatable.get("notUpToDate")))
                .build();
    }

    @DataTableType
    public Map<String, Seasonality> entrySeasonalityByIdTypeSeasonality(final DataTable datatable) {
        Map<String, Seasonality> mapResult = new HashMap<>();
        datatable.entries().forEach(map -> {
            try {
                mapResult.put(map.get("type"), Seasonality.builder()
                        .id(map.get("id"))
                        .startDate(getDateValueOrNull(map.get("startDate")))
                        .endDate(getDateValueOrNull((map.get("endDate"))))
                        .build());
            } catch (ParseException e) {
                throw new RuntimeException(e);
            }
        });
        return mapResult;
    }

    @DataTableType
    public NodeResponse entryNodeResponse(final Map<String, String> datatable) {
        NodeResponse node = new NodeResponse();
        node.setId(datatable.get("id"));
        node.setCode(datatable.get("code"));
        return node;
    }

    @DataTableType
    public NodeResponse.TimeSeries entryTimeSeries(final Map<String, String> datatable) throws ParseException {
        NodeResponse.TimeSeries timeSeries = new NodeResponse.TimeSeries();
        timeSeries.setId(TimeSeriesType.valueOf(datatable.get("timeSeriesType")));
        timeSeries.setVersion(getIntegerValueOrNull(datatable.get("version")));
        timeSeries.setDateStart(getDateValueOrNull(datatable.get("dateStart")));
        timeSeries.setDateEnd(getDateValueOrNull(datatable.get("dateEnd")));
        timeSeries.setValues(Objects.requireNonNull(getListDoubleValueOrNull(datatable.get("values"))).toArray(Double[]::new));
        return timeSeries;
    }

}

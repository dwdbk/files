package com.futurmaster.demandplanning.cucumber.node;

import com.futurmaster.demandplanning.cucumber.utils.dto.NodeDtoCucumber;
import com.futurmaster.demandplanning.model.*;
import com.futurmaster.legacyclient.node.NodeResponse;
import com.futurmaster.legacyclient.node.TimeSeriesType;
import io.cucumber.datatable.DataTable;
import io.cucumber.java.DataTableType;

import java.text.ParseException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import static com.futurmaster.demandplanning.cucumber.utils.FormatUtils.*;

public class DataTableConfigurer {

    @DataTableType
    public PerimeterDto entryPerimeterDto(final Map<String, String> datatable) throws ParseException {
        return PerimeterDto.builder()
                .id(getStringValueOrNull(datatable.get("id")))
                .description(datatable.get("description"))
                .date(getDateValueOrNull(datatable.get("date")))
                .value(getDoubleValueOrNull(datatable.get("value")))
                .status(getPerimeterDtoStatusValueOrNull(datatable.get("status")))
                .dynamic(getBooleanValueOrFalse(datatable.get("dynamic")))
                .build();
    }

    @DataTableType
    public Map<TimeSeriesType, NodeResponse.TimeSeries> entryMapTimeSeriesTypeTimeSeries(final DataTable datatable) throws ParseException {
        Map<TimeSeriesType, NodeResponse.TimeSeries> mapResult = new HashMap<>();
        datatable.entries().forEach(map -> {
            TimeSeriesType type = TimeSeriesType.valueOf(map.get("TimeSeriesType"));
            NodeResponse.TimeSeries ts = new NodeResponse.TimeSeries();
            ts.setVersion(getIntegerValueOrNull(map.get("TimeSeries.version")));
            ts.setValues(Objects.requireNonNull(getListDoubleValueOrNull(map.get("TimeSeries.values"))).toArray(Double[]::new));
            mapResult.put(type,ts);
        });
        return mapResult;
    }

    @DataTableType
    public ExceptionalEventDto entryExceptionalEventDto(final Map<String, String> datatable) throws ParseException {

       return ExceptionalEventDto.builder()
               .id(getStringValueOrNull(datatable.get("id")))
                .startDate(getDateValueOrNull(datatable.get("startDate")))
                .endDate(getDateValueOrNull(datatable.get("endDate")))
                .values(getListDoubleValueOrNull(datatable.get("values")))
                .effectValues(getListDoubleValueOrNull(datatable.get("effectValues")))
                .build();
    }
    @DataTableType
    public ExceptionalEvent entryExceptionalEvent(final Map<String, String> datatable) throws ParseException {

        return ExceptionalEvent.builder()
                .startDate(getDateValueOrNull(datatable.get("startDate")))
                .endDate(getDateValueOrNull(datatable.get("endDate")))
                .values(getListDoubleValueOrNull(datatable.get("values")))
                .effectValues(getListDoubleValueOrNull(datatable.get("effectValues")))
                .build();
    }

    @DataTableType
    public Perimeter entryPerimeter(final Map<String, String> datatable) throws ParseException {
        return Perimeter.builder()
                .id(getStringValueOrNull(datatable.get("id")))
                .description(datatable.get("description"))
                .date(getDateValueOrNull(datatable.get("date")))
                .value(getDoubleValueOrNull(datatable.get("value")))
                .status(getPerimeterStatusValueOrNull(datatable.get("status")))
                .dynamic(getBooleanValueOrFalse(datatable.get("dynamic")))
                .build();
    }

    @DataTableType
    public Seasonality entrySeasonality(final Map<String, String> datatable) throws ParseException {
        return Seasonality.builder()
                .id(datatable.get("id"))
                .startDate(getDateValueOrNull(datatable.get("startDate")))
                .endDate(getDateValueOrNull(datatable.get("endDate")))
                .type(SeasonalityType.builder()
                        .id(getStringValueOrNull(datatable.get("type.id")))
                        .color(datatable.get("type.color"))
                        .title(datatable.get("type.title"))
                        .ver(getLongValueOrNull(datatable.get("ver")))
                        .build())
                .build();
    }
    @DataTableType
    public NodeDtoCucumber entryNodeDtoCucumber(final Map<String, String> datatable) {
        return NodeDtoCucumber.builder()
                .id(datatable.get("id"))
                .nodeId(datatable.get("nodeId"))
                .alternativeId(datatable.get("alternativeId"))
                .code(datatable.get("code"))
                .ver(getLongValueOrNull(datatable.get("ver")))
                .chronology(ChronoType.valueOf(datatable.get("chronology")))
                .configId(datatable.get("configId"))
                .notUpToDate(getBooleanValueOrFalse(datatable.get("notUpToDate")))
                .build();
    }


}

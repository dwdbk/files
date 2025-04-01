package com.futurmaster.demandplanning.cucumber.utils.dto;

import com.futurmaster.demandplanning.model.*;
import com.futurmaster.demandplanning.model.demandsensing.BestFitResponse;
import com.futurmaster.demandplanning.model.demandsensing.KPI;
import com.futurmaster.demandplanning.model.externalvariables.ExternalVariableSeries;
import com.futurmaster.demandplanning.model.targets.TargetDto;
import com.futurmaster.legacyclient.node.NodeResponse;
import com.futurmaster.legacyclient.node.TimeSeriesType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class NodeDtoCucumber {

    private String id;

    private String nodeId;

    private String alternativeId;

    private String code;

    private Long ver;

    private Date createdAt;

    private String createdBy;

    private Date lastModified;

    private String lastModifiedBy;

    private String description;

    private Integer version;

    private ChronoType chronology;

    private Boolean notUpToDate;

    private String comments;

    private String measureUnit;

    private ContinuationOfDto continuationOf;

    private DetectSettingDto detectSetting;

    private TargetDto target;

    private Map<ChronoType, Date> historicStartDateByChrono;

    private Map<ChronoType, Date> historicEndDateByChrono;

    private Map<ChronoType, Date> forecastStartDateByChrono;

    private Map<ChronoType, Date> forecastEndDateByChrono;

    private Map<ChronoType, Integer> horizonHistByChrono;

    private Map<ChronoType, Integer> horizonForecastByChrono;

    private Map<ChronoType, Map<String, Map<String, String>>> hyperParamsByChronoAndModel;

    private Map<ChronoType, Map<String, Double>> forecastabilityByChrono;

    private List<BestFitResponse> bestFits;

    private String configId;

    private List<String> errors;

    private Object[] warnings;

    private String model;

    private List<ExceptionalEventDto> exceptionalEvents;

    private List<SeasonalityDto> seasonalities;

    private List<ExternalVariableSeries> externalVariablesSeries;

    private Date forecastStartDate;

    private Date forecastEndDate;

    private Date historicEndDate;

    private Date historicStartDate;

    private Map<String, Double> forecastability;

    private Integer horizonForecast;

    private Integer horizonHist;

    private Map<String, String> hyperParams;

    private List<KPI> kpi;

    private List<OutlierDto> outliers;

    private List<PerimeterDto> perimeters;

    private Map<TimeSeriesType, NodeResponse.TimeSeries> timeSeries;

    private Date calcHistStartDate;

    private Date calcHistEndDate;

    private Date calcForecastStartDate;

    private Date calcForecastEndDate;

    private Boolean forecastSplitting;

    private Map<ChronoType, Boolean> forecastSplittingByChrono;

    private Map<String, Double[]> weeklySeasonalitiesValues;

    private Date lastForecastDate;

    private int hashCodeForecast;
}

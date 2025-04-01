package com.futurmaster.demandplanning.cucumber.node;

import com.futurmaster.demandplanning.cucumber.utils.CommonUtils;
import com.futurmaster.demandplanning.cucumber.utils.dto.NodeDtoCucumber;
import com.futurmaster.demandplanning.model.ChronoType;
import com.futurmaster.demandplanning.model.ExceptionalEventDto;
import com.futurmaster.demandplanning.model.PerimeterDto;
import com.futurmaster.legacyclient.node.NodeResponse;
import com.futurmaster.legacyclient.node.TimeSeriesType;
import io.cucumber.java.en.Then;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class ThenSteps extends CommonUtils {

    SimpleDateFormat SIMPLE_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");

    @Then("The response does not contain externalVariablesSeries without using {} model and contains the id: {} and the node id: {}")
    public void checkExternalVariablesSeriesForModel(final String model, final String id, final String nodeId) {
        NodeDtoCucumber actualNode = getActualObjectFromJson(NodeDtoCucumber.class);

        assertThat(actualNode.getModel()).isNotEqualTo(model);
        assertThat(actualNode.getExternalVariablesSeries()).isNull();
        assertThat(actualNode.getId()).isEqualTo(id);
        assertThat(actualNode.getNodeId()).isEqualTo(nodeId);
    }

    @Then("The response contains the perimeter:")
    public void checkAddPerimeter(final List<PerimeterDto> perimeterDto) {
        NodeDtoCucumber actualNode = getActualObjectFromJson(NodeDtoCucumber.class);

        actualNode.getPerimeters().forEach(perimeter -> perimeter.setId(null));
        assertThat(actualNode.getPerimeters()).isEqualTo(perimeterDto);
    }

    @Then("The response contains the id: {}, the node id: {} and the chronology: {}")
    public void checkNodeDtoIdsChronology(final String id, final String nodeId, final ChronoType chronology) {
        NodeDtoCucumber actualNode = getActualObjectFromJson(NodeDtoCucumber.class);

        assertThat(actualNode.getId()).isEqualTo(id);
        assertThat(actualNode.getNodeId()).isEqualTo(nodeId);
        assertThat(actualNode.getChronology()).isEqualTo(chronology);
    }

    @Then("The response contains the id: {}, the node id: {} and the alternative id: {}")
    public void checkNodeDtoIds(final String id, final String nodeId, final String altId) {
        NodeDtoCucumber actualNode = getActualObjectFromJson(NodeDtoCucumber.class);

        assertThat(actualNode.getId()).isEqualTo(id);
        assertThat(actualNode.getNodeId()).isEqualTo(nodeId);
        assertThat(actualNode.getAlternativeId()).isEqualTo(altId);
    }

    @Then("The response contains at least the following time series:")
    public void checkValueOfSeriesOnAlternative(final Map<TimeSeriesType, NodeResponse.TimeSeries> expectedTimeSeries) {
        NodeDtoCucumber actualNode = getActualObjectFromJson(NodeDtoCucumber.class);
        Map<TimeSeriesType, NodeResponse.TimeSeries> actualTimeSeries = actualNode.getTimeSeries();

        expectedTimeSeries.forEach((expectedTSKey, expectedTSValues) -> {
            NodeResponse.TimeSeries actualTSValues = actualTimeSeries.get(expectedTSKey);
            assertThat(expectedTSValues.getVersion()).isEqualTo(actualTSValues.getVersion());
            assertThat(expectedTSValues.getValues()).isEqualTo(actualTSValues.getValues());
        });
    }

    @Then("The response contains the id: {}, the node id: {} and the notUpToDate: {}")
    public void checkValueOfIdsAndNotUpToDate(final String id, final String nodeId, final Boolean notUpToDate) {
        NodeDtoCucumber actualNode = getActualObjectFromJson(NodeDtoCucumber.class);

        assertThat(actualNode.getId()).isEqualTo(id);
        assertThat(actualNode.getNodeId()).isEqualTo(nodeId);
        assertThat(actualNode.getNotUpToDate()).isEqualTo(notUpToDate);
    }

    @Then("The response does not contains any exceptionalEvents, perimeters and seasonalities")
    public void checkEmptyExceptionalEventsPerimetersAndSeasonalities() {
        NodeDtoCucumber actualNode = getActualObjectFromJson(NodeDtoCucumber.class);

        assertThat(actualNode.getExceptionalEvents()).isEmpty();
        assertThat(actualNode.getPerimeters()).isEmpty();
        assertThat(actualNode.getSeasonalities()).isEmpty();
    }

    @Then("The response contains {int} exceptionalEvent, perimeter and seasonality")
    public void checkNotEmptyExceptionalEventsPerimetersAndSeasonalities(final int number) {
        NodeDtoCucumber actualNode = getActualObjectFromJson(NodeDtoCucumber.class);

        assertThat(actualNode.getExceptionalEvents()).hasSize(number);
        assertThat(actualNode.getPerimeters()).hasSize(number);
        assertThat(actualNode.getSeasonalities()).hasSize(number);
    }

    @Then("The response contains the new perimeter:")
    public void checkPerimeterId(final List<PerimeterDto> perimeterDto) {
        NodeDtoCucumber actualNode = getActualObjectFromJson(NodeDtoCucumber.class);

        assertThat(actualNode.getPerimeters()).isEqualTo(perimeterDto);

    }

    @Then("The response contains the exceptional event:")
    public void checkExceptionalEvent(final List<ExceptionalEventDto> exceptionalEventDto) {
        NodeDtoCucumber actualNode = getActualObjectFromJson(NodeDtoCucumber.class);

        actualNode.getExceptionalEvents().stream().filter(ee -> ee.getValues().size() != 0).forEach(ee -> {
            ee.setId(null);
            ee.setEndDate(null);
            ee.setEffectPercentValues(null);
            ee.setCleansedEffectPercentValues(null);
        });

        assertThat(actualNode.getExceptionalEvents()).isEqualTo(exceptionalEventDto);
    }

    @Then("The response contains the id: {}, the node id: {} and the HistoricStartDate: {}")
    public void checkValueOfIdsAndHistoricStartDate(final String id, final String nodeId, final String historicStartDate) throws ParseException {
        NodeDtoCucumber actualNode = getActualObjectFromJson(NodeDtoCucumber.class);
        Date histoStartDate = SIMPLE_DATE_FORMAT.parse(historicStartDate);

        assertThat(actualNode.getId()).isEqualTo(id);
        assertThat(actualNode.getNodeId()).isEqualTo(nodeId);
        assertThat(actualNode.getHistoricStartDate()).isEqualTo(histoStartDate);
    }
}

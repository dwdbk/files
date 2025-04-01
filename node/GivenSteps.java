package com.futurmaster.demandplanning.cucumber.node;

import com.futurmaster.demandplanning.cucumber.utils.CommonUtils;
import com.futurmaster.demandplanning.model.ExceptionalEvent;
import com.futurmaster.demandplanning.model.Perimeter;
import com.futurmaster.demandplanning.model.Seasonality;
import com.github.tomakehurst.wiremock.client.WireMock;
import io.cucumber.java.en.Given;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;

import static com.futurmaster.demandplanning.cucumber.utils.FormatUtils.getDateValueOrNull;
import static com.futurmaster.demandplanning.cucumber.utils.stub.DemandSensingStubbing.stubDemandSensingChronology;
import static com.futurmaster.demandplanning.cucumber.utils.stub.KeycloakStubbing.stubKeycloak;
import static com.futurmaster.demandplanning.cucumber.utils.stub.LegacyWrapperStubbing.*;


public class GivenSteps extends CommonUtils {
    private static final SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");

    @Given("The database contains the Exceptional Event:")
    public void addExceptionalEventToDatabase(ExceptionalEvent exceptionalEvent)  {

        genericListNode.get(0).setExceptionalEvents(List.of(exceptionalEvent));
    }

    @Given("The database contains the Perimeter:")
    public void addPerimeterToDatabase(Perimeter perimeter) {

        genericListNode.get(0).setPerimeters(List.of(perimeter));
    }

    @Given("The database contains the seasonality:")
    public void addSeasonalityToDatabase(Seasonality seasonality) {

        genericListNode.get(0).setSeasonalities(List.of(seasonality));
    }

    @Given("The database contains the ValidatorDisabled as {}")
    public void addValidatorDisabledDatabase(final Boolean validatorDisabled) throws ParseException {
        genericListNode.get(0).setValidatorDisabled(validatorDisabled);
        mongoTemplate.save(genericListNode.get(0));

    }

    @Given("The database contains setHistoricStartDate as {}")
    public void setHistoricStartDateToDatabase(final String date) throws ParseException {
        genericListNode.get(0).setHistoricStartDate(getDateValueOrNull(date));
        mongoTemplate.save(genericListNode.get(0));
    }

    @Given("The system contains the generic preconditions of scenario 00 from node")
    public void genericPreconditionsOfNode() throws IOException {
        mongoTemplate.getDb().drop();
        WireMock.reset();
        cacheService.clearAllCaches();
        stubKeycloak();
        stubLegacyWrapperAggRules(genericAggRules);
        stubLegacyWrapperSelRules(genericSelRules);
        stubLegacyWrapperConfig(genericLWConfig);
        stubDemandSensingChronology(genericChronologies, genericModelsFileUri);
        addAlternative(genericListAlternative);
        genericDpConfig.setVer(null);
        initializeDPConfig(genericDpConfig);
        saveListSearchNode(genericListSearchNode);
        saveListNodes(genericListNode);
        createListTimeSeries(listTimeSeries);
        stubLegacyWrapperLoadNode(genereicNodeResponse);
        stubLegacyWrapperSaveNode(genereicNodeResponse);
        stubLegacyWrapperSaveNodes(List.of(genereicNodeResponse));
    }

}

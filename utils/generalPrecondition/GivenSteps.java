package com.futurmaster.demandplanning.cucumber.utils.generalPrecondition;

import com.futurmaster.demandplanning.cucumber.utils.CommonUtils;
import com.futurmaster.demandplanning.model.*;
import io.cucumber.java.en.Given;

import java.util.List;
import java.util.Map;

public class GivenSteps extends CommonUtils {

    @Given("The database contains the seasonality type:")
    public void addSeasonalityTypeToDatabase(List<SeasonalityType> seasonalityType) {
        super.addSeasonalityTypeToDatabase(seasonalityType);
    }
    @Given("The database contains the alternative:")
    public void addAlternative(List<Alternative> listAlternative) {
        super.addAlternative(listAlternative);
    }
    @Given("The demand planning config:")
    public void initializeDPConfig(DPConfig dpConfig) {
        super.initializeDPConfig(dpConfig);
    }
    @Given("The database contains the SearchNode:")
    public void saveListSearchNode(List<SearchNode> listSearchNode) {
        super.saveListSearchNode(listSearchNode);
    }
    @Given("The database contain the node:")
    public void saveListNodes(List<Node> listNode) {
        super.saveListNodes(listNode);
    }
    @Given("The database contains the Seasonality:")
    public void saveSeasonality(Map<String, Seasonality> seasonalities) {
        super.saveSeasonality(seasonalities);
    }

}

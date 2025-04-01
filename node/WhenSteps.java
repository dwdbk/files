package com.futurmaster.demandplanning.cucumber.node;

import com.futurmaster.demandplanning.cucumber.utils.CommonUtils;
import com.futurmaster.demandplanning.model.PerimeterDto;
import io.cucumber.java.en.When;

import static java.lang.String.format;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpMethod.POST;

public class WhenSteps extends CommonUtils {

    private final static String FETCH_NODE_WITH_AlTERNATIVE = "/nodes/%s?alternative=%s";

    private final static String FETCH_NODE_WITH_CHRONOLOGY = "/nodes/%s?chronology=%s";

    private final static String SAVE_PROPORTIONAL_FACTOR_ON_NODE = "/nodes/%s/perimeter";

    @When("The user with permission: {}, wants on node: {} the alternative: {}")
    public void getAlternativeOnNode(final String permission, final String nodeId, final String alternativeId) throws Exception {

        performRequest(GET, format(FETCH_NODE_WITH_AlTERNATIVE, nodeId, alternativeId), permission);

    }

    @When("The user with permission: {}, wants on node: {} the chronology: {}")
    public void getChronologyOnNode(final String permission, final String nodeId, final String chronology) throws Exception {

        performRequest(GET, format(FETCH_NODE_WITH_CHRONOLOGY, nodeId, chronology), permission);

    }

    @When("The user with permission: {}, wants on id: {} to save the perimeter:")
    public void savePerimeterOnNode(final String permission, final String id, final PerimeterDto perimeterDto) throws Exception {

        performRequest(POST, format(SAVE_PROPORTIONAL_FACTOR_ON_NODE, id), perimeterDto, permission);
    }

}

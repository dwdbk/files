package com.futurmaster.demandplanning.componenttest;

import com.c4_soft.springaddons.security.oauth2.test.annotations.keycloak.WithMockKeycloakAuth;
import com.futurmaster.demandplanning.model.*;
import com.futurmaster.demandplanning.model.externalvariables.ExternalVariable;
import com.futurmaster.demandplanning.utils.RolesConstants;
import com.futurmaster.legacyclient.node.NodeResponse;
import org.hamcrest.Matchers;
import org.hamcrest.core.IsNull;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.ResultActions;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.Date;
import java.util.List;
import java.util.Map;

import static com.futurmaster.demandplanning.componenttest.utils.CommonStubs.*;
import static com.futurmaster.demandplanning.model.ChronoType.DAILY;
import static com.futurmaster.legacyclient.node.TimeSeriesType.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@DisplayName("Node")
@WithMockKeycloakAuth({RolesConstants.ROLE_DP_MANAGE_CARDS})
class NodeTest extends AbstractComponentTest {
    private Node node;
    public static final String ALTERNATIVE_ID = "ALT_ID";
    private static final SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");

    private void prepareConfig() {
        Alternative defaultAlternative = Alternative.builder()
                .id("DEFAULT_ALTERNATIVE_ID")
                .name("DEFAULT_ALTERNATIVE")
                .isDefault(true)
                .version(0L)
                .originHistory("HISTORY")
                .build();

        Alternative alternative = Alternative.builder()
                .id(ALTERNATIVE_ID)
                .name("ALTERNATIVE_1")
                .isDefault(false)
                .version(2L)
                .originHistory("INDISTINCT_DATA_STORAGE_1")
                .build();

        DPConfig dpConfig = DPConfig.builder()
                .id("CONFIG_ID")
                .level(ConfigLevel.builder()
                        .name("CONFIG_NAME")
                        .priority(1)
                        .aggregate("PRODUCT")
                        .chronologies(List.of(ChronoType.DAILY, ChronoType.WEEKLY))
                        .build())
                .alternativesConfig(Map.of(ALTERNATIVE_ID, AlternativeConfig.builder()
                        .externalVariables(List.of(ExternalVariable.builder().title("Trip").timeSeriesType(EXTERNAL_VARIABLES_1).version(1).build()))
                        .build()))
                .build();

        node = Node.builder()
                .id("ID")
                .nodeId("NODE_ID")
                .alternativeId(ALTERNATIVE_ID)
                .code("NODE_CODE")
                .chronology(ChronoType.DAILY)
                .modelByChrono(Map.of(ChronoType.DAILY, "sarimax", ChronoType.WEEKLY, "demandsensing"))
                .configId(dpConfig.getId())
                .build();

        SearchNode searchNode = SearchNode.builder()
                .id("SEARCH_NODE_ID")
                .code("SEARCH_NODE_CODE")
                .nodeId("NODE_ID")
                .configId(dpConfig.getId())
                .build();


        mongoTemplate.insertAll(List.of(defaultAlternative, alternative));
        mongoTemplate.save(dpConfig);
        mongoTemplate.save(searchNode);
        mongoTemplate.save(node);
    }

    @Test
    @DisplayName("Scenario 1 : Fetch Node that does not exist")
    void fetchNodeThatDoesNotExist() throws Exception {
        // When
        ResultActions result = mockMvc.perform(get("/nodes/{id}", "NOT_EXIST"));

        // Then
        result.andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Scenario 2 : External variable must not appears when DS model is not used")
    void remove_ee_effect_when_DS_model_is_not_used() throws Exception {
        // Given
        stubLegacyConfig();
        stubNode();
        prepareConfig();

        // When
        ResultActions result = mockMvc.perform(get("/nodes/" + node.getNodeId()));

        // Then
        result.andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.nodeId").value("NODE_ID"))
                .andExpect(jsonPath("$.externalVariablesSeries").value(IsNull.nullValue()));
    }

    @Test
    @DisplayName("Scenario 3 : Fetch Node With alternativeId")
    void fetchNodeWithAlternativeId() throws Exception {
        // Given
        stubLegacyConfig();
        stubNode();
        prepareConfig();

        // When
        ResultActions result = mockMvc.perform(get("/nodes/" + node.getNodeId()).param("alternativeId", node.getAlternativeId()));

        // Then
        result.andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.nodeId").value(node.getNodeId()))
                .andExpect(jsonPath("$.alternativeId").value(ALTERNATIVE_ID))
                .andExpect(jsonPath("$.timeSeries.CLEANSED_HISTORY.version").value(2))
                .andExpect(jsonPath("$.timeSeries.CLEANSED_HISTORY.values").value(Matchers.contains(10.1, 0.0, 20.2)))
                .andExpect(jsonPath("$.timeSeries.HISTORY.values").value(Matchers.contains(10.10, 0.0, 20.20)));
    }

    @Test
    @DisplayName("Scenario 4 : Fetch Node With chronology")
    void fetchNodeWithChronology() throws Exception {
        // Given
        stubLegacyConfig();
        stubNode();
        prepareConfig();

        // When
        ResultActions result = mockMvc.perform(get("/nodes/" + node.getNodeId()).param("chronology", String.valueOf(ChronoType.WEEKLY)));

        // Then
        result.andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.nodeId").value(node.getNodeId()))
                .andExpect(jsonPath("$.chronology").value(ChronoType.WEEKLY.toString()))
                .andExpect(jsonPath("$.timeSeries.CLEANSED_HISTORY.version").value(2))
                .andExpect(jsonPath("$.timeSeries.CLEANSED_HISTORY.values").value(Matchers.contains(10.1, 0.0, 20.2)))
                .andExpect(jsonPath("$.timeSeries.HISTORY.values").value(Matchers.contains(10.1, 0.0, 20.2)));
    }

    @Test
    @DisplayName("Save proportional factor")
    void saveProportionalFactor() throws Exception {
        // Given
        stubLegacyConfig();
        stubNode();
        prepareConfig();

        // When
        PerimeterDto perimeterToSave = PerimeterDto.builder()
                .description("New perimeter")
                .date(DATE_FORMAT.parse("01/01/2021"))
                .value(10d)
                .build();

        ResultActions result = mockMvc.perform(post("/nodes/" + node.getId() + "/perimeter")
                .content(objectMapper.writeValueAsString(perimeterToSave))
                .contentType(MediaType.APPLICATION_JSON));

        // Then
        result.andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));

        Node savedNode = mongoTemplate.findById(node.getId(), Node.class);
        assertThat(savedNode).isNotNull();
        assertThat(savedNode.getPerimeters().get(0).getId()).isNotNull();
        assertThat(savedNode.getPerimeters())
                .extracting(Perimeter::getDescription, Perimeter::getDate, Perimeter::getValue, Perimeter::getStatus, Perimeter::isDynamic)
                .containsExactly(
                        tuple(perimeterToSave.getDescription(), perimeterToSave.getDate(), perimeterToSave.getValue(), Perimeter.Status.VALIDATED, false)
                );
    }

    @Test
    @DisplayName("Check if all items before legacy start date are deleted")
    void shouldDeleteItems_whenStartDateItemBeforeLegacyHistStartDate() throws Exception {
        // Given
        LocalDate startHistory = DAILY.toLocalDate(sdf.parse("01/01/2021"));
        LocalDate endHistory = DAILY.toLocalDate(sdf.parse("01/01/2022"));
        stubLegacyConfig(startHistory, endHistory, null, null);
        stubNode();
        prepareConfig();

        ExceptionalEvent ee1 = ExceptionalEvent.builder()
                .startDate(sdf.parse("08/12/2020"))
                .endDate(sdf.parse("08/12/2020"))
                .values(List.of(1.))
                .effectValues(List.of(10.))
                .build();
        Perimeter perimeter = Perimeter.builder()
                .date(sdf.parse("12/12/2020"))
                .value(1.)
                .status(Perimeter.Status.VALIDATED)
                .description("desc")
                .build();
        Seasonality seasonality = Seasonality.builder()
                .startDate(sdf.parse("01/12/2020"))
                .endDate(sdf.parse("08/12/2020"))
                .id("s1")
                .type(SeasonalityType.builder().title("SEAS1").build())
                .build();

        node.setPerimeters(List.of(perimeter));
        node.setExceptionalEvents(List.of(ee1));
        node.setSeasonalities(List.of(seasonality));
        node.setValidatorDisabled(true);
        mongoTemplate.save(node);

        // When
        ResultActions result = mockMvc.perform(get("/nodes/" + node.getNodeId()));

        // Then
        result.andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.nodeId").value("NODE_ID"))
                .andExpect(jsonPath("$.exceptionalEvents").isEmpty())
                .andExpect(jsonPath("$.perimeters").isEmpty())
                .andExpect(jsonPath("$.seasonalities").isEmpty())
                .andExpect(jsonPath("$.notUpToDate").value("true"));
    }

    @Test
    @DisplayName("Check if items before legacy start date are partially deleted")
    void shouldPartiallyDeleteItems_whenLegacyHistStartDateBetweenStartDateAndEndDateOfItem() throws Exception {
        // Given
        Date startDate = sdf.parse("01/01/2021");
        Date endDate = sdf.parse("10/01/2021");
        Double[] values = new Double[]{10.0, 20.0, 10.0, 20.0, null, null, 10.0, 20.0, 10.0, 20.0};
        List<NodeResponse.TimeSeries> ts = List.of(
                generateTimeSeries(HISTORY, startDate, endDate, values),
                generateTimeSeries(CLEANSED_HISTORY, startDate, endDate, values));
        stubNode(startDate, endDate, ts);
        stubLegacyConfig();
        prepareConfig();

        ExceptionalEvent ee1 = ExceptionalEvent.builder()
                .startDate(sdf.parse("26/12/2020"))
                .endDate(sdf.parse("04/01/2021"))
                .values(List.of(1., 2., 3., 4., 5., 6., 7., 8., 9., 10.))
                .effectValues(List.of(5., 5., 5., 5., 5., 5., 5., 5., 5., 5.))
                .build();
        Perimeter perimeter = Perimeter.builder()
                .id("p1")
                .date(sdf.parse("07/01/2021"))
                .value(1.)
                .status(Perimeter.Status.VALIDATED)
                .description("desc")
                .build();
        node.setPerimeters(List.of(perimeter));
        node.setExceptionalEvents(List.of(ee1));
        node.setValidatorDisabled(true);
        mongoTemplate.save(node);

        // When
        ResultActions result = mockMvc.perform(get("/nodes/" + node.getNodeId()));

        // Then
        result.andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.nodeId").value("NODE_ID"))
                .andExpect(jsonPath("$.exceptionalEvents[0].startDate").value("2021-01-01T00:00:00.000+00:00"))
                .andExpect(jsonPath("$.exceptionalEvents[0].values").isNotEmpty())
                .andExpect(jsonPath("$.exceptionalEvents[0].values").value(Matchers.contains(7., 8., 9., 10.)))
                .andExpect(jsonPath("$.exceptionalEvents[0].effectValues").value(Matchers.contains(3., 12., 1., 10.)))
                .andExpect(jsonPath("$.perimeters[0].id").value("p1"))
                .andExpect(jsonPath("$.notUpToDate").value("true"));
    }

    @Test
    @DisplayName("Check if histo start date is updated when legacy histo start date changes")
    void shouldUpdateHistStartDate_whenStartDateAfterLegacyHistStartDate() throws Exception {
        // Given
        Date oldHistStartDate = sdf.parse("01/01/2021");
        Date endDate = sdf.parse("10/01/2021");
        stubLegacyConfig(DAILY.toLocalDate(sdf.parse("05/01/2021")), null, null, null);
        Double[] values = new Double[]{10.0, 20.0, 10.0, 20.0, null, null, 10.0, 20.0, 10.0, 20.0};
        List<NodeResponse.TimeSeries> ts = List.of(
                generateTimeSeries(HISTORY, oldHistStartDate, endDate, values),
                generateTimeSeries(CLEANSED_HISTORY, oldHistStartDate, endDate, values),
                generateTimeSeries(EE_EFFECT, oldHistStartDate, endDate, new Double[]{null, null, null}));
        stubNode(oldHistStartDate, endDate, ts);
        prepareConfig();

        node.setHistoricStartDate(oldHistStartDate);
        mongoTemplate.save(node);

        // When
        ResultActions result = mockMvc.perform(get("/nodes/" + node.getNodeId()));

        // Then
        result.andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.nodeId").value("NODE_ID"))
                .andExpect(jsonPath("$.historicStartDate").value("2021-01-07T00:00:00.000+00:00"));
    }
}

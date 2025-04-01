package com.futurmaster.demandplanning.cucumber.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.futurmaster.demandplanning.model.*;
import com.futurmaster.demandplanning.model.demandsensing.KPI;
import com.futurmaster.demandplanning.service.CacheService;
import com.futurmaster.generated.FMProtoFMConfigOuterClass;
import com.futurmaster.legacyclient.node.NodeResponse;
import com.futurmaster.legacyclient.node.Rule;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.util.NestedServletException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.request;

@Slf4j
public class CommonUtils {

    @Autowired
    protected MockMvc mockMvcCucumber;

    @Autowired
    protected ObjectMapper objectMapper;

    @Autowired
    protected MongoTemplate mongoTemplate;
    @Autowired
    protected CacheService cacheService;

    protected static MockHttpServletRequestBuilder request;

    protected static ResultActions mockResponse;

    protected static String mockResponseContent;

    protected static String mockErrorMessage;

    protected static List<SeasonalityType> genericSeasonalityType;

    protected static List<Rule> genericAggRules;

    protected static List<Rule> genericSelRules;

    protected static FMProtoFMConfigOuterClass.FMProtoFMConfig genericLWConfig;

    protected static String genericModelsFileUri;

    protected static String genericChronologies;

    protected static List<KPI> genericListKPI;

    protected static List<Alternative> genericListAlternative;

    protected static DPConfig genericDpConfig;

    protected static List<SearchNode> genericListSearchNode;

    protected static List<Node> genericListNode;

    protected static Map<String, Seasonality> genericSeasonalities;

    protected static NodeResponse genereicNodeResponse;

    protected static List<NodeResponse.TimeSeries> listTimeSeries = new ArrayList<>();

    protected void performRequest(final HttpMethod method, final String url, final String role) throws Exception {
        performRequest(method, url, null, "tester", role);
    }

    protected void performRequest(final HttpMethod method, final String url, final Object body, final String role) throws Exception {
        performRequest(method, url, body, "tester", role);
    }

    protected void performRequest(final HttpMethod method, final String url, final Object body, final String name, final String role) throws Exception {
        request = request(HttpMethod.resolve(String.valueOf(method)), url);
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(new MockHttpServletRequest()));
        if (name != null) {
            //request = request.with(user(name).roles(role, "admin"));
            request = request.with(user(name).roles(role));
        }
        if (body != null) {
            request.contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(body));
        }

        try {
            CommonUtils.mockResponse = mockMvcCucumber.perform(request);
            CommonUtils.mockResponseContent = CommonUtils.mockResponse.andDo(MockMvcResultHandlers.print())
                    .andReturn().getResponse().getContentAsString();
            CommonUtils.mockErrorMessage = CommonUtils.mockResponse.andDo(MockMvcResultHandlers.print())
                    .andReturn().getResponse().getErrorMessage();
        } catch (NestedServletException e) {
            log.error("Failed to perform the request ",e);
        }
    }

    protected <T> T getActualObjectFromJson(Class<T> classType) {
        try {
            return new ObjectMapper().readValue(CommonUtils.mockResponseContent, classType);
        } catch (JsonProcessingException e) {
            Assertions.fail("Failed to transform JSON to Object "+ classType.getSimpleName(),e);
           // log.error("Failed to transform JSON to Object {}", classType, e);
        }
        return null;
    }
    protected <T> T getActualComplexObjectFromJson(TypeReference<T> typeRef2) {
        try {
            return new ObjectMapper().readValue(CommonUtils.mockResponseContent, typeRef2);
        } catch (JsonProcessingException e) {
            log.error("Failed to transform JSON to Complex Object {}", typeRef2, e);
        }
        return null;
    }

    public void addSeasonalityTypeToDatabase(List<SeasonalityType> seasonalityType) {
        genericSeasonalityType = seasonalityType;
        seasonalityType.forEach(seasonality -> mongoTemplate.insert(seasonality));
    }
    public void addAlternative(List<Alternative> listAlternative) {
        genericListAlternative = listAlternative;
        mongoTemplate.insertAll(listAlternative);
    }
    public void initializeDPConfig(DPConfig dpConfig) {
        genericDpConfig = dpConfig;
        mongoTemplate.save(genericDpConfig);
    }
    public void saveListSearchNode(List<SearchNode> listSearchNode) {
        genericListSearchNode = listSearchNode;
        mongoTemplate.insertAll(listSearchNode);
    }
    public void saveListNodes(List<Node> listNode) {
        genericListNode = listNode;
        mongoTemplate.insertAll(listNode);
    }
    public void saveSeasonality(Map<String, Seasonality> seasonalities) {
        genericSeasonalities = seasonalities;
        SeasonalityType type = genericSeasonalityType.stream().filter(seasonalityType -> seasonalityType.getId().equals(seasonalities.keySet().stream().findFirst().get())).findFirst().get();
        seasonalities.values().stream().findFirst().get().setType(type);
        genericListNode.get(0).setSeasonalities(List.of(seasonalities.values().stream().findFirst().get()));
        mongoTemplate.save(genericListNode.get(0));
    }


}

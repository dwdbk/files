package com.futurmaster.demandplanning.cucumber;


import com.futurmaster.demandplanning.DemandPlanningApplication;
import com.futurmaster.demandplanning.cucumber.utils.BeanConfiguration;
import io.cucumber.junit.Cucumber;
import io.cucumber.junit.CucumberOptions;
import io.cucumber.spring.CucumberContextConfiguration;
import org.junit.runner.RunWith;
import org.springframework.boot.test.autoconfigure.data.mongo.AutoConfigureDataMongo;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.test.context.ContextConfiguration;

import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;


@CucumberContextConfiguration
@SpringBootTest(
        classes = {DemandPlanningApplication.class}, webEnvironment = RANDOM_PORT)
@RunWith(Cucumber.class)
@AutoConfigureWireMock(port = 0)
@CucumberOptions(
        plugin = {"pretty", "html:target/cucumber-report/cucumber.html", "json:target/cucumber-report/cucumber.json"},
        features = "src/test/resources/features",
        glue = "com.futurmaster.demandplanning.cucumber")
@AutoConfigureDataMongo
@AutoConfigureMockMvc
@ContextConfiguration(classes = BeanConfiguration.class)
public class CucumberIntegTest {
}
package hooks;

import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.java.Scenario;
import utils.CucumberReportGenerator;

public class Hooks {
    @Before
    public void beforeScenario(Scenario scenario) {
        // Setup code if needed
    }

    @After
    public void afterScenario(Scenario scenario) {
        if (scenario.isFailed()) {
            // Add screenshot or additional logging for failed scenarios
        }
    }

    @After("@GenerateReport")
    public static void generateReport() {
        CucumberReportGenerator.generateReport();
    }
}

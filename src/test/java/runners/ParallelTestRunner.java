package runners;

import org.junit.platform.suite.api.*;

@Suite
@IncludeEngines("cucumber")
@SelectClasspathResource("features")
@ConfigurationParameters({
        @ConfigurationParameter(key = "cucumber.plugin",
                value = "pretty, json:target/cucumber.json, html:target/cucumber-reports/Cucumber.html"),
        @ConfigurationParameter(key = "cucumber.execution.parallel.enabled",
                value = "true"),
        @ConfigurationParameter(key = "cucumber.execution.parallel.config.strategy",
                value = "fixed"),
        @ConfigurationParameter(key = "cucumber.execution.parallel.config.fixed.parallelism",
                value = "4")
})
public class ParallelTestRunner {
}

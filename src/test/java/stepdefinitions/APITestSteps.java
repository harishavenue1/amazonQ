package stepdefinitions;

import io.cucumber.java.Before;
import io.cucumber.java.Scenario;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.restassured.RestAssured;
import io.restassured.config.HttpClientConfig;
import io.restassured.config.LogConfig;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.apache.http.params.CoreConnectionPNames;
import org.junit.jupiter.api.Assertions;

import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import static io.restassured.RestAssured.given;

public class APITestSteps {
    private static final int TIMEOUT = 10000;
    private static final String BASE_URI = "https://reqres.in/api";
    private static final ThreadLocal<RequestSpecification> REQUEST = ThreadLocal.withInitial(() -> null);
    private static final ThreadLocal<Response> RESPONSE = ThreadLocal.withInitial(() -> null);
    private static final ThreadLocal<Scenario> SCENARIO = ThreadLocal.withInitial(() -> null);

    @Before
    public void setUp(Scenario scenario) {
        SCENARIO.set(scenario);
        configureRestAssured();
    }

    private void configureRestAssured() {
        RestAssured.reset();
        RestAssured.config = RestAssured.config()
                .logConfig(LogConfig.logConfig().enableLoggingOfRequestAndResponseIfValidationFails())
                .httpClient(HttpClientConfig.httpClientConfig()
                        .setParam(CoreConnectionPNames.CONNECTION_TIMEOUT, TIMEOUT)
                        .setParam(CoreConnectionPNames.SO_TIMEOUT, TIMEOUT));
        RestAssured.useRelaxedHTTPSValidation();
    }

    @Given("I have a base URI")
    public void iHaveBaseURI() {
        try {
            RequestSpecification requestSpec = createBaseRequest();
            configureProxy(requestSpec);
            setRequest(requestSpec);
            logSuccess("Base URI set successfully");
        } catch (Exception e) {
            handleException("Failed to set base URI", e);
        }
    }

    private RequestSpecification createBaseRequest() {
        return given()
                .baseUri(BASE_URI)
                .contentType("application/json")
                .log().all();
    }

    private void configureProxy(RequestSpecification requestSpec) {
        String proxyHost = System.getProperty("http.proxyHost");
        String proxyPort = System.getProperty("http.proxyPort");

        if (proxyHost != null && proxyPort != null) {
            requestSpec.proxy(proxyHost, Integer.parseInt(proxyPort));
        }
    }

    @When("I send a {word} request to {string}")
    public void sendRequest(String method, String endpoint) {
        try {
            Response response = executeRequest(method, endpoint);
            setResponse(response);
            logResponseDetails(method, response);
        } catch (Exception e) {
            handleRequestException(method, endpoint, e);
        }
    }

    private Response executeRequest(String method, String endpoint) {
        try {
            // Validate inputs
            if (endpoint == null || endpoint.trim().isEmpty()) {
                throw new IllegalArgumentException("Endpoint cannot be null or empty");
            }

            RequestSpecification request = getRequest();
            if (request == null) {
                throw new IllegalStateException("Request specification not initialized");
            }

            // Log request details
            request.log().all();
            SCENARIO.get().log("Executing " + method + " request to: " + endpoint);

            // Execute request based on method
            Response response = switch (method.toUpperCase()) {
                case "GET" -> request
                        .when()
                        .get(endpoint);
                case "POST" -> request
                        .when()
                        .post(endpoint);
                case "PUT" -> request
                        .when()
                        .put(endpoint);
                case "DELETE" -> request
                        .when()
                        .delete(endpoint);
                case "PATCH" -> request
                        .when()
                        .patch(endpoint);
                default -> throw new IllegalArgumentException("Unsupported HTTP method: " + method);
            };

            // Log response details
            response.then().log().all();
            SCENARIO.get().log("Response received with status code: " + response.getStatusCode());

            return response;
        } catch (IllegalArgumentException e) {
            SCENARIO.get().log("Invalid request parameters: " + e.getMessage());
            throw e;
        } catch (Exception e) {
            String errorMessage = String.format("Failed to execute %s request to %s: %s",
                    method, endpoint, e.getMessage());
            SCENARIO.get().log(errorMessage);
            throw new RuntimeException(errorMessage, e);
        }
    }


    @Then("the response status code should be {int}")
    public void verifyStatusCode(int expectedStatusCode) {
        try {
            int actualStatusCode = getResponse().getStatusCode();
            Assertions.assertEquals(expectedStatusCode, actualStatusCode,
                    String.format("Expected status code %d but got %d", expectedStatusCode, actualStatusCode));
            logSuccess("Status code validation successful");
        } catch (Exception e) {
            handleException("Status code validation failed", e);
        }
    }

    @Given("I have the following request body:")
    public void setRequestBody(String requestBody) {
        try {
            setRequest(getRequest().body(requestBody));
            logSuccess("Request body set: " + requestBody);
        } catch (Exception e) {
            handleException("Failed to set request body", e);
        }
    }

    @Then("the response should have field {string} with value {string}")
    public void verifyResponseField(String field, String expectedValue) {
        try {
            String actualValue = getResponse().jsonPath().getString(field);
            Assertions.assertEquals(expectedValue, actualValue,
                    String.format("Expected value '%s' for field '%s' but got '%s'",
                            expectedValue, field, actualValue));
            logSuccess("Field validation successful for " + field);
        } catch (Exception e) {
            handleException("Field validation failed", e);
        }
    }

    private void logSuccess(String message) {
        SCENARIO.get().log(message);
    }

    private void logResponseDetails(String method, Response response) {
        SCENARIO.get().log(String.format("%s Response Status Code: %d",
                method, response.getStatusCode()));
        SCENARIO.get().log(String.format("%s Response Body: %s",
                method, response.getBody().asPrettyString()));
    }

    private void handleException(String message, Exception e) {
        SCENARIO.get().log(message + ": " + e.getMessage());
        throw new RuntimeException(message, e);
    }

    private void handleRequestException(String method, String endpoint, Exception e) {
        String message = String.format("Failed to send %s request to %s", method, endpoint);
        SCENARIO.get().log(message + ": " + e.getMessage());
        SCENARIO.get().log("Request details: " + getRequest().log().toString());
        throw new RuntimeException(message, e);
    }

    private void setRequest(RequestSpecification request) {
        REQUEST.set(request);
    }

    private RequestSpecification getRequest() {
        return REQUEST.get();
    }

    private void setResponse(Response response) {
        RESPONSE.set(response);
    }

    private Response getResponse() {
        return RESPONSE.get();
    }

    @Then("the response should contain {string}")
    public void the_response_should_contain(String expectedText) {
        try {
            String responseBody = getResponse().getBody().asString();
            Assertions.assertTrue(responseBody.contains(expectedText),
                    String.format("Expected response to contain '%s' but was not found in: %s",
                            expectedText, responseBody));
            logSuccess("Response contains expected text: " + expectedText);
        } catch (Exception e) {
            handleException("Failed to verify response content", e);
        }
    }

    @Then("the response should have field {string} at index {int} with value {string}")
    public void the_response_should_have_field_at_index_with_value(String field, Integer index, String expectedValue) {
        try {
            // First, let's log the actual response for debugging
            String responseBody = getResponse().getBody().asPrettyString();
            SCENARIO.get().log("Response Body: " + responseBody);

            // Get the data array first
            List<Map<String, Object>> dataArray = getResponse().jsonPath().getList("data");

            // Validate array bounds
            if (dataArray == null || index >= dataArray.size()) {
                throw new IllegalArgumentException(
                        String.format("Index %d is out of bounds for data array of size %d",
                                index, (dataArray == null ? 0 : dataArray.size()))
                );
            }

            // Get the specific field value using JsonPath
            String actualValue = getResponse().jsonPath().getString("data[" + index + "]." + field.replace("data.", ""));

            // Perform the assertion
            Assertions.assertEquals(expectedValue, actualValue,
                    String.format("Expected value '%s' for field '%s' at index %d but got '%s'",
                            expectedValue, field, index, actualValue));

            logSuccess(String.format("Field '%s' at index %d has expected value '%s'",
                    field, index, expectedValue));
        } catch (Exception e) {
            handleException(String.format("Failed to verify field '%s' at index %d", field, index), e);
        }
    }

    @Then("the response should have {int} items in data array")
    public void the_response_should_have_items_in_data_array(Integer expectedCount) {
        try {
            List<Object> dataArray = getResponse().jsonPath().getList("data");
            int actualCount = dataArray != null ? dataArray.size() : 0;

            Assertions.assertEquals(expectedCount, actualCount,
                    String.format("Expected %d items in data array but found %d",
                            expectedCount, actualCount));
            logSuccess(String.format("Data array contains expected %d items", expectedCount));
        } catch (Exception e) {
            handleException("Failed to verify data array size", e);
        }
    }

}

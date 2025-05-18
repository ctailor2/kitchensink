package com.mongodbmodfactory.kitchensink_boot.member;

import io.github.bonigarcia.wdm.WebDriverManager;
import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.json.JsonValue;
import jakarta.json.stream.JsonParser;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.StringReader;
import java.time.Duration;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class MemberSeleniumIT {

    private WebDriver driver;
    private WebDriverWait wait;
    private String baseUrl = "http://localhost:8080/kitchensink";

    @BeforeEach
    public void setup() {
        WebDriverManager.chromedriver().setup();
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless"); // Run in headless mode
        driver = new ChromeDriver(options);
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        driver.get(baseUrl);
    }

    @AfterEach
    public void teardown() {
        if (driver != null) {
            driver.quit();
        }
    }

    @Test
    public void testSuccessfulMemberRegistration() {
        // Fill in the registration form
        driver.findElement(By.id("reg:name")).sendKeys("John Doe");
        String email = "john" + System.currentTimeMillis() + "@test.com";
        driver.findElement(By.id("reg:email")).sendKeys(email);
        driver.findElement(By.id("reg:phoneNumber")).sendKeys("1234567890");

        // Submit the form
        driver.findElement(By.id("reg:register")).click();

        // Wait for success message
        WebElement successMessage = wait.until(ExpectedConditions.presenceOfElementLocated(
                By.cssSelector(".messages")));
        assertTrue(successMessage.getText().contains("Registered!"));

        // Verify member appears in the table
        WebElement memberTable = driver.findElement(By.className("simpletablestyle"));
        assertTrue(memberTable.getText().contains(email));
    }

    @Test
    public void testInvalidNameRegistration() {
        // Test name with numbers (should fail)
        driver.findElement(By.id("reg:name")).sendKeys("John123");
        driver.findElement(By.id("reg:email")).sendKeys("john@test.com");
        driver.findElement(By.id("reg:phoneNumber")).sendKeys("1234567890");

        // Submit the form
        driver.findElement(By.id("reg:register")).click();

        // Wait for error message
        WebElement errorMessage = wait.until(ExpectedConditions.presenceOfElementLocated(
                By.cssSelector(".invalid")));
        assertTrue(errorMessage.getText().contains("Must not contain numbers"));
    }

    @Test
    public void testInvalidEmailRegistration() {
        // Test invalid email format
        driver.findElement(By.id("reg:name")).sendKeys("John Doe");
        driver.findElement(By.id("reg:email")).sendKeys("invalid-email");
        driver.findElement(By.id("reg:phoneNumber")).sendKeys("1234567890");

        // Submit the form
        driver.findElement(By.id("reg:register")).click();

        // Wait for error message
        WebElement errorMessage = wait.until(ExpectedConditions.presenceOfElementLocated(
                By.cssSelector(".invalid")));
        assertTrue(errorMessage.getText().contains("must be a well-formed email address"));
    }

    @Test
    public void testInvalidPhoneRegistration() {
        // Test phone number too short
        driver.findElement(By.id("reg:name")).sendKeys("John Doe");
        driver.findElement(By.id("reg:email")).sendKeys("john@test.com");
        driver.findElement(By.id("reg:phoneNumber")).sendKeys("123");

        // Submit the form
        driver.findElement(By.id("reg:register")).click();

        // Wait for error message
        WebElement errorMessage = wait.until(ExpectedConditions.presenceOfElementLocated(
                By.cssSelector(".invalid")));
        assertTrue(errorMessage.getText().contains("size must be between 10 and 12"));
    }

    @Test
    public void testDuplicateEmailRegistration() {
        String email = "duplicate" + System.currentTimeMillis() + "@test.com";

        // Register first member
        driver.findElement(By.id("reg:name")).sendKeys("John Doe");
        driver.findElement(By.id("reg:email")).sendKeys(email);
        driver.findElement(By.id("reg:phoneNumber")).sendKeys("1234567890");
        driver.findElement(By.id("reg:register")).click();

        // Wait for success message and clear form
        wait.until(ExpectedConditions.presenceOfElementLocated(
                By.cssSelector(".messages")));

        // Try to register second member with same email
        driver.findElement(By.id("reg:name")).sendKeys("Jane Doe");
        driver.findElement(By.id("reg:email")).sendKeys(email);
        driver.findElement(By.id("reg:phoneNumber")).sendKeys("0987654321");
        driver.findElement(By.id("reg:register")).click();

        // Wait for error message
        WebElement errorMessage = wait.until(ExpectedConditions.presenceOfElementLocated(
                By.cssSelector(".invalid")));
        assertTrue(errorMessage.getText().contains("Unique index or primary key violation"));
    }

    @Test
    public void testRestUrlLink() {
        // Register a new member
        String name = "REST Test User";
        driver.findElement(By.id("reg:name")).sendKeys(name);
        String email = "rest" + System.currentTimeMillis() + "@test.com";
        driver.findElement(By.id("reg:email")).sendKeys(email);
        String phoneNumber = "1234567890";
        driver.findElement(By.id("reg:phoneNumber")).sendKeys(phoneNumber);
        driver.findElement(By.id("reg:register")).click();

        // Wait for success message
        wait.until(ExpectedConditions.presenceOfElementLocated(
                By.cssSelector(".messages")));

        // Find the REST URL in the table
        WebElement memberTable = driver.findElement(By.className("simpletablestyle"));
        List<WebElement> rows = memberTable.findElements(By.tagName("tr"));
        WebElement memberTableRow = rows.stream().filter((row) -> row.getText().contains(email)).findFirst().orElseThrow();
        List<WebElement> rowData = memberTableRow.findElements(By.tagName("td"));
        WebElement restUrlDatum = rowData.get(rowData.size() - 1);
        restUrlDatum.findElement(By.tagName("a")).click();
        String memberRestUrlJson = driver.findElement(By.cssSelector("body pre")).getText();
        JsonParser jsonParser = Json.createParser(new StringReader(memberRestUrlJson));
        jsonParser.next();
        JsonObject jsonObject = jsonParser.getObject();
        assertEquals(name, jsonObject.getString("name"));
        assertEquals(email, jsonObject.getString("email"));
        assertEquals(phoneNumber, jsonObject.getString("phoneNumber"));
    }

    @Test
    public void testMembersLink() {
        // Register a new member
        String name = "REST Test User";
        driver.findElement(By.id("reg:name")).sendKeys(name);
        String email = "rest" + System.currentTimeMillis() + "@test.com";
        driver.findElement(By.id("reg:email")).sendKeys(email);
        String phoneNumber = "1234567890";
        driver.findElement(By.id("reg:phoneNumber")).sendKeys(phoneNumber);
        driver.findElement(By.id("reg:register")).click();

        // Wait for success message
        wait.until(ExpectedConditions.presenceOfElementLocated(
                By.cssSelector(".messages")));

        // Click the members link
        driver.findElement(By.linkText("/rest/members")).click();
        String membersJson = driver.findElement(By.cssSelector("body pre")).getText();
        JsonParser jsonParser = Json.createParser(new StringReader(membersJson));
        jsonParser.next();
        JsonObject jsonObject = jsonParser.getArrayStream()
                .filter((jsonValue) -> jsonValue.asJsonObject().getString("email").equals(email))
                .findFirst()
                .map(JsonValue::asJsonObject)
                .orElseThrow();;
        assertEquals(name, jsonObject.getString("name"));
        assertEquals(email, jsonObject.getString("email"));
        assertEquals(phoneNumber, jsonObject.getString("phoneNumber"));
    }
}
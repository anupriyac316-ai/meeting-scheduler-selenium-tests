package com.scheduler.selenium;

import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class DashboardFlowTest extends SeleniumTestBase {

    private static final String EMAIL = "selenium.test@example.com";
    private static final String PASSWORD = "TestPass123!";

    private WebDriverWait waitFor() {
        return new WebDriverWait(driver, Duration.ofSeconds(10));
    }

    private void setFieldValue(WebElement field, String value) {
        ((JavascriptExecutor) driver).executeScript(
                "arguments[0].value = arguments[1]; " +
                "arguments[0].dispatchEvent(new Event('input', { bubbles: true })); " +
                "arguments[0].dispatchEvent(new Event('change', { bubbles: true }));",
                field, value);
    }

    @Test
    @Order(1)
    void registerNewUser() {
        driver.get(baseUrl() + "/register");

        driver.findElement(By.name("name")).sendKeys("Selenium Tester");
        driver.findElement(By.name("email")).sendKeys(EMAIL);
        driver.findElement(By.name("password")).sendKeys(PASSWORD);
        driver.findElement(By.cssSelector("button[type=submit]")).click();

        waitFor().until(ExpectedConditions.urlContains("/login"));
    }

    @Test
    @Order(2)
    void loginRedirectsToDashboard() {
        driver.get(baseUrl() + "/login");

        driver.findElement(By.name("username")).sendKeys(EMAIL);
        driver.findElement(By.name("password")).sendKeys(PASSWORD);
        driver.findElement(By.cssSelector("button[type=submit]")).click();

        waitFor().until(ExpectedConditions.urlContains("/dashboard"));
        assertTrue(driver.getCurrentUrl().contains("/dashboard"));
    }

    @Test
    @Order(3)
    void scheduleMeetingAppearsInList() {
        driver.get(baseUrl() + "/dashboard");

        String futureDate = LocalDate.now().plusDays(3).format(DateTimeFormatter.ISO_DATE); // yyyy-MM-dd

        driver.findElement(By.id("title")).sendKeys("Selenium Sync Meeting");

        WebElement dateField = driver.findElement(By.id("date"));
        setFieldValue(dateField, futureDate);

        WebElement timeField = driver.findElement(By.id("startTime"));
        setFieldValue(timeField, "10:30");

        driver.findElement(By.id("durationMinutes")).sendKeys("30");
        driver.findElement(By.cssSelector(".btn-primary.full")).click();

        waitFor().until(ExpectedConditions.urlContains("/dashboard"));
        waitFor().until(ExpectedConditions.textToBePresentInElementLocated(
                By.cssSelector(".main"), "Selenium Sync Meeting"));

        assertTrue(driver.getPageSource().contains("Selenium Sync Meeting"));
    }

    @Test
    @Order(4)
    void editMeetingUpdatesTitle() {
        driver.get(baseUrl() + "/dashboard");

        WebElement editBtn = waitFor().until(ExpectedConditions.elementToBeClickable(
                By.cssSelector(".btn-edit")));
        editBtn.click();

        WebElement titleField = waitFor().until(ExpectedConditions.visibilityOfElementLocated(
                By.id("editTitle")));
        titleField.clear();
        titleField.sendKeys("Selenium Sync Meeting - Updated");

        driver.findElement(By.cssSelector("#editForm .btn-primary")).click();

        waitFor().until(ExpectedConditions.textToBePresentInElementLocated(
                By.cssSelector(".main"), "Selenium Sync Meeting - Updated"));

        assertTrue(driver.getPageSource().contains("Selenium Sync Meeting - Updated"));
    }

    @Test
    @Order(5)
    void cancelMeetingMovesItToCancelledList() {
        driver.get(baseUrl() + "/dashboard");

        driver.findElement(By.cssSelector("[data-target='cancel']")).click();

        WebElement cancelBtn = waitFor().until(ExpectedConditions.elementToBeClickable(
                By.cssSelector(".btn-cancel")));
        cancelBtn.click();

        WebElement reasonField = waitFor().until(ExpectedConditions.visibilityOfElementLocated(
                By.id("reason")));
        reasonField.sendKeys("No longer needed - automated test");

        driver.findElement(By.cssSelector("#cancelForm .btn-danger")).click();

        waitFor().until(ExpectedConditions.urlContains("/dashboard"));
        driver.findElement(By.cssSelector("[data-target='cancel']")).click();

        waitFor().until(ExpectedConditions.textToBePresentInElementLocated(
                By.cssSelector(".main"), "No longer needed - automated test"));

        assertTrue(driver.getPageSource().contains("badge-cancelled"));
    }

    @Test
    @Order(6)
    void deleteAccountRequiresTypedConfirmationAndRedirectsHome() {
        driver.get(baseUrl() + "/dashboard");

        driver.findElement(By.xpath("//button[contains(text(),'Delete account')]")).click();

        WebElement confirmBtn = waitFor().until(ExpectedConditions.visibilityOfElementLocated(
                By.id("confirmDeleteBtn")));
        assertFalse(confirmBtn.isEnabled(), "Delete button should be disabled before typing DELETE");

        WebElement confirmInput = driver.findElement(By.id("deleteConfirmText"));
        confirmInput.sendKeys("wrong");
        assertFalse(confirmBtn.isEnabled(), "Delete button should stay disabled on wrong text");

        confirmInput.clear();
        confirmInput.sendKeys("DELETE");
        waitFor().until(ExpectedConditions.elementToBeClickable(confirmBtn));

        confirmBtn.click();

        waitFor().until(ExpectedConditions.urlContains("/home"));
        assertTrue(driver.getCurrentUrl().contains("accountDeleted"));
    }

    @Test
    @Order(7)
    void deletedUserCannotLoginAgain() {
        driver.get(baseUrl() + "/login");

        driver.findElement(By.name("username")).sendKeys(EMAIL);
        driver.findElement(By.name("password")).sendKeys(PASSWORD);
        driver.findElement(By.cssSelector("button[type=submit]")).click();

        waitFor().until(ExpectedConditions.urlContains("/login"));
        assertTrue(driver.getCurrentUrl().contains("error") || driver.getCurrentUrl().equals(baseUrl() + "/login"));
    }
}
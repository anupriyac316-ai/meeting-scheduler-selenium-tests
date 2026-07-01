package com.scheduler.selenium;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.edge.EdgeOptions;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;

import java.io.File;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public abstract class SeleniumTestBase {

    @LocalServerPort
    protected int port;

    protected static EdgeDriver driver;

    @BeforeAll
    static void setUpDriver() {
        // Point directly at a manually downloaded msedgedriver.exe,
        // bypassing Selenium Manager's broken auto-resolution (azureedge.net is dead).
        // Using a path relative to the project root, resolved via user.dir,
        // so it works regardless of which drive/folder the project lives in.
        String projectRoot = System.getProperty("user.dir");
        String driverPath = projectRoot + File.separator + "drivers" + File.separator + "msedgedriver.exe";

        System.setProperty("webdriver.edge.driver", driverPath);

        EdgeOptions options = new EdgeOptions();
        options.addArguments("--headless=new"); // remove this line to watch the browser run
        options.addArguments("--window-size=1400,1000");
        options.addArguments("--disable-gpu");
        options.addArguments("--no-sandbox");
        driver = new EdgeDriver(options);
    }

    @AfterAll
    static void tearDownDriver() {
        if (driver != null) driver.quit();
    }

    protected String baseUrl() {
        return "http://localhost:" + port;
    }
}
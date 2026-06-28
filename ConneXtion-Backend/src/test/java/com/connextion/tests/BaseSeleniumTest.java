package com.connextion.tests;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.edge.EdgeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

public abstract class BaseSeleniumTest {

    protected static final String BASE_URL = System.getProperty(
            "app.base.url",
            "http://localhost:8080/ConneXtion-Frontend"
    );

    protected static final int DEFAULT_TIMEOUT_SECONDS = 15;

    protected WebDriver driver;
    protected WebDriverWait wait;

    @BeforeAll
    static void configureDriver() {
        WebDriverManager.edgedriver().setup();
    }

    @BeforeEach
    void RunBrowser() {
        EdgeOptions options = new EdgeOptions();
        options.addArguments("--window-size=1400,900");
        options.addArguments("--disable-notifications");
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-dev-shm-usage");

        driver = new EdgeDriver(options);
        wait = new WebDriverWait(driver, Duration.ofSeconds(DEFAULT_TIMEOUT_SECONDS));
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(5));
    }

    @AfterEach
    void closeBrowser() {
        if (driver != null) {
            driver.quit();
        }
    }

    protected void navegateTo(String page) {
        driver.get(BASE_URL + "/" + page);
    }

    protected void Loggin(String email, String password) {
        navegateTo("index.html");

        WebElement campoEmail = wait.until(
                ExpectedConditions.visibilityOfElementLocated(By.id("email"))
        );
        campoEmail.clear();
        campoEmail.sendKeys(email);

        WebElement campoPassword = driver.findElement(By.id("password"));
        campoPassword.clear();
        campoPassword.sendKeys(password);

        driver.findElement(By.cssSelector("button.btn")).click();

        wait.until(ExpectedConditions.urlContains("dashboard.html"));
    }

    protected WebElement waitVisible(By localizador) {
        return wait.until(ExpectedConditions.visibilityOfElementLocated(localizador));
    }

    protected WebElement waitClickeable(By localizador) {
        return wait.until(ExpectedConditions.elementToBeClickable(localizador));
    }

    protected void fullSpace(String id, String texto) {
        WebElement campo = waitVisible(By.id(id));
        campo.clear();
        campo.sendKeys(texto);
    }

    protected void SelectOption(String id, String value) {
        WebElement selectElement = waitVisible(By.id(id));
        new Select(selectElement).selectByValue(value);
    }

    protected void setSessionStorage(String clave, String valor) {
        ((JavascriptExecutor) driver)
                .executeScript("sessionStorage.setItem(arguments[0], arguments[1]);", clave, valor);
    }

    protected void stop(long milisegundos) {
        try {
            Thread.sleep(milisegundos);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}

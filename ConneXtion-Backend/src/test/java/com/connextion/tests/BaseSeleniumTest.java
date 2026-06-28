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

    /**
     * URL raíz del frontend. Configurable vía -Dapp.base.url=...
     */
    protected static final String BASE_URL = System.getProperty(
            "app.base.url",
            "http://localhost:8080/ConneXtion-Frontend" // ← así
    );

    /**
     * Tiempo de espera estándar para elementos dinámicos (segundos).
     */
    protected static final int DEFAULT_TIMEOUT_SECONDS = 15;

    protected WebDriver driver;
    protected WebDriverWait wait;

    // -------------------------------------------------------------------------
    // Ciclo de vida del WebDriver
    // -------------------------------------------------------------------------
    @BeforeAll
    static void configureDriver() {
        WebDriverManager.edgedriver().setup();
    }

    @BeforeEach
    void RunBrowser() {
        EdgeOptions options = new EdgeOptions();     // ← EdgeOptions
        options.addArguments("--window-size=1400,900");
        options.addArguments("--disable-notifications");
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-dev-shm-usage");

        driver = new EdgeDriver(options);            // ← EdgeDriver
        wait = new WebDriverWait(driver, Duration.ofSeconds(DEFAULT_TIMEOUT_SECONDS));
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(5));
    }

    @AfterEach
    void closeBrowser() {
        if (driver != null) {
            driver.quit();
        }
    }

    // -------------------------------------------------------------------------
    // Métodos auxiliares compartidos
    // -------------------------------------------------------------------------
    /**
     * Navega a una página relativa a la URL base del frontend.
     *
     * @param page nombre del archivo HTML (ej. "index.html")
     */
    protected void navegateTo(String page) {
        driver.get(BASE_URL + "/" + page);
    }

    /**
     * Realiza el flujo completo de inicio de sesión desde index.html.
     *
     * @param email correo electrónico del usuario
     * @param password contraseña del usuario
     */
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

        // Esperar a que la redirección al dashboard se complete.
        wait.until(ExpectedConditions.urlContains("dashboard.html"));
    }

    /**
     * Espera a que un elemento sea visible y lo devuelve.
     *
     * @param localizador selector del elemento
     * @return WebElement encontrado
     */
    protected WebElement waitVisible(By localizador) {
        return wait.until(ExpectedConditions.visibilityOfElementLocated(localizador));
    }

    /**
     * Espera a que un elemento sea clickeable y lo devuelve.
     *
     * @param localizador selector del elemento
     * @return WebElement encontrado
     */
    protected WebElement waitClickeable(By localizador) {
        return wait.until(ExpectedConditions.elementToBeClickable(localizador));
    }

    /**
     * Llena un campo de texto por su id, limpiando el contenido previo.
     *
     * @param id atributo id del campo
     * @param texto texto a escribir
     */
    protected void fullSpace(String id, String texto) {
        WebElement campo = waitVisible(By.id(id));
        campo.clear();
        campo.sendKeys(texto);
    }

    /**
     * Selecciona una opción de un elemento {@code <select>} por su value.
     *
     * @param id atributo id del select
     * @param value valor de la opción a seleccionar
     */
    protected void SelectOption(String id, String value) {
        WebElement selectElement = waitVisible(By.id(id));
        new Select(selectElement).selectByValue(value);
    }

    /**
     * Inyecta un valor en sessionStorage vía JavaScript. Útil para simular
     * sesiones ya establecidas en páginas que leen sessionStorage al cargar
     * (sin pasar por el flujo de login completo).
     *
     * @param clave nombre de la clave
     * @param valor valor a almacenar
     */
    protected void setSessionStorage(String clave, String valor) {
        ((JavascriptExecutor) driver)
                .executeScript("sessionStorage.setItem(arguments[0], arguments[1]);", clave, valor);
    }

    /**
     * Pausa la ejecución por el tiempo indicado. Usar sólo cuando no existe una
     * condición de espera más precisa.
     *
     * @param milisegundos tiempo a esperar
     */
    protected void stop(long milisegundos) {
        try {
            Thread.sleep(milisegundos);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}

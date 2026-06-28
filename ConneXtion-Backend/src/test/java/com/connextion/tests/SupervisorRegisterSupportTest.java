package com.connextion.tests;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * CU7 / CU8 – Prueba automatizada #2
 *
 * Flujo: 1. Iniciar sesión como supervisor (dario / 123456). 2. Verificar que
 * el dashboard muestra el rol SUPERVISOR. 3. Navegar al formulario de registro
 * de soportistas. 4. Completar todos los campos requeridos. 5. Seleccionar al
 * menos un servicio. 6. Enviar el formulario. 7. Verificar el mensaje de éxito.
 *
 * Credenciales del supervisor de prueba: email : dario password : 123456
 */
@DisplayName("CP-02: Login como supervisor y registro de soportista")
class SupervisorRegisterSupportTest extends BaseSeleniumTest {

    private static final String EMAIL_SUPERVISOR = "dario";
    private static final String PASSWORD_SUPERVISOR = "123456";

    private static final String SUFIJO
            = DateTimeFormatter.ofPattern("MMddHHmmss").format(LocalDateTime.now());

    private static final String NAME_SUPPORT = "SeleniumTest";
    private static final String LAST_NAME1 = "Prueba";
    private static final String LAST_NAME2 = "Automatica";
    private static final String EMAIL_SUPPORT = "auto.soportista." + SUFIJO + "@connextion.test";
    private static final String PASSWORD_SUPPORT = "Test1234";

    @Test
    @DisplayName("Debe iniciar sesión como supervisor y registrar un nuevo soportista")
    void testSupervisorRegistraSoportista() {

        Loggin(EMAIL_SUPERVISOR, PASSWORD_SUPERVISOR);

        WebElement badgeRol = waitVisible(By.id("roleBadge"));
        assertEquals("Supervisor", badgeRol.getText().trim(),
                "El badge debe indicar 'Supervisor' para este usuario");
        WebElement tarjetaRegistrar = waitClickeable(By.id("cardRegisterSupport"));
        tarjetaRegistrar.click();

        wait.until(ExpectedConditions.urlContains("support-register.html"));
        assertTrue(driver.getCurrentUrl().contains("support-register.html"),
                "Debe redirigir al formulario de registro de soportistas");
        fullSpace("name", NAME_SUPPORT);
        fullSpace("firstSurname", LAST_NAME1);
        fullSpace("secondSurname", LAST_NAME2);
        fullSpace("email", EMAIL_SUPPORT);
        fullSpace("password", PASSWORD_SUPPORT);
        WebElement checkSupervisor = driver.findElement(By.id("isSupervisor"));
        if (checkSupervisor.isSelected()) {
            checkSupervisor.click();
        }
        assertFalse(checkSupervisor.isSelected(),
                "El checkbox de supervisor no debe estar marcado para un soportista regular");
        List<WebElement> checkboxesServicio = wait.until(driver -> {
            List<WebElement> checks = driver.findElements(
                    By.cssSelector("#servicesGrid input[type='checkbox']")
            );
            return checks.isEmpty() ? null : checks;
        });

        assertFalse(checkboxesServicio.isEmpty(),
                "Deben existir servicios disponibles para asignar al soportista");

        WebElement primerServicio = checkboxesServicio.get(0);
        if (!primerServicio.isSelected()) {
            primerServicio.click();
        }
        assertTrue(primerServicio.isSelected(),
                "Al menos un servicio debe quedar seleccionado");

        driver.findElement(By.cssSelector("button.btn")).click();

        WebDriverWait waitCorto = new WebDriverWait(driver, Duration.ofSeconds(5));
        WebElement mensajeExito = waitCorto.until(
                ExpectedConditions.visibilityOfElementLocated(
                        By.cssSelector(".alert-success, #alertMsg.alert-success")
                )
        );
        String texto = mensajeExito.getText().toLowerCase();
        assertTrue(
                texto.contains("éxito") || texto.contains("registr") || texto.contains("usuario"),
                "El mensaje de éxito debe confirmar el registro. Texto: " + texto
        );
    }
}

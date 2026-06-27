package com.connextion.tests;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * CU7 / CU8 – Prueba automatizada #2
 *
 * Flujo:
 *   1. Iniciar sesión como supervisor (dario / 123456).
 *   2. Verificar que el dashboard muestra el rol SUPERVISOR.
 *   3. Navegar al formulario de registro de soportistas.
 *   4. Completar todos los campos requeridos.
 *   5. Seleccionar al menos un servicio.
 *   6. Enviar el formulario.
 *   7. Verificar el mensaje de éxito.
 *
 * Credenciales del supervisor de prueba:
 *   email    : dario
 *   password : 123456
 *
 * Nota: El campo "email" del formulario de login acepta el email almacenado
 * en la base de datos del usuario. Para el supervisor "dario" se usa dicho
 * valor directamente tal como está registrado en el sistema.
 */
@DisplayName("CP-02: Login como supervisor y registro de soportista")
class SupervisorRegisterSupportTest extends BaseSeleniumTest {

    // ── Credenciales del supervisor ──────────────────────────────────────────
    private static final String EMAIL_SUPERVISOR    = "dario";
    private static final String PASSWORD_SUPERVISOR = "123456";

    // ── Datos del nuevo soportista (sufijo timestamp para evitar duplicados) ──
    private static final String SUFIJO =
            DateTimeFormatter.ofPattern("MMddHHmmss").format(LocalDateTime.now());

    private static final String NAME_SUPPORT        = "SeleniumTest";
    private static final String LAST_NAME1          = "Prueba";
    private static final String LAST_NAME2         = "Automatica";
    private static final String EMAIL_SUPPORT         = "auto.soportista." + SUFIJO + "@connextion.test";
    private static final String PASSWORD_SUPPORT      = "Test1234";

    // ────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("Debe iniciar sesión como supervisor y registrar un nuevo soportista")
    void testSupervisorRegistraSoportista() {

        // ── Paso 1: Login como supervisor ────────────────────────────────────
        Loggin(EMAIL_SUPERVISOR, PASSWORD_SUPERVISOR);

        // ── Paso 2: Verificar rol en el dashboard ────────────────────────────
        WebElement badgeRol = waitVisible(By.id("roleBadge"));
        assertEquals("Supervisor", badgeRol.getText().trim(),
                "El badge debe indicar 'Supervisor' para este usuario");

        // ── Paso 3: Navegar a "Registrar soportista" ─────────────────────────
        // La tarjeta con id "cardRegisterSupport" lleva a support-register.html
        WebElement tarjetaRegistrar = waitClickeable(By.id("cardRegisterSupport"));
        tarjetaRegistrar.click();

        wait.until(ExpectedConditions.urlContains("support-register.html"));
        assertTrue(driver.getCurrentUrl().contains("support-register.html"),
                "Debe redirigir al formulario de registro de soportistas");

        // ── Paso 4: Completar campos del formulario ──────────────────────────
        fullSpace("name",           NAME_SUPPORT);
        fullSpace("firstSurname",   LAST_NAME1);
        fullSpace("secondSurname",  LAST_NAME2);
        fullSpace("email",          EMAIL_SUPPORT);
        fullSpace("password",       PASSWORD_SUPPORT);

        // Dejar "isSupervisor" sin marcar (registrar como soportista, no supervisor)
        WebElement checkSupervisor = driver.findElement(By.id("isSupervisor"));
        if (checkSupervisor.isSelected()) {
            checkSupervisor.click(); // desmarcar si estuviera marcado
        }
        assertFalse(checkSupervisor.isSelected(),
                "El checkbox de supervisor no debe estar marcado para un soportista regular");

        // ── Paso 5: Seleccionar al menos un servicio ─────────────────────────
        // Los servicios se cargan dinámicamente en #servicesGrid; esperar que aparezcan.
        List<WebElement> checkboxesServicio = wait.until(driver -> {
            List<WebElement> checks = driver.findElements(
                    By.cssSelector("#servicesGrid input[type='checkbox']")
            );
            return checks.isEmpty() ? null : checks;
        });

        assertFalse(checkboxesServicio.isEmpty(),
                "Deben existir servicios disponibles para asignar al soportista");

        // Marcar el primer servicio disponible
        WebElement primerServicio = checkboxesServicio.get(0);
        if (!primerServicio.isSelected()) {
            primerServicio.click();
        }
        assertTrue(primerServicio.isSelected(),
                "Al menos un servicio debe quedar seleccionado");

        // ── Paso 6: Enviar el formulario ─────────────────────────────────────
        driver.findElement(By.cssSelector("button.btn")).click();

        // ── Paso 7: Verificar mensaje de éxito ──────────────────────────────
        try {
            // Caso A: alert nativo del navegador
            wait.until(ExpectedConditions.alertIsPresent());
            String mensajeAlert = driver.switchTo().alert().getText().toLowerCase();
            assertTrue(
                    mensajeAlert.contains("éxito") ||
                    mensajeAlert.contains("registr") ||
                    mensajeAlert.contains("usuario"),
                    "El mensaje debe confirmar el registro del soportista. Texto: " + mensajeAlert
            );
            driver.switchTo().alert().accept();
        } catch (Exception sinAlertNativo) {
            // Caso B: mensaje inline en el DOM
            WebElement mensajeExito = wait.until(
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
}

package com.connextion.tests;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;

/**
 * CU1 / CU2 / CU4 – Prueba automatizada #1
 *
 * Flujo:
 *   1. Abrir la página de inicio de sesión (index.html).
 *   2. Ingresar credenciales del cliente Felipe Mora.
 *   3. Verificar que el dashboard se carga con el rol CLIENT.
 *   4. Navegar a "Nueva solicitud" y completar el formulario.
 *   5. Confirmar que la solicitud se registra con éxito.
 *
 * Credenciales del cliente de prueba:
 *   email    : felipemora073@gmail.com
 *   password : 123456
 */
@DisplayName("CP-01: Login como cliente y creación de solicitud")
class ClientMakeRequestTest extends BaseSeleniumTest {

    // ── Credenciales ────────────────────────────────────────────────────────
    private static final String EMAIL_CLIENT    = "felipemora073@gmail.com";
    private static final String PASSWORD_CLIENT = "123456";

    // ── Datos de la solicitud ────────────────────────────────────────────────
    private static final String DESCRIPTION_QUEST =
            "[Prueba Automatizada] El servicio de Internet presenta cortes intermitentes " +
            "desde el amanecer del día de hoy. La señal cae cada 20 minutos aproximadamente.";
    private static final String PHONE_CONTACT  = "88001234";
    private static final String EMAIL_CONTACT     = "felipemora073@gmail.com";
    private static final String ADDRESS = "Barrio Los Yoses, San José";

    // ────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("Debe iniciar sesión como cliente y registrar una solicitud nueva")
    void testLoginClienteYCrearSolicitud() {

        // ── Paso 1: Navegar al formulario de login ───────────────────────────
        navegateTo("index.html");

        WebElement campoEmail = waitVisible(By.id("email"));
        assertTrue(campoEmail.isDisplayed(),
                "El campo de email debe estar visible en la pantalla de login");

        // ── Paso 2: Completar credenciales y hacer login ─────────────────────
        campoEmail.clear();
        campoEmail.sendKeys(EMAIL_CLIENT);

        WebElement campoPassword = driver.findElement(By.id("password"));
        campoPassword.clear();
        campoPassword.sendKeys(PASSWORD_CLIENT);

        driver.findElement(By.cssSelector("button.btn")).click();

        // ── Paso 3: Verificar redirección al dashboard ───────────────────────
        wait.until(ExpectedConditions.urlContains("dashboard.html"));
        assertTrue(driver.getCurrentUrl().contains("dashboard.html"),
                "Después del login el usuario debe estar en el dashboard");

        // Verificar que el badge muestra el rol correcto
        WebElement badgeRol = waitVisible(By.id("roleBadge"));
        assertEquals("Cliente", badgeRol.getText().trim(),
                "El badge de rol debe indicar 'Cliente'");

        // Verificar que el nombre del usuario aparece en pantalla
        WebElement nombreUsuario = driver.findElement(By.id("userName"));
        assertFalse(nombreUsuario.getText().isBlank(),
                "El nombre del usuario debe mostrarse en el dashboard");

        // ── Paso 4: Navegar a "Nueva solicitud" ──────────────────────────────
        // La tarjeta "cardNewRequest" redirige a new-request.html
        WebElement tarjetaNuevaSolicitud = waitClickeable(By.id("cardNewRequest"));
        tarjetaNuevaSolicitud.click();

        wait.until(ExpectedConditions.urlContains("new-request.html"));
        assertTrue(driver.getCurrentUrl().contains("new-request.html"),
                "Debe redirigir a la página de nueva solicitud");

        // ── Paso 5: Llenar el formulario de solicitud ────────────────────────
        // Campo requerido: descripción
        fullSpace("description", DESCRIPTION_QUEST);

        // Campos opcionales
        fullSpace("contactPhone",      PHONE_CONTACT);
        fullSpace("contactEmail",      EMAIL_CONTACT);
        fullSpace("referenceAddress",  ADDRESS);

        // Seleccionar servicio: esperar a que el <select> se pueble con opciones
        // (se carga dinámicamente desde el backend).
        WebElement selectServicio = wait.until(driver -> {
            WebElement sel = driver.findElement(By.id("serviceId"));
            Select s = new Select(sel);
            // La lista está lista cuando tiene más de 1 opción (la primera es el placeholder)
            return s.getOptions().size() > 1 ? sel : null;
        });

        Select selectorServicio = new Select(selectServicio);
        // Seleccionar la primera opción real (índice 1 para omitir el placeholder)
        selectorServicio.selectByIndex(1);

        // ── Paso 6: Registrar la solicitud ───────────────────────────────────
        driver.findElement(By.cssSelector("button.btn")).click();

        // ── Paso 7: Verificar mensaje de éxito ──────────────────────────────
        // Esta pantalla solo usa el mensaje inline (#alertMsg.alert-success) y
        // redirige a my-requests.html ~1.2s después de mostrarlo. Por eso se usa
        // un WebDriverWait corto y dedicado en vez del wait largo por defecto,
        // para alcanzar a leerlo antes de que la página cambie.
        WebDriverWait waitCorto = new WebDriverWait(driver, Duration.ofSeconds(5));
        WebElement mensajeExito = waitCorto.until(
                ExpectedConditions.visibilityOfElementLocated(
                        By.cssSelector(".alert-success, #alertMsg.alert-success")
                )
        );
        String texto = mensajeExito.getText().toLowerCase();
        assertTrue(
                texto.contains("éxito") || texto.contains("registr") || texto.contains("solicitud"),
                "El mensaje de éxito debe confirmar el registro. Texto: " + texto
        );
    }
}
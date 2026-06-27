package com.connextion.tests;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * CU12 / CU14 / CU15 – Prueba automatizada #3
 *
 * Flujo:
 *   1. Iniciar sesión como soportista (lind / 123456).
 *   2. Verificar que el dashboard muestra el rol SUPPORTER.
 *   3. Navegar al panel de soportista (supporter.html) para ver los tiquetes asignados.
 *   4. Seleccionar el primer tiquete en estado EN_PROGRESO.
 *   5. Acceder al chat del tiquete (chat_module.html?issueId=X) y enviar un mensaje.
 *   6. Volver al panel y resolver el tiquete (resolution.html?issueId=X).
 *   7. Verificar el mensaje de resolución exitosa.
 *
 * Credenciales del soportista de prueba:
 *   email    : lind
 *   password : 123456
 *
 * Prerrequisito: debe existir al menos un tiquete en estado EN_PROGRESO
 * asignado al soportista "lind" en la base de datos. Si todos sus tiquetes
 * están en estado ASIGNADO, la prueba iniciará el proceso primero y luego
 * continuará con el chat y la resolución.
 */
@DisplayName("CP-03: Login como soportista, enviar mensaje en chat y resolver tiquete")
class SupportChatAndSolveQuestTest extends BaseSeleniumTest {

    // ── Credenciales del soportista ──────────────────────────────────────────
    private static final String EMAIL_SUPPORT    = "lind";
    private static final String PASSWORD_SUPPORT = "123456";

    // ── Datos de prueba ──────────────────────────────────────────────────────
    private static final String MESSAGE_CHAT =
            "[Prueba Automatizada] Hola, estoy revisando su caso en este momento. " +
            "Por favor confirme si el servicio sigue presentando fallas.";

    private static final String COMMENT_SOLUTION =
            "[Prueba Automatizada] Se verificó la configuración del equipo y se " +
            "restableció la conexión. El servicio fue restaurado satisfactoriamente.";

    // ────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("Debe iniciar sesión como soportista, chatear y resolver un tiquete")
    void testSoportistaEnviaMensajeYResuelve() throws Exception {

        // ── Paso 1: Login como soportista ────────────────────────────────────
        Loggin(EMAIL_SUPPORT, PASSWORD_SUPPORT);

        // ── Paso 2: Verificar rol en el dashboard ────────────────────────────
        WebElement badgeRol = waitVisible(By.id("roleBadge"));
        assertEquals("Soportista", badgeRol.getText().trim(),
                "El badge debe indicar 'Soportista' para este usuario");

        // ── Paso 3: Navegar al panel de soportista ───────────────────────────
        // La tarjeta "cardSupportRequests" navega a supporter.html
        WebElement tarjetaSolicitudes = waitClickeable(By.id("cardSupportRequests"));
        tarjetaSolicitudes.click();

        wait.until(ExpectedConditions.urlContains("supporter.html"));
        assertTrue(driver.getCurrentUrl().contains("supporter.html"),
                "Debe redirigir al panel de solicitudes del soportista");

        // ── Paso 4: Localizar un tiquete en el panel ─────────────────────────
        // Esperar a que la tabla se pueble con al menos una fila de datos
        wait.until(driver -> {
            List<WebElement> filas = driver.findElements(
                    By.cssSelector("#supporterTable tbody tr")
            );
            // Ignorar la fila "No tienes solicitudes asignadas"
            return filas.stream().anyMatch(
                    tr -> !tr.getText().contains("No tienes solicitudes")
            ) ? filas : null;
        });

        // Buscar el ID del primer tiquete en estado EN_PROGRESO.
        // Si no hay ninguno en ese estado, tomar el primero ASIGNADO e iniciarlo.
        String issueId = obtenerIssueIdParaProcesar();

        assertNotNull(issueId,
                "Debe haber al menos un tiquete asignado al soportista para ejecutar la prueba");

        // ── Paso 5: Navegar al chat del tiquete y enviar un mensaje ──────────
        driver.get(BASE_URL + "/chat_module.html?issueId=" + issueId);

        // Manejar posible alert de "Issue ID no encontrado" en modo de pruebas locales
        try {
            wait.until(ExpectedConditions.alertIsPresent());
            driver.switchTo().alert().accept();
        } catch (Exception ignorar) { /* no hay alert */ }

        // Esperar a que el WebSocket se conecte (el texto del status cambia a "Connected")
        WebElement statusConexion = waitVisible(By.id("connection-status"));
        wait.until(driver -> statusConexion.getText().toLowerCase().contains("connected"));
        assertTrue(statusConexion.getText().toLowerCase().contains("connected"),
                "El WebSocket debe establecer conexión con el servidor");

        // Escribir y enviar el mensaje en el chat
        WebElement campoMensaje = waitVisible(By.id("message-input"));
        campoMensaje.clear();
        campoMensaje.sendKeys(MESSAGE_CHAT);

        WebElement botonEnviar = waitClickeable(By.id("send-button"));
        botonEnviar.click();

        // Verificar que el mensaje apareció en la lista (scroll down para ver el último)
        stop(1500); // pequeña pausa para que el DOM se actualice
        List<WebElement> mensajesLista = driver.findElements(
                By.cssSelector("#message-list li")
        );
        assertFalse(mensajesLista.isEmpty(),
                "La lista de mensajes no debe estar vacía después de enviar uno");

        // Confirmar que alguno de los mensajes contiene parte del texto enviado
        boolean mensajeEncontrado = mensajesLista.stream().anyMatch(
                li -> li.getText().contains("[Prueba Automatizada]")
        );
        assertTrue(mensajeEncontrado,
                "El mensaje enviado debe aparecer en la lista del chat");

        // ── Paso 6: Navegar al formulario de resolución ──────────────────────
        driver.get(BASE_URL + "/resolution.html?issueId=" + issueId);

        // Esperar a que el formulario cargue y el ID ya esté prellenado
        WebElement campoIssueId = waitVisible(By.id("issueId"));
        assertEquals(issueId, campoIssueId.getAttribute("value"),
                "El campo issueId debe estar prellenado con el ID del tiquete");

        // Ingresar el comentario de resolución
        fullSpace("resolutionComment", COMMENT_SOLUTION);

        // Enviar el formulario de resolución
        WebElement botonResolver = driver.findElement(
                By.cssSelector("#resolveForm button[type='submit']")
        );
        botonResolver.click();

        // ── Paso 7: Verificar el mensaje de éxito en la resolución ──────────
        wait.until(ExpectedConditions.alertIsPresent());
        String mensajeAlert = driver.switchTo().alert().getText().toLowerCase();

        assertTrue(
                mensajeAlert.contains("éxito") ||
                mensajeAlert.contains("resuelto") ||
                mensajeAlert.contains("resolu"),
                "El alert debe confirmar la resolución del tiquete. Texto: " + mensajeAlert
        );

        driver.switchTo().alert().accept();
    }

    // ────────────────────────────────────────────────────────────────────────
    // Métodos privados de apoyo
    // ────────────────────────────────────────────────────────────────────────

    /**
     * Escanea la tabla de tiquetes del soportista y devuelve el ID de un tiquete
     * que pueda usarse para el chat y la resolución.
     *
     * Prioridad:
     *   1. Primer tiquete EN_PROGRESO → listo para chatear y resolver.
     *   2. Primer tiquete ASIGNADO → lo inicia (cambia a EN_PROGRESO) y lo devuelve.
     *
     * @return el issueId como String, o null si no hay tiquetes disponibles
     */
    private String obtenerIssueIdParaProcesar() {

        List<WebElement> filas = driver.findElements(
                By.cssSelector("#supporterTable tbody tr")
        );

        String idEnProgreso = null;
        String idAsignado   = null;

        for (WebElement fila : filas) {
            String textoCelda = fila.getText();
            if (textoCelda.contains("No tienes solicitudes")) continue;

            // La primera columna (td[1]) contiene el issueId
            List<WebElement> celdas = fila.findElements(By.tagName("td"));
            if (celdas.isEmpty()) continue;

            String id     = celdas.get(0).getText().trim();
            String estado = celdas.size() > 4 ? celdas.get(4).getText().trim() : "";

            if ("EN_PROGRESO".equalsIgnoreCase(estado) && idEnProgreso == null) {
                idEnProgreso = id;
            } else if ("ASIGNADO".equalsIgnoreCase(estado) && idAsignado == null) {
                idAsignado = id;
            }
        }

        // Caso 1: hay un tiquete en progreso → usarlo directamente
        if (idEnProgreso != null) {
            return idEnProgreso;
        }

        // Caso 2: hay uno asignado → iniciarlo primero
        if (idAsignado != null) {
            iniciarTiquete(idAsignado);
            return idAsignado;
        }

        return null; // No hay tiquetes disponibles
    }

    /**
     * Hace clic en el botón "Iniciar Proceso" de la fila correspondiente al issueId dado.
     *
     * @param issueId ID del tiquete a iniciar
     */
    private void iniciarTiquete(String issueId) {
        // El botón "Iniciar Proceso" invoca startIssue(id) — buscarlo en la fila correcta.
        String selectorBoton = String.format(
                "#supporterTable tbody tr td button[onclick*='startIssue(%s)']", issueId
        );

        try {
            WebElement botonIniciar = waitClickeable(By.cssSelector(selectorBoton));
            botonIniciar.click();

            // Aceptar el alert de confirmación
            wait.until(ExpectedConditions.alertIsPresent());
            driver.switchTo().alert().accept();

            // Pequeña pausa para que la tabla se recargue
            stop(2000);

        } catch (Exception e) {
            // Intentar via JavaScript como fallback
            ((JavascriptExecutor) driver)
                    .executeScript(String.format("startIssue(%s);", issueId));
            stop(2000);
            try {
                wait.until(ExpectedConditions.alertIsPresent());
                driver.switchTo().alert().accept();
            } catch (Exception ignorar) { /* sin alert */ }
        }
    }
}

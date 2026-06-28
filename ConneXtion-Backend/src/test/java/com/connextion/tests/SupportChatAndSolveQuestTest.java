package com.connextion.tests;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * CU12 / CU14 / CU15 – Prueba automatizada #3
 *
 * Flujo: 1. Iniciar sesión como soportista (lind / 123456). 2. Verificar que el
 * dashboard muestra el rol SUPPORTER. 3. Navegar al panel de soportista
 * (supporter.html) para ver los tiquetes asignados. 4. Seleccionar el primer
 * tiquete en estado EN_PROGRESO. 5. Acceder al chat del tiquete
 * (chat_module.html?issueId=X) y enviar un mensaje. 6. Volver al panel y
 * resolver el tiquete (resolution.html?issueId=X). 7. Verificar el mensaje de
 * resolución exitosa.
 *
 * Credenciales del soportista de prueba: email : lind password : 123456
 *
 * Prerrequisito: debe existir al menos un tiquete en estado EN_PROGRESO
 * asignado al soportista "lind" en la base de datos. Si todos sus tiquetes
 * están en estado ASIGNADO, la prueba iniciará el proceso primero y luego
 * continuará con el chat y la resolución.
 */
@DisplayName("CP-03: Login como soportista, enviar mensaje en chat y resolver tiquete")
class SupportChatAndSolveQuestTest extends BaseSeleniumTest {

    private static final String EMAIL_SUPPORT = "lind";
    private static final String PASSWORD_SUPPORT = "123456";

    private static final String MESSAGE_CHAT
            = "[Prueba Automatizada] Hola, estoy revisando su caso en este momento. "
            + "Por favor confirme si el servicio sigue presentando fallas.";

    private static final String COMMENT_SOLUTION
            = "[Prueba Automatizada] Se verificó la configuración del equipo y se "
            + "restableció la conexión. El servicio fue restaurado satisfactoriamente.";

    @Test
    @DisplayName("Debe iniciar sesión como soportista, chatear y resolver un tiquete")
    void testSoportistaEnviaMensajeYResuelve() throws Exception {

        Loggin(EMAIL_SUPPORT, PASSWORD_SUPPORT);

        WebElement badgeRol = waitVisible(By.id("roleBadge"));
        assertEquals("Soportista", badgeRol.getText().trim(),
                "El badge debe indicar 'Soportista' para este usuario");

        WebElement tarjetaSolicitudes = waitClickeable(By.id("cardSupportRequests"));
        tarjetaSolicitudes.click();

        wait.until(ExpectedConditions.urlContains("supporter.html"));
        assertTrue(driver.getCurrentUrl().contains("supporter.html"),
                "Debe redirigir al panel de solicitudes del soportista");

        wait.until(driver -> {
            List<WebElement> filas = driver.findElements(
                    By.cssSelector("#supporterTable tbody tr")
            );
            return filas.stream().anyMatch(
                    tr -> !tr.getText().contains("No tienes solicitudes")
            ) ? filas : null;
        });

        String issueId = obtenerIssueIdParaProcesar();

        assertNotNull(issueId,
                "Debe haber al menos un tiquete asignado al soportista para ejecutar la prueba");

        driver.get(BASE_URL + "/chat_module.html?issueId=" + issueId);

        try {
            wait.until(ExpectedConditions.alertIsPresent());
            driver.switchTo().alert().accept();
        } catch (Exception ignorar) {
            /* no hay alert */ }

        WebElement statusConexion = waitVisible(By.id("connection-status"));
        wait.until(driver -> statusConexion.getText().toLowerCase().contains("connected"));
        assertTrue(statusConexion.getText().toLowerCase().contains("connected"),
                "El WebSocket debe establecer conexión con el servidor");

        WebElement campoMensaje = waitVisible(By.id("message-input"));
        campoMensaje.clear();
        campoMensaje.sendKeys(MESSAGE_CHAT);

        WebElement botonEnviar = waitClickeable(By.id("send-button"));
        botonEnviar.click();

        stop(1500);
        List<WebElement> mensajesLista = driver.findElements(
                By.cssSelector("#message-list li")
        );
        assertFalse(mensajesLista.isEmpty(),
                "La lista de mensajes no debe estar vacía después de enviar uno");

        boolean mensajeEncontrado = mensajesLista.stream().anyMatch(
                li -> li.getText().contains("[Prueba Automatizada]")
        );
        assertTrue(mensajeEncontrado,
                "El mensaje enviado debe aparecer en la lista del chat");

        driver.get(BASE_URL + "/resolution.html?issueId=" + issueId);

        WebElement campoIssueId = waitVisible(By.id("issueId"));
        assertEquals(issueId, campoIssueId.getAttribute("value"),
                "El campo issueId debe estar prellenado con el ID del tiquete");

        fullSpace("resolutionComment", COMMENT_SOLUTION);

        WebElement botonResolver = driver.findElement(
                By.cssSelector("#resolveForm button[type='submit']")
        );
        botonResolver.click();

        wait.until(ExpectedConditions.alertIsPresent());
        String mensajeAlert = driver.switchTo().alert().getText().toLowerCase();

        assertTrue(
                mensajeAlert.contains("éxito")
                || mensajeAlert.contains("resuelto")
                || mensajeAlert.contains("resolu"),
                "El alert debe confirmar la resolución del tiquete. Texto: " + mensajeAlert
        );

        driver.switchTo().alert().accept();
    }

    private String obtenerIssueIdParaProcesar() {

        List<WebElement> filas = driver.findElements(
                By.cssSelector("#supporterTable tbody tr")
        );

        List<List<String>> filasComoTexto = new ArrayList<>();
        for (WebElement fila : filas) {
            try {
                List<WebElement> celdas = fila.findElements(By.tagName("td"));
                List<String> textosCeldas = new ArrayList<>();
                for (WebElement celda : celdas) {
                    textosCeldas.add(celda.getText().trim());
                }
                filasComoTexto.add(textosCeldas);
            } catch (org.openqa.selenium.StaleElementReferenceException stale) {
            }
        }

        String idEnProgreso = null;
        String idAsignado = null;

        for (List<String> celdas : filasComoTexto) {
            if (celdas.isEmpty()) {
                continue;
            }
            if (celdas.get(0).contains("No tienes solicitudes")) {
                continue;
            }

            String id = celdas.get(0);
            String estado = celdas.size() > 4 ? celdas.get(4) : "";

            if ("EN_PROGRESO".equalsIgnoreCase(estado) && idEnProgreso == null) {
                idEnProgreso = id;
            } else if ("ASIGNADO".equalsIgnoreCase(estado) && idAsignado == null) {
                idAsignado = id;
            }
        }
        if (idEnProgreso != null) {
            return idEnProgreso;
        }

        if (idAsignado != null) {
            iniciarTiquete(idAsignado);
            return idAsignado;
        }

        return null;
    }

    private void iniciarTiquete(String issueId) {
        String selectorBoton = String.format(
                "#supporterTable tbody tr td button[onclick*='startIssue(%s)']", issueId
        );

        try {
            WebElement botonIniciar = waitClickeable(By.cssSelector(selectorBoton));
            botonIniciar.click();
            wait.until(ExpectedConditions.alertIsPresent());
            driver.switchTo().alert().accept();

            stop(2000);

        } catch (Exception e) {
            ((JavascriptExecutor) driver)
                    .executeScript(String.format("startIssue(%s);", issueId));
            stop(2000);
            try {
                wait.until(ExpectedConditions.alertIsPresent());
                driver.switchTo().alert().accept();
            } catch (Exception ignorar) {
                /* sin alert */ }
        }
    }
}

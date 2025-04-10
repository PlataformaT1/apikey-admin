package org.apikey.util;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.UUID;

public class ApiKeyUtils {
    private static final ZoneId MEXICO_ZONE = ZoneId.of("America/Mexico_City");

    /**
     * Obtiene la fecha y hora actual en zona horaria de México
     */
    public static Date getCurrentMexicoDate() {
        ZonedDateTime nowInMexico = ZonedDateTime.now(MEXICO_ZONE);
        return Date.from(nowInMexico.toInstant());
    }

    /**
     * Obtiene una fecha futura en zona horaria de México
     * @param hoursToAdd Horas a añadir a la fecha actual
     */
    public static Date getFutureMexicoDate(int hoursToAdd) {
        ZonedDateTime futureInMexico = ZonedDateTime.now(MEXICO_ZONE).plusHours(hoursToAdd);
        return Date.from(futureInMexico.toInstant());
    }

    /**
     * Genera un nuevo valor para API key
     * @param platform Plataforma para la que se genera la API key
     */
    public static String generateApiKeyValue(String platform) {
        String uuid = UUID.randomUUID().toString();
        return String.format("APP-%s-%s", platform.toUpperCase(), uuid);
    }
}
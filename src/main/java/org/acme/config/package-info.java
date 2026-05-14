/**
 * Clases de configuración de la aplicación.
 * Mapean grupos de propiedades de {@code application.properties} a objetos Java tipados.
 *
 * <p>Usar {@code @ConfigMapping(prefix = "mi.prefijo")} para agrupar propiedades relacionadas.
 * Esto permite inyectar la configuración con {@code @Inject} en lugar de múltiples
 * {@code @ConfigProperty}.
 *
 * <p>Ejemplos: {@code AppConfig.java}, {@code DatabaseConfig.java}, {@code SecurityConfig.java}
 */
package org.acme.config;

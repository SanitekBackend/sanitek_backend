/**
 * Utilidades y helpers transversales sin lógica de negocio.
 * Clases con métodos estáticos para operaciones comunes: formateo de fechas,
 * normalización de strings, generación de slugs, constantes globales, etc.
 *
 * <p>Regla: si una clase aquí necesita {@code @Inject}, no pertenece a este paquete
 * — va en {@code service/} o {@code infrastructure/}.
 *
 * <p>Ejemplos: {@code DateUtils.java}, {@code StringUtils.java}, {@code Constants.java}
 */
package org.acme.util;

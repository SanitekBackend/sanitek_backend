/**
 * Capa de servicios: contiene la lógica de negocio de la aplicación.
 * Los servicios coordinan repositorios, validaciones, transformaciones y llamadas
 * a sistemas externos (Firebase, otros microservicios, etc.).
 *
 * <p>Reglas:
 * <ul>
 *   <li>Anotar con {@code @ApplicationScoped} (CDI singleton, una sola instancia)
 *   <li>Los métodos que modifican datos deben ser {@code @Transactional}
 *   <li>Reciben DTOs de request y devuelven DTOs de response (no entidades crudas)
 *   <li>Lanzan {@code AppException} para errores de negocio con su código HTTP
 * </ul>
 *
 * <p>Ejemplos: {@code UsuarioService.java}, {@code ProductoService.java}
 */
package org.acme.service;

/**
 * Recursos REST (controladores HTTP). Exponen los endpoints de la API.
 *
 * <p>Responsabilidad única: recibir la petición HTTP, delegar al servicio y devolver la respuesta.
 * No contienen lógica de negocio.
 *
 * <p>Reglas:
 * <ul>
 *   <li>Anotar con {@code @Path("/ruta")}, {@code @Produces}, {@code @Consumes}
 *   <li>Usar {@code @Valid} en parámetros de request para activar Bean Validation
 *   <li>Inyectar el servicio correspondiente con {@code @Inject}
 *   <li>Devolver {@code Response} o directamente el DTO de respuesta
 * </ul>
 *
 * <p>Convención de nombre: {@code <Entidad>Resource.java}
 * <br>Ejemplos: {@code UsuarioResource.java}, {@code ProductoResource.java}
 */
package org.acme.rest;

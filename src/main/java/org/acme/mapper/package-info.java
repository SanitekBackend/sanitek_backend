/**
 * Mappers: convierten entre entidades JPA y DTOs.
 *
 * <p>Dos direcciones:
 * <ul>
 *   <li>{@code Entidad → ResponseDTO} — al devolver datos al cliente
 *   <li>{@code RequestDTO → Entidad} — al crear o actualizar registros
 * </ul>
 *
 * <p>Los mappers son dependencia del {@code service/}, NO del {@code rest/}.
 * El resource solo recibe el DTO ya transformado desde el service.
 *
 * <p>Convención de nombre: {@code <Entidad>Mapper.java}
 * <br>Ejemplos: {@code UsuarioMapper.java}, {@code ZonaMapper.java}
 */
package org.acme.mapper;

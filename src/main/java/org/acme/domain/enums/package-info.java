/**
 * Enumeraciones del dominio de negocio.
 * Representan conjuntos de valores fijos: estados, tipos, roles, categorías, etc.
 *
 * <p>Al persistir en BD usar {@code @Enumerated(EnumType.STRING)} en la entidad
 * para guardar el nombre legible en lugar del ordinal numérico.
 *
 * <p>Ejemplos: {@code EstadoPedido.java}, {@code RolUsuario.java}, {@code TipoDocumento.java}
 */
package org.acme.domain.enums;

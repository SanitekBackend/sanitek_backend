/**
 * Repositorios de acceso a datos (capa de persistencia).
 * Cada repositorio gestiona una entidad y encapsula todas las consultas a BD.
 *
 * <p>Dos estilos con Panache:
 * <ul>
 *   <li><b>Repository pattern</b> (recomendado): clase separada anotada con
 *       {@code @ApplicationScoped} que implementa {@code PanacheRepository<Entidad>}
 *   <li><b>Active Record</b>: la propia entidad extiende {@code PanacheEntity}
 *       y los métodos de consulta van en ella
 * </ul>
 *
 * <p>Solo los servicios deben llamar a los repositorios. Nunca desde los recursos REST.
 *
 * <p>Ejemplos: {@code UsuarioRepository.java}, {@code ProductoRepository.java}
 */
package org.acme.repository;

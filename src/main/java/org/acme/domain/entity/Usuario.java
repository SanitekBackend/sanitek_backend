package org.acme.domain.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "usuarios")
@AttributeOverride(name = "id", column = @Column(name = "id_usuario"))
public class Usuario extends BaseEntity {

    // Clave de enlace con Firebase Auth — no se almacena contraseña
    @Column(name = "firebase_uid", nullable = false, unique = true, length = 128)
    public String firebaseUid;

    // Sincronizado desde Firebase; no se modifica directamente en BD
    @Column(name = "email", nullable = false, unique = true, length = 255)
    public String email;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_rol")
    public Rol rol;

    @Column(name = "activo", nullable = false)
    public Boolean activo = true;
}

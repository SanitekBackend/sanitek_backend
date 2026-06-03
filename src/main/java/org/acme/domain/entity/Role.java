package org.acme.domain.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "role")
public class Role extends BaseEntity {

    @Column(name = "role_name", nullable = false, unique = true, length = 50)
    private String roleName;

    public Role(){
    }

    public String getRoleName(){
        return roleName;
    }

    public void setRoleName(String roleName){
        this.roleName = roleName;
    }
}

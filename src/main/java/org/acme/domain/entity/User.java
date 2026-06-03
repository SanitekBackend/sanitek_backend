package org.acme.domain.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "user")
public class User extends BaseEntity {

    @Column(name = "names", length = 150)
    private String names;

    @Column(name = "firebase_uid", nullable = false, unique = true, length = 128)
    private String firebaseUid;

    @Column(name = "email", nullable = false, unique = true, length = 255)
    private String email;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "role_id", nullable = false)
    private Role role;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id")
    private Company company;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by_user_id")
    private User createdBy;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    public User(){
    }

    public String getNames(){
        return names;
    }

    public void setNames(String names){
        this.names = names;
    }

    public String getFirebaseUid(){
        return firebaseUid;
    }

    public void setFirebaseUid(String firebaseUid){
        this.firebaseUid = firebaseUid;
    }

    public String getEmail(){
        return email;
    }

    public void setEmail(String email){
        this.email = email;
    }

    public Role getRole(){
        return role;
    }

    public void setRole(Role role){
        this.role = role;
    }

    public Company getCompany(){
        return company;
    }

    public void setCompany(Company company){
        this.company = company;
    }

    public User getCreatedBy(){
        return createdBy;
    }

    public void setCreatedBy(User createdBy){
        this.createdBy = createdBy;
    }

    public Boolean getIsActive(){
        return isActive;
    }

    public void setIsActive(Boolean isActive){
        this.isActive = isActive;
    }
}

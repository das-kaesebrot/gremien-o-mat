package de.astahsrm.gremiomat.security;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToOne;
import javax.persistence.Version;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import de.astahsrm.gremiomat.candidate.Candidate;

@Entity
public class MgmtUser {
    @Version
    private long version;
    @Id
    @NotBlank
    private String username;
    @NotBlank
    private String password;
    @NotBlank
    private String role;
    @OneToOne
    private Candidate details;
    @Column(columnDefinition = "boolean default false")
    private boolean isLocked;

    public MgmtUser() {
    }

    public MgmtUser(@NotBlank String username, @NotBlank String password, @NotNull Candidate candidateDetails) {
        super();
        this.username = username;
        this.password = password;
        this.role = SecurityConfig.USER;
        this.details = candidateDetails;
    }

    public MgmtUser(@NotBlank String username, @NotBlank String password) {
        super();
        this.username = username;
        this.password = password;
        this.role = SecurityConfig.ADMIN;
        this.details = null;
    }

    public Candidate getDetails() {
        return details;
    }

    public void setDetails(Candidate candidateDetails) {
        this.details = candidateDetails;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public long getVersion() {
        return version;
    }

    public void setVersion(long version) {
        this.version = version;
    }

    public boolean isLocked() {
        return isLocked;
    }

    public void setLocked(boolean isLocked) {
        this.isLocked = isLocked;
    }
}

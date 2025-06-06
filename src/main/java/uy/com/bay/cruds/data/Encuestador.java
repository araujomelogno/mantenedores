package uy.com.bay.cruds.data;

import jakarta.persistence.Entity;

@Entity
public class Encuestador extends AbstractEntity {

    private String firstName;
    private String lastName;
    private String ci;

    public String getFirstName() {
        return firstName;
    }
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }
    public String getLastName() {
        return lastName;
    }
    public void setLastName(String lastName) {
        this.lastName = lastName;
    }
    public String getCi() {
        return ci;
    }
    public void setCi(String ci) {
        this.ci = ci;
    }

}

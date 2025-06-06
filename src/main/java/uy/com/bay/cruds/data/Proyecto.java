package uy.com.bay.cruds.data;

import jakarta.persistence.Entity;

@Entity
public class Proyecto extends AbstractEntity {

    private String name;
    private String alchemerId;
    private String doobloId;
    private String odooId;
    private String obs;

    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public String getAlchemerId() {
        return alchemerId;
    }
    public void setAlchemerId(String alchemerId) {
        this.alchemerId = alchemerId;
    }
    public String getDoobloId() {
        return doobloId;
    }
    public void setDoobloId(String doobloId) {
        this.doobloId = doobloId;
    }
    public String getOdooId() {
        return odooId;
    }
    public void setOdooId(String odooId) {
        this.odooId = odooId;
    }
    public String getObs() {
        return obs;
    }
    public void setObs(String obs) {
        this.obs = obs;
    }

}

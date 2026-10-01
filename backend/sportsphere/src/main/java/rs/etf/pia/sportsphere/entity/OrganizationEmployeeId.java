package rs.etf.pia.sportsphere.entity;

import java.io.Serializable;

public class OrganizationEmployeeId implements Serializable {
    
    private Long organizationId;
    private Long employeeId;

    public OrganizationEmployeeId() {
    }

    public OrganizationEmployeeId(Long organizationId, Long employeeId) {
        this.organizationId = organizationId;
        this.employeeId = employeeId;
    }

    public Long getOrganizationId() {
        return organizationId;
    }

    public void setOrganizationId(Long organizationId) {
        this.organizationId = organizationId;
    }

    public Long getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(Long employeeId) {
        this.employeeId = employeeId;
    }

}

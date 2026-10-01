package rs.etf.pia.sportsphere.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;

@Entity
@Table(name = "organization_employee")
@IdClass(OrganizationEmployeeId.class)
public class OrganizationEmployee {
    
    @Id
    @Column(name = "organization_id")
    private Long organizationId;
    @Id
    @Column(name = "employee_id")
    private Long employeeId;
    
    public OrganizationEmployee() {
    }

    public OrganizationEmployee(Long organizationId, Long employeeId) {
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

package rs.etf.pia.sportsphere.dto;

public class RegisterResponse {
    private Long id;
    private String username;
    private String role;
    private String status;
    private String message;

    public RegisterResponse() {
    }

    public RegisterResponse(Long id, String username, String role, String status, String message) {
        this.id = id;
        this.username = username;
        this.role = role;
        this.status = status;
        this.message = message;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}

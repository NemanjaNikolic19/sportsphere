package rs.etf.pia.sportsphere.controller;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import rs.etf.pia.sportsphere.dto.LoginRequest;
import rs.etf.pia.sportsphere.dto.LoginResponse;
import rs.etf.pia.sportsphere.dto.RegisterRequest;
import rs.etf.pia.sportsphere.dto.RegisterResponse;
import rs.etf.pia.sportsphere.service.AuthService;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    
    private AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request){
        try{
            LoginResponse response = authService.login(request);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", e.getMessage()));
        }
    }

    @PostMapping(
        value = "/register",
        consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResponseEntity<?> register(
            @RequestPart("request")
            RegisterRequest request,

            @RequestPart(
                value = "profileImage",
                required = false
            )
            MultipartFile profileImage) {

        try {
            RegisterResponse response =
                authService.register(
                    request,
                    profileImage
                );

            return ResponseEntity.ok(response);

        } catch(IllegalArgumentException e) {
            return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(
                    Map.of(
                        "message",
                        e.getMessage()
                    )
                );
        }
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(
        @RequestBody Map<String, String> data
    ) {
        try {
            String resetLink =
                authService.createPasswordResetLink(
                    data.get("identifier")
                );

            return ResponseEntity.ok(
                Map.of(
                    "message",
                    "Password reset link created",
                    "resetLink",
                    resetLink
                )
            );
        } catch(IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(
                Map.of("message", e.getMessage())
            );
        }
    }

    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(
        @RequestBody Map<String, String> data
    ) {
        try {
            authService.resetPassword(
                data.get("token"),
                data.get("newPassword")
            );

            return ResponseEntity.ok(
                Map.of(
                    "message",
                    "Password changed successfully"
                )
            );
        } catch(IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(
                Map.of("message", e.getMessage())
            );
        }
    }
}

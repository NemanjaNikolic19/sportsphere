package rs.etf.pia.sportsphere.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import rs.etf.pia.sportsphere.dto.LoginRequest;
import rs.etf.pia.sportsphere.dto.LoginResponse;
import rs.etf.pia.sportsphere.dto.RegisterRequest;
import rs.etf.pia.sportsphere.dto.RegisterResponse;
import rs.etf.pia.sportsphere.entity.AppUser;
import rs.etf.pia.sportsphere.entity.Organization;
import rs.etf.pia.sportsphere.entity.OrganizationEmployee;
import rs.etf.pia.sportsphere.entity.UserFavoriteSport;
import rs.etf.pia.sportsphere.repository.AppUserRepository;
import rs.etf.pia.sportsphere.repository.OrganizationEmployeeRepository;
import rs.etf.pia.sportsphere.repository.OrganizationRepository;
import rs.etf.pia.sportsphere.repository.SportRepository;
import rs.etf.pia.sportsphere.repository.UserFavoriteSportRepository;
import rs.etf.pia.sportsphere.util.PasswordUtil;
import org.springframework.web.multipart.MultipartFile;

@Service
@Transactional
public class AuthService {
    
    private static final long RESET_TOKEN_DURATION_MINUTES = 30;
    private static final String RESET_PAGE_URL = "http://localhost:4200/reset-password?token=";
    
    private final ProfileImageService profileImageService;

    private record PasswordResetData(
        Long userId,
        LocalDateTime expiresAt
    ) {
    }

    private final Map<String, PasswordResetData> passwordResetTokens = new ConcurrentHashMap<>();

    private final AppUserRepository appUserRepository;
    private final SportRepository sportRepository;
    private final UserFavoriteSportRepository userFavoriteSportRepository;
    private final OrganizationRepository organizationRepository;
    private final OrganizationEmployeeRepository organizationEmployeeRepository;

    public AuthService(AppUserRepository appUserRepository, SportRepository sportRepository,
            UserFavoriteSportRepository userFavoriteSportRepository, OrganizationRepository organizationRepository,
            OrganizationEmployeeRepository organizationEmployeeRepository, ProfileImageService profileImageService) {
        this.appUserRepository = appUserRepository;
        this.sportRepository = sportRepository;
        this.userFavoriteSportRepository = userFavoriteSportRepository;
        this.organizationRepository = organizationRepository;
        this.organizationEmployeeRepository = organizationEmployeeRepository;
        this.profileImageService = profileImageService;
    }

    private List<Long> normalizeFavoriteSportIds(List<Long> favoriteSportIds) {
        List<Long> normalizedIds = new ArrayList<>();

        if(favoriteSportIds == null) {
            return normalizedIds;
        }

        Set<Long> uniqueIds = new LinkedHashSet<>();

        for(Long sportId: favoriteSportIds) {
            if(sportId != null) {
                uniqueIds.add(sportId);
            }
        }

        normalizedIds.addAll(uniqueIds);

        return normalizedIds;
    }

    private void saveFavoriteSports(Long userId, List<Long> favoriteSportIds) {
        for(Long sportId: favoriteSportIds) {
            userFavoriteSportRepository.save(new UserFavoriteSport(userId, sportId));
        }
    }

    private void validatePassword(String password) {
        String passwordRegex = "^(?=.{8,12}$)(?=[A-Za-z])(?=.*[A-Z])(?=.*\\d)(?=.*[^A-Za-z0-9]).*$";

        if(!password.matches(passwordRegex)) {
            throw new IllegalArgumentException(
                "Password must have 8-12 characters, start with a letter, and contain uppercase letter, digit and special character"
            );
        }
    }

    private void validateEmail(String email) {
        String emailRegex = "^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$";

        if(!email.matches(emailRegex)) {
            throw new IllegalArgumentException("Invalid email format");
        }
    }

    private void validateFavoriteSportIds(List<Long> favoriteSportIds) {
        List<Long> uniqueIds = normalizeFavoriteSportIds(favoriteSportIds);

        if(uniqueIds.size() > 5) {
            throw new IllegalArgumentException("User can have at most five favorite sports");
        }

        for(Long sportId: uniqueIds) {
            if(!sportRepository.existsById(sportId)) {
                throw new IllegalArgumentException("Sport with id " + sportId + " does not exist");
            }
        }
    }

    private void validateEmployeeRegisterRequest(RegisterRequest request) {
        if(request.getOrganizationName() == null || request.getOrganizationName().isBlank()) {
            throw new IllegalArgumentException("Organization name is required");
        }

        if(request.getHeadquartersAddress() == null || request.getHeadquartersAddress().isBlank()) {
            throw new IllegalArgumentException("Headquarters address is required");
        }

        if(request.getRegistrationNumber() == null || request.getRegistrationNumber().isBlank()) {
            throw new IllegalArgumentException("Registration number is required");
        }

        if(request.getTaxNumber() == null || request.getTaxNumber().isBlank()) {
            throw new IllegalArgumentException("Tax number is required");
        }

        String registrationNumber = request.getRegistrationNumber().trim();
        String taxNumber = request.getTaxNumber().trim();

        if(!registrationNumber.matches("^\\d{8}$")) {
            throw new IllegalArgumentException("Registration number must have exactly 8 digits");
        }

        if(!taxNumber.matches("^[1-9]\\d{8}$")) {
            throw new IllegalArgumentException("Tax number must have exactly 9 digits and must not start with 0");
        }

        Organization existingOrganization = organizationRepository.findByRegistrationNumber(registrationNumber)
                .orElse(null);

        if(existingOrganization != null) {
            if(!existingOrganization.getTaxNumber().equals(taxNumber)) {
                throw new IllegalArgumentException("Registration number and tax number do not match existing organization");
            }

            long employeeCount = organizationEmployeeRepository.countByOrganizationId(existingOrganization.getId());

            if(employeeCount >= 2) {
                throw new IllegalArgumentException("Organization already has maximum number of employees");
            }
        } else {
            if(organizationRepository.findByTaxNumber(taxNumber).isPresent()) {
                throw new IllegalArgumentException("Tax number already exists");
            }
        }
    }  

    private void validateRegisterRequest(RegisterRequest request){
        if(request == null) {
            throw new IllegalArgumentException("Registration request is required");
        }

        if(request.getUsername() == null || request.getUsername().isBlank()) {
            throw new IllegalArgumentException("Username is required");
        }

        if(request.getPassword() == null || request.getPassword().isBlank()) {
            throw new IllegalArgumentException("Password is required");
        }

        if(request.getRole() == null || request.getRole().isBlank()) {
            throw new IllegalArgumentException("Role is required");
        }

        String role = request.getRole().trim().toUpperCase();

        if(!"ATHLETE".equals(role) && !"EMPLOYEE".equals(role)) {
            throw new IllegalArgumentException("Role must be ATHLETE or EMPLOYEE");
        }

        if(request.getFirstName() == null || request.getFirstName().isBlank()) {
            throw new IllegalArgumentException("First name is required");
        }

        if(request.getLastName() == null || request.getLastName().isBlank()) {
            throw new IllegalArgumentException("Last name is required");
        }

        if(request.getEmail() == null || request.getEmail().isBlank()) {
            throw new IllegalArgumentException("Email is required");
        }

        validatePassword(request.getPassword());
        validateEmail(request.getEmail().trim());
        validateFavoriteSportIds(request.getFavoriteSportIds());

        if(appUserRepository.existsByUsername(request.getUsername().trim())) {
            throw new IllegalArgumentException("Username already exists");
        }

        if(appUserRepository.existsByEmail(request.getEmail().trim())) {
            throw new IllegalArgumentException("Email already exists");
        }

        if("EMPLOYEE".equals(role)) {
            validateEmployeeRegisterRequest(request);
        }
    }

    private void createOrAttachOrganization(Long employeeId, RegisterRequest request) {
        String organizationName = request.getOrganizationName().trim();
        String headquartersAddress = request.getHeadquartersAddress().trim();
        String registrationNumber = request.getRegistrationNumber().trim();
        String taxNumber = request.getTaxNumber().trim();

        Organization organization = organizationRepository.findByRegistrationNumber(registrationNumber)
                .orElseGet(() -> {
                    Organization newOrganization = new Organization();
                    newOrganization.setName(organizationName);
                    newOrganization.setHeadquartersAddress(headquartersAddress);
                    newOrganization.setRegistrationNumber(registrationNumber);
                    newOrganization.setTaxNumber(taxNumber);

                    return organizationRepository.save(newOrganization);
                });

        organizationEmployeeRepository.save(new OrganizationEmployee(organization.getId(), employeeId));
    }

    private String optional(String value) {
        if(value == null || value.trim().isEmpty()) {
            return null;
        }

        return value.trim();
    }

    public LoginResponse login(LoginRequest request){
        if(request == null || request.getUsername() == null || request.getPassword() == null){
            throw new IllegalArgumentException("Invalid credentials");
        }

        String username = request.getUsername().trim();

        AppUser user = appUserRepository.findByUsername(username).orElseThrow(() -> new IllegalArgumentException("Invalid credentials"));

        if(!"ACTIVE".equalsIgnoreCase(user.getStatus())){
            throw new IllegalArgumentException("User account is not active");
        }

        boolean passwordMatches = PasswordUtil.matches(
            request.getPassword(),
            user.getPasswordHash()
        );

        if(!passwordMatches)
            throw new IllegalArgumentException("Invalid credentials");

        return new LoginResponse(
            user.getId(),
            user.getUsername(),
            user.getRole(),
            user.getStatus(),
            user.getFirstName(), 
            user.getLastName(),
            user.getEmail(),
            user.getProfileImagePath()
        );
    }

    public RegisterResponse register(RegisterRequest request, MultipartFile profileImage) {
        validateRegisterRequest(request);

        String profileImagePath = profileImageService.saveOrDefault(profileImage);

        String username = request.getUsername().trim();
        String password = request.getPassword();
        String role = request.getRole().trim().toUpperCase();

        String firstName = request.getFirstName().trim();
        String lastName = request.getLastName().trim();
        String email = request.getEmail().trim();

        List<Long> favoriteSportIds = normalizeFavoriteSportIds(request.getFavoriteSportIds());

        AppUser user = new AppUser();
        user.setUsername(username);
        user.setPasswordHash(PasswordUtil.sha256(password));
        user.setRole(role);
        user.setStatus("PENDING");
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setPhone(optional(request.getPhone()));
        user.setEmail(email);
        user.setProfileImagePath(profileImagePath);

        AppUser savedUser = appUserRepository.save(user);

        saveFavoriteSports(savedUser.getId(), favoriteSportIds);

        if("EMPLOYEE".equals(role)) {
            createOrAttachOrganization(savedUser.getId(), request);
        }

        return new RegisterResponse(
            savedUser.getId(),
            savedUser.getUsername(),
            savedUser.getRole(),
            savedUser.getStatus(),
            "Registration request submitted. Waiting for admin approval."
        );
    }

    public String createPasswordResetLink(String identifier) {
        if(identifier == null || identifier.isBlank()) {
            throw new IllegalArgumentException(
                "Username or email is required"
            );
        }

        String trimmedIdentifier = identifier.trim();

        AppUser user = appUserRepository
            .findByUsernameOrEmail(
                trimmedIdentifier,
                trimmedIdentifier
            )
            .orElseThrow(() ->
                new IllegalArgumentException("User not found")
            );

        passwordResetTokens.entrySet().removeIf(
            entry -> entry.getValue().userId().equals(user.getId())
        );

        String token = UUID.randomUUID().toString();

        PasswordResetData resetData = new PasswordResetData(
            user.getId(),
            LocalDateTime.now()
                .plusMinutes(RESET_TOKEN_DURATION_MINUTES)
        );

        passwordResetTokens.put(token, resetData);

        return RESET_PAGE_URL + token;
    }

    public void resetPassword(String token, String newPassword) {
        if(token == null || token.isBlank()) {
            throw new IllegalArgumentException(
                "Reset token is required"
            );
        }

        if(newPassword == null || newPassword.isBlank()) {
            throw new IllegalArgumentException(
                "New password is required"
            );
        }

        validatePassword(newPassword);

        PasswordResetData resetData =
            passwordResetTokens.get(token);

        if(resetData == null) {
            throw new IllegalArgumentException(
                "Invalid reset link"
            );
        }

        if(!resetData.expiresAt().isAfter(LocalDateTime.now())) {
            passwordResetTokens.remove(token);

            throw new IllegalArgumentException(
                "Reset link has expired"
            );
        }

        AppUser user = appUserRepository
            .findById(resetData.userId())
            .orElseThrow(() ->
                new IllegalArgumentException("User not found")
            );

        user.setPasswordHash(
            PasswordUtil.sha256(newPassword)
        );

        appUserRepository.save(user);

        // The link may only be used once.
        passwordResetTokens.remove(token);
    }
}

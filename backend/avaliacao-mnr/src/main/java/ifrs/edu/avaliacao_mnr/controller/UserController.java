package ifrs.edu.avaliacao_mnr.controller;

import ifrs.edu.avaliacao_mnr.dto.CreateUserRequest;
import ifrs.edu.avaliacao_mnr.dto.UserResponse;
import ifrs.edu.avaliacao_mnr.enums.Role;
import ifrs.edu.avaliacao_mnr.model.User;
import ifrs.edu.avaliacao_mnr.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedModel;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

@RestController
@RequestMapping("/users")
@Tag(name = "Users", description = "ADMIN-only user administration")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('USER_MANAGE')")
    @Operation(summary = "List users")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Users returned"),
        @ApiResponse(responseCode = "401", description = "Access token is missing, invalid, or expired"),
        @ApiResponse(responseCode = "403", description = "ADMIN permission required")
    })
    public PagedModel<UserResponse> listUsers(@RequestParam(required = false) String search,
            @RequestParam(required = false) Role role, @RequestParam(required = false) Boolean active,
            @PageableDefault(size = 20) Pageable pageable) {
        return new PagedModel<>(userService.searchUsers(search, role, active, pageable).map(UserResponse::from));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('USER_MANAGE')")
    @Operation(summary = "Get user details")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "User returned"),
        @ApiResponse(responseCode = "401", description = "Access token is missing, invalid, or expired"),
        @ApiResponse(responseCode = "403", description = "ADMIN permission required"),
        @ApiResponse(responseCode = "404", description = "User not found")
    })
    public UserResponse getUser(@PathVariable UUID id) {
        return UserResponse.from(userService.findById(id));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('USER_MANAGE')")
    @Operation(summary = "Create a user", description = "Creates an EVALUATOR or ADMIN account. Passwords must contain at least 12 characters.")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "User created"),
        @ApiResponse(responseCode = "400", description = "Request validation failed"),
        @ApiResponse(responseCode = "401", description = "Access token is missing, invalid, or expired"),
        @ApiResponse(responseCode = "403", description = "ADMIN permission required"),
        @ApiResponse(responseCode = "409", description = "Email already exists")
    })
    public ResponseEntity<UserResponse> createUser(@Valid @RequestBody CreateUserRequest request) {
        User user = new User();
        user.setName(request.name().trim());
        user.setSurname(request.surname().trim());
        user.setEmail(request.email().trim());
        user.setCpf(request.cpf().trim());
        user.setRole(request.role());
        try {
            User created = userService.createUser(user, request.password());
            return ResponseEntity.status(HttpStatus.CREATED).body(UserResponse.from(created));
        } catch (RuntimeException exception) {
            if (exception.getMessage() != null && exception.getMessage().contains("already used")) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "E-mail already exists");
            }
            throw exception;
        }
    }
}
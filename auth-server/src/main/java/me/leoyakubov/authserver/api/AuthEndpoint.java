package me.leoyakubov.authserver.api;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import me.leoyakubov.authserver.exception.BadRequestException;
import me.leoyakubov.authserver.exception.EmailAlreadyExistsException;
import me.leoyakubov.authserver.exception.UsernameAlreadyExistsException;
import me.leoyakubov.authserver.model.Profile;
import me.leoyakubov.authserver.model.Role;
import me.leoyakubov.authserver.model.User;
import me.leoyakubov.authserver.payload.ApiResponse;
import me.leoyakubov.authserver.payload.FacebookLoginRequest;
import me.leoyakubov.authserver.payload.JwtAuthenticationResponse;
import me.leoyakubov.authserver.payload.LoginRequest;
import me.leoyakubov.authserver.payload.SignUpRequest;
import me.leoyakubov.authserver.service.FacebookService;
import me.leoyakubov.authserver.service.UserService;

import java.net.URI;

@RestController
@Slf4j
public class AuthEndpoint {
    private final UserService userService;
    private final FacebookService facebookService;

    public AuthEndpoint(UserService userService, FacebookService facebookService) {
        this.userService = userService;
        this.facebookService = facebookService;
    }

    @PostMapping("/signin")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {
        String token = userService.loginUser(loginRequest.getUsername(), loginRequest.getPassword());
        return ResponseEntity.ok(new JwtAuthenticationResponse(token));
    }

    @PostMapping("/facebook/signin")
    public ResponseEntity<?> facebookAuth(@Valid @RequestBody FacebookLoginRequest facebookLoginRequest) {
        log.info("facebook login {}", facebookLoginRequest);
        String token = facebookService.loginUser(facebookLoginRequest.getAccessToken());
        return ResponseEntity.ok(new JwtAuthenticationResponse(token));
    }

    @PostMapping(value = "/users", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> createUser(@Valid @RequestBody SignUpRequest payload) {
        log.info("creating user {}", payload.getUsername());

        User user = User
                .builder()
                .username(payload.getUsername())
                .email(payload.getEmail())
                .password(payload.getPassword())
                .userProfile(Profile
                        .builder()
                        .displayName(payload.getName())
                        .build())
                .build();

        try {
            userService.registerUser(user, Role.USER);
        } catch (UsernameAlreadyExistsException | EmailAlreadyExistsException e) {
            throw new BadRequestException(e.getMessage());
        }

        URI location = ServletUriComponentsBuilder
                .fromCurrentContextPath().path("/users/{username}")
                .buildAndExpand(user.getUsername()).toUri();

        return ResponseEntity
                .created(location)
                .body(new ApiResponse(true, "User registered successfully"));
    }
}

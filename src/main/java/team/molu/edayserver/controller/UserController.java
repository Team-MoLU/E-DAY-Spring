package team.molu.edayserver.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import team.molu.edayserver.domain.Jwt;
import team.molu.edayserver.domain.Oauth;
import team.molu.edayserver.domain.User;
import team.molu.edayserver.service.UserService;

@RestController
@RequestMapping("api/v1/users")
@Slf4j
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public ResponseEntity<User> getUserByEmail(@RequestParam String email) {
        User user = userService.findUserByEmail(email);
        return ResponseEntity.ok(user);
    }

    @PostMapping
    public ResponseEntity<Void> createUser(@RequestBody User user) {
        userService.createUser(user);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PutMapping
    public ResponseEntity<Void> updateUser(@RequestBody User user) {
        boolean updated = userService.updateUser(user);
        if (updated) {
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.notFound().build();
    }

    @DeleteMapping
    public ResponseEntity<Void> deleteUser(@RequestParam String email) {
        boolean deleted = userService.deleteUserByEmail(email);
        if (deleted) {
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/oauth")
    public ResponseEntity<Void> createUserOauth(@RequestParam String email, @RequestBody Oauth oauth) {
        userService.createUserOauth(oauth, email);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PostMapping("/jwt")
    public ResponseEntity<Void> createUserJwt(@RequestParam String email, @RequestBody Jwt jwt) {
        userService.createUserJwt(jwt, email);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
}

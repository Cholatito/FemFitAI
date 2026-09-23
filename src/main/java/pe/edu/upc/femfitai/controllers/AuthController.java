package pe.edu.upc.femfitai.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import pe.edu.upc.femfitai.dtos.LoginRequestDTO;
import pe.edu.upc.femfitai.dtos.LoginResponseDTO;
import pe.edu.upc.femfitai.securities.JwtTokenService;

@RestController
public class AuthController {
    private final AuthenticationManager authenticationManager;
    private final JwtTokenService jwtTokenService;

    public AuthController(AuthenticationManager authenticationManager,
                          JwtTokenService jwtTokenService) {
        this.authenticationManager = authenticationManager;
        this.jwtTokenService = jwtTokenService;
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponseDTO> login(@RequestBody LoginRequestDTO datos) {
        if (datos == null || datos.getCorreo() == null || datos.getCorreo().isBlank()
                || datos.getPassword() == null || datos.getPassword().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Correo y password son obligatorios");
        }

        try {
            Authentication autenticacion = authenticationManager.authenticate(
                    UsernamePasswordAuthenticationToken.unauthenticated(
                            datos.getCorreo(), datos.getPassword()));
            UserDetails usuario = (UserDetails) autenticacion.getPrincipal();
            String token = jwtTokenService.generateToken(usuario);
            String rol = usuario.getAuthorities().stream().findFirst()
                    .map(authority -> authority.getAuthority().replaceFirst("^ROLE_", ""))
                    .orElse("TESTER");
            return ResponseEntity.ok(new LoginResponseDTO(token, "Bearer", rol));
        } catch (AuthenticationException ex) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED,
                    "Credenciales inválidas");
        }
    }
}

package com.carlos.todoapi.service;


import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.carlos.todoapi.config.JwtService;
import com.carlos.todoapi.domain.bean.Usuario;
import com.carlos.todoapi.domain.dto.usuario.LoginRequest;
import com.carlos.todoapi.domain.dto.usuario.UsuarioRequest;
import com.carlos.todoapi.exception.ApiException;
import com.carlos.todoapi.factory.UsuarioFactory;
import com.carlos.todoapi.repository.UsuarioRepository;
import com.carlos.todoapi.service.AuthService;
import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

import java.util.Optional;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private JwtService jwtService;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private UsuarioFactory usuarioFactory;

    @InjectMocks
    private AuthService authService;

    // Métodos de teste:

    @Test
    void registrar_ComDadosValidos_DeveRetornarToken() {
        // Arrange
        UsuarioRequest request = new UsuarioRequest("João", "joao@email.com", "Senha@123");
        Usuario usuario = new Usuario(1L, "João", "joao@email.com", "senhaCriptografada");

        when(usuarioRepository.findByEmail("joao@email.com")).thenReturn(Optional.empty());
        when(usuarioFactory.criarNovoUsuario(request)).thenReturn(usuario);
        doNothing().when(usuarioRepository).save(usuario);
        when(jwtService.generateToken(usuario)).thenReturn("token123");

        // Act
        String token = authService.registrar(request);

        // Assert
        assertEquals("token123", token);
        verify(usuarioRepository).save(usuario);
    }

    @Test
    void registrar_ComEmailExistente_DeveLancarExcecao() {
        // Arrange
        UsuarioRequest request = new UsuarioRequest("João", "joao@email.com", "Senha@123");
        Usuario usuarioExistente = new Usuario(1L, "João", "joao@email.com", "senhaCriptografada");

        when(usuarioRepository.findByEmail("joao@email.com")).thenReturn(Optional.of(usuarioExistente));

        // Act & Assert
        ApiException exception = assertThrows(ApiException.class, () -> {
            authService.registrar(request);
        });

        assertEquals("Email já cadastrado", exception.getMessage());
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
        verify(usuarioRepository, never()).save(any());
    }

    @Test
    void registrar_QuandoFactoryFalhar_DeveLancarExcecao() {
        // Arrange
        UsuarioRequest request = new UsuarioRequest("João", "joao@email.com", "Senha@123");

        when(usuarioRepository.findByEmail("joao@email.com")).thenReturn(Optional.empty());
        when(usuarioFactory.criarNovoUsuario(request))
                .thenThrow(new RuntimeException("Erro na factory"));

        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            authService.registrar(request);
        });

        verify(usuarioRepository, never()).save(any());
    }

    @Test
    void registrar_QuandoRepositorioFalhar_DeveLancarExcecao() {
        // Arrange
        UsuarioRequest request = new UsuarioRequest("João", "joao@email.com", "Senha@123");
        Usuario usuario = new Usuario(1L, "João", "joao@email.com", "senhaCriptografada");

        when(usuarioRepository.findByEmail("joao@email.com")).thenReturn(Optional.empty());
        when(usuarioFactory.criarNovoUsuario(request)).thenReturn(usuario);
        doThrow(new RuntimeException("Erro no banco")).when(usuarioRepository).save(usuario);

        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            authService.registrar(request);
        });
    }

    @Test
    void registrar_QuandoGeracaoTokenFalhar_DeveLancarExcecao() {
        // Arrange
        UsuarioRequest request = new UsuarioRequest("João", "joao@email.com", "Senha@123");
        Usuario usuario = new Usuario(1L, "João", "joao@email.com", "senhaCriptografada");

        when(usuarioRepository.findByEmail("joao@email.com")).thenReturn(Optional.empty());
        when(usuarioFactory.criarNovoUsuario(request)).thenReturn(usuario);
        doNothing().when(usuarioRepository).save(usuario);
        when(jwtService.generateToken(usuario))
                .thenThrow(new RuntimeException("Erro ao gerar token"));

        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            authService.registrar(request);
        });
    }


    @Test
    void autenticar_ComCredenciaisValidas_DeveRetornarToken() {
        // Arrange
        LoginRequest request = new LoginRequest("joao@email.com", "Senha@123");
        Usuario usuario = new Usuario(1L, "João", "joao@email.com", "senhaCriptografada");

        when(usuarioRepository.findByEmail("joao@email.com")).thenReturn(Optional.of(usuario));
        when(jwtService.generateToken(usuario)).thenReturn("token123");

        // Act
        String token = authService.autenticar(request);

        // Assert
        assertEquals("token123", token);
        verify(authenticationManager).authenticate(
                new UsernamePasswordAuthenticationToken("joao@email.com", "Senha@123")
        );
    }

        @Test
        void autenticar_ComCredenciaisInvalidas_DeveLancarExcecao() {
            // Arrange
            LoginRequest request = new LoginRequest("joao@email.com", "senhaErrada");

            doThrow(new BadCredentialsException("Credenciais inválidas"))
                    .when(authenticationManager).authenticate(any());

            // Act & Assert
            assertThrows(BadCredentialsException.class, () -> {
                authService.autenticar(request);
            });

            verify(usuarioRepository, never()).findByEmail(any());
            verify(jwtService, never()).generateToken(any());
        }

    @Test
    void autenticar_QuandoUsuarioNaoExiste_DeveLancarExcecao() {
        // Arrange
        LoginRequest request = new LoginRequest("inexistente@email.com", "Senha@123");

        when(usuarioRepository.findByEmail("inexistente@email.com")).thenReturn(Optional.empty());

        // Act & Assert
        ApiException exception = assertThrows(ApiException.class, () -> {
            authService.autenticar(request);
        });

        assertEquals("Usuário não encontrado", exception.getMessage());
        assertEquals(HttpStatus.NOT_FOUND, exception.getStatus());
    }

    @Test
    void autenticar_QuandoGeracaoTokenFalha_DeveLancarExcecao() {
        // Arrange
        LoginRequest request = new LoginRequest("joao@email.com", "Senha@123");
        Usuario usuario = new Usuario(1L, "João", "joao@email.com", "senhaCriptografada");

        when(usuarioRepository.findByEmail("joao@email.com")).thenReturn(Optional.of(usuario));
        when(jwtService.generateToken(usuario)).thenThrow(new RuntimeException("Erro ao gerar token"));

        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            authService.autenticar(request);
        });
    }

        @Test
        void autenticar_ComEmailInvalido_DeveLancarExcecaoDeValidacao() {
            // Arrange
            LoginRequest request = new LoginRequest("email-invalido", "Senha@123");

            // Act & Assert
            assertThrows(ApiException.class, () -> {
                authService.autenticar(request);
            });
        }

        @Test
        void autenticar_ComSenhaVazia_DeveLancarExcecaoDeValidacao() {
            // Arrange
            LoginRequest request = new LoginRequest("joao@email.com", "");

            // Act & Assert
            assertThrows(ApiException.class, () -> {
                authService.autenticar(request);
            });
        }
}

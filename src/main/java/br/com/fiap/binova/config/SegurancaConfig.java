
package br.com.fiap.binova.config;

import br.com.fiap.binova.repository.UsuarioRepository;
import br.com.fiap.binova.security.JwtFiltro;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

@Configuration
@EnableMethodSecurity
@RequiredArgsConstructor
public class SegurancaConfig {

    private final UsuarioRepository usuarios;

    // Criptografa as senhas dos usuários
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // Busca os usuários e suas permissões no H2
    @Bean
    public UserDetailsService userDetailsService() {
        return email -> usuarios.findByEmailIgnoreCase(email)
                .map(usuario -> User.withUsername(usuario.getEmail())
                        .password(usuario.getSenhaHash())
                        .roles(usuario.getPapel().name())
                        .build())
                .orElseThrow(() -> new UsernameNotFoundException("Usuario nao encontrado"));
    }

    // Configura a autenticação por e-mail e senha
    @Bean
    public AuthenticationManager authenticationManager(UserDetailsService detalhes, PasswordEncoder codificador) {

        var provedor = new DaoAuthenticationProvider(detalhes);
        provedor.setPasswordEncoder(codificador);
        return new ProviderManager(provedor);
    }

    // Define as rotas públicas e protegidas por JWT
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, JwtFiltro filtro,
                                           @Qualifier("corsConfigurationSource") CorsConfigurationSource cors) throws Exception {

        return http.csrf(csrf -> csrf.disable())
                .headers(headers -> headers.frameOptions(frame -> frame.sameOrigin()))
                .cors(corsConfig -> corsConfig.configurationSource(cors))
                .sessionManagement(sessao -> sessao.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(rotas -> rotas
                        .requestMatchers("/api/auth/**", "/swagger-ui.html", "/swagger-ui/**", "/v3/api-docs/**",
                                "/h2-console/**", "/actuator/health", "/actuator/info", "/error").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/pontos-coleta", "/api/pontos-coleta/busca-nome",
                                "/api/pontos-coleta/*", "/api/pontos-coleta/*/situacao").permitAll()
                        .requestMatchers("/actuator/metrics", "/actuator/metrics/**").hasRole("ADMIN")
                        .anyRequest().authenticated())
                .exceptionHandling(erro -> erro
                        .authenticationEntryPoint((req, res, e) -> {
                            res.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                            res.setContentType("application/json;charset=UTF-8");
                            res.getWriter().write("{\"status\":401,\"mensagem\":\"Autenticacao necessaria\"}");
                        })

                        .accessDeniedHandler((req, res, e) -> {
                            res.setStatus(HttpServletResponse.SC_FORBIDDEN);
                            res.setContentType("application/json;charset=UTF-8");
                            res.getWriter().write("{\"status\":403,\"mensagem\":\"Acesso negado\"}");
                        }))
                .addFilterBefore(filtro, UsernamePasswordAuthenticationFilter.class)
                .build();
    }

    // Impede que o filtro JWT seja registrado duas vezes
    @Bean
    public FilterRegistrationBean<JwtFiltro> registrarSomenteNaSeguranca(JwtFiltro filtro) {
        var registro = new FilterRegistrationBean<>(filtro);
        registro.setEnabled(false);
        return registro;
    }

    // Configura as origens permitidas para acessar a API
    @Bean
    public CorsConfigurationSource corsConfigurationSource(@Value("${app.cors.origens}") String origens) {
        CorsConfiguration config = new CorsConfiguration();

        config.setAllowedOrigins(Arrays.stream(origens.split(","))
                .map(String::trim).filter(s -> !s.isBlank()).toList());

        config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("Authorization", "Content-Type", "Accept"));
        config.setExposedHeaders(List.of("Location"));

        UrlBasedCorsConfigurationSource fonte = new UrlBasedCorsConfigurationSource();
        fonte.registerCorsConfiguration("/**", config);

        return fonte;
    }
}

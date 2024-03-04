package me.leoyakubov.authserver.config;


import jakarta.servlet.http.HttpServletResponse;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.BeanIds;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;
import me.leoyakubov.authserver.service.JwtTokenProvider;
import me.leoyakubov.authserver.service.UserService;

@Configuration
@EnableWebSecurity
//@EnableGlobalMethodSecurity(prePostEnabled = true)
public class SecurityCredentialsConfig {

    private final JwtConfigProperties jwtConfig;
    private final JwtTokenProvider tokenProvider;
    private final UserService userService;
    private final UserDetailsService userDetailsService;

    public SecurityCredentialsConfig(JwtConfigProperties jwtConfig,
                                     JwtTokenProvider tokenProvider,
                                     UserService userService,
                                     UserDetailsService userDetailsService) {
        this.jwtConfig = jwtConfig;
        this.tokenProvider = tokenProvider;
        this.userService = userService;
        this.userDetailsService = userDetailsService;
    }

    @Bean
    public SecurityFilterChain myFilterChain(HttpSecurity http) throws Exception {
       http
               //.cors(Customizer.withDefaults())
               .cors(httpSecurityCorsConfigurer -> httpSecurityCorsConfigurer.configurationSource(сorsConfigurationSource()))
               .csrf(httpSecurityCsrfConfigurer -> httpSecurityCsrfConfigurer.disable())
               .sessionManagement(sess -> sess.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
               .exceptionHandling(exConf -> exConf.authenticationEntryPoint((req, resp, ex) -> resp.sendError
               (HttpServletResponse.SC_UNAUTHORIZED)))
               .addFilterBefore(new JwtTokenAuthenticationFilter(jwtConfig, tokenProvider, userService),
               UsernamePasswordAuthenticationFilter.class)
               .authorizeHttpRequests((authz) -> authz
                       .requestMatchers(HttpMethod.POST, "/signin").permitAll()
                       .requestMatchers(HttpMethod.POST, "/facebook/signin").permitAll()
                       .requestMatchers(HttpMethod.POST, "/users").anonymous()
                       .anyRequest().authenticated()
               );
        return http.build();
    }

    @Bean
    public CorsFilter corsFilter() {
        final UrlBasedCorsConfigurationSource source = сorsConfigurationSource();
        return new CorsFilter(source);
    }

    private static UrlBasedCorsConfigurationSource сorsConfigurationSource() {
        final UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        final CorsConfiguration config = new CorsConfiguration();
        config.setAllowCredentials(true);
        config.addAllowedOrigin("*");
        config.addAllowedHeader("*");
        config.addAllowedMethod("OPTIONS");
        config.addAllowedMethod("HEAD");
        config.addAllowedMethod("GET");
        config.addAllowedMethod("PUT");
        config.addAllowedMethod("POST");
        config.addAllowedMethod("DELETE");
        config.addAllowedMethod("PATCH");
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    @Bean(BeanIds.AUTHENTICATION_MANAGER)
    public AuthenticationManager authenticationManager(AuthenticationManagerBuilder auth) {
        // Configure DB authentication provider for user accounts
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return new ProviderManager(authProvider);
    }

    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder builder) {
        return builder.build();
    }
}
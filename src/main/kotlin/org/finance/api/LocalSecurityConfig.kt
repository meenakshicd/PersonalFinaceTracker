package org.finance.api

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.AnonymousAuthenticationFilter
import org.springframework.web.filter.OncePerRequestFilter
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse

@Configuration
@EnableMethodSecurity
@Profile("local")
class LocalSecurityConfig {
    @Bean
    fun localSecurityFilterChain(http: HttpSecurity): SecurityFilterChain =
        http
            .csrf { it.disable() }
            .sessionManagement { it.sessionCreationPolicy(SessionCreationPolicy.STATELESS) }
            .authorizeHttpRequests { it.anyRequest().permitAll() }
            .addFilterBefore(LocalTestAuthenticationFilter(), AnonymousAuthenticationFilter::class.java)
            .build()
}

private class LocalTestAuthenticationFilter : OncePerRequestFilter() {
    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain,
    ) {
        val jwt = Jwt.withTokenValue("local-test-token")
            .header("alg", "none")
            .subject("dummy-idp-subject-client-01")
            .build()
        SecurityContextHolder.getContext().authentication = JwtAuthenticationToken(
            jwt,
            listOf(SimpleGrantedAuthority("SCOPE_finance.admin")),
        )
        filterChain.doFilter(request, response)
    }
}

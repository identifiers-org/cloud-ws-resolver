package org.identifiers.cloud.ws.resolver.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;

@Configuration
@EnableWebSecurity
@Profile("production")
public class AuthorizationConfiguration extends WebSecurityConfigurerAdapter {

    @Value("${org.identifiers.cloud.ws.resolver.requiredrole}")
    String requiredRole;

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        String aExp = String.format(
                "isAuthenticated() and principal.claims['realm_access']['roles'].contains('%s')",
                requiredRole
        );

        http.authorizeRequests()
                .antMatchers("/actuator").access(aExp)
                .antMatchers("/actuator/loggers/**").access(aExp)
//                .antMatchers("/insightApi/**").permitAll()
//                .antMatchers(HttpMethod.GET, "/resolveMirId/**").permitAll()
//                .antMatchers(HttpMethod.GET, "/actuators/health/**").permitAll()
                .antMatchers(HttpMethod.GET, "/**").permitAll()
            .and().cors()
            .and().anonymous()
            .and()
                .csrf().disable()
                .logout().disable()
                .formLogin().disable()
                .oauth2ResourceServer().jwt();
    }
}

package de.astahsrm.gremiomat.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.BeanIds;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {
    public static final String ADMIN = "ADMIN";
    public static final String USER = "USER";

    /* 
    @Autowired
    private MgmtUserDetailsService mgmtUserDetailsService;
    */

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.authorizeRequests().antMatchers("/assets/**", "/css/**", "/favicon/**", "/img/**").permitAll()
                .antMatchers("/gremien/**", "/gremien**").permitAll()
                .antMatchers("/mgmt", "/logout").permitAll()
                .antMatchers(HttpMethod.DELETE).hasRole(ADMIN)
                .antMatchers("/user*", "/user/*").authenticated()
                .anyRequest().hasAnyRole(ADMIN)
                .and()
                .formLogin()
                .defaultSuccessUrl("/mgmt")
                .permitAll()
                .and()
                .logout()
                .logoutUrl("/logout")
                .logoutSuccessUrl("/")
                .permitAll();
    }

    @Override
    protected void configure(AuthenticationManagerBuilder authmanagerbuilder) throws Exception {
        // For testing purposes:
        PasswordEncoder pwenc = passwordEncoder();
        authmanagerbuilder.inMemoryAuthentication()
            .withUser("user").password(pwenc.encode("user")).roles(USER)
        .and()
            .withUser("admin").password(pwenc.encode("admin")).roles(ADMIN);
        /* User-auth with DB:
        authmanagerbuilder.userDetailsService(mgmtUserDetailsService).passwordEncoder(passwordEncoder());
        */
    }

    @Bean(name = BeanIds.AUTHENTICATION_MANAGER)
    @Override
    public AuthenticationManager authenticationManagerBean() throws Exception {
        return super.authenticationManagerBean();
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }
}
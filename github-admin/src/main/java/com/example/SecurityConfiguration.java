package com.example;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;
import org.springframework.security.web.authentication.preauth.AbstractPreAuthenticatedProcessingFilter;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;
import org.springframework.security.web.csrf.CsrfFilter;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.security.web.csrf.CsrfTokenRepository;
import org.springframework.security.web.csrf.HttpSessionCsrfTokenRepository;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.WebUtils;

@Configuration
public class SecurityConfiguration extends WebSecurityConfigurerAdapter {

	@Autowired
	private MutableCloudCredentials cloudCredentials;

	@Override
	public void configure(HttpSecurity http) throws Exception {
		http//
				.exceptionHandling()
				.authenticationEntryPoint(new LoginUrlAuthenticationEntryPoint("/auth"))//
				.and().authorizeRequests()//
				.antMatchers(HttpMethod.POST, "/api/applications").permitAll()//
				.antMatchers("/img/**", "/mgmt/health", "/auth", "/login*").permitAll()//
				.anyRequest().authenticated()//
				.and().csrf().ignoringAntMatchers("/api/**", "/mgmt/**")
				.csrfTokenRepository(csrfTokenRepository()).and()
				.addFilterBefore(authFilter(),
						AbstractPreAuthenticatedProcessingFilter.class)
				.addFilterAfter(csrfHeaderFilter(), CsrfFilter.class);
	}

	private Filter authFilter() {

		AbstractPreAuthenticatedProcessingFilter filter = new AbstractPreAuthenticatedProcessingFilter() {

			@Override
			protected Object getPreAuthenticatedPrincipal(HttpServletRequest request) {
				return "user";
			}

			@Override
			protected Object getPreAuthenticatedCredentials(HttpServletRequest request) {
				return "N/A";
			}
		};

		filter.setAuthenticationManager(authentication -> {
			if (this.cloudCredentials.getToken() != null) {
				return new PreAuthenticatedAuthenticationToken("user", "N/A",
						AuthorityUtils.commaSeparatedStringToAuthorityList("ROLE_USER"));
			}
			return null;
		});

		filter.afterPropertiesSet();
		return filter;

	}

	private Filter csrfHeaderFilter() {
		return new OncePerRequestFilter() {
			@Override
			protected void doFilterInternal(HttpServletRequest request,
					HttpServletResponse response, FilterChain filterChain)
							throws ServletException, IOException {
				CsrfToken csrf = (CsrfToken) request
						.getAttribute(CsrfToken.class.getName());
				if (csrf != null) {
					Cookie cookie = WebUtils.getCookie(request, "XSRF-TOKEN");
					String token = csrf.getToken();
					if (cookie == null
							|| token != null && !token.equals(cookie.getValue())) {
						cookie = new Cookie("XSRF-TOKEN", token);
						cookie.setPath("/");
						response.addCookie(cookie);
					}
				}
				filterChain.doFilter(request, response);
			}
		};
	}

	private CsrfTokenRepository csrfTokenRepository() {
		HttpSessionCsrfTokenRepository repository = new HttpSessionCsrfTokenRepository();
		repository.setHeaderName("X-XSRF-TOKEN");
		return repository;
	}

}

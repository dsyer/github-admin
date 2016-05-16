package com.example;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.boot.autoconfigure.security.oauth2.client.EnableOAuth2Sso;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.access.AccessDeniedHandlerImpl;
import org.springframework.security.web.csrf.CsrfFilter;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.security.web.csrf.CsrfTokenRepository;
import org.springframework.security.web.csrf.HttpSessionCsrfTokenRepository;
import org.springframework.security.web.savedrequest.HttpSessionRequestCache;
import org.springframework.security.web.savedrequest.RequestCache;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.WebUtils;

@Configuration
@EnableOAuth2Sso
public class SecurityConfiguration extends WebSecurityConfigurerAdapter {

	@Configuration
	protected static class RequestCacheConfiguration {
		@Bean
		public RequestCache savedRequestCache() {
			return new HttpSessionRequestCache() {
				@Override
				public void removeRequest(HttpServletRequest currentRequest,
						HttpServletResponse response) {
					Authentication authentication = SecurityContextHolder.getContext()
							.getAuthentication();
					if (authentication != null && authentication.getAuthorities()
							.contains(new SimpleGrantedAuthority("ROLE_SPACE"))) {
						super.removeRequest(currentRequest, response);
					}
				}
			};
		}
	}

	@Override
	public void configure(HttpSecurity http) throws Exception {
		http//
				.exceptionHandling().accessDeniedHandler(accessDeniedHandler()).and()//
				.authorizeRequests()//
				.antMatchers(HttpMethod.POST, "/api/applications").permitAll()//
				.antMatchers("/img/**", "/mgmt/health", "/login*").permitAll()//
				.antMatchers("/auth/**", "/spaces/**").authenticated()
				.anyRequest().hasRole("SPACE")//
				.and().csrf().ignoringAntMatchers("/api/**", "/mgmt/**")
				.csrfTokenRepository(csrfTokenRepository()).and()
				.addFilterAfter(csrfHeaderFilter(), CsrfFilter.class);
	}

	private AccessDeniedHandler accessDeniedHandler() {
		return new AccessDeniedHandlerImpl() {
			@Override
			public void handle(HttpServletRequest request, HttpServletResponse response,
					AccessDeniedException accessDeniedException)
					throws IOException, ServletException {
				Authentication authentication = SecurityContextHolder.getContext()
						.getAuthentication();
				if (authentication != null && !authentication.getAuthorities()
						.contains(new SimpleGrantedAuthority("ROLE_SPACE"))) {
					RequestDispatcher dispatcher = request
							.getRequestDispatcher("/auth");
					dispatcher.forward(request, response);
				}
				super.handle(request, response, accessDeniedException);
			}
		};
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

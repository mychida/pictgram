//セキュリティ設定クラス
package com.example.pictgram;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.servlet.util.matcher.MvcRequestMatcher;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.OrRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.web.servlet.handler.HandlerMappingIntrospector;

import com.example.pictgram.entity.SocialUser;
import com.example.pictgram.entity.User;
import com.example.pictgram.entity.User.Authority;
import com.example.pictgram.filter.FormAuthenticationProvider;
import com.example.pictgram.repository.UserRepository;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

	@Autowired
	private UserRepository repository;
	@Autowired
	private FormAuthenticationProvider authenticationProvider;

	protected static Logger log = LoggerFactory.getLogger(SecurityConfig.class);

	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http,
			HandlerMappingIntrospector introspector) throws Exception {
		//ログアウトの認証と、フォームでのログインの認証に関する設定をする
		MvcRequestMatcher h2RequestMatcher = new MvcRequestMatcher(introspector, "/**");
		h2RequestMatcher.setServletPath("/h2-console");

		RequestMatcher publicMatchers = new OrRequestMatcher(
				//変数publicMatchersに認証から除外するURLを定義する
				new AntPathRequestMatcher("/"),
				new AntPathRequestMatcher("/favicon.ico"),
				new AntPathRequestMatcher("/error"),
				new AntPathRequestMatcher("/h2-cosole/**"),
				new AntPathRequestMatcher("/login"),
				new AntPathRequestMatcher("/users/new"),
				new AntPathRequestMatcher("/user"),
				new AntPathRequestMatcher("/css/**"),
				new AntPathRequestMatcher("/images/**"),
				new AntPathRequestMatcher("/scripts/**"),
				new AntPathRequestMatcher("/push7-worker.js"),
				new AntPathRequestMatcher("/manifest.json"));

		//@formatter:off
		http.authorizeHttpRequests(authz -> authz
				.requestMatchers(publicMatchers)
				.permitAll()
				.anyRequest().authenticated()) //antMatchersで指定したパス以外認証する
				.formLogin(login -> login
						.loginProcessingUrl("/login") //ログイン処理をするパス
						.loginPage("/login") //ログインページのリンク先
						.defaultSuccessUrl("/topics") //ログイン成功時の遷移先
						.failureUrl("/login-failure") //ログイン失敗時の遷移先
						.permitAll()) //未ログインでもアクセス可能
				.oauth2Login(oauth2 -> oauth2//gogleアカウントでのログインの場合
						.loginPage("/login")//ログイン画面
						.defaultSuccessUrl("/topics")//ログイン成功時の遷移先
						.failureUrl("/login-failure")//ログイン失敗時の遷移先
						.permitAll()//未ログインでもアクセス可能
						.userInfoEndpoint(userInfoEndpoint -> userInfoEndpoint
								.oidcUserService(this.oidcUserService())
								.userService(this.oauth2UserService())))
				.logout(logout -> logout
						.logoutSuccessUrl("/logout-complete") //ログアウト成功時の遷移先
						.invalidateHttpSession(true)
						.deleteCookies("JSESSIONID")
						.permitAll())
				.csrf(csrf -> csrf
						.ignoringRequestMatchers(h2RequestMatcher))
				.headers(headers -> headers.frameOptions(
						frame -> frame.sameOrigin()))
				.cors(cors -> cors.disable());
		//@formatter:on		
		//↑eclipseのフォーマット機能が発動しないようにしている

		return http.build();
	}

	public FormAuthenticationProvider userDetailsService() {
		return this.authenticationProvider;
	}

	public AuthenticationManager authManager(HttpSecurity http) throws Exception {
		AuthenticationManagerBuilder authenticationManagerBuilder = http
				.getSharedObject(AuthenticationManagerBuilder.class);
		authenticationManagerBuilder.authenticationProvider(authenticationProvider);
		return authenticationManagerBuilder.build();
	}
	
	public OAuth2UserService<OidcUserRequest, OidcUser> oidcUserService(){
		final OidcUserService delegate = new OidcUserService();
		return(userRequest) ->{
			OidcUser oidcUser = delegate.loadUser(userRequest);
			OAuth2AccessToken accessToken = userRequest.getAccessToken();
			
			log.debug("accessToken={}", accessToken);
			
			oidcUser = new DefaultOidcUser(oidcUser.getAuthorities(), oidcUser.getIdToken(),
					oidcUser.getUserInfo());
			String email  = oidcUser.getEmail();
			User user = repository.findByUsername(email);
            if (user == null) {
                user = new User(email, oidcUser.getFullName(), "", Authority.ROLE_USER);
                repository.saveAndFlush(user);
			}
			oidcUser = new SocialUser(oidcUser.getAuthorities(), oidcUser.getIdToken(), oidcUser.getUserInfo(),
                    user.getUserId());
			
			return oidcUser;
		};
		

	}
	
	public OAuth2UserService<OAuth2UserRequest, OAuth2User> oauth2UserService(){
		DefaultOAuth2UserService delegate = new DefaultOAuth2UserService();
		return request -> {
			OAuth2User oauth2User = delegate.loadUser(request);
			
			log.debug(oauth2User.toString());
			
			String name = oauth2User.getAttribute("login");
			User user = repository.findByUsername(name);
			if(user == null) {
				user = new User(name, name, "", Authority.ROLE_USER);
				repository.saveAndFlush(user);
			}
			SocialUser socialUser =new SocialUser(oauth2User.getAuthorities(),
					oauth2User.getAttributes(), "id", user.getUserId());
			
			return socialUser;
		};
	}

	@Bean
	PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}
}

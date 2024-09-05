//ソーシャルログイン用のクラス

package com.example.pictgram.entity;

import java.util.Collection;
import java.util.Map;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.core.oidc.OidcIdToken;
import org.springframework.security.oauth2.core.oidc.OidcUserInfo;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;

public class SocialUser implements OidcUser, OAuth2User, UserInf {
	
	private OAuth2User oauth2User = null;
	
	private static final long serialVersionUID = 1L;
	
	private Long userId = null;
	
	private SocialUser(Collection<? extends GrantedAuthority> authorities, OidcIdToken idToken) {
		this.oauth2User = new DefaultOidcUser(authorities, idToken);
	}
	
	private SocialUser(Collection<? extends GrantedAuthority> authorities, OidcIdToken idToken,
			OidcUserInfo userInfo) {
		this.oauth2User = new DefaultOidcUser(authorities, idToken, userInfo);
	}
	
	public SocialUser(Collection<? extends GrantedAuthority> authorities, OidcIdToken idToken,
			OidcUserInfo userInfo, Long userId) {
		this.oauth2User = new DefaultOidcUser(authorities, idToken, userInfo);
		this.userId = userId;
	}
	
	public SocialUser(Collection<? extends GrantedAuthority> authorities,
			Map<java.lang.String, java.lang.Object> attributes, String nameAttributeKey) {
		this.oauth2User = new DefaultOAuth2User(authorities, attributes, nameAttributeKey);
	}
	
	public SocialUser(Collection<? extends GrantedAuthority> authorities,
			Map<java.lang.String, java.lang.Object> attributes, String nameAttributeKey, Long userId) {
		this.oauth2User = new DefaultOAuth2User(authorities, attributes, nameAttributeKey);
		this.userId = userId;
	}
	
	@Override
	public Long getUserId() {
		return this.userId;
	}
	
	@Override
	public String getUsername() {
		if(this.oauth2User instanceof OidcUser) {
			return ((OidcUser) this.oauth2User).getFullName();
		} else {
			return this.oauth2User.getName();
		}
	}
	
	@Override
	public Map<String, Object> getAttributes(){
		return this.oauth2User.getAttributes();
	}
	
	@Override
	public Collection<? extends GrantedAuthority> getAuthorities(){
		return this.oauth2User.getAuthorities();
	}
	
	@Override
	public String getName() {
		return this.oauth2User.getName();
	}
	
	@Override
	public Map<String, Object> getClaims(){
		if(this.oauth2User instanceof OidcUser) {
			return((OidcUser) this.oauth2User).getClaims();
		} else {
			throw new RuntimeException();
		}
	}
	
	@Override
	public OidcUserInfo getUserInfo() {
		if(this.oauth2User instanceof OidcUser) {
			return((OidcUser) this.oauth2User).getUserInfo();
		} else {
			throw new RuntimeException();
		}
	}

	@Override
	public OidcIdToken getIdToken() {
		if(this.oauth2User instanceof OidcUser) {
			return ((OidcUser) this.oauth2User).getIdToken();
		} else {
			throw new RuntimeException();
		}
	}
}

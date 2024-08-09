/**抽象クラスを継承したEntityクラス
 */
package com.example.pictgram.entity;

import java.util.Collection;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Entity
@Table(name = "users")
@Data
@EqualsAndHashCode(callSuper=false)
public class User extends AbstractEntity implements UserDetails, UserInf {
	private static final long serialVersionUID = 1L;

	public enum Authority {
		//普通のユーザーと管理者権限
		ROLE_USER, ROLE_ADMIN
	};

	public User() {
		super();
	}

	public User(String email, String name, String password, Authority authority) {
		this.username = email;
		this.name = name;
		this.password = password;
		this.authority = authority;
	}

	@Id
	@SequenceGenerator(name = "users_id_seq")
	@GeneratedValue(strategy = GenerationType.IDENTITY)//主キーを生成
	private Long userId;

	@Column(nullable = false, unique = true)
	private String username;

	@Column(nullable = false)
	private String name;

	@Column(nullable = false)
	private String password;

	@Column(nullable = false)
	@Enumerated(EnumType.STRING)
	//↑アノテーションの意味は？
	private Authority authority;

	@Override
	public boolean isAccountNonExpired() {
		return true;
	}

	@Override
	public boolean isAccountNonLocked() {
		return true;
	}

	@Override
	public boolean isCredentialsNonExpired() {
		return true;
	}

	@Override
	public boolean isEnabled() {
		return true;
	}

	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		// TODO 自動生成されたメソッド・スタブ
		return null;
	}
}

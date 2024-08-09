/**Spring Security ではユーザー情報を取得するインターフェースとして UserDetailsService が定義されている。
 * UserDetailsService の処理を、このクラスで実装。
 */
package com.example.pictgram.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.example.pictgram.entity.User;
import com.example.pictgram.repository.UserRepository;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {
	
	@Autowired
	private UserRepository repository;
	protected static Logger log = LoggerFactory.getLogger(UserDetailsServiceImpl.class);
	
	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException{
		log.debug("username={}", username);
		
		//名前なしor空白の場合の処理
		if(username == null || "".equals(username)) {
			throw new UsernameNotFoundException("Username is empty");
		}
		User entity = repository.findByUsername(username);
		
		return entity;
	}

}

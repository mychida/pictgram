package com.example.pictgram.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.pictgram.entity.User;
import com.example.pictgram.entity.User.Authority;
import com.example.pictgram.form.UserForm;
import com.example.pictgram.repository.UserRepository;

@Controller
public class UsersController {
	
	@Autowired
	private PasswordEncoder passwordEncoder;
	@Autowired
	private UserRepository repository;
	
	@GetMapping("/users/new")
	//ユーザー登録画面を表示
	public String newUser(Model model) {
		model.addAttribute("form", new UserForm());
		return "users/new";
	}
	
	@PostMapping("/user")
	//usersテーブルにユーザー情報を保存
	public String create(@Validated @ModelAttribute("form") UserForm form, BindingResult result,
			Model model, RedirectAttributes redirAttrs) {
		String name = form.getName();
		String email = form.getEmail();
		String password = form.getPassword();
		
		if(repository.findByUsername(email) != null) {
			FieldError fieldError = new FieldError(result.getObjectName(),
				"email", "そのEメールはすでに使用されています。");
				result.addError(fieldError);
		}
		if (result.hasErrors()) {
			//バリデーションでのユーザー登録失敗時
			model.addAttribute("hasMessage", true);
			model.addAttribute("class", "alert-danger");
			model.addAttribute("message", "ユーザー登録に失敗しました");
			return "users/new";
		}
		
		User entity = new User(email, name, passwordEncoder.encode(password), Authority.ROLE_USER);
		repository.saveAndFlush(entity);
		
		//ユーザー登録完了メッセージを表示
		model.addAttribute("hasMessage", true);
		model.addAttribute("class", "alert-info");
		model.addAttribute("message", "ユーザー登録が完了しました");
		
		return "layouts/complete";
		
	}

}

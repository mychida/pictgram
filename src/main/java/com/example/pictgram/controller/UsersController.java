package com.example.pictgram.controller;

import java.util.Locale;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
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
	@Autowired
	private MessageSource messageSource;
	
	@GetMapping("/users/new")
	//ユーザー登録画面を表示
	public String newUser(Model model) {
		model.addAttribute("form", new UserForm());
		return "users/new";
	}
	
	@PostMapping("/user") //→new.htmlの中のth:action="@{/user}と対応
	//usersテーブルにユーザー情報を保存
	public String create(@Validated @ModelAttribute("form") UserForm form, BindingResult result,
			Model model, RedirectAttributes redirAttrs, Locale locale) {
		String name = form.getName();
		String email = form.getEmail();
		String password = form.getPassword();
		
		if(repository.findByUsername(email) != null) {
			//同じメールアドレス＝ユーザー名がないか探す
			FieldError fieldError = new FieldError(result.getObjectName(),"email",
					messageSource.getMessage("users.create.error.1", new String [] {}, locale));
				result.addError(fieldError);
		}
		if (result.hasErrors()) {
			//バリデーションでひっかかり、ユーザー登録失敗時。
			model.addAttribute("hasMessage", true);
			model.addAttribute("class", "alert-danger");
			model.addAttribute("message", messageSource.getMessage
					("users.create.flash.1", new String[] {}, locale));
			return "users/new";
			
			//ユーザー登録に失敗した際は、以下のDBへ保存される処理は行われない？
		}
		
		//入力された情報をDBへ保存
		User entity = new User(email, name, passwordEncoder.encode(password), Authority.ROLE_USER);
		repository.saveAndFlush(entity);
		
		//ユーザー登録完了メッセージを表示。
		model.addAttribute("hasMessage", true);
		model.addAttribute("class", "alert-info");
		model.addAttribute("message", "ユーザー登録が完了しました");
		
		return "layouts/complete";
		
	}

}

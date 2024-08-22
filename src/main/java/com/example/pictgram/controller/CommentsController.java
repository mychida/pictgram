package com.example.pictgram.controller;

import java.util.Locale;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.pictgram.entity.Comment;
import com.example.pictgram.form.CommentForm;
import com.example.pictgram.repository.CommentRepository;

@Controller
public class CommentsController {

	@Autowired
	private MessageSource messageSource;

	@Autowired
	private ModelMapper modelMapper;

	@Autowired
	private CommentRepository repository;

	/**topics/{topicId}/comments/new の呼び出しで newComment メソッドはコメント登録画面を表示する
	 * {topicId} はコメント登録対象の話題の持つ ID
	 * 
	 * @param topicId
	 * @param model
	 * @return
	 */
	@GetMapping("/topics/{topicId}/comments/new")
	public String newComment(@PathVariable("topicId") long topicId, Model model) {
		CommentForm form = new CommentForm();
		form.setTopicId(topicId);
		model.addAttribute("form", form);

		return "comments/new";
	}

	/**topics/{topicId}/comment の呼び出しで create メソッドはコメントを登録する
	 * 
	 * @param topicId
	 * @param form
	 * @param result
	 * @param model
	 * @param redirAttrs
	 * @param locale
	 * @return
	 */
	@PostMapping("/topics/{topicId}/comment")
	public String create(@PathVariable("topicId") long topicId,
			@Validated @ModelAttribute("form") CommentForm form,
			BindingResult result, Model model, RedirectAttributes redirAttrs, Locale locale) {
		if(result.hasErrors()) {
			model.addAttribute("hasMessage", true);
			model.addAttribute("class", "alert-danger");
			model.addAttribute("message", messageSource.getMessage("comments.create.flash.1",
					new String[] {}, locale));
			
			return "comments/new";
		}
		
		Comment entity = modelMapper.map(form, Comment.class);
		entity.setTopicId(topicId);
		repository.saveAndFlush(entity);
		
		redirAttrs.addFlashAttribute("hasMessage", true);
		redirAttrs.addFlashAttribute("class", "alert-info");
		redirAttrs.addFlashAttribute("message", messageSource.getMessage
				("comments.create.flash.2", new String[] {}, locale));
		
		return "redirect:/topics";
	}

}

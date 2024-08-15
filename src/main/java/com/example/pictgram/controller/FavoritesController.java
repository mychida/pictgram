package com.example.pictgram.controller;

import java.io.IOException;
import java.security.Principal;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.pictgram.entity.Favorite;
import com.example.pictgram.entity.Topic;
import com.example.pictgram.entity.UserInf;
import com.example.pictgram.form.TopicForm;
import com.example.pictgram.repository.FavoriteRepository;

import jakarta.transaction.Transactional;

@Controller
public class FavoritesController {

	@Autowired
	private MessageSource messageSource;
	@Autowired
	private FavoriteRepository repository;
	@Autowired
	private TopicsController topicsController;

	/**お気に入り一覧を表示
	 * 
	 * @param principal
	 * @param model
	 * @return
	 * @throws IOException
	 */
	@GetMapping("/favorites")
	public String index(Principal principal, Model model) throws IOException {
		//princial→ログインに必要なユーザーの情報
		Authentication authentication = (Authentication) principal;
		UserInf user = (UserInf) authentication.getPrincipal();
		List<Favorite> topics = repository.findByUserIdOrderByUpdatedAtDesc(user.getUserId());
		List<TopicForm> list = new ArrayList<>();
		for (Favorite entity : topics) {
			Topic topicEntity = entity.getTopic();
			TopicForm form = topicsController.getTopic(user, topicEntity);
			list.add(form);
		}
		model.addAttribute("list", list);

		/**テンプレートは、topicsと共用のためfavorite専用のhtmlはない */
		return "topics/index";
	}

	/**お気に入りを登録
	 * 
	 * @param principal
	 * @param topicId
	 * @param redirAttrs
	 * @param locale
	 * @return
	 */
	//favorite.htmlは存在せず、topics/indesと共用でその中にth:href="@{'/favorite'}"がある。
	@PostMapping("/favorite")
	public String create(Principal principal, @RequestParam("topic_id") long topicId,
			RedirectAttributes redirAttrs, Locale locale) {
		Authentication authentication = (Authentication) principal;
		UserInf user = (UserInf) authentication.getPrincipal();
		Long userId = user.getUserId();
		/**お気に入りは、user_idとtopic_idの組み合わせで登録される */
		List<Favorite> results = repository.findByUserIdAndTopicId(userId, topicId);
		if (results.size() == 0) {
			Favorite entity = new Favorite();
			entity.setUserId(userId);
			entity.setTopicId(topicId);
			repository.saveAndFlush(entity);

			redirAttrs.addFlashAttribute("hasMessage", true);
			redirAttrs.addFlashAttribute("class", "alert-info");
			redirAttrs.addFlashAttribute("message",
					messageSource.getMessage("favorites.create.flash", new String[] {}, locale));
		}

		//どこへリダイレクト？
		return "redirect:/topics";
	}

	/**@Transactional がないとrepository.deleteByUserIdAndTopicIdがエラーになる*/
	/**お気に入りを削除（Deleteメソッドで呼び出された場合）
	 * 
	 * @param principal
	 * @param topicId
	 * @param redirAttrs
	 * @param locale
	 * @return
	 */
	@DeleteMapping("favorite")
	@Transactional
	public String destroy(Principal principal, @RequestParam("topic_id") long topicId,
			RedirectAttributes redirAttrs, Locale locale) {
		Authentication authentication = (Authentication) principal;
		UserInf user = (UserInf) authentication.getPrincipal();
		Long userId = user.getUserId();
		List<Favorite> results = repository.findByUserIdAndTopicId(userId, topicId);
		if (results.size() == 1) {
			/**お気に入りの登録を削除する */
			repository.deleteByUserIdAndTopicId(user.getUserId(), topicId);

			redirAttrs.addFlashAttribute("hasMessage", true);
			redirAttrs.addFlashAttribute("class", "alert-info");
			redirAttrs.addFlashAttribute("message",
					messageSource.getMessage("favorites.destroy.flash", new String[] {}, locale));
		}
		
		//どこへリダイレクト？
		return "redirect:/topics";
	}

}

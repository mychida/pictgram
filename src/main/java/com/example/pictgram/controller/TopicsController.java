package com.example.pictgram.controller;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.security.Principal;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.apache.commons.io.FilenameUtils;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.Base64Utils;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.pictgram.entity.Comment;
import com.example.pictgram.entity.Favorite;
import com.example.pictgram.entity.Topic;
import com.example.pictgram.entity.UserInf;
import com.example.pictgram.form.CommentForm;
import com.example.pictgram.form.FavoriteForm;
import com.example.pictgram.form.TopicForm;
import com.example.pictgram.form.UserForm;
import com.example.pictgram.repository.TopicRepository;

import jakarta.servlet.http.HttpServletRequest;

@Controller
public class TopicsController {

	@Autowired
	private ModelMapper modelMapper;
	@Autowired
	private TopicRepository repository;
	@Autowired
	private HttpServletRequest request;
	@Autowired
	private MessageSource messageSource;

	/**画像保存先のデフォルト値を設定
	 * image.local(=プロパティー)が指定されない場合は、ローカルに保存しないように false (=値)を指定している
	 */
	@Value("${image.local:false}")
	private String imageLocal;

	@GetMapping("/topics")
	/**
	 * 話題一覧画面を表示
	 * @param principal
	 * @param model
	 * @return
	 * @throws IOException
	 */
	public String index(Principal principal, Model model) throws IOException {
		Authentication authentication = (Authentication) principal;
		UserInf user = (UserInf) authentication.getPrincipal();
		
		//投稿順に取り出してmodel("list")に保存する
		List<Topic> topics = repository.findAllByOrderByUpdatedAtDesc();
		List<TopicForm> list = new ArrayList<>();
		for (Topic entity : topics) {
			TopicForm form = getTopic(user, entity);
			list.add(form);
		}
		model.addAttribute("list", list);

		return "topics/index";
	}

	//バリデーション？どのタイミングでこのメソッドが呼ばれる？
	public TopicForm getTopic(UserInf user, Topic entity) throws FileNotFoundException, IOException {
		modelMapper.getConfiguration().setAmbiguityIgnored(true);
		modelMapper.typeMap(Topic.class, TopicForm.class).addMappings(mapper -> mapper.skip(TopicForm::setUser));
		modelMapper.typeMap(Topic.class, TopicForm.class).addMappings
			(mapper -> mapper.skip(TopicForm::setFavorites));
		modelMapper.typeMap(Topic.class, TopicForm.class).addMappings
			(mapper -> mapper.skip(TopicForm::setComments));
		modelMapper.typeMap(Favorite.class, FavoriteForm.class).addMappings
			(mapper -> mapper.skip(FavoriteForm::setTopic));

		boolean isImageLocal = false;
		if (imageLocal != null) {
			isImageLocal = new Boolean(imageLocal);
		}
		
		//modelMapperを利用してエンティティーからフォームへの移し替えをする
		TopicForm form = modelMapper.map(entity, TopicForm.class);

		if (isImageLocal) {
			try (InputStream is = new FileInputStream(new File(entity.getPath()));
					ByteArrayOutputStream os = new ByteArrayOutputStream()) {
				byte[] indata = new byte[10240 * 16];
				int size;
				while ((size = is.read(indata, 0, indata.length)) > 0) {
					os.write(indata, 0, size);
				}
				StringBuilder data = new StringBuilder();
				data.append("data:");
				data.append(getMimeType(entity.getPath()));
				/**画像は表示するのにBase64形式の文字列としてHTMLソースに埋め込みをする*/
				data.append(";base64,");

				data.append(new String(Base64Utils.encode(os.toByteArray()), "ASCII"));
				form.setImageData(data.toString());
			}
		}

		UserForm userForm = modelMapper.map(entity.getUser(), UserForm.class);
		form.setUser(userForm);
		
		List<FavoriteForm> favorites = new ArrayList<>();
		for(Favorite favoriteEntity : entity.getFavorites()) {
			FavoriteForm favorite = modelMapper.map(favoriteEntity,  FavoriteForm.class);
			favorites.add(favorite);
			if(user.getUserId().equals(favoriteEntity.getUserId())) {
				form.setFavorite(favorite);
			}
		}

		form.setFavorites(favorites);
		
		List<CommentForm> comments = new ArrayList<CommentForm>();
		
		for(Comment commentEntity : entity.getComments()) {
			CommentForm comment = modelMapper.map(commentEntity,  CommentForm.class);
			comments.add(comment);
		}
		form.setComments(comments);
		
		return form;
	}

	//画像のバリデーション？どのタイミングでこのメソッドが呼ばれる？
	private String getMimeType(String path) {
		String extension = FilenameUtils.getExtension(path);
		String mimeType = "image/";
		switch (extension) {
		case "jpg":
		case "jpeg":
			mimeType += "jpeg";
			break;
		case "png":
			mimeType += "png";
			break;
		case "gif":
			mimeType += "gif";
			break;
		}
		return mimeType;
	}

	@GetMapping("/topics/new")
	/**
	 * 話題投稿画面の表示
	 * @param model
	 * @return
	 */
	public String newTopic(Model model) {
		model.addAttribute("form", new TopicForm());
		return "topics/new";
	}

	
	/**
	 * 作成した話題を登録
	 * @param principal
	 * @param form
	 * @param result
	 * @param model
	 * @param image
	 * @param redirAttrs
	 * @return
	 * @throws IOException
	 * 
	 * @ModelAttributeアノテーションを付けると、自動でModelにインスタンスが登録される
	 * 
	 */
	@PostMapping("/topic")
	public String create(Principal principal, @Validated @ModelAttribute("form") TopicForm form,
			/**アップロードする画像は、MultipartFileで受け取る*/
			//Bindingresult→バリデーションエラーが発生しているかどうかか確認できる。
			//hasErrors() メソッドがtrueの場合、エラーが発生している。
			BindingResult result, Model model, @RequestParam MultipartFile image,//BindingResult resultユーザーにメッセージ表示する時に使う
			RedirectAttributes redirAttrs, Locale locale) throws IOException {
		
		if (result.hasErrors()) {
			model.addAttribute("hasMessage", true);
			model.addAttribute("class", "alert-danger");
			model.addAttribute("message", messageSource.getMessage("topics.create.flash.1",
					new String[] {}, locale));
			return "topics/new";
		}

		
		//画像保存先の設定？
		boolean isImageLocal = false;
		if (imageLocal != null) {
			isImageLocal = new Boolean(imageLocal);
		}

		//新しいトピックオブジェクトを作成して、現在のユーザー情報を設定
		Topic entity = new Topic();
		Authentication authentication = (Authentication) principal;
		UserInf user = (UserInf) authentication.getPrincipal();
		entity.setUserId(user.getUserId());
		
		//画像保存の処理
		File destFile = null;
		if (isImageLocal) {
			destFile = saveImageLocal(image, entity);
			entity.setPath(destFile.getAbsolutePath());
		} else {
			entity.setPath("");
		}
		
		//投稿コメントの保存
		entity.setDescription(form.getDescription());
		repository.saveAndFlush(entity);

		redirAttrs.addFlashAttribute("hasMessage", true);
		redirAttrs.addFlashAttribute("class", "alert-info");
		redirAttrs.addFlashAttribute("message", messageSource.getMessage("topics.create.flash.2",new String[] {}, locale));

		return "redirect:/topics";
	}

	private File saveImageLocal(MultipartFile image, Topic entity) throws IOException {
		/**アップロードした画像は、uploadsディレクトリに保存する*/
		File uploadDir = new File("/uploads");
		uploadDir.mkdir();

		String uploadsDir = "/uploads/";
		String realPathToUploads = request.getServletContext().getRealPath(uploadsDir);
		if (!new File(realPathToUploads).exists()) {
			new File(realPathToUploads).mkdir();
		}
		String fileName = image.getOriginalFilename();
		File destFile = new File(realPathToUploads, fileName);
		image.transferTo(destFile);

		return destFile;
	}

}

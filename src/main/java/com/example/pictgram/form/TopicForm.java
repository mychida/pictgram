//データ受け渡しとバリデーションのクラス

package com.example.pictgram.form;

import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import com.example.pictgram.validation.constraints.ImageByte;
import com.example.pictgram.validation.constraints.ImageNotEmpty;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class TopicForm {

	private Long id;//編集用のID

	private Long userId; //投稿した人のID

	@ImageNotEmpty
	@ImageByte(max = 2000000)//2MB
	private MultipartFile image; //投稿する画像データ

	private String imageData; //画像データを文字列で保存するとき利用

	private String path; //画像保存パス

	@NotEmpty
	@Size(max = 1000)
	private String description; //投稿コメント
	
	private Double latitude;//経度
	
	private Double longitude;//緯度

	private UserForm user; //投稿ユーザーのデータ
	
	private List<FavoriteForm> favorites;
	
	private FavoriteForm favorite;
	
	private List<CommentForm> comments;

}

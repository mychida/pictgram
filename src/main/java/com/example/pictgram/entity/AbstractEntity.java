package com.example.pictgram.entity;

import java.util.Date;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import lombok.Data;

@MappedSuperclass //このクラスを継承したクラスでtableの記載があるからこのクラスではその記載はしませんの印
@Data
public class AbstractEntity {
	@Column(name = "created_at")
	private Date createdAt;

	@Column(name = "updated_at")
	private Date updatedAt;

	@PrePersist //データベースを保存する前に必ず実行するもの
	public void onPrePersist() {
		Date date = new Date();
		setCreatedAt(date);
		setUpdatedAt(date);
	}

	@PreUpdate //データベースを更新する前に必ず実行するもの
	public void onPreUpdate() {
		setUpdatedAt(new Date());
	}

}

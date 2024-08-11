/*「Entity」とは、データベースのテーブル構造を表したオブジェクト。
 * データベースの1行を1インスタンスと対応させるためのクラス。
 */
package com.example.pictgram.entity;

import java.io.Serializable;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Entity
@Table(name = "topics")
@Data
@EqualsAndHashCode(callSuper = false)
public class Topic extends AbstractEntity implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@SequenceGenerator(name = "topic_id_seq")
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private long id;

	@Column(nullable = false)
	private Long userId;

	@Column(nullable = false)
	private String path;

	@Column(nullable = false, length = 1000)
	private String description;

	@Column
	private Double latitude;

	@Column
	private Double longitude;

	@ManyToOne
	@JoinColumn(name = "userId", insertable = false, updatable = false)
	private User user;
}

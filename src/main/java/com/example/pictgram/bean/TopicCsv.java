package com.example.pictgram.bean;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import lombok.Data;

@JsonPropertyOrder({"ID", "ユーザーID", "パス", "説明", "緯度", "経度"})
@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class TopicCsv {
	
	@JsonProperty("ID")
	private Long id;
	
	@JsonProperty("ユーザーID")
	private Long userId;
	
	@JsonProperty("パス")
	private String path;
	
	@JsonProperty("説明")
	private String description;
	
	@JsonProperty("緯度")
	private Double latitude;
	
	@JsonProperty("経度")
	private Double longitude;

}

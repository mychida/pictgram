package com.example.pictgram.controller;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.pictgram.entity.Topic;
import com.example.pictgram.form.EventForm;
import com.example.pictgram.repository.TopicRepository;
import com.fasterxml.jackson.databind.ObjectMapper;

@RestController
@RequestMapping("/events")
public class EventsController {

	@Autowired
	private TopicRepository repository;

	/**
	 * カレンダーに表示するEvent情報を取得する
	 * @return Event情報をjsonエンコードした文字列
	 */
	@GetMapping("/all")
	public String getEvents() {
		String jsonMsg = null;
		try {
			Iterable<Topic> topics = repository.findAll();
			List<EventForm> form = new ArrayList<EventForm>();
			for (Topic entity : topics) {
				EventForm event = new EventForm();
				event.setTitle(entity.getDescription());
				String createdAt = new SimpleDateFormat("yyy-MM-dd").format(entity.getCreatedAt());
				event.setStart(createdAt);
				event.setUrl("/topics#" + entity.getId());
				form.add(event);
			}
			ObjectMapper mapper = new ObjectMapper();
			jsonMsg = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(form);
		} catch (IOException ioex) {
			System.out.println(ioex.getMessage());
		}
		return jsonMsg;
	}

}

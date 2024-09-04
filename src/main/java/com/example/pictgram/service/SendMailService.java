package com.example.pictgram.service;

import java.nio.charset.StandardCharsets;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.mail.javamail.MimeMessagePreparator;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;

import jakarta.mail.internet.MimeMessage;

@Service
public class SendMailService {
	@Autowired
	private JavaMailSender javaMailSender;
	
	@Value("${SPRING_MAIL_USERNAME}")
	private String springMailUsername;
	
	public void sendMail(Context context) {
		
		javaMailSender.send(new MimeMessagePreparator() {
			
			@Override
			public void prepare(MimeMessage mimeMessage) throws Exception {
				MimeMessageHelper helper = new MimeMessageHelper
						(mimeMessage, StandardCharsets.UTF_8.name());
				helper.setFrom(springMailUsername);
				helper.setTo(springMailUsername);
				helper.setSubject((String) context.getVariable("title"));
				helper.setText(getMailBody("email", context), true);
			}
		});
	}
	
	private String getMailBody(String templateName, Context context) {
		SpringTemplateEngine templateEngine = new SpringTemplateEngine();
		templateEngine.setTemplateResolver(mailTemplateResolver());
		return templateEngine.process(templateName, context);
	}
	
	private ClassLoaderTemplateResolver mailTemplateResolver() {
		ClassLoaderTemplateResolver templateResolver = new ClassLoaderTemplateResolver();
		templateResolver.setTemplateMode(TemplateMode.HTML);
		templateResolver.setPrefix("mailtemplates/");
		templateResolver.setSuffix(".html");
		templateResolver.setCharacterEncoding("UTF-8");
		templateResolver.setCacheable(true);
		return templateResolver;
	}
}

package com.project.chatbot;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.metrics.buffering.BufferingApplicationStartup;

@SpringBootApplication
public class ChatbotApplication implements CommandLineRunner {

	private static final Logger logger = LoggerFactory.getLogger(ChatbotApplication.class);

	@Value("${openai.api.key}")
	private String openaiApiKey;

	@Value("${spring.profiles.active:}")
	private String activeProfile;

	public static void main(String[] args) {
		SpringApplication app = new SpringApplication(ChatbotApplication.class);
		app.setApplicationStartup(new BufferingApplicationStartup(10000));
		app.run(args);
	}

	@Override
	public void run(String... args) throws Exception {
		if ("dev".equals(activeProfile)) {
			logger.info("OpenAI API Key: {}", openaiApiKey);
		} else {
			logger.info("OpenAI API Key is set");
		}
	}
}




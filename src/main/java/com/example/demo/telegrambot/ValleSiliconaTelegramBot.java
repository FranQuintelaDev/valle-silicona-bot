package com.example.demo.telegrambot;

import org.springframework.stereotype.Service;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

@Service
public class ValleSiliconaTelegramBot extends TelegramLongPollingBot {

	private final String BOT_TOKEN = "5740527557:AAFe5wPRGOYPwmrkhOPj0gFUtYBMLpERGk4";
	private final String BOT_NAME = "ValleSiliconaBot";
	private final String groupId = "-818085683";

	@Override
	public void onUpdateReceived(Update update) {

		try {
			execute(new SendMessage("-818085683",
					"Este es un bot automatico, no hay métodos para interactuar. Disculpa las molestias"));
		} catch (TelegramApiException e) {
			e.printStackTrace();
		}

	}

	public void sendMessage(String message) throws TelegramApiException {
		SendMessage sendMessage = new SendMessage(groupId, message);
		execute(sendMessage);
	}

	@Override
	public String getBotToken() {
		return BOT_TOKEN;
	}

	@Override
	public String getBotUsername() {
		return BOT_NAME;
	}

}

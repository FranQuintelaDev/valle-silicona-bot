package com.example.demo.controller;

import java.io.IOException;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import com.example.demo.service.ValleSiliconaBotService;

@RestController
@RequestMapping("/valleSiliconaBot")
public class ValleSiliconaBotController {

    @Autowired
    ValleSiliconaBotService valleSiliconaBotService;

    @PostMapping()
    @ResponseStatus(HttpStatus.OK)
    public void getPayload(@RequestBody String postInformation){
			
    	try {
			valleSiliconaBotService.processPostInformation(postInformation);
		} catch (IOException | GitAPIException | TelegramApiException e) {
			
			e.printStackTrace();
		}

    }

    
}

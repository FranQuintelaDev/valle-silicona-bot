package com.example.demo.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.service.ValleSiliconaBotService;

@RestController
@RequestMapping("/valleSiliconaBot")
public class ValleSiliconaBotController {

    @Autowired
    ValleSiliconaBotService service;

    @PostMapping()
    @ResponseStatus(HttpStatus.OK)
    public void getPayload(@RequestBody String payload){
        service.pullTeamData(payload);
    }

    
}

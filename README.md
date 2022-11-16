
## Guia


- Descargar e instalar Spring 

- New -> SpringStarterProject (Type: Maven Project)

- Añadir dependencias en pom.xml

```xml
<dependency>
			<groupId>org.eclipse.jgit</groupId>
			<artifactId>org.eclipse.jgit</artifactId>
			<version>6.3.0.202209071007-r</version>
</dependency>
<dependency>
			<groupId>com.google.code.gson</groupId>
			<artifactId>gson</artifactId>
			<version>2.8.5</version>
</dependency>
<dependency>
			<groupId>org.telegram</groupId>
			<artifactId>telegrambots-spring-boot-starter</artifactId>
			<version>6.1.0</version>
</dependency>
<dependency>
			<groupId>org.springframework.boot</groupId>
			<artifactId>spring-boot-starter-web</artifactId>
</dependency>
```

- Create a new Java package. Ex: com.vallesilicona.bot.telegrambot

- Create a new Java class. Ex: ValleSiliconaTelegramBot

Para configurar el Bot se deben cambiar los valores de BOT_TOKEN, BOT_NAME y groupId.

```java
package com.vallesilicona.bot.telegrambot;

import org.springframework.stereotype.Service;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

@Service
public class ValleSiliconaTelegramBot extends TelegramLongPollingBot {

    private final String BOT_TOKEN = "5740527557:AAFe5wPRGOYPwmrkhOPj0gFUtYBMLpERGk4";
    private final String groupId = "-818085683";
    private final String soleraGroupId = "-1001561970415";
    private final String BOT_NAME = "ValleSiliconaBot";
    @Override
    public void onUpdateReceived(Update update) {

        Message receivedMessage = update.getMessage();
        String chatId = receivedMessage.getChatId().toString();

        try {
            SendMessage message = new SendMessage();
            message.setChatId(chatId);
            if (chatId.equals(groupId)) {
                if (receivedMessage.getText().equals("/scoreboard")
                        || receivedMessage.getText().equals("/scoreboard@" + getBotUsername())) {
                    // Generate scoreboard
                    message.setText("Esta funcionalidad no está disponible.");
                    execute(message);

                }
            }
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }

    }

    public void sendWinnerMessage(String message) {
        SendMessage sendMessage = new SendMessage(groupId, message);
        try {
            execute(sendMessage);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
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

```

- Create a new Java package. Ex: com.vallesilicona.bot.service

- Create a new Java class. Ex: ValleSiliconaBotService

Para configurar el servicio a nuestro repositorio habría que cambiar GIT_REMOTE.

```java
package com.vallesilicona.bot.service;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.PullCommand;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.InvalidRemoteException;
import org.eclipse.jgit.errors.RepositoryNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.google.gson.stream.JsonReader;
import com.vallesilicona.bot.telegrambot.ValleSiliconaTelegramBot;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonIOException;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;

@Service
public class ValleSiliconaBotService {

    private final String LOCAL_REPO_PATH = "res/localRepo";
    private final String TEAM_DATA_LOCATION = "src/data/teamdata.json";
    private final String MAIN_REF = "refs/heads/main";
    private final String GIT_REMOTE = "https://github.com/FranQuintelaDev/bootcampsolera-vallesilicona.git";
    private final String SOLERA_GIT_REMOTE = "https://github.com/danibanez/bootcampsolera";

    @Autowired
    ValleSiliconaTelegramBot bot;

    Gson gson = new Gson();
    private JsonArray teams;

    public void pullTeamData(String payload) {
        if (isTeamDataModified(payload)) {
            try {
                Git repo = getRepo(LOCAL_REPO_PATH);
                PullCommand pull = repo.pull();
                pull.call();
                readJson(LOCAL_REPO_PATH + "/" + TEAM_DATA_LOCATION);
            } catch (InvalidRemoteException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (GitAPIException e) {
                e.printStackTrace();
            } catch (URISyntaxException e) {
                e.printStackTrace();
            }
        }
    }

    private String readJson(String path) {

        /*
         * 1.- Leer el json con JsonElement jsonElement = new JsonParser().parse(path);
         */
        JsonElement jsonElement;
        try {
            jsonElement = gson.fromJson(new JsonReader(Files.newBufferedReader(Paths.get(path))),
                    JsonElement.class);
            JsonObject jsonObject = jsonElement.getAsJsonObject();
            teams = jsonObject.getAsJsonArray("teamdata");
            getCurrentWinner(teams);
        } catch (JsonIOException e) {
            e.printStackTrace();
        } catch (JsonSyntaxException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return "";

    }

    private boolean isTeamDataModified(String payload) {
        JsonObject json = gson.fromJson(payload,
                JsonObject.class);
        String ref = json.get("ref").getAsString();
        if(ref.equals(MAIN_REF)){
            JsonObject headCommit = json.getAsJsonObject("head_commit");
        // If the value is an Array[], it is parsed as ArrayList
        JsonArray modifiedFiles = headCommit.getAsJsonArray("modified");
        for (JsonElement fileName : modifiedFiles) {
            if (fileName.getAsString().equals(TEAM_DATA_LOCATION)) {
                return true;
            }
        }
        }
        return false;
    }

    private Git getRepo(String LOCAL_REPO_PATH)
            throws IOException, GitAPIException, URISyntaxException {
        Git repo;
        // We try to get our local repository. If it doesn't exist yet, we create it.
        try {
            repo = Git.open(Paths.get(LOCAL_REPO_PATH).toFile());
        } catch (RepositoryNotFoundException ex) {
            repo = Git.cloneRepository().setURI(GIT_REMOTE).setDirectory(Paths.get(LOCAL_REPO_PATH).toFile()).call();
        }
        return repo;
    }

    public void getCurrentWinner(JsonArray teams) {
        ArrayList<String> winningTeams = new ArrayList<>();
        int maxPoints = 0;
        for (JsonElement team : teams) {
            JsonArray activities = team.getAsJsonObject().getAsJsonArray("actividades");
            int teamPoints = 0;
            for (JsonElement activity : activities) {
                teamPoints += activity.getAsJsonObject().get("puntos").getAsInt();
            }
            if (teamPoints > maxPoints) {
                maxPoints = teamPoints;
                winningTeams.clear();
                winningTeams.add(team.getAsJsonObject().get("name").getAsString());
            } else if (teamPoints == maxPoints) {
                winningTeams.add(team.getAsJsonObject().get("name").getAsString());
            }
        }

        String message = "Se ha modificado la clasificación.\n\n";

        if (winningTeams.size() > 1) {
            message += "¡Hay empate a " + maxPoints + " puntos entre los equipos ";
            for (int i = 0; i < winningTeams.size(); i++) {
                if (i == winningTeams.size() - 1) {
                    message += "y \"" + winningTeams.get(i) + "\"!";
                } else if (i == winningTeams.size() - 2) {
                    message += "\"" + winningTeams.get(i) + "\" ";
                } else {
                    message += "\"" + winningTeams.get(i) + "\", ";
                }
            }
        } else {
            message += "Va ganando el equipo \"" + winningTeams.get(0) + "\" con " + maxPoints + " puntos.";
        }

        bot.sendWinnerMessage(message);
    }

}


```



- Create a new Java package. Ex: com.vallesilicona.bot.controller

- Create a new Java class. Ex: ValleSiliconaBotController

```java
package com.vallesilicona.bot.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.vallesilicona.bot.service.ValleSiliconaBotService;

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

```

Con esto estaría configurado el endpoint asociado al webhook (controller + service).


Para asociar este endpoint al webhook debemos desplegarlo o en su defecto exponer el puerto
usando 'ngrok' como se explica en  - [exposing-localhost-to-the-internet](https://docs.github.com/en/developers/webhooks-and-events/webhooks/creating-webhooks#exposing-localhost-to-the-internet)
 
Por último quedaría crear un webhook en el repositorio GitHub y asociarlo a esta url expuesta.

package com.example.demo.service;

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
import com.example.demo.telegrambot.ValleSiliconaTelegramBot;
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
    private final String GIT_REMOTE = "https://github.com/FranQuintelaDev/bootcampsolera-vallesilicona";
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

package com.example.demo.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.InvalidRemoteException;
import org.eclipse.jgit.api.errors.TransportException;
import org.eclipse.jgit.errors.RepositoryNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

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

	@Autowired
	ValleSiliconaTelegramBot valleSiliconaTelegramBot;

	public void processPostInformation(String postInformation)
			throws InvalidRemoteException, TransportException, IOException, GitAPIException, TelegramApiException {
		if (!gitFileHasChanged(postInformation, "src/data/teamdata.json"))
			return;

		getAndUpdateGitRepository("res/localRepo", "https://github.com/FranQuintelaDev/bootcampsolera-vallesilicona");

		JsonObject jsonObject = getJsonFileFromPath("res/localRepo" + "/" + "src/data/teamdata.json");

		Map<String, Integer> winningTeams = getWinnersFromTeamDataJson(jsonObject);

		sendWinningTeamsMessage(winningTeams);

	}

	private JsonObject getJsonFileFromPath(String path) throws JsonIOException, JsonSyntaxException, IOException {
		Gson gson = new Gson();
		JsonElement jsonElement = gson.fromJson(new JsonReader(Files.newBufferedReader(Paths.get(path))),
				JsonElement.class);
		JsonObject jsonObject = jsonElement.getAsJsonObject();
		return jsonObject;
	}

	private boolean gitFileHasChanged(String payload, String fileLocation) {
		Gson gson = new Gson();

		JsonObject json = gson.fromJson(payload, JsonObject.class);
		String ref = json.get("ref").getAsString();
		if (ref.equals("refs/heads/main")) {
			JsonObject headCommit = json.getAsJsonObject("head_commit");
			JsonArray modifiedFiles = headCommit.getAsJsonArray("modified");
			for (JsonElement fileName : modifiedFiles) {
				if (fileName.getAsString().equals(fileLocation)) {
					return true;
				}
			}
		}
		return false;
	}

	private Git getAndUpdateGitRepository(String localPath, String remotePath)
			throws IOException, InvalidRemoteException, TransportException, GitAPIException {
		Git gitRepository;
		try {
			gitRepository = Git.open(Paths.get("res/localRepo").toFile());
		} catch (RepositoryNotFoundException ex) {
			gitRepository = Git.cloneRepository()
					.setURI("https://github.com/FranQuintelaDev/bootcampsolera-vallesilicona")
					.setDirectory(Paths.get("res/localRepo").toFile()).call();
		}
		gitRepository.pull().call();
		return gitRepository;
	}

	public Map<String, Integer> getWinnersFromTeamDataJson(JsonObject teamDataJson) {
		teamDataJson.getAsJsonArray("teamdata");
		Map<String, Integer> winningTeams = new HashMap<String, Integer>();
		JsonArray teamsJsonArray = teamDataJson.getAsJsonArray("teamdata");
		Integer totalPointsWinningTeams = 0;
		for (JsonElement teamsJsonElement : teamsJsonArray) {
			Integer totalPointsCurrentTeam = 0;
			JsonArray actividadesJsonArray = teamsJsonElement.getAsJsonObject().getAsJsonArray("actividades");

			for (JsonElement actividadJsonElement : actividadesJsonArray) {
				totalPointsCurrentTeam += actividadJsonElement.getAsJsonObject().get("puntos").getAsInt();
			}

			if (totalPointsCurrentTeam > totalPointsWinningTeams) {
				totalPointsWinningTeams = totalPointsCurrentTeam;
				winningTeams.clear();

			}
			;
			if (totalPointsCurrentTeam >= totalPointsWinningTeams)
				winningTeams.put(teamsJsonElement.getAsJsonObject().get("name").getAsString(), totalPointsWinningTeams);
		}

		return winningTeams;
	}

	void sendWinningTeamsMessage(Map<String, Integer> winningTeams) throws TelegramApiException {
		valleSiliconaTelegramBot.sendMessage(
				winningTeams.size() > 1 ?  "Los equipos ganadores actualmente son..." : "El equipo ganador actual es...");
		for (Map.Entry<String, Integer> winningTeam : winningTeams.entrySet()) {
			valleSiliconaTelegramBot.sendMessage((winningTeam.getKey() + ": " + winningTeam.getValue() + " puntos"));		
		}
		valleSiliconaTelegramBot.sendMessage(("¡Y 100 PUNTOS PARA GRYFFINDOR!"));



	}

}

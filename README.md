
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

- Create a new Java package. Ex: com.example.demo.telegrambot

- Create a new Java class. Ex: ValleSiliconaTelegramBot

Para configurar el Bot se deben cambiar los valores de BOT_TOKEN, BOT_NAME y groupId.

```java
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

```

- Create a new Java package. Ex: com.example.demo.service

- Create a new Java class. Ex: ValleSiliconaBotService

Para configurar el servicio a nuestro repositorio habría que cambiar GIT_REMOTE.

```java

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

```



- Create a new Java package. Ex: com.example.bot.controller

- Create a new Java class. Ex: ValleSiliconaBotController

```java
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


```

Con esto estaría configurado el endpoint asociado al webhook (controller + service).


Para asociar este endpoint al webhook debemos desplegarlo o en su defecto exponer el puerto
usando 'ngrok' como se explica en  - [exposing-localhost-to-the-internet](https://docs.github.com/en/developers/webhooks-and-events/webhooks/creating-webhooks#exposing-localhost-to-the-internet)
 
Por último quedaría crear un webhook en el repositorio GitHub y asociarlo a esta url expuesta.

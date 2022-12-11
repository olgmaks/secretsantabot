package ua.bot.secretsaintnicholas;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;
import ua.bot.secretsaintnicholas.bot.telegram.SecretSaintNicholasBot;

import javax.annotation.PostConstruct;
import javax.ws.rs.GET;

@SpringBootApplication
@RestController
public class SecretsaintnicholasApplication {

    @Autowired
    private SecretSaintNicholasBot bot;

    @GetMapping("/health")
    public String helth() {
        return "OK";
    }
    @PostConstruct
    public void createBot() throws TelegramApiException {
        TelegramBotsApi botsApi = new TelegramBotsApi(DefaultBotSession.class);
        // Register your newly created AbilityBot
        botsApi.registerBot(bot);
    }

    public static void main(String[] args) {
        System.setProperty("jdk.tls.client.protocols", "TLSv1.2");
        SpringApplication.run(SecretsaintnicholasApplication.class, args);
    }

}

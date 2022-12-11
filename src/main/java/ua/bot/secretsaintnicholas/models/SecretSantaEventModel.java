package ua.bot.secretsaintnicholas.models;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.telegram.telegrambots.meta.api.objects.User;

import java.util.List;

@Data
public class SecretSantaEventModel {

    public static final String STATUS_OPEN = "OPEN";
    public static final String STATUS_CLOSED = "CLOSED";

    @Id
    private String id;

    private String evenName;

    private UserModel admin;

    private List<UserModel> participants;

    private String status;
}

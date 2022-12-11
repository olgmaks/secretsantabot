package ua.bot.secretsaintnicholas.models;

import lombok.Builder;
import lombok.Data;
import org.springframework.data.annotation.Id;

@Data
@Builder
public class UserModel {

    @Id
    private Long id;
    private String firstName;
    private String lastName;
    private String username;

    public String getStringName() {
        return new StringBuilder()
                .append(firstName != null ? firstName + " " : "")
                .append(lastName != null ? lastName + " " : "")
                .append(username != null ? username + " " : "")
                .toString().trim();
    }
}

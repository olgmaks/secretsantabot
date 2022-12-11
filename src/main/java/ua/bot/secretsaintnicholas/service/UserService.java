package ua.bot.secretsaintnicholas.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.User;
import ua.bot.secretsaintnicholas.models.UserModel;
import ua.bot.secretsaintnicholas.repositories.UserRepository;

import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    public UserModel welcomeUser(User user) {

        final Optional<UserModel> existingUser = userRepository.findById(user.getId());

        if (!existingUser.isPresent()) {

            log.info("creating new user ... ");

            final UserModel build = UserModel.builder()
                    .id(user.getId())
                    .username(user.getUserName())
                    .firstName(user.getFirstName())
                    .lastName(user.getLastName()).build();

            return userRepository.save(build);
        }

        return existingUser.get();
    }
}

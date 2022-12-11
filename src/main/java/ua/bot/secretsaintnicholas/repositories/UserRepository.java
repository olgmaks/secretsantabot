package ua.bot.secretsaintnicholas.repositories;

import org.springframework.data.mongodb.repository.MongoRepository;
import ua.bot.secretsaintnicholas.models.UserModel;

public interface UserRepository extends MongoRepository<UserModel, Long> {
}

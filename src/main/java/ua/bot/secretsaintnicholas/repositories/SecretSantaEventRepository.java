package ua.bot.secretsaintnicholas.repositories;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import ua.bot.secretsaintnicholas.models.SecretSantaEventModel;

@Repository
public interface SecretSantaEventRepository extends MongoRepository<SecretSantaEventModel, String> {

    boolean existsByEvenName(String eventName);

    SecretSantaEventModel findByEvenName(String eventName);
}

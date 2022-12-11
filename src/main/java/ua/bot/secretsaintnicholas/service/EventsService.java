package ua.bot.secretsaintnicholas.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomUtils;
import org.springframework.stereotype.Service;
import ua.bot.secretsaintnicholas.models.SecretSantaEventModel;
import ua.bot.secretsaintnicholas.models.UserModel;
import ua.bot.secretsaintnicholas.repositories.SecretSantaEventRepository;

import java.util.*;

@Service
@Slf4j
@RequiredArgsConstructor
public class EventsService {

    private final SecretSantaEventRepository secretSantaEventRepository;

    public boolean isEventAlreadyExists(String eventName) {
        return secretSantaEventRepository.existsByEvenName(eventName);
    }

    public boolean isAlreadyParticipant(String eventName, UserModel userModel) {
        return secretSantaEventRepository.findByEvenName(eventName).getParticipants().contains(userModel);
    }

    public void createEvent(String eventName, UserModel userModel) {
        final SecretSantaEventModel model = new SecretSantaEventModel();
        model.setAdmin(userModel);
        model.setEvenName(eventName);
        model.setStatus(SecretSantaEventModel.STATUS_OPEN);
        model.setParticipants(Collections.emptyList());
        secretSantaEventRepository.save(model);
    }

    public void joinUser(String eventName, UserModel userModel) {
        SecretSantaEventModel eventModel = secretSantaEventRepository.findByEvenName(eventName);
        eventModel.getParticipants().add(userModel);
        secretSantaEventRepository.save(eventModel);
    }

    public Map<UserModel, UserModel> processEvent(String eventName) {
        final SecretSantaEventModel eventModel = getEvent(eventName);
        final List<UserModel> participants = eventModel.getParticipants();
        if (participants.size() <= 1) {
            return new HashMap<>();
        }
        final Map<UserModel, UserModel> gifterReceiverMap = new HashMap<>();
        for (UserModel gifter : new ArrayList<>(participants)) {
            UserModel receiver;
            do {
                receiver = participants.get(RandomUtils.nextInt(0, participants.size()));
            }
            while (receiver.getId().equals(gifter.getId()));
            gifterReceiverMap.put(gifter, receiver);
            participants.remove(receiver);
        }
        return gifterReceiverMap;
    }

    public SecretSantaEventModel getEvent(String eventName) {
        return secretSantaEventRepository.findByEvenName(eventName);
    }

    public void closeEvent(String eventName) {
        final SecretSantaEventModel eventModel = secretSantaEventRepository.findByEvenName(eventName);
        eventModel.setStatus(SecretSantaEventModel.STATUS_CLOSED);
        secretSantaEventRepository.save(eventModel);
    }
}

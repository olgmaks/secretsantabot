package ua.bot.secretsaintnicholas.bot.telegram;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.abilitybots.api.bot.AbilityBot;
import org.telegram.abilitybots.api.bot.BaseAbilityBot;
import org.telegram.abilitybots.api.objects.*;
import org.telegram.telegrambots.meta.api.objects.Update;
import ua.bot.secretsaintnicholas.models.SecretSantaEventModel;
import ua.bot.secretsaintnicholas.models.UserModel;
import ua.bot.secretsaintnicholas.service.EventsService;
import ua.bot.secretsaintnicholas.service.UserService;

import java.util.List;
import java.util.Map;

import static java.lang.String.format;
import static org.telegram.abilitybots.api.util.AbilityUtils.getChatId;

@Component
@Slf4j

public class SecretSaintNicholasBot extends AbilityBot {

    public static final String CREATE_ACTION = "CREATE_ACTION";
    public static final String JOIN_ACTION = "JOIN_ACTION";
    public static final String PROCESS_ACTION = "PROCESS_ACTION";
    public static final String INVALID_EVENT_NAME = "Назва події повинна бути більше 3х символів і не повинна містити пробілів";
    public static final String EVENT_CREATED = "Подію створено, долучайте друзів ...";
    public static final String EVENT_ALREADY_EXISTS = "подія з такою назвою вже створена, спробуйте іншу ...";
    public static final String NO_SUCH_EVENT = "Події з такою назвою не існує, спробуйте уточнити в організатора";
    public static final String JOINED_SUCCESSFULLY = "Вітаю, ви усіпшно зареєструвалися :)";
    public static final String NEW_PARTICIPANT_NOTIFICATION = "(%s) візьме увасть у події %s";
    public static final String CURRENT_PARTICIPANTS = "Наразі у %s беруть участь %s людей";
    public static final String ONLY_ADMIN = "Завершити реєстрацію може лише організатор";
    public static final String CONGRATS = "Вітаю, %s . Ви будете секретним Миколайчиком для %s.";
    public static final String EVENT_CLOSED = "Реєстрація на подію закрита";
    public static final String EVENT_SUCCESSFULLY_CLOSED = "Подія %s - успішно закрита.";
    public static final String TWO_LESS_OF_PARTICIPANTS = "занадто мало учасників :(";

    private final UserService userService;
    private final EventsService eventsService;

    protected SecretSaintNicholasBot(
            @Value("${telegram.bot.api.token}") String botToken,
            @Value("${telegram.bot.api.username}") String botUsername,
            UserService userService, EventsService eventsService) {
        super(botToken, botUsername);
        this.userService = userService;
        this.eventsService = eventsService;
    }

    @Override
    public long creatorId() {
        return 428951355;
    }

    @Override
    public void onRegister() {
        super.onRegister();
        db.clear();
    }

    public Ability replyToStart() {
        return Ability
                .builder()
                .name("start")
                .locality(Locality.ALL)
                .privacy(Privacy.PUBLIC)
                .action(ctx -> {
                    silent.send("" +
                                    "/create - організувати Секретного Миколая\n\n" +
                                    "/join - приєднатися до існуючої події\n\n" +
                                    "/process - закрити реєстрацію та розіслати імена учасників (Лише організатор події)",
                            ctx.chatId());
                })
                .build();
    }


    public Ability replyToCreateSecretSantaEvent() {
        return Ability
                .builder()
                .name("create")
                .locality(Locality.ALL)
                .privacy(Privacy.PUBLIC)
                .action(ctx -> {
                    setUserAction(CREATE_ACTION);
                    silent.send("Введіть унікальну назву-ідентифікатор щоб інші учасники могли долучитися", ctx.chatId());
                })
                .build();
    }

    public Ability replyToJoinSecretSantaEvent() {
        return Ability
                .builder()
                .name("join")
                .locality(Locality.ALL)
                .privacy(Privacy.PUBLIC)
                .action(ctx -> {
                    setUserAction(JOIN_ACTION);
                    silent.send("Введіть назву-ідентифікатор яку вказав організатор події", ctx.chatId());
                })
                .build();
    }

    public Ability replyToProcessSecretSantaEvent() {
        return Ability
                .builder()
                .name("process")
                .locality(Locality.ALL)
                .privacy(Privacy.PUBLIC)
                .action(ctx -> {
                    setUserAction(PROCESS_ACTION);
                    silent.send("Введіть назву-ідентифікатор яку вказав організатор події", ctx.chatId());
                })
                .build();
    }


    public ReplyFlow textReplyFlow() {
        return ReplyFlow.builder(db)
                .onlyIf(upt -> isNotCommand(upt.getMessage().getText()))
                .next(Reply.of((bot, upt) -> silent.send(INVALID_EVENT_NAME, getChatId(upt)),
                        udt -> !isValidEventName(udt.getMessage().getText())))
                .next(ReplyFlow.builder(db)
                        // check if valid event name
                        .onlyIf(upt -> isValidEventName(upt.getMessage().getText()))
                        .next(ReplyFlow.builder(db)
                                // check if create action
                                .onlyIf(upt -> isCreateAction())
                                .next(Reply.of((bot, upt) -> silent.send(EVENT_ALREADY_EXISTS, getChatId(upt)),
                                        udt -> isEventAlreadyExists(udt.getMessage().getText())))
                                .next(Reply.of((bot, udt) -> handleCreateEvent(bot, udt),
                                        udt -> !isEventAlreadyExists(udt.getMessage().getText())))
                                .build())
                        .next(ReplyFlow.builder(db)
                                .onlyIf(upt -> isJoinAction())
                                .next(Reply.of((bot, upt) -> silent.send(NO_SUCH_EVENT, getChatId(upt)),
                                        udt -> !isEventAlreadyExists(udt.getMessage().getText())))
                                .next(Reply.of(this::handleJoinEvent,
                                        udt -> isEventAlreadyExists(udt.getMessage().getText())))
                                .build())
                        .next(ReplyFlow.builder(db)
                                .onlyIf(upt -> isProcessAction())
                                .next(Reply.of((bot, upt) -> silent.send(NO_SUCH_EVENT, getChatId(upt)),
                                        udt -> !isEventAlreadyExists(udt.getMessage().getText())))
                                .next(ReplyFlow.builder(db)
                                        .onlyIf(upt -> isEventAlreadyExists(upt.getMessage().getText()))
                                        .next(Reply.of((bot, upt) -> silent.send(ONLY_ADMIN, getChatId(upt)), upt -> !isEventAdmin(upt)))
                                        .next(Reply.of(this::handleProcessEvent, this::isEventAdmin))
                                        .build())
                                .build())
                        .build())
                .build();


    }

    private boolean isEventAdmin(Update update) {
        final UserModel userModel = userService.welcomeUser(update.getMessage().getFrom());
        return eventsService.getEvent(update.getMessage().getText()).getAdmin().getId().equals(userModel.getId());
    }

    private void handleProcessEvent(BaseAbilityBot bot, Update update) {
        try {
            final String eventName = update.getMessage().getText();
            final Map<UserModel, UserModel> participantMap = eventsService.processEvent(eventName);
            if (participantMap.isEmpty()) {
                silent.send(TWO_LESS_OF_PARTICIPANTS, getChatId(update));
            } else {
                participantMap.forEach((gifter, receiver) -> {
                    final String message = format(CONGRATS,
                            gifter.getStringName(),
                            receiver.getStringName());
                    silent.send(message, gifter.getId());
                });
                eventsService.closeEvent(eventName);
                silent.send(String.format(EVENT_SUCCESSFULLY_CLOSED, update.getMessage().getText()), getChatId(update));
            }
        } catch (Throwable e) {
          silent.send(e.getMessage(), creatorId());
        }
    }

    private void handleJoinEvent(BaseAbilityBot bot, Update update) {
        try {
            final UserModel participant = userService.welcomeUser(update.getMessage().getFrom());
            final String eventName = update.getMessage().getText();
            final SecretSantaEventModel eventModel = eventsService.getEvent(eventName);
            final List<UserModel> participants = eventModel.getParticipants();
            if (SecretSantaEventModel.STATUS_OPEN.equalsIgnoreCase(eventModel.getStatus())) {

                if (eventsService.isAlreadyParticipant(eventName, participant)) {
                    silent.send("Ви вже учасник цієї події", getChatId(update));
                    silent.send(format(CURRENT_PARTICIPANTS, eventName, eventModel.getParticipants().size()), getChatId(update));
                } else {
                    eventsService.joinUser(eventName, participant);
                    silent.send(JOINED_SUCCESSFULLY, getChatId(update));
                    participants.forEach(p -> {
                        silent.send(format(NEW_PARTICIPANT_NOTIFICATION,
                                participant.getStringName(), eventName),
                                p.getId());
                        silent.send(format(CURRENT_PARTICIPANTS, eventName, eventsService.getEvent(eventName).getParticipants().size()), p.getId());
                    });
                }
            } else {
                silent.send(EVENT_CLOSED, getChatId(update));
            }
        } catch (Throwable e) {
            silent.send(e.getMessage(), creatorId());
        }
    }

    private void handleCreateEvent(BaseAbilityBot bot, Update update) {
        try {
            final UserModel admin = userService.welcomeUser(update.getMessage().getFrom());
            String eventName = update.getMessage().getText();
            eventsService.createEvent(eventName, admin);
            eventsService.joinUser(eventName, admin);
            silent.send(EVENT_CREATED, update.getMessage().getChatId());
        } catch (Throwable e) {
            silent.send(e.getMessage(), creatorId());
        }
    }

    private boolean isEventAlreadyExists(String eventName) {
        return eventsService.isEventAlreadyExists(eventName);
    }

    private boolean isNotCommand(String text) {
        return !StringUtils.containsAny(text, "/start", "/create", "/join", "/process");
    }

    private boolean isValidEventName(String text) {
        final String trimmed = StringUtils.defaultIfBlank(text, StringUtils.EMPTY);
        return trimmed.length() >= 4 && !trimmed.contains(" ");
    }

    private boolean isCreateAction() {
        return getUserAction() != null && CREATE_ACTION.equalsIgnoreCase(getUserAction());
    }

    private boolean isJoinAction() {
        return getUserAction() != null && JOIN_ACTION.equalsIgnoreCase(getUserAction());
    }

    private boolean isProcessAction() {
        return getUserAction() != null && PROCESS_ACTION.equalsIgnoreCase(getUserAction());
    }

    private String getUserAction() {
        return db.<String>getVar("ACTION").get();
    }

    private void setUserAction(String action) {
        db.getVar("ACTION").set(action);
    }
}
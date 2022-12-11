package ua.bot.secretsaintnicholas.bot.telegram;

import lombok.SneakyThrows;
import org.springframework.stereotype.Component;
import org.telegram.abilitybots.api.objects.MessageContext;
import org.telegram.abilitybots.api.sender.MessageSender;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import ua.bot.secretsaintnicholas.bot.telegram.keyboard.KeyboardFactory;

@Component
public class MessageFactory {


    @SneakyThrows
    public void start(MessageSender sender, MessageContext messageContext) {

        final String chatId = String.valueOf(messageContext.chatId());

        sender.execute(SendMessage.builder()
                .text("Привіт")
                .chatId(chatId).build());

        sender.execute(SendMessage.builder()
                .text("Вітаємо в Секретому Миколаї :) " +
                        "\n\nЯкщо ви хочете організувати секретного санту серед своїх друзів - натисніть 'Створити') " +
                        "\n\nА якщо хтось з ваших друзів вже організував - натисніть 'Приєднатися'")
                .chatId(chatId)
                .replyMarkup(KeyboardFactory.startButtons()).build());

    }
}
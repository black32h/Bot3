package org.example;

import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TelegramBot extends TelegramLongPollingBot {

    private Map<Long, String> userStates = new HashMap<>();
    private Map<Long, Double> carPrices = new HashMap<>();
    private Map<Long, Double> firstPayments = new HashMap<>();
    private Map<Long, Integer> loanTerms = new HashMap<>();
    private Map<Long, String> selectedBanks = new HashMap<>();

    private LoanTerms loanTermsUtil = new LoanTerms() {};
    private void sendMessage(long chatId, String text) {
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText(text);

        try {
            execute(message); // Відправка повідомлення
        } catch (TelegramApiException e) {
            e.printStackTrace(); // Виведення помилки в консоль
        }
    }


    @Override
    public void onUpdateReceived( Update update) {
        long chatId = update.hasMessage() ? update.getMessage().getChatId() : update.getCallbackQuery().getMessage().getChatId();

        if (update.hasMessage() && update.getMessage().hasText()) {
            String message = update.getMessage().getText();
            handleMessage(chatId, message);
        } else if (update.hasCallbackQuery()) {
            String callbackData = update.getCallbackQuery().getData();
            handleCallback(chatId, callbackData);
        }
    }

    private void handleMessage(long chatId, String message) {
        switch (userStates.getOrDefault(chatId, "")) {
            case "WAITING_FOR_CAR_PRICE":
                handleCarPriceInput(chatId, message);
                break;
            case "WAITING_FOR_FIRST_PAYMENT":
                handleFirstPaymentInput(chatId, message);
                break;
            case "WAITING_FOR_LOAN_TERM":
                handleLoanTermInput(chatId, message);
                break;
            default:
                if (message.equals("/start")) {
                    sendCarOptions(chatId);
                } else {
                    sendMessage(chatId, "Невідома команда. Будь ласка, використовуйте /start для початку.");
                }
                break;
        }
    }

    private void handleCallback(long chatId, String callbackData) {
        // Обробка вибору автомобіля
        switch (callbackData) {
            case "Тойота":
            case "Мазда":
            case "MG":
            case "Сітроен":
            case "Сузукі":
            case "Кіа":
            case "Хюндай":
            case "Форд":
                userStates.put(chatId, "WAITING_FOR_CAR_PRICE");
                sendMessage(chatId, "Введіть вартість автомобіля:");
                break;
            // Обробка вибору банку
            case "Ощадбанк":
            case "Приватбанк":
            case "Кредит Агриколь":
            case "Укргазбанк":
                selectedBanks.put(chatId, callbackData);
                userStates.put(chatId, "WAITING_FOR_LOAN_TERM");
                sendLoanTermOptions(chatId);
                break;
            // Обробка вибору терміна кредиту
            default:
                try {
                    int loanTerm = Integer.parseInt(callbackData);
                    handleLoanTermInput(chatId, String.valueOf(loanTerm));
                } catch (NumberFormatException e) {
                    sendMessage(chatId, "Невідома команда.");
                }
                break;
        }
    }

    private void sendLoanTermOptions(long chatId) {
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText("Виберіть термін кредиту в місяцях:");

        InlineKeyboardMarkup keyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> buttons = new ArrayList<>();

        for (int term : loanTermsUtil.getTerms()) {
            List<InlineKeyboardButton> row = new ArrayList<>();
            row.add(InlineKeyboardButton.builder().text(term + " місяців").callbackData(String.valueOf(term)).build());
            buttons.add(row);
        }

        keyboardMarkup.setKeyboard(buttons);
        message.setReplyMarkup(keyboardMarkup);

        try {
            execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    private void handleLoanTermInput(long chatId, String message) {
        try {
            int loanTerm = Integer.parseInt(message);
            if (!loanTermsUtil.isValidTerm(loanTerm)) {
                sendMessage(chatId, "Будь ласка, виберіть коректний термін кредиту: 12, 24, 36, 48, 60, 72, 84 місяців.");
                return;
            }

            loanTerms.put(chatId, loanTerm);
            double carPrice = carPrices.get(chatId);
            double firstPayment = firstPayments.get(chatId);
            String bank = selectedBanks.get(chatId);

            if (bank == null) {
                sendMessage(chatId, "Будь ласка, виберіть банк перед введенням терміну кредиту.");
                return;
            }

            sendMessage(chatId, String.format("Введені дані: ціна авто = %.2f, перший внесок = %.2f, банк = %s, термін = %d", carPrice, firstPayment, bank, loanTerm));

            double downPaymentPercentage = (firstPayment / carPrice) * 100;
            String loanResults = calculateLoanConditions(carPrice, downPaymentPercentage, bank, loanTerm);
            sendMessage(chatId, loanResults);

            resetUserState(chatId); // Скидання стану
        } catch (NumberFormatException e) {
            sendMessage(chatId, "Будь ласка, введіть коректний термін кредиту в місяцях.");
        } catch (Exception e) {
            sendMessage(chatId, "Сталася помилка: " + e.getMessage());
        }
    }

    private void resetUserState(long chatId) {
        userStates.remove(chatId);
        carPrices.remove(chatId);
        firstPayments.remove(chatId);
        loanTerms.remove(chatId);
        selectedBanks.remove(chatId);
    }

    private String calculateLoanConditions(double carPrice, double downPaymentPercentage, String bank, int loanTerm) {
        switch (bank) {
            case "Ощадбанк":
                return Oschad.calculate(carPrice, downPaymentPercentage, loanTerm);
            case "Приватбанк":
                return Privat.calculate(carPrice, downPaymentPercentage, loanTerm);
            case "Кредит Агриколь":
                return KreditAgrikol.calculate(carPrice, downPaymentPercentage, loanTerm);
            case "Укргазбанк":
                return UkrGazBank.calculate(carPrice, downPaymentPercentage, loanTerm);
            default:
                return "Невідомий банк.";
        }
    }

    private void handleCarPriceInput(long chatId, String message) {
        try {
            double price = Double.parseDouble(message);
            carPrices.put(chatId, price);
            userStates.put(chatId, "WAITING_FOR_FIRST_PAYMENT");
            sendMessage(chatId, "Введіть перший внесок:");
        } catch (NumberFormatException e) {
            sendMessage(chatId, "Будь ласка, введіть коректну вартість автомобіля.");
        }
    }

    private void handleFirstPaymentInput(long chatId, String message) {
        try {
            double firstPayment = Double.parseDouble(message);
            firstPayments.put(chatId, firstPayment);
            userStates.put(chatId, "WAITING_FOR_BANK_SELECTION");
            sendLoanOptions(chatId);
        } catch (NumberFormatException e) {
            sendMessage(chatId, "Будь ласка, введіть коректну суму першого внеску.");
        }
    }

    private void sendCarOptions(long chatId) {
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText("Виберіть марку автомобіля:");

        InlineKeyboardMarkup keyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> buttons = new ArrayList<>();

        List<InlineKeyboardButton> row1 = new ArrayList<>();
        row1.add(InlineKeyboardButton.builder().text("Тойота").callbackData("Тойота").build());
        row1.add(InlineKeyboardButton.builder().text("Мазда").callbackData("Мазда").build());

        List<InlineKeyboardButton> row2 = new ArrayList<>();
        row2.add(InlineKeyboardButton.builder().text("MG").callbackData("MG").build());
        row2.add(InlineKeyboardButton.builder().text("Сітроен").callbackData("Сітроен").build());

        List<InlineKeyboardButton> row3 = new ArrayList<>();
        row3.add(InlineKeyboardButton.builder().text("Сузукі").callbackData("Сузукі").build());
        row3.add(InlineKeyboardButton.builder().text("Кіа").callbackData("Кіа").build());

        List<InlineKeyboardButton> row4 = new ArrayList<>();
        row4.add(InlineKeyboardButton.builder().text("Хюндай").callbackData("Хюндай").build());
        row4.add(InlineKeyboardButton.builder().text("Форд").callbackData("Форд").build());

        buttons.add(row1);
        buttons.add(row2);
        buttons.add(row3);
        buttons.add(row4);
        keyboardMarkup.setKeyboard(buttons);
        message.setReplyMarkup(keyboardMarkup);

        try {
            execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    private void sendLoanOptions(long chatId) {
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText("Виберіть банк:");

        InlineKeyboardMarkup keyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> buttons = new ArrayList<>();

        // Додавання кнопок для вибору банків
        List<InlineKeyboardButton> row1 = new ArrayList<>();
        row1.add(InlineKeyboardButton.builder().text("Ощадбанк").callbackData("Ощадбанк").build());
        row1.add(InlineKeyboardButton.builder().text("Приватбанк").callbackData("Приватбанк").build());

        List<InlineKeyboardButton> row2 = new ArrayList<>();
        row2.add(InlineKeyboardButton.builder().text("Кредит Агриколь").callbackData("Кредит Агриколь").build());
        row2.add(InlineKeyboardButton.builder().text("Укргазбанк").callbackData("Укргазбанк").build());

        buttons.add(row1);
        buttons.add(row2);
        keyboardMarkup.setKeyboard(buttons);
        message.setReplyMarkup(keyboardMarkup);

        try {
            execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    @Override
    public String getBotUsername() {
        return "MazdaCredit_Bot"; // Введіть ім'я вашого бота
    }

    @Override
    public String getBotToken() {
        return "7176542474:AAFJbodxYH-70q2zXPFCIs2SsVUZGhFVj8Y"; // Введіть токен вашого бота
    }
}

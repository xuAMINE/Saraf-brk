package com.saraf.service.telegram;

import com.saraf.service.transfer.Transfer;
import com.saraf.service.transfer.TransferAdminDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

@Service
@RequiredArgsConstructor
public class TelegramBot {

    @Value("${application.telegram.bot-token}")
    private String BOT_TOKEN;
    @Value("${application.telegram.channel-id}")
    private String CHANNEL_ID;

    public void sendMessageToChannel(String message) {
        try {
            String apiUrl = "https://api.telegram.org/bot" + BOT_TOKEN + "/sendMessage";
            HttpURLConnection conn = getHttpURLConnection(message, apiUrl);

            // Check response code
            int responseCode = conn.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                System.out.println("Message sent successfully.");
            } else {
                System.out.println("Failed to send message. Response Code: " + responseCode);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void sendTransferToChannel(TransferAdminDTO transferDto) {
        String message = formatTransferMessage(transferDto);
        sendMessageToChannel(message);
    }


    public void sendNewRateToChannel(Integer newRate, int oldRate) {
        String message = String.format(
                """
                📈 *Exchange Rate Update* 📈
                    - Previous Rate: %d
                    - New Rate: %d""",
                oldRate,
                newRate
        );

        sendMessageToChannel(message);
    }

    private String formatTransferMessage(TransferAdminDTO transferDto) {
        return String.format(
                "📤 *NEW TRANSFER*\n\n" +
                        "🏦 Recipient: %s\n" +
                        "💰 Amount: %s\n" +
                        "💵 Amount to be Received: %s\n" +
                        "💳 CCP: %s\n" +
                        "📅 Date: %s\n" +
                        "💳 Payment Method: %s\n" +
                        "🔢 Code: %s\n" +
                        "👤 User: %s %s\n",
                transferDto.getRecipientFullName(),
                transferDto.getAmount(),
                transferDto.getAmountReceived(),
                transferDto.getRecipientCCP(),
                transferDto.getTransferDate().toLocalDate(),
                transferDto.getPaymentMethod(),
                transferDto.getCode(),
                transferDto.getFirstName(),
                transferDto.getLastName()
        );
    }

    private HttpURLConnection getHttpURLConnection(String message, String apiUrl) throws IOException {
        URL url = new URL(apiUrl);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setDoOutput(true);

        // Create JSON payload
        String jsonPayload = "{ \"chat_id\": \"" + CHANNEL_ID + "\", \"text\": \"" + message + "\" }";

        // Send the JSON payload
        try (OutputStream os = conn.getOutputStream()) {
            byte[] input = jsonPayload.getBytes("utf-8");
            os.write(input, 0, input.length);
        }
        return conn;
    }

}

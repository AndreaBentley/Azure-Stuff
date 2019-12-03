package com.example.AzurePeekSendDemo;


import com.google.gson.reflect.TypeToken;
import com.microsoft.azure.servicebus.*;
import com.microsoft.azure.servicebus.primitives.ConnectionStringBuilder;
import com.google.gson.Gson;

import java.time.Duration;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;



import static java.nio.charset.StandardCharsets.UTF_8;

@RestController
class Program {

    @GetMapping("/receivemessages")
    public void run() throws Exception {
        // Create a QueueClient instance for receiving using the connection string builder
        // We set the receive mode to "PeekLock", meaning the message is delivered
        // under a lock and must be acknowledged ("completed") to be removed from the queue
        QueueClient receiveClient =
                new QueueClient(new ConnectionStringBuilder("Endpoint=sb://avantarde-poc-bus.servicebus.windows.net/;SharedAccessKeyName=df-process-data;SharedAccessKey=I19UFNOto9Y1rK+yTPSRKLmXju4eSaNH+hxFkU67jHM=;EntityPath=df-process-data", "df-process-data"), ReceiveMode.PEEKLOCK);
        this.registerReceiver(receiveClient);
        // shut down receiver to close the receive loop
        receiveClient.close();
    }

    void registerReceiver(QueueClient queueClient) throws Exception {
        // register the RegisterMessageHandler callback
        queueClient.registerMessageHandler(new IMessageHandler() {
            // callback invoked when the message handler loop has obtained a message
            public CompletableFuture<Void> onMessageAsync(IMessage message) {
                // receives message is passed to callback
                if (message.getLabel() != null && message.getContentType() != null &&  message.getLabel().contentEquals("Scientist") &&  message.getContentType().contentEquals("application/json")) {
                    byte[] body = message.getBody();
                    Gson gson = new Gson();
                    Map scientist = gson.fromJson(new String(body, UTF_8), Map.class);
                    System.out.printf(
                            "\n\t\t\t\tMessage received: \n\t\t\t\t\t\tMessageId = %s, \n\t\t\t\t\t\tSequenceNumber = %s, \n\t\t\t\t\t\tEnqueuedTimeUtc = %s," +
                                    "\n\t\t\t\t\t\tExpiresAtUtc = %s, \n\t\t\t\t\t\tContentType = \"%s\",  \n\t\t\t\t\t\tContent: [ firstName = %s, name = %s ]\n",
                            message.getMessageId(),
                            message.getSequenceNumber(),
                            message.getEnqueuedTimeUtc(),
                            message.getExpiresAtUtc(),
                            message.getContentType(),
                            scientist != null ? scientist.get("firstName") : "",
                            scientist != null ? scientist.get("name") : ""
                    );
                }
                return CompletableFuture.completedFuture(null);
            }
            // callback invoked when the message handler has an exception to report
            public void notifyException(Throwable throwable, ExceptionPhase exceptionPhase) {
                System.out.printf(exceptionPhase + "-" + throwable.getMessage());
            }
        },
        // 1 concurrent call, messages are auto-completed, auto-renew duration
        new MessageHandlerOptions(1, true, Duration.ofMinutes(1)));
    }

}

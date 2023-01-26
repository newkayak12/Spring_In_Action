package com.example.integration.settings;

import org.springframework.integration.annotation.MessagingGateway;
import org.springframework.messaging.handler.annotation.Header;

@MessagingGateway(defaultRequestChannel = "textIntChannel")
public interface FileWriterGateway {
    void writeToFile (@Header("file_name") String fileName, String data);
}

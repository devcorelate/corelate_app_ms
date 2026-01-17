package com.corelate.app.functions;

import com.corelate.app.service.IAppService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.function.Consumer;

@Configuration
public class AppFunctions {

    private static final Logger log = LoggerFactory.getLogger(AppFunctions.class);

    @Bean
    public Consumer<String> updateCommunication(IAppService iAppService) {
        return formId -> {
            log.info("Updating Communication status for the template : " + formId);
            iAppService.updateCommunicationStatus(formId);
        };
    }

}

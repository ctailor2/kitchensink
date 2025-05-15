package com.mongodbmodfactory.kitchensink_boot.config;

import com.mongodbmodfactory.kitchensink_boot.member.MemberValidator;
import jakarta.validation.Validator;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.rest.core.event.ValidatingRepositoryEventListener;
import org.springframework.data.rest.webmvc.config.RepositoryRestConfigurer;

@Configuration
public class RepositoryRestConfig implements RepositoryRestConfigurer {
    private final Validator jakartaValidator;

    public RepositoryRestConfig(Validator jakartaValidator) {
        this.jakartaValidator = jakartaValidator;
    }

    @Override
    public void configureValidatingRepositoryEventListener(ValidatingRepositoryEventListener validatingListener) {
        validatingListener.addValidator("beforeCreate", new MemberValidator(jakartaValidator));
    }
}

package com.mongodbmodfactory.kitchensink_boot.member;

import org.springframework.data.rest.core.annotation.HandleAfterCreate;
import org.springframework.data.rest.core.annotation.RepositoryEventHandler;
import org.springframework.stereotype.Component;

@Component
@RepositoryEventHandler
public class MemberEventHandler {
    @HandleAfterCreate
    public void handleMemberCreate(Member member) {
//        Example of how to hook into the event published after the member is created
        System.out.println("handling member create = " + member);
    }
}

package com.mongodbmodfactory.kitchensink_boot.member;

import org.springframework.data.annotation.Id;

public record Member(@Id Long id, String name, String email, String phoneNumber) {
}

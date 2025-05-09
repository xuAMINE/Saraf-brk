package com.saraf.security.email;

import lombok.Getter;

@Getter
public enum EmailTemplateName {

    ACTIVATE_ACCOUNT("activate_account"),
    RESET_PASSWORD("reset_password"),
    CONTACT_CONFIRM("contact_confirm"),
    STATUS_CHANGE("status_change");

    private final String name;

    EmailTemplateName(String name) {
        this.name = name;
    }
}

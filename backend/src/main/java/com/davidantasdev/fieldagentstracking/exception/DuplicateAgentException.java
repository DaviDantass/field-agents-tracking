package com.davidantasdev.fieldagentstracking.exception;

public class DuplicateAgentException extends RuntimeException {

    public DuplicateAgentException(String name) {
        super("Ja existe um agente com o nome: " + name);
    }
}

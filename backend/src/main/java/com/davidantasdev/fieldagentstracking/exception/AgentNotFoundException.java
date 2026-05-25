package com.davidantasdev.fieldagentstracking.exception;

public class AgentNotFoundException extends RuntimeException {

    public AgentNotFoundException(Long id) {
        super("Agente nao encontrado com id: " + id);
    }
}

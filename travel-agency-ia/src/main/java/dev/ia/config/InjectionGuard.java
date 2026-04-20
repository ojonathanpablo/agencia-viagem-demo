package dev.ia.config;

import dev.ia.assistant.SecurityExpert;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.guardrail.InputGuardrail;
import dev.langchain4j.guardrail.InputGuardrailResult;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class InjectionGuard implements InputGuardrail {

    @Inject
    SecurityExpert expert;

    @Override
    public InputGuardrailResult validate(UserMessage userMessage) {
        if (expert.isAttack(userMessage.singleText())) {
            return failure("Sua mensagem foi bloqueada por conter instruções não permitidas.");

        }
        return InputGuardrailResult.success();
    }
}

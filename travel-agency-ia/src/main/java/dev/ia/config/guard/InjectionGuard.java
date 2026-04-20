package dev.ia.config.guard;

import dev.ia.assistant.SecurityExpert;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.guardrail.InputGuardrail;
import dev.langchain4j.guardrail.InputGuardrailResult;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

/**
 * Guardrail de entrada que protege o {@link dev.ia.assistant.TravelAssistant} contra prompt injection.
 * <p>
 * Intercepta toda mensagem do usuário antes de ela ser processada pelo LLM principal,
 * delegando a análise ao {@link SecurityExpert}. Se a mensagem for classificada como
 * maliciosa, o fluxo é interrompido e uma mensagem de bloqueio é retornada ao cliente.
 * Caso contrário, a mensagem segue normalmente para o pipeline RAG + MCP.
 */
@ApplicationScoped
public class InjectionGuard implements InputGuardrail {

    @Inject
    SecurityExpert expert;

    /**
     * Valida a mensagem do usuário antes de enviá-la ao LLM.
     * <p>
     * Extrai o texto da {@link UserMessage} e passa para o {@link SecurityExpert#isAttack}.
     * Se o resultado for {@code true}, retorna {@code failure} com uma mensagem amigável,
     * interrompendo o processamento. Se for {@code false}, retorna {@code success} e
     * o pipeline continua normalmente.
     *
     * @param userMessage mensagem do usuário interceptada pelo framework de guardrails
     * @return {@link InputGuardrailResult#success()} se a mensagem for segura,
     *         ou {@code failure} com mensagem de bloqueio se for detectado um ataque
     */
    @Override
    public InputGuardrailResult validate(UserMessage userMessage) {
        if (expert.isAttack(userMessage.singleText())) {
            return failure("Sua mensagem foi bloqueada por conter instruções não permitidas.");

        }
        return InputGuardrailResult.success();
    }
}

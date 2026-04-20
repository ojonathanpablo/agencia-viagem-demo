package dev.ia.config.guard;

import dev.ia.assistant.ToneJudge;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.guardrail.OutputGuardrail;
import dev.langchain4j.guardrail.OutputGuardrailResult;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

/**
 * Guardrail de saída que garante tom profissional nas respostas do {@link dev.ia.assistant.TravelAssistant}.
 * <p>
 * Intercepta toda resposta gerada pelo LLM antes de ser entregue ao usuário,
 * delegando a avaliação ao {@link ToneJudge}. Se a resposta for considerada inadequada,
 * o LLM é instruído a reescrever com linguagem mais profissional via {@code reprompt}.
 * Caso contrário, a resposta segue normalmente.
 */
@ApplicationScoped
public class ToneGuardrail implements OutputGuardrail {

    @Inject
    ToneJudge toneJudge;

    /**
     * Valida o tom da resposta gerada pelo LLM.
     * <p>
     * Extrai o texto da {@link AiMessage} e passa para {@link ToneJudge#isProfessional}.
     * Se o resultado for {@code false}, aciona {@code reprompt} solicitando ao LLM que
     * reescreva a resposta com linguagem mais adequada ao atendimento ao cliente.
     *
     * @param aiMessage resposta gerada pelo LLM interceptada pelo framework de guardrails
     * @return {@link OutputGuardrailResult#success()} se o tom for profissional,
     *         ou {@code reprompt} com instrução de reescrita se for inadequado
     */
    @Override
    public OutputGuardrailResult validate(AiMessage aiMessage) {

        if (!toneJudge.isProfessional(aiMessage.text())) {
            return reprompt(
                    aiMessage.text(),
                    "Sua resposta foi detectada como rude. Por favor, reformule sua pergunta." +
                            "Reescreva a pergunta para que ela seja mais profissional."
            );
        }

        return OutputGuardrailResult.success();
    }

}

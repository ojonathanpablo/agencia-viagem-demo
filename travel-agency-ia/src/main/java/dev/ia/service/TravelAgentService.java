package dev.ia.service;

import dev.ia.assistant.TravelAssistant;
import dev.langchain4j.guardrail.InputGuardrailException;
import dev.langchain4j.guardrail.OutputGuardrailException;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

/**
 * Serviço responsável pela lógica de negócio do assistente de viagens.
 * <p>
 * Centraliza as regras de negócio como validação de autenticação,
 * tratamento de exceções dos guardrails e mapeamento dos eventos de streaming,
 * mantendo o controller limpo e focado apenas no protocolo HTTP.
 */
@ApplicationScoped
public class TravelAgentService {

    @Inject
    TravelAssistant travelAssistant;

    /**
     * Processa a mensagem do usuário e retorna a resposta do assistente.
     * <p>
     * Valida se o usuário está autenticado antes de invocar o LLM.
     * Trata exceções de guardrails retornando mensagens amigáveis ao invés de erros HTTP.
     *
     * @param question  pergunta enviada pelo usuário
     * @param userName  nome do usuário autenticado
     * @return resposta do assistente ou mensagem de erro
     */
    public String chat(String question, String userName) {
        if (userName == null || userName.isEmpty()) {
            return "Usuário precisa estar autenticado!";
        }
        try {
            return travelAssistant.chat(userName, question, userName);
        } catch (InputGuardrailException e) {
            return "Desculpe, não consigo processar essa mensagem. Por favor, reformule sua pergunta.";
        } catch (OutputGuardrailException e) {
            return "Desculpe, ocorreu um problema ao processar a resposta. Por favor, tente novamente.";
        }
    }

//    public Multi<String> chatStream(String question, String userName) {
//        if (userName == null || userName.isEmpty()) {
//            return Multi.createFrom().item("Usuário precisa estar autenticado!");
//        }
//        return travelAssistant.chatStream(userName, question, userName).map(this::toStreamChunk);
//    }
//
//    private String toStreamChunk(ChatEvent event) {
//        if (event instanceof ChatEvent.PartialResponseEvent partial) {
//            return partial.getChunk();
//        }
//        if (event instanceof ChatEvent.ChatCompletedEvent completed) {
//            var usage = completed.getChatResponse().tokenUsage();
//            return "\n[tokens: entrada=" + usage.inputTokenCount() + " saída=" + usage.outputTokenCount() + "]";
//        }
//        return "";
//    }
//
//    public TravelPackageList listPackages(String category) {
//        TravelPackageList result = travelAssistant.listPackagesAsJson(category);
//        return new TravelPackageList(category, result.pacotes());
//    }
}

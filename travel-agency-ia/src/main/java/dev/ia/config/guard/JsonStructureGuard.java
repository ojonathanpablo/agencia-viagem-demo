package dev.ia.config.guard;

import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.guardrail.OutputGuardrail;
import dev.langchain4j.guardrail.OutputGuardrailResult;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.json.Json;
import jakarta.json.JsonReader;

import java.io.StringReader;

/**
 * Guardrail de saída que garante que a resposta do LLM seja um JSON válido.
 * <p>
 * Utilizado no método {@link dev.ia.assistant.TravelAssistant#listPackagesAsJson},
 * intercepta a resposta gerada pelo modelo e tenta interpretá-la como um objeto JSON.
 * Se a estrutura for inválida (ex: texto com markdown, resposta livre), o LLM é instruído
 * a regenerar a resposta via {@code reprompt} até produzir um JSON puro e correto.
 */
@ApplicationScoped
public class JsonStructureGuard implements OutputGuardrail {

    /**
     * Valida se a resposta gerada pelo LLM é um JSON válido.
     * <p>
     * Tenta fazer o parse da resposta como {@code JsonObject}. Se o parse falhar,
     * aciona {@code reprompt} com a mensagem de erro para que o LLM corrija a saída.
     *
     * @param aiMessage resposta gerada pelo LLM interceptada pelo framework de guardrails
     * @return {@link OutputGuardrailResult#success()} se a resposta for JSON válido,
     *         ou {@code reprompt} com instrução de correção caso contrário
     */
    @Override
    public OutputGuardrailResult validate(AiMessage aiMessage) {
        String response = aiMessage.text();
        try (JsonReader reader = Json.createReader(new StringReader(response))) {
            reader.readObject();
            return OutputGuardrailResult.success();

        } catch (Exception e) {
            return reprompt(aiMessage.text(),
                    "Erro: Sua resposta não e um json valido." +
                            "Problema encontrado: " + e.getMessage() + "." +
                            "Gere NOVAMENTE apenas o JSON, sem blocos de código markdown ou texto adicional.");
        }
    }
}

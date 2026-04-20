package dev.ia.assistant;

import dev.langchain4j.service.SystemMessage;
import io.quarkiverse.langchain4j.RegisterAiService;

/**
 * AI Service especializado em avaliar se uma resposta do assistente mantém tom profissional.
 * <p>
 * Utilizado pelo {@link dev.ia.config.guard.ToneGuardrail} como guardrail de saída —
 * toda resposta gerada pelo {@link TravelAssistant} passa por este serviço antes de
 * ser entregue ao usuário, garantindo que a comunicação da agência seja sempre adequada.
 */
@RegisterAiService
public interface ToneJudge {

    /**
     * Avalia se o texto fornecido possui tom profissional adequado para um assistente de viagens.
     * <p>
     * O LLM é instruído a reprovar respostas que contenham:
     * <ul>
     *   <li>Linguagem rude ou grosseira (ex: "Não é problema meu")</li>
     *   <li>Gírias ou linguagem excessivamente informal (ex: "Se vira aí")</li>
     *   <li>Expressões inadequadas para atendimento ao cliente (ex: "Cara, isso é chato")</li>
     * </ul>
     * E aprovar respostas educadas e formais (ex: "Sinto muito, mas isso está fora da minha alçada.").
     *
     * @param text texto da resposta gerada pelo assistente a ser avaliado
     * @return {@code true} se o tom for profissional, {@code false} caso contrário
     */
    @SystemMessage("""
        Você é um auditor de qualidade de linguagem. Sua ÚNICA tarefa é classificar se o tom do texto é profissional.
        REGRA ABSOLUTA: Responda SOMENTE com a palavra 'true' ou a palavra 'false'. Nenhum outro texto, explicação ou formatação.

        Classifique como 'false' (tom inadequado) se houver:
        - Linguagem rude ou grosseira (ex: "Não é problema meu")
        - Gírias ou linguagem excessivamente informal (ex: "Se vira aí")
        - Expressões inadequadas para atendimento ao cliente (ex: "Cara, isso é chato")

        Classifique como 'true' (tom profissional) se o texto for educado e formal, como:
        - "Sinto muito, mas isso está fora da minha alçada."
        - "Por favor, verifique os termos no site."
        - Qualquer resposta informativa e respeitosa.

        LEMBRE-SE: responda APENAS 'true' ou 'false'.
        """)
    boolean isProfessional(String text);
}

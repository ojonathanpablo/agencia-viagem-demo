package dev.ia.tools;

import dev.ia.model.Booking;
import dev.ia.model.Category;
import dev.ia.service.BookingService;
import io.quarkiverse.mcp.server.Tool;
import io.quarkiverse.mcp.server.ToolArg;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.time.LocalDate;
import java.util.List;

/**
 * Ferramentas MCP expostas ao cliente LangChain4j para gerenciamento de reservas.
 * <p>
 * Cada método anotado com {@link Tool} é registrado automaticamente pelo Quarkus MCP Server
 * e disponibilizado via protocolo MCP (Model Context Protocol) no endpoint SSE.
 * O LLM pode invocar qualquer uma dessas ferramentas durante a geração de uma resposta.
 */
@ApplicationScoped
public class BookingTools {

    @Inject
    BookingService bookingService;

    /**
     * Busca e retorna os detalhes completos de uma reserva pelo seu ID.
     * <p>
     * Chamada pelo LLM quando o usuário solicita informações sobre uma reserva específica,
     * como datas, destino, status e categoria.
     *
     * @param bookingId ID numérico único da reserva
     * @return string com os detalhes da reserva, ou mensagem de erro se não encontrada
     */
    @Tool(name = "Obtém os detalhes completos de uma reserva com base em seu número de identificação (bookingId).")
    public String getBookingDetails(
            @ToolArg(description = "O ID númerico único da reserva (ex: 12345)") long bookingId) {

        return bookingService.findBookingDetails(bookingId)
                .map(Booking::toString)
                .orElse("Reserva com ID " + bookingId + " não encontrada.");
    }

    /**
     * Cancela uma reserva existente, validando que o usuário é o titular da reserva.
     * <p>
     * O {@code name} deve corresponder exatamente ao {@code customerName} da reserva —
     * caso contrário, o cancelamento é recusado. Isso impede que um usuário cancele
     * reservas de outros clientes.
     *
     * @param bookingId ID da reserva a ser cancelada
     * @param name      nome do usuário autenticado que solicita o cancelamento
     * @return mensagem de sucesso com o ID cancelado, ou mensagem de erro se não autorizado
     */
    @Tool(name = """
                Cancela uma reserva existente com base no seu ID (bookingId).
                O usuário deve estar autenticado.
            """)
    public String cancelBooking(
            @ToolArg(description = "ID da reserva a cancelar") long bookingId,
            @ToolArg(description = "Usuário que está tentando cancelar a reserva") String name) {

        return bookingService.cancelBooking(bookingId, name)
                .map(b -> "Reserva " + b.id() + " cancelada com sucesso.")
                .orElse("Não foi possível cancelar a reserva. Verifique se o ID está correto e se você tem permissão.");
    }

    /**
     * Cria uma nova reserva de viagem para o usuário autenticado.
     * <p>
     * A reserva é criada imediatamente com status {@code CONFIRMED} e um ID gerado
     * automaticamente pela sequência interna do {@link BookingService}.
     *
     * @param destination destino da viagem (ex: "Paris", "Amazônia")
     * @param startDate   data de início no formato {@code YYYY-MM-DD}
     * @param endDate     data de fim no formato {@code YYYY-MM-DD}
     * @param category    categoria do pacote: {@code ADVENTURE} ou {@code TREASURES}
     * @param userName    nome do usuário autenticado que está fazendo a reserva
     * @return string com o resumo da reserva criada (ID, cliente, destino, período, status)
     */
    @Tool(name = """
                Cria uma nova reserva de viagem para o usuário autenticado.
                Requer: destino (destination), data de início (startDate no formato YYYY-MM-DD),
                data de fim (endDate no formato YYYY-MM-DD) e categoria (category: ADVENTURE ou TREASURES).
            """)
    public String createBooking(
           @ToolArg(description = "Destino da viagem (ex: Paris, Tokyo, Nova York)") String destination,
           @ToolArg(description = "Data de início da viagem no formato YYYY-MM-DD") LocalDate startDate,
           @ToolArg(description = "Data de fim da viagem no formato YYYY-MM-DD") LocalDate endDate,
           @ToolArg(description = "Categoria do pacote de viagem: ADVENTURE ou TREASURES") Category category,
           @ToolArg(description = "Nome do usuário que está realizando a reserva") String userName) {

        Booking booking = bookingService.createBooking(userName, destination, startDate, endDate, category);
        return "Reserva criada com sucesso! ID: " + booking.id() +
                " | Cliente: " + booking.customerName() +
                " | Destino: " + booking.destination() +
                " | Período: " + booking.startDate() + " até " + booking.endDate() +
                " | Status: " + booking.status();
    }

    /**
     * Lista todas as reservas associadas a um usuário específico.
     * <p>
     * O LLM invoca esta ferramenta quando o usuário pergunta "quais são minhas reservas"
     * ou solicita um histórico de viagens. A busca é feita por correspondência exata do nome.
     *
     * @param userName nome do usuário cujas reservas serão listadas
     * @return string com todas as reservas do usuário, ou mensagem informando que não há reservas
     */
    @Tool(name = "Lista todas as reservas de viagem de um usuário específico.")
    public String listBookingsByUser(
            @ToolArg(description = "Nome do usuário cujas reservas serão listadas") String userName) {

        List<Booking> userBookings = bookingService.findBookingsByUser(userName);
        if (userBookings.isEmpty()) {
            return "Nenhuma reserva encontrada para o usuário: " + userName;
        }

        return "Reservas de " + userName + ":\n" + userBookings.stream()
                .map(Booking::toString)
                .reduce("", (a, b) -> a + "\n" + b);
    }

    /**
     * Lista os destinos disponíveis filtrados por categoria de pacote.
     * <p>
     * Usado pelo LLM quando o usuário quer explorar opções de viagem, como
     * "quais pacotes de aventura vocês têm?" ou "mostre destinos TREASURES".
     *
     * @param category categoria de filtro: {@code ADVENTURE} ou {@code TREASURES}
     * @return string com a lista de destinos encontrados na categoria, ou mensagem se vazio
     */
    @Tool(name = "Lista os pacotes de viagem disponíveis para uma determinada categoria (ex: ADVENTURE, TREASURES).")
    public String listPackagesByCategory(
            @ToolArg(description = "Categoria utilizada como filtro para pacotes") Category category) {

        List<Booking> packages = bookingService.findPackagesByCategory(category);
        if (packages.isEmpty()) {
            return "Nenhum pacote encontrado para a categoria: " + category;
        }

        return "Pacotes encontrados para a categoria '" + category + "': " + packages.stream()
                .map(Booking::destination)
                .toList();
    }
}

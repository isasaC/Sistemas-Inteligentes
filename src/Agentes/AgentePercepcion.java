package Agentes;

import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.URI;

public class AgentePercepcion extends Agent {
    private HttpClient client;
    private final String chatBotURL = "http://yourchatboturl.com/api"; // Asegúrate de cambiar esta URL por la real

    @Override
    protected void setup() {
        client = HttpClient.newHttpClient();

        addBehaviour(new CyclicBehaviour(this) {
            public void action() {
                ACLMessage msg = myAgent.receive(MessageTemplate.MatchPerformative(ACLMessage.REQUEST));
                if (msg != null) {
                    String userQuery = msg.getContent();
                    String response = getResponseFromChatBot(userQuery);
                    ACLMessage reply = msg.createReply();
                    reply.setPerformative(ACLMessage.INFORM);
                    reply.setContent(response);
                    send(reply);
                } else {
                    block();
                }
            }
        });
    }

    private String getResponseFromChatBot(String input) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                .uri(new URI(chatBotURL))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString("{\"query\": \"" + input + "\"}"))
                .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            return response.body(); // Asumiendo que la respuesta ya es adecuada para mostrar
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}


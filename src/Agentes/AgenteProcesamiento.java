package Agentes;

import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse.BodyHandlers;

public class AgenteProcesamiento extends Agent {

    private HttpClient httpClient;

    @Override
    protected void setup() {
        httpClient = HttpClient.newHttpClient();

        addBehaviour(new CyclicBehaviour(this) {
            public void action() {
                ACLMessage msg = myAgent.receive(MessageTemplate.MatchPerformative(ACLMessage.REQUEST));
                if (msg != null) {
                    String question = msg.getContent();
                    System.out.println("Recibido: " + question);
                    handleRequest(question, msg.getSender().getLocalName());
                } else {
                    block();
                }
            }
        });
    }

    private void handleRequest(String question, String senderName) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(new URI("http://localhost:3000/api/v1/prediction/fc63d842-c67e-4ae1-93f9-ea99d4a3781e"))
                    .header("Authorization", "Bearer hud4rrmHRbUCP31J7RvpOVApYUpKdlnDSj+kaZAFvco=")
                    .header("Content-Type", "application/json")
                    .POST(BodyPublishers.ofString("{\"question\":\"" + question + "\"}"))
                    .build();

            httpClient.sendAsync(request, BodyHandlers.ofString())
                .thenAccept(response -> {
                    System.out.println("Respuesta recibida: " + response.body());
                    sendResponseToVisualizationAgent(response.body(), senderName);
                })
                .exceptionally(e -> {
                    e.printStackTrace();
                    return null;
                });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void sendResponseToVisualizationAgent(String response, String senderName) {
        ACLMessage reply = new ACLMessage(ACLMessage.INFORM);
        reply.addReceiver(new jade.core.AID(senderName, jade.core.AID.ISLOCALNAME));
        reply.setContent(response);
        send(reply);
    }

    @Override
    protected void takeDown() {
        System.out.println("Agente " + getLocalName() + " terminando.");
    }
}

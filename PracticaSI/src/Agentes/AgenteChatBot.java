package Agentes;

import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

public class AgenteChatBot extends Agent {

    @Override
    protected void setup() {
        addBehaviour(new CyclicBehaviour(this) {
            public void action() {
                ACLMessage msg = myAgent.receive(MessageTemplate.MatchPerformative(ACLMessage.REQUEST));
                if (msg != null) {
                	String question = msg.getContent();
                	System.out.println("El AgenteChatBot ha recibido la pregunta: " + question);
                    enviarRespuestaAVisualizacion("Te estoy vacilando", msg.getSender().getLocalName());
                } else {
                    block();
                }
            }
        });
    }

    private void handleRequest(String question, String senderName) {
        try (CloseableHttpClient client = HttpClients.createDefault()) {
            HttpPost httpPost = new HttpPost("http://localhost:3000/api/v1/prediction/fc63d842-c67e-4ae1-93f9-ea99d4a3781e");
            httpPost.setHeader("Authorization", "Bearer hud4rrmHRbUCP31J7RvpOVApYUpKdlnDSj+kaZAFvco=");
            httpPost.setHeader("Content-Type", "application/json");
            httpPost.setEntity(new StringEntity("{\"question\":\"" + question + "\"}"));

            HttpResponse response = client.execute(httpPost);
            String responseBody = EntityUtils.toString(response.getEntity());

            enviarRespuestaAVisualizacion(responseBody, senderName);
        } catch (Exception e) {
            System.out.println("Error al procesar la solicitud: " + e.getMessage());
            enviarRespuestaAVisualizacion("Error en la solicitud al servidor del chatbot.", senderName);
        }
    }

    private void enviarRespuestaAVisualizacion(String response, String senderName) {
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

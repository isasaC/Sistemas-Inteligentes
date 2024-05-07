package Agentes;

import jade.core.Agent;
import jade.lang.acl.ACLMessage;

import javax.swing.*;

public class AgenteVisualizacion extends Agent {
    private JFrame frame;
    private JTextField textField;
    private JTextArea responseArea;

    @Override
    protected void setup() {
        frame = new JFrame(getLocalName());
        frame.setSize(300, 200);
        frame.setLayout(new BoxLayout(frame.getContentPane(), BoxLayout.Y_AXIS));
        frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE); // Cambiado para gestionar el cierre manualmente.

        textField = new JTextField(20);
        JButton sendButton = new JButton("Consultar");
        responseArea = new JTextArea(5, 20);
        responseArea.setEditable(false);

        frame.add(textField);
        frame.add(sendButton);
        frame.add(new JScrollPane(responseArea));
        frame.setVisible(true);

        sendButton.addActionListener(e -> {
            String text = textField.getText();
            sendRequestToProcessingAgent(text);
        });
    }

    private void sendRequestToProcessingAgent(String text) {
        ACLMessage msg = new ACLMessage(ACLMessage.REQUEST);
        msg.addReceiver(new jade.core.AID("procesamiento", jade.core.AID.ISLOCALNAME));
        msg.setContent(text);
        send(msg);
    }

    @Override
    protected void takeDown() {
        if (frame != null) {
            frame.dispose(); // Asegurar que la ventana se cierre
        }
        System.out.println("Agente " + getLocalName() + " terminando.");
    }
}

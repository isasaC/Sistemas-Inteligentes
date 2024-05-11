package Agentes;

import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

import javax.swing.*;
import java.awt.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;

public class AgenteVisualizacion extends Agent {
    private JFrame frame;
    private JTextField textField;
    private JTextArea responseArea;
    private JPanel mainPanel;
    private  JButton dbRequestButton;

    @Override
    protected void setup() {
        prepareGUI();
        
        addBehaviour(new CyclicBehaviour(this) {
        	
            @Override
            public void action() {
                ACLMessage msg = receive(MessageTemplate.MatchPerformative(ACLMessage.INFORM));
                if (msg != null) {
                    String content = msg.getContent();
                    System.out.println("Respuesta recibida: " + content);
                    SwingUtilities.invokeLater(() -> {
                        parseAndShowResponse(content);
                    });
                		                 		
                } else {
                	System.out.println("AgenteVisualizacion no recibió correctamente un  mensaje");
                    block();
                }
            }
        });
    }

    private void prepareGUI() {
        frame = new JFrame(getLocalName());
        frame.setSize(500, 275);  // Tamaño ajustado para compactar la interfaz
        frame.setLayout(new BorderLayout());
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Panel principal con mejor espaciado
        mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        mainPanel.setBackground(new Color(50, 50, 50)); // Fondo oscuro

        // Configuración de la fuente
        Font textFieldFont = new Font("Sans Serif", Font.PLAIN, 14);

        // Panel para el campo de texto y el botón Consultar
        JPanel inputPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 5));
        inputPanel.setBackground(new Color(50, 50, 50));  // Fondo del panel oscuro

        textField = new JTextField(20);  // Tamaño ajustado
        textField.setFont(textFieldFont);
        textField.setBorder(BorderFactory.createLineBorder(new Color(80, 80, 80), 1));
        textField.setForeground(new Color(220, 220, 220)); // Texto claro
        textField.setBackground(new Color(70, 70, 70));   // Fondo del campo de texto oscuro

        // Botón de envío
        JButton sendButton = new JButton("Consultar");
        styleButton(sendButton);
        sendButton.addActionListener(e -> {
            String text = textField.getText();
            sendRequestToProcessingAgent(text);
        });

        inputPanel.add(textField);
        inputPanel.add(sendButton);

        // Área de respuesta
        responseArea = new JTextArea(6, 20);  // Reducción de altura
        responseArea.setEditable(false);
        responseArea.setFont(textFieldFont);
        responseArea.setForeground(new Color(220, 220, 220)); // Texto claro
        responseArea.setBackground(new Color(70, 70, 70));    // Fondo oscuro del área de texto
        responseArea.setBorder(BorderFactory.createLineBorder(new Color(80, 80, 80), 1));
        responseArea.setWrapStyleWord(true);
        responseArea.setLineWrap(true);
        JScrollPane scrollPane = new JScrollPane(responseArea);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.setBackground(new Color(50, 50, 50));  // Fondo oscuro

        // Botón de solicitud a la BBDD
        dbRequestButton = new JButton("Solicitar a la BBDD");
        styleButton(dbRequestButton);
        dbRequestButton.setVisible(false); // Inicialmente no visible
        dbRequestButton.addActionListener(e -> {
            enviarSQLBBDD(responseArea.getText());
        });

        // Añadir componentes al panel principal
        mainPanel.add(inputPanel);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 10))); // Espaciador
        mainPanel.add(scrollPane);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 5))); // Espaciador
        mainPanel.add(dbRequestButton); // Ubicación ajustada debajo de la caja de texto

        // Añadir panel principal al marco
        frame.add(mainPanel, BorderLayout.CENTER);
        frame.setVisible(true);
    }

    private void styleButton(JButton button) {
        button.setFont(new Font("Sans Serif", Font.BOLD, 12));
        button.setBackground(new Color(100, 100, 100));  // Color de fondo oscuro
        button.setForeground(new Color(220, 220, 220)); // Texto claro
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder(5, 15, 5, 15));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }




    private void sendRequestToProcessingAgent(String text) {
        ACLMessage msg = new ACLMessage(ACLMessage.REQUEST);
        msg.addReceiver(new jade.core.AID("AgenteChatBot", jade.core.AID.ISLOCALNAME));
        msg.setContent(text);
        send(msg);
        //doWait();
    }

    private void parseAndShowResponse(String json) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode rootNode = mapper.readTree(json);
            String text = rootNode.path("text").asText();
            responseArea.setText(text);
            SwingUtilities.invokeLater(() -> {
                dbRequestButton.setVisible(true);
            });
        } catch (Exception e) {
            responseArea.setText("Error al procesar JSON: " + e.getMessage());
            System.out.println("Error al procesar JSON: " + e.getMessage());
        }
    }
    
    private void enviarSQLBBDD(String text) {
        ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
        msg.addReceiver(new jade.core.AID("AgenteBBDD", jade.core.AID.ISLOCALNAME));
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

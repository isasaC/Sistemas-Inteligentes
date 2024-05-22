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
    private JProgressBar progressBar;
    
    String sentenciaSQL;

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
                        progressBar.setVisible(false);
                    });
                		                 		
                } else {
                    block();
                }
            }
        });
    }

    private void prepareGUI() {
        frame = new JFrame(getLocalName());
        frame.setSize(650, 358);  // Tamaño ajustado para compactar la interfaz
        frame.setLayout(new BorderLayout());
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Panel principal con mejor espaciado
        mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        mainPanel.setBackground(new Color(50, 50, 50)); // Fondo oscuro
        
     // JProgressBar configuración
        progressBar = new JProgressBar();
        progressBar.setIndeterminate(true);
        progressBar.setVisible(false); // Inicialmente oculto
        progressBar.setPreferredSize(new Dimension(300, 20));

        // Configuración de la fuente
        Font textFieldFont = new Font("Sans Serif", Font.PLAIN, 14);

        // Panel para el campo de texto y el botón Consultar
        JPanel inputPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 5));
        inputPanel.setBackground(new Color(50, 50, 50));  // Fondo del panel oscuro

        textField = new JTextField(45);  // Inicializa el JTextField con espacio para 32 caracteres
        textField.setFont(new Font("Sans Serif", Font.PLAIN, 14));
        textField.setBorder(BorderFactory.createLineBorder(new Color(80, 80, 80), 1));
        textField.setForeground(new Color(220, 220, 220)); // Color del texto
        textField.setBackground(new Color(70, 70, 70));   // Color de fondo
        textField.setMaximumSize(new Dimension(Integer.MAX_VALUE, textField.getPreferredSize().height));

        // Configuración del layout y adición al panel
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;  // Importante para permitir expansión horizontal
        inputPanel.add(textField, gbc);

        // Botón de envío
        JButton sendButton = new JButton("Query");
        styleButton(sendButton);
        sendButton.addActionListener(e -> {
            String text = textField.getText();
            sendRequestToChatBotAgent(text);
            SwingUtilities.invokeLater(() -> progressBar.setVisible(true));
        });

        inputPanel.add(textField);
        inputPanel.add(sendButton);

        // Área de respuesta
        responseArea = new JTextArea(12, 9);  // Reducción de altura
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
            enviarSQLBBDD(sentenciaSQL);
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




    private void sendRequestToChatBotAgent(String text) {
        ACLMessage msg = new ACLMessage(ACLMessage.REQUEST);
        msg.addReceiver(new jade.core.AID("AgenteChatBot", jade.core.AID.ISLOCALNAME));
        msg.setContent(text);
        send(msg);
        //doWait();
    }

    private void parseAndShowResponse3(String json) {
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
    
    private void parseAndShowResponse(String json) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode rootNode = mapper.readTree(json);

            // Variables para almacenar los valores extraídos
            String explicacionSentencia = null;
            StringBuilder responseText = new StringBuilder();

            // Comprobar si el JSON contiene el objeto "json" con los atributos "sentenciaSQL" y "explicacionSentencia"
            if (rootNode.has("json")) {
                JsonNode jsonNode = rootNode.path("json");
                if (jsonNode.has("sentenciaSQL") && jsonNode.has("explicacionSentencia")) {
                    sentenciaSQL = jsonNode.path("sentenciaSQL").asText();
                    explicacionSentencia = jsonNode.path("explicacionSentencia").asText();
                    responseText.append("Sentencia SQL: ").append(sentenciaSQL).append("\n\n");                    
                    responseText.append("Explicación: ").append(explicacionSentencia);
                 // Hacer visible el botón dbRequestButton
                    SwingUtilities.invokeLater(() -> {
                        dbRequestButton.setVisible(true);
                    });
                }
            } else if (rootNode.has("text")) {
                explicacionSentencia = rootNode.path("text").asText();
                responseText.append(explicacionSentencia);
                SwingUtilities.invokeLater(() -> {
                    dbRequestButton.setVisible(false);
                });
            }

            // Mostrar el texto en responseArea
            responseArea.setText(responseText.toString());
            
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

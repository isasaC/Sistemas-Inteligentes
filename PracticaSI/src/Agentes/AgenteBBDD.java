package Agentes;

import java.sql.Connection;

import javax.swing.JFrame;
import javax.swing.JTextArea;
import javax.swing.JScrollPane;
import javax.swing.JPanel;
import javax.swing.BorderFactory;
import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.Color;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;

import Connection.DatabaseConnection;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;

public class AgenteBBDD extends Agent {
	
	DatabaseConnection dbCon = new DatabaseConnection();
	String sentenciaSQL;
	ACLMessage msg;

	String resultBBDD = "Nombre	
			Nippon Grill	
			Nippon Grill	
			Nippon Grill	
			Marisquería del Norte	
			Trattoria Da Luigi	
			Tokyo Ramen	
			El Asador Castellano	
			Antojitos Juanita	
			Gran Muralla	
			Dolce Vita	
			Taberna del Gourmet	
			Marisquería del Norte	
			Fiesta Mexicana	
			Marisquería del Norte	
			Trattoria Da Luigi	
			Trattoria Da Luigi	
			Palacio Pekín	
			Tokyo Ramen	
			Trattoria Da Luigi	
			Trattoria Da Luigi"
	
    @Override
    protected void setup() {
        addBehaviour(new CyclicBehaviour(this) {
            @Override
            public void action() {
                msg = receive();
                if (msg != null) {
                	sentenciaSQL = msg.getContent();
                    System.out.println("El AgenteBBDD ha recibido la sentencia: " + msg.getContent());
                    mostrarResultadosEnVentana(resultBBDD);
                } else {
                    block();
                }
            }
        });
    }
    
    public void enviarSentenciaBBDD(String sentencia) {
    	try {
    		Connection connection = dbCon.getConnection();
    		Statement statement = connection.createStatement();
    		ResultSet resultSet = statement.executeQuery(sentencia);
    		
    		String resultString = resultSetToString(resultSet);
    		mostrarResultadosEnVentana(resultString);
    		System.out.println(resultString);
    		enviarRespuestaAVisualizacion(resultString, msg.getSender().getLocalName());  		
    	} catch (SQLException e) {
    		System.out.println("ERROR: no se pudo procesar la sentencia");
    	}
    }
    
    private void mostrarResultadosEnVentana(String resultString) {
        JFrame frame = new JFrame("Resultados de la Consulta");
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setSize(600, 500);  // Ajusta el tamaño del JFrame
        frame.setLocationRelativeTo(null);  // Centra la ventana en la pantalla

        // Crea un JTextArea para mostrar los resultados
        JTextArea textArea = new JTextArea(20, 50);
        textArea.setText(resultString);
        textArea.setEditable(false);
        textArea.setFont(new Font("SansSerif", Font.PLAIN, 14));  // Configura la fuente del texto
        textArea.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));  // Añade un borde vacío para el relleno

        // Establece un color de fondo y de primer plano para el textArea
        textArea.setBackground(new Color(233, 236, 239));  // Un gris claro
        textArea.setForeground(new Color(33, 37, 41));  // Casi negro

        // Añade un JScrollPane para permitir el desplazamiento vertical y horizontal
        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createEmptyBorder(10, 10, 10, 10),
            BorderFactory.createLineBorder(new Color(204, 209, 217), 1)
        ));

        // Añade el JScrollPane al JFrame
        frame.getContentPane().add(scrollPane, BorderLayout.CENTER);

        // Hace visible el JFrame
        frame.setVisible(true);
    }

    
    public String resultSetToString(ResultSet rs) throws SQLException {
        StringBuilder sb = new StringBuilder();
        ResultSetMetaData metaData = rs.getMetaData();
        int columnCount = metaData.getColumnCount();

        // Encabezados de columna
        for (int i = 1; i <= columnCount; i++) {
            sb.append(metaData.getColumnName(i)).append("\t");
        }
        sb.append("\n");

        // Datos de las filas
        while (rs.next()) {
            for (int i = 1; i <= columnCount; i++) {
                sb.append(rs.getString(i)).append("\t");
            }
            sb.append("\n");
        }
        return sb.toString();
    }
    
    private void enviarRespuestaAVisualizacion(String text, String sender) {
        ACLMessage msg = new ACLMessage(ACLMessage.REQUEST);
        msg.addReceiver(new jade.core.AID(sender, jade.core.AID.ISLOCALNAME));
        msg.setContent(text);
        System.out.println("Agente BBDD envia la respuesta final a AgenteVisualizacion");
        send(msg);
    }
    

    
    
    
    @Override
    protected void takeDown() {
        System.out.println("Agente " + getLocalName() + " terminando.");
    }
}

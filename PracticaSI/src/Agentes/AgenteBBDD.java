package Agentes;

import java.sql.Connection;

import javax.swing.JFrame;
import javax.swing.JTextArea;



import javax.swing.JScrollPane;
import javax.swing.JPanel;
import javax.swing.BorderFactory;
import java.awt.BorderLayout;
import java.awt.Font;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
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
	
    @Override
    protected void setup() {
        addBehaviour(new CyclicBehaviour(this) {
            @Override
            public void action() {
                msg = receive();
                if (msg != null) {
                	sentenciaSQL = msg.getContent();
                    System.out.println("El AgenteBBDD ha recibido la sentencia: " + msg.getContent());
                    enviarSentenciaBBDD(sentenciaSQL);
                } else {
                    block();
                }
            }
        });
    }
    
    public void enviarSentenciaBBDD(String sentencia) {
    	try {
    		Connection connection = dbCon.getConnection();
    		Statement statement = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
    		
    		ResultSet resultSet = statement.executeQuery(sentencia);
    		String nextFileName = getNextFileName("results");
            guardarResultSetEnCSV(resultSet, "results/" + nextFileName);
    		
            resultSet = statement.executeQuery(sentencia);
            String resultString = resultSetToString(resultSet);
    		mostrarResultadosEnVentana(resultString);
    	} catch (SQLException e) {
    		System.out.println("ERROR: no se pudo procesar la sentencia");
    	}
    }
    
    private void mostrarResultadosEnVentana(String resultString) {
        JFrame frame = new JFrame("Resultados de la Consulta");
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setSize(800, 600);  // Ajusta el tamaño del JFrame
        frame.setLocationRelativeTo(null);  // Centra la ventana en la pantalla

        // Crea un JTextArea para mostrar los resultados
        JTextArea textArea = new JTextArea(20, 50);
        textArea.setText(resultString);
        textArea.setEditable(false);
        textArea.setFont(new Font("Monospaced", Font.PLAIN, 14));  // Usa una fuente monoespaciada
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

        // Calcular la longitud máxima de los datos en cada columna
        int[] maxColumnWidths = new int[columnCount];
        for (int i = 1; i <= columnCount; i++) {
            maxColumnWidths[i - 1] = metaData.getColumnName(i).length();
        }

        // Scroll through ResultSet to determine max widths
        rs.beforeFirst(); // Posiciona el cursor antes de la primera fila para iterar de nuevo si es necesario
        while (rs.next()) {
            for (int i = 1; i <= columnCount; i++) {
                String columnValue = rs.getString(i);
                if (columnValue != null) {
                    maxColumnWidths[i - 1] = Math.max(maxColumnWidths[i - 1], columnValue.length());
                }
            }
        }

        // Encabezados de columna
        for (int i = 1; i <= columnCount; i++) {
            String header = metaData.getColumnName(i);
            sb.append(String.format("%-" + (maxColumnWidths[i - 1] + 2) + "s", header));
        }
        sb.append("\n");

        // Línea de separación
        int totalWidth = 0;
        for (int i = 1; i <= columnCount; i++) {
            int columnWidth = maxColumnWidths[i - 1] + 2;
            totalWidth += columnWidth;
            sb.append("-".repeat(columnWidth));
        }
        sb.append("\n");

        // Datos de las filas
        rs.beforeFirst(); // Posiciona de nuevo antes de la primera fila
        while (rs.next()) {
            for (int i = 1; i <= columnCount; i++) {
                String fieldValue = rs.getString(i) == null ? "" : rs.getString(i);
                sb.append(String.format("%-" + (maxColumnWidths[i - 1] + 2) + "s", fieldValue));
            }
            sb.append("\n");
        }

        // Añadir línea de separación final
        sb.append("-".repeat(totalWidth));
        sb.append("\n");

        return sb.toString();
    }


    
 // Método para guardar ResultSet en CSV
    private void guardarResultSetEnCSV(ResultSet resultSet, String filePath) throws SQLException {
        try {
            File file = new File(filePath);
            file.getParentFile().mkdirs(); // Crea el directorio si no existe
            FileWriter csvWriter = new FileWriter(file);
            
            int columnCount = resultSet.getMetaData().getColumnCount();
            // Escribir nombres de columnas como encabezados del CSV
            for (int i = 1; i <= columnCount; i++) {
                csvWriter.append(resultSet.getMetaData().getColumnName(i));
                if (i < columnCount) csvWriter.append(",");
            }
            csvWriter.append("\n");
            
            // Escribir datos del ResultSet
            while (resultSet.next()) {
                for (int i = 1; i <= columnCount; i++) {
                    csvWriter.append(resultSet.getString(i));
                    if (i < columnCount) csvWriter.append(",");
                }
                csvWriter.append("\n");
            }
            
            csvWriter.flush();
            csvWriter.close();
        } catch (IOException e) {
            System.out.println("ERROR: no se pudo escribir el archivo CSV");
        }
    }
    
    private String getNextFileName(String directoryPath) {
        File directory = new File(directoryPath);
        File[] files = directory.listFiles();
        int maxNumber = 0;
        if (files != null) {
            for (File file : files) {
                String name = file.getName();
                if (name.matches("resultado(\\d+)\\.csv")) {
                    int number = Integer.parseInt(name.substring(9, name.length() - 4));
                    if (number > maxNumber) {
                        maxNumber = number;
                    }
                }
            }
        }
        return "resultado" + (maxNumber + 1) + ".csv";
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

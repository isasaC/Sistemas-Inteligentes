package Main;


import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.core.Runtime;
import jade.wrapper.AgentContainer;
import jade.wrapper.AgentController;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import Agentes.AgenteVisualizacion;
import Agentes.AgenteBBDD;
import Agentes.AgenteChatBot;
import Agentes.AgenteVisualizacion;

import java.awt.EventQueue;
import java.net.Socket;
import java.net.ServerSocket;


public class Main {

    public static void main(String[] args) {
        // Inicializar el entorno de Jade
        Runtime rt = Runtime.instance();
        Profile profile = new ProfileImpl();
        
        profile.setParameter(Profile.MAIN_HOST, "localhost");
        profile.setParameter(Profile.MAIN_PORT, "1414");
        profile.setParameter(Profile.GUI, "true");
        AgentContainer container = rt.createMainContainer(profile);

        
        try { 
            // Crear el AgentePercepcion
            AgentController agenteBBDD = container.createNewAgent("AgenteBBDD", AgenteBBDD.class.getName(), null);
            agenteBBDD.start();

         // Crear el AgenteProcesamiento
            AgentController agenteChatBot = container.createNewAgent("AgenteChatBot", AgenteChatBot.class.getName(), null);
            agenteChatBot.start();
            
            // Crear el AgenteVisualizacion
            AgentController agenteVisualizacion = container.createNewAgent("AgenteVisualizacion", AgenteVisualizacion.class.getName(), null);
            agenteVisualizacion.start();
            
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

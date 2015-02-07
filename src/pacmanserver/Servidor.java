/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pacmanserver;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

/**
 *
 * @author Diego
 */
public class Servidor extends Thread {

    public static PacListLobby listaSalas;
    public static ArrayList<ClienteServidor> clientes;
    
    private final ServerSocket listener;
    private final int MAX_COLA = 200;
    
    public Servidor(int puerto) throws IOException
    {
        listener = new ServerSocket(puerto, MAX_COLA, InetAddress.getLocalHost());
        clientes = new ArrayList<>();
        listaSalas = new PacListLobby();
    }

    @Override
    public void run()
    {
        
        while (true)
        {
            try
            {
                System.out.println("Esperando conexiones...");
                Socket socket = listener.accept();
                System.out.println("Conexion entrante desde: " + socket.getInetAddress());
                ClienteServidor client = new ClienteServidor(socket);
                new Thread(client).start();
            }
            catch (IOException e)
            {
                System.err.println("Error: " + e.getMessage());
                break;
            }
        }
    }
}
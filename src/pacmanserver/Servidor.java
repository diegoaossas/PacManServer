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

    public static int MAX_COLA = 4;
    protected static ArrayList<Cliente> clientes;
    
    private ServerSocket listener;
    private int puerto;
    

    public Servidor(int puerto) throws IOException
    {
        this.puerto = puerto;
        listener = new ServerSocket(puerto, MAX_COLA, InetAddress.getLocalHost());
        clientes = new ArrayList<Cliente>();
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
                System.out.println("Conexion entrante desde: "+ socket.getInetAddress());
                Cliente client = new Cliente(socket, this);
                new Thread(client).start();
            }
            catch (IOException e)
            {
                break;
            }
        }
    }
}
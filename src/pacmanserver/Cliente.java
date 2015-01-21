/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pacmanserver;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import static pacmanserver.Servidor.clientes;


/**
 *
 * @author Diego
 */
public class Cliente implements Runnable {

    private Socket socket;
    private Servidor server;
    private BufferedReader in;
    private PrintWriter out;
    
    String action;
    String usuario;
    String clave;
    boolean success = false;

    public Cliente(Socket sock, Servidor serv) {
        socket = sock;
        server = serv;
        in = null;
        out = null;
    }

    @Override
    public void run() {
        try
        {
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);

            while (true)
            {
                readAction();
            }
        } catch (IOException e)
        {
            System.out.println("Error:"+e.getMessage());
        }
        finally
        {
            Servidor.clientes.remove(this);
        }
    }
    
    public void read() throws IOException
    {
        String leido = in.readLine();
        
        if(leido == null)
            throw new IOException();
        
        System.out.println("Recibido:" + leido);
    }
    
    private void readAction() throws IOException
    {
        action = in.readLine();
        System.out.println("Action:"+action);

        if(action.equals("login"))
        {
            usuario = in.readLine();
            System.out.println("Usuario:"+usuario);
            clave = in.readLine();
            System.out.println("Clave:"+clave);
            success = false;
            if(usuario.equals("diego") && clave.equals("diego013"))
            {
                Servidor.clientes.add(this);
                System.out.println("Logeado:"+success);
                System.out.println("Cliente conectado: " + clientes.size());
                success = true;
            }
            out.println(success);

            for(Cliente cl:Servidor.clientes)
            {
                cl.send("hola");
                System.out.println("Enviando a:" + cl.usuario);
            }
        }
        else
            read();
    }

    public void send(String data) {
        out.println(data);
    }

}
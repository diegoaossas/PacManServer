/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pacmanserver;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import static pacmanserver.Servidor.clientes;


/**
 *
 * @author Diego
 */
public class Cliente implements Runnable {

    private final Socket socket;
    private final Servidor server;
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
        System.out.println("");
        System.out.println("Accion: " + action);

        if(action == null)
            throw new IOException("Socket Cerrado(?)");
        
        if(action.equals("login"))
        {
            usuario = in.readLine();
            System.out.println("Usuario: " + usuario);
            clave = in.readLine();
            System.out.println("Clave: " + clave);
            success = false;
            if(usuario.equals("a") && clave.equals("a"))
            {
                Servidor.clientes.add(this);
                success = true;
            }
            out.println(success);
            
            Usuario usu = new Usuario();
            usu.ID = 2;
            usu.Usuario = "diegoaossas";
            usu.Nickname = "Azzshifto";
            usu.pGanadas = 231;
            usu.pJugadas = 321;
            usu.pPerdidas = 21;
            
            if(success){
                ObjectOutputStream ooS = new ObjectOutputStream(socket.getOutputStream());
                ooS.writeObject(usu);
            }
            
            System.out.println("Acceso: " + success);
            
            System.out.println("Cliente conectado: " + clientes.size());
        }
        else
        {
            read();
        }
    }

    public void send(String data) {
        out.println(data);
    }

}
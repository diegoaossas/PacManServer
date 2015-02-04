/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pacmanserver;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;


/**
 *
 * @author Diego
 */
public class Cliente implements Runnable {

    private final Socket socket;
    private ObjectInputStream in;
    private ObjectOutputStream out;

    boolean success = false;

    public Cliente(Socket sock)
    {
        socket = sock;
        in = null;
        out = null;
    }

    @Override
    public void run() {
        try
        {
            out = new ObjectOutputStream(socket.getOutputStream());
            in = new ObjectInputStream(socket.getInputStream());

            while (true)
            {
                procesaData();
            }
        }
        catch (IOException | ClassNotFoundException e)
        {
            System.out.println("Error: " + e.getMessage());
        }
        finally
        {
            Servidor.clientes.remove(this);
        }
    }
    
    public void procesaData() throws IOException, ClassNotFoundException
    {
        Actions action = (Actions)in.readObject();
        
        if(action == Actions.LOGIN)
        {
            success = false;
            System.out.println("Solicitud de login recibida.");
            
            Credenciales cred = (Credenciales)in.readObject();
            System.out.println("Credenciales recibidas.");
            
            if(cred.usuario.equals("a") && cred.clave.equals("a"))
                success = true;
            
            out.writeObject(success);
            System.out.println("Resultado de login enviado -> " + success);
            
            if(success)
            {
                //Enviar usuario de prueba
                Usuario usu = new Usuario();
                usu.ID = 1;
                usu.Usuario = "diegoaossas";
                usu.pGanadas = 2323;
                usu.pJugadas = 6923;
                usu.pPerdidas = 0;
                
                out.writeObject(usu);
                System.out.println("Login completo, usuario enviado -> " + usu.Usuario);
            }
            else
                socket.close();
        }
    }
}
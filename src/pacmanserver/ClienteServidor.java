/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pacmanserver;

import Libreria.Actions;
import Libreria.Credenciales;
import Libreria.Sala;
import Libreria.Usuario;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 *
 * @author Diego
 */
public class ClienteServidor implements Runnable{

    private final Socket socket;
    private ObjectInputStream in;
    private ObjectOutputStream out;
    private Thread lobbyStream;
    
    public Usuario usuarioLog;
    private boolean success;

    public ClienteServidor(Socket sock)
    {
        this.success = false;
        this.socket = sock;
        this.in = null;
        this.out = null;
        this.usuarioLog = null;
    }

    @Override
    public void run() {
        try
        {
            this.out = new ObjectOutputStream(socket.getOutputStream());
            this.in = new ObjectInputStream(socket.getInputStream());
            Servidor.clientes.add(this);
            
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
                
                usuarioLog = usu;
                out.writeObject(usuarioLog);
                System.out.println("Login completo, usuario enviado -> " + usu.Usuario);
            }
            else
                socket.close();
        }
        
        if(action == Actions.NEWLOBBY)
        {
            System.out.println("Solicitud NEWLOBBY.");
            if(usuarioLog == null)
                return;
            
            Servidor.listaSalas.CrearSala("nombre", this);
        }   
        
        if(action == Actions.GETLOBBYS)
        {
            System.out.println("Solicitud GETLOBBYS.");
            ArrayList<Sala> salas = Servidor.listaSalas.getSalas();
            
            out.writeObject(salas);
        }       
        
        if(action == Actions.GETLOBBYSstream)
        {
            System.out.println("Solicitud GETLOBBYSstream.");
            
            lobbyStream = new Thread(() ->{
                while(true)
                {
                    try {                               
                        out.writeObject(Servidor.listaSalas.getSalas());       
                        
                        try
                        {
                            Thread.sleep(1000);
                        }
                        catch ( InterruptedException e)
                        {
                            Thread.currentThread().interrupt(); // restore interrupted status
                            break;
                        }
                        
                    } catch (IOException ex) {
                        Logger.getLogger(ClienteServidor.class.getName()).log(Level.SEVERE, null, ex);
                        break;
                    }
                }
            });
            
            lobbyStream.start();
        }        
        
        if(action == Actions.GETLOBBYSstreamStop)
        {
            System.out.println("Solicitud GETLOBBYSstreamStop.");
            this.lobbyStream.interrupt();
            this.lobbyStream = null;
        }
    }
}
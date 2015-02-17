package pacmanserver;

import Libreria.Actions;
import Libreria.Credenciales;
import Libreria.Respuesta;
import Libreria.Sala;
import Libreria.Usuario;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;


public class ClienteServidor implements Runnable{
    private final Socket socket;
    private ObjectInputStream in;
    private ObjectOutputStream out;
    private Thread lobbyStream;
    private Thread salaStream;
    
    private Usuario usuarioLog;
    private boolean fin = false;
    
    public ClienteServidor(Socket sock)
    {
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
                
                if(fin)
                	break;
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
            System.out.println("Solicitud de login recibida.");
            
            Credenciales cred = (Credenciales)in.readObject();
            System.out.println("Credenciales recibidas.");
            
            for(Usuario usu : Servidor.usuariosRegistrados)
            {
            	if(usu.Cuenta.toLowerCase().equals(cred.usuario.toLowerCase()))
            	{
            		if(usu.Clave.equals(cred.clave))
            		{
                        out.writeObject(Respuesta.LOGGED);
                        usuarioLog = usu;
                        out.writeObject(getUsuarioLog());
                        System.out.println("Login completo, usuario enviado -> " + usu.Cuenta);
            		}
            		else
            		{
                        break;
            		}
            	}
            }
            
            if(usuarioLog == null)
            {
                out.writeObject(Respuesta.NOLOGGED);
                socket.close();
            }
        }
        
        if(action == Actions.REGISTRO)
        {
            System.out.println("Solicitud de registro recibida.");
            
            Credenciales cred = (Credenciales)in.readObject();
            System.out.println("Credenciales recibidas.");
            boolean existente = false;
            
            for(Usuario usu : Servidor.usuariosRegistrados)
            {
            	if(usu.Cuenta.toLowerCase().equals(cred.usuario.toLowerCase()))
            	{
                    existente = true;
                    break;
            	}
            }
            
            if(!existente)
            {
            	Usuario usu = new Usuario();
            	usu.ID = Servidor.usuariosRegistrados.size() + 1;
                usu.Cuenta = cred.usuario;
                usu.Clave = cred.clave;
                usu.Nombre = cred.usuario;
                usuarioLog = usu;
                Servidor.usuariosRegistrados.add(usu);
                
                out.writeObject(Respuesta.REGISTRADO);
                out.writeObject(getUsuarioLog());
                System.out.println("Registro completo, usuario enviado -> " + usu.Cuenta);
            }
            else
            {
                System.out.println("Registro fallido, usuario existente");
                out.writeObject(Respuesta.NOREGISTRADO);
                socket.close();
            }

        }
        
        if(action == Actions.NEWLOBBY)
        {
            System.out.println("Solicitud NEWLOBBY.");
            Sala sala = (Sala) in.readObject();
            
            long creado = 0;
            
            if(getUsuarioLog() != null)
                creado = Servidor.listaSalas.CrearSala(sala, this);
            
            out.writeObject(creado);
        }      
        
        if(action == Actions.GETLOBBYSstream)
        {
            System.out.println("Solicitud GETLOBBYSstream.");
            
            lobbyStream = new Thread(() ->{
                while(true)
                {
                    try {
                        ArrayList<Sala> salas = Servidor.listaSalas.getSalas();
                        out.reset();
                        out.writeObject(salas);
                        
                        for(Sala sala : salas)
                        {
                            System.out.println("ClienteServidor::GETLOBBYSstream -> Sala enviada " + sala.nombreSala + " con " + sala.jugadoresEnSala + " de " + sala.maxjugadores);   
                        }
                        
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
        
        if(action == Actions.GETSALAstream)
        {
            System.out.println("Solicitud GETSALAstream.");
            long id = (long)in.readObject();
            
            salaStream = new Thread(() ->{
                while(true)
                {
                    try {
                        Sala sala = Servidor.listaSalas.getSala(id);
                        out.reset();
                        out.writeObject(sala);
                        
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
                        SalaServidor salaServ = Servidor.listaSalas.getSalaServidor(id);
                        salaServ.QuitarJugador(this);
                        break;
                    }
                }
            });
            
            salaStream.start();
        }        
        
        if(action == Actions.GETSALAstreamStop)
        {
            System.out.println("Solicitud GETSALAstreamStop.");
            this.salaStream.interrupt();
            this.salaStream = null;
        }      
        
        if(action == Actions.GETLOBBYSstreamStop)
        {
            System.out.println("Solicitud GETLOBBYSstreamStop.");
            this.lobbyStream.interrupt();
            this.lobbyStream = null;
        } 
        
        if(action == Actions.JoinSALA)
        {
            System.out.println("Solicitud JoinSALA.");
            long id = (long)in.readObject();
            SalaServidor salaServ = Servidor.listaSalas.getSalaServidor(id);
            Sala sala = salaServ.pacLobby;
            
            if(sala.jugadoresEnSala < sala.maxjugadores)
            {
                salaServ.AgregaJugador(this);
                out.writeObject(true);
                System.out.println("jugador agrergado.");
            }
            else
            {
                out.writeObject(false);
                System.out.println("jugador no agrergado.");
            }
        }  

        if(action == Actions.LeaveSALA)
        {
            System.out.println("Solicitud LeaveSALA.");
            long id = (long)in.readObject();
            SalaServidor salaServ = Servidor.listaSalas.getSalaServidor(id);
            
            salaServ.QuitarJugador(this);
        }
        
        if(action == Actions.DESCONECTAR)
        {
            System.out.println("Solicitud DESCONECTAR.");
            fin	= true;
        }
    }

    /**
     * @return the usuarioLog
     */
    public Usuario getUsuarioLog()
    {
        return usuarioLog;
    }
}
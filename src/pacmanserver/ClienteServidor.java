package pacmanserver;

import Libreria.Actions;
import Libreria.Credenciales;
import Libreria.Mapa;
import Libreria.Pacman;
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

public class ClienteServidor implements Runnable
{

    private final Socket socket;
    private ObjectInputStream in;
    private ObjectOutputStream out;
    
    private Thread lobbyStream;
    private boolean intLobby = false;
    private Thread salaStream;
    private boolean intSala = false;
    private Thread pacmanStream;
    private boolean intPacman = false;
    private Thread juegoStream;
    private boolean intJuego = false;

    private Usuario usuarioLog;
    private boolean fin = false;

    public ClienteServidor(Socket sock)
    {
        this.socket = sock;
        this.in = null;
        this.out = null;
        this.usuarioLog = null;
    }

    /**
     * @return the usuarioLog
     */
    public Usuario getUsuarioLog()
    {
        return usuarioLog;
    }

    public void procesaData() throws ClassNotFoundException, IOException
    {
        Actions action = (Actions) in.readObject();

        if (action == Actions.LOGIN)
        {
            Credenciales cred = (Credenciales) in.readObject();

            for (Usuario usu : Servidor.usuariosRegistrados)
            {
                if (usu.Cuenta.toLowerCase().equals(cred.usuario.toLowerCase()))
                {
                    if (usu.Clave.equals(cred.clave))
                    {
                        out.writeObject(Respuesta.LOGGED);
                        usuarioLog = usu;
                        out.writeObject(getUsuarioLog());
                    }
                    else
                    {
                        break;
                    }
                }
            }

            if (usuarioLog == null)
            {
                out.writeObject(Respuesta.NOLOGGED);
                socket.close();
            }
        }

        if (action == Actions.REGISTRO)
        {
            Credenciales cred = (Credenciales) in.readObject();
            boolean existente = false;

            for (Usuario usu : Servidor.usuariosRegistrados)
            {
                if (usu.Cuenta.toLowerCase().equals(cred.usuario.toLowerCase()))
                {
                    existente = true;
                    break;
                }
            }

            if (!existente)
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
            } else
            {
                out.writeObject(Respuesta.NOREGISTRADO);
                socket.close();
            }

        }

        if (action == Actions.NEWLOBBY)
        {
            String nombreSala = (String) in.readObject();

            long creado = 0;

            if (getUsuarioLog() != null)
            {
                creado = Servidor.listaSalas.CrearSala(nombreSala, this);
            }

            out.writeObject(creado);
        }

        if (action == Actions.GETLOBBYSstream)
        {

            lobbyStream = new Thread(() ->
            {
                while (true)
                {
                    try
                    {
                        ArrayList<Sala> salas = Servidor.listaSalas.getSalas();
                        out.writeObject(salas);
                        System.out.println("Envie sala");
                        out.reset();

                        if(intLobby)
                        {
                            System.out.println("SOLIC DETENER");
                            Thread.currentThread().interrupt();
                            break;
                        }
                        
                        Thread.sleep(200);

                    } catch (IOException ex)
                    {
                        System.out.println("ERROR Y SED ETUVO");
                        ex.printStackTrace();
                        break;
                    }
                    catch(InterruptedException e) { }
                }
            });

            lobbyStream.start();
        }

        if (action == Actions.GETSALAstream)
        {
            long id = (long) in.readObject();

            salaStream = new Thread(() ->
            {
                while (true)
                {
                    try
                    {
                        Sala sala = Servidor.listaSalas.getSala(id);
                        out.writeObject(sala);
                        out.reset();
                        
                        if(intSala)
                        {
                            System.out.println("SOLIC DETENER");
                            Thread.currentThread().interrupt();
                            break;
                        }
                        
                        Thread.sleep(200);

                    } catch (IOException ex)
                    {
                        ex.printStackTrace();

                        SalaServidor salaServ = Servidor.listaSalas.getSalaServidor(id);
                        salaServ.QuitarJugador(this);
                        Servidor.listaSalas.verificarValidez(salaServ);
                        break;
                    }
                    catch(InterruptedException e) { }
                }
            });

            salaStream.start();
        }

        if (action == Actions.GETJUEGOstream)
        {
            long id = (long) in.readObject();

            juegoStream = new Thread(() ->
            {
                while (true)
                {
                    try
                    {
                        SalaServidor salaServ = Servidor.listaSalas.getSalaServidor(id);
                        Sala sala = salaServ.pacLobby;
                        out.writeObject(sala);
                        out.reset();
                        
                        if (sala == null)
                        {
                            continue;
                        }

                        Pacman elMio = getUsuarioLog().paco;
                        out.writeObject(elMio);

                        for (int i = 0; i < 4; i++)
                        {
                            Pacman pacman = null;
                            try
                            {
                                pacman = sala.jugadores.get(i).paco;
                                if (pacman.equals(elMio))
                                    continue;
                            }
                            catch (IndexOutOfBoundsException e) { }

                            out.writeObject(pacman);
                        }
                        
                        if(intJuego)
                        {
                            System.out.println("SOLIC DETENER");
                            Thread.currentThread().interrupt();
                            break;
                        }
                        
                        Thread.sleep(5);

                    } catch (IOException ex)
                    {
                        SalaServidor salaServ = Servidor.listaSalas.getSalaServidor(id);
                        salaServ.QuitarJugador(this);
                        Servidor.listaSalas.verificarValidez(salaServ);
                        break;
                    }
                    catch(InterruptedException e) { }
                }
            });

            juegoStream.start();
        }

        if (action == Actions.PacManSTREAM)
        {
            long id = (long) in.readObject();

            pacmanStream = new Thread(() ->
            {
                while (true)
                {
                    try
                    {
                        SalaServidor salaServ = Servidor.listaSalas.getSalaServidor(id);
                        Pacman elMio = getUsuarioLog().paco;
                        out.writeObject(elMio);
                        out.reset();

                        for (int i = 0; i < 4; i++)
                        {
                            Pacman pacman = null;
                            try
                            {
                                pacman = salaServ.pacLobby.jugadores.get(i).paco;
                                if (pacman.equals(elMio))
                                {
                                    continue;
                                }
                            } catch (Exception e)
                            {
                                e.printStackTrace();
                            }

                            out.writeObject(pacman);
                        }

                        
                        if(intPacman)
                        {
                            System.out.println("SOLIC DETENER");
                            Thread.currentThread().interrupt();
                            break;
                        }
                        
                        Thread.sleep(20);

                    } catch (IOException ex)
                    {
                        ex.printStackTrace();
                        getUsuarioLog().paco = null;
                        break;
                    }
                    catch(InterruptedException e) { }
                }
            });

            pacmanStream.start();
        }

        if (action == Actions.GETSALAstreamStop)
        {
            intSala = true;
            if (salaStream != null)
            {
                while(salaStream.isAlive());
                salaStream = null;
            }
            intSala = false;
            
            out.writeObject(Respuesta.OK);
        }

        if (action == Actions.GETLOBBYSstreamStop)
        {
            intLobby = true;
            if (lobbyStream != null)
            {
                while(lobbyStream.isAlive());
                lobbyStream = null;
            }
            intLobby = false;
            
            out.writeObject(Respuesta.OK);
        }

        if (action == Actions.JoinSALA)
        {
            long id = (long) in.readObject();
            SalaServidor salaServ = Servidor.listaSalas.getSalaServidor(id);
            Sala sala = salaServ.pacLobby;
            out.reset();
            
            if (sala.jugadores.size() < sala.maxjugadores)
            {
                salaServ.AgregaJugador(this);
                out.writeObject(true);
            } else
            {
                out.writeObject(false);
            }
        }

        if (action == Actions.LeaveSALA)
        {
            long id = (long) in.readObject();
            SalaServidor salaServ = Servidor.listaSalas.getSalaServidor(id);
            salaServ.QuitarJugador(this);
            Servidor.listaSalas.verificarValidez(salaServ);
            out.writeObject(Respuesta.OK);
        }

        if (action == Actions.DESCONECTAR)
        {
            fin = true;
        }

        if (action == Actions.GETMAPA)
        {
            //createCellArray("/src/libreria/mapa.txt);
            Mapa mapa = new Mapa();
            //mapa.lineList = cargaMapa();

            out.writeObject(mapa);
        }

        if (action == Actions.ActPACMAN)
        {
            long id = (long) in.readObject();
            SalaServidor salaServ = Servidor.listaSalas.getSalaServidor(id);
            Pacman paco = (Pacman) in.readObject();

            getUsuarioLog().paco = paco;

            if (getUsuarioLog().paco != null)
            {
                char type = salaServ.pacLobby.cellsMapa[getUsuarioLog().paco.pacmanRow][getUsuarioLog().paco.pacmanCol].getType();

                if (type == 'm')
                {
                    salaServ.pacLobby.cellsMapa[getUsuarioLog().paco.pacmanRow][getUsuarioLog().paco.pacmanCol].type = 'v';
                    getUsuarioLog().paco.puntos += 10;
                }
                else if (type == 'n')
                {
                    salaServ.pacLobby.cellsMapa[getUsuarioLog().paco.pacmanRow][getUsuarioLog().paco.pacmanCol].type = 'v';
                    getUsuarioLog().paco.puntos += 50;
                }
            }

        }

        if (action == Actions.PLAYALL)
        {
            long id = (long) in.readObject();
            SalaServidor salaServ = Servidor.listaSalas.getSalaServidor(id);
            salaServ.empezar();

            for (ClienteServidor cliente : salaServ.jugadores)
            {
                if (cliente != this)
                {
                    cliente.out.writeObject(Respuesta.PLAY);
                    if (cliente.salaStream != null)
                    {
                        cliente.intSala = true;
                        while(cliente.salaStream.isAlive());
                        cliente.salaStream = null;
                    }
                    cliente.out.writeObject(Respuesta.OK);
                } else
                {
                    out.writeObject(Respuesta.PLAY);
                    if (salaStream != null)
                    {
                        intSala = true;
                        while(salaStream.isAlive());
                        salaStream = null;
                    }
                    out.writeObject(Respuesta.OK);
                }
            }
        }
    }

    @Override
    public void run()
    {
        try
        {
            this.out = new ObjectOutputStream(socket.getOutputStream());
            this.in = new ObjectInputStream(socket.getInputStream());
            Servidor.clientes.add(this);

            while (true)
            {
                procesaData();

                if (fin)
                {
                    break;
                }
            }
        }
        catch (IOException e) { }
        catch(ClassNotFoundException e)
        {
            e.printStackTrace();
        }
        finally
        {
            Servidor.clientes.remove(this);
        }
    }
}

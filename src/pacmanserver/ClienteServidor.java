package pacmanserver;

import Libreria.Actions;
import Libreria.Credenciales;
import Libreria.Jugadores;
import Libreria.Pacman;
import Libreria.Respuesta;
import Libreria.Sala;
import Libreria.Usuario;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Objects;

public class ClienteServidor implements Runnable
{
    private final Socket socket;
    public ObjectInputStream in;
    public ObjectOutputStream out;

    public Thread lobbyStream = null;
    public boolean intLobby = false;
    public Thread salaStream = null;
    public boolean intSala = false;
    public Thread juegoStream = null;
    public boolean intJuego = false;

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
    public int hashCode()
    {
        int hash = 5;
        hash = 19 * hash + Objects.hashCode(this.usuarioLog);
        
        return hash;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (obj == null)
            return false;
        
        if (getClass() != obj.getClass())
            return false;
        
        final ClienteServidor other = (ClienteServidor) obj;
        
        return Objects.equals(this.getUsuarioLog(), other.getUsuarioLog());
    }

    public Usuario getUsuarioLog()
    {
        return usuarioLog;
    }

    @SuppressWarnings("unchecked")
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
                Servidor.guardaUsuarios();
                
                out.writeObject(Respuesta.REGISTRADO);
                out.writeObject(getUsuarioLog());
            }
            else
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
                creado = Servidor.listaSalas.CrearSala(nombreSala, this);

            out.writeObject(creado);
        }

        if (action == Actions.GETLOBBYSstream)
        {

            lobbyStream = new Thread(() ->
            {
                ArrayList<Sala> salasAnteriores = null;
                while (true)
                {
                    try
                    {
                        Thread.sleep(5);
                        if (intLobby)
                        {
                            System.out.println("SOLIC DETENER");
                            Thread.currentThread().interrupt();
                            break;
                        }

                        ArrayList<Sala> salas = Servidor.listaSalas.getSalas();

                        if (salasAnteriores == null)
                        {
                            salasAnteriores = (ArrayList<Sala>) salas.clone();
                        }
                        else
                        {
                            if (salas.equals(salasAnteriores))
                                continue;
                            else
                                salasAnteriores = (ArrayList<Sala>) salas.clone();
                        }

                        System.out.println("Lobby cambio, enviada act.");
                        out.writeObject(salas);
                        System.out.println("Envie sala");
                        out.reset();
                    }
                    catch (IOException ex)
                    {
                        System.out.println("ERROR Y SED ETUVO");
                        ex.printStackTrace();
                        break;
                    }
                    catch (InterruptedException ex){ }
                }
            });

            lobbyStream.start();
        }

        if (action == Actions.GETSALAstream)
        {
            long id = (long) in.readObject();

            salaStream = new Thread(() ->
            {
                Sala salaAnterior = null;

                while (true)
                {
                    try
                    {
                        Thread.sleep(5);
                        
                        if (intSala)
                        {
                            System.out.println("SOLIC DETENER");
                            Thread.currentThread().interrupt();
                            break;
                        }

                        Sala sala = Servidor.listaSalas.getSala(id).clone();

                        if (salaAnterior == null)
                        {
                            salaAnterior = sala.clone();
                        }
                        else
                        {
                            if (sala.equals(salaAnterior))
                                continue;
                            else
                                salaAnterior = sala.clone();
                        }

                        out.writeObject(sala);
                        System.out.println("Sala cambio, enviada act.");
                        out.reset();

                    }
                    catch (IOException ex)
                    {
                        ex.printStackTrace();

                        SalaServidor salaServ = Servidor.listaSalas.getSalaServidor(id);
                        salaServ.QuitarJugador(this);
                        Servidor.listaSalas.verificarValidez(salaServ);
                        break;
                    }
                    catch (InterruptedException | CloneNotSupportedException ex){}
                }
            });

            salaStream.start();
        }

        if (action == Actions.GETJUEGOstream)
        {
            long id = (long) in.readObject();

            juegoStream = new Thread(() ->
            {
                Sala salaAnterior = null;
                Jugadores jugadoresAnterior = null;

                while (true)
                {
                    try
                    {
                        Thread.sleep(500);

                        if (intJuego)
                        {
                            System.out.println("SOLIC DETENER");
                            Thread.currentThread().interrupt();
                            break;
                        }

                        SalaServidor salaServ = Servidor.listaSalas.getSalaServidor(id);
                        Sala sala = salaServ.pacLobby;

                        if (salaAnterior == null || jugadoresAnterior == null)
                        {
                            salaAnterior = sala.clone();
                            jugadoresAnterior = sala.jugadores.clone();
                        }
                        else
                        {
                            boolean salas = salaAnterior.equals(sala);
                            boolean jugadores = jugadoresAnterior.equals(sala.jugadores);
                            
                            if (salas && jugadores)
                                continue;
                            else
                            {
                                salaAnterior = sala.clone();
                                jugadoresAnterior = sala.jugadores.clone();
                            }
                        }

                        out.reset();
                        out.writeObject(sala);
                        Pacman miPacman = getUsuarioLog().paco;
                        out.writeObject(miPacman);

                        for (int i = 0; i < 4; i++)
                        {
                            Pacman pacman = null;
                            try
                            {
                                pacman = sala.jugadores.get(i).paco;
                                
                                if (pacman.equals(miPacman))
                                    continue;
                            }
                            catch (IndexOutOfBoundsException e){ }

                            out.writeObject(pacman);
                        }
                        
                        if(sala.pelletsRestantes < 1)
                        {
                            sala.fant1.intMovimiento = true;
                            sala.fant2.intMovimiento = true;
                            sala.fant3.intMovimiento = true;
                            sala.fant4.intMovimiento = true;
                            while(sala.fant1.movimiento.isAlive() || sala.fant2.movimiento.isAlive() || sala.fant3.movimiento.isAlive() || sala.fant4.movimiento.isAlive() );
                            
                            Usuario ganador = getUsuarioLog();
                            
                            for(Usuario usu : sala.jugadores)
                            {
                                usu.pJugadas++;
                                usu.pPerdidas++;
                                
                                if(usu.recordPuntos < usu.puntosPaco)
                                    usu.recordPuntos = usu.puntosPaco;
                                
                                if(usu.puntosPaco > ganador.puntosPaco)
                                    ganador = usu;
                            }
                            
                            ganador.pGanadas++;
                            ganador.pPerdidas--;
                            
                            out.writeObject(Respuesta.JUEGOTERMINADO);
                            out.writeObject(ganador);
                            
                            Thread.currentThread().interrupt();
                            salaServ.QuitarJugador(this);
                            Servidor.listaSalas.verificarValidez(salaServ);
                            Servidor.guardaUsuarios();
                            break;
                        }

                    }
                    catch (IOException ex)
                    {
                        SalaServidor salaServ = Servidor.listaSalas.getSalaServidor(id);
                        salaServ.QuitarJugador(this);
                        Servidor.listaSalas.verificarValidez(salaServ);
                        break;
                    }
                    catch (InterruptedException | CloneNotSupportedException e){}
                }
            });

            juegoStream.start();
        }

        if (action == Actions.GETSALAstreamStop)
        {
            intSala = true;
            if (salaStream != null)
            {
                while (salaStream.isAlive());
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
                while (lobbyStream.isAlive());
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
                if (salaServ.AgregaJugador(this))
                    out.writeObject(true);
            }

            out.writeObject(false);
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

        if (action == Actions.ActPACMAN)
        {
            long id = (long) in.readObject();
            SalaServidor salaServ = Servidor.listaSalas.getSalaServidor(id);
            Pacman paco = (Pacman) in.readObject();
            int restaPuntos = (int) in.readObject();
            char type = salaServ.pacLobby.cellsMapa[paco.pacmanRow][paco.pacmanCol].getType();
            
            for (int i = 0; i < salaServ.pacLobby.jugadores.size(); i++)
            {
                if (salaServ.pacLobby.jugadores.get(i).equals(getUsuarioLog()))
                {
                    getUsuarioLog().paco = paco;
                    salaServ.pacLobby.jugadores.get(i).paco = paco;
                    break;
                }
            }
            
            if (type == 'm')
            {
                salaServ.pacLobby.cellsMapa[paco.pacmanRow][paco.pacmanCol].type = 'v';
                salaServ.pacLobby.pelletsRestantes--;
                getUsuarioLog().puntosPaco += 10;
            }
            else if (type == 'n')
            {
                salaServ.pacLobby.cellsMapa[paco.pacmanRow][paco.pacmanCol].type = 'v';
                salaServ.pacLobby.pelletsRestantes--;
                getUsuarioLog().puntosPaco += 50;
                getUsuarioLog().paco.powerUP = true;
                paco.powerUP = true;                
                
                for (int i = 0; i < salaServ.pacLobby.jugadores.size(); i++)
                {
                    salaServ.jugadores.get(i).out.writeObject(Respuesta.PLAYSONIDO);
                    salaServ.jugadores.get(i).out.writeObject("POWER");
                }
            }
            
            if (paco.powerUP)
            {
                Jugadores jugadores = salaServ.pacLobby.jugadores;
                
                for (int i = 0; i < jugadores.size(); i++)
                {
                    if (!jugadores.get(i).equals(getUsuarioLog()))
                    {
                        if (jugadores.get(i).paco.chocan(paco))
                        {
                            if(jugadores.get(i).paco.livesLeft < 1)
                            {
                                if(jugadores.get(i).paco.puntos > 1000)
                                    jugadores.get(i).paco.puntos -= 1000;
                                else
                                    jugadores.get(i).paco.puntos -= jugadores.get(i).paco.puntos;
                            }
                            else
                            {
                                jugadores.get(i).paco.livesLeft--;
                            }
                            
                            jugadores.get(i).paco.ubicados = false;
                            paco.powerUP = false;
                            getUsuarioLog().paco.powerUP = false;
                                            
                            salaServ.jugadores.get(i).out.writeObject(Respuesta.PLAYSONIDO);
                            salaServ.jugadores.get(i).out.writeObject("ATE");
                        }
                    }
                }
            }

            getUsuarioLog().puntosPaco -= restaPuntos;
            paco.puntos = getUsuarioLog().puntosPaco;
        }

        if (action == Actions.PLAYALL)
        {
            long id = (long) in.readObject();
            SalaServidor salaServ = Servidor.listaSalas.getSalaServidor(id);
            salaServ.empezar();

            for (ClienteServidor cliente : salaServ.jugadores)
            {
                if (!cliente.equals(this))
                {
                    if (cliente.salaStream != null)
                    {
                        cliente.intSala = true;
                        while (cliente.salaStream.isAlive());
                        cliente.intSala = false;
                        cliente.salaStream = null;
                    }
                    cliente.out.writeObject(Respuesta.PLAY);
                    cliente.out.writeObject(Respuesta.OK);
                }
                else
                {
                    if (salaStream != null)
                    {
                        intSala = true;
                        while (salaStream.isAlive());
                        intSala = false;
                        salaStream = null;
                    }
                    out.writeObject(Respuesta.PLAY);
                    out.writeObject(Respuesta.OK);
                }
            }
        }
        
        if (action == Actions.TOP5)
        {
            ArrayList<Usuario> top5 = new ArrayList<Usuario>();
            ArrayList<Usuario> registrados = (ArrayList<Usuario>) Servidor.usuariosRegistrados.clone();
            int total = 5;
            
            if(registrados.size() < 5)
                total = registrados.size();
                
            for(int i=0; i < total; i++)
            {
                Usuario posicion = new Usuario();
                
                
                for(Usuario usu : registrados)
                {
                    if(usu.recordPuntos > posicion.recordPuntos)
                    {
                        posicion = usu;
                    }
                }
                
                if(posicion.recordPuntos < 1)
                    continue;
                
                top5.add(posicion);
                registrados.remove(posicion);
            }
            
            out.writeObject(top5);
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
        catch (IOException | ClassNotFoundException e)
        {
            System.err.println(e.getMessage());
        }
        finally
        {
            Servidor.clientes.remove(this);
        }
    }
}

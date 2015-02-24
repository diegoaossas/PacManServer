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

public class ClienteServidor implements Runnable
{

    private final Socket socket;
    private ObjectInputStream in;
    private ObjectOutputStream out;
    private Thread lobbyStream;
    private Thread salaStream;
    private Thread pacmanStream;

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
            System.out.println("Solicitud de login recibida.");

            Credenciales cred = (Credenciales) in.readObject();
            System.out.println("Credenciales recibidas.");

            for (Usuario usu : Servidor.usuariosRegistrados)
            {
                if (usu.Cuenta.toLowerCase().equals(cred.usuario.toLowerCase()))
                {
                    if (usu.Clave.equals(cred.clave))
                    {
                        out.writeObject(Respuesta.LOGGED);
                        usuarioLog = usu;
                        out.writeObject(getUsuarioLog());
                        System.out.println("Login completo, usuario enviado -> " + usu.Cuenta);
                    } else
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
            System.out.println("Solicitud de registro recibida.");

            Credenciales cred = (Credenciales) in.readObject();
            System.out.println("Credenciales recibidas.");
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
                System.out.println("Registro completo, usuario enviado -> " + usu.Cuenta);
            } else
            {
                System.out.println("Registro fallido, usuario existente");
                out.writeObject(Respuesta.NOREGISTRADO);
                socket.close();
            }

        }

        if (action == Actions.NEWLOBBY)
        {
            System.out.println("Solicitud NEWLOBBY.");
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
            System.out.println("Solicitud GETLOBBYSstream.");

            lobbyStream = new Thread(() ->
            {
                while (true)
                {
                    try
                    {
                        ArrayList<Sala> salas = Servidor.listaSalas.getSalas();
                        out.reset();
                        out.writeObject(salas);

                        for (Sala sala : salas)
                        {
                            System.out.println("ClienteServidor::GETLOBBYSstream -> Sala enviada " + sala.nombreSala + " con " + sala.jugadores.size() + " de " + sala.maxjugadores);
                        }

                        try
                        {
                            Thread.sleep(500);
                        } catch (InterruptedException e)
                        {
                            Thread.currentThread().interrupt(); // restore interrupted status
                            break;
                        }

                    } catch (IOException ex)
                    {
                        break;
                    }
                }
            });

            lobbyStream.start();
        }

        if (action == Actions.GETSALAstream)
        {
            System.out.println("Solicitud GETSALAstream.");
            long id = (long) in.readObject();

            salaStream = new Thread(() ->
            {
                while (true)
                {
                    try
                    {
                        out.reset();
                        Sala sala = Servidor.listaSalas.getSala(id);
                        out.writeObject(sala);

                        try
                        {
                            Thread.sleep(500);
                        } catch (InterruptedException e)
                        {
                            Thread.currentThread().interrupt();
                            break;
                        }

                    } catch (IOException ex)
                    {
                        System.err.println("Error: " + ex.getMessage());
                        ex.printStackTrace();

                        SalaServidor salaServ = Servidor.listaSalas.getSalaServidor(id);
                        salaServ.QuitarJugador(this);
                        Servidor.listaSalas.verificarValidez(salaServ);
                        break;
                    }
                }
            });

            salaStream.start();
        }

        if (action == Actions.GETJUEGOstream)
        {
            System.out.println("Solicitud GETJUEGOstream.");
            long id = (long) in.readObject();

            salaStream = new Thread(() ->
            {
                while (true)
                {
                    try
                    {
                        out.reset();
                        SalaServidor salaServ = Servidor.listaSalas.getSalaServidor(id);
                        Sala sala = salaServ.pacLobby;
                        out.writeObject(sala);
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
                                {
                                    continue;
                                }
                            } catch (Exception e)
                            {
                            }

                            out.writeObject(pacman);
                        }

                        try
                        {
                            Thread.sleep(60);
                        } catch (InterruptedException e)
                        {
                            Thread.currentThread().interrupt();
                            break;
                        }

                    } catch (IOException ex)
                    {
                        System.err.println("Error: " + ex.getMessage());
                        ex.printStackTrace();

                        SalaServidor salaServ = Servidor.listaSalas.getSalaServidor(id);
                        salaServ.QuitarJugador(this);
                        Servidor.listaSalas.verificarValidez(salaServ);
                        break;
                    }
                }
            });

            salaStream.start();
        }

        if (action == Actions.PacManSTREAM)
        {
            System.out.println("Solicitud GETSALAstream.");
            long id = (long) in.readObject();

            pacmanStream = new Thread(() ->
            {
                while (true)
                {
                    try
                    {
                        out.reset();
                        SalaServidor salaServ = Servidor.listaSalas.getSalaServidor(id);
                        Pacman elMio = getUsuarioLog().paco;
                        out.writeObject(elMio);

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
                            }

                            out.writeObject(pacman);
                        }

                        try
                        {
                            Thread.sleep(10);
                        } catch (InterruptedException e)
                        {
                            Thread.currentThread().interrupt();
                            break;
                        }

                    } catch (IOException ex)
                    {
                        getUsuarioLog().paco = null;
                        break;
                    }
                }
            });

            pacmanStream.start();
        }

        if (action == Actions.GETSALAstreamStop)
        {
            System.out.println("Solicitud GETSALAstreamStop.");
            if (salaStream != null)
            {
                salaStream.interrupt();
                salaStream = null;
            }
            out.writeObject(Respuesta.OK);
        }

        if (action == Actions.GETLOBBYSstreamStop)
        {
            System.out.println("Solicitud GETLOBBYSstreamStop.");
            if (lobbyStream != null)
            {
                lobbyStream.interrupt();
                lobbyStream = null;
            }
            out.writeObject(Respuesta.OK);
        }

        if (action == Actions.JoinSALA)
        {
            System.out.println("Solicitud JoinSALA.");
            long id = (long) in.readObject();
            SalaServidor salaServ = Servidor.listaSalas.getSalaServidor(id);
            Sala sala = salaServ.pacLobby;

            if (sala.jugadores.size() < sala.maxjugadores)
            {
                salaServ.AgregaJugador(this);
                out.writeObject(true);
                System.out.println("jugador agrergado.");
            } else
            {
                out.writeObject(false);
                System.out.println("jugador no agrergado.");
            }
        }

        if (action == Actions.LeaveSALA)
        {
            System.out.println("Solicitud LeaveSALA.");
            long id = (long) in.readObject();
            SalaServidor salaServ = Servidor.listaSalas.getSalaServidor(id);
            System.out.println("Remover jugador '" + this.usuarioLog.Cuenta + "' de sala: " + salaServ.pacLobby.nombreSala);
            salaServ.QuitarJugador(this);
            Servidor.listaSalas.verificarValidez(salaServ);
            out.writeObject(Respuesta.OK);
        }

        if (action == Actions.DESCONECTAR)
        {
            System.out.println("Solicitud DESCONECTAR.");
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
                    getUsuarioLog().paco.puntos += 1;
                }
                else if (type == 'n')
                {
                    salaServ.pacLobby.cellsMapa[getUsuarioLog().paco.pacmanRow][getUsuarioLog().paco.pacmanCol].type = 'v';
                    getUsuarioLog().paco.puntos += 5;
                }

                System.out.println("puntos de paquito: " + getUsuarioLog().paco.puntos);
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
                        cliente.salaStream.interrupt();
                        cliente.salaStream = null;
                    }
                    cliente.out.writeObject(Respuesta.OK);
                } else
                {
                    out.writeObject(Respuesta.PLAY);
                    if (salaStream != null)
                    {
                        salaStream.interrupt();
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
        } catch (IOException | ClassNotFoundException e)
        {
            System.out.println("Error: " + e.getMessage());
        } finally
        {
            Servidor.clientes.remove(this);
        }

    }

}

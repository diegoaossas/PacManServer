package pacmanserver;

import Libreria.Pacman;
import Libreria.Sala;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class SalaServidor implements Serializable
{

    private static final long serialVersionUID = 1L;

    public List<ClienteServidor> jugadores;
    public Sala pacLobby;

    public SalaServidor(Sala sala)
    {
        this.pacLobby = sala;
        this.jugadores = new ArrayList<>();
    }

    public boolean AgregaJugador(ClienteServidor jugador)
    {
        for (ClienteServidor cliente : jugadores)
        {
            if (cliente.getUsuarioLog().Cuenta.equals(jugador.getUsuarioLog().Cuenta))
            {
                return false;
            }
        }

        if (jugadores.size() >= pacLobby.maxjugadores)
        {
            return false;
        }

        jugador.getUsuarioLog().paco = new Pacman(3);
        jugador.getUsuarioLog().paco.pos = 0;
        this.jugadores.add(jugador);
        this.pacLobby.agregarJugador(jugador.getUsuarioLog());
        return true;
    }

    public boolean QuitarJugador(ClienteServidor jugador)
    {
        for (ClienteServidor cliente : jugadores)
        {
            if (cliente.getUsuarioLog().Cuenta.equals(jugador.getUsuarioLog().Cuenta))
            {
                this.jugadores.remove(cliente);
                this.pacLobby.quitarJugador(cliente.getUsuarioLog());
                jugador.getUsuarioLog().paco = null;
                return true;
            }
        }

        return false;
    }
    
    public void empezar()
    {
        int posN = 0;
        for(ClienteServidor cliente : jugadores)
        {
            cliente.getUsuarioLog().paco.pos = posN;
            cliente.getUsuarioLog().paco.color = posN;
            posN++;
        }
        pacLobby.empezado = true;
    }
}

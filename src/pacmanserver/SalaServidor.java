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
        pacLobby = sala;
        jugadores = new ArrayList<>();
    }

    public boolean AgregaJugador(ClienteServidor jugador)
    {
        for (ClienteServidor cliente : jugadores)
        {
            if(cliente.equals(jugador))
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
        jugadores.add(jugador);
        pacLobby.agregarJugador(jugador.getUsuarioLog());
        return true;
    }

    public boolean QuitarJugador(ClienteServidor jugador)
    {
        for (ClienteServidor cliente : jugadores)
        {
            if (cliente.equals(jugador))
            {
                pacLobby.quitarJugador(cliente.getUsuarioLog());
                jugadores.remove(cliente);
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

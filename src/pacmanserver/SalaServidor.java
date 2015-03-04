package pacmanserver;

import Libreria.Pacman;
import Libreria.Sala;
import java.awt.Color;
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
                return false;
        }

        if (jugadores.size() >= pacLobby.maxjugadores)
            return false;

        jugador.getUsuarioLog().paco = new Pacman(3);
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
                jugador.getUsuarioLog().paco = new Pacman(3);
                
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
            cliente.getUsuarioLog().puntosPaco = 0;
            cliente.getUsuarioLog().paco.pos = posN;
            
            switch(posN)
            {
                case 0:
                    cliente.getUsuarioLog().paco.color = Color.YELLOW;
                    break;
                case 1:
                    cliente.getUsuarioLog().paco.color = Color.CYAN;
                    break;
                case 2:
                    cliente.getUsuarioLog().paco.color = Color.GREEN;
                    break;
                case 3:
                    cliente.getUsuarioLog().paco.color = Color.ORANGE;
                    break;
            }
            posN++;
        }
        pacLobby.empezado = true;
    }
    
    @Override
    protected SalaServidor clone() throws CloneNotSupportedException
    {
        SalaServidor salaS = (SalaServidor) super.clone();
        salaS.pacLobby = pacLobby.clone();
        salaS.jugadores = new ArrayList<>();
        
        for(ClienteServidor cliente : jugadores)
        {
            salaS.jugadores.add(cliente);
        }
        
        return salaS;
    }
}

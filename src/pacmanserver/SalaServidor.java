package pacmanserver;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import Libreria.Sala;
import Libreria.Usuario;

public class SalaServidor implements Serializable
{    
    private static final long serialVersionUID = 1L;
    
    private final List<ClienteServidor> jugadores;
    public Sala pacLobby;
    
    public SalaServidor(long idSala, Sala sala, ClienteServidor propietario)
    {
        sala.idSala = idSala;
        sala.jugadores = new ArrayList<Usuario>();
        sala.agregarJugador(propietario.getUsuarioLog());
        
        this.pacLobby = sala;
        this.jugadores = new ArrayList<>(sala.maxjugadores);
    }
    
    public boolean AgregaJugador(ClienteServidor jugador)
    {
        //if(jugadores.contains(jugador))
            //return true;
        
        if(jugadores.size() >= pacLobby.maxjugadores)
            return false;
        
        this.jugadores.add(jugador);
        this.pacLobby.agregarJugador(jugador.getUsuarioLog());
        return true;
    }   
    
    public boolean QuitarJugador(ClienteServidor jugador)
    {
        //if(jugadores.contains(jugador))
            //return true;
        

        this.jugadores.remove(jugador);
        this.pacLobby.quitarJugador(jugador.getUsuarioLog());
        return true;
    }
    /*
    public Sala getSala()
    {
        return this.pacLobby;
    }
    */
}

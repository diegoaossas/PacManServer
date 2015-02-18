package pacmanserver;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import Libreria.Sala;

public class SalaServidor implements Serializable
{    
    private static final long serialVersionUID = 1L;
    
    private final List<ClienteServidor> jugadores;
    public Sala pacLobby;
    
    public SalaServidor(Sala sala)
    {        
        this.pacLobby = sala;
        this.jugadores = new ArrayList<>();
    }
    
    public boolean AgregaJugador(ClienteServidor jugador)
    {
    	for(ClienteServidor cliente : jugadores)
    	{
    		if(cliente.getUsuarioLog().Cuenta.equals(jugador.getUsuarioLog().Cuenta))
    			return false;
    	}
        
        if(jugadores.size() >= pacLobby.maxjugadores)
            return false;
        
        this.jugadores.add(jugador);
        this.pacLobby.agregarJugador(jugador.getUsuarioLog());
        return true;
    }
    
    public boolean QuitarJugador(ClienteServidor jugador)
    {
    	for(ClienteServidor cliente : jugadores)
    	{
    		if(cliente.getUsuarioLog().Cuenta.equals(jugador.getUsuarioLog().Cuenta))
    		{
    	        this.jugadores.remove(cliente);
    	        this.pacLobby.quitarJugador(cliente.getUsuarioLog());
    			return true;
    		}
    	}

        return false;
    }
}

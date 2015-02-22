package pacmanserver;

import Libreria.Pacman;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import Libreria.Sala;

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
    	for(ClienteServidor cliente : jugadores)
    	{
    		if(cliente.getUsuarioLog().Cuenta.equals(jugador.getUsuarioLog().Cuenta))
    			return false;
    	}
        
        if(jugadores.size() >= pacLobby.maxjugadores)
            return false;
		
		jugador.paquito = new Pacman(10);
		jugador.paquito.pos = this.jugadores.size();
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
				jugador.paquito = null;
    			return true;
    		}
    	}

        return false;
    }
}

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pacmanserver;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Diego
 */
public class PacLobby implements Serializable{
    private final long idLobby;
    private final String nombreLobby;
    
    private transient final PacCliente propietario;
    private transient final List<PacCliente> jugadores;
    
    public PacLobby(long idLobby, String nombreLobby, PacCliente propietario)
    {
        this.idLobby = idLobby;
        this.nombreLobby = nombreLobby;
        this.propietario = propietario;
        this.jugadores = new ArrayList<>(3);
    }
    
    public boolean AgregaJugador(PacCliente jugador)
    {
        if(jugadores.contains(jugador))
            return true;
        
        if(jugadores.size() >= 3)
            return false;
        
        jugadores.add(jugador);
        return true;
    }
    
    public long getID()
    {
        return this.idLobby;
    }
    
    public String getNombre()
    {
        return this.nombreLobby;
    }
}

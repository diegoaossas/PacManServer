/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pacmanserver;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import Libreria.Sala;

/**
 *
 * @author Diego
 */
public class SalaServidor implements Serializable{    
    private transient final ClienteServidor propietario;
    private transient final List<ClienteServidor> jugadores;
    private Sala pacLobby;
    
    public SalaServidor(long idSala, String nombreSala, ClienteServidor propietario)
    {
        this.pacLobby = new Sala();
        this.pacLobby.idSala = idSala;
        this.pacLobby.nombreSala = nombreSala;
        
        this.propietario = propietario;
        this.jugadores = new ArrayList<>(3);
    }
    
    public boolean AgregaJugador(ClienteServidor jugador)
    {
        if(jugadores.contains(jugador))
            return true;
        
        if(jugadores.size() >= 3)
            return false;
        
        jugadores.add(jugador);
        return true;
    }
    
    public Sala getSala()
    {
        return this.pacLobby;
    }
}

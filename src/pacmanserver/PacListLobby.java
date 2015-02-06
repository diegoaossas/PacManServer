/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pacmanserver;

import java.io.Serializable;
import java.util.ArrayList;

/**
 *
 * @author Diego
 */
public class PacListLobby  implements Serializable{
    private final ArrayList<PacLobby> salas;
    private long contador;
    
    public PacListLobby()
    {
        salas = new ArrayList<>();
        contador = 0;
    }
    
    public boolean CrearSala(String nombreSala, PacCliente propietario)
    {
        PacLobby sala = new PacLobby(contador++, "Sala de " + propietario.usuarioLog.Usuario, propietario);
        salas.add(sala);
        
        System.out.println("CrearSala(): Sala (" + sala.getNombre() + " - ID: " + sala.getID() + ") creada.");
        System.out.println(salas.size() + " salas actualmente.");
        
        return true;
    }
    
    public ArrayList<PacLobby> getSalas()
    {
        return (ArrayList<PacLobby>)salas.clone();
    }
}

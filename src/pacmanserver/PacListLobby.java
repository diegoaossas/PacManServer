/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pacmanserver;

import Libreria.Sala;
import java.io.Serializable;
import java.util.ArrayList;

/**
 *
 * @author Diego
 */
public class PacListLobby  implements Serializable{
    private final ArrayList<SalaServidor> salas;
    private long contador;
    
    public PacListLobby()
    {
        salas = new ArrayList<>();
        contador = 0;
    }
    
    public boolean CrearSala(String nombreSala, ClienteServidor propietario)
    {
        SalaServidor sala = new SalaServidor(contador++, "Sala de " + propietario.usuarioLog.Usuario, propietario);
        salas.add(sala);
        
        System.out.println("CrearSala(): Sala (" + sala.getSala().getNombre() + " - ID: " + sala.getSala().getID() + ") creada.");
        System.out.println(salas.size() + " salas actualmente.");
        
        return true;
    }
    
    public ArrayList<SalaServidor> getSalasServidor()
    {
        return (ArrayList<SalaServidor>)salas.clone();
    }
    
    public ArrayList<Sala> getSalas()
    {
        ArrayList<Sala> listaSalas = new ArrayList<>();
        
        for(SalaServidor sala : salas)
        {
            listaSalas.add(sala.getSala());
        }
        
        return listaSalas;
    }
}

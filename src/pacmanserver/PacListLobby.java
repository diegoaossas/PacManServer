package pacmanserver;

import java.io.Serializable;
import java.util.ArrayList;

import Libreria.Sala;

public class PacListLobby  implements Serializable
{
	private static final long serialVersionUID = 1L;
	
	private final ArrayList<SalaServidor> salas;
    private long contador;
    
    public PacListLobby()
    {
        salas = new ArrayList<SalaServidor>();
        contador = 0;
    }

    public long CrearSala(Sala sala, ClienteServidor propietario)
    {
        contador++;
        
        SalaServidor salaServidor = new SalaServidor(contador, sala, propietario);
        salas.add(salaServidor);
        
        System.out.println("CrearSala(): Sala (" + salaServidor.pacLobby.nombreSala + " - ID: " + salaServidor.pacLobby.idSala + ") creada.");
        System.out.println(salas.size() + " salas actualmente.");
        
        return contador;
    }
    
    public int getCantSalas()
    {
        return salas.size();
    }
    
    public Sala getSala(long id)
    {
        for(SalaServidor salaServ : salas)
        {
            Sala sala = salaServ.pacLobby;
            
            if(sala.idSala == id)
            {
                System.out.println("PacListLobby::getSala() -> Sala " + sala.nombreSala + " con " + sala.jugadoresEnSala + " de " + sala.maxjugadores);
                return sala;
            }
        }
        return null;
    }
    
    public ArrayList<Sala> getSalas()
    {
        ArrayList<Sala> listaSalas = new ArrayList<>();
        
        for(SalaServidor salaServ : salas)
        {
            Sala sala = salaServ.pacLobby;
            listaSalas.add(sala);
            System.out.println("PacListLobby::getSalas() -> Sala " + sala.nombreSala + " con " + sala.jugadoresEnSala + " de " + sala.maxjugadores);
        }
        
        return listaSalas;
    }
    
    public SalaServidor getSalaServidor(long id)
    {
        for(SalaServidor salaServ : salas)
        {
            Sala sala = salaServ.pacLobby;
            
            if(sala.idSala == id)
            {
                System.out.println("PacListLobby::getSalaServidor() -> Sala " + sala.nombreSala + " con " + sala.jugadoresEnSala + " de " + sala.maxjugadores);
                return salaServ;
            }
        }
        return null;
    }
}

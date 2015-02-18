package pacmanserver;

import java.io.Serializable;
import java.util.ArrayList;

import Libreria.Sala;
import Libreria.Usuario;

public class PacListLobby  implements Serializable
{
	private static final long serialVersionUID = 1L;
	
	private int maxSalas = 4;
	private long idSala = 1;
	private final ArrayList<SalaServidor> salas;
    
    public PacListLobby()
    {
        salas = new ArrayList<SalaServidor>();
    }

    public long CrearSala(String nombreSala, ClienteServidor propietario)
    {
    	if(salas.size() >= maxSalas)
    		return 0;
    	
    	Sala sala = new Sala();
    	sala.idSala = idSala++;
    	sala.nombreSala = nombreSala;
        sala.jugadores = new ArrayList<Usuario>();
        
        SalaServidor salaServidor = new SalaServidor(sala);
        salaServidor.AgregaJugador(propietario);
        
        salas.add(salaServidor);
        System.out.println("CrearSala(): Sala (" + salaServidor.pacLobby.nombreSala + " - ID: " + salaServidor.pacLobby.idSala + ") creada.");
        System.out.println(salas.size() + " salas actualmente.");
        
        return salaServidor.pacLobby.idSala;
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
                System.out.println("PacListLobby::getSala() -> Sala " + sala.nombreSala + " con " + sala.jugadores.size() + " de " + sala.maxjugadores);
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
            System.out.println("PacListLobby::getSalas() -> Sala " + sala.nombreSala + " con " + sala.jugadores.size() + " de " + sala.maxjugadores);
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
                System.out.println("PacListLobby::getSalaServidor() -> Sala " + sala.nombreSala + " con " + sala.jugadores.size() + " de " + sala.maxjugadores);
                return salaServ;
            }
        }
        return null;
    }
}

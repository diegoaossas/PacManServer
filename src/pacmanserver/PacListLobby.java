package pacmanserver;

import Libreria.Sala;
import java.io.Serializable;
import java.util.ArrayList;

public class PacListLobby implements Serializable
{
    private static final long serialVersionUID = 1L;

    private final int maxSalas = 4;
    private long idSala = 1;
    private final ArrayList<SalaServidor> salas;

    public PacListLobby()
    {
        salas = new ArrayList<SalaServidor>();
    }

    public long CrearSala(String nombreSala, ClienteServidor propietario)
    {
        if (salas.size() >= maxSalas)
            return 0;

        Sala sala = new Sala();
        sala.idSala = idSala;
        sala.nombreSala = nombreSala;
        sala.capitan = propietario.getUsuarioLog().Cuenta;

        SalaServidor salaServidor = new SalaServidor(sala);
        salaServidor.AgregaJugador(propietario);
        salas.add(salaServidor);
        
        idSala++;

        return salaServidor.pacLobby.idSala;
    }

    public int getCantSalas()
    {
        return salas.size();
    }

    public Sala getSala(long id)
    {
        for (SalaServidor salaServ : salas)
        {
            Sala sala = salaServ.pacLobby;

            if (sala.idSala == id)
                return sala;
        }
        
        return null;
    }

    public ArrayList<Sala> getSalas()
    {
        @SuppressWarnings("unchecked")
        ArrayList<SalaServidor> salasS = (ArrayList<SalaServidor>) salas.clone();
        ArrayList<Sala> listaSalas = new ArrayList<>();

        for (SalaServidor salaServ : salasS)
        {
            Sala sala = salaServ.pacLobby;
            listaSalas.add(sala);
        }

        return listaSalas;
    }

    public SalaServidor getSalaServidor(long id)
    {
        for (SalaServidor salaServ : salas)
        {
            Sala sala = salaServ.pacLobby;

            if (sala.idSala == id)
                return salaServ;
        }
        
        return null;
    }

    public void verificarValidez(SalaServidor salaServ)
    {
        if (salaServ.jugadores.size() < 1)
            salas.remove(salaServ);
    }
}

package pacmanserver;

import Libreria.Pacman;
import java.io.Serializable;
import java.util.ArrayList;

import Libreria.Sala;
import Libreria.Usuario;

public class PacListLobby implements Serializable
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
        if (salas.size() >= maxSalas)
        {
            return 0;
        }

        Sala sala = new Sala();
        sala.idSala = idSala++;
        sala.nombreSala = nombreSala;
        sala.jugadores = new ArrayList<Usuario>();
        sala.capitan = propietario.getUsuarioLog().Cuenta;

        SalaServidor salaServidor = new SalaServidor(sala);
        salaServidor.AgregaJugador(propietario);

        salas.add(salaServidor);

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
            {
                return sala;
            }
        }
        return null;
    }

    public ArrayList<Sala> getSalas()
    {
        ArrayList<Sala> listaSalas = new ArrayList<>();

        for (SalaServidor salaServ : salas)
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
            {
                return salaServ;
            }
        }
        return null;
    }
    
    public void verificarValidez(SalaServidor salaServ)
    {
        if(salaServ.jugadores.size() < 1)
            salas.remove(salaServ);
    }
}

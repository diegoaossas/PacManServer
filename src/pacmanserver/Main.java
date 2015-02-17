package pacmanserver;

public class Main
{
    public static void main(String[] args)
    {
            Servidor servidor = new Servidor(3000);
            servidor.start();
    }
    
}

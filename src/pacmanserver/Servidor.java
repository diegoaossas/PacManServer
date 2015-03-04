package pacmanserver;

import Libreria.Usuario;
import java.io.FileWriter;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;

public class Servidor extends Thread
{
    public static PacListLobby listaSalas;
    public static ArrayList<ClienteServidor> clientes;
    public static ArrayList<Usuario> usuariosRegistrados;
    
    private ServerSocket listener = null;
    private final int MAX_COLA = 200;
    private final int puerto;

    public Servidor(int puerto)
    {
        this.puerto = puerto;
        clientes = new ArrayList<ClienteServidor>();
        usuariosRegistrados = new ArrayList<Usuario>();
        listaSalas = new PacListLobby();

        cargarUsuarios();
    }
    
    public static void cargarUsuarios()
    {
        List<Element> list = XML.cargaElementos("Usuarios.xml", "Usuario");

        if (list != null)
        {
            for (Element node : list)
            {
                Usuario usuario = XML.elementToUsuario(node);
                usuariosRegistrados.add(usuario);
            }
        }
    }

    public static void guardaUsuarios()
    {
        try
        {
            Element root = new Element("Usuarios");
            Document doc = new Document();
            doc.setRootElement(root);

            for (Usuario usu : usuariosRegistrados)
            {
                Element usuario = XML.usuarioToElement(usu);
                doc.getRootElement().addContent(usuario);
            }

            XMLOutputter xmlOutput = new XMLOutputter();
            xmlOutput.setFormat(Format.getPrettyFormat());
            xmlOutput.output(doc, new FileWriter("Usuarios.xml"));
        }
        catch (IOException ex)
        {
            System.out.println(ex.getMessage());
        }

    }

    @Override
    public void run()
    {
        try
        {
            listener = new ServerSocket(puerto, MAX_COLA, InetAddress.getLocalHost());

            while (true)
            {
                try
                {
                    System.out.println("Esperando conexiones...");
                    Socket socket = listener.accept();
                    System.out.println("Conexion entrante desde: " + socket.getInetAddress());
                    ClienteServidor client = new ClienteServidor(socket);
                    new Thread(client).start();
                }
                catch (IOException ex)
                {
                    System.err.println(ex.getMessage());
                    break;
                }
            }
        }
        catch (IOException ex)
        {
            System.err.println(ex.getMessage());
        }

    }
}

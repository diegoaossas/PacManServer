package pacmanserver;

import Libreria.Usuario;
import java.io.File;
import java.io.IOException;
import java.util.List;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;

public class XML
{
    private XML() {}
    
    public static List<Element> cargaElementos(String archivo, String children)
    {
        SAXBuilder builder = new SAXBuilder();
        File xmlFile = new File(archivo);

        try
        {

            Document document = builder.build(xmlFile);
            Element rootNode = document.getRootElement();
            List<Element> list = rootNode.getChildren(children);

            return list;
        }
        catch (IOException | JDOMException ex)
        {
            System.err.println(ex.getMessage());
        }

        return null;
    }

    public static Usuario elementToUsuario(Element elemento)
    {
        Usuario usuario = new Usuario();

        usuario.ID = Integer.parseInt(elemento.getChildText("ID"));
        usuario.Cuenta = elemento.getChildText("Cuenta");
        usuario.Nombre = elemento.getChildText("Nombre");
        usuario.Clave = elemento.getChildText("Clave");
        usuario.pJugadas = Integer.parseInt(elemento.getChildText("pJugadas"));
        usuario.pGanadas = Integer.parseInt(elemento.getChildText("pGanadas"));
        usuario.pPerdidas = Integer.parseInt(elemento.getChildText("pPerdidas"));

        return usuario;
    }

    public static Element usuarioToElement(Usuario usuario)
    {
        Element elemento = new Element("Usuario");

        elemento.addContent(new Element("ID").setText(String.valueOf(usuario.ID)));
        elemento.addContent(new Element("Cuenta").setText(usuario.Cuenta));
        elemento.addContent(new Element("Nombre").setText(usuario.Nombre));
        elemento.addContent(new Element("Clave").setText(usuario.Clave));
        elemento.addContent(new Element("pJugadas").setText(String.valueOf(usuario.pJugadas)));
        elemento.addContent(new Element("pGanadas").setText(String.valueOf(usuario.pGanadas)));
        elemento.addContent(new Element("pPerdidas").setText(String.valueOf(usuario.pPerdidas)));

        return elemento;
    }

}

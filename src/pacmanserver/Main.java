/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pacmanserver;

import java.io.IOException;

/**
 *
 * @author Diego
 */
public class Main {
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args)
    {
        try
        {
            Servidor servidor = new Servidor(3000);
            servidor.start();
        }
        catch (IOException ex)
        {
            System.out.println("Error creando servidor...");
            System.out.println("-Mensaje del error: " + ex.getMessage());
        }
    }
    
}

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pacmanserver;

import java.io.Serializable;

/**
 *
 * @author Diego
 */
public class Usuario implements Serializable {
    public int ID;
    public String Usuario;
    public String Nickname;
    public int pJugadas;
    public int pGanadas;
    public int pPerdidas;
}

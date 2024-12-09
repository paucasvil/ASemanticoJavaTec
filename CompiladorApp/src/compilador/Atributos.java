/*:-----------------------------------------------------------------------------
 *:                       INSTITUTO TECNOLOGICO DE LA LAGUNA
 *:                     INGENIERIA EN SISTEMAS COMPUTACIONALES
 *:                         LENGUAJES Y AUTOMATAS II           
 *: 
 *:                       SEMESTRE: 7mo.     HORA: 8 - 9 HRS
 *:                                   
 *:               
 *:                      # Clase que contiene los atributos.
 *:                           
 *: Archivo       : Atributos.java
 *: Autor         : Layla Vanessa González Martínez 
 *: Fecha         : 05/OCT/2024
 *: Compilador    : Java JDK 18
 *: Descripción   : Se capturaron los atributos.
 *:                  
 *:           	     
 *: Ult.Modif.    :
 *:  Fecha      Modificó            Modificacion
 *:=============================================================================
 *:
 *:-----------------------------------------------------------------------------
 */

package compilador;

/**
 *
 * @author layla
 */
public class Atributos {

    public String tipo;
    public String h;
    public String longitud;
    public boolean esArreglo;
    public boolean esMetodo;

    public Atributos() {
        tipo = "";
        h = "";
        longitud = "";
        esArreglo = false;
        esMetodo = false;
    }

}

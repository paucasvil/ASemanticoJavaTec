/*:-----------------------------------------------------------------------------
 *:                       INSTITUTO TECNOLOGICO DE LA LAGUNA
 *:                     INGENIERIA EN SISTEMAS COMPUTACIONALES
 *:                         LENGUAJES Y AUTOMATAS II           
 *: 
 *:                SEMESTRE: AGO-DIC 2024    HORA: 8:00 - 9:00 HRS
 *:                                   
 *:               
 *:         Clase con la funcionalidad del Analizador Sintáctico
 *                 
 *:                           
 *: Archivo       : SintacticoSemantico.java
 *: Autor         : Fernando Gil  ( Estructura general de la clase  )
 *:                 Grupo de Lenguajes y Automatas II ( Procedures  )
 *: Fecha         : 03/SEP/2014
 *: Compilador    : Java JDK 7
 *: Descripción   : Esta clase implementa un parser descendente del tipo 
 *:                 Predictivo Recursivo. Se forma por un método por cada símbolo
 *:                 No-Terminal de la gramática más el método emparejar ().
 *:                 El análisis empieza invocando al método del símbolo inicial.
 *: Ult.Modif.    :
 *:  Fecha          Modificó                Modificacion
 *:============================================================================================
 *: 22/Feb/2015     FGil                    -Se mejoro errorEmparejar () para mostrar el
 *:                                         número de línea en el código fuente donde 
 *:                                         ocurrió el error.
 *:
 *: 08/Sep/2015     FGil                    -Se dejó lista para iniciar un nuevo analizador
 *:                                         sintáctico.
 *:
 *: 17/Sep/2024     Layla González y        -Se implementaron los procedures del PPR
 *:                 Paulina Castañeda       del lenguaje SIMPLE.
 *:
 *: 09/Nov/2024     Layla González y        -Se implentaron las acciones semánticas de 
 *:                 Paulina Castañeda       JavaTec.
 *:--------------------------------------------------------------------------------------------
 */

package compilador;

import general.Linea_BE;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SintacticoSemantico {
    // Constantes
    private Compilador cmp;
    private boolean analizarSemantica = false;
    private String preAnalisis;
    
    // Variables para resolver la gramática
    private boolean retroceso;  // Bandera que indica si se realizó un retroceso en el buffer de entrada
    private int ptr;            // Apuntador auxiliar a una localidad de Buffer
    
    // Variables de tipo
    public static String VACIO = "vacio";
    public static String ERROR_TIPO = "error_tipo";
    public static String BOOLEAN = "boolean";

    // Nueva estructura auxiliar para almacenar las firmas de los métodos
    private Map<Integer, List<String>> firmasMetodos = new HashMap<>();

    private ArrayList<Integer> arrayAuxiliar = new ArrayList ();
    
    // ------------------------------------------------------------------------------------------------------------------------------------------------
    
    // Constructor de la clase, recibe la referencia de la clase principal del compilador.
    public SintacticoSemantico ( Compilador c ) {
        cmp = c;
    }

    // ------------------------------------------------------------------------------------------------------------------------------------------------
    
    // Método que inicia la ejecucion del análisis sintáctico predictivo.
    // analizarSemantica : true  = realiza el análisis semántico a la par del sintáctico
    //                     false = realiza solo el análisis sintáctico sin comprobación semántica
    public void analizar ( boolean analizarSemantica ) {
        this.analizarSemantica = analizarSemantica;
        preAnalisis = cmp.be.preAnalisis.complex;

        // * * *   INVOCAR AQUI EL PROCEDURE DEL SIMBOLO INICIAL   * * *
        clase ( new Atributos () );
    }

    // ------------------------------------------------------------------------------------------------------------------------------------------------
    
    // Método para emparejar
    private void emparejar ( String t ) {
        if ( cmp.be.preAnalisis.complex.equals ( t ) ) {
            cmp.be.siguiente ();
            preAnalisis = cmp.be.preAnalisis.complex;
        } else {
            errorEmparejar ( t, cmp.be.preAnalisis.lexema, cmp.be.preAnalisis.numLinea );
        }
    }

    // ------------------------------------------------------------------------------------------------------------------------------------------------
    
    // Método para devolver un error al emparejar
    private void errorEmparejar ( String _token, String _lexema, int numLinea ) 
    {
        String msjError = "[emparejar] ";

        if ( _token.equals ( "id" ) ) 
        {
            msjError += "Se esperaba un identificador";
        } 
        else if ( _token.equals ( "num" ) ) 
        {
            msjError += "Se esperaba una constante entera";
        } 
        else if ( _token.equals ( "num.num" ) ) 
        {
            msjError += "Se esperaba una constante real";
        } 
        else if ( _token.equals ( "literal" ) ) 
        {
            msjError += "Se esperaba una literal";
        } 
        else if ( _token.equals ( "oparit" ) ) 
        {
            msjError += "Se esperaba un operador aritmético";
        } 
        else if ( _token.equals ( "oprel" ) ) 
        {
            msjError += "Se esperaba un operador relacional";
        } 
        else if ( _token.equals ( "opasig" ) ) 
        {
            msjError += "Se esperaba operador de asignación";
        } 
        else 
        {
            msjError += "Se esperaba " + _token;
        }
        msjError += " se encontró " + ( _lexema.equals ( "$" )? "fin de archivo" : _lexema ) + ". Línea " + numLinea; // FGil: Se agregó el número de línea

        cmp.me.error ( Compilador.ERR_SINTACTICO, msjError );
    }
    // Fin de ErrorEmparejar
    
    // ------------------------------------------------------------------------------------------------------------------------------------------------
    
    // Método para retroceder el símbolo de preAnalisis en el Buffer de entrada a la posición previamente guardada en ptr.
    private void retroceso () {
        cmp.be.setPrt ( ptr );
        preAnalisis = cmp.be.preAnalisis.complex;
        retroceso = true;
    }
    // Fin de ErrorEmparejar
    
    // ------------------------------------------------------------------------------------------------------------------------------------------------
    
    // Método para mostrar un error sintáctico
    private void error ( String _descripError ) {
        cmp.me.error ( cmp.ERR_SINTACTICO, _descripError );
    }
    // Fin de error
    
    // ------------------------------------------------------------------------------------------------------------------------------------------------
    
    // ------------------------------------------------------------------------------------------------------------------------------------------------
    //  *  *   *   *    PEGAR AQUÍ EL CÓDIGO DE LOS PROCEDURES  *  *  *  *
    // ------------------------------------------------------------------------------------------------------------------------------------------------
    
    // ------------------------------------------------------------------------------------------------------------------------------------------------
    // CÓDIGO DEL PARSER PREDICTIVO RECURSIVO DE LENGUAJE JAVATEC
    // ------------------------------------------------------------------------------------------------------------------------------------------------

    // Implementado por: Luis Ernesto Barranco (21130876)
    // PRIMEROS ( clase ) = { ‘public’ }
    private void clase ( Atributos clase ) {
        // Variables locales
        Linea_BE id = new Linea_BE();
        Atributos declaraciones = new Atributos ();
        Atributos declaraciones_metodos = new Atributos ();
        Atributos metodo_principal = new Atributos ();

        if ( preAnalisis.equals ( "public" ) ) {
            // clase → public class id {1} { declaraciones declaraciones_metodos metodo_principal } {2}
            
            emparejar ( "public" );
            emparejar ( "class" );
            id = cmp.be.preAnalisis; 
            emparejar ( "id" );

            // Accion semántica 1
            if ( analizarSemantica ) {
                cmp.ts.anadeTipo ( id.entrada, "class" );
            }
            // Fin Acción semántica 1

            emparejar ( "{" );
            declaraciones ( declaraciones );
            declaraciones_metodos ( declaraciones_metodos );
            metodo_principal ( metodo_principal );
            emparejar ( "}" );

            // Acción semántica 2
            if ( analizarSemantica ) {
                if ( declaraciones.tipo.equals ( VACIO ) && declaraciones_metodos.tipo.equals ( VACIO ) && metodo_principal.tipo.equals ( VACIO ) ) {
                    clase.tipo = VACIO;
                } else {
                    clase.tipo = ERROR_TIPO;
                    cmp.me.error ( Compilador.ERR_SEMANTICO, "[clase]. Hay incompatibilidad de tipos en el programa." );
                }
            }
            // Fin Acción semántica 2

        } else {
            error ( "[clase] Error al iniciar la clase. Línea: " + cmp.be.preAnalisis.numLinea );
        }
    }

    // ------------------------------------------------------------------------------------------------------------------------------------------------
    
    private void lista_identificadores ( Atributos lista_identificadores ) {
        // Variables locales
        Linea_BE id = new Linea_BE();
        Atributos dimension = new Atributos();
        Atributos lista_identificadores_prima = new Atributos();
        arrayAuxiliar.clear();
        
        if ( preAnalisis.equals ( "id" ) ) {
            // lista_identificadores -> id dimension {12} {13} lista_identificadores’ {14}
            
            id = cmp.be.preAnalisis;
            arrayAuxiliar.add( id.entrada );
            
            emparejar ( "id" );
            dimension ( dimension );
            
            // Acción semántica 12
            if ( analizarSemantica ) {
                if ( cmp.ts.buscaTipo ( id.entrada ).equals ( "" ) ) {
                    
                    if ( dimension.esArreglo ) {
                        cmp.ts.anadeTipo ( id.entrada, "array(0.." + dimension.longitud + ", " + lista_identificadores.h + ")" );
                    } else {
                        cmp.ts.anadeTipo ( id.entrada, lista_identificadores.h );
                    }
                    
                    lista_identificadores.tipo = VACIO;
                    
                } else {
                    lista_identificadores.tipo = ERROR_TIPO;
                    cmp.me.error ( Compilador.ERR_SEMANTICO, "[lista_identificadores]. Variable " + id.lexema + " re-declarada." );
                }
            }
            // Fin Acción semántica 12
            
            // Acción semántica 13
            if ( analizarSemantica ) {
                lista_identificadores_prima.h = lista_identificadores.h;
            }
            // Fin Acción semántica 13
            
            lista_identificadores_prima ( lista_identificadores_prima );
            
            // Acción semántica 14
            if ( analizarSemantica ) {
                if ( lista_identificadores_prima.tipo.equals ( VACIO ) && lista_identificadores.tipo.equals ( VACIO ) ) {
                    lista_identificadores.tipo = VACIO;
                } else {
                    lista_identificadores.tipo = ERROR_TIPO;
                    cmp.me.error ( Compilador.ERR_SEMANTICO, "[lista_identificadores]. Hay un error de tipo en la variable" + id.lexema + "." );
                }
            }
            // Fin Acción semántica 14

        } else {
            error ( "[lista_identificadores] Se esperaba un 'id'. Línea: " + cmp.be.preAnalisis.numLinea );
        }
    }

    // ------------------------------------------------------------------------------------------------------------------------------------------------
    
    // Implementado por: Edgar Manuel Carrillo Muruato 21130864
    // PRIMEROS ( lista_identificadores_prima ) = { , } U { empty }
    private void lista_identificadores_prima ( Atributos lista_identificadores_prima ) {
        // Variables locales
        Linea_BE id = new Linea_BE();
        Atributos dimension = new Atributos ();
        Atributos lista_identificadores_prima1 = new Atributos ();
        
        if ( preAnalisis.equals ( "," ) ) {
            // lista_identificadores’ -> , id dimension { 68 } { 69 } lista_identificadores’ { 70 }
            
            emparejar ( "," );
            id = cmp.be.preAnalisis;
            emparejar ( "id" );
            dimension ( dimension );
            
            // Acción semántica 68
            if ( analizarSemantica ) {
                if ( cmp.ts.buscaTipo ( id.entrada ).equals ( "" ) ) {
                    
                    if ( dimension.esArreglo ) {
                        cmp.ts.anadeTipo ( id.entrada, "array(0.." + dimension.longitud + ", " + lista_identificadores_prima.h + ")" );
                    } else {
                        cmp.ts.anadeTipo ( id.entrada, lista_identificadores_prima.h );
                    }
                    
                    lista_identificadores_prima.tipo = VACIO;
                    
                } else {
                    lista_identificadores_prima.tipo = ERROR_TIPO;
                    cmp.me.error ( Compilador.ERR_SEMANTICO, "[lista_identificadores_prima]. Variable " + id.lexema + " re-declarada." );
                }
                
            }
            // Fin Acción semántica 68
            
            // Acción semántica 69
            if ( analizarSemantica ) {
                lista_identificadores_prima1.h = lista_identificadores_prima.h; 
            }
            // Fin Acción semántica 69
            
            lista_identificadores_prima ( lista_identificadores_prima1 );
            
            // Acción semántica 70
            if ( analizarSemantica ) {
                if ( lista_identificadores_prima1.tipo.equals ( VACIO ) && lista_identificadores_prima.tipo.equals ( VACIO ) ) {
                    lista_identificadores_prima.tipo = VACIO;
                } else {
                    lista_identificadores_prima.tipo = ERROR_TIPO;
                    cmp.me.error ( Compilador.ERR_SEMANTICO, "[lista_identificadores_prima]. Hay errores de tipo en la asignación de variables." );
                }
            }
            // Fin Acción semántica 70

        } else {
            // lista_identificadores’ -> empty { 71 }
            
            // Acción semántica 71
            if ( analizarSemantica ) {
                lista_identificadores_prima.tipo = VACIO;
            }
            // Fin Acción semántica 71
        }
    }

    // ------------------------------------------------------------------------------------------------------------------------------------------------
    
    // Implementado por:  Paulina Jaqueline Castañeda Villalobos (21130850)
    // PRIMEROS ( declaraciones ) = { 'public' } U { 'ϵ' }
    private void declaraciones ( Atributos declaraciones ) {
        // Variables locales
        Atributos tipo = new Atributos ();
        Atributos lista_identificadores = new Atributos ();
        Atributos declaraciones1 = new Atributos ();

        retroceso = false;
        
        if ( preAnalisis.equals ( "public" ) ) {
            ptr = cmp.be.getPrt ();

            // declaraciones -> public static  tipo { 3 } lista_identificadores ; declaraciones1 { 4 }
            
            emparejar ( "public" );
            emparejar ( "static" );
            tipo ( tipo );
            
            // Acción semántica 3
            if ( analizarSemantica ) {
                lista_identificadores.h = tipo.tipo;
            }
            // Fin Acción semántica 3
            
            if ( !retroceso ) { // Si no hubo retroceso, continuamos con el procedure
                lista_identificadores ( lista_identificadores );
                
                if ( preAnalisis.equals ( ";" ) ) { // Si es ; se trata de una sentencia de declaración de variable
                    emparejar ( ";" );
                    declaraciones ( declaraciones1 );
                    
                    // Acción semántica 4
                    if ( analizarSemantica ) {
                        if ( lista_identificadores.tipo.equals ( VACIO ) && declaraciones1.tipo.equals ( VACIO ) ) {
                            declaraciones.tipo = VACIO;
                        } else {
                            declaraciones.tipo = ERROR_TIPO;
                            cmp.me.error ( Compilador.ERR_SEMANTICO, "[declaraciones]. Error de tipo en la declaración de identificadores." );
                        }
                    }
                    // Fin Acción semántica 4
                    
                } else {
                    // Limpia la columna tipo de la tabla de símbolos
                    for ( int i = 0; i <= arrayAuxiliar.size() - 1; i++ ) {
                        int idEntrada = arrayAuxiliar.get ( i );
                        cmp.ts.anadeTipo ( idEntrada, "" );
                    }
                    
                    retroceso ();
                }
            } 
        } 

        if ( retroceso ) { 
            // declaraciones -> ϵ { 5 } 
            
            // Acción semántica 5 
            if ( analizarSemantica ) {
                declaraciones.tipo = VACIO; 
            }
            // Fin Acción semántica 5 
        }
    }

    // ------------------------------------------------------------------------------------------------------------------------------------------------
    
    // Implementado por: Gael Costilla Garcia (21130923) 
    // PRIMEROS ( tipo ) = PRIMEROS ( tipo_estandar ) 
    //                   = { entero,  float,  string }
    private void tipo ( Atributos tipo ) {
        // Variable local
        Atributos tipo_estandar = new Atributos ();
        
        if ( preAnalisis.equals ( "int" ) || preAnalisis.equals ( "float" ) || preAnalisis.equals ( "string" ) ) {
            // tipo -> tipo_estandar { 6 }
            
            tipo_estandar ( tipo_estandar );
            
            // Acción semántica 6
            if ( analizarSemantica ) {
                tipo.tipo = tipo_estandar.tipo;
            } 
            // Fin Acción semántica 6

        } else if ( preAnalisis.equals ( "void" ) ) { // Si el tipo es void, corresponde a la declaración de un método.
            retroceso ();
        } else {
            error ( " [tipo] tipo de dato no reconocido ( int, float, string ). Línea: " + cmp.be.preAnalisis.numLinea );
        }
    }

    // ------------------------------------------------------------------------------------------------------------------------------------------------
    
    // Implementado por Jose Eduardo Gijon Mora  (21130883)
    // PRIMEROS ( tipo_estandar ) = { 'int' } U { 'float' } U { 'string' }
    private void tipo_estandar ( Atributos tipo_estandar ) {
        // No hay variables locales
        
        if ( preAnalisis.equals ( "int" ) ) {
            // tipo_estandar -> int { 7 }
            
            emparejar ( "int" );
            
            // Acción semántica 7
            if ( analizarSemantica ) {
                tipo_estandar.tipo = "int";                
            }
            // Fin Acción semántica 7

        } else if ( preAnalisis.equals ( "float" ) ) {
            // tipo_estandar -> float { 8 }
            
            emparejar ( "float" );
            
            // Acción semántica 8
            if ( analizarSemantica ) {
                tipo_estandar.tipo = "float";                
            }
            // Fin Acción semántica 8

        } else if ( preAnalisis.equals ( "string" ) ) {
            // tipo_estandar -> string { 9 }
            
            emparejar ( "string" );
            
            // Acción semántica 9
            if ( analizarSemantica ) {
                tipo_estandar.tipo = "string";                
            }
            // Fin Acción semántica 9

        } else {
            error ( "[tipo_estandar] ERROR: no se reconoce el token como un tipo de dato estándar. Línea: " + cmp.be.preAnalisis.numLinea );
        }
    }

    // ------------------------------------------------------------------------------------------------------------------------------------------------
    
    // Implementado por: Layla Vanessa González Martínez 21130868
    // PRIMEROS ( dimension ) = { [ } U { empty }
    private void dimension ( Atributos dimension ) {
        // Variable local
        Linea_BE num = new Linea_BE();
        
        if ( preAnalisis.equals ( "[" ) ) {
            // dimension -> [ num ] { 10 }
            
            emparejar ( "[" );
            num = cmp.be.preAnalisis;
            emparejar ( "num" );
            emparejar ( "]" );
            
            // Acción semántica 10
            if ( analizarSemantica ) {
                cmp.ts.anadeTipo ( num.entrada, "int" );
                dimension.longitud = (Integer.parseInt(num.lexema)-1)+ "" ;
                dimension.esArreglo = true;
            }
            // Fin Acción semántica 10

        } else {
            // dimension -> empty { 11 }
            
            // Acción semántica 11
            if ( analizarSemantica ) {
                dimension.longitud = null;
                dimension.esArreglo = false;
            }
            // Fin Acción semántica 11
        }
    }

    // ------------------------------------------------------------------------------------------------------------------------------------------------
    
    // Implementado por: ANA SOFIA GONZALEZ VALERIO
    private void declaraciones_metodos ( Atributos declaraciones_metodos ) {
        // Variables locales
        Atributos declaracion_metodo = new Atributos ();
        Atributos declaraciones_metodos1 = new Atributos ();

        retroceso = false;
        
        if ( preAnalisis.equals ( "public" ) ) {
            // declaraciones_metodos -> declaración_metodo declaraciones_metodos1 { 15 }
            
            declaracion_metodo ( declaracion_metodo );
            
            if ( !retroceso ) {
                declaraciones_metodos ( declaraciones_metodos1 );
                
                // Acción semántica 15
                if ( analizarSemantica ) {
                    if ( declaracion_metodo.tipo.equals ( VACIO ) && declaraciones_metodos1.tipo.equals ( VACIO ) ) {
                        declaraciones_metodos.tipo = VACIO;
                        
                    } else {
                        declaraciones_metodos.tipo = ERROR_TIPO;
                        cmp.me.error ( Compilador.ERR_SEMANTICO, "[declaraciones_metodos] Hay errores de tipo en la declaración de métodos." );
                    }
                }
                // Fin Acción semántica 15
            }
        } 
            
        if ( retroceso || !preAnalisis.equals ( "public" ) ) {
            //declaraciones_metodos -> empty { 16 }

            // Acción semántica 16
            if ( analizarSemantica ) {
                declaraciones_metodos.tipo = VACIO;
            }
            // Fin Acción semántica 16
        }

    }

    // ------------------------------------------------------------------------------------------------------------------------------------------------
    
    // Implementado por: VALERY ARACELI GUERRERO RODRIGUEZ (21130925) 
    private void declaracion_metodo ( Atributos declaracion_metodo ) {
        // Variables locales
        Atributos proposicion_compuesta = new Atributos ();
        Atributos encab_metodo = new Atributos ();
        
        if ( preAnalisis.equals ( "public" ) ) {
            // declaracion_metodo -> encab_metodo  proposición_compuesta { 17 }
            
            encab_metodo ( encab_metodo );
            
            if ( !retroceso ) {
                proposicion_compuesta ( proposicion_compuesta );
                
                // Acción semántica 17
                if ( analizarSemantica ) {
                    if ( encab_metodo.tipo.equals ( VACIO ) && proposicion_compuesta.tipo.equals ( VACIO ) ) {
                        declaracion_metodo.tipo = VACIO;
                    } else {
                        declaracion_metodo.tipo = ERROR_TIPO;
                        cmp.me.error ( Compilador.ERR_SEMANTICO, "[declaracion_metodo] Hay errores de tipo en la declaración de método." );
                    }
                }
                // Fin Acción semántica 17
            }

        } else {
            error ( "[declaracion_metodo] El programa debe iniciar con declaración de variable con la palabra reservada ( public static ). Línea: "
                    + cmp.be.preAnalisis.numLinea );
        }
    }

    // ------------------------------------------------------------------------------------------------------------------------------------------------
    
    // Implementado por: Alejandro Huerta Reyna 21130857
   private void encab_metodo(Atributos encab_metodo) {
    // Variables locales
    Linea_BE id = new Linea_BE();
    Atributos tipo_metodo = new Atributos();
    Atributos lista_parametros = new Atributos();

    retroceso = false;

    if (preAnalisis.equals("public")) {
        ptr = cmp.be.getPrt();
        // encab_metodo → public static tipo_metodo id ( lista_parametros ) { 56 }
        
        emparejar("public");
        emparejar("static");
        tipo_metodo(tipo_metodo);
        
        if (!retroceso) {
            if (preAnalisis.equals("id")) {
                id = cmp.be.preAnalisis;
                emparejar("id");
                emparejar("(");
                lista_parametros(lista_parametros);
                emparejar(")");

                if (analizarSemantica) {
                    if (cmp.ts.buscaTipo(id.entrada).equals("") && !lista_parametros.tipo.equals(ERROR_TIPO)) {
                        // Generar la firma del método: "tipo arg1, tipo arg2 -> tipo_retorno"
                        for(int i =0; i < lista_parametros.argumentos.size(); i++){
                            System.out.println(lista_parametros.argumentos.get(i));
                        }
                        
                        String firma = String.join(" X ", lista_parametros.argumentos) + " -> " + tipo_metodo.tipo;

                        // Registrar en la tabla de símbolos
                        cmp.ts.anadeTipo(id.entrada, firma);

                        encab_metodo.tipo = VACIO;
                    } else {
                        encab_metodo.tipo = ERROR_TIPO;
                        cmp.me.error(Compilador.ERR_SEMANTICO, "[encab_metodo] El método " + id.lexema + " ya ha sido declarado.");
                    }
                }




            } else {
                retroceso();
            }
        }
    } else {
        error("[encab_metodo] Error de sintaxis. Linea: " + cmp.be.preAnalisis.numLinea);
    }
}


    // ------------------------------------------------------------------------------------------------------------------------------------------------
    
    // Implementado por: Diana Laura Juarez Cordova
    private void metodo_principal ( Atributos metodo_principal ) {
        // Variables locales
        Atributos proposicion_compuesta = new Atributos ();
        Linea_BE id = new Linea_BE();

        if ( preAnalisis.equals ( "public" ) ) {
            // metodo_principal -> public static void main ( string args[] ) proposición_compuesta { 67 }
            
            emparejar ( "public" );
            emparejar ( "static" );
            emparejar ( "void" );
            emparejar ( "main" );
            emparejar ( "(" );
            emparejar ( "string" );
            emparejar ( "args" );
            emparejar ( "[" );
            emparejar ( "]" );
            emparejar ( ")" );
            proposicion_compuesta ( proposicion_compuesta );

            // Acción semántica 67
            if ( analizarSemantica ) {
                if ( proposicion_compuesta.tipo.equals ( VACIO ) ) {
                    metodo_principal.tipo = VACIO;
                } else {
                    metodo_principal.tipo = ERROR_TIPO;
                    cmp.me.error ( Compilador.ERR_SEMANTICO, "[metodo_principal]. Hay errores de tipo dentro del código del método main." );
                }
            }
            // Fin Acción semántica 67

        } else {
            error ( "[metodo_principal] - Error de sintaxis al declarar el método main. Linea: " + cmp.be.preAnalisis.numLinea );
        }
    }

    // ------------------------------------------------------------------------------------------------------------------------------------------------
    
    // Implementado por: Marcos Juárez ( 21130852 )
    // PRIMEROS ( tipo_metodo ) = { ‘void’ , ‘int’ , ‘float’ , ‘string’ }
    private void tipo_metodo ( Atributos tipo_metodo ) {
        // Variables locales
        Atributos tipo_estandar = new Atributos ();
        Atributos corchetes = new Atributos ();
        
        if ( preAnalisis.equals ( "void" ) ) {
            // tipo_metodo -> void { 57 }
            
            emparejar ( "void" );
            
            // Acción semántica 57
            if ( analizarSemantica ) {
                tipo_metodo.tipo = "void";
            }
            // Fin Acción semántica 57

        } else if ( preAnalisis.equals ( "int" ) || preAnalisis.equals ( "float" ) || preAnalisis.equals ( "string" ) ) {
            // tipo_metodo -> tipo_estandar corchetes { 58 }
            
            tipo_estandar ( tipo_estandar );
            corchetes ( corchetes );
            
            // Acción semántica 58
            if ( analizarSemantica ) {
                if ( corchetes.esArreglo == true ) {
                    tipo_metodo.tipo = "array(0.." + ", " + tipo_estandar.tipo + ")"; 
                } else {
                    tipo_metodo.tipo = tipo_estandar.tipo;
                }
            }
            // Fin Acción semántica 58

        } else {
            error ( "[tipo_metodo] - Tipo de dato incorrecto ( int, float, string ). Línea: " + cmp.be.preAnalisis.numLinea );
        }
    }

    // ------------------------------------------------------------------------------------------------------------------------------------------------
    
    // PRIMEROS ( corchetes ) = { [ } U { ϵ }
    // Implementado por: Jesus Emmanuel Llamas Hernandez - 21130904
    private void corchetes ( Atributos corchetes ) {
        // No hay variables locales
        
        if ( preAnalisis.equals ( "[" ) ) {
            // corchetes -> [] { 59 }
            
            emparejar ( "[" );
            emparejar ( "]" );
            
            // Acción semántica 59
            if ( analizarSemantica ) {
                corchetes.tipo = VACIO;
                corchetes.esArreglo = true;
            }
            // Fin Acción semántica 59
            

        } else {
            // corchetes -> ϵ { 60 }
            
            // Acción semántica 60
            if ( analizarSemantica ) {
                corchetes.tipo = VACIO;
                corchetes.esArreglo = false;
            }
            // Fin Acción semántica 60
        }
    }

    // ------------------------------------------------------------------------------------------------------------------------------------------------
    
    // Implementado por: Sergio Antonio López Delgado (21130847)
    // PRIMEROS ( lista_parametros ) = { id },  { empty }
    private void lista_parametros ( Atributos lista_parametros ) {
        // Variables locales
        Atributos tipo = new Atributos ();
        Atributos dimension = new Atributos ();
        Linea_BE id = new Linea_BE();
        Atributos lista_parametros_prima = new Atributos ();
        
        if ( preAnalisis.equals ( "int" ) || preAnalisis.equals ( "float" ) || preAnalisis.equals ( "string" ) ) {
            // lista_parametros -> tipo id dimension { 61 } lista_parametros' { 62 }
           
            tipo ( tipo );
            id = cmp.be.preAnalisis;
            emparejar ( "id" );
            dimension ( dimension );
            
            // Acción semántica 61
            if ( analizarSemantica ) {
                if (lista_parametros.argumentos == null) {
                    lista_parametros.argumentos = new ArrayList<>();
                }
                if ( cmp.ts.buscaTipo ( id.entrada ).equals ( "" ) ) {
                    if ( dimension.esArreglo == true ) {
                        cmp.ts.anadeTipo ( id.entrada, "array(0.." + dimension.longitud + ", " + tipo.tipo + ")" );
                        lista_parametros.argumentos.add("array(0.." + dimension.longitud + ", " + tipo.tipo + ")");
                    } else {
                        cmp.ts.anadeTipo ( id.entrada, tipo.tipo );
                        lista_parametros.argumentos.add(tipo.tipo + " " + id.lexema);
                    }
                    
                    lista_parametros.tipo = VACIO;
                    
                } else {
                    lista_parametros.tipo = ERROR_TIPO;
                    cmp.me.error ( Compilador.ERR_SEMANTICO, "[lista_parametros]. Variable " + id.lexema + " re-declarada." );
                }
            }
            // Fin Acción semántica 61
            
            lista_parametros_prima ( lista_parametros_prima );
            
            // Acción semántica 62
            if ( analizarSemantica ) {
                if ( lista_parametros_prima.tipo.equals ( VACIO ) && lista_parametros.tipo.equals ( VACIO ) ) {
                    lista_parametros.tipo = VACIO;
                } else {
                    lista_parametros.tipo = ERROR_TIPO;
                    cmp.me.error ( Compilador.ERR_SEMANTICO, "[lista_parametros]. Hay un error de tipo en el parámetro " + id.lexema + "." );
                }
            }
            // Fin Acción semántica 62

        } else {
            // lista_parametros -> empty { 63 }
            
            // Acción semántica 63
            if ( analizarSemantica ) {
                lista_parametros.tipo = VACIO; 
            }
            // Fin Acción semántica 63
        }
    }

    // ------------------------------------------------------------------------------------------------------------------------------------------------
    
    // Implementado por: Rodrigo Macias Ruiz (21131531 )
    // PRIMEROS ( lista_parametros ) = { int, float, string } U { empty } 
    private void lista_parametros_prima ( Atributos lista_parametros_prima ) {
        // Variables locales
        Atributos tipo = new Atributos ();
        Atributos dimension = new Atributos ();
        Linea_BE id = new Linea_BE();
        Atributos lista_parametros_prima1 = new Atributos ();
        
        if ( preAnalisis.equals ( "," ) ) {
            //lista_parametros' → tipo  id  dimension  { 64 } lista_parametros1' { 65 }
            
            emparejar ( "," );
            tipo ( tipo );
            id = cmp.be.preAnalisis;
            emparejar ( "id" );
            dimension ( dimension );
            
            // Acción semántica 64
            if ( analizarSemantica ) {
                if ( cmp.ts.buscaTipo ( id.entrada ).equals ( "" ) ) {
                    if ( dimension.esArreglo == true ) {
                        cmp.ts.anadeTipo ( id.entrada, "array(0.." + dimension.longitud + ", " + tipo.tipo + ")" );
                    } else {
                        cmp.ts.anadeTipo ( id.entrada, tipo.tipo );
                    }
                    
                    lista_parametros_prima.tipo = VACIO;
                    
                } else {
                    lista_parametros_prima.tipo = ERROR_TIPO;
                    cmp.me.error ( Compilador.ERR_SEMANTICO, "[lista_parametros_prima]. Variable " + id.lexema + " re-declarada." );
                }
            }
            // Fin Acción semántica 64
            
            lista_parametros_prima ( lista_parametros_prima1 );
            
            // Acción semántica 65
            if ( analizarSemantica ) {
                if ( lista_parametros_prima.tipo.equals ( VACIO ) && lista_parametros_prima1.tipo.equals ( VACIO ) ) {
                    lista_parametros_prima.tipo = VACIO;
                } else {
                    lista_parametros_prima.tipo = ERROR_TIPO;
                    cmp.me.error ( Compilador.ERR_SEMANTICO, "[lista_parametros_prima]. Hay un error de tipo en el parámetro " + id.lexema + "." );
                }
            }
            // Fin Acción semántica 65

        } else {
            // lista_parametros_prima -> empty { 66 }
            
            // Acción semántica 66
            if ( analizarSemantica ) {
                lista_parametros_prima.tipo = VACIO; 
            }
            // Fin Acción semántica 66
        }
    }

    // ------------------------------------------------------------------------------------------------------------------------------------------------
    
    // Implementado por: José Alejandro Martínez Escobedo - 19130939
    private void proposicion_compuesta ( Atributos proposicion_compuesta ) {
        // Variables locales
        Atributos proposiciones_optativas = new Atributos ();

        if ( preAnalisis.equals ( "{" ) ) {
            // proposiciones_optativas -> {  proposiciones_optativas  } { 18 } 
            
            emparejar ( "{" );
            proposiciones_optativas ( proposiciones_optativas );
            emparejar ( "}" );

            // Acción semántica 18
            if ( analizarSemantica ) {
                proposicion_compuesta.tipo = proposiciones_optativas.tipo;
            }
            // Fin Acción semántica 18

        } else {
            error ( "[proposicion_compuesta] Se esperaba ({). " + "Linea: " + cmp.be.preAnalisis.numLinea );
        }
    }

    // ------------------------------------------------------------------------------------------------------------------------------------------------
    
    // Implementado por: Rodrigo Mazuca Ramirez
    // PRIMEROS ( proposicion ) = PRIMEROS ( proposicion’)
    //                          = { id , if, while, {  } 
    private void proposiciones_optativas ( Atributos proposiciones_optativas ) {
        // Variables locales
        Atributos lista_proposiciones = new Atributos ();

        if ( preAnalisis.equals ( "id" ) || preAnalisis.equals ( "{" ) || preAnalisis.equals ( "if" ) || preAnalisis.equals ( "while" ) ) {
            // proposiciones_optativas -> lista_proposiciones { 19 }
            
            lista_proposiciones ( lista_proposiciones );

            // Acción semántica 19
            if ( analizarSemantica ) {
                proposiciones_optativas.tipo = lista_proposiciones.tipo;
            }
            // Fin Acción semántica 19

        } else {
            // proposiciones_optativas -> empty
            
            // Acción semántica 20
            if ( analizarSemantica ) {
                proposiciones_optativas.tipo = VACIO;
            }
            // Fin Acción semántica 20
        }

    }

    // ------------------------------------------------------------------------------------------------------------------------------------------------
    
    private void lista_proposiciones ( Atributos lista_proposiciones ) {
        // Variables locales
        Atributos proposicion = new Atributos ();
        Atributos lista_proposiciones1 = new Atributos ();
        
        if ( preAnalisis.equals ( "id" ) || preAnalisis.equals ( "{" ) || preAnalisis.equals ( "if" ) || preAnalisis.equals ( "while" ) ) {
            // lista_proposiciones -> proposición lista_proposiciones { 21 }  
            
            proposicion ( proposicion );
            lista_proposiciones ( lista_proposiciones1 );
            
            // Acción semántica 21
            if ( analizarSemantica ) {
                if ( proposicion.tipo.equals ( VACIO ) && lista_proposiciones1.tipo.equals ( VACIO ) ) {
                    lista_proposiciones.tipo = VACIO;
                } else {
                    lista_proposiciones.tipo = ERROR_TIPO;
                    cmp.me.error ( Compilador.ERR_SEMANTICO, "[lista_proposiciones]. Hay errores de tipo en el bloque de código." );
                }
            }
            // Fin Acción semántica 21

        } else {
            // lista proposiciones -> empty { 22 }
            
            // Acción semántica 22
            if ( analizarSemantica ) {
                lista_proposiciones.tipo = VACIO;
            }
            // Fin Acción semántica 22
        }
    }

    // ------------------------------------------------------------------------------------------------------------------------------------------------
    
    // Implementado por: Humberto Medina Santos (21130862)
    public void proposicion ( Atributos proposicion ) {
        if ( preAnalisis.equals ( "id" ) ) {
            // Variables locales
            Linea_BE id = new Linea_BE();
            Atributos proposicion_prima = new Atributos ();
            
            // proposicion -> id { 23 } proposicion' ; { 24 }
            id = cmp.be.preAnalisis;
            emparejar ( "id" );
            
            // Acción semántica 23
            if ( analizarSemantica ) {
                String aux = cmp.ts.buscaTipo ( id.entrada );
                
                if ( aux.equals ( "" ) ) {
                    proposicion.tipo = ERROR_TIPO;
                    cmp.me.error ( Compilador.ERR_SEMANTICO, "[proposicion]. El identificador " + id.lexema + " no ha sido declarado." );
                } else {
                    proposicion_prima.h = aux;
                }
            }
            // Fin Acción semántica 23
            
            proposicion_prima ( proposicion_prima );
            emparejar ( ";" );
            
            // Acción semántica 24
            if ( analizarSemantica ) {
                if ( proposicion_prima.tipo.equals ( ERROR_TIPO ) ) {
                    proposicion.tipo = ERROR_TIPO;
                    cmp.me.error ( Compilador.ERR_SEMANTICO, "[proposicion]. El identificador " + id.lexema + " no es compatible con el tipo de dato asigando." );
                } else {
                    proposicion.tipo = VACIO;
                }
            }
            // Fin Acción semántica 24
            
        } else if ( preAnalisis.equals ( "{" ) ) {
            // Variables locales
            Atributos proposicion_compuesta = new Atributos ();
            proposicion_compuesta ( proposicion_compuesta );
            
            // proposicion -> proposicion_compuesta { 25 }
            
            // Acción semántica 25
            if ( analizarSemantica ) {
                proposicion.tipo = proposicion_compuesta.tipo;
            }
            // Fin Acción semántica 25

        } else if ( preAnalisis.equals ( "if" ) ) {
            // Variables locales
            Atributos expresion = new Atributos ();
            Atributos proposicion1 = new Atributos ();
            Atributos proposicion2 = new Atributos ();
            
            // proposicion -> if ( expresión ) proposición1 else proposición2 { 26 }
            
            emparejar ( "if" );
            emparejar ( "(" );
            expresion ( expresion );
            emparejar ( ")" );
            proposicion ( proposicion1 );
            emparejar ( "else" );
            proposicion ( proposicion2);
            
            // Acción semántica 26
            if ( analizarSemantica ) {
                if ( expresion.tipo.equals ( BOOLEAN ) && proposicion1.tipo.equals ( VACIO ) && proposicion2.tipo.equals ( VACIO ) ) {
                    proposicion.tipo = VACIO;
                } else {
                    proposicion.tipo = ERROR_TIPO;
                    cmp.me.error ( Compilador.ERR_SEMANTICO, "[proposicion]. Hay errores de tipo en la condicional if-else" );
                }
            }
            // Fin Acción semántica 26

        } else if ( preAnalisis.equals ( "while" ) ) {
            // Variables locales
            Atributos expresion = new Atributos ();
            Atributos proposicion1 = new Atributos ();
            
            // proposicion -> while ( expresión ) proposición1 { 27 } 
            
            emparejar ( "while" );
            emparejar ( "(" );
            expresion ( expresion );
            emparejar ( ")" );
            proposicion ( proposicion1 );
            
            // Acción semántica 27
            if ( analizarSemantica ) {
                if ( expresion.tipo.equals ( BOOLEAN ) && proposicion1.tipo.equals ( VACIO ) ) {
                    proposicion.tipo = VACIO;
                } else {
                    proposicion.tipo = ERROR_TIPO;
                    cmp.me.error ( Compilador.ERR_SEMANTICO, "[proposicion]. Hay errores de tipo en el while" );
                }
            }
            // Fin Acción semántica 27

        } else {
            error ( "[proposicion] Error en la proposicion Linea:" + cmp.be.preAnalisis.numLinea );
        }
    }

    // ------------------------------------------------------------------------------------------------------------------------------------------------
    
    // Implementado por: Manuel Mijares Lara
    // PRIMEROS ( proposicion' ) = { [ } U { opasig } U { ( } U { empty };
    private void proposicion_prima ( Atributos proposicion_prima ) {
        // Variables locales
        Atributos expresion = new Atributos ();
        Atributos expresion1 = new Atributos ();
        Atributos proposicion_metodo = new Atributos ();
        
        if ( preAnalisis.equals ( "[" ) ) {
            // proposicion' -> [expresion] opasig expresion { 28 }
            
            emparejar ( "[" );
            expresion ( expresion );
            emparejar ( "]" );
            emparejar ( "opasig" );
            expresion ( expresion1 );
            
            // Acción semántica 28
            if ( analizarSemantica ) {
                if ( proposicion_prima.h.startsWith ( "array(0.." ) && proposicion_prima.h.endsWith ( expresion1.tipo + ")" ) &&  expresion.tipo.equals ( "int" ) ) {
                    proposicion_prima.tipo = VACIO;
                } else {
                    proposicion_prima.tipo = ERROR_TIPO;
                    cmp.me.error ( Compilador.ERR_SEMANTICO, "[proposicion_prima]. El id no es un arreglo o el tipo del arreglo no coincide con el tipo de la expresion." );
                }
            }
            // Fin Acción semántica 28

        } else if ( preAnalisis.equals ( "opasig" ) ) {
            //proposicion' -> opasig expresion { 29 }
            
            emparejar ( "opasig" );
            expresion ( expresion );
            
            // Acción semántica 29
            if ( analizarSemantica ) {
                if ( proposicion_prima.h.equals ( expresion.tipo ) || ( proposicion_prima.h.equals ( "float" ) && expresion.tipo.equals ( "int" ) ) ) {
                    proposicion_prima.tipo = VACIO;
                } else {
                    proposicion_prima.tipo = ERROR_TIPO;
                    cmp.me.error ( Compilador.ERR_SEMANTICO, "[proposicion_prima]. No se puede asignar un " + expresion.tipo + " a un " + proposicion_prima.h + "." );
                }
            }
            // Fin Acción semántica 29

        } else if ( preAnalisis.equals ( "(" ) ) {
            //proposicion' -> proposicion_metodo { 30 }
            
            proposicion_metodo ( proposicion_metodo );
            
            // Acción semántica 30
            if ( analizarSemantica ) {
                proposicion_prima.tipo = proposicion_metodo.tipo;
            }
            // Fin Acción semántica 30
            
        } else {
            //proposicion' -> empty { 31 }
            
            // Acción semántica 31
            if ( analizarSemantica ) {
                proposicion_prima.tipo = VACIO;
            }
            // Fin Acción semántica 31
        }
    }

    // ------------------------------------------------------------------------------------------------------------------------------------------------
    
    private void proposicion_metodo(Atributos proposicion_metodo) {
    // Variables locales
    Atributos lista_expresiones = new Atributos();
    Linea_BE id = cmp.be.preAnalisis; // Identificador del método

    if (preAnalisis.equals("(")) {
        // proposicion_metodo -> ( lista_expresiones ) { 54 }
        
        emparejar("(");
        lista_expresiones(lista_expresiones);
        emparejar(")");

        // Acción semántica 54
        if (analizarSemantica) {
            // Verificar si el identificador es un método declarado
            String tipoMetodo = cmp.ts.buscaTipo(id.entrada);
            if (tipoMetodo.isEmpty()) {
                proposicion_metodo.tipo = ERROR_TIPO;
                cmp.me.error(Compilador.ERR_SEMANTICO, "[proposicion_metodo] El identificador " + id.lexema + " no es un método declarado.");
                return;
            }

            // Verificar la firma del método en la estructura auxiliar
            List<String> firmaParametros = firmasMetodos.get(id.entrada);
            if (firmaParametros == null) {
                proposicion_metodo.tipo = ERROR_TIPO;
                cmp.me.error(Compilador.ERR_SEMANTICO, "[proposicion_metodo] No se encontraron los parámetros del método " + id.lexema + ".");
                return;
            }

            // Validar el número de argumentos proporcionados
            if (firmaParametros.size() != lista_expresiones.argumentos.size()) {
                proposicion_metodo.tipo = ERROR_TIPO;
                cmp.me.error(Compilador.ERR_SEMANTICO, "[proposicion_metodo] El método " + id.lexema + " esperaba " + firmaParametros.size() + " argumentos, pero se proporcionaron " + lista_expresiones.argumentos.size() + ".");
                return;
            }

            // Validar los tipos de cada argumento
            for (int i = 0; i < firmaParametros.size(); i++) {
                String tipoEsperado = firmaParametros.get(i);
                String tipoArgumento = lista_expresiones.argumentos.get(i);
                if (!tipoEsperado.equals(tipoArgumento) && !(tipoEsperado.equals("float") && tipoArgumento.equals("int"))) {
                    proposicion_metodo.tipo = ERROR_TIPO;
                    cmp.me.error(Compilador.ERR_SEMANTICO, "[proposicion_metodo] El argumento " + (i + 1) + " del método " + id.lexema + " esperaba un " + tipoEsperado + ", pero se recibió un " + tipoArgumento + ".");
                    return;
                }
            }

            // Establecer el tipo de retorno del método
            proposicion_metodo.tipo = tipoMetodo;
        }
        // Fin Acción semántica 54
    } else {
        // proposicion_metodo -> empty { 55 }

        // Acción semántica 55
        if (analizarSemantica) {
            proposicion_metodo.tipo = VACIO;
        }
        // Fin Acción semántica 55
    }
}




    // ------------------------------------------------------------------------------------------------------------------------------------------------
    
    private void lista_expresiones(Atributos lista_expresiones) {
    // Variables locales
    Atributos expresion = new Atributos();
    Atributos lista_expresiones_prima = new Atributos();
    lista_expresiones.argumentos = new ArrayList<>();

    if (preAnalisis.equals("id") || preAnalisis.equals("num") || preAnalisis.equals("num.num") || preAnalisis.equals("(") || preAnalisis.equals("literal")) {
        // lista_expresiones -> expresión lista_expresiones’ { 44 }
        
        expresion(expresion);
        lista_expresiones.argumentos.add(expresion.tipo); // Guardar el tipo del primer argumento

        // Analizar expresiones adicionales
        lista_expresiones_prima(lista_expresiones_prima);
        if (lista_expresiones_prima.argumentos != null) {
            lista_expresiones.argumentos.addAll(lista_expresiones_prima.argumentos); // Agregar más argumentos si existen
        }

        // Acción semántica 44
        if (analizarSemantica) {
            lista_expresiones.tipo = VACIO;
        }
        // Fin Acción semántica 44

    } else {
        // lista_expresiones -> empty { 45 }
        lista_expresiones.argumentos = new ArrayList<>(); // Ningún argumento
        if (analizarSemantica) {
            lista_expresiones.tipo = VACIO;
        }
    }
}



    // ------------------------------------------------------------------------------------------------------------------------------------------------
    
    // Implementado por: Diego Muñoz Rede (21130893)
    // PRIMEROS ( lista_expresiones') = { ',' } U { 'empty' }
    private void lista_expresiones_prima(Atributos lista_expresiones_prima) {
    // Variables locales
    Atributos expresion = new Atributos();
    Atributos lista_expresiones_prima1 = new Atributos();
    lista_expresiones_prima.argumentos = new ArrayList<>();

    if (preAnalisis.equals(",")) {
        // lista_expresiones' -> , expresion lista_expresiones' { 46 }

        emparejar(",");
        expresion(expresion);
        lista_expresiones_prima.argumentos.add(expresion.tipo);
        lista_expresiones_prima(lista_expresiones_prima1);

        // Concatenar los argumentos
        if (lista_expresiones_prima1.argumentos != null) {
            lista_expresiones_prima.argumentos.addAll(lista_expresiones_prima1.argumentos);
        }

        if (analizarSemantica) {
            lista_expresiones_prima.tipo = VACIO;
        }

    } else {
        // lista_expresiones' -> empty { 47 }
        lista_expresiones_prima.argumentos = new ArrayList<>(); // Ningún argumento adicional
        if (analizarSemantica) {
            lista_expresiones_prima.tipo = VACIO;
        }
    }
}

    // ------------------------------------------------------------------------------------------------------------------------------------------------
    
    private void expresion ( Atributos expresion ) {
        // Variables locales
        Atributos expresion_simple = new Atributos ();
        Atributos expresion_prima = new Atributos ();
        
        if ( preAnalisis.equals ( "id" ) || preAnalisis.equals ( "num" ) || preAnalisis.equals ( "num.num" ) || preAnalisis.equals ( "(" ) ) {
            // expresion -> expresión_simple { 32 } expresion’ { 33 }
            
            expresion_simple ( expresion_simple );
            
            // Acción semántica 32
            if ( analizarSemantica ) {
                expresion_prima.h = expresion_simple.tipo;
            }
            // Fin Acción semántica 32
            
            expresion_prima ( expresion_prima );
            
            // Acción semántica 33
            if ( analizarSemantica ) {
                if ( expresion_prima.tipo.equals ( VACIO ) ) {
                    expresion.tipo = expresion_simple.tipo;
                } else if ( expresion_prima.tipo.equals ( BOOLEAN ) ) {
                    expresion.tipo = BOOLEAN;
                } else {
                    expresion.tipo = ERROR_TIPO;
                    cmp.me.error ( Compilador.ERR_SEMANTICO, "[expresion]. Hay errores de tipo en la expresion." );
                }
            }
            // Fin Acción semántica 33

        } else if ( preAnalisis.equals ( "literal" ) ) {
            // expresion -> literal { 34 }
            
            Linea_BE literal = new Linea_BE();
            literal = cmp.be.preAnalisis;
            emparejar ( "literal" );
            
            // Acción semántica 34
            if ( analizarSemantica ) {
                cmp.ts.anadeTipo ( literal.entrada, "string" );
                expresion.tipo = "string";
            }
            // Fin Acción semántica 34

        } else {
            error ( "[expresion] Error en la expresion, tiene que empezar con ( id, num, num.num, ( ). Línea: " + cmp.be.preAnalisis.numLinea );
        }
    }

    // ------------------------------------------------------------------------------------------------------------------------------------------------
    
    private void expresion_prima ( Atributos expresion_prima ) {
        // Variables locales
        Atributos expresion_simple = new Atributos ();
        Linea_BE oprel = new Linea_BE();
        
        if ( preAnalisis.equals ( "oprel" ) ) {
            // expresion' -> oprel expresión_simple { 52 }
            
            oprel = cmp.be.preAnalisis;
            emparejar ( "oprel" );
            
            expresion_simple ( expresion_simple );
            
            // Acción semántica 52
            if ( analizarSemantica ) {
                if ( !expresion_simple.tipo.equals ( ERROR_TIPO ) && !expresion_prima.h.equals ( ERROR_TIPO ) ) {
                    
                    if ( !expresion_simple.tipo.equals ( "string" ) && !expresion_prima.h.equals ( "string" ) ) {
                        expresion_prima.tipo = BOOLEAN;
                    } else {
                        
                        if ( expresion_simple.tipo.equals ( "string" ) && expresion_prima.h.equals ( "string" ) ) {
                            
                            if ( oprel.lexema.equals ( "==" ) || oprel.lexema.equals ( "!=" ) ) {
                                expresion_prima.tipo = BOOLEAN;
                            } else {
                                expresion_prima.tipo = ERROR_TIPO;
                                cmp.me.error ( Compilador.ERR_SEMANTICO, "[expresion_prima]. Para compara strings debe de usar el operador relacional '==' o '!='." );
                            }
                            
                        } else {
                            expresion_prima.tipo = ERROR_TIPO;
                            cmp.me.error ( Compilador.ERR_SEMANTICO, "[expresion_prima]. No se puede comparar un " + expresion_simple.tipo + " con un " + expresion_prima.h + "." );
                        }
                    }
                    
                } else {
                    expresion_prima.tipo = ERROR_TIPO;          
                    cmp.me.error ( Compilador.ERR_SEMANTICO, "[expresion_prima]. Hay errores de tipo en la expresion." );
                }
            }
            // Fin Acción semántica 52

        } else {
            // expresion' -> empty { 53 }
            
            // Acción semántica 53
            if ( analizarSemantica ) {
                expresion_prima.tipo = VACIO;
            }
            // Fin Acción semántica 53
        }
    }

    // ------------------------------------------------------------------------------------------------------------------------------------------------
    
    // Implementado por: Miriam Alicia Sanchez Cervantes (21130882)
    // PRIMEROS ( expresion_simple ) = PRIMEROS ( termino )
    //			             = { id, num, num.num, ( }
    private void expresion_simple ( Atributos expresion_simple ) {
        // Variables locales
        Atributos termino = new Atributos ();
        Atributos expresion_simple_prima = new Atributos ();
        
        if ( preAnalisis.equals ( "id" ) || preAnalisis.equals ( "num" ) || preAnalisis.equals ( "num.num" ) || preAnalisis.equals ( "(" ) ) {
            // expresion_simple -> termino expresión_simple’ { 35 }
            
            termino ( termino );
            expresion_simple_prima ( expresion_simple_prima );
            
            // Acción semántica 35
            if ( analizarSemantica ) {
                if ( !termino.tipo.equals ( ERROR_TIPO ) && !expresion_simple_prima.tipo.equals ( ERROR_TIPO ) ) {
                    
                    if ( expresion_simple_prima.tipo.equals ( VACIO ) ) {
                        expresion_simple.tipo = termino.tipo;
                        
                    } else if ( termino.tipo.equals ( "string" ) ) {
                        expresion_simple.tipo = ERROR_TIPO;
                        cmp.me.error ( Compilador.ERR_SEMANTICO, "[expresion_simple]. No se puede sumar un string con un " + expresion_simple_prima.tipo + "." );
                        
                    } else if ( termino.tipo.equals ( "int" ) && expresion_simple_prima.tipo.equals ( "int" ) ) {
                        expresion_simple.tipo = "int";
                        
                    } else {
                        expresion_simple.tipo = "float";
                    }
                    
                } else {
                    expresion_simple.tipo = ERROR_TIPO;
                    cmp.me.error ( Compilador.ERR_SEMANTICO, "[expresion_simple]. Hay errores de tipo en la expresión." );
                }
            }
            // Fin Acción semántica 35

        } else {
            error ( "[expresion_simple] Tiene que empezar con ( id, num, num.num, ( ). Linea: " + cmp.be.preAnalisis.numLinea );
        }
    }

    // ------------------------------------------------------------------------------------------------------------------------------------------------
    
    // Implementado por : María Fernanda Torres Herrera (21130859)
    // PRIMEROS ( expresion_simple_prima ) = { 'opsuma' } U { 'empty' }
    private void expresion_simple_prima ( Atributos expresion_simple_prima ) {
        // Variables locales
        Atributos termino = new Atributos ();
        Atributos expresion_simple_prima1 = new Atributos ();
        
        if ( preAnalisis.equals ( "opsuma" ) ) {
            // expresion_simple_prima -> opsuma termino expresion_simple_prima { 50 }
            
            emparejar ( "opsuma" );
            termino ( termino );
            expresion_simple_prima ( expresion_simple_prima1 );
            
            // Acción semántica 50
            if ( analizarSemantica ) {
                if ( !termino.tipo.equals ( ERROR_TIPO ) && !expresion_simple_prima1.tipo.equals ( ERROR_TIPO ) && !termino.tipo.equals ( "string" ) ) {
                    
                    if ( expresion_simple_prima1.tipo.equals ( VACIO ) ) {
                        expresion_simple_prima.tipo = termino.tipo;
                        
                    } else if ( termino.tipo.equals ( "int" ) && expresion_simple_prima1.tipo.equals ( "int" ) ) {
                        expresion_simple_prima.tipo = "int";
                        
                    } else {
                        expresion_simple_prima.tipo = "float";
                    }
                    
                } else {
                    expresion_simple_prima.tipo = ERROR_TIPO;
                    cmp.me.error ( Compilador.ERR_SEMANTICO, "[expresion_simple_prima]. Hay errores de tipo en la expresión o está intentando sumar un string." );
                }
            }
            // Fin Acción semántica 50

        } else {
            // expresion_simple_prima -> empty { 51 }
            
            // Acción semántica 51
            if ( analizarSemantica ) {
                expresion_simple_prima.tipo = VACIO;
            }
            // Fin Acción semántica 51
        }
    }

    // ------------------------------------------------------------------------------------------------------------------------------------------------
    
    // Implementando por:  Juan Fernando Vaquera Sanchez 21130869
    // PRIMEROS ( termino )  = PRIMEROS ( factor ) 
    //                       = { id, num, num.num, ( }
    private void termino ( Atributos termino ) {
        // Variables locales
        Atributos termino_prima = new Atributos ();
        Atributos factor = new Atributos ();
        
        if ( preAnalisis.equals ( "id" ) || preAnalisis.equals ( "num" ) || preAnalisis.equals ( "num.num" ) || preAnalisis.equals ( "(" ) ) {
            // termino -> factor termino' { 36 }
            
            factor ( factor );
            termino_prima ( termino_prima );
            
            // Acción semántica 36
            if ( analizarSemantica ) {
                if ( !factor.tipo.equals ( ERROR_TIPO ) && !termino_prima.tipo.equals ( ERROR_TIPO ) ) {
                    
                    if ( termino_prima.tipo.equals ( VACIO ) ) {
                        termino.tipo = factor.tipo;
                        
                    } else if ( factor.tipo.equals ( "string" ) ) {
                        termino.tipo = ERROR_TIPO;
                        cmp.me.error ( Compilador.ERR_SEMANTICO, "[termino]. No se puede sumar un string con un " + termino_prima.tipo + "." );
                        
                    } else if ( factor.tipo.equals ( "int" ) && termino_prima.tipo.equals ( "int" ) ) {
                        termino.tipo = "int";
                        
                    } else {
                        termino.tipo = "float";
                    }
                    
                } else {
                    termino.tipo = ERROR_TIPO;
                    cmp.me.error ( Compilador.ERR_SEMANTICO, "[termino]. Hay errores de tipo en la expresión." );
                }
            }
            // Fin Acción semántica 36

        } else {
            error ( "[termino] Faltó la definición del factor al inicio del termino. Linea: " + cmp.be.preAnalisis.numLinea );
        }
    }

    // ------------------------------------------------------------------------------------------------------------------------------------------------
    
    // Implementado por: Luis Alejandro Vazquez Saucedo
    // PRIMEROS ( termino’ ) = { opmult }  
    public void termino_prima ( Atributos termino_prima ) {
        // Variables locales
        Atributos termino_prima1 = new Atributos ();
        Atributos factor = new Atributos ();
        
        if ( preAnalisis.equals ( "opmult" ) ) {
            //termino’→ opmult  factor  termino1’ { 48 }
            
            emparejar ( "opmult" );
            factor ( factor );
            termino_prima ( termino_prima1 );
            
            // Acción semántica 48
            if ( analizarSemantica ) {
                if ( !factor.tipo.equals ( ERROR_TIPO ) && !termino_prima.tipo.equals ( ERROR_TIPO ) ) {
                    
                    if ( termino_prima1.tipo.equals ( VACIO ) ) {
                        termino_prima.tipo = factor.tipo;
                        
                    } else if ( factor.tipo.equals ( "string" ) ) {
                        termino_prima.tipo = ERROR_TIPO;
                        cmp.me.error ( Compilador.ERR_SEMANTICO, "[termino_prima]. No se puede sumar un string con un " + termino_prima1.tipo + "." );
                        
                    } else if ( factor.tipo.equals ( "int" ) && termino_prima1.tipo.equals ( "int" ) ) {
                        termino_prima.tipo = "int";
                        
                    } else {
                        termino_prima.tipo = "float";
                    }
                    
                } else {
                    termino_prima.tipo = ERROR_TIPO;
                    cmp.me.error ( Compilador.ERR_SEMANTICO, "[termino_prima]. Hay errores de tipo en la expresión." );
                }
            }
            // Fin Acción semántica 48

        } else {
            // termino' -> empty { 49 }
            
            // Acción semántica 49
            if ( analizarSemantica ) {
                termino_prima.tipo = VACIO;
            }
            // Fin Acción semántica 49
            
        }
    }

    // ------------------------------------------------------------------------------------------------------------------------------------------------
    
    private void factor ( Atributos factor ) {
        // Variables locales
        Atributos expresion = new Atributos ();
        Atributos factor_prima = new Atributos ();
        
        if ( preAnalisis.equals ( "id" ) ) {
            // factor -> id { 37 } factor' { 38 }
            
            // Variable local
            Linea_BE id = new Linea_BE();
            
            id = cmp.be.preAnalisis;
            emparejar ( "id" );
            
            // Acción semántica 37
            if ( analizarSemantica ) {
                String aux = cmp.ts.buscaTipo ( id.entrada );
                
                if ( aux.equals ( "" ) ) {
                    factor.tipo = ERROR_TIPO;
                    cmp.me.error ( Compilador.ERR_SEMANTICO, "[factor]. El identificador " + id.lexema + " no ha sido declarado" );
                } else {
                    factor_prima.h = aux;
                }
            }
            // Fin Acción semántica 37
            
            factor_prima ( factor_prima );
            
            // Acción semántica 38
            if ( analizarSemantica ) {
                if ( factor_prima.tipo.equals ( ERROR_TIPO ) ) {
                    factor.tipo = ERROR_TIPO;
                    cmp.me.error ( Compilador.ERR_SEMANTICO, "[factor]. Hay errores en los tipos de datos en la expresion." );
                } else {
                    factor.tipo = cmp.ts.buscaTipo ( id.entrada );
                } 
            }
            // Fin Acción semántica 38

        } else if ( preAnalisis.equals ( "num" ) ) {
            // factor -> num { 39 }
            
            // Variable local
            Linea_BE num = new Linea_BE();
            
            num = cmp.be.preAnalisis;
            emparejar ( "num" );
            
            // Acción semántica 39
            if ( analizarSemantica ) {
                cmp.ts.anadeTipo ( num.entrada, "int" );
                factor.tipo = "int";
            }
            // Fin Acción semántica 39

        } else if ( preAnalisis.equals ( "num.num" ) ) {
            // factor -> num.num { 40 }
            
            // Variable local
            Linea_BE numnum = new Linea_BE();
            
            numnum = cmp.be.preAnalisis;
            emparejar ( "num.num" );
            
            // Acción semántica 40
            if ( analizarSemantica ) {
                cmp.ts.anadeTipo ( numnum.entrada, "float" );
                factor.tipo = "float";
            }
            // Fin Acción semántica 40

        } else if ( preAnalisis.equals ( "(" ) ) {
            // factor -> ( expresion ) { 41 }
            
            emparejar ( "(" );
            expresion ( expresion );
            emparejar ( ")" );
            
            // Acción semántica 41
            if ( analizarSemantica ) {
                factor.tipo = expresion.tipo;
            }
            // Fin Acción semántica 41

        } else {
            error ( "[factor] Se esperaba 'id', 'num', 'num.num' o '('. Linea: " + cmp.be.preAnalisis.numLinea );
        }
    }

    // ------------------------------------------------------------------------------------------------------------------------------------------------
    
    // Implementado por: Leonardo Zavala (21130874)
    // PRIMEROS ( factor’ )  = { ( } U  { empty }
    private void factor_prima ( Atributos factor_prima ) {
        // Variable local
        Atributos lista_expresiones = new Atributos ();
        
        if ( preAnalisis.equals ( "(" ) ) {
            // factor' -> ( lista_expreisones ) { 42 }
            
            emparejar ( "(" );
            lista_expresiones ( lista_expresiones );
            emparejar ( ")" );
            
            // Acción semántica 42
            if ( analizarSemantica ) {
                if ( lista_expresiones.tipo.equals ( VACIO ) ) {
                    factor_prima.tipo = factor_prima.h;
                } else {
                    factor_prima.tipo = ERROR_TIPO;
                    cmp.me.error ( Compilador.ERR_SEMANTICO, "[factor_prima] "
                            + "Hay errores en los tipos de dato en la expresion." );
                }
            }
            // Fin Acción semántica 42

        } else {
            // factor' -> empty { 43 }
            
            // Acción semántica 43
            if ( analizarSemantica ) {
                factor_prima.tipo = VACIO;
            }
            // Fin Acción semántica 43
        }
    }
}
    // ------------------------------------------------------------------------------------------------------------------------------------------------
//::

package principal;

import gui.CompiladorFrame;
import compilador.Compilador;

public class Main {
    
    //--------------------------------------------------------------------------
    
    public static void main ( String [] args ) {
        /* Set the Windows look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Windows".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(Main.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(Main.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(Main.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(Main.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                CompiladorFrame cmpFrame = new CompiladorFrame ( new Compilador () );
                cmpFrame.setColaboradoresAcercaDe ( colaboradores, 2 );
                cmpFrame.setVisible ( true );
            }
        });
        
    }

    //--------------------------------------------------------------------------
    // Nombres a desplegar en el "Acerca de" como colaboradores .
    
    public static final String [] colaboradores = { 
        "Lenguajes y Automatas II :: Grupo 8-9 am :: Semestre Ago-Dic/2024",
        "Valery Araceli Guerrero Rodriguez  (21130925)", 
        "Edgar Manuel Carrillo Muruato      (21130864)" 
    };  
   
}
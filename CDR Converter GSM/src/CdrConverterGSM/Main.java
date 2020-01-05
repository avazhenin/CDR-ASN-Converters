/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package CdrConverterGSM;

import org.apache.log4j.PropertyConfigurator;

/**
 *
 * @author vazhenin
 */
public class Main {

    /**
     * @param args the command line arguments
     *
     * 0 - help / path, from where CDRs will be loaded
     * 1 - amount of CDRs being loaded at a time
     * 2 - parameters file full path
     *
     */
    public static void main(String[] args) {

        String paramFileFullPath = null;
        String info = "";
//        System.out.println("Test");
        try {
            /* using help command */
            /* if number of incoming parameters = 1 , bot value isn't 'help' */ {
                if (args.length == 1 && args[0].toString().indexOf("help") != -1) {
                    System.err.println(info);
                } else {
                    paramFileFullPath = args[0];
                    new Worker(paramFileFullPath).run();
                }
            }
        } catch (Exception e) {
            System.err.println(e);
        }

    }

}

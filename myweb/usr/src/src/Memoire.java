import java.awt.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.DecimalFormat;

/**
 * Cette classe permet d'avoir des informations sur la mémoire d'une machine
 */
public class Memoire {

    /**
     * renvoit la mémoire vive totale de la machine.
     * @return la mémoire vive totale en bytes
     */
    public static long afficherMemoireRAMTotalMachine(){
        try {
            ProcessBuilder processBuilder = new ProcessBuilder("free", "-m");
            Process process = processBuilder.start();
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));

            String line;
            line = reader.readLine();
            line = reader.readLine(); // On lit la 2ème ligne du résultat de la commande (free) qui est la ligne de la mémoire
            reader.close();
            return Long.parseLong(line.split("\\s+")[1]);

        } catch (IOException e) {
            e.printStackTrace();
            return -1;
        }
    }

    /**
     * renvoit la mémoire vive libre complètement (buff/cache non compris)
     * @return la mémoire vive libre en bytes
     */
    public static long afficherMemoireRAMLibreMachine(){
        try {
            ProcessBuilder processBuilder = new ProcessBuilder("free", "-m");
            Process process = processBuilder.start();
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));

            String line;
            line = reader.readLine();
            line = reader.readLine(); // On lit la 2ème ligne du résultat de la commande (free) qui est la ligne de la mémoire
            reader.close();
            return Long.parseLong(line.split("\\s+")[3]);

        } catch (IOException e) {
            e.printStackTrace();
            return -1;
        }
    }

    /**
     * renvoit la mémoire disque totale de la machine.
     * @return la mémoire disque totale en bytes
     */
    public static long afficherMemoireDDTotalMachine(){
        try {
            ProcessBuilder processBuilder = new ProcessBuilder("df", "-m");
            Process process = processBuilder.start();
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));

            String line;
            line = reader.readLine();
            line = reader.readLine(); // On lit la 2ème ligne du résultat de la commande (df) qui est la ligne de la mémoire
            long sommeMemoireDD = 0;
            while(line != null){
                sommeMemoireDD += Long.parseLong(line.split("\\s+")[1]);
                line = reader.readLine();
            }
            reader.close();
            return sommeMemoireDD;

        } catch (IOException e) {
            e.printStackTrace();
            return -1;
        }
    }

    /**
     * renvoit la mémoire disque libre
     * @return la mémoire disque libre en bytes
     */
    public static long afficherMemoireDDLibreMachine(){
        try {
            ProcessBuilder processBuilder = new ProcessBuilder("df", "-m");
            Process process = processBuilder.start();
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));

            String line;
            line = reader.readLine();
            line = reader.readLine(); // On lit la 2ème ligne du résultat de la commande (free) qui est la ligne de la mémoire
            long sommeMemoireDD = 0;
            while(line != null){
                sommeMemoireDD += Long.parseLong(line.split("\\s+")[3]);
                line = reader.readLine();
            }
            reader.close();
            return sommeMemoireDD;

        } catch (IOException e) {
            e.printStackTrace();
            return -1;
        }
    }

    /**
     * renvoit le nombre de processus total (actif ou non) de la machine
     * @return le nombre de processus total
     */
    public static int afficherNbProcessus(){
        try {
            // -b est très important car cela lance le programme en mode traitement par lots ou Batch mode sinon le processus top reste actif et scanne les autres processus donc on a pas d'output
            // -n 1 permet de spécifier le nombre de lots que l'on veut que le processus fasse
            ProcessBuilder processBuilder = new ProcessBuilder("top", "-b", "-n", "1");
            Process process = processBuilder.start();
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));

            String line;
            line = reader.readLine();
            line = reader.readLine(); // On lit la 2ème ligne du résultat de la commande (free) qui est la ligne de la mémoire
            int nbProcessus = 0;
            if(line != null){
                nbProcessus = Integer.parseInt(line.split("\\s+")[1]);
            }
            reader.close();
            return nbProcessus;

        } catch (IOException e) {
            e.printStackTrace();
            return -1;
        }
    }

    /**
     * Convertit un nombre d'octets, soit en Mb, soit en Gb ou bytes si la valeur est déjà petite pour que cela soit lisible pour l'utilisateur.
     * @param bytes le nombre de bytes à convertir.
     * @return une chaine de caractères avec le nombre d'octets convertis et son unité.
     */
    public static String castBytesForHumanReadable(long bytes){

        final String[] unites = {"Mb", "Gb", "Tb"};

        int unite = 0;

        // Dès qu'on divise par 2³, soit 1024, on change l'unité associé au nombre.
        while (bytes >= 1024 && unite < unites.length - 1) {
            bytes /= 1024;
            unite++;
        }

        return bytes + " " + unites[unite];
    }
}

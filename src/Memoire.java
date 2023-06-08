import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
/**
 * Cette classe permet de 
 */
public class Memoire {

    public static long afficherMemoireRAMMachine(){
        try {
            ProcessBuilder processBuilder = new ProcessBuilder("free", "-m");
            Process process = processBuilder.start();
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));

            String line;
            line = reader.readLine();
            line = reader.readLine(); // On lit la 2ème ligne du résultat de la commande (free) qui est la ligne de la mémoire
            reader.close();
            return Long.parseLong(line.split("\\s+")[6]);

        } catch (IOException e) {
            e.printStackTrace();
            return -1;
        }
    }

    public static long afficherMemoireDDMachine(){
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

    public static String castBytesForHumanReadable(long bytes){

        final String[] unites = {"Mb", "Gb"};

        int unite = 0;

        while (bytes >= 1024 && unite < unites.length - 1) {
            bytes /= 1024;
            unite++;
        }

        return bytes + " " + unites[unite];
    }
}

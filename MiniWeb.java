import java.io.*;
import java.net.*;

public class MiniWeb {
    public static void main(String[] args) throws Exception {
        try{
            // Création du serveur socket à partir du numéro de port passé en ligne
            // d'arguments (ou pas donc 80 par défaut)
            int port;
            if (args[0] == null) {
                port = 80;
            } else {
                port = Integer.parseInt(args[0]);
            }

            ServerSocket serveur = new ServerSocket(port);

            System.out.println("Serveur en attente de connexions étrangères...");
            while (true) {
                // Socket du client correspond au navigateur, celui à qui on doit tout retourner
                // les lignes du fichier qu'il demande
                Socket socketClient = serveur.accept();

                InputStream inputStream = socketClient.getInputStream();

                PrintStream out = new PrintStream(socketClient.getOutputStream());
                // Une fois qu'on a la requête du client alors il faut la lire pour voir ce
                // qu'il veut comme ressource
                // Donc on crée un lecteur ligne par ligne qui prend en paramètre l'input du
                // client (navigateur pour le coup)
                // On a donc accès à la requête du client
                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                String ligne = reader.readLine(); // Première ligne de la requête du type : GET / HTTP/1.1
                // champs séparés par des espaces " "


                System.out.println("Requête du client : " + ligne); // Requête du client


                // l[0] contient GET, l[1] le chemin vers le fichier et l[2] la version d'HTTP
                String[] l = ligne.split(" ");

                // Si le client accède au serveur via http://ip:80 sans spécifier de page, alors on lui retourne l'index de base
                if(l[1].equals("/")){
                    l[1] = "/index.html";
                }
                // On renvoie au navigateur que sa requête est valide
                out.println("HTTP/1.1 200 OK");

                // Problème avec le favicon.ico
                if(!l[1].equals("/favicon.ico")){
                    // Lire le fichier qui a été demandé dans la requête
                    BufferedReader fich = new BufferedReader(new FileReader("."+l[1]));

                    while((ligne = fich.readLine()) != null){

                        out.println(ligne);
                    }
                    fich.close();
                }
                socketClient.close();
            }
        } catch(IOException e){
            e.printStackTrace();
        }

    }
}
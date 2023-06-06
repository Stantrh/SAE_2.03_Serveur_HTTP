import java.io.*;
import java.net.*;

public class HTTPServer {
    public static void main(String[] args) throws Exception {
        try{

            BufferedReader bfRXML = new BufferedReader(new FileReader("./config.xml")); 
            String ligne = bfRXML.readLine();
            boolean ouvertureWebConfTrouve = false;
            boolean fermetureWebConfTrouve = false;
            // Au cas où il y a des sauts de ligne inutiles au début du fichier
            while(ligne != null && !ouvertureWebConfTrouve){
                if(ligne.equals("<webconf>"))
                    ouvertureWebConfTrouve = true;
                bfRXML.readLine();
            }
            // Après avoir trouvé la balise webconf indiquant le début des lignes xml, on traite chaque ligne à l'aide de notre méthode parseXMLLine de la classe XML
            while(ligne != null && !fermetureWebConfTrouve){
                if(ligne.equals("</webconf>"))
                    fermetureWebConfTrouve = false;
                else{
                    String[] tabStrLigneC = XML.parseXMLLine(ligne); // Un tableau de la forme : [intituleXML, valeur]
                    String intituleXML = tabStrLigneC[0];
                    String valeur = tabStrLigneC[1];
                    switch (intituleXML){
                        case "port":
                            this.port = Integer.parseInt(valeur);
                            break;
                        case "root":
                            this.root = valeur;
                            break;
                        case "accept":
                            this.addrAutorisees.add(valeur);
                            break;
                        case "reject":
                            this.addrInterdites.add(valeur);
                            break;
                        case "acceslog":
                            this.accessLog(valeur);
                            break;
                        case "errorlog":
                            this.errorLog(valeur);
                            break;
                    }

                }
            }

            bfRXML.close();
            // Création du serveur socket à partir du numéro de port passé en ligne
            // d'arguments (ou pas donc 80 par défaut)
            int port;
            if (args.length == 0) {
                port = 80;
            } else {
                port = Integer.parseInt(args[0]);
            }

            // Cette variable prendra la valeur du chemin du dossier contenant les ressources web
            String cheminRessources = "Ressources";

            // On crée un socket sur le port initialisé plus haut
            ServerSocket serveur = new ServerSocket(port);

            System.out.println("Serveur en attente de connexions étrangères...");
            while (true) {
                // Socket du client correspond au navigateur, celui à qui on doit tout retourner
                // les lignes du fichier qu'il demande
                Socket socketClient = serveur.accept();

                // Nécéssaire pour connaitre la requête du client, on veut récupérer le flux en provenance de lui
                InputStream inputStream = socketClient.getInputStream();

                // On crée un flux de données binaires pour retourner au navigateur le fichier demandé (pour réussir à afficher les images)
                DataOutputStream out = new DataOutputStream(socketClient.getOutputStream());

                
                // Une fois qu'on a la requête du client alors il faut la lire pour voir ce
                // qu'il veut comme ressource
                // Donc on crée un lecteur ligne par ligne qui prend en paramètre l'input du
                // client (navigateur pour le coup)
                // On a donc accès à la requête du client
                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                ligne = reader.readLine(); // Première ligne de la requête du type : GET / HTTP/1.1
                // champs séparés par des espaces " "

                System.out.println("Requête du client : " + ligne); // Requête du client


                // l[0] contient GET, l[1] le chemin vers le fichier et l[2] la version d'HTTP
                String[] l = ligne.split(" ");

                // Si le client accède au serveur via http://ip:80 sans spécifier de page, alors on lui retourne l'index de base
                if(l[1].equals("/")){
                    l[1] = cheminRessources + "/index.html";
                }else{
                    l[1] = cheminRessources + l[1];
                }
                
                // On utilise la méthode créée préalablement pour savoir quel type de données la requête veut
                // Sinon, les images ne s'affichent pas
                String typeDonnees = avoirTypeDonnees(l[1]);

                // On renvoie au navigateur que sa requête est valide
                out.writeBytes("HTTP/1.1 200 OK\n");
                // On doit donc préciser le type des données qu'on souhaite afficher, puis le retour charriot obligatoire
                out.writeBytes("Content-Type: " + typeDonnees + "\r\n");
                out.writeBytes("\r\n");


                    // Lire le fichier qui a été demandé dans la requête
                    FileInputStream fich = new FileInputStream(new File(l[1]));

                    // On crée un tableau qui va contenir les données à donner au navigateur pour qu'il les interprète
                    byte[] res = new byte[4096];
                    int nbOctets; // Nombre d'octets qu'on devra lire pour utiliser la méthode write
                    // Si nbOctets vaut -1, on est à la fin du fichier.

                    // Tant qu'on est pas à la fin du fichier
                    while((nbOctets = (fich.read(res))) != -1){
                        out.write(res, 0, nbOctets);
                    }
                    // On ferme tous les flux
                    out.close();
                    fich.close();
                    socketClient.close();
                socketClient.close();
            }
        } catch(IOException e){
            e.printStackTrace();
        }

        
    }


    private static String avoirTypeDonnees(String cheminFichier) {
        if (cheminFichier.endsWith(".html")){
            return "text/html";
        } else if (cheminFichier.endsWith(".css")) {
            return "text/css";
        } else if (cheminFichier.endsWith(".jpg") || cheminFichier.endsWith(".jpeg")) {
            return "image/jpeg";
        } else if (cheminFichier.endsWith(".gif")) {
            return "image/gif";
        }else{
            return "";
        }
    }

    private void ecrireDansFichTxt(String line, String cheminFichDest){
        try{
            BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(cheminFichDest, true)); // l'argument true au constructeur FileWriter permet l'ajout de la ligne à la suite de ce qui est déjà dans le fichier
            bufferedWriter.write(line);
            bufferedWriter.close();
        } catch (IOException e){
            System.err.println("Erreur écriture fichier texte");;
        }
    }

    
}
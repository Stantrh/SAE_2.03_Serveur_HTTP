import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.zip.GZIPOutputStream;

public class HTTPServer {
    /**
     * Réponse du serveur à une ip non autorisée
     */
    public static final String INCONNU = "HTTP/1.1 401 Unauthorized\n";

    /**
     * Réponse du serveur à une ip inconnue
     */
    public static final String INTERDIT = "HTTP/1.1 403 Forbidden\n";

    /**
     * Réponse du serveur lorsque la ressource n'est pas trouvée
     */
    public static final String RESSOURCE_NON_TROUVEE = "HTTP/1.1 404 Not Found\n";

    /**
     * Réponse du serveur lorsque la ressource est bien trouvée et que l'ip du client est autorisée
     */

    public static final String AUTORISE = "HTTP/1.1 200 OK\n";

    /**
     * Correspond au port sur lequel le serveur écoute
     */
    private int port;

    /**
     * Liste des adresses ip autorisées
     */
    private ArrayList<String> resAutorises;

    /**
     * Liste des adresses ip non autorisées
     */
    private ArrayList<String> resInterdits;

    /**
     * Chemin du dossier contenant les fichiers web (absolu ou relatif)
     */
    private String root;

    /**
     * Chemin du fichier qui contient les logs d'accès
     */
    private String accessLog;

    /**
     * Chemin du fichier qui contient les logs d'erreurs
     */
    private String errorLog;

    /**
     * Correspond au fait qu'il y ait, ou pas des restrictions d'accès au serveur
     */
    private boolean nonRestreint;

    /**
     * Constructeur des configurations d'un serveur HTTP
     *
     * @param cheminConfig chemin du fichier config.xml
     */
    public HTTPServer(String cheminConfig) {
        try {
            // Lire ligne par ligne le fichier xml contenant la config du serveur
            BufferedReader bfRXML = new BufferedReader(new FileReader(cheminConfig));
            String ligne = bfRXML.readLine();
            boolean ouvertureWebConfTrouve = false; // --> <webconf>
            boolean fermetureWebConfTrouve = false; // --> </webconf>

            this.resAutorises = new ArrayList<String>();
            this.resInterdits = new ArrayList<String>();

            // Au cas où il y a des sauts de ligne inutiles au début du fichier
            while (ligne != null && !ouvertureWebConfTrouve) {
                if (ligne.equals("<webconf>"))
                    ouvertureWebConfTrouve = true;
                ligne = bfRXML.readLine();
            }
            // Après avoir trouvé la balise webconf indiquant le début des lignes xml, on
            // traite chaque ligne à l'aide de notre méthode parseXMLLine de la classe XML
            while (ligne != null && !fermetureWebConfTrouve) {
                if (ligne.equals("</webconf>"))
                    fermetureWebConfTrouve = false;
                else {
                    String[] tabStrLigneC = XML.parseXMLLine(ligne); // Un tableau de la forme : [champXML, valeur]
                    String intituleXML = tabStrLigneC[0];
                    //String valeur = tabStrLigneC[1];
                    switch (intituleXML) {
                        case "port":
                            if (tabStrLigneC.length == 1){
                                this.port = 80;
                            }else {
                                String valeur = tabStrLigneC[1];
                                this.port = Integer.parseInt(valeur);
                            }
                            break;
                        case "root":
                            if(tabStrLigneC.length == 1){
                                this.root = "Ressources"; // Par défaut
                            }else{
                                String valeur = tabStrLigneC[1];
                                this.root = valeur;
                            }
                            break;
                        case "accept":
                            if(tabStrLigneC.length != 1){
                                String valeur = tabStrLigneC[1];
                                this.resAutorises.add(valeur);
                            }
                            break;
                        case "reject":
                            if(tabStrLigneC.length != 1){
                                String valeur = tabStrLigneC[1];
                                this.resInterdits.add(valeur);
                            }
                            break;
                        case "acceslog":
                            if(tabStrLigneC.length == 1){
                                this.accessLog = "./accessLogs";
                            }else{
                                String valeur = tabStrLigneC[1];
                                this.accessLog = valeur;
                            }
                            break;
                        case "errorlog":
                            if(tabStrLigneC.length == 1){
                                this.errorLog = "./errorLogs";
                            }else{
                                String valeur = tabStrLigneC[1];
                                this.errorLog = valeur;
                            }
                            break;
                    }
                }
                ligne = bfRXML.readLine();
            }
            // Si les deux listes de réseaux acceptés et refusés sont vides, cela implique que le serveur n'a aucune restriction
            // et que toutes les machines peuvent s'y connecter
            this.nonRestreint = this.resAutorises.isEmpty() && this.resInterdits.isEmpty();

            bfRXML.close();
        } catch (FileNotFoundException e) {
            System.err.println("Fichier introuvable");
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    /**
     * Méthode qui permet d'avoir le type du fichier à retourner
     * @param cheminFichier correspond
     * @return le type de contenu pour l'entête de la réponse http
     */
    private static String avoirTypeDonnees(String cheminFichier) {
        if (cheminFichier.endsWith(".html")) {
            return "text/html";
        } else if (cheminFichier.endsWith(".css")) {
            return "text/css";
        } else if (cheminFichier.endsWith(".jpg") || cheminFichier.endsWith(".jpeg")) {
            return "image/jpeg";
        } else if (cheminFichier.endsWith(".gif")) {
            return "image/gif";
        } else {
            return "";
        }
    }

    /**
     * Utilisée dans le main pour simplifier la gestion des flux et ne pas avoir trop de BufferedReader
     * Elle permet d'écrire dans un fichier dont le chemin est passé en paramètres, la chaine de caractères passée aussi
     * en paramètres
     * @param line
     * @param cheminFichDest
     */
    private void ecrireDansFichTxt(String line, String cheminFichDest){
        try{
            BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(cheminFichDest, true)); // l'argument true au constructeur FileWriter permet l'ajout de la ligne à la suite de ce qui est déjà dans le fichier
            bufferedWriter.write(line);
            bufferedWriter.close();
        } catch (IOException e){
            System.err.println("Erreur écriture fichier texte");;
        }
    }

    /**
     *
     * @return
     */
    public long getMemoireThread(){
        return Runtime.getRuntime().freeMemory();
    }

    public static void genererStatusServeurHTML(){
        String memoireRAM = Memoire.castBytesForHumanReadable(Memoire.afficherMemoireRAMMachine());
        String memoireDD = Memoire.castBytesForHumanReadable(Memoire.afficherMemoireDDMachine());
        String nbProcessus = Integer.toString(Memoire.afficherNbProcessus());

        String htmlContent =
                "<html>\n" +
                        "<head>\n" +
                        "<title>Status</title>\n" +
                        "</head>\n" +
                        "<body>\n" +
                        "<h1>Memoire RAM : " + memoireRAM + "</h1>\n" +
                        "<h1>Memoire DD : " + memoireDD + "</h1>\n" +
                        "<h1>Nombre de Processus : " + nbProcessus + "</h1>\n" +
                        "</body>\n" +
                        "</html>";

        // Produis le fichier html dans le dossier Ressources
        try {
            BufferedWriter statusBW = new BufferedWriter(new FileWriter("./Ressources/status.html"));
            statusBW.write(htmlContent);
            statusBW.close();
        } catch (FileNotFoundException e1) {
            System.err.println("Les dossiers du chemin n'existent pas");
        } catch (IOException e2){
            System.err.println("Erreur écriture fichier HTML");
        }
    }

    public static void main(String[] args) throws Exception {

        try {
            BufferedWriter mywebpidBW = new BufferedWriter(new FileWriter("/var/run/myweb.pid"));
            mywebpidBW.write(Long.toString(ProcessHandle.current().pid()));
            mywebpidBW.close();
        } catch(FileNotFoundException e){
            System.err.println("Chemin de myweb.pid mauvais");
        } catch (IOException e) {
            e.printStackTrace();
        }

        genererStatusServeurHTML();

        try {
            // Pour plus de lisibilté on va utiliser un objet HTTPServer
            HTTPServer s = new HTTPServer("/etc/myweb/myweb.conf");

            // On crée un socket sur le port initialisé plus haut
            ServerSocket serveur = new ServerSocket(s.port);

            System.out.println("\u001B[34m" + "Serveur démarré... en attente de connexions..." + "\u001B[0m");


            while (true) {


                // Socket du client correspond au navigateur, celui à qui on doit tout retourner
                // les lignes du fichier qu'il demande
                Socket socketClient = serveur.accept();

                // Objet de comparation entre l'ip du client et les listes de réseaux autorisés refusés
                ComparateurIP autorisees = new ComparateurIP();
                ComparateurIP refusees = new ComparateurIP();

                // On crée un flux de données binaires pour retourner au navigateur le fichier
                // demandé (pour réussir à afficher les images)
                DataOutputStream out = new DataOutputStream(socketClient.getOutputStream());

                // Nécéssaire pour connaitre la requête du client, on veut récupérer le flux en
                // provenance de lui
                String date = java.time.LocalDateTime.now().toString();
                InputStream inputStream = socketClient.getInputStream();
                InetAddress adresseClient = socketClient.getInetAddress();

                // Une fois qu'on a la requête du client alors il faut la lire pour voir ce
                // qu'il veut comme ressource
                // Donc on crée un lecteur ligne par ligne qui prend en paramètre l'input du
                // client (navigateur pour le coup)
                // On a donc accès à la requête du client
                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                String ligne = reader.readLine(); // Première ligne de la requête du type : GET / HTTP/1.1
                // champs séparés par des espaces " "

                if (s.nonRestreint || autorisees.estInclue(socketClient.getInetAddress(), s.resAutorises)) {
                    System.out.println("Requête du client : " + ligne); // Requête du client

                    // l[0] contient GET, l[1] le chemin vers le fichier et l[2] la version d'HTTP
                    String[] l = ligne.split(" ");



                    // Pour vérifier si le fichier existe
                    File f = new File(s.root + l[1]);

                    // Si le chemin demandé est soit l'index.html (par défaut) ou alors si le fichier demandé existe
                    if(l[1].equals("/") || f.exists()){
                        // Si on est dans le cas où le client demande la racine (par défaut index.html)
                        if(l[1].equals("/")){
                            l[1] = s.root + "/index.html";
                        }else{
                            l[1] = s.root + l[1];

                        }
                        out.writeBytes(HTTPServer.AUTORISE);
                        // On récupère le type du fichier que le client veut pour agir en fonction de ce dernier
                        String typeDonnees = avoirTypeDonnees(l[1]);
                        out.writeBytes("Content-Type: " + typeDonnees + "\r\n");

                        // Lire le fichier qui a été demandé dans la requête
                        FileInputStream fich = new FileInputStream(new File(l[1]));


                        // Si le type du fichier est image, video ou son
                        String type = typeDonnees.split("/")[0];
                        if(type.equals("image") || type.equals("video") || type.equals("audio")){
                            out.writeBytes("Content-Encoding: gzip\r\n");
                            out.writeBytes("\r\n");
                            GZIPOutputStream gzip = new GZIPOutputStream(socketClient.getOutputStream());
                            byte[] res = new byte[4096];
                            int nbOctets;
                            while ((nbOctets = fich.read(res)) != -1) {
                                gzip.write(res, 0, nbOctets);
                            }
                            gzip.finish();
                            gzip.close();
                        }else{ // sinon, on ne le compresse pas
                            out.writeBytes("\r\n");
                            // On crée un tableau qui va contenir les données à donner au navigateur pour
                            // qu'il les interprète
                            byte[] res = new byte[4096];
                            int nbOctets; // Nombre d'octets qu'on devra lire pour utiliser la méthode write
                            // Si nbOctets vaut -1, on est à la fin du fichier.
                            // Tant qu'on est pas à la fin du fichier
                            while ((nbOctets = (fich.read(res))) != -1) {
                                out.write(res, 0, nbOctets);
                            }
                        }
                        fich.close();

                        s.ecrireDansFichTxt("----------------------\n" , s.accessLog);
                        s.ecrireDansFichTxt("IP du client : " + socketClient.getInetAddress() + " : " + date + "\n", s.accessLog);
                        s.ecrireDansFichTxt("Client : " + ligne + " Retour du serveur : " + HTTPServer.AUTORISE + "Elément retourné : " + l[1] + "\n", s.accessLog);
                    }else{// Si le fichier demandé n'existe pas on indique au client l'erreur 404 NOT FOUND
                        s.ecrireDansFichTxt("----------------------\n" , s.errorLog);
                        s.ecrireDansFichTxt("IP du client : " + socketClient.getInetAddress() + " : " + date + "\n", s.errorLog);;
                        s.ecrireDansFichTxt("Client : " + ligne + " Retour du serveur : " + HTTPServer.RESSOURCE_NON_TROUVEE, s.errorLog);
                        out.writeBytes(HTTPServer.RESSOURCE_NON_TROUVEE);
                    }

                }else if(refusees.estInclue(adresseClient, s.resInterdits)){// Dans le cas où l'adresse figure parmi les adresse refusées
                    s.ecrireDansFichTxt("----------------------\n", s.errorLog);
                    s.ecrireDansFichTxt("IP du client : " + socketClient.getInetAddress() + " : " + date + "\n", s.errorLog);
                    s.ecrireDansFichTxt("Client : " + ligne + " Retour du serveur : " + HTTPServer.INTERDIT, s.errorLog);
                    out.writeBytes(HTTPServer.INTERDIT);
                }
                else{// Si l'ip est tout simplement inconnue
                    s.ecrireDansFichTxt("----------------------\n", s.errorLog);
                    s.ecrireDansFichTxt("IP du client : " + socketClient.getInetAddress() + " : " + date + "\n", s.errorLog);
                    s.ecrireDansFichTxt("Client : " + ligne + " Retour du serveur : " + HTTPServer.INCONNU, s.errorLog);
                    out.writeBytes(HTTPServer.INCONNU);
                }
                socketClient.close();
            } // Fin de la boucle true
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
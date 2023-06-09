import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.zip.GZIPOutputStream;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import javax.imageio.ImageIO;

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
                                this.accessLog = "Logs/accessLogs";
                            }else{
                                String valeur = tabStrLigneC[1];
                                this.accessLog = valeur;
                            }
                            break;
                        case "errorlog":
                            if(tabStrLigneC.length == 1){
                                this.errorLog = "Logs/errorLogs";
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
    private String avoirTypeDonnees(String cheminFichier) {
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
     * Méthode très simple qui permet d'écrire dans le fichier de logs passé en paramètres.
     * Mise en forme avec IP du client, la date, sa requete, la réponse du serveur et la ressource renvoyée (si c'est le cas)
     * @param ipClient
     * @param requete
     * @param reponse
     * @param fichierlog
     * @param date
     * @param eltRetourne
     */
    public void log(String ipClient, String requete, String reponse, String fichierlog, String date, String eltRetourne){
        ecrireDansFichTxt("----------------------\n" , fichierlog);
        ecrireDansFichTxt("IP du client : " + ipClient + " : " + date + "\n", fichierlog);
        if(eltRetourne == null){
            ecrireDansFichTxt("Client : " + requete + " Retour du serveur : " + reponse, fichierlog);
        }else{
            ecrireDansFichTxt("Client : " + requete + " Retour du serveur : " + reponse + "Elément retourné : " + eltRetourne + "\n", fichierlog);
        }
        
    }

    /**
     * Méthode permettant de stocker dans un fichier logPoids.txt, le poids de chaque fichier source .java
     * Ce système permettra d'éviter de compiler à chaque fois les fichiers sources du serveur au démarrage de la machine (en vérifiant que les poids n'ont pas changé dans un bash) avant de compiler
     */
    public void enregistrerPoidsSources(){
        // On crée un File qui contient le chemin de notre répertoire où les sources sont
        File dossier = new File("src");
        // On met dans un tableau tous les fichiers (répertoires ou non)
        File[] files = dossier.listFiles();

        long poidsTotal = 0;
        try {
            // On va écrire dans un fichier texte le poids de tous les fichiers non répertoires de notre tableau
            BufferedWriter logPoidsBW = new BufferedWriter(new FileWriter("/var/log/myweb/logPoids.txt"));
            // On parcourt notre tableau
            for (File file : files) {
                // Si c'est un fichier et non pas un répertoire et que c'est un fichier .java alors on ajoute le poids du fichier courant au poids total
                if (file.isFile() && file.getName().endsWith(".java")) {
                    poidsTotal += file.length();
                }
            }
            // On écris dans le fichier logPoids.txt le poids total (car c'est beaucoup plus simple de voir s'il y a un changement dans le poids total des fichiers sources plutôt
            // que de faire correspondre chaque nom de fichier à son poids et vérifier cette correspondance dans le fichier bash.
            logPoidsBW.write(Long.toString(poidsTotal));
            logPoidsBW.close(); // On ferme bien évidemment le flux à la fin ! (qui pourrait bien oublier ça et se retrouver avec un fichier vide ??? Pas moi bien sûr)
        } catch (FileNotFoundException e1){
            System.err.println("Erreur : Les dossiers indiqués n'existent pas");
        } catch (IOException e2){
            System.err.println("Erreur lors de l'écriture du fichier logPoids.txt");
        }

    }

    /**
     * Genère l'image d'un diagramme circulaire à partir de deux nombres en bytes
     * @param memoireTotale la valeur totale (la plus grande donc)
     * @param memoirePartielle la valeur partielle (la plus petite)
     * @param cheminImageSortie le dossier où l'image doit être générée
     */
    public static void genererGraphiquePieChart(long memoireTotale, long memoirePartielle, String cheminImageSortie){
        int width = 200; // largeur image
        int height = 200; // hauteur image

        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);

        // Création d'une instance Graphics2D à partir de l'image
        Graphics2D g2d = image.createGraphics();


        // Calculer les angles pour les portions
        double total = memoireTotale + memoirePartielle;
        double angle2 = (memoireTotale / total) * 360;
        double angle1 = (memoirePartielle / total) * 360;

        // Faire un fond en blanc
        g2d.setColor(Color.WHITE);
        g2d.fillRect(0, 0, width, height);

        // Dessiner la portion la plus petite, soit memoirePartielle
        g2d.setColor(Color.GREEN);
        g2d.fillArc(50, 50, width - 100, height - 100, (int) angle1, (int) angle2);

        // Dessiner la portion la plus grande, soit memoireTotale
        g2d.setColor(Color.RED);
        g2d.fillArc(50, 50, width - 100, height - 100, 0, (int) angle1);

        // Libérer les ressources graphiques utilisées
        g2d.dispose();

        // On enregistre une image png du graphique
        File output = new File(cheminImageSortie);
        try {
            ImageIO.write(image, "png", output);
        } catch (IOException e) {
            System.err.println("Erreur lors de l'enregistrement de l'image : ");
            e.printStackTrace();
        }
    }


    /**
     * Cette méthode permet de créer un fichier status html au chemin donné en paramètre
     * Il contient les informations sur la mémoire du serveur
     * @param chemin du fichier status.html
     */
    public void genererStatusServeurHTML(String chemin){
        long memoireRAMTotale = Memoire.afficherMemoireRAMTotalMachine();
        long memoireRAMLibre = Memoire.afficherMemoireRAMLibreMachine();
        long memoireDDTotale = Memoire.afficherMemoireDDTotalMachine();
        long memoireDDLibre = Memoire.afficherMemoireDDLibreMachine();

        genererGraphiquePieChart(memoireRAMTotale, memoireRAMLibre, this.root + "/images/memoire-RAM.png");
        genererGraphiquePieChart(memoireDDTotale, memoireDDLibre, this.root + "/images/memoire-Disque.png");



        String memoireRAM = Memoire.castBytesForHumanReadable(memoireRAMLibre);
        String memoireDD = Memoire.castBytesForHumanReadable(memoireDDLibre);
        String nbProcessus = Integer.toString(Memoire.afficherNbProcessus());

        String htmlContent = "<html>\n" +
                            "<head>\n" +
                            "<title>Status</title>\n" +
                            "<style>\n" +
                            "body {\n" +
                            "    background-color: #f2f2f2;\n" +
                            "    font-family: Arial, sans-serif;\n" +
                            "    margin: 0;\n" +
                            "    padding: 0;\n" +
                            "}\n" +
                            ".container {\n" +
                            "    max-width: 600px;\n" +
                            "    margin: 0 auto;\n" +
                            "    padding: 20px;\n" +
                            "    background-color: #fff;\n" +
                            "    border: 1px solid #ccc;\n" +
                            "    border-radius: 4px;\n" +
                            "    box-shadow: 0 0 10px rgba(0, 0, 0, 0.1);\n" +
                            "}\n" +
                            "h1 {\n" +
                            "    color: #333333;\n" +
                            "    font-size: 24px;\n" +
                            "    margin-bottom: 20px;\n" +
                            "    text-align: center;\n" +
                            "}\n" +
                            ".label {\n" +
                            "    font-weight: bold;\n" +
                            "    margin-bottom: 5px;\n" +
                            "}\n" +
                            ".value {\n" +
                            "    margin-bottom: 15px;\n" +
                            "}\n" +
                            "</style>\n" +
                            "</head>\n" +
                            "<body>\n" +
                            "<div class=\"container\">\n" +
                            "    <h1>Status du Serveur</h1>\n" +
                            "    <div class=\"label\">Memoire RAM :</div>\n" +
                            "    <div class=\"value\">" + memoireRAM + "</div>\n" +
                            "    <img src=\"/images/memoire-RAM.png"+ "\" alt=\"Diagramme circulaire de la memoire vive\">\n" +
                            "    <div class=\"label\">Memoire DD :</div>\n" +
                            "    <div class=\"value\">" + memoireDD + "</div>\n" +
                            "    <img src=\"/images/memoire-Disque.png" + "\" alt=\"Diagramme circulaire de la memoire disque\">\n" +
                            "    <div class=\"label\">Nombre de Processus :</div>\n" +
                            "    <div class=\"value\">" + nbProcessus + "</div>\n" +
                            "</div>\n" +
                            "</body>\n" +
                            "</html>";
        // Produit le fichier html dans le dossier Ressources
        try {
            BufferedWriter statusBW = new BufferedWriter(new FileWriter(chemin));
            statusBW.write(htmlContent);
            statusBW.close();
        } catch (FileNotFoundException e1) {
            System.err.println("Les dossiers du chemin n'existent pas");
        } catch (IOException e2){
            System.err.println("Erreur écriture fichier HTML");
        }
    }

    /**
     * Méthode pour le code dynamique
     * Exécute le code en fonction de l'interpreteur et du code qui est passé en paramètres.
     * @param interpreteur chemin vers l'interpreteur
     * @param code code à exécuter
     * @return
     */
    public String executerCode(String interpreteur, String code) {
        try {
            // L'interpreteur arrive avec des guillemets, pour cela on doit faire
            String bonInter = interpreteur.replace("«", "").replace("»", "");
            Process process;
            String res;
            if(bonInter.equals("/bin/bash") || bonInter.equals("/usr/bin/python3")) {
                process = Runtime.getRuntime().exec(new String[] { bonInter, "-c", code });
            }else {
                return "Interpréteur : " + interpreteur + " inconnu par ce serveur";
            }
            // Ensuite on veut le résultat de la commande (qui peut faire plusieurs lignes)
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            StringBuilder output = new StringBuilder(); // Pour éviter les problèmes d'incrémentation de String toujours
            String ligne;
            while ((ligne = reader.readLine()) != null) {
                output.append(ligne).append("\n");
            }

            // On attend la fin de l'exécution du processus
            int exitCode = process.waitFor();

            if (exitCode == 0) { // si ça s'est bien passé alors on retourne
                res = output.toString(); // Retourner la sortie du processus
            } else {
                return "Erreur d'exécution : " + exitCode;
            }
            return res;
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            return "";
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

        // Pour plus de lisibilté on va utiliser un objet HTTPServer
        // Pour la version pour professeur, laisser ./config.xml car comme ça tout fonctionne depuis le dossier
        HTTPServer s = new HTTPServer("src/config.xml");

        s.enregistrerPoidsSources(); // Permet d'enregistrer la somme des poids des fichiers sources dans un fichier texte dans les logs

        try {

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
                    

                    // l[0] contient GET, l[1] le chemin vers le fichier et l[2] la version d'HTTP
                    String[] l = ligne.split(" ");
                    String nomFich = l[1];

                    // Pour vérifier si le fichier existe
                    File f = new File(s.root + l[1]);

                    // Si le chemin demandé est soit l'index.html (par défaut) ou alors si le fichier demandé existe
                    if(l[1].equals("/") || f.exists() || l[1].equals("/status.html")){
                        // Si on est dans le cas où le client demande la racine (par défaut index.html)
                        if(l[1].equals("/")){
                            l[1] = s.root + "/index.html";
                        }else if(l[1].equals("/status.html")){
                            // Génère la page qui contient les informations sur le status du serveur
                            // Comme ça, à chaque fois que le client demande cette page, les informations qui y figurent
                            // sont mises à jour.
                            l[1] = s.root + l[1];
                            s.genererStatusServeurHTML(l[1]);
                        }else{
                            l[1] = s.root + l[1];

                        }
                        out.writeBytes(HTTPServer.AUTORISE);
                        // On récupère le type du fichier que le client veut pour agir en fonction de ce dernier
                        String typeDonnees = s.avoirTypeDonnees(l[1]);
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
                        }else if(nomFich.equals("/CodeDynamique.html")){ // Etant donné qu'un seul fichier contient des balises code
                            out.writeBytes("\r\n");
                            // Alors on va devoir le lire ligne par ligne et utiliser un parseur d'html pour pouvoir obtenir le contenu
                            // Car autrement, on lit le fichier octet par octet. Là, nous allons devoir faire autrement
                            BufferedReader bf = new BufferedReader(new FileReader(l[1]));
                            // On crée un StringBuilder pour éviter les erreurs, car si on incrémente un String d'un autre String, cela recrée un nouvel objet
                            StringBuilder sb = new StringBuilder();
                            String li;
                            while ((li = bf.readLine()) != null) {
                                sb.append(li);
                                sb.append("\n"); // Ajouter un saut de ligne après chaque ligne
                            }
                            bf.close();
                            Document doc = Jsoup.parse(sb.toString());

                            Elements codes = doc.getElementsByTag("code");
                            for(Element e : codes){
                                String interpreteur = e.attr("interpreteur");
                                String code = e.text();
                                // On exécute maintenant le code et on enregistre le résultat pour remplacer l'html
                                String res = s.executerCode(interpreteur, code);
                                e.html(res);

                            }
                            String nouveauHtml = doc.html();
                            out.writeBytes(nouveauHtml);
                        }
                        else{ // sinon, on ne le compresse pas

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
                        // String requete, String reponse, String fichierlog, String date, String eltRetourne
                        System.out.println("Requête du client : " + "\u001B[32m" + ligne + "\u001B[0m"); // Requête du client
                        s.log(socketClient.getInetAddress().toString(), ligne, HTTPServer.AUTORISE, s.accessLog, date, l[1]);
                    }else{// Si le fichier demandé n'existe pas on indique au client l'erreur 404 NOT FOUND
                        System.out.println("Requête du client : " + "\u001B[31m" + ligne + "\u001B[0m"); // Requête du client
                        l[1] = null;
                        s.log(socketClient.getInetAddress().toString(), ligne, HTTPServer.RESSOURCE_NON_TROUVEE, s.errorLog, date, l[1]);
                        out.writeBytes(HTTPServer.RESSOURCE_NON_TROUVEE);
                    }

                }else if(refusees.estInclue(adresseClient, s.resInterdits)){// Dans le cas où l'adresse ip figure parmi les adresses refusées
                    s.log(socketClient.getInetAddress().toString(), ligne, HTTPServer.INTERDIT, s.errorLog, date, null);
                    out.writeBytes(HTTPServer.INTERDIT);
                }
                else{// Si l'ip est tout simplement inconnue
                    s.log(socketClient.getInetAddress().toString(), ligne, HTTPServer.INCONNU, s.errorLog, date, null);
                    out.writeBytes(HTTPServer.INCONNU);
                }
                socketClient.close();
            } // Fin de la boucle true
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
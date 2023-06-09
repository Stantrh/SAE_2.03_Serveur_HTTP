public final class XML {
    
    /**
     * Renvoit un String de la forme "NomBalise:Argument" à partir une ligne d'un fichier XML.
     * @param line
     * @return
     */
    public static String[] parseXMLLine(String line){
        String ligneIntermediaire = line.split("<")[1]; // Dans <port>80</port>, prend l'élément d'indice 1 du tableau de String donc après le < pour avoir : port>80
        String[] res = ligneIntermediaire.split(">"); // On resplit pour prendre le port et le 80 seulement on a donc un tableau ["port", "80"]
        return res;
    }
}

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;

public class ComparateurIP{


    /**
     * Méthode qui permet de savoir si une ip appartient à une liste d'ip réseaux
     * @param address adresse du client
     * @param l liste des réseaux
     * @return si l'ip appartient ou non au réseau
     * @throws UnknownHostException
     */
    public boolean estInclue(InetAddress address, ArrayList<String> l) throws UnknownHostException{
        // Méthode pour vérifier si une adresse ip est autorisée
        // Donc parcourt de la liste des réseaux et tester pour tous les réseaux tant
        // que c'est pas trouvé
        // Si l'ip appartient à un des réseaux.
        int i = 0;
        boolean trouvee = false; // Si elle n'est pas trouvee, ça veut dire qu'elle n'est pas acceptée
        while(!trouvee && i < l.size()){
            // On veut le masque sous réseau, soit la deuxième partie des ip réseau fournis
            String[] reseau = l.get(i).split("/");
            int masque = Integer.parseInt(reseau[1]);
            InetAddress res = InetAddress.getByName(reseau[0]);

            // Convertit l'ip du réseau en tableau d'octets
            byte[] reseauOctets = res.getAddress();

            // Masque de sous réseau
            int masqueSousReseau = 0xFFFFFFFF << (32 - masque);


            byte[] subnetMaskBytes = new byte[]{
                    (byte) (masqueSousReseau >>> 24),
                    (byte) (masqueSousReseau >>> 16),
                    (byte) (masqueSousReseau >>> 8),
                    (byte) masqueSousReseau
            };


            // Opération ET bit à bit entre l'ip à vérifier et le masque sous réseau
            boolean appartientTmp = true;
            byte[] addressBytes = address.getAddress();
            for (int k = 0; k < reseauOctets.length; k++) {
                if ((addressBytes[k] & subnetMaskBytes[k]) != reseauOctets[k]) {
                    appartientTmp = false;
                    break;
                }
            }
            if(appartientTmp){
                trouvee = true;
            }
            i++;
        }
        return trouvee;
    }

}
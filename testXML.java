import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class testXML{
    // public static void main(String[] args) throws IOException{
    //     BufferedReader bfRXML = new BufferedReader(new FileReader("./salut.txt")); 
    //         String ligne = bfRXML.readLine();
    //         while(ligne != null){
    //             System.out.println(ligne);
    //             ligne = bfRXML.readLine();
    //         }
    //         bfRXML.close();
    // } // Conclusion : Les /n sont biens lus mais la fermeture du fichier est différent

    public static void main(String[] args) throws IOException{
        String[] s = XML.parseXMLLine("<port>80</port>");
        System.out.println(s[0]);
        System.out.println(s[1]);
    } // Conclusion la méthode parseXMLLine de XML marche bien
}


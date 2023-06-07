import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestMemoire {

    @Test
    public void testMemoire_CastBytesForHumanReadable(){
        assertEquals("212 Gb", Memoire.castBytesForHumanReadable(217153));
    }

    @Test
    public void testMemoire_AffichageMemoire(){
        System.out.println(Memoire.castBytesForHumanReadable(Memoire.afficherMemoireDDMachine()));
    }

    @Test
    public void testMemoire_afficherNbProcessus(){
        System.out.println(Memoire.afficherNbProcessus());
    }
}

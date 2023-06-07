import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestXML{

    @Test
    public void testXML_parseXMLLine() throws IOException{
        String[] s = XML.parseXMLLine("<port>80</port>");
        System.out.println(s[0]);
        System.out.println(s[1]);
        assertEquals(s[0], "port");
        assertEquals(s[1], "80");
    }

}


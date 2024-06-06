import nl.copernicus.niklas.test.FunctionalTestCase;
import nl.copernicus.niklas.test.MockupComponentContext;
import nl.copernicus.niklas.test.MockupHeader;
import nl.copernicus.niklas.transformer.Header;
import nl.copernicus.niklas.transformer.NiklasComponentException;
import org.apache.commons.io.FileUtils;
import org.junit.Test;

import java.io.File;



public class RELLTokenTest extends FunctionalTestCase {

    private String baseURL = "https://pilotstsd365.rell.net/adfs/oauth2/";//"https://apitest.logicall.com:8443/scs/soap/adfs/oauth2/"; //"https://pilotstsd365.rell.net/adfs/oauth2/";
    private String client_id = "fc187026-5fad-44d5-b396-92af7a6345d2";
    private String resource = "https://pilotauthd365.rell.net";
    private String password = "Thr33$trik3$&Out";
    private String userName = "RELLDOM\\VCK1";

    public void testProcess(String fileName, String baseURL, String client_id, String resource, String password, String userName) throws Exception {
        File TEST_FILE = new File(fileName);

        this.setComponentContext(new MockupComponentContext());
        this.getComponentContext().getProperties().put("baseURL", baseURL);
        this.getComponentContext().getProperties().put("client_id", client_id);
        this.getComponentContext().getProperties().put("resource", resource);
        this.getComponentContext().getProperties().put("password", password);
        this.getComponentContext().getProperties().put("userName", userName);

        // initialise the transformer
        RELL_Token transformerInstance = getTransformerInstance(RELL_Token.class);

        Header hdr = new MockupHeader();
        byte[] start = FileUtils.readFileToByteArray(TEST_FILE);
        byte[] result = transformerInstance.process(hdr, start);
        assert result.equals(start);
        super.destroy(transformerInstance);
        for(String prop : hdr.getProperties().keySet()){
            System.out.println(prop +": " + hdr.getProperty(prop));
        }
    }


    @Test
    public void testCorrect() throws Exception {
        testProcess("src/test/resources/log4j.xml", baseURL, client_id, resource, password, userName);
    }

    @Test(expected = NiklasComponentException.class)
    public void testnoURL() throws Exception {
        testProcess("src/test/resources/log4j.xml", null, client_id, resource, password, userName);
    }

    @Test(expected = NiklasComponentException.class)
    public void testnoID() throws Exception {
        testProcess("src/test/resources/log4j.xml", baseURL, null, resource, password, userName);
    }

    @Test(expected = NiklasComponentException.class)
    public void testnoresource() throws Exception {
        testProcess("src/test/resources/log4j.xml", baseURL, client_id, null, password, userName);
    }

    @Test(expected = NiklasComponentException.class)
    public void testnopassword() throws Exception {
        testProcess("src/test/resources/log4j.xml", baseURL, client_id, resource, null, userName);
    }

    @Test(expected = NiklasComponentException.class)
    public void testnoName() throws Exception {
        testProcess("src/test/resources/log4j.xml", baseURL, client_id, resource, password, null);
    }
}


import com.janssengroup.https.HttpsClient;
import com.janssengroup.https.HttpsClientBuilder;
import com.janssengroup.https.HttpsResponse;
import nl.copernicus.niklas.transformer.*;
import nl.copernicus.niklas.transformer.context.ComponentContext;
import nl.copernicus.niklas.transformer.context.NiklasLogger;
import okhttp3.*;
import okhttp3.Response;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RELL_Token implements NiklasComponent<byte[], byte[]>, NiklasLoggerAware, ComponentContextAware, Lifecycle {

    protected NiklasLogger log;
    protected ComponentContext cc;
    private String baseURL;
    private String client_id;
    private String resource;
    private String password;
    private String userName;
    private List<Integer> acceptedErrors;


    @Override
    public byte[] process(Header header, byte[] payload) throws NiklasComponentException {

        try {
            //step 1: Get cookie:
            HttpsClientBuilder builder = new HttpsClientBuilder()
                    .dontRedirect();
            HttpsClient client = HttpsClient.getInstance(builder);

            RequestBody body = new FormBody.Builder()
                    .addEncoded("username", userName)
                    .addEncoded("password", password)
                    .addEncoded("AuthMethod", "FormsAuthentication")
                    .build();

            Map<String, String> headers = new HashMap<String,String>();
            headers.put("Content-Type","application/x-www-form-urlencoded");
            headers.put("Accept","*/*");
            headers.put("Cache-Control","no-cache");

            HttpUrl url = HttpUrl.parse(baseURL).newBuilder()
                    .addPathSegment("authorize")
                    .addEncodedQueryParameter("response_type", "code")
                    .addEncodedQueryParameter("client_id", client_id)
                    .addEncodedQueryParameter("redirect_uri", "http://localhost")
                    .addEncodedQueryParameter("resource", resource)
                    .addEncodedQueryParameter("response_mode", "query")
                    .build();

            Request req = new Request.Builder()
                .url(url)
                .headers(Headers.of(headers))
                .post(body)
                .build();

            HttpsResponse response = client.HttpRequest(req, acceptedErrors);

            String Cookie = response.getHeaders().get("Set-Cookie");

            //Step 2: get auth code

            headers = new HashMap<String,String>();
            headers.put("Cookie", Cookie);
            body = new FormBody.Builder().build();
            req = new Request.Builder()
                    .url(url)
                    .headers(Headers.of(headers))
                    .method("POST", body)
                    .build();

            response = client.HttpRequest(req, acceptedErrors);

            String authCode = HttpUrl.parse(response.getHeaders().get("Location")).queryParameter("code");

            //step3: get access token
            url = HttpUrl.parse(baseURL).newBuilder()
                    .addPathSegment("token")
                    .build();

            headers = new HashMap<String,String>();
            headers.put("Content-Type","application/x-www-form-urlencoded");

            body = new FormBody.Builder()
                    .add("grant_type", "authorization_code")
                    .add("client_id", client_id)
                    .add("redirect_uri", "http://localhost")
                    .add("code", authCode)
                    .build();

            req = new Request.Builder()
                    .url(url)
                    .headers(Headers.of(headers))
                    .method("POST",body)
                    .build();

            ResponseBody responseBody = client.HttpRequest(ResponseBody.class, req);

            // Set access_token to header

            header.setProperty("Authorization", "Bearer " + responseBody.getAccess_token());

        }catch (Exception e){
            throw new NiklasComponentException(e);
        }

        return payload;
    }


    @Override
    public void setLogger(NiklasLogger nl) {
        this.log = nl;
    }

    @Override
    public void setComponentContext(ComponentContext cc) {
        this.cc = cc;
    }

    @Override
    public void initialise() throws NiklasComponentException {
        baseURL = cc.getProperty("baseURL");
        if(baseURL == null){
            throw new NiklasComponentException("Manditory property baseURL not set!");
        }
        client_id = cc.getProperty("client_id");
        if(client_id == null){
            throw new NiklasComponentException("Manditory property client_id not set!");
        }
        resource = cc.getProperty("resource");
        if(resource == null){
            throw new NiklasComponentException("Manditory property resource not set!");
        }
        password = cc.getProperty("password");
        if(password == null){
            throw new NiklasComponentException("Manditory property password not set!");
        }
        userName = cc.getProperty("userName");
        if(userName == null){
            throw new NiklasComponentException("Manditory property userName not set!");
        }
        acceptedErrors = new ArrayList<>();
        acceptedErrors.add(302);
    }

    @Override
    public void destroy() {

    }
}


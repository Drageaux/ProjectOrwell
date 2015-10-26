package service;

import javax.net.ssl.HttpsURLConnection;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.URL;

/**
 * Created by Lance on 10/25/2015.
 */
public class WebhookService {
    private final String USER_AGENT = "Mozilla/5.0";

    public void createWebhook() throws Exception {

        String url = "a.wunderlist.com/api/v1/webhoooks";
        URL obj = new URL(url);
        HttpsURLConnection con = (HttpsURLConnection) obj.openConnection();

        //add request header
        con.setRequestMethod("POST");
        con.setRequestProperty("User-Agent", USER_AGENT);
        con.setRequestProperty("Accept-Language", "en-US,en;q=0.5");
        String urlParameters = "list_id%3D1101010%26url%3Dhttps%3A%2F%2Ffoo.bar.chadfowler.com%2Fstruts%2Fasdf.do%26caller%26processor_type%3Dgeneric%26configuration%3D";
//                "list_id=1101010&" +
//                "url=https://foo.bar.chadfowler.com/struts/asdf.do&caller&" +
//                "processor_type=generic&" +
//                "configuration=";

        // Send post request
        con.setDoOutput(true);
        DataOutputStream wr = new DataOutputStream(con.getOutputStream());
        wr.writeBytes(urlParameters);
        wr.flush();
        wr.close();

        int responseCode = con.getResponseCode();
        System.out.println("\nSending 'POST' request to URL : " + url);
        System.out.println("Post parameters : " + urlParameters);
        System.out.println("Response Code : " + responseCode);

        BufferedReader in = new BufferedReader(
                new InputStreamReader(con.getInputStream()));
        String inputLine;
        StringBuffer response = new StringBuffer();

        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();

        //print result
        System.out.println(response.toString());
    }
}

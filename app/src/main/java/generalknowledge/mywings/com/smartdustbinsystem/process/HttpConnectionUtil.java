package generalknowledge.mywings.com.smartdustbinsystem.process;

import org.json.JSONObject;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;


public class HttpConnectionUtil {

    public String requestGet(String url) {
        try {
            URL uri = new URL(url);
            HttpURLConnection httpURLConnection = (HttpURLConnection) uri.openConnection();
            httpURLConnection.setRequestProperty("Content-Type", "application/json");
            httpURLConnection.setRequestProperty("Accept", "application/json");
            httpURLConnection.setRequestMethod("GET");
            httpURLConnection.connect();
            int responseCode = httpURLConnection.getResponseCode();
            if (responseCode == 200) {
                InputStream inputStream = new BufferedInputStream(httpURLConnection.getInputStream());
                return convertStreamToString(inputStream);
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public String requestPost(String url, JSONObject request) {

        try {
            URL uri = new URL(url);
            HttpURLConnection httpURLConnection = (HttpURLConnection) uri.openConnection();
            httpURLConnection.setRequestProperty("Content-Type", "application/json");
            httpURLConnection.setRequestProperty("Accept", "application/json");
            httpURLConnection.setRequestMethod("POST");
            DataOutputStream dataOutputStream = new DataOutputStream(httpURLConnection.getOutputStream());
            dataOutputStream.writeBytes(request.toString());
            dataOutputStream.flush();
            dataOutputStream.close();
            httpURLConnection.connect();
            int responseCode = httpURLConnection.getResponseCode();
            if (responseCode == 200) {
                InputStream inputStream = new BufferedInputStream(httpURLConnection.getInputStream());
                return convertStreamToString(inputStream);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }


        return null;
    }


    private String convertStreamToString(InputStream inputStream) throws IOException {
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
        String line = "";
        String result = "";
        while ((line = bufferedReader.readLine()) != null) {
            result += line;
        }
        if (null != inputStream) inputStream.close();
        return result;
    }

}

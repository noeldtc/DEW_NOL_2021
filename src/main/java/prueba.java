import okhttp3.*;
import org.json.JSONObject;

import java.io.IOException;

public class prueba {
    public static void main(String[] args)  {
        String url = "http://dew-amongab-2122.dsicv.upv.es:9090/CentroEducativo/login";
        OkHttpClient httpClient = new OkHttpClient.Builder().build();
        JSONObject authdata = new JSONObject();
        authdata.put("dni", "12345678W");
        authdata.put("password", "123456");
        RequestBody body = RequestBody.create(MediaType.parse("application/json"), authdata.toString());
        Request requestHttp = new Request.Builder().url(url).addHeader("content-type", "application/json").post(body).build();
        Response responseAPI = null;
        try {
            responseAPI = httpClient.newCall(requestHttp).execute();
            if (responseAPI.isSuccessful()){
                System.out.println(responseAPI.body().string());
            }
        } catch (IOException e) {
            try {
                Thread.sleep(2000);
                main(args);
            } catch (InterruptedException ex) {
                throw new RuntimeException(ex);
            }
        }


    }



}

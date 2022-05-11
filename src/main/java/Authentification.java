
import okhttp3.*;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.servlet.ServletException;
import javax.servlet.http.*;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

public class Authentification extends HttpServlet {
    private Map<String, String[]> map = new HashMap<String, String[]>();
    public Authentification() {
        super();
    }


    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String nombreMaquina = request.getServerName();
        //Cambiar por .getServletContext().getInitParameter("credspath");
        String jcontent = new String(Files.readAllBytes(Paths.get("/home/user/test.json")));
        JSONArray credentials = new JSONArray(jcontent);
        for (int i = 0; i < credentials.length(); i++) {
            JSONObject temp = credentials.getJSONObject(i);
            map.put(temp.getString("login"), new String[]{temp.getString("DNI"), temp.getString("password")});
        }

        String url = "http://" + nombreMaquina + ":9090/CentroEducativo/login";
        HttpSession session = request.getSession(false);
        if (session == null) {
            if (map.containsKey(request.getRemoteUser())) {
                String[] data = map.get(request.getRemoteUser());
                session.setAttribute("dni", data[0]);
                session.setAttribute("password", data[1]);
                JSONObject authdata = new JSONObject();
                authdata.put("dni", data[0]);
                authdata.put("password", data[1]);
                OkHttpClient httpClient = new OkHttpClient.Builder().build();
                RequestBody body = RequestBody.create(MediaType.parse("application/json"), authdata.toString());
                Request requestHttp = new Request.Builder().url(url).addHeader("content-type", "application/json").post(body).build();
                Response responseAPI = httpClient.newCall(requestHttp).execute();
                if (responseAPI.isSuccessful()) {
                    session.setAttribute("key", responseAPI.body().toString());
                }

            }
        }
    }
}
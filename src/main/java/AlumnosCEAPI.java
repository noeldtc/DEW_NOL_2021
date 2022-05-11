import okhttp3.CookieJar;
import okhttp3.OkHttpClient;

import okhttp3.Request;
import okhttp3.Response;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.*;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class AlumnosCEAPI extends HttpServlet {

    private final String QUERRY_PARAM = "args";
    private final String DNI_PARAM = "dni";
    private final String KEY_PARAM = "key";

    private final String ROL_ALU="rolalu";
    private final String ASIG_PATH = "asignaturas";
    private final String DETALLES_ASIG_PATH = "detallesasig";
    private final String CERT_PATH = "cert";
    private final String ACRONIMO_ASIG_PARAM = "acron";
    private final String ERR_ASIG = "La asignatura solicitada o no existe o el alumno no está matriculado en ella.";
    private final String ERR_DEFAULT = "No tienes permisos";
    private final int ERROR_CODE= 403;
    public AlumnosCEAPI() {
        super();
    }
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String nombreMaquina = request.getServerName();
        HttpSession session = request.getSession(false);
        String dni = session.getAttribute(DNI_PARAM).toString();
        String key = session.getAttribute(KEY_PARAM).toString();

        //Se inicia el cliente okhttp
        OkHttpClient httpClient = new OkHttpClient.Builder().build();

        String url = "";
        if(request.isUserInRole(ROL_ALU)) {
            String param = request.getParameter(QUERRY_PARAM);
            response.setContentType("application/json");
            if(param.equals(ASIG_PATH)) {
                //Consultar la lista de asignaturas
                //en las que está matriculada
                url = "http://"+nombreMaquina+":9090/CentroEducativo/alumnos/"+dni+"/asignaturas?key="+key;
            } else if(param.equals(DETALLES_ASIG_PATH)) {
                //Consulta la nota obtenida en una asignatura
                String acronimo = request.getParameter(ACRONIMO_ASIG_PARAM);
                //Consulta las notas de la asignatura
                Request consulta_notas = new Request.Builder().url("http://"+nombreMaquina+":9090/CentroEducativo/asignaturas/"+acronimo+"/alumnos?key="+key).build();
                Response responseNotas = httpClient.newCall(consulta_notas).execute();
                if(responseNotas.isSuccessful()){
                    JSONArray notas = new JSONArray(responseNotas.body().string());
                    for(int i =0; i < notas.length() && notas.getJSONObject(i).has("dni");i++){
                        String dni_alumno = notas.getJSONObject(i).getString("dni");
                        if(dni_alumno.equals(dni)){
                            //Es este la nota del alumno
                            response.getWriter().append(notas.getJSONObject(i).getInt("nota")+"");
                            return;
                        }
                    }
                }
                responseNotas.close();
                //No se ha encontrado el usuario en la asignatura
                response.sendError(ERROR_CODE, ERR_ASIG);

            }else if(param.equals(CERT_PATH)) {
                //TODO Creación del certificado

            }

            if(!url.equals("")) {
                Request requestHttp = new Request.Builder().url(url).addHeader("content-type","application/json").build();
                Response responseAPI = httpClient.newCall(requestHttp).execute();
                String content = "-1";
                if(responseAPI.isSuccessful()) {
                    content = responseAPI.body().toString();
                    responseAPI.close();
                    response.getWriter().append(content);

                }else {
                    response.getWriter().append(responseAPI.code()+"");
                }

            }
            else {
                response.setStatus(ERROR_CODE);
                response.getWriter().append(ERR_DEFAULT);
                return;
            }
        }

    }






    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doGet(request, response);
    }




}

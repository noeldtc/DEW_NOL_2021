import okhttp3.OkHttpClient;

import okhttp3.Request;
import okhttp3.Response;
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

    private Map<String,String> asigAlumnos;
    private final String DEFAULT_ROLE = "algo";
    private final String QUERRY_PARAM = "args";
    private final String DNI_PARAM = "dni";
    private final String KEY_PARAM = "key";
    private final String AVATAR_PATH = "avatar";
    private final String ASIG_PATH = "asignaturas";
    private final String DETALLES_ASIG_PATH = "detallesasig";
    private final String DNI_PATH = "dni";
    private final String PROF_ASIG_PATH = "profsasig";

    private final String PATH_IMAGES = "/WEB-INF/img";
    private final String ACRONIMO_ASIG_PARAM = "acron";
    private final String ERR_ASIG = "La asignatura solicitada o no existe o el alumno no está matriculado en ella.";
    private final String ERR_DEFAULT = "No tienes permisos";

    public AlumnosCEAPI() {
        super();
        this.asigAlumnos= new ConcurrentHashMap<>();
    }
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String nombreMaquina = request.getServerName();
        HttpSession session = request.getSession(false);
        List<Cookie> cookies = (List<Cookie>) session.getAttribute("cookie");
        //TODO manejar las cookies
        String dni = session.getAttribute(DNI_PARAM).toString();
        String key = session.getAttribute(KEY_PARAM).toString();

        //Se inicia el cliente okhttp
        OkHttpClient httpClient = new OkHttpClient.Builder().build();
        String url = "";
        if(request.isUserInRole(DEFAULT_ROLE)) {
            String param = request.getParameter(QUERRY_PARAM);
            response.setContentType("application/json");
            if(param.equals(AVATAR_PATH)) {
                JSONObject responseJSON = new JSONObject();

                ServletContext context = getServletContext();
                String pathToAvatar = context.getRealPath(PATH_IMAGES);
                response.setContentType("text/plain");
                response.setCharacterEncoding("UTF-8");
                BufferedReader origen = new BufferedReader(new FileReader(pathToAvatar+"/"+dni+".pngb64"));

                responseJSON.put(DNI_PARAM,dni);
                PrintWriter out = response.getWriter();
                String linea ="";
                while ((linea += origen.readLine()) != null);
                responseJSON.put("img",linea);
                out.println(responseJSON.toString());
                out.close(); origen.close();
            }else if(param.equals(ASIG_PATH)) {
                url = "http://"+nombreMaquina+":9090/CentroEducativo/alumnos/"+dni+"/asignaturas?key="+key;

            } else if(param.equals(DNI_PATH)) {
                url = "http://"+nombreMaquina+":9090/CentroEducativo/alumnos/"+dni+"?key="+key;
            } else if(param.equals(DETALLES_ASIG_PATH)) {
                String acronimo = request.getParameter(ACRONIMO_ASIG_PARAM);
                if(alumnoIsInAsig(acronimo,dni)) {
                    url = "http://"+nombreMaquina+":9090/CentroEducativo/asignaturas/"+acronimo+"?key="+key;
                }else {
                    response.sendError(403, ERR_ASIG);
                }
            }else if(param.equals(PROF_ASIG_PATH)) {
                String acronimo = request.getParameter(ACRONIMO_ASIG_PARAM);
                if(alumnoIsInAsig(acronimo,dni)) {
                    url = "http://"+nombreMaquina+":9090/CentroEducativo/asignaturas/"+acronimo+"/profesores?key="+key;
                }else {
                    response.sendError(403, ERR_ASIG);
                }
            }

            if(!url.equals("")) {
                Request requestHttp = new Request.Builder().url(url).addHeader("content-type","application/json").build();
                //TODO añadir la cookie de la peticion al cliente.
                Response responseAPI = httpClient.newCall(requestHttp).execute();
                String content = "-1";
                if(responseAPI.isSuccessful()) {
                    content = responseAPI.body().toString();
                    if(alumnoIsSaved(dni) && param.equals("asignaturas")) {
                        saveAlumno(dni, content);
                    }
                    responseAPI.close();
                    response.getWriter().append(content);

                }else {
                    response.getWriter().append(responseAPI.code()+"");
                }

            }
            else {
                response.setStatus(401);
                response.getWriter().append(ERR_DEFAULT);
                return;
            }
        }

    }

    private boolean alumnoIsInAsig(String acron_asig,String dni){
        return asigAlumnos.containsKey(dni) && asigAlumnos.get(dni).contains(acron_asig);
    }
    private boolean alumnoIsSaved(String dni){
        return asigAlumnos.containsKey(dni);
    }

    private void saveAlumno(String dni,String acron_asig){
        if(asigAlumnos.containsKey(dni)) asigAlumnos.put(dni,asigAlumnos.get(dni) + ","+acron_asig);
        else asigAlumnos.put(dni,acron_asig);
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doGet(request, response);
    }




}

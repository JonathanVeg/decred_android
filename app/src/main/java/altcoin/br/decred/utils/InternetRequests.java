package altcoin.br.decred.utils;

import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

import java.util.HashMap;
import java.util.Map;

import altcoin.br.decred.application.MyApplication;

public class InternetRequests {
    /*
    * Para usá-la, add isso no build.gradle
    *
    * compile 'com.mcxiaoke.volley:library:1.0.17'
    *
    * MyApplication é uma classe declarada como application no manifest que salva a instancia da RequestQueue.
    *
    * basicamente, se ela n existe é criada e se existe ele retorna ela, garantindo uma instancia só pra toda a aplicação
    * */

    // cria um Listener vazio para erro, caso a chamada da função não passe um (obrigatório)
    private static final Response.ErrorListener emptyErrorListener = new Response.ErrorListener() {
        @Override
        public void onErrorResponse(VolleyError error) {
            Utils.log("VolleyError: " + error.toString());
        }
    };

    // cria um Listener vazio para respostas com sucesso, caso a chamada da função não passe um (obrigatório)
    private static final Response.Listener emptyResponseListener = new Response.Listener<String>() {
        @Override
        public void onResponse(String response) {
        }
    };

    private final Map<String, String> params;
    private final String tag;
    private final Map<String, String> headers;

    public InternetRequests() {
        params = new HashMap<>();
        headers = new HashMap<>();

        tag = "InternetRequests";
    }

    public void executeGet(String url, Response.Listener responseListener) {
        executeGet(url, responseListener, null);
    }

    private void executeGet(String url, Response.Listener responseListener, Response.ErrorListener errorListener) {
        executeRequest(Request.Method.GET, url, responseListener, errorListener, params);
    }

    public void executePost(String url, Response.Listener responseListener) {
        executePost(url, responseListener, null);
    }

    private void executePost(String url, Response.Listener responseListener, Response.ErrorListener errorListener) {
        executeRequest(Request.Method.POST, url, responseListener, errorListener, params);
    }

    // mais completo, onde pode mandar tudo.
    private void executeRequest(int method, String url, Response.Listener responseListener, Response.ErrorListener errorListener, final Map<String, String> params) {
        try {
            if (responseListener == null) responseListener = emptyResponseListener;

            if (errorListener == null) errorListener = emptyErrorListener;

            RequestQueue rq = MyApplication.getInstance().getRequestQueue();

            StringRequest request = new StringRequest(method,
                    url,
                    responseListener,
                    errorListener
            ) {
                @Override
                public Map<String, String> getParams() throws AuthFailureError {
                    return params;
                }

                @Override
                public Priority getPriority() {
                    return (Priority.HIGH);
                }

                @Override
                public Map<String, String> getHeaders() throws AuthFailureError {
                    return headers;
                }
            };

            request.setRetryPolicy(new DefaultRetryPolicy(
                    45000,
                    DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                    DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

            request.setTag(tag);

            rq.add(request);
        } catch (Exception e) {
            Log.e("executePost", "Erro ao executar URL: " + url);

            e.printStackTrace();
        }
    }
}

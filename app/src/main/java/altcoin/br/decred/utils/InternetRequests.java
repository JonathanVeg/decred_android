package altcoin.br.decred.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
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

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

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
    private static Response.ErrorListener emptyErrorListener = new Response.ErrorListener() {
        @Override
        public void onErrorResponse(VolleyError error) {
            Utils.log("VolleyError: " + error.toString());
        }
    };

    // cria um Listener vazio para respostas com sucesso, caso a chamada da função não passe um (obrigatório)
    private static Response.Listener emptyResponseListener = new Response.Listener<String>() {
        @Override
        public void onResponse(String response) {
        }
    };

    private Map<String, String> params;
    private String tag;
    private Map<String, String> headers;

    public InternetRequests() {
        params = new HashMap<>();
        headers = new HashMap<>();

        tag = "InternetRequests";
    }

    // verifica se o aparelho possui conexão com a internet no momento.
    public static boolean isOnline(Context context) {
        if (context == null) return false;

        ConnectivityManager cm = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();

        return netInfo != null && netInfo.isConnectedOrConnecting();
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    // parâmetros para requisição
    public void setParams(Map<String, String> params) {
        this.params = params;
    }

    public void setHeaders(Map<String, String> headers) {
        this.headers = headers;
    }

    // mesmo da função acima, mas invez de mandar um HashMap pode se mandar Strings e ele prepara o map internamente.
    // obviamente precisa de um numero par de parametros (mas n tratei isso ainda)
    // Exemplo:
    // makeParams("nome", "Jonathan", "idade", "22")
    public void makeParams(String... vars) {
        params = new HashMap<>();

        for (int i = 0; i < vars.length; i += 2)
            params.put(vars[i], vars[i + 1]);
    }

    public void makeHeaders(String... vars) {
        headers = new HashMap<>();

        for (int i = 0; i < vars.length; i += 2)
            headers.put(vars[i], vars[i + 1]);
    }

    // adiciona paramestro a lista já criada, mesma lógica do item acima.
    public void addParam(String... vars) {
        for (int i = 0; i < vars.length; i += 2) params.put(vars[i], vars[i + 1]);
    }

    public void addHeader(String... vars) {
        for (int i = 0; i < vars.length; i += 2) headers.put(vars[i], vars[i + 1]);
    }

    // modos de executar um post.
    public void executeGet(String url) {
        executeGet(url, null, null);
    }

    public void executeGet(String url, Response.Listener responseListener) {
        executeGet(url, responseListener, null);
    }

    public void executeGet(String url, Response.Listener responseListener, Response.ErrorListener errorListener) {
        executeRequest(Request.Method.GET, url, responseListener, errorListener, params);
    }

    // modos de executar um post.
    public void executePost(String url) {
        executePost(url, null, null);
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

    private static String hmacDigest(String msg, String keyString, String algo) {
        String digest = null;
        try {
            SecretKeySpec key = new SecretKeySpec(
                    (keyString).getBytes("UTF-8"), algo);
            Mac mac = Mac.getInstance(algo);
            mac.init(key);

            byte[] bytes = mac.doFinal(msg.getBytes("ASCII"));

            StringBuilder hash = new StringBuilder();
            for (byte aByte : bytes) {
                String hex = Integer.toHexString(0xFF & aByte);

                if (hex.length() == 1)
                    hash.append('0');

                hash.append(hex);
            }

            digest = hash.toString();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return digest;
    }

    public static String hmacDigest(String msg, String keyString) {
        return hmacDigest(msg, keyString, "HmacSHA512");
    }
}

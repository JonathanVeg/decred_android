package altcoin.br.decred.utils;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.text.Html;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.text.style.URLSpan;
import android.util.Log;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.crashlytics.android.answers.Answers;
import com.crashlytics.android.answers.CustomEvent;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;

import static android.content.Context.CLIPBOARD_SERVICE;

public class Utils {
    public static double eval(EditText et) {
        if (et != null)
            return eval(et.getText().toString());
        
        return 0.0;
    }
    
    public static double eval(final String str) {
        if (str.isEmpty())
            return 0.0;
        
        return new Object() {
            int pos = -1, ch;
            
            void nextChar() {
                ch = (++pos < str.length()) ? str.charAt(pos) : -1;
            }
            
            boolean eat(int charToEat) {
                while (ch == ' ') nextChar();
                if (ch == charToEat) {
                    nextChar();
                    return true;
                }
                return false;
            }
            
            double parse() {
                nextChar();
                double x = parseExpression();
                if (pos < str.length()) throw new RuntimeException("Unexpected: " + (char) ch);
                return x;
            }
            
            // Grammar:
            // expression = term | expression `+` term | expression `-` term
            // term = factor | term `*` factor | term `/` factor
            // factor = `+` factor | `-` factor | `(` expression `)`
            //        | number | functionName factor | factor `^` factor
            
            double parseExpression() {
                double x = parseTerm();
                for (; ; ) {
                    if (eat('+')) x += parseTerm(); // addition
                    else if (eat('-')) x -= parseTerm(); // subtraction
                    else return x;
                }
            }
            
            double parseTerm() {
                double x = parseFactor();
                for (; ; ) {
                    if (eat('*')) x *= parseFactor(); // multiplication
                    else if (eat('/')) x /= parseFactor(); // division
                    else return x;
                }
            }
            
            double parseFactor() {
                if (eat('+')) return parseFactor(); // unary plus
                if (eat('-')) return -parseFactor(); // unary minus
                
                double x;
                int startPos = this.pos;
                if (eat('(')) { // parentheses
                    x = parseExpression();
                    eat(')');
                } else if ((ch >= '0' && ch <= '9') || ch == '.') { // numbers
                    while ((ch >= '0' && ch <= '9') || ch == '.') nextChar();
                    x = Double.parseDouble(str.substring(startPos, this.pos));
                } else if (ch >= 'a' && ch <= 'z') { // functions
                    while (ch >= 'a' && ch <= 'z') nextChar();
                    String func = str.substring(startPos, this.pos);
                    x = parseFactor();
                    if (func.equals("sqrt")) x = Math.sqrt(x);
                    else if (func.equals("sin")) x = Math.sin(Math.toRadians(x));
                    else if (func.equals("cos")) x = Math.cos(Math.toRadians(x));
                    else if (func.equals("tan")) x = Math.tan(Math.toRadians(x));
                    else throw new RuntimeException("Unknown function: " + func);
                } else {
                    throw new RuntimeException("Unexpected: " + (char) ch);
                }
                
                if (eat('^')) x = Math.pow(x, parseFactor()); // exponentiation
                
                return x;
            }
        }.parse();
    }
    
    public static void logFabric(String event, String... items) {
        try {
            CustomEvent customEvent = new CustomEvent(event);
            
            for (int i = 0; i < items.length; i += 2)
                customEvent.putCustomAttribute(items[i], items[i + 1]);
            
            Answers.getInstance().logCustom(customEvent);
            
            log("logFabric", event + " - " + Arrays.toString(items));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public static void logFabric(String event) {
        try {
            Answers.getInstance().logCustom(new CustomEvent(event));
            
            log("logFabric", event);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public static void textViewLink(TextView tv, String url) {
        // make a textview clickable
        
        if (tv == null || url == null) return;
        
        String label = tv.getText().toString();
        
        tv.setText(Html.fromHtml("<a href=\"" + url + "\">" + label + "</a> "));
        tv.setMovementMethod(LinkMovementMethod.getInstance());
        
        SpannableStringBuilder ssb = new SpannableStringBuilder();
        ssb.append(tv.getText());
        ssb.setSpan(new URLSpan("#"), 0, ssb.length(),
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        tv.setText(ssb, TextView.BufferType.SPANNABLE);
    }
    
    public static String numberComplete(double number, int decimalPlaces) {
        return numberComplete("" + number, decimalPlaces);
    }
    
    public static String numberComplete(String number, int decimalPlaces) {
        // I like to use every value in altcoins always with 8 decimal cases.
        try {
            BigDecimal bd = new BigDecimal(number);
            
            bd = bd.setScale(decimalPlaces, BigDecimal.ROUND_DOWN);
            
            return bd.toPlainString();
        } catch (Exception e) {
            return "";
        }
    }
    
    public static void copyToClipboard(Context context, String text) {
        
        ClipboardManager clipboard = (ClipboardManager) context.getSystemService(CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("wallet", text);
        clipboard.setPrimaryClip(clip);
    }
    
    public static long timestampLong() {
        return (System.currentTimeMillis() / 1000);
    }
    
    public static String now() {
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
        return sdf.format(new Date());
    }
    
    public static void log(String text) {
        log("Globals Log", text);
    }
    
    private static void log(String tag, String text) {
        Log.e(tag, text);
    }
    
    public static void writePreference(Context context, String key, String value) {
        try {
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
            
            preferences.edit().putString(key, value).apply();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public static String readPreference(Context context, String key, String defaultValue) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        
        return preferences.getString(key, defaultValue);
    }
    
    public static boolean isTrue(String value) {
        try {
            return value.equals("1") || value.equalsIgnoreCase("true") || value.equalsIgnoreCase("t") || value.equalsIgnoreCase("yes"); // || value.equalsIgnoreCase("no");
        } catch (Exception e) {
            e.printStackTrace();
            
            return false;
        }
    }
    
    public static void alert(Context context, String text) {
        Toast.makeText(context, text, Toast.LENGTH_LONG).show();
    }
}

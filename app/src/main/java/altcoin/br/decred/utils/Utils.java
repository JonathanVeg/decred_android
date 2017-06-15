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

    public static void writePreference(Context context, String key, long value) {
        try {
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);

            preferences.edit().putLong(key, value).apply();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

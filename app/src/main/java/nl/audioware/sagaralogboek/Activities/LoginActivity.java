package nl.audioware.sagaralogboek.Activities;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import nl.audioware.sagaralogboek.Libraries.Encryption;
import nl.audioware.sagaralogboek.NetworkGetters.NGLoginRequest;
import nl.audioware.sagaralogboek.R;

public class LoginActivity extends AppCompatActivity {
    public EditText ServerAddressET, UserET, PasswordET;
    public Button LoginButton;
    String key = "35kphy4y60lesiui";
    String keyDBG = "kj0vbyrma8on3a9h";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        SharedPreferences sharedPref = getSharedPreferences(getString(R.string.prefs_Main), Context.MODE_PRIVATE);
        ServerAddressET = findViewById(R.id.ServerAddressET);
        UserET = findViewById(R.id.UserET);
        PasswordET = findViewById(R.id.PasswordET);
        LoginButton = findViewById(R.id.LoginButton);
        LoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                attemptLogin();
            }
        });

        if(sharedPref.contains("PW")){
            String User = sharedPref.getString("User", "");
            String ServerAddress;
            if(User.charAt(0)=='_'){
                ServerAddress = "http://audioware.nl/WebTest/SagaraLogboek/";
                User = User.substring(1);
                key = keyDBG;
            } else {
                ServerAddress = "http://sagara.nl/logboek/";
            }
            ServerAddressET.setText(ServerAddress);
            UserET.setText(User);
            String PasswordEnc = sharedPref.getString("PW", "");
            String iv = sharedPref.getString("iv", "");
            new NGLoginRequest(this, ServerAddress, User, PasswordEnc, iv, key).get();
        }
    }
    void attemptLogin(){
        String ServerAddress;
        String User = UserET.getText().toString();
        if(User.charAt(0)=='_'){
            ServerAddress = "http://www.audioware.nl/webtest/SagaraLogboek/";
            User = User.substring(1);
            key = keyDBG;
        } else {
            ServerAddress = "http://sagara.nl/logboek/";
        }
        String Password = PasswordET.getText().toString();
        String iv = Encryption.getRandomString(16);
        String PasswordEnc = Encryption.encrypt(key, iv, Password);
        new NGLoginRequest(this, ServerAddress, User, PasswordEnc, iv, key).get();
        Log.d("LoginAttempt",
                "Server: "+ServerAddress+
                "\nUser: "+User+
                "\nPassword: "+Password+
                "\nPasswordEnc: "+PasswordEnc+
                "\niv: "+iv+
                "\npkey: "+key
        );
    }

    String fixURL(String url, boolean secure){
        if(!url.startsWith("http")){
            url = (secure)?"https://"+url:"http://"+url;
        }
        if(!url.endsWith("/"))url += "/";
        return url;
    }
}
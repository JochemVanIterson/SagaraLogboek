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
    String key = "kj0vbyrma8on3a9h";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        SharedPreferences sharedPref = getSharedPreferences("Settings", Context.MODE_PRIVATE);
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
            String ServerAddress = sharedPref.getString("BaseUrl", "");
            ServerAddressET.setText(ServerAddress);
            String User = sharedPref.getString("User", "");
            UserET.setText(User);
            String PasswordEnc = sharedPref.getString("PW", "");
            String iv = sharedPref.getString("iv", "");
            NGLoginRequest loginRequest = new NGLoginRequest(this, null, ServerAddress, User, PasswordEnc, iv, key);
            loginRequest.get();
        }
    }
    void attemptLogin(){
        String ServerAddress = ServerAddressET.getText().toString();
        ServerAddress = fixURL(ServerAddress, false);
        String User = UserET.getText().toString();
        String Password = PasswordET.getText().toString();
        String iv = Encryption.getRandomString(16);
        String PasswordEnc = Encryption.encrypt(key, iv, Password);
        NGLoginRequest loginRequest = new NGLoginRequest(this, null, ServerAddress, User, PasswordEnc, iv, key);
        loginRequest.get();
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
package nl.audioware.sagaralogboek.Activities;

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
    String key = "c7n4cct5pceu83y4";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
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
    }
    void attemptLogin(){
        String ServerAddress = ServerAddressET.getText().toString();
        ServerAddress = fixURL(ServerAddress, false);
        String User = UserET.getText().toString();
        String Password = PasswordET.getText().toString();
        byte[] iv = Encryption.generateIVSpec();
        String PasswordEnc = Encryption.encrypt(key, iv, Password);
        boolean Success = false;
        NGLoginRequest loginRequest = new NGLoginRequest(this, null, ServerAddress, User, Password);
        loginRequest.get();
        Log.d("LoginAttempt",
                "Server: "+ServerAddress+
                "\nUser: "+User+
                "\nPassword: "+Password+
                "\nPasswordEnc: "+PasswordEnc+
                "\nSuccess: "+Success
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

package c.neo.tolldemoperso;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

public class LoginActivity extends Activity {

	final protected String TAG = LoginActivity.class.getSimpleName();
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_login);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.login, menu);
		return true;
	}
	
	public void validaLogIn(View view){
		EditText password = (EditText) findViewById(R.id.editText1);
		String text = password.getText().toString();
		Log.d(TAG,"Ingresado: " +text);
		if(text.equals("NeologyNFC")){
			Toast.makeText(this, "Password OK", Toast.LENGTH_SHORT).show();
			 Intent intentPerso = new Intent(this, MenuPersoActivity.class);
			 startActivity(intentPerso);
		}else{
			Toast.makeText(this, "Password Fail", Toast.LENGTH_SHORT).show();
		}
	}

}

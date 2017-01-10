package c.neo.tolldemoperso;

import java.io.IOException;
import java.nio.charset.Charset;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentFilter.MalformedMimeTypeException;
import android.nfc.FormatException;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.Ndef;
import android.nfc.tech.NdefFormatable;
import android.nfc.tech.NfcF;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import c.neo.secure.PersoSecure_Froovie;

public class PersoFroovieActivity extends Activity {
	private static final String TAG = PersoFroovieActivity.class
			.getSimpleName();

	TextView tv_ins2;

	// UI Control
	RelativeLayout rl_ui;
	TextView tv_ui;
	EditText et_ui;

	// Flow Control

	int type_perso = 0;
	int flavor_selected = 0;
	int farm_id = 3;
	
	//NFC Control
	
	private NfcAdapter mAdapter;
	private PendingIntent mPendingIntent;
	private IntentFilter[] mFilters;
	private String[][] mTechLists;
	
	

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_perso_froovie);

		tv_ins2 = (TextView) findViewById(R.id.label_fr_ins2);
		
		mAdapter = NfcAdapter.getDefaultAdapter(this);

		// Create a generic PendingIntent that will be deliver to this activity.
		// The NFC stack
		// will fill in the intent with the details of the discovered tag before
		// delivering to
		// this activity.
		mPendingIntent = PendingIntent.getActivity(this, 0, new Intent(this,
				getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);

		// Setup an intent filter for all MIME based dispatches
		IntentFilter ndef = new IntentFilter(NfcAdapter.ACTION_NDEF_DISCOVERED);
		IntentFilter discovery = new IntentFilter(
				NfcAdapter.ACTION_TAG_DISCOVERED);
		try {
			ndef.addDataType("*/*");
		} catch (MalformedMimeTypeException e) {
			throw new RuntimeException("fail", e);
		}
		mFilters = new IntentFilter[] { discovery, ndef, };

		// Setup a tech list for all NfcF tags
		mTechLists = new String[][] { new String[] { NfcF.class.getName() } };
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.perso_froovie, menu);
		return true;
	}
	
	public void onResume() {
		super.onResume();
		// process the msgs array
		if (mAdapter != null) {
			mAdapter.enableForegroundDispatch(this, mPendingIntent, mFilters,mTechLists);
		}
	}

	@Override
	public void onPause() {
		super.onPause();
		if (mAdapter != null)
			mAdapter.disableForegroundDispatch(this);
	}

	@Override
	public void onNewIntent(Intent intent) {
		Log.d("Foreground dispatch", "Discovered tag with intent: " + intent);

		setIntent(intent);

		// Personalizacion del TAG
		try {
			Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
			write(tag);
		} catch (IOException e) {
			Toast mensajeVencido = Toast.makeText(this, R.string.error_label,
					Toast.LENGTH_LONG);
			mensajeVencido.setGravity(Gravity.CENTER_VERTICAL, 0, 0);
			mensajeVencido.show();
			e.printStackTrace();
		} catch (FormatException e) {
			e.printStackTrace();
		}

	}
	
	@SuppressLint("DefaultLocale")
	private void write(Tag tag) throws IOException, FormatException {
		String cadena_grabar = "";
		String cup_id_grabar = "";
		byte[] b = null;
		//Taza PreGrabada -> Tipo|Lote|Numero|Validador  1|AAA|000000001|5
		//Etiqueta Sabor -> Tipo|Id Maquina|Id Inventario|Pais|Granja|Sabor|Validador 0|ABCDEFGH1|0001|1|1|0|5
		switch(type_perso){
		case 0:
			Log.d(TAG, "Grabar CUP");
			et_ui = (EditText)findViewById(R.id.cup_et);
			cup_id_grabar = et_ui.getText().toString();
			if(cup_id_grabar.isEmpty()){
				Toast mensajeVencido = Toast.makeText(this, R.string.froovie_alert_cup, Toast.LENGTH_SHORT);
				mensajeVencido.setGravity(Gravity.CENTER_VERTICAL, 0, 0);
				mensajeVencido.show();
			}else{
			cadena_grabar = "1|BBB|"+cup_id_grabar+"|5";
			
			Log.d(TAG, "Datos a grabar: " + cadena_grabar);
			b = cadena_grabar.getBytes();
	        byte[] key;
	        byte[] keypbe;
	        byte[] encryptedData = null;
	        String encryptedString;
	        PersoSecure_Froovie perso = new PersoSecure_Froovie();
	        String stringKey = perso.bin2hex(tag.getId())+"Ne0logyFr00vi3Demo";
	        try {
				key = perso.getkey(stringKey);
				keypbe = perso.getkey(stringKey);
				encryptedData = PersoSecure_Froovie.encrypt(key,b);
				
				encryptedString = perso.bin2hex(encryptedData);
				Log.d(TAG, "PERSO::Key: " + perso.bin2hex(key));
				Log.d(TAG, "PERSO::KeyPBE: " + perso.bin2hex(keypbe));
				Log.d(TAG, "PERSO::encryptedData: " + encryptedString);
				
				b=encryptedData;
				
			} catch (NoSuchAlgorithmException e) {
				
				e.printStackTrace();
			} catch (NoSuchProviderException e) {
				
				e.printStackTrace();
			} catch (Exception e) {
				
				e.printStackTrace();
			}
			
			}
			break;
		case 1:
			Log.d(TAG, "Grabar POSTER");
			cadena_grabar = "0|ANDROID|0001|"+flavor_selected+"|"+farm_id+"|"+flavor_selected+"|5";
			b=cadena_grabar.getBytes();
			break;
		}
		
		if(!cadena_grabar.isEmpty()){
			
	        
	        NdefRecord relayRecord = new NdefRecord(NdefRecord.TNF_MIME_MEDIA,
	                new String("app/c.neo.froovie").getBytes(Charset.forName("US-ASCII")),
	                Integer.toString(type_perso).getBytes(), b);
	        
	        NdefRecord[] records = { relayRecord, NdefRecord.createApplicationRecord("c.neo.froovie") };
	        NdefMessage  message = new NdefMessage(records);
	        
	     // Get an instance of Ndef for the tag.
	        Ndef ndef = Ndef.get(tag);
	        
	        if(ndef !=null){
	        	Log.d(TAG, "NDEF!=null");
	        	// Enable I/O
	            ndef.connect();

	            // Write the message
	            ndef.writeNdefMessage(message);

	            // Close the connection
	            ndef.close();

	        }else{
	        	Log.d(TAG, "NDEF=null");
	        	NdefFormatable format = NdefFormatable.get(tag);  
	            if (format != null) {
	            	Log.d(TAG, "format=null");
	            	format.connect();
	            	format.format(message);
	            	format.close();
	            }
	        	
	        }
	        
	        Toast mensajeVencido = Toast.makeText(this, R.string.grabado_label, Toast.LENGTH_SHORT);
			mensajeVencido.setGravity(Gravity.CENTER_VERTICAL, 0, 0);
			mensajeVencido.show();
		}
	}

	public void onChangeTypePerso(View view) {
		// Is the button now checked?
		boolean checked = ((RadioButton) view).isChecked();

		// Check which radio button was clicked
		switch (view.getId()) {
		case R.id.radioButton1:
			if (checked) {
				Log.d(TAG, "Seleccion CUP");
				rl_ui = (RelativeLayout) findViewById(R.id.cup_rl);
				rl_ui.setVisibility(View.VISIBLE);

				rl_ui = (RelativeLayout) findViewById(R.id.poster_rl);
				rl_ui.setVisibility(View.GONE);

				tv_ins2.setText(getText(R.string.froovie_instrucciones1_cup));

				type_perso = 0;
			}
			break;
		case R.id.radioButton2:
			if (checked) {
				Log.d(TAG, "Seleccion POSTER");
				rl_ui = (RelativeLayout) findViewById(R.id.cup_rl);
				rl_ui.setVisibility(View.GONE);

				rl_ui = (RelativeLayout) findViewById(R.id.poster_rl);
				rl_ui.setVisibility(View.VISIBLE);

				tv_ins2.setText(getText(R.string.froovie_instrucciones1_poster));

				type_perso = 1;
			}
			break;
		}
	}

	public void onColorSelection(View view) {
		boolean checked = ((RadioButton) view).isChecked();

		switch (view.getId()) {
		case R.id.flavor_0_radioButton:
			if (checked) {
				Log.d(TAG, "Seleccion Sabor 0");
				flavor_selected = 0;
				farm_id = 3;
			}
			break;

		case R.id.flavor_1_radioButton:
			if (checked) {
				Log.d(TAG, "Seleccion Sabor 1");
				flavor_selected = 1;
				farm_id = 2;
			}
			break;

		case R.id.flavor_2_radioButton:
			if (checked) {
				Log.d(TAG, "Seleccion Sabor 2");
				flavor_selected = 2;
				farm_id = 1;
			}
			break;
			
		case R.id.flavor_3_radioButton:
			if (checked) {
				Log.d(TAG, "Seleccion Sabor 3");
				flavor_selected = 3;
				farm_id = 1;
			}
			break;
			
		case R.id.flavor_4_radioButton:
			if (checked) {
				Log.d(TAG, "Seleccion Sabor 4");
				flavor_selected = 4;
				farm_id = 1;
			}
			break;
			
		case R.id.flavor_5_radioButton:
			if (checked) {
				Log.d(TAG, "Seleccion Sabor 5");
				flavor_selected = 5;
				farm_id = 1;
			}
			break;
			
		case R.id.flavor_6_radioButton:
			if (checked) {
				Log.d(TAG, "Seleccion Sabor 6");
				flavor_selected = 6;
				farm_id = 1;
			}
			break;
			
		case R.id.flavor_7_radioButton:
			if (checked) {
				Log.d(TAG, "Seleccion Sabor 7");
				flavor_selected = 7;
				farm_id = 1;
			}
			break;
			
		case R.id.flavor_8_radioButton:
			if (checked) {
				Log.d(TAG, "Seleccion Sabor 8");
				flavor_selected = 8;
				farm_id = 1;
			}
			break;
			
		case R.id.flavor_9_radioButton:
			if (checked) {
				Log.d(TAG, "Seleccion Sabor 9");
				flavor_selected = 9;
				farm_id = 1;
			}
			break;
			
		case R.id.flavor_10_radioButton:
			if (checked) {
				Log.d(TAG, "Seleccion Sabor 10");
				flavor_selected = 10;
				farm_id = 1;
			}
			break;
			
		case R.id.flavor_11_radioButton:
			if (checked) {
				Log.d(TAG, "Seleccion Sabor 11");
				flavor_selected = 11;
				farm_id = 1;
			}
			break;
			
		case R.id.flavor_12_radioButton:
			if (checked) {
				Log.d(TAG, "Seleccion Sabor 12");
				flavor_selected = 12;
				farm_id = 1;
			}
			break;

		}
	}

}

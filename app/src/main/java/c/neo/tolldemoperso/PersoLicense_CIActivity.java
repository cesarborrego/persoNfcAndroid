package c.neo.tolldemoperso;

import java.io.IOException;
import java.nio.charset.Charset;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.util.Locale;

import c.neo.secure.PersoSecure_BO;
import c.neo.secure.PersoSecure_CI;
import android.nfc.FormatException;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.Ndef;
import android.nfc.tech.NdefFormatable;
import android.nfc.tech.NfcF;
import android.os.Bundle;
import android.app.Activity;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentFilter.MalformedMimeTypeException;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.widget.EditText;
import android.widget.Toast;

public class PersoLicense_CIActivity extends Activity {

	private final String TAG = PersoLicense_CIActivity.class.getSimpleName();
	EditText edittext;

	private NfcAdapter mAdapter;
	private PendingIntent mPendingIntent;
	private IntentFilter[] mFilters;
	private String[][] mTechLists;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_perso_license__ci);
		
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
		IntentFilter discovery = new IntentFilter(NfcAdapter.ACTION_TAG_DISCOVERED);
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
		getMenuInflater().inflate(R.menu.perso_license__ci, menu);
		return true;
	}
	
	public void onResume() {
		super.onResume();
		// process the msgs array
		if (mAdapter != null) {
			mAdapter.enableForegroundDispatch(this, mPendingIntent, mFilters,
					mTechLists);
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
	
	
	private void write(Tag tag) throws IOException, FormatException {
		String cadena_grabar = "";
		boolean cadena_vacia = false;
		edittext = (EditText)findViewById(R.id.ci_1_et);
		cadena_grabar+=edittext.getText().toString().toUpperCase(Locale.getDefault())+"|";
		if(edittext.getText().toString().isEmpty()){
			cadena_vacia = true;
		}
			
		edittext = (EditText)findViewById(R.id.ci_2_et);
		cadena_grabar+=edittext.getText()+"|";
		if(edittext.getText().toString().isEmpty()){
			cadena_vacia = true;
		}
		edittext = (EditText)findViewById(R.id.ci_3_et);
		cadena_grabar+=edittext.getText()+"|";
		if(edittext.getText().toString().isEmpty()){
			cadena_vacia = true;
		}
		edittext = (EditText)findViewById(R.id.ci_4_et);
		cadena_grabar+=edittext.getText()+"|1";
		if(edittext.getText().toString().isEmpty()){
			cadena_vacia = true;
		}
		Log.d(TAG, cadena_grabar);
		
		if(cadena_vacia){
			Toast mensajeVencido = Toast.makeText(this, R.string.alert_placa, Toast.LENGTH_SHORT);
			mensajeVencido.setGravity(Gravity.CENTER_VERTICAL, 0, 0);
			mensajeVencido.show();
			
		}else{
			
			byte[] b = cadena_grabar.getBytes();
	        byte[] key;
	        byte[] keypbe;
	        byte[] encryptedData = null;
	        String encryptedString;
	        PersoSecure_CI perso = new PersoSecure_CI();
	        String stringKey = perso.bin2hex(tag.getId())+"Neology-ci_lic_validator";
	        try {
				key = perso.getkey(stringKey);
				keypbe = perso.getkey(stringKey);
				encryptedData = PersoSecure_BO.encrypt(key,b);
				
				encryptedString = perso.bin2hex(encryptedData);
				Log.d(TAG, "PERSO::Key: " + perso.bin2hex(key));
				Log.d(TAG, "PERSO::KeyPBE: " + perso.bin2hex(keypbe));
				Log.d(TAG, "PERSO::encryptedData: " + encryptedString);
				
			} catch (NoSuchAlgorithmException e) {
				
				e.printStackTrace();
			} catch (NoSuchProviderException e) {
				
				e.printStackTrace();
			} catch (Exception e) {
				
				e.printStackTrace();
			}
	        
	        NdefRecord relayRecord = new NdefRecord(NdefRecord.TNF_MIME_MEDIA,
	                new String("app/c.neo.ci_lic_validator").getBytes(Charset.forName("US-ASCII")),
	                null, encryptedData);
	        
	        NdefRecord[] records = { relayRecord, NdefRecord.createApplicationRecord("c.neo.ci_lic_validator") };
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

}

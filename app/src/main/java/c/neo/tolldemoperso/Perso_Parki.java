package c.neo.tolldemoperso;

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
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import java.io.IOException;
import java.nio.charset.Charset;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.util.Locale;

import c.neo.secure.PersoSecureParking;
import c.neo.secure.PersoSecure_NEO_Cedula;
import c.neo.secure.PersoSecure_Parki;

public class Perso_Parki extends Activity {

	private final String TAG = Perso_Parki.class.getSimpleName();
	Spinner spinner;
	EditText edittext;

	private NfcAdapter mAdapter;
	private PendingIntent mPendingIntent;
	private IntentFilter[] mFilters;
	private String[][] mTechLists;



	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_perso_parking);

		spinner = (Spinner) findViewById(R.id.spinner1);
		// Create an ArrayAdapter using the string array and a default spinner layout
		ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
				R.array.entidades, android.R.layout.simple_spinner_item);
		// Specify the layout to use when the list of choices appears
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		// Apply the adapter to the spinner
		spinner.setAdapter(adapter);

		spinner = (Spinner) findViewById(R.id.spinner2);
		adapter = ArrayAdapter.createFromResource(this,
				R.array.tipostransporte, android.R.layout.simple_spinner_item);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spinner.setAdapter(adapter);
		spinner.setSelection(20);

		spinner = (Spinner) findViewById(R.id.spinner3);
		adapter = ArrayAdapter.createFromResource(this,
				R.array.colores, android.R.layout.simple_spinner_item);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spinner.setAdapter(adapter);

		mAdapter = NfcAdapter.getDefaultAdapter(this);

		// Create a generic PendingIntent that will be deliver to this activity. The NFC stack
		// will fill in the intent with the details of the discovered tag before delivering to
		// this activity.
		mPendingIntent = PendingIntent.getActivity(this, 0,
				new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);

		// Setup an intent filter for all MIME based dispatches
		IntentFilter ndef = new IntentFilter(NfcAdapter.ACTION_NDEF_DISCOVERED);
		IntentFilter discovery=new IntentFilter(NfcAdapter.ACTION_TAG_DISCOVERED);
		try {
			ndef.addDataType("*/*");
		} catch (MalformedMimeTypeException e) {
			throw new RuntimeException("fail", e);
		}
		mFilters = new IntentFilter[] {
				discovery,ndef,
		};

		// Setup a tech list for all NfcF tags
		mTechLists = new String[][] { new String[] { NfcF.class.getName() } };
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.perso_parking, menu);
		return true;
	}

	public void onResume() {
		super.onResume();
		//process the msgs array
		if (mAdapter != null){
			mAdapter.enableForegroundDispatch(this, mPendingIntent, mFilters, mTechLists);
		}
	}

	@Override
	public void onPause() {
		super.onPause();
		if (mAdapter != null) mAdapter.disableForegroundDispatch(this);
	}

	@Override
	public void onNewIntent(Intent intent) {
		Log.d("Foreground dispatch", "Discovered tag with intent: " + intent);



		setIntent(intent);


		//Personalizacion del TAG
		try {
			Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
			write(tag);
		} catch (IOException e) {
			Toast mensajeVencido = Toast.makeText(this, R.string.error_label, Toast.LENGTH_LONG);
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
		edittext = (EditText)findViewById(R.id.editText1);
		Log.d(TAG, "Placa: " + edittext.getText());
		cadena_grabar+= edittext.getText().toString().toUpperCase(Locale.getDefault());
		cadena_grabar = cadena_grabar.replaceAll("\\s", "");

		if(cadena_grabar.isEmpty() ){
			Toast mensajeVencido = Toast.makeText(this, R.string.alert_placa, Toast.LENGTH_SHORT);
			mensajeVencido.setGravity(Gravity.CENTER_VERTICAL, 0, 0);
			mensajeVencido.show();

		}else{


			spinner = (Spinner)findViewById(R.id.spinner1);

			String id_estado;
			id_estado = String.format("%02d", spinner.getSelectedItemPosition());
			Log.d(TAG, "Estado ID ["+id_estado+"]: " + spinner.getSelectedItemPosition());
			cadena_grabar+="|"+id_estado;
			spinner = (Spinner)findViewById(R.id.spinner2);
			Log.d(TAG, "Tipo ID: " + spinner.getSelectedItemPosition());
			cadena_grabar+="|"+spinner.getSelectedItemPosition();
			spinner = (Spinner)findViewById(R.id.spinner3);
			Log.d(TAG, "Color ID: " + spinner.getSelectedItemPosition());
			cadena_grabar+="|"+spinner.getSelectedItemPosition()+"|1";

			Log.d(TAG, "CADENA A GRABAR: " + cadena_grabar);

			byte[] b = cadena_grabar.getBytes();
			byte[] key;
			byte[] keypbe;
			byte[] encryptedData = null;
			String encryptedString;
			PersoSecure_Parki perso = new PersoSecure_Parki();
			String stringKey = perso.bin2hex(tag.getId())+"ParkiNeo";
			try {
				key = perso.getkey(stringKey);
				keypbe = perso.getkey(stringKey);
				encryptedData = PersoSecure_Parki.encrypt(key,b);

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
					new String("app/com.neology.parkingneology").getBytes(Charset.forName("US-ASCII")),
					null, encryptedData);

			NdefRecord[] records = { relayRecord, NdefRecord.createApplicationRecord("com.neology.parkingneology") };
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

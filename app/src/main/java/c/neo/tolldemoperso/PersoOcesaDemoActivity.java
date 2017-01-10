package c.neo.tolldemoperso;

import java.io.IOException;
import java.nio.charset.Charset;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;

import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapPrimitive;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;

import android.app.Activity;
import android.app.PendingIntent;
import android.app.ProgressDialog;
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
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;
import c.neo.secure.PersoSecure_OCESA_Demo;

public class PersoOcesaDemoActivity extends Activity {

	private final String TAG = PersoOcesaDemoActivity.class.getSimpleName();

	Spinner spinner;
	EditText edittext;

	private NfcAdapter mAdapter;
	private PendingIntent mPendingIntent;
	private IntentFilter[] mFilters;
	private String[][] mTechLists;

	// Declaracion de variables para consumir el web service
	private SoapObject request = null;
	private SoapSerializationEnvelope envelope = null;
	private SoapPrimitive resultsRequestSOAP = null;

	private static final String SOAP_ACTION = "";
	private static final String METHOD_NAME = "insertTagIDWithNameReplacement";
	private static final String NAMESPACE = "http://impl.neology.com/";
	private static final String URL = "http://148.245.107.245:8095/WSConexionDB/services/transactiondetailservice?wsdl";

	// Dialog to show a wait message
	private ProgressDialog dialog;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_perso_ocesa_demo);
		// Show the Up button in the action bar.
		setupActionBar();

		spinner = (Spinner) findViewById(R.id.ocesa_spinner1);
		// Create an ArrayAdapter using the string array and a default spinner
		// layout
		ArrayAdapter<CharSequence> adapter = ArrayAdapter
				.createFromResource(this, R.array.tiposacceso,
						android.R.layout.simple_spinner_item);
		// Specify the layout to use when the list of choices appears
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		// Apply the adapter to the spinner
		spinner.setAdapter(adapter);

		spinner = (Spinner) findViewById(R.id.ocesa_spinner2);
		adapter = ArrayAdapter.createFromResource(this, R.array.tiposservicio,
				android.R.layout.simple_spinner_item);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spinner.setAdapter(adapter);
		// spinner.setSelection(20);

		spinner = (Spinner) findViewById(R.id.ocesa_spinner3);
		adapter = ArrayAdapter.createFromResource(this, R.array.tipospromocion,
				android.R.layout.simple_spinner_item);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spinner.setAdapter(adapter);

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

		// Creacion del mensaje de espera
		dialog = new ProgressDialog(this);
		dialog.setMessage(getString(R.string.wait));
		dialog.setTitle(getString(R.string.gettingdata));
	}

	/**
	 * Set up the {@link android.app.ActionBar}.
	 */
	private void setupActionBar() {

		getActionBar().setDisplayHomeAsUpEnabled(true);

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.perso_ocesa_demo, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			// This ID represents the Home or Up button. In the case of this
			// activity, the Up button is shown. Use NavUtils to allow users
			// to navigate up one level in the application structure. For
			// more details, see the Navigation pattern on Android Design:
			//
			// http://developer.android.com/design/patterns/navigation.html#up-vs-back
			//
			NavUtils.navigateUpFromSameTask(this);
			return true;
		}
		return super.onOptionsItemSelected(item);
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
		PersoSecure_OCESA_Demo perso = new PersoSecure_OCESA_Demo();
		Log.d(TAG, "TAGID: " + perso.bin2hex(tag.getId()));

		edittext = (EditText) findViewById(R.id.ocesa_et1);
		Log.d(TAG, "Usuario: " + edittext.getText());
		cadena_grabar += edittext.getText().toString();
		cadena_grabar = cadena_grabar.replaceAll("\\s", "");

		if (cadena_grabar.isEmpty()) {
			Toast mensajeVencido = Toast.makeText(this, R.string.alert_usuario,
					Toast.LENGTH_SHORT);
			mensajeVencido.setGravity(Gravity.CENTER_VERTICAL, 0, 0);
			mensajeVencido.show();

		} else {

			spinner = (Spinner) findViewById(R.id.ocesa_spinner1);

			Log.d(TAG, "Tipo Acceso: " + spinner.getSelectedItemPosition());
			cadena_grabar += "|" + spinner.getSelectedItemPosition();
			spinner = (Spinner) findViewById(R.id.ocesa_spinner2);
			Log.d(TAG, "Tipo Servicio: " + spinner.getSelectedItemPosition());
			cadena_grabar += "|" + spinner.getSelectedItemPosition();
			spinner = (Spinner) findViewById(R.id.ocesa_spinner3);
			Log.d(TAG, "Tipo Promocion: " + spinner.getSelectedItemPosition());
			cadena_grabar += "|" + spinner.getSelectedItemPosition() + "|6";

			Log.d(TAG, "CADENA A GRABAR: " + cadena_grabar);

			byte[] b = cadena_grabar.getBytes();
			byte[] key;
			byte[] keypbe;
			byte[] encryptedData = null;

			String encryptedString;
			String stringKey = perso.bin2hex(tag.getId()) + "NeoOCESADemo";
			try {
				key = perso.getkey(stringKey);
				keypbe = perso.getkey(stringKey);
				encryptedData = PersoSecure_OCESA_Demo.encrypt(key, b);

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
					new String("app/c.neo.ocesa_demo").getBytes(Charset
							.forName("US-ASCII")), null, encryptedData);

			NdefRecord[] records = { relayRecord,
					NdefRecord.createApplicationRecord("c.neo.ocesa_demo") };
			NdefMessage message = new NdefMessage(records);

			// Get an instance of Ndef for the tag.
			Ndef ndef = Ndef.get(tag);

			if (ndef != null) {
				Log.d(TAG, "NDEF!=null");
				// Enable I/O
				ndef.connect();

				// Write the message
				ndef.writeNdefMessage(message);

				// Close the connection
				ndef.close();

			} else {
				Log.d(TAG, "NDEF=null");
				NdefFormatable format = NdefFormatable.get(tag);
				if (format != null) {
					Log.d(TAG, "format=null");
					format.connect();
					format.format(message);
					format.close();
				}

			}

			Toast mensajeVencido = Toast.makeText(this, R.string.grabado_label,
					Toast.LENGTH_SHORT);
			mensajeVencido.setGravity(Gravity.CENTER_VERTICAL, 0, 0);
			mensajeVencido.show();

			new ConsultaWS().execute(perso.bin2hex(tag.getId()), edittext.getText().toString());
		}

	}

	private class ConsultaWS extends AsyncTask<String, Float, String> {

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			dialog.show();
		}

		@Override
		protected String doInBackground(String... params) {
			Log.d(TAG, "Consultar Permiso: " + params[0]);
			String res = null;
			try {
				request = new SoapObject(NAMESPACE, METHOD_NAME);
				request.addProperty("tagID", params[0]);
				request.addProperty("tagName", params[1]);
				envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
				Log.d(TAG, "Method called " + METHOD_NAME + " Envelope: "
						+ envelope.toString());
				envelope.setOutputSoapObject(request);
				HttpTransportSE transporte = new HttpTransportSE(URL);
				transporte.debug = false;

				transporte.call(SOAP_ACTION, envelope);
				resultsRequestSOAP = (SoapPrimitive) envelope.getResponse();
				res = resultsRequestSOAP.toString();
				Log.d(TAG, "Response: " + res);

			} catch (Exception e) {
				Log.e(TAG,
						"Exception::ConsultaWS:doInBackground->"
								+ e.getMessage());
			}
			return res;
		}

		@Override
		protected void onPostExecute(String response) {
			dialog.dismiss();
			if (response != null && response.equalsIgnoreCase("1")) {
				try {
					Toast mensajeVencido = Toast.makeText(
							getApplicationContext(), R.string.registrado_ok,
							Toast.LENGTH_SHORT);
					mensajeVencido.setGravity(Gravity.CENTER_VERTICAL, 0, 0);
					mensajeVencido.show();

				} catch (Exception e) {
					Log.e(TAG,
							"Exception::ConsultaWS:onPostExecute->"
									+ e.getMessage());
					e.printStackTrace();
				}
			} else {

				Log.d(TAG, "No hubo conexion...");

			}
		}

	}

}

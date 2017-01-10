package c.neo.tolldemoperso;

import java.io.IOException;
import java.nio.charset.Charset;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;

import c.neo.secure.PersoSecure_OCESA_Demo;
import c.neo.secure.PersoSecure_SkiData;
import android.nfc.FormatException;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.Ndef;
import android.nfc.tech.NdefFormatable;
import android.nfc.tech.NfcF;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.app.Activity;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentFilter.MalformedMimeTypeException;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.widget.TextView;
import android.widget.Toast;

public class PersoSkiDataActivity extends Activity {
	
	private NfcAdapter mAdapter;
    private PendingIntent mPendingIntent;
    private IntentFilter[] mFilters;
    private String[][] mTechLists;
    private TextView mText;
    //private int mCount = 0;
    private static final String TAG = PersoTagActivity.class.getSimpleName();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_perso_ski_data);
		

		// Make sure we're running on Honeycomb or higher to use ActionBar APIs
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            // Show the Up button in the action bar.
            getActionBar().setDisplayHomeAsUpEnabled(true);
        }
        
               
        mText = (TextView) findViewById(R.id.text);
        //mText.setText("Lectura de datos del NFC");

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
		getMenuInflater().inflate(R.menu.perso_ski_data, menu);
		return true;
	}
	
	public void onResume() {
	    super.onResume();
	    System.out.println("onResume");
	    /*Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
	    if (NfcAdapter.ACTION_NDEF_DISCOVERED.equals(getIntent().getAction())) {
	        Parcelable[] rawMsgs = intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);
	        if (rawMsgs != null) {
	            msgs = new NdefMessage[rawMsgs.length];
	            for (int i = 0; i < rawMsgs.length; i++) {
	                msgs[i] = (NdefMessage) rawMsgs[i];
	            }
	        }
	    }*/
	    //process the msgs array
	    if (mAdapter != null){ 
	    	mAdapter.enableForegroundDispatch(this, mPendingIntent, mFilters, mTechLists);
	    }
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
        //originalRead(intent);
        //resolveIntent(intent);
        lectura(intent);
        
    }
	
	public void lectura(Intent intent){
		super.onNewIntent(intent);
		if(NfcAdapter.ACTION_NDEF_DISCOVERED.equals(intent.getAction())){
			NdefMessage[] messages = null;  
            Parcelable[] rawMsgs = intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);  
            if (rawMsgs != null) {  
                 messages = new NdefMessage[rawMsgs.length];  
                 for (int i = 0; i < rawMsgs.length; i++) {  
                      messages[i] = (NdefMessage) rawMsgs[i];  
                 }  
            }
            if(messages[0] != null) {  
                String result="";  
                byte[] payload = messages[0].getRecords()[0].getPayload();  
                // this assumes that we get back am SOH followed by host/code  
                for (int b = 1; b<payload.length; b++) { // skip SOH  
                     result += (char) payload[b];  
                }  
                //Toast.makeText(getApplicationContext(), "Tag Contains " + result, Toast.LENGTH_SHORT).show();
                //result = result.substring(2);
                Log.d(TAG, result);
                //mText.setText(result);
           }  
			
		}
	}
	
	 @Override
	    public void onPause() {
	        super.onPause();
	        if (mAdapter != null) mAdapter.disableForegroundDispatch(this);
	    }
	 
	 private void write(Tag tag) throws IOException, FormatException {
	        
		 PersoSecure_SkiData perso = new PersoSecure_SkiData();
	    	String textEdit = "";
	    	mText = (TextView)findViewById(R.id.editText1);
	    	textEdit = mText.getText().toString()+"|8";
	    	Log.d(TAG, "Valor a Guardar: " + textEdit);
	        
	        byte[] textBytes  = textEdit.getBytes();
	        
	        
	        int    textLength = textBytes.length;
	        byte[] payload    = new byte[1 + textLength];

	        String stringKey = perso.bin2hex(tag.getId())+"Ne0logySkiD4ta";
			Log.d(TAG, "Personalizar::Lectura UID: " + (stringKey));
			
			byte[] b = textEdit.getBytes();
	        byte[] key;
	        byte[] keypbe;
	        byte[] encryptedData = null;
	       
			String encryptedString = "";
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
	        
	        
	        
	        System.arraycopy(textBytes, 0, payload, 1 , textLength);

	        NdefRecord relayRecord = new NdefRecord(NdefRecord.TNF_MIME_MEDIA,
	                new String("app/c.neo.skidata").getBytes(Charset.forName("US-ASCII")),
	                null, encryptedData);
	        
	       
	    	
	    	NdefRecord[] records = { relayRecord, NdefRecord.createApplicationRecord("c.neo.skidata") };
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
	        
	        Toast mensajeVencido = Toast.makeText(this, R.string.grabado_label, Toast.LENGTH_LONG);
			mensajeVencido.setGravity(Gravity.CENTER_VERTICAL, 0, 0);
			mensajeVencido.show();
	  }
	 
	 

}

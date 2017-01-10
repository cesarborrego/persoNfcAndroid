package c.neo.tolldemoperso;

import java.io.IOException;
import java.nio.charset.Charset;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.util.ArrayList;

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
import android.nfc.tech.MifareUltralight;
import android.nfc.tech.Ndef;
import android.nfc.tech.NdefFormatable;
import android.nfc.tech.NfcF;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.app.NavUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

public class PersoTagActivity extends Activity {
	
	
	ArrayList<String> groupItem = new ArrayList<String>();
	ArrayList<Object> childItem = new ArrayList<Object>();
	private NfcAdapter mAdapter;
    private PendingIntent mPendingIntent;
    private IntentFilter[] mFilters;
    private String[][] mTechLists;
    private TextView mText;
    //private int mCount = 0;
    private static final String TAG = PersoTagActivity.class.getSimpleName();
    
    
    
	
	@SuppressLint("NewApi")
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_recharge);
		
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
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case android.R.id.home:
            NavUtils.navigateUpFromSameTask(this);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
	
	public void setGroupData(){
		groupItem.add("Recargar");
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
	
	/*
	
	private void resolveIntent(Intent intent){
		//Get the Action
		String action = intent.getAction();
		NdefMessage[] msgs;
		if(action.equals(NfcAdapter.ACTION_NDEF_DISCOVERED)){
			Log.d(TAG, "Intent " + intent);
			Parcelable[] rawMsgs = intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);
            
            if (rawMsgs != null){
            	msgs = new NdefMessage[rawMsgs.length];
            	Log.d(TAG, "rawMsgs.length " + Integer.toString(rawMsgs.length));
                for (int i = 0; i < rawMsgs.length; i++){
                	msgs[i] = (NdefMessage) rawMsgs[i];
                }
            }else{
            	// Unknown tag type
                byte[] empty = new byte[] {};
                NdefRecord record = new NdefRecord(NdefRecord.TNF_UNKNOWN, empty, empty, empty);
                NdefMessage msg = new NdefMessage(new NdefRecord[] {record});
                msgs = new NdefMessage[] {msg};
            }
            
            Log.d(TAG, "msgs " + msgs.toString());
            //Display the received messages
            List<TextRecord> records = NdefMessageParser.parse(msgs[0]);
            
            String text = "";
            
            for(TextRecord local:records){
            	text = local.getText() + " | " + text;
            	
            }
            
            //mText.setText(text);
		}else{
			Log.e("NFC/MainActivity", "Unknown intent " + intent);
		}
	}

	*/
	
	
    @Override
    public void onPause() {
        super.onPause();
        if (mAdapter != null) mAdapter.disableForegroundDispatch(this);
    }
    
    public String readTag(Tag tag) {
        MifareUltralight mifare = MifareUltralight.get(tag);
         try {
             mifare.connect();
             byte[] payload = mifare.readPages(1);
             return new String(payload, Charset.forName("US-ASCII"));
         } catch (IOException e) {
             Log.e(TAG, "IOException while writing MifareUltralight  message...", e);
         } finally {
             if (mifare != null) {
                try {
                    mifare.close();
                }
                catch (IOException e) {
                    Log.e(TAG, "Error closing tag...", e);
                }
             }
         }
         return null;
     }
    
    private void write(Tag tag) throws IOException, FormatException {
        
    	
    	String textEdit = "";
    	mText = (TextView)findViewById(R.id.editText1);
    	textEdit = mText.getText().toString();
    	Log.d(TAG, "Valor a Guardar: " + textEdit);
        
        byte[] textBytes  = textEdit.getBytes();
        
        
        int    textLength = textBytes.length;
        byte[] payload    = new byte[1 + textLength];

        String stringKey = PersoSecure.bin2hex(tag.getId())+"tollingNeology";
		Log.d(TAG, "Personalizar::Lectura UID: " + (stringKey));
		
		byte[] b = textEdit.getBytes();
        byte[] key;
        //byte[] keypbe;
        byte[] encryptedData = null;
        //byte[] encryptedDatapbe = null;
		String encryptedString = "";
        try {
			key = SimpleCrypto.createkey(stringKey);
			//keypbe = SimpleCrypto.getkey(stringKey);
			encryptedData = SimpleCrypto.encrypt(key,b);
			//encryptedDatapbe = SimpleCrypto.encrypt(keypbe,b);
			encryptedString = SimpleCrypto.bin2hex(encryptedData);
			Log.d(TAG, "PERSO::Key: " + PersoSecure.bin2hex(key));
			//Log.d(TAG, "PERSO::KeyPBE: " + PersoSecure_BO.bin2hex(keypbe));
			Log.d(TAG, "PERSO::encryptedData: " + encryptedString);
			//Log.d(TAG, "PERSO::encryptedDataPBE: " + PersoSecure.bin2hex(encryptedDatapbe));
		} catch (NoSuchAlgorithmException e) {
			
			e.printStackTrace();
		} catch (NoSuchProviderException e) {
			
			e.printStackTrace();
		} catch (Exception e) {
			
			e.printStackTrace();
		}
        
        
        
        System.arraycopy(textBytes, 0, payload, 1 , textLength);

        NdefRecord relayRecord = new NdefRecord(NdefRecord.TNF_MIME_MEDIA,
                new String("app/c.neo.peaje").getBytes(Charset.forName("US-ASCII")),
                null, encryptedData);
        
       
    	
    	//NdefRecord[] records = { createRecord(), NdefRecord.createApplicationRecord("c.neo.tolldemoperso") };
        NdefRecord[] records = { relayRecord, NdefRecord.createApplicationRecord("c.neo.peaje") };
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
    /*
    private NdefRecord createRecord() throws UnsupportedEncodingException {
        String text       = "NFC2";
        String lang       = "en";
        byte[] textBytes  = text.getBytes();
        byte[] langBytes  = lang.getBytes("US-ASCII");
        int    langLength = langBytes.length;
        int    textLength = textBytes.length;
        byte[] payload    = new byte[1 + langLength + textLength];

        // set status byte (see NDEF spec for actual bits)
        payload[0] = (byte) langLength;

        // copy langbytes and textbytes into payload
        System.arraycopy(langBytes, 0, payload, 1,              langLength);
        System.arraycopy(textBytes, 0, payload, 1 + langLength, textLength);

        NdefRecord record = new NdefRecord(NdefRecord.TNF_WELL_KNOWN, 
                                           NdefRecord.RTD_TEXT, 
                                           new byte[0], 
                                           payload);

        return record;
    }*/
    
    
}

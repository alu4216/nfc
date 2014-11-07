package com.example.nfc;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Locale;

import android.app.Activity;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.Ndef;
import android.nfc.tech.NdefFormatable;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;


public class MainActivity extends Activity {

	public static final String MIME_TEXT_PLAIN = "text/plain";
	public static final String TAG = "NfcDemo";

	private TextView mTextView;
	private NfcAdapter mNfcAdapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		mTextView = (TextView) findViewById(R.id.textView_explanation);

		mNfcAdapter = NfcAdapter.getDefaultAdapter(this);

		if (mNfcAdapter == null) {
			// Stop here, we definitely need NFC
			Toast.makeText(this, "This device doesn't support NFC.", Toast.LENGTH_LONG).show();
			finish();
			return;

		} 
		else
		{
			Toast.makeText(this, "This device support NFC.", Toast.LENGTH_LONG).show();

		}

		if (!mNfcAdapter.isEnabled()) {
			mTextView.setText("NFC is disabled.");
		} else {
			mTextView.setText(R.string.explanation);
		}
		if (NfcAdapter.ACTION_NDEF_DISCOVERED.equals(getIntent().getAction()));
		{
			Tag detectedTag =getIntent().getParcelableExtra(NfcAdapter.EXTRA_TAG); //Instance of discovered tag
			//Prepara NDIF
			//if( ESCRIBIR TEXTO PLANO) elseif(URI) elseif(URI ABSOLUTE) else (MIME_MEDIA)
			//De momento, solo texto plano

			//NDEF
			Locale locale = new Locale ("en","US");
			byte[] langBytes = locale.getLanguage().getBytes(Charset.forName("US-ASCII"));
			boolean encodeInUtf8 = false;
			Charset utfEncoding = encodeInUtf8 ? Charset.forName("UTF-8"):Charset.forName("UTF-16");
			int UtfBit = encodeInUtf8 ? 0 : (1<<7);
			char status = (char) (UtfBit + langBytes.length);
			String RTD_TEXT = "This is a RTD_TEXT";
			byte[] textByte =RTD_TEXT.getBytes(utfEncoding);
			byte[] data = new byte[1+langBytes.length + textByte.length ];
			data[0] = (byte) status;
			System.arraycopy(langBytes, 0, data, 1,langBytes.length);
			System.arraycopy(textByte, 0, data, 1+langBytes.length,textByte.length);
			NdefRecord textRecord = new NdefRecord(NdefRecord.TNF_WELL_KNOWN,NdefRecord.RTD_TEXT,new byte[0],data);
			NdefMessage newMessage = new NdefMessage(new NdefRecord[] {textRecord } );

			//WRITE DATA TO TAG

			writeNdefMessageToTag(newMessage, detectedTag);
		}


	}
	boolean writeNdefMessageToTag(NdefMessage message, Tag detectedTag) 
	{
		int size = message.toByteArray().length;
		try
		{
			Ndef ndef = Ndef.get(detectedTag);
			if(ndef != null)
			{
				ndef.connect();
				if(!ndef.isWritable())
				{
					Toast.makeText(this,"Tag Read Only",Toast.LENGTH_SHORT).show();
					return false;
				}
				if(ndef.getMaxSize()< size)
				{
					Toast.makeText(this,"Data cannot writtent to tag. Tag capacity is " + ndef.getMaxSize(), Toast.LENGTH_SHORT).show();
					return false;
				}
				ndef.writeNdefMessage(message);
				ndef.close();
				Toast.makeText(this,"Message is written tag", Toast.LENGTH_SHORT).show();
				return true;
			}
			else
			{
				NdefFormatable ndefFormat = NdefFormatable.get(detectedTag);
				if(ndefFormat != null)
				{
					try 
					{
						ndefFormat.connect();
						ndefFormat.format(message);
						ndefFormat.close();
						Toast.makeText(this,"The data is written to tag", Toast.LENGTH_SHORT).show();
						return true;
					}catch (IOException e)
					{
						Toast.makeText(this,"Fail to forma tag", Toast.LENGTH_SHORT).show();
						return false;
					}
				}
				else
				{
					Toast.makeText(this,"NDEF is not supported", Toast.LENGTH_SHORT).show();
					return false;
				}
			}

		}
		catch (Exception e)
		{
			Toast.makeText(this,"Write operation is failed", Toast.LENGTH_SHORT).show();
			return false;
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
}


package com.example.signlanguage;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.ArrayList;

import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutionException;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

public class SearchText extends Activity implements OnClickListener {
    private static final int REQUEST_CODE = 1234;
    private EditText entertext;
    private ImageView imagev;
    private ImageButton searchbtn, vsearchbtn, exitbtn, signbtn;
    private List<StringBuffer> mURLs = new ArrayList<StringBuffer>();
    private SpeechRecognizer sr;
    private Bitmap bitmap;
    private StringBuffer url;


    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout);
        searchbtn = (ImageButton) findViewById(R.id.search_txt);
        vsearchbtn = (ImageButton) findViewById(R.id.VoiceConvert);
        signbtn = (ImageButton) findViewById(R.id.SignBible);
        exitbtn = (ImageButton) findViewById(R.id.TouchToExit);
        entertext = (EditText) findViewById(R.id.entert);
        imagev = (ImageView) findViewById(R.id.imageView1);

        imagev.setOnClickListener(this);
        searchbtn.setOnClickListener(this);
        vsearchbtn.setOnClickListener(this);
        signbtn.setOnClickListener(this);
        exitbtn.setOnClickListener(this);

        initSpeechRecognizer();
    }

    public void onClick(View v) {

        InputMethodManager inputManager = (InputMethodManager) this.getSystemService(getApplicationContext().INPUT_METHOD_SERVICE);

        inputManager.hideSoftInputFromWindow(v.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);


        if (v.getId() == R.id.VoiceConvert) {

            entertext.setText("");

            Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
            intent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, "com.sign.language");
            intent.putExtra(RecognizerIntent.EXTRA_PROMPT, getString(R.string.speak));
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.ENGLISH);
            intent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1);
            startActivityForResult(intent, REQUEST_CODE);

        }

        if (v.getId() == R.id.search_txt) {

            inputManager = (InputMethodManager) this.getSystemService(getApplicationContext().INPUT_METHOD_SERVICE);

            inputManager.hideSoftInputFromWindow(v.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);

            Toast.makeText(getApplicationContext(), R.string.message_processing, Toast.LENGTH_SHORT).show();


            imagev = (ImageView) findViewById(R.id.imageView1);
            String strtxt;

            if (entertext.getText().toString().equals("")) {
                Toast.makeText(getApplicationContext(), R.string.message_empty, Toast.LENGTH_SHORT).show();
                return;
            } else
                strtxt = entertext.getText().toString();
            String[] words = strtxt.split("\\s+");

            for (int i = 0; i < words.length; i++) {

                words[i] = words[i].toLowerCase();
            }


            for (int i = 0; i < words.length; i++) {

                url = new StringBuffer("http://www.lifeprint.com/asl101/pages-signs/");
                url.append(words[i].charAt(0));
                url.append('/');
                url.append(words[i]);
                url.append(".htm");
                mURLs.add(url);
            }
            try {
                loadNext();
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (ExecutionException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

        if (v.getId() == R.id.SignBible) {
            Toast.makeText(getApplicationContext(), "Moving to sign language dictionary!", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(SearchText.this, FirstActivity.class);
            startActivity(intent);
            finish();
        }

        if (v.getId() == R.id.TouchToExit) {
            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
            alertDialogBuilder.setTitle("Exit Application?");
            alertDialogBuilder
                    .setMessage("Click yes to exit!")
                    .setCancelable(false)
                    .setPositiveButton("Yes",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    moveTaskToBack(true);
                                    android.os.Process.killProcess(android.os.Process.myPid());
                                    System.exit(1);
                                }
                            })

                    .setNegativeButton("No", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.cancel();
                        }
                    });

            AlertDialog alertDialog = alertDialogBuilder.create();
            alertDialog.show();
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE && resultCode == RESULT_OK) {
            ArrayList<String> matches = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            String topResult;
            topResult = matches.get(0);
            entertext.append(topResult);

            super.onActivityResult(requestCode, resultCode, data);
        }
    }
    private void initSpeechRecognizer() {
        if (sr == null) {
            sr = SpeechRecognizer.createSpeechRecognizer(this);
            if (!SpeechRecognizer.isRecognitionAvailable(getApplicationContext())) {
                Toast.makeText(getApplicationContext(), "Speech Recognition is not available in your phone,You have to enter text in edit box next to you", Toast.LENGTH_LONG).show();
                vsearchbtn.setEnabled(false);
            }
        }
        return;
    }


    private void loadNext() throws InterruptedException, ExecutionException {
        if (mURLs.isEmpty()) {
            return;
        }
        url = mURLs.remove(0);
        if (url != null) {
            new LoadImage().execute();
        }
    }

    private class LoadImage extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(Void... args) {
            if(url.indexOf("numbers")>=0 || url.indexOf("fingerspelling")>=0){
                try {
                    bitmap = BitmapFactory.decodeStream((InputStream) new URL(url.toString()).getContent());
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }


            else {
                try {

                    Document doc = Jsoup.connect(url.toString()).ignoreContentType(true).get();
                    Element img = doc.select("img[src$=.jpg]").first();
                    String Str = img.attr("abs:src");
                    bitmap = BitmapFactory.decodeStream((InputStream) new URL(Str).getContent());
                }
                catch (UnknownHostException e) {
                    runOnUiThread(new Runnable() {
                        public void run() {
                            Toast.makeText(SearchText.this, "No internet", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
                catch (Exception e) {

                    bitmap = null;
                    String t = url.toString();


                    String str = t.substring(46, t.lastIndexOf('.'));

                    for (int i = str.length() - 1; i >= 0; i--) {
                        if (str.charAt(i) <= '9' && str.charAt(i) >= '0')
                            mURLs.add(0, new StringBuffer("https://www.lifeprint.com/asl101/signjpegs/numbers/number0" + str.charAt(i) + ".jpg"));

                        else
                            mURLs.add(0, new StringBuffer("https://www.lifeprint.com/asl101/fingerspelling/abc-gifs/" + str.charAt(i) + "_small.gif"));
                    }


                    StringBuffer url = mURLs.remove(0);
                    try {
                        bitmap = BitmapFactory.decodeStream((InputStream) new URL(url.toString()).getContent());
                    } catch (IOException e1) {
                        e1.printStackTrace();
                    }
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {

            if (bitmap != null) {
                imagev.setImageBitmap(bitmap);
            }
            try {
                loadNext();
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (ExecutionException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }
}


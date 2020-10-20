package com.sanjeev.mytranslator.Fragments;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.speech.tts.Voice;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.ml.common.modeldownload.FirebaseModelDownloadConditions;
import com.google.firebase.ml.naturallanguage.FirebaseNaturalLanguage;
import com.google.firebase.ml.naturallanguage.languageid.FirebaseLanguageIdentification;
import com.google.firebase.ml.naturallanguage.translate.FirebaseTranslateLanguage;
import com.google.firebase.ml.naturallanguage.translate.FirebaseTranslator;
import com.google.firebase.ml.naturallanguage.translate.FirebaseTranslatorOptions;
import com.rey.material.widget.ProgressView;
import com.sanjeev.mytranslator.R;

import java.util.HashMap;
import java.util.Locale;

import static android.content.Context.MODE_PRIVATE;

public class TextFragment extends Fragment {
    TextInputLayout setTextLayout;
    private static final String TAG = "task";
    TextInputEditText getText, setText;
    MaterialButton button;
    Spinner spinner;
    int targetLanguage;
    public static final String SHARED_PREFS = "shared_string";
    ImageButton buttonCopy, buttonSpeak;
    ProgressView progressView;
    TextToSpeech textToSpeech;
    Locale lang = Locale.forLanguageTag("hi");

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_text, container, false);
        getText = view.findViewById(R.id.getText);
        setText = view.findViewById(R.id.setText);
        button = view.findViewById(R.id.translateButton);
        spinner = view.findViewById(R.id.spinner);
        setTextLayout = view.findViewById(R.id.setTextLayout);
        buttonCopy = view.findViewById(R.id.buttonCopy);
        buttonSpeak = view.findViewById(R.id.buttonSpeak);
        progressView = view.findViewById(R.id.progressView);

        buttonCopy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ClipboardManager clipboard = (ClipboardManager) getActivity().getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("simple text", setText.getText());
                clipboard.setPrimaryClip(clip);

                Toast.makeText(getActivity(), "Copied", Toast.LENGTH_SHORT).show();
            }
        });

        textToSpeech = new TextToSpeech(getActivity(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status != TextToSpeech.ERROR) {
                    textToSpeech.setLanguage(lang);
                }
            }
        });

        Voice voice = new Voice("hi-in-x-cfn#female_3-local",
                Locale.getDefault(), 1, 1, false, null);
        textToSpeech.setVoice(voice);
        textToSpeech.setSpeechRate(0.9f);

        buttonSpeak.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                textToSpeech.speak(setText.getText().toString(), TextToSpeech.QUEUE_FLUSH, null);
            }
        });

        try {
            SharedPreferences sharedPreferences = this.getActivity().getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
            String text = sharedPreferences.getString("text", null);
            if (!text.equals(null)) {
                getText.setText(text);
            }
        } catch (Exception e) {

        }

        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getActivity(),
                R.array.languages, android.R.layout.simple_spinner_item);
        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        spinner.setAdapter(adapter);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String lang = String.valueOf(parent.getItemAtPosition(position));
                switch (lang) {
                    case "Arabic":
                        targetLanguage = FirebaseTranslateLanguage.AR;
                        break;
                    case "Bulgarian":
                        targetLanguage = FirebaseTranslateLanguage.BG;
                        break;
                    case "Catalan":
                        targetLanguage = FirebaseTranslateLanguage.CA;
                        break;
                    case "English":
                        targetLanguage = FirebaseTranslateLanguage.EN;
                        break;
                    case "Galician":
                        targetLanguage = FirebaseTranslateLanguage.GL;
                        break;
                    case "Hindi":
                        targetLanguage = FirebaseTranslateLanguage.HI;
                        break;
                    case "Japanese":
                        targetLanguage = FirebaseTranslateLanguage.JA;
                        break;
                    case "Russian":
                        targetLanguage = FirebaseTranslateLanguage.RU;
                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                final String text = getText.getText().toString().trim();

                FirebaseLanguageIdentification languageIdentifier = FirebaseNaturalLanguage.getInstance()
                        .getLanguageIdentification();
                languageIdentifier.identifyLanguage(text)
                        .addOnSuccessListener(new OnSuccessListener<String>() {
                            @Override
                            public void onSuccess(String languageCode) {
                                if (languageCode != "und") {
                                    translate(languageCode, text);
                                } else {
                                    setTextLayout.setVisibility(View.INVISIBLE);
                                    buttonCopy.setVisibility(View.INVISIBLE);
                                    buttonSpeak.setVisibility(View.INVISIBLE);
                                    Toast.makeText(getActivity(), "Can't Identify language", Toast.LENGTH_LONG).show();
                                }
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(getActivity(), "Error : " + e.getMessage(), Toast.LENGTH_LONG).show();
                                setTextLayout.setVisibility(View.INVISIBLE);
                                buttonCopy.setVisibility(View.INVISIBLE);
                                buttonSpeak.setVisibility(View.INVISIBLE);
                                Log.i(TAG, "Can't identify language." + e.getMessage());
                            }
                        });
            }
        });

        return view;
    }

    private void translate(final String languageCode, final String text) {
        try {

            FirebaseTranslatorOptions options =
                    new FirebaseTranslatorOptions.Builder()
                            .setSourceLanguage(FirebaseTranslateLanguage.languageForLanguageCode(languageCode))
                            .setTargetLanguage(targetLanguage)
                            .build();


            final FirebaseTranslator translator = FirebaseNaturalLanguage.getInstance().getTranslator(options);
            translator.translate(text)
                    .addOnSuccessListener(
                            new OnSuccessListener<String>() {
                                @Override
                                public void onSuccess(@NonNull final String translatedText) {
                                    setText.setText(translatedText);
                                    setTextLayout.setVisibility(View.VISIBLE);
                                    buttonCopy.setVisibility(View.VISIBLE);
                                    buttonSpeak.setVisibility(View.VISIBLE);

                                }
                            })
                    .addOnFailureListener(
                            new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    setTextLayout.setVisibility(View.INVISIBLE);
                                    buttonCopy.setVisibility(View.INVISIBLE);
                                    buttonSpeak.setVisibility(View.INVISIBLE);
                                    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                                    builder.setTitle("Language not Downloaded");
                                    builder.setMessage("Download language using Data or Wifi ?");
                                    builder.setPositiveButton("Data", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            progressView.setVisibility(View.VISIBLE);
                                            FirebaseModelDownloadConditions conditions = new FirebaseModelDownloadConditions.Builder()
                                                    .build();
                                            translator.downloadModelIfNeeded(conditions)
                                                    .addOnSuccessListener(
                                                            new OnSuccessListener<Void>() {
                                                                @Override
                                                                public void onSuccess(Void v) {
                                                                    progressView.setVisibility(View.INVISIBLE);
                                                                    translate(languageCode, text);
                                                                }
                                                            })
                                                    .addOnFailureListener(
                                                            new OnFailureListener() {
                                                                @Override
                                                                public void onFailure(@NonNull Exception e) {
                                                                    Toast.makeText(getActivity(), "Download Failed", Toast.LENGTH_SHORT).show();
                                                                }
                                                            });
                                        }
                                    });

                                    builder.setNegativeButton("Wifi", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            progressView.setVisibility(View.VISIBLE);
                                            FirebaseModelDownloadConditions conditions = new FirebaseModelDownloadConditions.Builder()
                                                    .requireWifi()
                                                    .build();
                                            translator.downloadModelIfNeeded(conditions)
                                                    .addOnSuccessListener(
                                                            new OnSuccessListener<Void>() {
                                                                @Override
                                                                public void onSuccess(Void v) {
                                                                    progressView.setVisibility(View.INVISIBLE);
                                                                    translate(languageCode, text);
                                                                }
                                                            })
                                                    .addOnFailureListener(
                                                            new OnFailureListener() {
                                                                @Override
                                                                public void onFailure(@NonNull Exception e) {
                                                                    Toast.makeText(getActivity(), "Download Failed", Toast.LENGTH_SHORT).show();
                                                                }
                                                            });
                                        }
                                    });

                                    builder.show();
                                }
                            });
        } catch (NullPointerException e) {
            setTextLayout.setVisibility(View.INVISIBLE);
            buttonCopy.setVisibility(View.INVISIBLE);
            buttonSpeak.setVisibility(View.INVISIBLE);
            Toast.makeText(getActivity(), "Not Detect Language", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onPause() {
        if (textToSpeech != null) {
            textToSpeech.stop();
            textToSpeech.shutdown();
        }
        super.onPause();
    }
}

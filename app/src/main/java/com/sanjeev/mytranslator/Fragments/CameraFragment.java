package com.sanjeev.mytranslator.Fragments;

import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.text.TextBlock;
import com.google.android.gms.vision.text.TextRecognizer;
import com.google.auto.value.AutoAnnotation;
import com.rey.material.widget.FloatingActionButton;
import com.rey.material.widget.RippleManager;
import com.sanjeev.mytranslator.R;


public class CameraFragment extends Fragment {
    SurfaceView myCameraView;
    TextView textView;
    CameraSource cameraSource;
    final int ReqCameraPermissionID = 1001;
    FloatingActionButton button;
    public static final String SHARED_PREFS = "shared_string";
    SharedPreferences sharedPreferences;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_camera, container, false);
        myCameraView = view.findViewById(R.id.surface);
        textView = view.findViewById(R.id.text_msg);
        button = view.findViewById(R.id.button);

        sharedPreferences = getContext().getSharedPreferences(SHARED_PREFS, Context.MODE_PRIVATE);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                capture(v);
            }
        });

        TextRecognizer textRecognizer = new TextRecognizer.Builder(getActivity()).build();
        if (!textRecognizer.isOperational()) {
            Log.w("MainActivity", "Detector dependencies are not yet available");

        } else {
            Detector detector;
            cameraSource = new CameraSource.Builder(getActivity(), textRecognizer).
                    setFacing(CameraSource.CAMERA_FACING_BACK).
                    setRequestedPreviewSize(1280, 1024).setRequestedFps(2.0f).setAutoFocusEnabled(true).build();
            myCameraView.getHolder().addCallback(new SurfaceHolder.Callback() {
                @Override
                public void surfaceCreated(SurfaceHolder surfaceHolder) {
                    try {
                        if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.CAMERA}, ReqCameraPermissionID);
                            return;
                        }
                        cameraSource.start(myCameraView.getHolder());
                    } catch (Exception e) {

                    }
                }

                @Override
                public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {

                }

                @Override
                public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
                    cameraSource.stop();

                }
            });
            textRecognizer.setProcessor(new Detector.Processor<TextBlock>() {
                @Override
                public void release() {

                }

                @Override
                public void receiveDetections(Detector.Detections<TextBlock> detections) {
                    final SparseArray<TextBlock> items = detections.getDetectedItems();
                    if (items.size() != 0) {
                        textView.post(new Runnable() {
                            @Override
                            public void run() {
                                StringBuilder stringBuilder = new StringBuilder();
                                for (int i = 0; i < items.size(); i++) {
                                    TextBlock item = items.valueAt(i);
                                    stringBuilder.append(item.getValue());
                                    stringBuilder.append("\n");
                                }
                                textView.setText(stringBuilder.toString());
                                button.setVisibility(View.VISIBLE);
                            }
                        });
                    }

                }
            });
        }
        return view;
    }

    private void capture(View v) {
        cameraSource.stop();
        String text = textView.getText().toString().trim();
        if (!text.equals(null)) {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString("text", text);
            editor.apply();
            FragmentTransaction fragmentTransaction = getActivity().getSupportFragmentManager().beginTransaction();
            fragmentTransaction.replace(R.id.navigationFragments, new TextFragment());
            fragmentTransaction.commitAllowingStateLoss();
        }
    }
}

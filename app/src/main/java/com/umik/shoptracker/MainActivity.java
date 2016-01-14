package com.umik.shoptracker;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewFrame;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener2;
import org.opencv.objdetect.CascadeClassifier;
import org.opencv.imgproc.Imgproc;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.WindowManager;
import android.widget.Toast;
import java.net.Socket;

/**
 * Author Jakub Sokół
 */

public class MainActivity extends Activity implements CvCameraViewListener2 {

    private static final Scalar    FACE_RECT_COLOR     = new Scalar(0, 255, 0, 255);

    private Mat                    mRgba;
    private Mat                    mGray;
    private File                   mCascadeFile;
    private CascadeClassifier      mJavaDetector;
    private static final int       LOCK = 10;
    private int                    count = 10;

    private float                  mRelativeFaceSize   = 0.2f;
    private int                    mAbsoluteFaceSize   = 0;
    private static int             n,i = 0;


    private CameraBridgeViewBase   mOpenCvCameraView;
    private ClientModel            klient;
    private String                 root;
    private File                   myDir;

    private boolean                flag = false;
    private int                    id;
    private Intent                 intent;
    private long                   time;

    public int getId() {
        return id;
    }

    public boolean getFlag() {
        return flag;
    }

    private BaseLoaderCallback  mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS:
                {
                    Log.i("TAG", "OpenCV loaded successfully");

                    try {
                        InputStream is = getResources().openRawResource(R.raw.lbpcascade_frontalface);
                        File cascadeDir = getDir("cascade", Context.MODE_PRIVATE);
                        mCascadeFile = new File(cascadeDir, "lbpcascade_frontalface.xml");
                        FileOutputStream os = new FileOutputStream(mCascadeFile);

                        byte[] buffer = new byte[4096];
                        int bytesRead;
                        while ((bytesRead = is.read(buffer)) != -1) {
                            os.write(buffer, 0, bytesRead);
                        }
                        is.close();
                        os.close();

                        mJavaDetector = new CascadeClassifier(mCascadeFile.getAbsolutePath());
                        if (mJavaDetector.empty()) {
                            Log.i("TAG", "Failed to load cascade classifier");
                            mJavaDetector = null;
                        } else
                            Log.i("TAG", "Loaded cascade classifier from " + mCascadeFile.getAbsolutePath());

                        cascadeDir.delete();

                    } catch (IOException e) {
                        e.printStackTrace();
                        Log.i("TAG", "Failed to load cascade. Exception thrown: " + e);
                    }

                    mOpenCvCameraView.enableView();
                } break;
                default:
                {
                    super.onManagerConnected(status);
                } break;
            }
        }
    };

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.i("TAG", "called onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        mOpenCvCameraView = (CameraBridgeViewBase) findViewById(R.id.fd_activity_surface_view);
        mOpenCvCameraView.setCvCameraViewListener(this);
        mOpenCvCameraView.setMaxFrameSize(1280,720);

        klient = new ClientModel();
        new Thread(klient).start();

        root = Environment.getExternalStorageDirectory().toString();

        intent = getIntent();
        flag = intent.getBooleanExtra("flag", false);
        id = Integer.parseInt(intent.getStringExtra("id"));

        Log.i("TAG","FLAGA: " + flag + " ID: " + id);
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!OpenCVLoader.initDebug()) {
            Log.i("TAG", "Internal OpenCV library not found. Using OpenCV Manager for initialization");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_0_0, this, mLoaderCallback);
        } else {
            Log.i("TAG", "OpenCV library found inside package. Using it!");
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }
    }

    public void onDestroy() {
        super.onDestroy();
        mOpenCvCameraView.disableView();
    }

    public void onCameraViewStarted(int width, int height) {
        mGray = new Mat();
        mRgba = new Mat();
    }

    public void onCameraViewStopped() {
        mGray.release();
        mRgba.release();
    }

    public Mat onCameraFrame(CvCameraViewFrame inputFrame) {
        mRgba = inputFrame.rgba();
        mGray = inputFrame.gray();
        if(count == LOCK) {
            if (mAbsoluteFaceSize == 0) {
                int height = mGray.rows();
                if (Math.round(height * mRelativeFaceSize) > 0) {
                    mAbsoluteFaceSize = Math.round(height * mRelativeFaceSize);
                }
            }

            MatOfRect faces = new MatOfRect();
            if (mJavaDetector != null) {
                mJavaDetector.detectMultiScale(mGray, faces, 1.1, 1, 2, new Size(mAbsoluteFaceSize, mAbsoluteFaceSize), new Size());
            }

            Rect[] facesArray = faces.toArray();

            for (int i = 0; i < facesArray.length; i++) {
                Imgproc.rectangle(mRgba, facesArray[i].tl(), facesArray[i].br(), FACE_RECT_COLOR, 3);
            }

            if (facesArray.length > 0) {
                saveImage(mRgba);
                i++;
            }

            if (i == 5) {
                sendToServer();
                i = 0;
            }

            count = 0;
        }

        count++;
        return mRgba;
    }

    private void saveImage(Mat mat) {
        Bitmap bitmap = Bitmap.createBitmap(mRgba.cols(), mRgba.rows(), Bitmap.Config.RGB_565); // zamiast mat w oryginale było mRgba !!!!!!!!
        Utils.matToBitmap(mat, bitmap);

        if (i == 0) {
            time = System.currentTimeMillis();
            myDir = new File(root + "/" + time);
        }

        myDir.mkdirs();
        n++;
        String fname = n + ".jpg";
        File file = new File(myDir, fname);
        if (file.exists()) file.delete();

        try {
            FileOutputStream out = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
            out.flush();
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showSend() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(MainActivity.this, "Wysylanie na serwer!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void sendToServer() {
        showSend();
        Socket temp_gniazdko = klient.getGniazdko();

        if(temp_gniazdko!=null && !temp_gniazdko.isClosed()&& temp_gniazdko.isConnected() ) {
            prepareUser();
        }
        else{
            Log.i("TAG", "SERWER NIE WLACZONY");
        }
    }

    public User prepareUser(){
        File mDir = new File(myDir.getAbsolutePath());
        File[] files = mDir.listFiles();
        User u = new User(getId(),new Date(time),getFlag());

        try {
            klient.wyslijWiadomosc(u);
        } catch (IOException e) {
            e.printStackTrace();
        }

       for(File plik: files) {
           klient.wyslijPlik(plik.getAbsolutePath());
       }
        return u;
    }

}

package com.example.mypc.facedetector;


// 参考にしたサイト　http://blog.kotemaru.org/2015/05/23/android-camera2-sample.html

import java.nio.ByteBuffer;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Image;
import android.media.ImageReader;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;

import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;
import android.os.Handler;

public class MainActivity extends Activity {
    private AutoFitTextureView mTextureView;
    private ImageView mImageView;
    private ImageButton button_img;

    private Thread mThread;
    volatile private boolean yFinDraw = false;
    volatile private boolean mIsRunnable;

    volatile private int iPicStage;
    private Camera2StateMachine mCamera2;

    static {
        System.loadLibrary("opencv_java3");
    }



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mTextureView = (AutoFitTextureView) findViewById(R.id.TextureView);
        mImageView = (ImageView) findViewById(R.id.ImageView);

        button_img = (ImageButton)findViewById(R.id.imageButton1);
        mCamera2 = new Camera2StateMachine();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mCamera2.open(this, mTextureView);
        iPicStage = 0;
        start();
    }
    @Override
    protected void onPause() {
        mCamera2.close();
        super.onPause();
        stop();
    }
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && mImageView.getVisibility() == View.VISIBLE) {
            mTextureView.setVisibility(View.VISIBLE);
            mImageView.setVisibility(View.INVISIBLE);
            return false;
        }
        return super.onKeyDown(keyCode, event);
    }
    public void onClickShutter(View view) {
        mCamera2.takePicture(new ImageReader.OnImageAvailableListener() {
            @Override
            public void onImageAvailable(ImageReader reader) {
                // 撮れた画像をImageViewに貼り付けて表示。
                final Image image = reader.acquireLatestImage();
                ByteBuffer buffer = image.getPlanes()[0].getBuffer();
                byte[] bytes = new byte[buffer.remaining()];
                buffer.get(bytes);
                Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                image.close();
                //mImageView.setImageBitmap(bitmap);

                Mat mat = new Mat();
                Utils.bitmapToMat(bitmap,mat);
                 // mat をグレーに
                Imgproc.cvtColor(mat, mat, Imgproc.COLOR_RGB2GRAY);
                Imgproc.cvtColor(mat, mat, Imgproc.COLOR_GRAY2RGBA, 4);

                //  Bitmap dst に空のBitmapを作成
                Bitmap dst = Bitmap.createBitmap(mat.width(), mat.height(), Bitmap.Config.ARGB_8888);
                //  MatからBitmapに変換
                Utils.matToBitmap(mat, dst);
                mImageView.setImageBitmap(dst);


                mImageView.setVisibility(View.VISIBLE);
                mTextureView.setVisibility(View.INVISIBLE);

                //button_img.performClick();
                // yFinDraw = true;
                //
                //mTextureView.setVisibility(View.VISIBLE);
                //mImageView.setVisibility(View.INVISIBLE);
            }
        });
    }
    public void start(){
        // ハンドラを生成
        final Handler handler = new Handler();
        mThread = new Thread(new Runnable() {
            @Override
            public void run() {

                while (true) {
                    long startTime = System.currentTimeMillis();
                    if (!mIsRunnable) {      //  継続確認
                        break;
                    }
                    //if (yFinDraw == true) {
                    //    yFinDraw = false;
                        handler.post(new Runnable(){
                           @Override
                           public void run(){
                               button_img.performClick();
                           }
                        });
                    //}
                    long sleepTime = 1000 - System.currentTimeMillis() + startTime; // 1000mSec待ち
                    if(sleepTime >  0 ){
                        try {
                            Thread.sleep(sleepTime);
                        } catch(InterruptedException e){

                        }
                    }

                }
            }
        });
        //yFinDraw = true;
        mIsRunnable = true;
        mThread.start();
    }
    public void stop(){
        mIsRunnable = false;
    }

}

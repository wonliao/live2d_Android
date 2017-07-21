package com.studio.rai.live2d2.live2d;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.SoundPool;
import android.media.audiofx.Visualizer;
import android.os.Build;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.util.Log;

import com.studio.rai.live2d2.R;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

import javax.microedition.khronos.opengles.GL10;

import jp.live2d.android.Live2DModelAndroid;
import jp.live2d.android.UtOpenGL;
import jp.live2d.motion.Live2DMotion;
import jp.live2d.motion.MotionQueueManager;

/**
 * Created by LUTAS on 2017/1/3.
 */

public class MyL2DModel
{
    private static final String TAG = MyL2DModel.class.getSimpleName();
    private static final int TTS_SESSION_ID = 0;

    private Context context;
    private AssetManager assetManager;

    private L2DModelSetting mSetting;
    //model
    private Live2DModelAndroid mLive2DModel;
    //motion
    private Live2DMotion[] mMotions;
    private MotionQueueManager mMotionMgr;
    private L2DPhysics physics;
    //motion onTouch
    private float scaleX;
    private float scaleY;
    private final float minAngle = -30f;
    private final float maxAngle = 30f;
    //Sound
    private SoundPool mSoundPool;
    private Map<String,Integer> sounds;
    private Visualizer visualizer;
    private TextToSpeech mTts;
    private HashMap<String, String> mTtsParam;

    public MyL2DModel(Context context, L2DModelSetting setting) {
        this.context = context;
        mSetting = setting;
        sounds = new LinkedHashMap<>();
        assetManager = context.getAssets();
        mMotionMgr = new MotionQueueManager();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mSoundPool = new SoundPool.Builder().build();
        } else {
            mSoundPool = new SoundPool(1, AudioManager.STREAM_MUSIC, 5);
        }

        initTTS();
        initVisualizer();



        setupModel();
        setupPhysics();
        setupMotions();
        setupSounds();


    }

    private void initTTS() {
        mTts = new TextToSpeech(context, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if(status != TextToSpeech.ERROR) {
                    mTts.setLanguage(Locale.TAIWAN);

                    mTts.setOnUtteranceProgressListener(new UtteranceProgressListener() {
                        @Override
                        public void onStart(String utteranceId) {
                            visualizer.setEnabled(true);
                        }

                        @Override
                        public void onDone(String utteranceId) {
                            visualizer.setEnabled(false);
                        }

                        @Override
                        public void onError(String utteranceId) {}
                    });


                    mTtsParam = new HashMap<>();
                    mTtsParam.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "SOME MESSAGE");
                    mTtsParam.put(TextToSpeech.Engine.KEY_PARAM_SESSION_ID, "99");
                }
            }
        });
    }

    private void initVisualizer() {

        visualizer = new Visualizer(TTS_SESSION_ID);
        visualizer.setCaptureSize(Visualizer.getCaptureSizeRange()[0]);

        visualizer.setDataCaptureListener(new Visualizer.OnDataCaptureListener() {
            @Override
            public void onWaveFormDataCapture(Visualizer visualizer, byte[] waveform, int samplingRate) {
            }

            @Override
            public void onFftDataCapture(Visualizer visualizer, byte[] fft, int samplingRate) {

                float sum = 0.0f;
                for(int i = 0; i < fft.length; i+=1) {
                    sum += fft[i];
                }

                sum /= -250;
                if(sum > 1.0f) sum = 1.0f;
                //Log.e("won test 2 ==> ", "sum("+sum+")");

                mLive2DModel.setParamFloat( "PARAM_MOUTH_OPEN_Y", sum ,1 );
            }
        }, 8000, true, true);
    }

    private void setupModel() {
        try {
            InputStream in = context.getAssets().open(mSetting.getModel()) ;
            mLive2DModel = Live2DModelAndroid.loadModel(in) ;
            in.close() ;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void setupPhysics() {
        try {
            InputStream in = context.getAssets().open( mSetting.getPhysics() ) ;
            physics = L2DPhysics.load(in);
            in.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setupMotions() {
        try {
            Map<String,String> motionPaths = mSetting.getMotions();
            mMotions = new Live2DMotion[motionPaths.size()];

            Iterator<String> iterator = motionPaths.keySet().iterator();
            int count = 0;
            while (iterator.hasNext()) {
                String key = iterator.next();
                InputStream in = context.getAssets().open( motionPaths.get(key) ) ;
                mMotions[count] = Live2DMotion.loadMotion( in ) ;
                in.close() ;
                count++;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void setupSounds() {
        Map<String,String> soundPaths = mSetting.getSounds();

        Iterator<String> iterator = soundPaths.keySet().iterator();
        while (iterator.hasNext()) {
            String key = iterator.next();
            String path = soundPaths.get(key);
            try {
                int soundID = mSoundPool.load(assetManager.openFd(path), 1);
                sounds.put(key, soundID);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    //========================== Public Method =====================================================

    public void showMotion(int index, String key) {
        if (mMotionMgr.isFinished())
            mMotionMgr.startMotion(mMotions[index], false);

        if (!sounds.containsKey(key))
            return;
        int soundID = sounds.get(key);
        //Log.d(TAG, "play sound: "+ soundID);
        mSoundPool.play(soundID, 1f, 1f, 0, 0, 1f);
    }

    public void onTouch(int x, int y) {
        float angleX = x * scaleX - maxAngle;
        float angleY = -y * scaleY + maxAngle;

        mLive2DModel.setParamFloat( "PARAM_ANGLE_X", angleX ,1 );
        mLive2DModel.setParamFloat( "PARAM_ANGLE_Y", angleY ,1 );
        mLive2DModel.setParamFloat( "PARAM_EYE_BALL_X", angleY ,1 );

    }

    //對嘴
    public void lipSynch(String text) {

        String utteranceId=this.hashCode() + "";

        //mTts.speak(text, TextToSpeech.QUEUE_FLUSH, mTtsParam);

        mTts.speak(text, TextToSpeech.QUEUE_FLUSH, null, utteranceId);
    }

    public void lipStop() {
        mTts.stop();
    }

    //========================= SurfaceView Method =================================================

    public void onSurfaceCreated(GL10 gl) {
        setupTexure(gl);
    }

    private void setupTexure(GL10 gl) {
        try {
            String[] texures = mSetting.getTexures();

            for (int i=0; i<texures.length ; i++) {
                InputStream tin = context.getAssets().open(texures[i]) ;
                int texNo = UtOpenGL.loadTexture(gl , tin , true) ;
                //Log.d(TAG, i+" "+texNo);
                mLive2DModel.setTexture(i , texNo) ;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void onSurfaceChanged(GL10 gl, int width, int height) {
        initAngle(width, height);
        gl.glViewport(0 , 0 , width , height) ;

        gl.glMatrixMode(GL10.GL_PROJECTION) ;
        gl.glLoadIdentity() ;

        float modelWidth = mLive2DModel.getCanvasWidth();
        float visibleWidth = modelWidth * (3.0f/4.0f);
        float margin = 0.5f * (modelWidth/4.0f) ;

        gl.glOrthof(margin, margin+visibleWidth, visibleWidth*height/width, 0, 0.5f, -0.5f);
    }

    private void initAngle(int width, int height) {
        scaleX = (maxAngle-minAngle) / width;
        scaleY = (maxAngle-minAngle) / height;
    }

    public void onDrawFrame(GL10 gl) {
        gl.glMatrixMode(GL10.GL_MODELVIEW) ;
        gl.glLoadIdentity() ;
        gl.glClear(GL10.GL_COLOR_BUFFER_BIT) ;
        gl.glEnable(GL10.GL_BLEND) ;
        gl.glBlendFunc(GL10.GL_ONE , GL10.GL_ONE_MINUS_SRC_ALPHA) ;
        gl.glDisable(GL10.GL_DEPTH_TEST) ;
        gl.glDisable(GL10.GL_CULL_FACE) ;

        if(!mMotionMgr.isFinished())
            mMotionMgr.updateParam(mLive2DModel);

        mLive2DModel.setGL(gl) ;

        mLive2DModel.update();
        mLive2DModel.draw();
    }
}

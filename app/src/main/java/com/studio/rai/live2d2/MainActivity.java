package com.studio.rai.live2d2;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.media.audiofx.Visualizer;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.github.stuxuhai.jpinyin.ChineseHelper;
import com.microsoft.speech.tts.Synthesizer;
import com.microsoft.speech.tts.Voice;
import com.studio.rai.live2d2.live2d.L2DModelSetting;
import com.studio.rai.live2d2.live2d.MyL2DModel;
import com.tsy.sdk.myokhttp.MyOkHttp;
import com.tsy.sdk.myokhttp.response.JsonResponseHandler;
import com.tsy.sdk.myokhttp.response.RawResponseHandler;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import jp.live2d.Live2D;

public class MainActivity extends Activity
{
    private static final String TAG = MainActivity.class.getSimpleName();

    private GLSurfaceView mGlSurfaceView;
    private EditText et;
    //private Button testBtn;

    private Live2DRender mLive2DRender;
    private L2DModelSetting mModelSetting;
    private MyL2DModel mModel;

    private Synthesizer m_syn;
    private MyOkHttp mMyOkhttp;
    private String mAnswer;
    private WebView mWebView;

    private int searchType;

    Button mCloseBtn;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);


        if (m_syn == null) {
            // Create Text To Speech Synthesizer.
            m_syn = new Synthesizer("2a8555d2ccfe4b22b89f71d8fb99e78b");
        }
        m_syn.SetServiceStrategy(Synthesizer.ServiceStrategy.AlwaysService);
        Voice v = new Voice("zh-CN", "Microsoft Server Speech Text to Speech Voice (zh-TW, Yating, Apollo)", Voice.Gender.Female, true);
        m_syn.SetVoice(v, null);


        Live2D.init();
        initView();

        searchType = 0;

    }

    private void initView() {

        mMyOkhttp = new MyOkHttp();

        mGlSurfaceView = (GLSurfaceView) findViewById(R.id.main_glSurface);
        //mGlSurfaceView.setZOrderOnTop(true);

        et = (EditText) findViewById(R.id.main_et);

        setupLive2DModels();
        mGlSurfaceView.setRenderer(mLive2DRender);


        initButton();
    }

    private void setupLive2DModels() {
        try {
            //String modelName = "tsumiki";
            String modelName = "Epsilon_free";
            //String modelName = "izumi_illust";
            //String modelName = "hibiki";
            mModelSetting = new L2DModelSetting(this, modelName);
            mModel = new MyL2DModel(this, mModelSetting);

            mLive2DRender = new Live2DRender();
            mLive2DRender.setModel(mModel);


        } catch (JSONException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void initButton() {

        mWebView = (WebView)findViewById(R.id.callCalWebView);

        Button testBtn = (Button)findViewById(R.id.chestBtn);
        if(testBtn != null) {
            testBtn.setOnClickListener(new Button.OnClickListener() {

                @Override

                public void onClick(View v) {
                    test(v);
                }

            });
        }

        Button qnaBtn = (Button)findViewById(R.id.qna_btn);
        if(qnaBtn != null) {
            qnaBtn.setOnClickListener(new Button.OnClickListener() {
                @Override
                public void onClick(View v) {
                    searchType = 0;
                    Button button = (Button)findViewById(R.id.questionButton);
                    button.setBackgroundColor(Color.parseColor("#ff00ddff"));
                }
            });
        }

        Button luisBtn = (Button)findViewById(R.id.luis_btn);
        if(luisBtn != null) {
            luisBtn.setOnClickListener(new Button.OnClickListener() {
                @Override
                public void onClick(View v) {
                    searchType = 1;
                    Button button = (Button)findViewById(R.id.questionButton);
                    button.setBackgroundColor(Color.parseColor("#ff009688"));
                }
            });
        }

        Button tulingBtn = (Button)findViewById(R.id.tuling_btn);
        if(tulingBtn != null) {
            tulingBtn.setOnClickListener(new Button.OnClickListener() {
                @Override
                public void onClick(View v) {
                    searchType = 2;
                    Button button = (Button)findViewById(R.id.questionButton);
                    button.setBackgroundColor(Color.parseColor("#ffff8800"));
                }
            });
        }

        // 送出
        Button button = (Button)findViewById(R.id.questionButton);
        button.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {

                mWebView.setVisibility(View.INVISIBLE);
                mCloseBtn.setVisibility(View.INVISIBLE);

                InputMethodManager inputManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                inputManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);

                TextView textview = (TextView)findViewById(R.id.questionText);
                String question = textview.getText().toString();

                questionSubmit(question);
            }
        });

        mCloseBtn = (Button)findViewById(R.id.closeBtn);
        if(mCloseBtn != null) {
            mCloseBtn.setOnClickListener(new WebView.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mWebView.setVisibility(View.INVISIBLE);
                    mCloseBtn.setVisibility(View.INVISIBLE);
                }
            });
        }


        TextView ttsBtn = (TextView)findViewById(R.id.tts_tv);
        ttsBtn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                mModel.lipStop();
            }
        });
    }

    public void motions(View view) {
        final String[] keys = mModelSetting.getMotions().keySet().toArray(new String[]{});

        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setTitle("Motions")
                .setItems(keys, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mModel.showMotion(which, keys[which]);
                    }
                })
                .show();
    }

    public void test(View view) {

        mAnswer = "我的老天鵝，你這個色狼，九四八七九四狂";
        TextView _v = (TextView)(findViewById(R.id.tts_tv));
        _v.setText(mAnswer);
        //m_syn.SpeakToAudio(mAnswer);
        mModel.lipSynch(mAnswer);
    }

    private void qnaFunction(View view) {

        String text = et.getText().toString();
        if(!text.isEmpty()) {
            callQnAMaker(text);
        }
    }

    private void tts() {
        //String toSpeak = "臣亮言：先帝創業未半，而中道崩殂。今天下三分，益州疲弊，此誠危急存亡之秋也。";
        //Toast.makeText(getApplicationContext(), toSpeak,Toast.LENGTH_SHORT).show();


        //t1.speak(toSpeak, TextToSpeech.QUEUE_FLUSH, null, null);
        //t1.getVoice().
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int x = (int)event.getX();
        int y = (int)event.getY();

        if (event.getAction() == MotionEvent.ACTION_MOVE)
            mModel.onTouch(x, y);

        return false;
    }

    private void questionSubmit(String question) {

        switch (searchType) {
            case 0:
                callQnAMaker(question);
                break;
            case 1:
                callLUIS(question);
                break;
            case 2:
                callTuling123(question);
                break;
        }
    }

    private void callQnAMaker(final String question) {

        String url = "https://westus.api.cognitive.microsoft.com/qnamaker/v2.0/knowledgebases/6a23ec05-7a49-4914-bc76-15c41d9ac720/generateAnswer";
        String key = "c9a5176e79ec459d88dcdc0358c420a9";

        //String url = "https://westus.api.cognitive.microsoft.com/qnamaker/v2.0/knowledgebases/1344013b-5e30-4d6c-ad70-bbd11bb3385b/generateAnswer";
        //String key = "c9a5176e79ec459d88dcdc0358c420a9";

        Map<String, String> params = new HashMap<>();
        params.put("question", question);

        mMyOkhttp.post()
                .url(url)
                .addHeader("Ocp-Apim-Subscription-Key", key)
                .addHeader("Content-Type", "application/json")
                .params(params)
                .tag(this)
                .enqueue(new JsonResponseHandler() {
                    @Override
                    public void onSuccess(int statusCode, JSONObject response) {
                        Log.d("won test", "doPost onSuccess JSONObject:" + response);

                        try{

                            JSONArray answers = response.getJSONArray("answers");
                            JSONObject answers2  = answers.getJSONObject(0);
                            mAnswer  = answers2.getString("answer");
                            Log.e("won test", mAnswer);

                            if(mAnswer.equals("No good match found in the KB")) {

                                mAnswer = "很抱歉，我的主人，我不懂您的問題";
                            }

                            TextView _v = (TextView)(findViewById(R.id.tts_tv));
                            _v.setText(mAnswer);
                            //m_syn.SpeakToAudio(mAnswer);
                            mModel.lipSynch(mAnswer);
                        }catch(Exception obj){
                            Log.e("won test ==> ", obj.toString());
                        }
                    }

                    @Override
                    public void onSuccess(int statusCode, JSONArray response) {
                        Log.d("won test", "doPost onSuccess JSONArray:" + response);
                    }

                    @Override
                    public void onFailure(int statusCode, String error_msg) {
                        Log.d("won test", "doPost onFailure:" + error_msg);
                    }
                });
    }

    private void callLUIS(String question) {


        String url = "https://westus.api.cognitive.microsoft.com/luis/v2.0/apps/1a5eff99-4dbd-4b86-8c2c-2c7b314493ca?subscription-key=f9a1366042a3474eaa9c4c3ddd882dd2&timezoneOffset=0&verbose=true&q="+question;

        //Map<String, String> params = new HashMap<>();
        //params.put("question", question);

        mMyOkhttp.get()
                .url(url)
                .tag(this)
                .enqueue(new JsonResponseHandler() {
                    @Override
                    public void onSuccess(int statusCode, JSONObject response) {
                        Log.d("won test", "doPost onSuccess JSONObject:" + response);

                        try{
                            JSONObject topScoringIntent = response.getJSONObject("topScoringIntent");
                            String intent = topScoringIntent.getString("intent");

                            Log.e("won test ==> ", "intent(" + intent + ")");
                            if(intent.equals("None")) {

                                String query = response.getString("query");
                                Log.e("won test ==> ", "query(" + query + ")");
                                mAnswer = "很抱歉，我的主人，我不懂您的問題";
                                TextView _v = (TextView)(findViewById(R.id.tts_tv));
                                _v.setText(mAnswer);
                                //m_syn.SpeakToAudio(mAnswer);
                                mModel.lipSynch(mAnswer);
                            } else if(intent.equals("名字")) {

                                mAnswer = "我是五五六八八 AI，先進叫車系統。很高興為您服務，我的主人。";
                                TextView _v = (TextView)(findViewById(R.id.tts_tv));
                                _v.setText(mAnswer);
                                //m_syn.SpeakToAudio(mAnswer);
                                mModel.lipSynch(mAnswer);
                            } else if(intent.equals("找車")) {

                                String carType = "0";
                                String address = "";

                                JSONArray entities = response.getJSONArray("entities");
                                for(int i=0; i<entities.length(); i++) {

                                    JSONObject obj = entities.getJSONObject(i);
                                    String type = obj.getString("type");

                                    // 車型
                                    if (type.equals("車型::計程車")) {
                                        carType = "0";
                                    }
                                    else if (type.equals("車型::舒適型")) {
                                        carType = "1";
                                    }
                                    else if (type.equals("車型::豪華型")) {
                                        carType = "2";
                                    }
                                    else if (type.equals("車型::九人座")) {
                                        carType = "3";
                                    }
                                    else if (type.equals("地點")) {

                                        address = obj.getString("entity");
                                        address = address.replaceAll(" ", "");
                                    }
                                }
                                Log.e("won test", "車型("+carType+") 地點("+address+")");

                                if(carType.equals("0")) {

                                    mAnswer = "非常抱歉，目前系統不支援呼叫計程車，請改呼叫豪華車。";
                                    TextView _v = (TextView)(findViewById(R.id.tts_tv));
                                    _v.setText(mAnswer);
                                    //m_syn.SpeakToAudio(mAnswer);
                                    mModel.lipSynch(mAnswer);
                                } else {
                                    getGoogleMapsAddress(carType, address);
                                }
                            }
                        }catch(Exception obj){
                            Log.e("won test ==> ", obj.toString());
                        }
                    }

                    @Override
                    public void onSuccess(int statusCode, JSONArray response) {
                        Log.d("won test", "doPost onSuccess JSONArray:" + response);
                    }

                    @Override
                    public void onFailure(int statusCode, String error_msg) {
                        Log.d("won test", "doPost onFailure:" + error_msg);
                    }
                });
    }

    // 向 google maps 取得正確地址
    private void getGoogleMapsAddress(final String carType, String address) {

        String url = "http://52.197.124.196/luis/index.php?action=getGoogleAddress&address=" + address;

        mMyOkhttp.get()
                .url(url)
                .tag(this)
                .enqueue(new RawResponseHandler() {
                    @Override
                    public void onSuccess(int statusCode, String response) {

                        Log.d("won test", "doGet onSuccess:" + response);

                        String carName = "";
                        switch (carType) {
                            case "1": carName = "舒適型"; break;
                            case "2": carName = "豪華型"; break;
                            case "3": carName = "九人座"; break;
                        }
                        mAnswer = "你即將在" + response + "叫一台" + carName + "。為你派車中，請稍候!" ;
                        TextView _v = (TextView)(findViewById(R.id.tts_tv));
                        _v.setText(mAnswer);
                        //m_syn.SpeakToAudio(mAnswer);
                        mModel.lipSynch(mAnswer);

                        // call 叫車 API
                        String callCarUrl = "https://17-vr-live.wonliao.com/luis/index.php?action=callCar&car_type=" + carType + "&address=" + response;

                        mWebView.getSettings().setJavaScriptEnabled(true);
                        mWebView.setWebChromeClient(new WebChromeClient() {
                            public void onProgressChanged(WebView view, int progress) {
                                Log.e("won test", "test 1");
                            }
                        });
                        mWebView.setWebViewClient(new WebViewClient() {
                            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                                Log.e("won test", "test 2");
                            }
                        });

                        mWebView.loadUrl(callCarUrl);
                        mWebView.setVisibility(View.VISIBLE);
                        mCloseBtn.setVisibility(View.VISIBLE);
                    }

                    @Override
                    public void onFailure(int statusCode, String error_msg) {
                        Log.d("won test", "doGet onFailure:" + error_msg);
                    }
                });
    }

    private void callTuling123(String question) {

        String url = "http://www.tuling123.com/openapi/api";

        JSONObject jsonObject = new JSONObject();

        try {
            jsonObject.put("key", "96dd6767a03447f48a10fd108d0e7983");
            jsonObject.put("info", question);
            jsonObject.put("loc", "台湾台北市");
            jsonObject.put("lon", "25.0482323");
            jsonObject.put("lat", "121.5371275");
            jsonObject.put("userid", "1");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        mMyOkhttp.post()
                .url(url)
                .addHeader("Content-Type", "application/json")
                .jsonParams(jsonObject.toString())
                .tag(this)
                .enqueue(new JsonResponseHandler() {
                    @Override
                    public void onSuccess(int statusCode, JSONObject response) {
                        Log.d("won test", "doPost onSuccess JSONObject:" + response);

                        try{

                            mAnswer = response.getString("text");
                            mAnswer = ChineseHelper.convertToTraditionalChinese(mAnswer);
                            Log.e("won test", mAnswer);

                            TextView _v = (TextView)(findViewById(R.id.tts_tv));
                            _v.setText(mAnswer);
                            //m_syn.SpeakToAudio(mAnswer);
                            mModel.lipSynch(mAnswer);
                        }catch(Exception obj){
                            Log.e("won test ==> ", obj.toString());
                        }
                    }

                    @Override
                    public void onSuccess(int statusCode, JSONArray response) {
                        Log.d("won test", "doPost onSuccess JSONArray:" + response);
                    }

                    @Override
                    public void onFailure(int statusCode, String error_msg) {
                        Log.d("won test", "doPost onFailure:" + error_msg);
                    }
                });

    }
}

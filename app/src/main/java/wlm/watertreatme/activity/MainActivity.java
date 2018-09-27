package wlm.watertreatme.activity;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.JavascriptInterface;
import android.webkit.JsResult;
import android.webkit.SslErrorHandler;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import wlm.watertreatme.R;
import wlm.watertreatme.common.Constants;
import wlm.watertreatme.common.SPCommonInfoBean;

public class MainActivity extends AppCompatActivity {

    private WebView webView;
    private String url;
    private Context context;
    private Activity activity;

    private ValueCallback<Uri> mUploadMessage;
    public ValueCallback<Uri[]> uploadMessage;
    public static final int REQUEST_SELECT_FILE = 100;
    private final static int FILECHOOSER_RESULTCODE = 2;

    private boolean isWixPay = false;
    private String str_pay;
    private String returnHtmlStr = "";
    private String postStr; //传给HTML的数据
    private ProgressBar pg1;

    private String userCode, userName, userType, userType_code;

    private SharedPreferences spConfig;
    private boolean okToExit;//是否退出


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        spConfig = getSharedPreferences(SPCommonInfoBean.SPName, MODE_PRIVATE);
        userCode = spConfig.getString(SPCommonInfoBean.userCode,"");
        userName = spConfig.getString(SPCommonInfoBean.userName,"");
        userType_code = spConfig.getString(SPCommonInfoBean.userType_code,"");
        userType = spConfig.getString(SPCommonInfoBean.userType,"");

        webView = findViewById(R.id.web);
        pg1=(ProgressBar) findViewById(R.id.progress);
        context = this;
        activity = this;
        str_pay = "http://wxt.jingke.net";
        okToExit = false;


//        findViewById(R.id.tv).setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
////                Intent intent = new Intent("android.intent.action.VIEW",
////                        android.net.Uri.parse("androidamap://route?sourceApplication=softname&dname=永泰庄&dev=0&m=0&t=0&showType=2"));
////                intent.setPackage("com.autonavi.minimap");
//
//                Intent intent = null;
////                try {
////                    intent = Intent.getIntent("intent://map/direction?destination=永泰庄&mode=driving®ion=北京&src=yourCompanyName|yourAppName#Intent;scheme=bdapp;package=com.baidu.BaiduMap;end");
////                } catch (URISyntaxException e) {
////                    e.printStackTrace();
////                }
//                intent = new Intent(MainActivity.this, LocationActivity.class);
//                startActivity(intent);
//            }
//        });

//        url = "http://tst.zhongqizhiyun.com:8020/api/watertreatapi/index.html";
//        url = "http://192.168.0.139/WaterTreatment/Index.html";
        url = Constants.Common_URL + "/api/watertreatapi/index.html";

//        initWebView();

        mutualLoadWeb();
//
//        wixPayLoadWeb();
    }



    //html调用系统权限获取照片视频等文件时，监听到返回操作并获取到选择结果
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent)
    {

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
        {
            if (requestCode == REQUEST_SELECT_FILE)
            {
                if (uploadMessage == null)
                    return;
                uploadMessage.onReceiveValue(WebChromeClient.FileChooserParams.parseResult(resultCode, intent));
                uploadMessage = null;
            }
        }
        else if (requestCode == FILECHOOSER_RESULTCODE)
        {
            if (null == mUploadMessage)
                return;
            // Use MainActivity.RESULT_OK if you're implementing WebView inside Fragment
            // Use RESULT_OK only if you're implementing WebView inside an Activity
            Uri result = intent == null || resultCode != MainActivity.RESULT_OK ? null : intent.getData();
            mUploadMessage.onReceiveValue(result);
            mUploadMessage = null;
        }
        else
            Toast.makeText(context, "Failed to Upload Image", Toast.LENGTH_LONG).show();
    }


    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        if ((keyCode == KeyEvent.KEYCODE_BACK)) {

            //选课碎片里webview
            if (webView.canGoBack()) {
                webView.goBack(); //goBack()表示返回WebView的上一页面
            }else {

                // 处理返回操作. 退回首页后双击返回
                if (okToExit) {
                    System.exit(0);
                } else {
                    okToExit = true;
                    Toast.makeText(this, "再按一次退出", Toast.LENGTH_SHORT).show();
                    new Handler().postDelayed(new Runnable() {
                        public void run() {
                            okToExit = false;
                        }
                    }, 2000); // 2秒后重置
                }

            }
            return true;
        }

        return super.onKeyDown(keyCode, event);
    }


    //普通加载html界面，（相当于加载静态界面，不做任何交互）
    public void initWebView(){
        isWixPay = false;
        webView.clearCache(true);
        if (Build.VERSION.SDK_INT >= 19) {
            webView.getSettings().setCacheMode(
                    WebSettings.LOAD_CACHE_ELSE_NETWORK);
        }

        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                super.onProgressChanged(view, newProgress);
                activity.setTitle("Loading...");
                activity.setProgress(newProgress * 100);
                if (newProgress == 100) {
                    activity.setTitle(R.string.app_name);
                    pg1.setVisibility(View.GONE);//加载完网页进度条消失
                }else{
                    pg1.setVisibility(View.VISIBLE);//开始加载网页时显示进度条
                    pg1.setProgress(newProgress);//设置进度值
                }
            }

            @Override
            public boolean onJsAlert(WebView view, String url, String message,
                                     JsResult result) {
                return super.onJsAlert(view, url, message, result);
            }
        });

        webView.setWebViewClient(webViewClient);
        WebSettings s = webView.getSettings();
        s.setJavaScriptEnabled(true);
        s.setDomStorageEnabled(true);
        webView.clearCache(true);
        webView.loadUrl(url);

    }

    //加载html界面,实现数据交互,并支持HTML的File标签
    public void mutualLoadWeb(){
        webView.clearCache(true);

        isWixPay = false;

        if (Build.VERSION.SDK_INT >= 19) {
            webView.getSettings().setCacheMode(
                    WebSettings.LOAD_CACHE_ELSE_NETWORK);
        }

        webView.setWebChromeClient(webChromeClient);

        webView.setWebViewClient(webViewClient);
        WebSettings s = webView.getSettings();
        s.setJavaScriptEnabled(true);
        s.setDomStorageEnabled(true);
        webView.clearCache(true);
        webView.loadUrl(url);

        webView.addJavascriptInterface(new Object(){

            //将信息传给后台
            @SuppressWarnings("unused")
            @JavascriptInterface
            public String getMyInfo() {
                //html调用该方法获取信息，同时可以监控到HTML的点击操作并做出相应的操作

//                if (userCode == null || userCode.equals("")){
////                    AlertDialogUtil.showAlertDialog(context,"提示", "用户编号为空，请退出重新登陆!");
//                    Toast.makeText(context, "用户编号为空，请退出重新登陆!", Toast.LENGTH_SHORT).show();
//                    Intent intent = new Intent(MainActivity.this, LoginActivity.class);
//                    startActivity(intent);
//                    finish();
//                    return "";
//                }
                if (userType_code == null || userType_code.equals("")){
//                    AlertDialogUtil.showAlertDialog(context,"提示", "未获取到角色编号，请退出重新登陆!");
                    Toast.makeText(context, "未获取到角色编号，请退出重新登陆!", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                    startActivity(intent);
                    finish();
                    return "";
                }

                JSONObject jsonObject = new JSONObject();
                try {
                    jsonObject.put("UserCode", userCode); //用户编号
                    jsonObject.put("UserType_code", userType_code);//用户角色编号
                    jsonObject.put("UserType", userType);//用户角色类型
                    jsonObject.put("UserName", userName);//用户名

                } catch (JSONException e) {
                    e.printStackTrace();
                }
                Log.d("222222222     ", jsonObject.toString());
                return jsonObject.toString();

//                return postStr;//"要传给后台的信息，为空则是不传信息"
            }

            //将信息传给html同时获取到HTML传回的信息
            @SuppressWarnings("unused")
            @JavascriptInterface
            public String getInfo(String htmlStr) {
                //html调用该方法获取信息，同时可以获取到HTML传递过来的数据并可以做出相应操作
                returnHtmlStr = htmlStr;
                Log.d("222222222     ", returnHtmlStr);
                return postStr;//"要传给后台的信息，为空则是不传信息"
            }

        }, "android");

    }


    //加载html界面,实现数据交互,同时实现微信支付,并且可以选择图片等文件(支持html的file标签)
    public void wixPayLoadWeb(){
        webView.clearCache(true);
        isWixPay = true;


        if (Build.VERSION.SDK_INT >= 19) {
            webView.getSettings().setCacheMode(
                    WebSettings.LOAD_CACHE_ELSE_NETWORK);
        }

        //html需要调用系统权限获取图片视频等文件时需要重写new WebChromeClient()里的方法
        webView.setWebChromeClient(webChromeClient);

        webView.setWebViewClient(webViewClient);
        WebSettings s = webView.getSettings();
        s.setJavaScriptEnabled(true);
        s.setDomStorageEnabled(true);
        webView.clearCache(true);

        webView.loadUrl(url);

        webView.addJavascriptInterface(new Object(){

            //将信息传给后台
            @SuppressWarnings("unused")
            @JavascriptInterface
            public String postInfo() {
                //html调用该方法获取信息，同时可以监控到HTML的点击操作并做出相应的操作

                return postStr;//"要传给后台的信息，为空则是不传信息"
            }

            //将信息传给html同时获取到HTML传回的信息
            @SuppressWarnings("unused")
            @JavascriptInterface
            public String getInfo(String htmlStr) {
                //html调用该方法获取信息，同时可以获取到HTML传递过来的数据并可以做出相应操作
                returnHtmlStr = htmlStr;

                return postStr;//"要传给后台的信息，为空则是不传信息"
            }
        }, "android");

    }


    //微信支付需要重写该方法
    WebViewClient webViewClient = new WebViewClient() {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            view.clearCache(true);
            if (isWixPay){
                // 如下方案可在非微信内部WebView的H5页面中调出微信支付
                if (url.startsWith("weixin://wap/pay?")) {
                    Intent intent = new Intent();
                    intent.setAction(Intent.ACTION_VIEW);
                    intent.setData(Uri.parse(url));
                    activity.startActivity(intent);
                    return true;
                } else {
                    Map<String, String> extraHeaders = new HashMap<String, String>();
                    extraHeaders.put("Referer", str_pay);
                    view.loadUrl(url, extraHeaders);
                }
            }else {
                view.loadUrl(url);
            }
            return true;
        }

        @Override
        public void onReceivedSslError(WebView view, SslErrorHandler handler, android.net.http.SslError error) {
            // 重写此方法可以让webview处理https请求
            handler.proceed();
        }

        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
        }

        @Override
        public void onPageFinished(WebView view, String url) {

        }
    };


    //html需要调用系统权限获取图片视频等文件时需要重写new WebChromeClient()里的方法
    WebChromeClient webChromeClient = new WebChromeClient(){

        protected void openFileChooser(ValueCallback uploadMsg, String acceptType)
        {

            mUploadMessage = uploadMsg;
            Intent i = new Intent(Intent.ACTION_GET_CONTENT);
            i.addCategory(Intent.CATEGORY_OPENABLE);

//            i.setType("video/image/*");
            if (returnHtmlStr.equals("1")){
                i.setType("image/*");
            }else {
                i.setType("video/*");
            }

            activity.startActivityForResult(Intent.createChooser(i, "File Browser"), FILECHOOSER_RESULTCODE);
        }

        // For Lollipop 5.0+ Devices
        @TargetApi(Build.VERSION_CODES.LOLLIPOP)
        public boolean onShowFileChooser(WebView mWebView, ValueCallback<Uri[]> filePathCallback, WebChromeClient.FileChooserParams fileChooserParams)
        {
            if (uploadMessage != null) {
                uploadMessage.onReceiveValue(null);
                uploadMessage = null;
            }

            uploadMessage = filePathCallback;

            Intent intent = fileChooserParams.createIntent();
            try
            {
                activity.startActivityForResult(intent, REQUEST_SELECT_FILE);
            } catch (ActivityNotFoundException e)
            {
                uploadMessage = null;
                Toast.makeText(activity, "Cannot Open File Chooser", Toast.LENGTH_LONG).show();
                return false;
            }
            return true;
        }


        //For Android 4.1 only
        protected void openFileChooser(ValueCallback<Uri> uploadMsg, String acceptType, String capture)
        {
            mUploadMessage = uploadMsg;
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);

//            intent.setType("video/image/*");
            if (returnHtmlStr.equals("1")){
                intent.setType("image/*");
            }else {
                intent.setType("video/*");
            }
            activity.startActivityForResult(Intent.createChooser(intent, "File Browser"), FILECHOOSER_RESULTCODE);
        }

        protected void openFileChooser(ValueCallback<Uri> uploadMsg)
        {
            mUploadMessage = uploadMsg;
            Intent i = new Intent(Intent.ACTION_GET_CONTENT);
            i.addCategory(Intent.CATEGORY_OPENABLE);

//            i.setType("video/image/*");
            if (returnHtmlStr.equals("1")){
                i.setType("image/*");
            }else {
                i.setType("video/*");
            }

            activity.startActivityForResult(Intent.createChooser(i, "File Chooser"), FILECHOOSER_RESULTCODE);
        }


        @Override
        public void onProgressChanged(WebView view, int newProgress) {
            super.onProgressChanged(view, newProgress);
            activity.setTitle("Loading...");
            activity.setProgress(newProgress * 100);
            if (newProgress == 100) {
                activity.setTitle(R.string.app_name);
                pg1.setVisibility(View.GONE);//加载完网页进度条消失
            }else{
                pg1.setVisibility(View.VISIBLE);//开始加载网页时显示进度条
                pg1.setProgress(newProgress);//设置进度值
            }
        }

        @Override
        public boolean onJsAlert(WebView view, String url, String message,
                                 JsResult result) {
            return super.onJsAlert(view, url, message, result);
        }

    };

}
package top.khora.userinterface;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.FileProvider;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.DownloadManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.net.http.SslError;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.util.Log;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.JavascriptInterface;
import android.webkit.JsPromptResult;
import android.webkit.JsResult;
import android.webkit.SslErrorHandler;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URLDecoder;
import java.util.List;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.StreamProgress;
import cn.hutool.http.HttpUtil;

import static top.khora.userinterface.FileUtils.getAllFileNameInFold;

public class MainActivity extends AppCompatActivity {

    private WebView webView;
    private TextView tv;
    private FloatingActionButton fabtn;
    private Boolean canGoBack=true;

    @SuppressLint({"SourceLockedOrientationActivity", "RestrictedApi"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.web_activity);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);//强制横屏
        verifyStoragePermissions(this);
        closeAndroidPDialog();
        //超级耗时间！！
//        List<String> allFileNameInFoldRoot =
//                getAllFileNameInFold("/storage/emulated/0");
//        if (!allFileNameInFoldRoot.contains("IBDS")){
//            FileUtils.createNewDir("/storage/emulated/0/IBDS");
//        }
//        tv = findViewById(R.id.tv_main);
        webView=findViewById(R.id.webView_webactivity2);

        webView.setWebViewClient(new WebViewClient(){
            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
                Log.i("onPageStarted", "onPageStarted----for test----url: \n"+url);

            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                return super.shouldOverrideUrlLoading(view, request);
            }


            @Override
            public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
                handler.proceed();//忽略https证书
            }



            @Override
            public void onPageFinished(WebView view, String url) {
//                webView.loadUrl("javascript:receiveTMP(\"加载完毕\")");
                Log.i("onPageFinished", "onPageFinished----from js----url: \n"+url);

                CookieManager cookieManager = CookieManager.getInstance();
                String CookieStr = cookieManager.getCookie(url);
                if (CookieStr != null) {
                    Log.i("onPageFinished-cookie", CookieStr);
                    try {
                        Log.i("转码后Cookie:",  URLDecoder.decode(CookieStr, "utf-8"));
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                }
                canGoBack=true;//-629
                super.onPageFinished(view, url);
            }
        });
        webView.setWebChromeClient(new WebChromeClient(){

            @Override
            public boolean onJsAlert(WebView view, String url, String message, JsResult result) {
//                Log.e("WebActivity-jsAlert",url+"\n"+message+"\n"+result.toString());
//                return true;
                if (BuildConfig.DEBUG) {
                    return super.onJsAlert(view, url, message, result);
                }else{
                    result.confirm();
                    return true;
                }
            }

            @Override
            public boolean onJsConfirm(WebView view, String url, String message, JsResult result) {
//                return true;
                return super.onJsConfirm(view, url, message, result);
            }

            @Override
            public boolean onJsPrompt(WebView view, String url, String message, String defaultValue, JsPromptResult result) {
//                return true;
                return super.onJsPrompt(view, url, message, defaultValue, result);
            }
        });
        WebSettings webSettings = webView.getSettings();
        //如果访问的页面中要与Javascript交互，则webview必须设置支持Javascript
        webSettings.setJavaScriptEnabled(true);
        // 若加载的 html 里有JS 在执行动画等操作，会造成资源浪费（CPU、电量）
        // 在 onStop 和 onResume 里分别把 setJavaScriptEnabled() 给设置成 false 和 true 即可

        webSettings.setDomStorageEnabled(true);


        //设置自适应屏幕，两者合用
        webSettings.setUseWideViewPort(true); //将图片调整到适合webview的大小
        webSettings.setLoadWithOverviewMode(true); // 缩放至屏幕的大小

        //缩放操作
        webSettings.setSupportZoom(false); //支持缩放，默认为true。是下面那个的前提。
        webSettings.setBuiltInZoomControls(false); //设置内置的缩放控件。若为false，则该WebView不可缩放
        webSettings.setDisplayZoomControls(false); //隐藏原生的缩放控件

        //其他细节操作
//        webSettings.setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK); //关闭webview中缓存
        webSettings.setAllowFileAccess(true); //设置可以访问文件
        webSettings.setJavaScriptCanOpenWindowsAutomatically(true); //支持通过JS打开新窗口
        webSettings.setLoadsImagesAutomatically(true); //支持自动加载图片
        webSettings.setDefaultTextEncodingName("utf-8");//设置编码格式
        webSettings.setUserAgentString("fjdClient1.0");

        webSettings.setSavePassword(false);

        //webView屏蔽页面长按菜单
        webView.setLongClickable(true);
        Log.i("MainActivity_test","loadurl开始！");
        webView.loadUrl("http://cnd.khora.top/http/testadmin.html");
//        webView.loadUrl("http://www.baidu.com");
//        webView.setOnLongClickListener(new View.OnLongClickListener() {
//            @Override
//            public boolean onLongClick(View v) {
//                return true;
//            }
//        });
        //cookies
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            CookieManager.getInstance().setAcceptThirdPartyCookies(webView, true);
        }
//        webView.reload();
        //JS接口
        webView.addJavascriptInterface(new Object(){
            @JavascriptInterface
            public void startIBDS(){
                String uid="1";
                String sessionid="1";
                String action="1";
                Log.i("JSObject-startIBDS","startIBDS!! \n"+uid+"\n"+sessionid+"\n"+action);
                assetApkToFile();
                openAppProcedure(uid,sessionid,action);
            }
            @JavascriptInterface
            public void canNotGoBack(){
                Log.i("JSObject-cannotgoback","不可后退页面！");
                canGoBack=false;
            }
        },"ibds");
        fabtn=findViewById(R.id.floatingActionButton);
        fabtn.setVisibility(View.GONE);



    }

    private boolean isBound_remote = false;
    //serviceMessenger表示的是Service端的Messenger，其内部指向了MyService的ServiceHandler实例
    //可以用serviceMessenger向MyService发送消息
    private Messenger serviceMessenger = null;
    private ServiceConnection conn_remote = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder binder) {
            //客户端与Service建立连接
            Log.i("conn_remote", "客户端 onServiceConnected");

            //我们可以通过从Service的onBind方法中返回的IBinder初始化一个指向Service端的Messenger
            serviceMessenger = new Messenger(binder);
            isBound_remote = true;
//            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            //客户端与Service失去连接
            serviceMessenger = null;
            isBound_remote = false;
            Log.i("conn_remote", "客户端 onServiceDisconnected");
        }
    };


    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            "android.permission.READ_EXTERNAL_STORAGE",
            "android.permission.WRITE_EXTERNAL_STORAGE" };
    //然后通过一个函数来申请
    public static void verifyStoragePermissions(Activity activity) {
        try {
            //检测是否有写的权限
            int permission = ActivityCompat.checkSelfPermission(activity,
                    "android.permission.WRITE_EXTERNAL_STORAGE");
            if (permission != PackageManager.PERMISSION_GRANTED) {
                // 没有写的权限，去申请写的权限，会弹出对话框
                ActivityCompat.requestPermissions(activity, PERMISSIONS_STORAGE,REQUEST_EXTERNAL_STORAGE);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private void savePs(String line1, String line2, String line3, Context context){
        Log.i("FileDir", String.valueOf(context.getExternalFilesDir("")));
//
//        List<String> allFileNameInIBDS =
//                getAllFileNameInFold("/storage/emulated/0/IBDS");
//        if (allFileNameInIBDS.contains("ps")){
//            return;
//        }
        FileUtils.writeLog("/storage/emulated/0/IBDS/ps",
                line1+"\r\n"+line2+"\r\n"+line3,false,false);
//        if (line1!=null) {
//            FileUtils.writeLog("/storage/emulated/0/IBDS/ps",line1,true,false);
//        }
//        if (line2!=null) {
//            FileUtils.writeLog("/storage/emulated/0/IBDS/ps",line2,true,true);
//        }
//        if (line3!=null) {
//            FileUtils.writeLog("/storage/emulated/0/IBDS/ps",line3,false,true);
//        }
    }
    private void savePsCache(String line1, String line2, String line3, Context context){
        Log.i("FileDir", String.valueOf(context.getExternalFilesDir("")));
        List<String> allFileNameInFoldRoot =
                getAllFileNameInFold("/storage/emulated/0");
        if (!allFileNameInFoldRoot.contains("IBDS1")){
            FileUtils.createNewDir("/storage/emulated/0/IBDS1");
        }
        List<String> allFileNameInIBDS =
                getAllFileNameInFold("/storage/emulated/0/IBDS");
        if (allFileNameInIBDS.contains("ps")){
            return;
        }

        if (line1!=null) {
            FileUtils.writeLog("/storage/emulated/0/IBDS1/ps",line1,true,false);
        }
        if (line2!=null) {
            FileUtils.writeLog("/storage/emulated/0/IBDS1/ps",line2,true,true);
        }
        if (line3!=null) {
            FileUtils.writeLog("/storage/emulated/0/IBDS1/ps",line3,false,true);
        }
    }

    /**
     * 多线程
     * */
    public static final int DOWNLOAD_FAILED=0;
    private class MainActivityHandler extends Handler{
        @SuppressLint("RestrictedApi")
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            switch (msg.what){
                case DOWNLOAD_FAILED:
                    Toast.makeText(MainActivity.this,(String)msg.obj
                            ,Toast.LENGTH_LONG).show();
                    fabtn.setVisibility(View.VISIBLE);
//                    tv.setVisibility(View.GONE);
                    break;
            }
        }
    }
    MainActivityHandler mainActHandler =new MainActivityHandler();


    int time=0;
    Runnable launchAppRunnable =new Runnable() {
        @Override
        public void run() {
            Log.e("MainAct-Runnable","子线程查找ps，time="+time);
            List<String> allFileNameInFoldIBDS =
                    getAllFileNameInFold("/storage/emulated/0/IBDS");
            System.out.println(allFileNameInFoldIBDS.toString());
            Boolean isPsContained=allFileNameInFoldIBDS.contains("/storage/emulated/0/IBDS/ps");
            if (isPsContained){
                time=0;
                launchapp(MainActivity.this);
            }else{
                if (time<1000){
                    time+=200;
                    mainActHandler.postDelayed(launchAppRunnable,200);
                }else {
                    openAppProcedureStatus=false;
                    Toast.makeText(MainActivity.this,"配置文件未创建成功，开启检测失败！"
                            ,Toast.LENGTH_SHORT).show();
                    return;
                }
            }
        }
    };


    private void launchapp(Context context) {
        //判断当前手机是否有要跳入的app
        if (isAppInstalled(context,"air.BORCHE")){
            //如果有根据包名跳转
//            context.startActivity(context.getPackageManager()
//                    .getLaunchIntentForPackage("com.MyFusApp.h"));
            context.startActivity(context.getPackageManager()
                    .getLaunchIntentForPackage("air.BORCHE"));
        }else{
            //如果没有，走进入系统商店找到这款APP，提示你去下载这款APP的程序
            Toast.makeText(context,"未找到检测BORCHE模块",Toast.LENGTH_SHORT).show();
        }
    }
    //这里是判断APP中是否有相应APP的方法
    private boolean isAppInstalled(Context context, String packageName) {
        try {
            context.getPackageManager().getPackageInfo(packageName,0);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    static Boolean isBackToasted=false;
    @Override
    public void onBackPressed() {
        if (webView.canGoBack()){
            if (canGoBack) {
                webView.goBack();
            }
            return;
        }else {
            if (!isBackToasted){
                isBackToasted=true;
                Toast toast=Toast.makeText(MainActivity.this,null
                        ,Toast.LENGTH_SHORT);
                toast.setText("Tap BACK button again to exit");
                toast.show();
                mainActHandler.postDelayed(resetIsBackToastedRunnable,3000);
                return;
            }else {

            }
        }
        super.onBackPressed();//本活动关闭
    }
    Runnable resetIsBackToastedRunnable=new Runnable() {
        @Override
        public void run() {
            isBackToasted=false;
        }
    };

    private Boolean openAppProcedureStatus=false;
    private void openAppProcedure(String line1,String line2,String line3){//启动检测app执行该方法
        Log.i("openAppProcedure","openAppProcedure执行");
        Toast.makeText(getApplicationContext(),"正在启动IBDS...",Toast.LENGTH_SHORT).show();
        if (openAppProcedureStatus){
            Log.e("openAppProcedure","重复点击跳转不可生效");
        }else {
            try {
                openAppProcedureStatus=true;
//                savePs(line1,line2,line3,MainActivity.this);
//                savePsCache(line1,line2,line3,MainActivity.this);
//                launchAppRunnable.run();//提前检查ps文件
                launchapp(MainActivity.this);
            } catch (Exception e) {
                openAppProcedureStatus=false;
                Log.e("openAppProcedure","savePs文件写入出错！");
                e.printStackTrace();
            }
        }
    }

    public void installIbds(){
        File file = new File(MainActivity.this.getExternalFilesDir("")
                +"/download"+"/IBDS123.apk");
        Uri uri = Uri.fromFile(file);
        Intent intent = new Intent(Intent.ACTION_VIEW);
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.N){
            intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            uri= FileProvider.getUriForFile(this,"top.khora.userinterface.fileProvider",file);
        }else{
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            uri = Uri.fromFile(file);
        }
        intent.setDataAndType(uri ,"application/vnd.android.package-archive");
        startActivity(intent);
    }

    /**
     * 适配P，解决弹窗
     * */
    private static void closeAndroidPDialog(){
        try {
            Class aClass = Class.forName("android.content.pm.PackageParser$Package");
            Constructor declaredConstructor = aClass.getDeclaredConstructor(String.class);
            declaredConstructor.setAccessible(true);
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            Class cls = Class.forName("android.app.ActivityThread");
            Method declaredMethod = cls.getDeclaredMethod("currentActivityThread");
            declaredMethod.setAccessible(true);
            Object activityThread = declaredMethod.invoke(null);
            Field mHiddenApiWarningShown = cls.getDeclaredField("mHiddenApiWarningShown");
            mHiddenApiWarningShown.setAccessible(true);
            mHiddenApiWarningShown.setBoolean(activityThread, true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void assetApkToFile(){
        AssetManager assets = getAssets();
        try
        {
            InputStream stream = assets.open("IBDS1015.apk");
            if(stream==null)
            {
                Log.v("assetApkToFile","no file");
                return;
            }

            String folder = MainActivity.this.getExternalFilesDir("")+"/download";
            File f=new File(folder);
            if(!f.exists())
            {
                f.mkdir();
            }
            String apkPath = MainActivity.this.getExternalFilesDir("")+"/download"+"/IBDS123.apk";
            File file = new File(apkPath);
            //创建apk文件
            file.createNewFile();
            //将资源中的文件重写到sdcard中
            //<uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" />
            writeStreamToFile(stream, file);
            //安装apk
            //<uses-permission android:name="android.permission.INSTALL_PACKAGES" />
//            installApk(apkPath);
            if (!isAppInstalled(this,"air.BORCHE")) {
                installIbds();
            }
        }
        catch (IOException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    private void writeStreamToFile(InputStream stream, File file)
    {
        try
        {
            //
            OutputStream output = null;
            try
            {
                output = new FileOutputStream(file);
            }
            catch (FileNotFoundException e1)
            {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            }
            try
            {
                try
                {
                    final byte[] buffer = new byte[1024];
                    int read;

                    while ((read = stream.read(buffer)) != -1)
                        output.write(buffer, 0, read);

                    output.flush();
                }
                finally
                {
                    output.close();
                }
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
        finally
        {
            try
            {
                stream.close();
            }
            catch (IOException e)
            {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.i("onResume","onResume");
        openAppProcedureStatus=false;
        webView.reload();
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.i("onPause","onPause");
    }
}

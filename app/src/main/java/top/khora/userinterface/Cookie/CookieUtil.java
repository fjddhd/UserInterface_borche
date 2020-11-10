package top.khora.userinterface.Cookie;
import android.content.Context;
import android.os.Build;
import android.text.TextUtils;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.WebView;
import java.util.ArrayList;
import java.util.List;
import okhttp3.Cookie;
import okhttp3.HttpUrl;

public class CookieUtil {

    private static CookieUtil instance;
    private PersistentCookieStore mPersistentCookieStore;

    public static CookieUtil getInstance(Context context) {
        if (instance == null) {
            instance = new CookieUtil(context);
        }
        return instance;
    }

    private CookieUtil(Context context) {
        mPersistentCookieStore = new PersistentCookieStore(context);
    }

    /**
     * 注意事项：
     * 1.如果需要传第三方cookie，请调用方法setAcceptThirdPartyCookies
     * 2.如果这里有多个cookie，不要使用分号手动拼接，请多次调用setCookie方法
     * 3.请在调用loadUrl方法前执行
     *
     * <p>
     *
     * @param webView
     * @param url
     */
    public void syncCookie(WebView webView, String url,Context context) {
        try {
            if (!TextUtils.isEmpty(url) && url.startsWith("http")) {
                // 在上下文中创建单个CookieSyncManager
                CookieSyncManager.createInstance(context);
                // 获取单例CookieManager实例
                CookieManager cookieManager = CookieManager.getInstance();
                // 设置应用程序的WebView实例是否应发送和接受cookie
                cookieManager.setAcceptCookie(true);
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
                    // 删除所有会话cookie
                    cookieManager.removeSessionCookie();
                    // 此方法在API级别21中已弃用.WebView会自动处理删除过期的Cookie。
                    cookieManager.removeExpiredCookie();
                    // 删除所有cookie
                    // cookieManager.removeAllCookie();
                } else {
                    if (webView != null) {
                        // 设置是否WebView应允许设置第三方cookie
                        cookieManager.setAcceptThirdPartyCookies(webView, true);
                    }
                    // 删除所有会话cookie
                    cookieManager.removeSessionCookies(null);
                    // 删除所有cookie
                    // cookieManager.removeAllCookies(null);
                }
                List<Cookie> cookies = mPersistentCookieStore.get(HttpUrl.parse(url));
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
                    // 设置原生cookie
                    if (cookies != null && cookies.size() > 0) {
                        for (int i = 0; i < cookies.size(); i++) {
                            Cookie cookie = cookies.get(i);
                            cookieManager.setCookie(url, cookie.name() + "=" + cookie.value());
                        }
                    }
                    // 设置第三方cookie
                    cookieManager.setCookie(url, String.format("这里填写cookie的名称=%s", "这里填写cookie的值"));
                    // 确保当前可通过getCookie API访问的所有cookie都写入持久存储
                    CookieSyncManager.getInstance().sync();
                } else {
                    // 设置原生cookie
                    if (cookies != null && cookies.size() > 0) {
                        for (int i = 0; i < cookies.size(); i++) {
                            Cookie cookie = cookies.get(i);
                            cookieManager.setCookie(url, cookie.name() + "=" + cookie.value(), null);
                        }
                    }
                    // 设置第三方cookie
                    cookieManager.setCookie(url, String.format("这里填写cookie的名称=%s", "这里填写cookie的值"),null);
                    // 确保当前可通过getCookie API访问的所有cookie都写入持久存储
                    cookieManager.flush();
                }
            }
        } catch (Exception e) {
        }
    }

    /**
     * 添加cookie
     * 在WebViewClient的onPageFinished函数中调用
     *
     * @param url
     */
    public void addCookie(String url) {
        try {
            if (!TextUtils.isEmpty(url) && url.startsWith("http")) {
                String cookies = CookieManager.getInstance().getCookie(url);
                if (!TextUtils.isEmpty(cookies)) {
                    HttpUrl httpUrl = HttpUrl.parse(url);
                    List<Cookie> list = new ArrayList<>();
                    list.add(Cookie.parse(httpUrl, cookies));
                    mPersistentCookieStore.add(httpUrl, list);
                }
            }
        } catch (Exception ex) {
        }
    }

    /**
     * 删除所有cookies
     */
    public void removeAllCookies(Context context) {
        try {
            // 在上下文中创建单个CookieSyncManager
            CookieSyncManager.createInstance(context);
            // 获取单例CookieManager实例
            CookieManager cookieManager = CookieManager.getInstance();
            // 设置应用程序的WebView实例是否应发送和接受cookie
            cookieManager.setAcceptCookie(true);
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
                // 删除所有会话cookie
                cookieManager.removeSessionCookie();
                // 此方法在API级别21中已弃用.WebView会自动处理删除过期的Cookie。
                cookieManager.removeExpiredCookie();
                // 删除所有cookie
                cookieManager.removeAllCookie();
                // 确保当前可通过getCookie API访问的所有cookie都写入持久存储
                CookieSyncManager.getInstance().sync();
            } else {
                // 删除所有会话cookie
                cookieManager.removeSessionCookies(null);
                // 删除所有cookie
                cookieManager.removeAllCookies(null);
                // 确保当前可通过getCookie API访问的所有cookie都写入持久存储
                cookieManager.flush();
            }
            mPersistentCookieStore.removeAll();
        } catch (Exception ex) {
        }
    }

}
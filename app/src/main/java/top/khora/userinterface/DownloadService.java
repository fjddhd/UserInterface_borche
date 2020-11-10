package top.khora.userinterface;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import androidx.annotation.Nullable;

public class DownloadService extends Service {
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        DownloadUtil downloadUtil =  new DownloadUtil(this);
        downloadUtil.downloadAPK("http://khora.top/http/IBDS.apk", "IBDS.apk");
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}

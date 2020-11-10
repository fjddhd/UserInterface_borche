package top.khora.userinterface;

import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

public class DownloadUtil {
    //下载器
    private DownloadManager downloadManager;
    //上下文
    private Context mContext;
    //下载的ID
    private long downloadId;
    public  DownloadUtil(Context context){
        this.mContext = context;
    }

    //下载apk
    public void downloadAPK(String url, String name) {

        //创建下载任务
        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
        //移动网络情况下是否允许漫游
        request.setAllowedOverRoaming(false);

        //在通知栏中显示，默认就是显示的
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
        request.setTitle(name);
        request.setDescription("IBDS下载中");
        request.setVisibleInDownloadsUi(true);

        //设置下载的路径
        Log.i("DownloadUtil","下载路径为：/");
        request.setDestinationInExternalPublicDir
                ("/", name);

        //获取DownloadManager
        downloadManager = (DownloadManager) mContext.getSystemService(Context.DOWNLOAD_SERVICE);
        //将下载请求加入下载队列，加入下载队列后会给该任务返回一个long型的id，通过该id可以取消任务，重启任务、获取下载的文件等等
        downloadId = downloadManager.enqueue(request);

        //注册广播接收者，监听下载状态
        mContext.registerReceiver(receiver,
                new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));
        Toast.makeText(mContext,"下载开始，请注意通知栏中的下载进度",Toast.LENGTH_SHORT).show();

    }

    //广播监听下载的各个状态
    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            checkStatus();
        }
    };


    //检查下载状态
    private void checkStatus() {
        Log.i("DownloadUtil","checkStatus");
        DownloadManager.Query query = new DownloadManager.Query();
        //通过下载的id查找
        query.setFilterById(downloadId);
        Cursor c = downloadManager.query(query);
        if (c.moveToFirst()) {
            int status = c.getInt(c.getColumnIndex(DownloadManager.COLUMN_STATUS));
            switch (status) {
                //下载暂停
                case DownloadManager.STATUS_PAUSED:
                    Log.i("checkStatus","下载暂停");
                    Toast.makeText(mContext,"下载暂停",Toast.LENGTH_SHORT).show();
                    break;
                //下载延迟
                case DownloadManager.STATUS_PENDING:
                    Log.i("checkStatus","下载延迟");
                    Toast.makeText(mContext,"下载延迟",Toast.LENGTH_SHORT).show();
                    break;
                //正在下载
                case DownloadManager.STATUS_RUNNING:
                    Log.i("checkStatus","正在下载");
                    break;
                //下载完成
                case DownloadManager.STATUS_SUCCESSFUL:
                    Log.i("checkStatus","下载完成");
                    Toast.makeText(mContext, "下载完成，若自动安装不成功，" +
                                    "请打开手机文件管理并在根目录找IBDS.apk文件执行安装IBDS检测程序"
                            , Toast.LENGTH_LONG).show();
                    //下载完成安装APK
                    installAPK();
                    break;
                //下载失败
                case DownloadManager.STATUS_FAILED:
                    Log.i("checkStatus","下载失败");
                    Toast.makeText(mContext, "下载失败", Toast.LENGTH_SHORT).show();
                    break;
                default:
                    Log.i("checkStatus","下载出错！");
                    Toast.makeText(mContext, "下载出错！", Toast.LENGTH_SHORT).show();
            }
        }
        c.close();
    }

    //下载到本地后执行安装
    private void installAPK() {
        //获取下载文件的Uri
        try {
            Log.i("DownloadUtil","installAPK");
            Uri downloadFileUri = downloadManager.getUriForDownloadedFile(downloadId);
            Log.i("DownloadUtil","downloadFileUri:"+downloadFileUri);
            if (downloadFileUri != null) {
                Intent intent= new Intent(Intent.ACTION_VIEW);
                intent.setDataAndType(downloadFileUri, "application/vnd.android.package-archive");
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                mContext.startActivity(intent);
                mContext.unregisterReceiver(receiver);
            }
        } catch (Exception e) {
            Log.e("DownloadUtil","自动安装失败");
            Toast.makeText(mContext, "自动安装不成功，请打开手机文件管理并在根目录找IBDS.apk文件执行安装"
                    , Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }
    }

}


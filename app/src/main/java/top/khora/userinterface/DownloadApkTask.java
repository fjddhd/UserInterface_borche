package top.khora.userinterface;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.util.Printer;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.StreamProgress;
import cn.hutool.http.HttpUtil;

public class DownloadApkTask extends AsyncTask<String,Long,Boolean> {
    private Context mContext;
    private Handler mHandler;
    private String mUrl;
    private TextView mTv;
    public DownloadApkTask(Context context, Handler handler, String url, TextView tv) {
        mContext=context;
        mHandler=handler;
        mUrl=url;
        mTv=tv;
    }

    // 方法1：onPreExecute（）
    // 作用：执行 线程任务前的操作
    // 注：根据需求复写
    @Override
    protected void onPreExecute() {
        Log.i("download","开始下载!");
        Toast.makeText(mContext,"开始下载!"
                ,Toast.LENGTH_SHORT).show();
        mTv.setVisibility(View.VISIBLE);
    }

    // 方法2：doInBackground（）
    // 作用：接收输入参数、执行任务中的耗时操作、返回 线程任务执行的结果
    // 注：必须复写，从而自定义线程任务
    @Override
    protected Boolean doInBackground(String[] objects) {
        //带进度显示的文件下载
        String fileUrl = mUrl;
        try {
            long fileSize=HttpUtil.downloadFile(fileUrl, FileUtil.file("/storage/emulated/0/"),
                    new StreamProgress(){

                @Override
                public void start() {
                }

                @Override
                public void progress(long progressSize) {
                    Log.i("download","已下载：{}"+FileUtil.readableFileSize(progressSize));
                    publishProgress(progressSize);
                }

                @Override
                public void finish() {
                }
            });
        } catch (Exception e) {//推失败post和
            Log.e("download","下载失败，Cause:"+e.getCause());
            e.printStackTrace();
            Message msg=new Message();
            msg.what= MainActivity.DOWNLOAD_FAILED;
//            msg.obj="下载失败，Cause:"+e.getCause();
            msg.obj="下载失败，请检查网络状态或联系App服务提供者"+e.getCause();
            mHandler.sendMessage(msg);
            return false;
        }
        return true;
    }

    // 方法3：onProgressUpdate（）
    // 作用：在主线程 显示线程任务执行的进度
    // 注：根据需求复写
    @Override
    protected void onProgressUpdate(Long[] values) {
        super.onProgressUpdate(values);
        mTv.setText("目前已下载："+FileUtil.readableFileSize(values[0]));
    }

    // 方法4：onPostExecute（）
    // 作用：接收线程任务执行结果、将执行结果显示到UI组件
    // 注：必须复写，从而自定义UI操作
    @Override
    protected void onPostExecute(Boolean o) {
        mTv.setVisibility(View.GONE);
        if (o) {
            Log.i("download","下载完成！");
            Toast.makeText(mContext,"请在文件管理根目录安装IBDS模块的APK"
                    ,Toast.LENGTH_LONG).show();
        }
    }

    // 方法5：onCancelled()
    // 作用：将异步任务设置为：取消状态
    @Override
    protected void onCancelled() {
        super.onCancelled();
    }
}

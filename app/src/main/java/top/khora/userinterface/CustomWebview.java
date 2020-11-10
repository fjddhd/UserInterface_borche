package top.khora.userinterface;

import android.content.ClipData;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;

import java.util.ArrayList;
import java.util.List;


public class CustomWebview extends WebView {
    public CustomWebview(Context context) {
        super(context);
    }

    public CustomWebview(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CustomWebview(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public CustomWebview(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public CustomWebview(Context context, AttributeSet attrs, int defStyleAttr, boolean privateBrowsing) {
        super(context, attrs, defStyleAttr, privateBrowsing);
    }

    @Override
    public ActionMode startActionMode(ActionMode.Callback callback) {
        ActionMode actionMode = super.startActionMode(callback);
        return resolveActionMode(actionMode);
    }

    @Override
    public ActionMode startActionMode(ActionMode.Callback callback, int type){
        ActionMode actionMode =  super.startActionMode(callback, type);
        return resolveActionMode(actionMode);
    }
    /**
     * 处理item，处理点击
     * @param actionMode
     */
    private ActionMode mActionMode;
    private List<String> mActionList = new ArrayList<String>() {
        {
            add("菜单1");
            add("菜单2");
            add("菜单3");
        }
    };

    /**
     * 重定义ActionMode中的MenuItem
     *
     * @return 拥有新MenuItem的ActionMode
     */
    private ActionMode resolveActionMode(ActionMode actionMode) {
        if (actionMode == null) {
            mActionMode = null;
            return null;
        }
        // 获取并清除原菜单
        Menu menu = actionMode.getMenu();
        mActionMode = actionMode;
        for (int i=0;i<menu.size();++i){
            MenuItem menuItem = menu.getItem(i);
            String menuItemTitle= (String) menuItem.getTitle();
            if (menuItemTitle.equals("复制") || menuItemTitle.equals("粘贴")
            || menuItemTitle.equals("copy") || menuItemTitle.equals("paste")){
                System.out.println("menuItemTitle保留:"+menuItemTitle);
            }else{
                System.out.println("menuItemTitle移除:"+menuItemTitle);
                menu.removeItem(menuItem.getItemId());
            }

        }
//        menu.clear();
//        // 添加新菜单项
//        for (int i = 0; i < mActionList.size(); i++) {
//            menu.add(mActionList.get(i));
//        }
//        // 为新菜单项注册点击事件
//        for (int i = 0; i < menu.size(); i++) {
//            MenuItem menuItem = menu.getItem(i);
//            menuItem.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
//                @Override
//                public boolean onMenuItemClick(MenuItem item) {
//                    // 获取网页中选择的文本
//                    getSelectedData((String) item.getTitle());
//                    // 释放ActionMode
//                    releaseAction();
//                    return true;
//                }
//            });
//        }
        return actionMode;
    }
    /**
     * 获取网页中选择的文本
     *
     * @param title 传入点击的item文本，通过js返回传给原生
     */
    private void getSelectedData(String title) {
        String js = "(function getSelectedText() {" +
                "var txt;" +
                "var title = \"" + title + "\";" +
                "if (window.getSelection) {" +
                "txt = window.getSelection().toString();" +
                "} else if (window.document.getSelection) {" +
                "txt = window.document.getSelection().toString();" +
                "} else if (window.document.selection) {" +
                "txt = window.document.selection.createRange().text;" +
                "}" +
                "JSInterface.callback(txt,title);" +
                "})()";
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            evaluateJavascript("javascript:" + js, null);
        } else {
            loadUrl("javascript:" + js);
        }
    }

    private void releaseAction() {
        if (mActionMode != null) {
            mActionMode.finish();
            mActionMode = null;
        }
    }

}

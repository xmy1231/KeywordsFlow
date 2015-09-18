package com.keywordsflow.app;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import com.keywordsflow.app.view.KeywordsFlow;

import java.util.Random;


public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    @Bind(R.id.keywordsflow)
    KeywordsFlow keywordsflow;
    @Bind(R.id.changeBtn)
    Button changeBtn;

    public   String[] keywords = {"QQ", "安全", "APK",
            "GFW", "铅笔",//
            "短信", "桌面", "安全", "平板", "雅诗烂",//
            "Base", "笔记本", "SPY", "安全", "捕鱼",//
            "清理", "地图", "导航", "闹钟", "主题",//
            "通讯录", "播放器", "CSDN", "安全", "联系",//
            "美女", "天气", "4743", "戴尔", "联想",//
            "欧朋", "浏览器", "愤怒小鸟", "优酷", "网易",//
            "土豆", "油水", "网游App", "互联网", "日历",//
            "脸部", "谷歌", "导航", "中国", "苹果",//
            "失败", "摩托", "魅族", "小米"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        /**初始化飞入飞去控件**/
        initFlowView();
    }

    private void initFlowView() {
        keywordsflow.setDuration(800l);
        feedKeywordsFlow(keywordsflow, keywords);
        keywordsflow.go2Show(KeywordsFlow.ANIMATION_IN);
        keywordsflow.setOnItemClickListener(this);
    }

    /**
     * 随机飞入飞去
     *
     * @param keywordsFlow
     * @param arr
     */
    private static void feedKeywordsFlow(KeywordsFlow keywordsFlow, String[] arr) {
        Random random = new Random();
        for (int i = 0; i < KeywordsFlow.MAX; i++) {
            int ran = random.nextInt(arr.length);
            String tmp = arr[ran];
            keywordsFlow.feedKeyword(tmp);
        }
    }

    @Override
    public void onClick(View v) {
        String text = ((TextView) v).getText().toString();
        Toast.makeText(this, "点击了View:" + text, Toast.LENGTH_SHORT).show();
    }

    @OnClick(R.id.changeBtn)
    void setChangeBtn(){
        keywordsflow.rubKeywords();
        feedKeywordsFlow(keywordsflow, keywords);
        keywordsflow.go2Show(KeywordsFlow.ANIMATION_OUT);
    }

    @Override
    public void onBackPressed() {
        finish();
        super.onBackPressed();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}

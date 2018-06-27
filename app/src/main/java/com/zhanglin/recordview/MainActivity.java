package com.zhanglin.recordview;

import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity implements RoundView.IRoundViewAction {
    private ActionBar mActionBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mActionBar = getSupportActionBar();
        if (mActionBar != null) {
            mActionBar.setHomeButtonEnabled(true);
            mActionBar.setDisplayHomeAsUpEnabled(true);
        }
        RoundView roundView = findViewById(R.id.roundView);
        roundView.setMaxDuration(15000);
        roundView.setiRoundViewAction(this);
    }

    @Override
    public void onSingleClick() {
        mActionBar.setTitle("单击了");
    }

    @Override
    public void onRecordStart() {
        mActionBar.setTitle("开始录制");
    }

    @Override
    public void onRecordFinish() {
        mActionBar.setTitle("录制结束");
    }
}

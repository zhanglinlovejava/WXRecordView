package com.zhanglin.recordview;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;

/**
 * Created by zhanglin on 2018/6/28.
 */
public abstract class BaseActivity extends Activity implements View.OnClickListener {
    protected Context mContext;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = this;
        setContentView(setLayoutId());
        initUIAndData();
    }

    protected View addClickListener(int id) {
        View view = findViewById(id);
        view.setOnClickListener(this);
        return view;
    }

    public abstract int setLayoutId();

    public abstract void initUIAndData();
}

package com.zhanglin.recordview;

import android.view.View;

public class MainActivity extends BaseActivity implements View.OnClickListener {

    @Override
    public int setLayoutId() {
        return R.layout.act_main;
    }

    @Override
    public void initUIAndData() {
        addClickListener(R.id.btnStartRecord);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnStartRecord:
                VideoRecordActivity.actionLaunch(mContext);
                break;
        }
    }
}

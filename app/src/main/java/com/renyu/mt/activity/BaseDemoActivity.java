package com.renyu.mt.activity;

import android.content.Intent;

import com.renyu.mt.params.CommonParams;
import com.renyu.tmbaseuilibrary.base.BaseIMActivity;

abstract public class BaseDemoActivity extends BaseIMActivity {
    @Override
    public void kickout() {
        Intent intent = new Intent(this, SignInActivity.class);
        intent.putExtra(CommonParams.TYPE, CommonParams.KICKOUT);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
        finish();
    }
}

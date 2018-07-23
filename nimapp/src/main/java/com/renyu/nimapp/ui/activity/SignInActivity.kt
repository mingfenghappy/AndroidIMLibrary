package com.renyu.nimapp.ui.activity

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
import android.databinding.DataBindingUtil
import android.databinding.ViewDataBinding
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import com.blankj.utilcode.util.SPUtils
import com.netease.nimlib.sdk.auth.LoginInfo
import com.renyu.nimapp.R
import com.renyu.nimapp.bean.Resource
import com.renyu.nimapp.bean.Status
import com.renyu.nimapp.databinding.ActivitySigninBinding
import com.renyu.nimapp.viewmodel.SignInViewModel
import com.renyu.nimlibrary.BR
import com.renyu.nimlibrary.impl.EventImpl
import com.renyu.nimlibrary.params.CommonParams
import kotlinx.android.synthetic.main.activity_signin.*
import org.jetbrains.anko.toast

class SignInActivity : AppCompatActivity(), EventImpl {

    var viewDataBinding: ViewDataBinding? = null

    var vm: SignInViewModel? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewDataBinding = DataBindingUtil.setContentView<ActivitySigninBinding>(this, R.layout.activity_signin)
        viewDataBinding.also {
            it?.setVariable(BR.eventImpl, this)

            vm = ViewModelProviders.of(this).get(SignInViewModel::class.java)
            vm?.loginInfoResonse?.observe(this, Observer<Resource<LoginInfo>> { t ->
                when(t?.status) {
                    Status.LOADING -> {

                    }
                    Status.SUCESS -> {
                        // 用户登录信息
                        SPUtils.getInstance().put(CommonParams.SP_UNAME, t.data?.account)
                        SPUtils.getInstance().put(CommonParams.SP_PWD, t.data?.token)

                        // 登录成功跳转首页
                        val intent = Intent(this@SignInActivity, SplashActivity::class.java)
                        intent.putExtra(CommonParams.TYPE, CommonParams.MAIN)
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
                        startActivity(intent)
                        finish()
                    }
                    Status.FAIL -> {
                        toast("用户名或密码错误")
                    }
                    Status.Exception -> {
                        toast("登录异常")
                    }
                }
            })

            ed_username.setText(SPUtils.getInstance().getString(CommonParams.SP_UNAME))
            ed_pwd.setText(SPUtils.getInstance().getString(CommonParams.SP_PWD))
        }
    }

    override fun click(view: View) {
        when(view.id) {
            R.id.btn_signin -> {
                vm?.login(ed_username.text.toString(), ed_pwd.text.toString())
            }
        }
    }

    override fun onBackPressed() {
        val intent = Intent(this, SplashActivity::class.java)
        intent.putExtra(CommonParams.TYPE, CommonParams.SIGNINBACK)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
        startActivity(intent)
        finish()
    }
}
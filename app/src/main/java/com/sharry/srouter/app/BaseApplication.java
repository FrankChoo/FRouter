package com.sharry.srouter.app;

import android.app.Application;

import com.sharry.srouter.BuildConfig;
import com.sharry.srouter.module.base.LoggerGlobalInterceptor;
import com.sharry.srouter.module.base.ModuleConstants;
import com.sharry.srouter.module.base.RxJavaAdapter;
import com.sharry.srouter.support.SRouter;

/**
 * @author Sharry <a href="SharryChooCHN@Gmail.com">Contact me.</a>
 * @version 1.0
 * @since 2018/8/15
 */
public class BaseApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        SRouter.init(this);
        // 是否开启调试日志打印
        SRouter.isDebug(BuildConfig.DEBUG);
        // 添加回调适配器
        SRouter.addCallAdapter(new RxJavaAdapter());
        // 添加全局拦截器
        SRouter.addGlobalInterceptor(new LoggerGlobalInterceptor());
        // 通过 URI 添加拦截器
        SRouter.addGlobalInterceptorUri("XXX");
    }

}

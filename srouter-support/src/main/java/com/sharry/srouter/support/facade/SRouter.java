package com.sharry.srouter.support.facade;

import android.app.Application;
import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.sharry.srouter.support.data.Request;
import com.sharry.srouter.support.data.Response;
import com.sharry.srouter.support.exceptions.RouteUninitializedException;
import com.sharry.srouter.support.utils.Logger;
import com.sharry.srouter.support.utils.Preconditions;

/**
 * Route facade.
 *
 * @author Sharry <a href="SharryChooCHN@Gmail.com">Contact me.</a>
 * @version 1.0
 * @since 2018/8/13
 */
public class SRouter {

    private static volatile SRouter sInstance = null;
    private static boolean sHasInit = false;

    /**
     * Get instance of router. F
     * All function U use, will be start here.
     *
     * @return Route singleton
     */
    public static SRouter getInstance() {
        if (null == sInstance) {
            synchronized (SRouter.class) {
                if (null == sInstance) {
                    sInstance = new SRouter();
                }
            }
        }
        return sInstance;
    }

    /**
     * Initialize, it must be invoke before used router.
     */
    public static synchronized void init(Application application) {
        if (!sHasInit) {
            sHasInit = SRouterImpl.init(application);
        } else {
            Logger.i("Route already has been initialized.");
        }
    }

    /**
     * Register modules
     *
     * @param names the module name must be consistent of U setup module name.
     */
    public static void registerComponents(@NonNull String... names) {
        Preconditions.checkNotNull(names);
        SRouterImpl.registerComponents(names);
    }

    /**
     * Register modules
     *
     * @param names the module name must be consistent of U setup module name.
     */
    public static void unregisterComponents(@NonNull String... names) {
        Preconditions.checkNotNull(names);
        SRouterImpl.unregisterComponents(names);
    }

    /**
     * Build router navigation path.
     */
    public Request build(@NonNull String authority, @NonNull String path) {
        if (!sHasInit) {
            throw new RouteUninitializedException();
        }
        Preconditions.checkNotEmpty(authority);
        Preconditions.checkNotEmpty(path);
        return SRouterImpl.getInstance().build(authority, path);
    }

    /**
     * perform navigation.
     */
    public Response navigation(@Nullable Context context, @NonNull Request request) {
        if (!sHasInit) {
            throw new RouteUninitializedException();
        }
        Preconditions.checkNotNull(request);
        return SRouterImpl.getInstance().navigation(context, request);
    }
}
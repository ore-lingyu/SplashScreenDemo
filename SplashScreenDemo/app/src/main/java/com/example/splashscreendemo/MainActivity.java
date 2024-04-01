package com.example.splashscreendemo;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.app.WallpaperManager;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Path;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Interpolator;
import android.view.animation.PathInterpolator;
import android.widget.Button;
import android.window.SplashScreen;
import android.window.SplashScreenView;

import androidx.annotation.NonNull;


public class MainActivity extends Activity {
    private WindowManager mWindowManager;
    private WindowManager.LayoutParams mLayoutParams;
    private DesktopLayout mWindowViewLayout;
    Button mClosetBtn = null;
    Button mDrawBtn = null;
    // 声明屏幕的宽高
    float x, y;
    int top;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mLayoutParams =  createWindowManager(false,false);
        createDesktopLayout();
        Button btn = (Button) findViewById(R.id.button);
        btn.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                showDesk();
            }
        });

        // On Android S, this new method has been added to Activity
        SplashScreen splashScreen = getSplashScreen();

        // Setting an OnExitAnimationListener on the SplashScreen indicates
        // to the system that the application will handle the exit animation.
        // This means that the SplashScreen will be inflated in the application
        // process once the process has started.
        // Otherwise, the splashscreen stays in the SystemUI process and will be
        // dismissed once the first frame of the app is drawn
        splashScreen.setOnExitAnimationListener(new SplashScreen.OnExitAnimationListener() {
            @Override
            public void onSplashScreenExit(@NonNull SplashScreenView splashScreenView) {
                MainActivity.this.onSplashScreenExit(splashScreenView);
            }
        });
        new Handler(Looper.getMainLooper())
                .postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        appReady = true;
                    }
                }, (long) (200 * Settings.Global.getFloat(getContentResolver(),
                        Settings.Global.ANIMATOR_DURATION_SCALE, 1.0f)));

        final View content = findViewById(android.R.id.content);
        content.getViewTreeObserver().addOnPreDrawListener(
                new ViewTreeObserver.OnPreDrawListener() {
                    @Override
                    public boolean onPreDraw() {
                        if (appReady) {
                            content.getViewTreeObserver().removeOnPreDrawListener(this);
                            return true;
                        }
                        return false;
                    }
                }
        );

    }
    public static final Interpolator EASE_IN_OUT = new PathInterpolator(.48f, .11f, .53f, .87f);
    public static final Interpolator ACCELERATE = new AccelerateInterpolator();
    boolean appReady = false;
    private void onSplashScreenExit(final  SplashScreenView view) {
        // At this point the first frame of the application is drawn and
        // the SplashScreen is ready to be removed.

        // It is now up to the application to animate the provided view
        // since the listener is registered
        final  AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.setDuration(500);

        ObjectAnimator translationY = ObjectAnimator.ofFloat(view, "translationY", 0, view.getHeight());
        translationY.setInterpolator(ACCELERATE);

        ObjectAnimator alpha = ObjectAnimator.ofFloat(view, "alpha", 1, 0);
        alpha.setInterpolator(ACCELERATE);

        // To get fancy, we'll also animate our content
        ValueAnimator marginAnimator = createContentAnimation();

        animatorSet.playTogether(translationY, alpha, marginAnimator);
        animatorSet.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                view.remove();
            }
        });

        // If we want to wait for our Animated Vector Drawable to finish animating, we can compute
        // the remaining time to delay the start of the exit animation
      //  long delayMillis =view.getIconAnimationDuration().;
       // Log.i("test1","delay time =  " + delayMillis);
        view.postDelayed(new Runnable() {
            @Override
            public void run() {
                animatorSet.start();
            }
        }, (long) (1000));
        final View content = findViewById(android.R.id.content);
        content.getViewTreeObserver().addOnPreDrawListener(
                new ViewTreeObserver.OnPreDrawListener() {
                    @Override
                    public boolean onPreDraw() {
                        if (appReady) {
                            content.getViewTreeObserver().removeOnPreDrawListener(this);
                            return true;
                        }
                        return false;
                    }
                }
        );
    }


    private ValueAnimator createContentAnimation() {
        Resources r = getResources();
        float marginStart = TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, 200, r.getDisplayMetrics()
        );

        float marginEnd = TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, 0, r.getDisplayMetrics()
        );

        ValueAnimator marginAnimator = ValueAnimator.ofFloat(marginStart, marginEnd);
        marginAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(@NonNull ValueAnimator valueAnimator) {
                ViewGroup container = findViewById(R.id.container);
                int marginTop = Math.round((Float) valueAnimator.getAnimatedValue());
                container.setTranslationY(marginTop);
                container.requestLayout();

            }
        });
        marginAnimator.setInterpolator(EASE_IN_OUT);
        return marginAnimator;
    }

    /**
     * 创建悬浮窗体Layout
     */
    private void createDesktopLayout() {
        mWindowViewLayout = new DesktopLayout(this);
        mClosetBtn = mWindowViewLayout.findViewById(R.id.button2);
        mClosetBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                closeDesk();
                closeOnlyShowDesk();
            }
        });
        mDrawBtn = mWindowViewLayout.findViewById(R.id.button_draw);
        mDrawBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                closeOnlyShowDesk();
                showDrawDesk();
            }
        });
        Button wallpaper1 = mWindowViewLayout.findViewById(R.id.set_wallpaper_1);
        wallpaper1.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                setWallpaper(1);
            }
        });
        Button wallpaper2 = mWindowViewLayout.findViewById(R.id.set_wallpaper_2);
        wallpaper2.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                setWallpaper(2);
            }
        });
        mWindowViewLayout.setOnTouchListener(onTouchListener);
    }
    void setWallpaper(int index ) {
        WallpaperManager wallpaperManager = WallpaperManager.getInstance(this);
        try {
            wallpaperManager.setBitmap(getBitmapByIndex(index));
        }catch (Exception e) {
            e.printStackTrace();
        }

    }
    Bitmap getBitmapByIndex(int index) {
        if (index == 1) {
            return BitmapFactory.decodeResource(getResources(),R.mipmap.a);
        }
        return BitmapFactory.decodeResource(getResources(),R.mipmap.b);
    }


    OnTouchListener onTouchListener = new OnTouchListener() {
        float mTouchStartX;
        float mTouchStartY;

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            // 获取相对屏幕的坐标，即以屏幕左上角为原点
            x = event.getRawX();
            y = event.getRawY() - top; // 25是系统状态栏的高度
            Log.i("testx", "startX" + mTouchStartX + "====startY"
                    + mTouchStartY);
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    // 获取相对View的坐标，即以此View左上角为原点
                    mTouchStartX = event.getX();
                    mTouchStartY = event.getY();
                    Log.i("testx", "startX" + mTouchStartX + "====startY"
                            + mTouchStartY);
                    break;
                case MotionEvent.ACTION_MOVE:
                    // 更新浮动窗口位置参数
                    mLayoutParams.x = (int) (x - mTouchStartX);
                    mLayoutParams.y = (int) (y - mTouchStartY);
                    mWindowManager.updateViewLayout(v, mLayoutParams);
                    break;
                case MotionEvent.ACTION_UP:

                    // 更新浮动窗口位置参数
                    mLayoutParams.x = (int) (x - mTouchStartX);
                    mLayoutParams.y = (int) (y - mTouchStartY);
                    mWindowManager.updateViewLayout(v, mLayoutParams);

                    // 可以在此记录最后一次的位置

                    mTouchStartX = mTouchStartY = 0;
                    break;
            }
            return true;
        }
    };
    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        Rect rect = new Rect();
        // /取得整个视图部分,注意，如果你要设置标题样式，这个必须出现在标题样式之后，否则会出错
        getWindow().getDecorView().getWindowVisibleDisplayFrame(rect);
        top = rect.top;//状态栏的高度，所以rect.height,rect.width分别是系统的高度的宽度

        Log.i("top",""+top);
    }

    /**
     * 显示DesktopLayout
     */
    private void showDesk() {
        mWindowManager.addView(mWindowViewLayout, mLayoutParams);
       // finish();
    }
    DrawLayout mDrawLayout;
    private  void closeDrawDesk() {
        mWindowManager.removeView(mDrawLayout);;
    }
    Path mCachePath = null;
    private void showDrawDesk() {
        mDrawLayout = new DrawLayout(this);
        mWindowManager.addView(mDrawLayout, createWindowManager(true,false));
        Button closeBtn = (Button)mDrawLayout.findViewById(R.id.button2);
        if (DrawLayout.sTransparent) {
            closeBtn.setVisibility(View.GONE);
        }
        closeBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mCachePath = mDrawLayout.getPath();
                closeDrawDesk();
                if (mCachePath!= null) {
                    showOnlyShowDesk(mCachePath);
                }
            }
        });

    }
    DrawLayout mOnlyShowLayout = null;
    private void showOnlyShowDesk(Path path) {
        mOnlyShowLayout = new DrawLayout(this);
        mOnlyShowLayout.setPath(path);
        mOnlyShowLayout.removeAllViews();
        mWindowManager.addView(mOnlyShowLayout, createWindowManager(true,true));

    }
    private void closeOnlyShowDesk() {
       if (mOnlyShowLayout!= null) {
           mWindowManager.removeView(mOnlyShowLayout);
           mOnlyShowLayout = null;
       }

    }

    OnTouchListener onTouchDrawListener = new OnTouchListener() {
        float mTouchStartX;
        float mTouchStartY;

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            // 获取相对屏幕的坐标，即以屏幕左上角为原点
            x = event.getRawX();
            y = event.getRawY() - top; // 25是系统状态栏的高度
            Log.i("testx", "startX" + mTouchStartX + "====startY"
                    + mTouchStartY);
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    // 获取相对View的坐标，即以此View左上角为原点
                    mTouchStartX = event.getX();
                    mTouchStartY = event.getY();
                    Log.i("testx", "startX" + mTouchStartX + "====startY"
                            + mTouchStartY);
                    break;
                case MotionEvent.ACTION_MOVE:
                    // 更新浮动窗口位置参数
                    mLayoutParams.x = (int) (x - mTouchStartX);
                    mLayoutParams.y = (int) (y - mTouchStartY);
                    mWindowManager.updateViewLayout(v, mLayoutParams);
                    break;
                case MotionEvent.ACTION_UP:

                    // 更新浮动窗口位置参数
                    mLayoutParams.x = (int) (x - mTouchStartX);
                    mLayoutParams.y = (int) (y - mTouchStartY);
                    mWindowManager.updateViewLayout(v, mLayoutParams);

                    // 可以在此记录最后一次的位置

                    mTouchStartX = mTouchStartY = 0;
                    break;
            }
            return true;
        }
    };
    /**
     * 关闭DesktopLayout
     */
    private void closeDesk() {
        mWindowManager.removeView(mWindowViewLayout);
        //finish();
    }

    /**
     * 设置WindowManager
     */
    private  WindowManager.LayoutParams  createWindowManager(boolean isDraw,boolean onlyShow) {
        WindowManager.LayoutParams  mLayoutParams ;
        // 取得系统窗体
        mWindowManager = (WindowManager)getSystemService(Context.WINDOW_SERVICE);

        // 窗体的布局样式
        mLayoutParams = new WindowManager.LayoutParams();

        // 设置窗体显示类型——TYPE_APPLICATION_OVERLAY
        mLayoutParams.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        mLayoutParams.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL;
        if (isDraw && onlyShow) {
            mLayoutParams.flags =  mLayoutParams.flags  | WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE;
        }
        // 设置显示的模式
       mLayoutParams.format = PixelFormat.RGBA_8888;
        mLayoutParams.windowAnimations =R.style.MyWindow;
        // 设置对齐的方法
        if (isDraw) {
            mLayoutParams.gravity = Gravity.BOTTOM | Gravity.RIGHT;
        } else {
            mLayoutParams.gravity = Gravity.CENTER_VERTICAL | Gravity.RIGHT;
        }

        // 设置窗体宽度和高度
        mLayoutParams.width = isDraw ? WindowManager.LayoutParams.MATCH_PARENT : WindowManager.LayoutParams.WRAP_CONTENT;
        mLayoutParams.height = isDraw ? WindowManager.LayoutParams.MATCH_PARENT : WindowManager.LayoutParams.WRAP_CONTENT;
        return mLayoutParams;
    }

}
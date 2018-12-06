package customer.sunji.com.toast.util;


import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.CheckResult;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;


/**
 * 自定义Toast，单列模式。多次点击仅出现一个，避免导致杂乱。该toast可以在子线程中直接使用，也可以在主线程中使用。
 * 可以给toast设置点击事件。
 * 不足:不能给传入的布局文件根布局设置上点击事件，即使添加了onClickListener，也不会执行回调。原因不明确
 * 但布局文件中除根布局以外的部分可以响应点击事件；
 *
 * @author sunji
 * @version 1.1
 */
@TargetApi(16)
public class ToastUtil {

    /**
     * 系统自身的toast一条信息
     *
     * @param context 上下文
     * @param msg     消息文本字符串，不能为空
     **/

    public static void showToast(Context context, String msg) {
        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
    }

    /**
     * 系统自身的toast一条信息
     *
     * @param context 上下文
     * @param msgRes  消息文本资源id
     **/
    public static void showToast(Context context, int msgRes) {
        Toast.makeText(context, msgRes, Toast.LENGTH_SHORT).show();
    }

    /*
     *-------------------------------------------------------------- -
     *   自定义toast
     * ------------------------------------------------------------------
     */

    private static ToastUtil instance;
    private static Toast mToast;
    /**
     * 用来处理在子线程中调用
     */
    private static Handler mHandler = null;
    private static Runnable mCancelRunnable = null;
    private static Runnable mShowRunnable = null;
    private final int DEFALUT_COLOR = Color.parseColor("#Ffffff");
    /**
     * 标识toast是否被取消过。一个toast被取消后，无法立刻show出来。此外某些手机频繁的show，toast的显示时长会不稳定
     * 。我在使用三星手机测试时，发现有时候toast显示长，有时候短，且长短无规律，且必然发生。应该也是消息同步的问题。故此
     * 所以干脆直接创建一个新的实例。
     */
    private boolean isCanceled = false;
    private Config config;

    private ToastUtil() {
        mHandler = new Handler(Looper.getMainLooper());
    }

    public static synchronized ToastUtil getInstance() {
        if (instance == null) {
            instance = new ToastUtil();
        }
        return instance;
    }

    /**
     * 销毁并清空，减少内存泄漏的可能。可以在Activity的destroy调用，也可以在app销毁的时候使用。
     * 推荐在app销毁的时候使用。
     */
    public void destroy() {
        mHandler.removeCallbacks(mCancelRunnable, null);
        mHandler.removeCallbacks(mShowRunnable, null);
        mHandler = null;
        mCancelRunnable = null;
        mToast = null;
        instance = null;
    }

    /**
     * 子线程也可以调用。取消toast。如果toast先cancel调用后立马调用show，此时toast是不会show出来的，如果延时几百毫米执行show，是可以的。
     * 源码里面没有看出来原因，猜测是消息同步异步的问题
     * 如下写法，在cancel后可以show出来。
     * mHandler.postDelayed(new Runnable() {
     *
     * @Override public void run() {
     * mToast.show();
     * }
     * },200);
     * }
     */
    public void cancelRunnable() {
        if (!isCanceled) {
            if (mCancelRunnable == null) {
                mCancelRunnable = new Runnable() {
                    @Override
                    public void run() {
                        mToast.cancel();
                    }
                };
            }
            if (mToast != null) {
                mHandler.post(mCancelRunnable);
            }
            isCanceled = true;
        }
    }


    private void setData(Config config) {
        //构建Toast
        mToast.setDuration(config.duration);
        mToast.setGravity(config.gravity, config.xDimen, config.yDimen);
        LayoutInflater factory = LayoutInflater.from(config.context);
        View view = factory.inflate(config.layoutId, null);
        ViewGroup viewGroup = view.findViewById(config.rootViewId);
        TextView textView = view.findViewById(config.textViewId);
        if (textView != null) {
            textView.setText(config.message == null ? "" : config.message);
            if (config.resTextColor > 0) {
                textView.setTextColor(config.context.getResources().getColor(config.resTextColor));
            } else {
                textView.setTextColor(DEFALUT_COLOR);
            }
            textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, config.textSize);
        }
        ImageView imageView = view.findViewById(config.imageViewId);
        if (imageView != null) {
            Drawable drawable = config.drawable;
            if (drawable == null && config.imageRes <= 0)
                imageView.setVisibility(View.GONE);
            else {
                imageView.setVisibility(View.VISIBLE);
                if (drawable != null) {
                    imageView.setImageDrawable(drawable);
                } else if (config.imageRes > 0) {
                    imageView.setImageResource(config.imageRes);
                }
            }
        }
        //优先给rootview设置点击事件
        if (viewGroup != null) {
            view.setOnClickListener(config.listener);
        } else if (textView != null) {
            textView.setOnClickListener(config.listener);
        } else if (imageView != null) {
            imageView.setOnClickListener(config.listener);
        }
        mToast.setView(view);
    }

    /**
     * 每次show之前cancel掉之前的实例。其实单列的情况下，toast也最多只显示一个，但快速多次点击之后，在某些手机上会出现
     * toast的显示时间变短的异常，发生无规律，且会自动恢复。
     *
     * @param c
     */
    public void show(Config c) {
        this.config = c;
        cancelRunnable();
//        if (Looper.myLooper() != Looper.getMainLooper()) {
        if (mShowRunnable == null) {
            mShowRunnable = new Runnable() {
                @Override
                public void run() {
                    doShow(config);
                }
            };
        }
        mHandler.post(mShowRunnable);
//        } else {
//            doShow(config);
//        }
    }

    private void doShow(Config config) {
        if (mToast == null || isCanceled) {
            mToast = new Toast(config.context);
            isCanceled = false;
        }

        setData(config);
        mToast.show();
    }


    public static class Config {

        private int rootViewId;

        public Config setRootViewId(int rootViewId) {
            this.rootViewId = rootViewId;
            return this;
        }

        private Context context;
        //显示的文本消息
        private String message;
        //R.color.colorAccent 文本颜色
        private int resTextColor = 0;
        //文本大小
        private int textSize = 16;
        //文本组件id
        private int textViewId;
        private int imageViewId;
        //图片资源
        private int imageRes;
        //图片
        private Drawable drawable;
        private int xDimen;
        private int yDimen;
        private int gravity = Gravity.BOTTOM;
        private int duration = Toast.LENGTH_SHORT;
        private View.OnClickListener listener;
        private int layoutId;


        private Config(Context context) {
            // avoiding instantiation
            this.context = context.getApplicationContext();
        }

        public static Config getInstance(Context context) {
            return new Config(context);
        }


        public Config setImageViewId(int imageViewId) {
            this.imageViewId = imageViewId;
            return this;
        }

        public Config setxDimen(int xDimen) {
            this.xDimen = xDimen;
            return this;
        }

        public Config setyDimen(int yDimen) {
            this.yDimen = yDimen;
            return this;
        }

        public Config setImageRes(int res) {
            this.imageRes = res;
            return this;
        }

        public Config setDrawable(Drawable drawable) {
            this.drawable = drawable;
            return this;
        }

        public Config setTextViewId(int textViewId) {
            this.textViewId = textViewId;
            return this;
        }

        public Config setMessage(String message) {
            this.message = message;
            return this;
        }

        public Config setGravity(int gravity) {
            this.gravity = gravity;
            return this;
        }

        public Config setDuration(int duration) {
            this.duration = duration;
            return this;
        }

        public Config setListener(View.OnClickListener listener) {
            this.listener = listener;
            return this;
        }

        public Config setLayoutId(int layoutId) {
            this.layoutId = layoutId;
            return this;
        }

        public Config setTextColor(int textColor) {
            resTextColor = textColor;
            return this;
        }


        @CheckResult
        public Config setTextSize(int sizeInSp) {
            this.textSize = sizeInSp;
            return this;
        }

        /**
         * 显示toast
         */
        public void apply() {
            ToastUtil.getInstance().show(this);
        }
    }

}
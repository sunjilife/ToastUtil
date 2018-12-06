package customer.sunji.com.toast;

import android.app.Activity;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import customer.sunji.com.toast.util.ToastUtil;


/**
 * des:测试使用toast
 * verison:1.0
 * author:sunji
 * create time:2018/12/3 14:35
 */
public class SecondActivity extends Activity implements View.OnClickListener {
    private TextView textView;
    private Button button;
    private Button button1;
    private Button button2;
    private Button button3;
    private Button button4;
    private Button button5;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_second);
        initView();

    }

    private void initView() {
        textView = (TextView) findViewById(R.id.textView);
        button = (Button) findViewById(R.id.button);
        button.setOnClickListener(this);
        button1 = (Button) findViewById(R.id.button1);
        button1.setOnClickListener(this);
        button2 = (Button) findViewById(R.id.button2);
        button2.setOnClickListener(this);
        button3 = (Button) findViewById(R.id.button3);
        button3.setOnClickListener(this);
        button4 = (Button) findViewById(R.id.button4);
        button5 = (Button) findViewById(R.id.button5);
        button5.setOnClickListener(this);
        button4.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.button://子线程
                new Thread(new Runnable() {
                    @Override
                    public void run() {

                        ToastUtil.Config.getInstance(SecondActivity.this).setGravity(Gravity.CENTER).setLayoutId(R.layout.toast_layout).
                                setTextViewId(R.id.toast_tView).setMessage("我是子线程里面show出来哒").apply();
                    }
                }).start();

                break;
            case R.id.button1://系统
                ToastUtil.showToast(this, "我说我是系统的toast，哒哒哒");
                break;
            case R.id.button2://普通toast
                ToastUtil.Config.getInstance(SecondActivity.this).setGravity(Gravity.BOTTOM).setLayoutId(R.layout.toast_layout).setTextColor(R.color.colorPrimaryDark).
                        setTextViewId(R.id.toast_tView).setMessage("我就是个普通人~~~").apply();
                break;
            case R.id.button3://有图标的toast
                ToastUtil.Config.getInstance(SecondActivity.this).setGravity(Gravity.CENTER).setLayoutId(R.layout.toast_layout).
                        setTextViewId(R.id.toast_tView).setMessage("我是有图标的").setImageViewId(R.id.iv_icon).setImageRes(android.R.drawable.ic_menu_compass).apply();
                break;
            case R.id.button4://toast再点击，不晓得有什么鬼用

                ToastUtil.Config.getInstance(SecondActivity.this).setGravity(Gravity.CENTER).setLayoutId(R.layout.toast_layout).
                        setTextViewId(R.id.toast_tView).setMessage("我弹出来后还可以点一哈").setListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        ToastUtil.showToast(SecondActivity.this, "哪个在点我？给我出来");
                    }
                }).apply();

                break;
            case R.id.button5:
                ToastUtil.Config.getInstance(SecondActivity.this).setGravity(Gravity.BOTTOM).setyDimen(50).setLayoutId(R.layout.toast_layout).setTextColor(R.color.colorPrimaryDark).
                        setTextViewId(R.id.toast_tView).setMessage("我就是个普通人~~~").apply();
                break;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //可以在退出app时
        ToastUtil.getInstance().destroy();
    }
}

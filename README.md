# ToastUtil
特点:
自定义Toast，使用建造者模式+单列模式,多次点击仅出现一个，避免导致杂乱。
toast定义ui灵活，不用再修改代码，直接传入布局文件id即可
该toast可以在子线程中直接使用，也可以在主线程中使用。
可以给toast设置点击事件。

不足:不能给传入的布局文件根布局设置上点击事件，即使添加了onClickListener，也不会执行回调。原因不明确但布局文件中除根布局以外的部分可以响应点击事件；

how to use:

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

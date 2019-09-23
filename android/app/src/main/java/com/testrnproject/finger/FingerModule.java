package com.testrnproject.finger;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.KeyguardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.hardware.biometrics.BiometricPrompt;
import android.os.Build;
import android.os.CancellationSignal;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import androidx.core.hardware.fingerprint.FingerprintManagerCompat;

import com.facebook.react.bridge.LifecycleEventListener;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.testrnproject.R;

import javax.annotation.Nonnull;

public class FingerModule extends ReactContextBaseJavaModule implements LifecycleEventListener {

    private static final String TAG = "FingerModule";
    private Context context;
    private FingerprintManagerCompat mFingerManger;
    private KeyguardManager mKeyManger;
    private androidx.core.os.CancellationSignal mCancellationSignal;
    private Dialog dialog;
    private Handler handler;

    public FingerModule(@Nonnull ReactApplicationContext reactContext) {
        super(reactContext);
        this.context = reactContext;
        mFingerManger = FingerprintManagerCompat.from(context);
        mKeyManger = (KeyguardManager) context.getSystemService(Context.KEYGUARD_SERVICE);
        reactContext.addLifecycleEventListener(this);
    }

    @Nonnull
    @Override
    public String getName() {
        return "FingerUtil";
    }

    @ReactMethod
    public void fingerCheck() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            api28();
            return;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            //判断权限
            if (judgePermission()) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    mCancellationSignal = new androidx.core.os.CancellationSignal();
                }
                mFingerManger = FingerprintManagerCompat.from(context);
                mFingerManger.authenticate(null, 0, mCancellationSignal, new FingerCallBack(), null);

                dialog = new AlertDialog.Builder(getCurrentActivity())
                        .setTitle("指纹识别")
                        .setMessage("描述")
                        .setIcon(R.mipmap.ic_fp_40px)
                        .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                mCancellationSignal.cancel();
                            }
                        })
                        .create();
                dialog.show();
            }
        }
    }

    @Override
    public void onHostResume() {
        handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                switch (msg.what) {
                    case 0:
                        dialog.setTitle("指纹不匹配，请重新尝试！");
                        break;
                }
            }
        };
    }

    @Override
    public void onHostPause() {

    }

    @Override
    public void onHostDestroy() {
        if (mCancellationSignal != null) {
            mCancellationSignal.cancel();
        }
    }

    private class FingerCallBack extends FingerprintManagerCompat.AuthenticationCallback {
        //多次识别失败,并且，不能短时间内调用指纹验证
        @Override
        public void onAuthenticationError(int errMsgId, CharSequence errString) {
            super.onAuthenticationError(errMsgId, errString);
//            Toast.makeText(context, "多次识别失败", Toast.LENGTH_LONG).show();
        }

        //出错可恢复
        @Override
        public void onAuthenticationHelp(int helpMsgId, CharSequence helpString) {
            super.onAuthenticationHelp(helpMsgId, helpString);
            Toast.makeText(context, "有点问题，稍后再试", Toast.LENGTH_LONG).show();
        }

        //识别成功
        @Override
        public void onAuthenticationSucceeded(FingerprintManagerCompat.AuthenticationResult result) {
            super.onAuthenticationSucceeded(result);
            Toast.makeText(context, "识别成功", Toast.LENGTH_LONG).show();
            dialog.dismiss();
        }

        //识别失败
        @Override
        public void onAuthenticationFailed() {
            super.onAuthenticationFailed();
            Toast.makeText(context, "识别失败", Toast.LENGTH_LONG).show();
            handler.sendEmptyMessage(0);
        }
    }

    /**
     * 返回权限、硬件的判断
     *
     * @return
     */
    public boolean judgePermission() {
        //硬件是否支持指纹识别
        if (!mFingerManger.isHardwareDetected()) {
            Toast.makeText(context, "您手机不支持指纹识别功能", Toast.LENGTH_LONG).show();
            return false;
        }
        //是否已经录入指纹
        if (!mFingerManger.hasEnrolledFingerprints()) {
            Toast.makeText(context, "您还未录入指纹", Toast.LENGTH_LONG).show();
            return false;
        }
        //手机是否开启锁屏密码
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            if (!mKeyManger.isKeyguardSecure()) {
                Toast.makeText(context, "请开启开启锁屏密码，并录入指纹后再尝试", Toast.LENGTH_LONG).show();
                return false;
            }
        }

        return true;
    }

    private void api28() {
        PackageManager packageManager = context.getPackageManager();
        if (Build.VERSION.SDK_INT >= 28) {
            if (packageManager.hasSystemFeature(PackageManager.FEATURE_FINGERPRINT)) {
                BiometricPrompt biometricPrompt = new BiometricPrompt.Builder(context)
                        .setTitle("指纹验证")
                        .setDescription("描述")
                        .setNegativeButton("取消", context.getMainExecutor(), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                Log.i(TAG, "Cancel button clicked");
                            }
                        })
                        .build();
                CancellationSignal mCancellationSignal = new CancellationSignal();
                mCancellationSignal.setOnCancelListener(new CancellationSignal.OnCancelListener() {
                    @Override
                    public void onCancel() {
                        //handle cancel result
                        Log.i(TAG, "Canceled");
                    }
                });

                BiometricPrompt.AuthenticationCallback mAuthenticationCallback = new BiometricPrompt.AuthenticationCallback() {
                    @Override
                    public void onAuthenticationError(int errorCode, CharSequence errString) {
                        super.onAuthenticationError(errorCode, errString);

                        Log.i(TAG, "onAuthenticationError " + errString);
                        Toast.makeText(context, errString, Toast.LENGTH_LONG).show();
                    }

                    @Override
                    public void onAuthenticationSucceeded(BiometricPrompt.AuthenticationResult result) {
                        super.onAuthenticationSucceeded(result);
                        Toast.makeText(context, "识别成功", Toast.LENGTH_LONG).show();
                        Log.i(TAG, "onAuthenticationSucceeded " + result.toString());
                    }

                    @Override
                    public void onAuthenticationFailed() {
                        super.onAuthenticationFailed();
                        Log.i(TAG, "onAuthenticationFailed ");
                    }
                };
                biometricPrompt.authenticate(mCancellationSignal, context.getMainExecutor(), mAuthenticationCallback);
            }
        }
    }

}

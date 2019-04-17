package com.tizz.signin;

import android.app.DialogFragment;
import android.content.Context;
import android.hardware.fingerprint.FingerprintManager;
import android.os.Bundle;
import android.os.CancellationSignal;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.tizz.signin.activity.MainActivity;

import javax.crypto.Cipher;

public class FingerprintDialogFragment extends DialogFragment {
    private FingerprintManager fingerprintManager;
    private CancellationSignal cancellationSignal;
    private Cipher cipher;
    private TextView errorMsg;
    private TextView cancel;
    private boolean isSelfCancelled;
    private MainActivity mainActivity;


    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        fingerprintManager=getContext().getSystemService(FingerprintManager.class);
        setStyle(DialogFragment.STYLE_NORMAL,android.R.style.Theme_Material_Light_Dialog);
    }

    @Override
    public View onCreateView(LayoutInflater inflater,
                             @Nullable ViewGroup container, Bundle savedInstanceState){
        View view=inflater.inflate(R.layout.fingerprint_dialog,container,false);
        errorMsg=view.findViewById(R.id.error_msg);
        cancel=view.findViewById(R.id.cancel);
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
                stopListening();
            }
        });
        return view;
    }

    public void setCipher(Cipher cipher){
        this.cipher=cipher;
    }

    @Override
    public void onAttach(Context context){
        super.onAttach(context);
        mainActivity=(MainActivity)getActivity();
    }

    @Override
    public void onResume(){
        super.onResume();
        startListening(cipher);
    }

    @Override
    public void onPause(){
        super.onPause();
        stopListening();
    }

    private void startListening(Cipher cipher){
        isSelfCancelled=false;
        cancellationSignal=new CancellationSignal();
        fingerprintManager.authenticate(new FingerprintManager.CryptoObject(cipher), cancellationSignal,
                0, new FingerprintManager.AuthenticationCallback() {

                    @Override
                    public void onAuthenticationError(int errorCode, CharSequence errString) {
                        super.onAuthenticationError(errorCode, errString);
                        if(!isSelfCancelled){
                            errorMsg.setText("验证失败，请再次尝试！");
                        }
                    }

                    @Override
                    public void onAuthenticationHelp(int helpCode, CharSequence helpString) {
                    }

                    @Override
                    public void onAuthenticationSucceeded(FingerprintManager.AuthenticationResult result){
                        super.onAuthenticationSucceeded(result);
                        mainActivity.fingerprintSucceded=true;
                        Toast.makeText(mainActivity,"指纹认证成功！",
                                Toast.LENGTH_SHORT).show();
                        dismiss();
                    }

                    @Override
                    public void onAuthenticationFailed(){
                        errorMsg.setText("指纹认证失败，请再次尝试!");
                    }

                },null);
    }

    private void stopListening(){
        if(cancellationSignal!=null){
            cancellationSignal.cancel();
            cancellationSignal=null;
            isSelfCancelled=true;
        }
    }


}

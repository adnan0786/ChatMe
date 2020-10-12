package com.example.chatme.Fragment;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.chatme.Constants.AllConstants;
import com.example.chatme.R;
import com.example.chatme.UserModel;
import com.example.chatme.databinding.FragmentVerifyNumberBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;


/**
 * A simple {@link Fragment} subclass.
 * Use the {@link VerifyNumber#newInstance} factory method to
 * create an instance of this fragment.
 */
public class VerifyNumber extends Fragment {
    private FragmentVerifyNumberBinding binding;
    private String OTP, pin;
    private FirebaseAuth firebaseAuth;
    private DatabaseReference databaseReference;
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public VerifyNumber() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment VerifyNumber.
     */
    // TODO: Rename and change types and number of parameters
    public static VerifyNumber newInstance(String param1, String param2) {
        VerifyNumber fragment = new VerifyNumber();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentVerifyNumberBinding.inflate(inflater, container, false);
        View view = binding.getRoot();
        firebaseAuth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference("Users");
        Bundle bundle = getArguments();


        if (bundle != null) {
            OTP = bundle.getString(AllConstants.VERIFICATION_CODE);
        }

        binding.btnVerify.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkPin();
                if (checkPin()) {
                    binding.progressLayout.setVisibility(View.VISIBLE);
                    binding.progressBar.start();

                    verifyPin(pin);
                }
            }
        });

        return view;
    }


    private boolean checkPin() {

        pin = binding.otpTextView.getText().toString();
        if (TextUtils.isEmpty(pin)) {
            binding.otpTextView.setError("Enter the pin");
            return false;
        } else if (pin.length() < 6) {
            binding.otpTextView.setError("Enter valid pin");
            return false;
        } else {
            binding.otpTextView.setError(null);
            return true;
        }
    }

    private void verifyPin(String pin) {
        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(OTP, pin);
        signInWithPhoneAuthCredential(credential);
    }

    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {


        firebaseAuth.signInWithCredential(credential).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {

                    FirebaseInstanceId.getInstance().getInstanceId().addOnCompleteListener(data -> {

                        String token = data.getResult().getToken();
                        UserModel userModel = new UserModel("", "", "", firebaseAuth.getCurrentUser().getPhoneNumber(),
                                firebaseAuth.getUid(), "online", "false",token);
                        databaseReference.child(firebaseAuth.getUid()).setValue(userModel).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {

                                    getFragmentManager().beginTransaction().replace(R.id.container, new UserData()).commit();
                                    binding.progressLayout.setVisibility(View.GONE);
                                    binding.progressBar.stop();

                                } else
                                    Toast.makeText(getContext(), "" + task.getException(), Toast.LENGTH_SHORT).show();
                            }
                        });
                    });

                } else
                    Toast.makeText(getContext(), "" + task.getResult(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}

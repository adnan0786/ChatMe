package com.example.chatme.Fragment;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;

import com.example.chatme.Constants.AllConstants;
import com.example.chatme.R;
import com.example.chatme.databinding.FragmentGetNumberBinding;
import com.google.firebase.FirebaseException;
import com.google.firebase.FirebaseTooManyRequestsException;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;

import java.util.concurrent.TimeUnit;


/**
 * A simple {@link Fragment} subclass.
 * Use the {@link GetNumber#newInstance} factory method to
 * create an instance of this fragment.
 */
public class GetNumber extends Fragment {

    private FragmentGetNumberBinding binding;
    private String number;
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public GetNumber() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment GetNumber.
     */
    // TODO: Rename and change types and number of parameters
    public static GetNumber newInstance(String param1, String param2) {
        GetNumber fragment = new GetNumber();
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
    public View onCreateView(LayoutInflater inflater, final ViewGroup container,
                             Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_get_number, container, false);
        View view = binding.getRoot();


        binding.btnGenerateOTP.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkNumber();
                if (checkNumber()) {
                    String phoneNumber = binding.countryCodePicker.getSelectedCountryCodeWithPlus() + number;

                    sendOTP(phoneNumber);
                }
            }
        });
        return view;
    }

    private boolean checkNumber() {
        number = binding.edtNumber.getText().toString().trim();
        if (TextUtils.isEmpty(number)) {
            binding.edtNumber.setError("Enter number");
            return false;
        } else if (number.length() < 10) {
            binding.edtNumber.setError("Enter valid number");
            return false;
        } else {
            binding.edtNumber.setError(null);

            return true;
        }
    }

    private void sendOTP(String phoneNumber) {
        binding.progressLayout.setVisibility(View.VISIBLE);
        binding.progressBar.start();

        PhoneAuthProvider.getInstance().verifyPhoneNumber(


                phoneNumber,        // Phone number to verify
                60,                 // Timeout duration
                TimeUnit.SECONDS,   // Unit of timeout
                getActivity(),               // Activity (for callback binding)
                mCallbacks);
    }


    PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks =
            new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

                @Override
                public void onVerificationCompleted(PhoneAuthCredential credential) {

                }

                @Override
                public void onVerificationFailed(FirebaseException e) {

                    if (e instanceof FirebaseAuthInvalidCredentialsException){

                        Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_LONG).show();
                        Log.d("TAG", "onVerificationFailed: "+e.getMessage());

                    } else if (e instanceof FirebaseTooManyRequestsException)
                        Toast.makeText(getContext(), "The SMS quota for the project has been exceeded ", Toast.LENGTH_LONG).show();


                    Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_LONG).show();
                    Log.d("TAG", "onVerificationFailed: "+e.getMessage());
                    binding.progressLayout.setVisibility(View.GONE);
                    binding.progressBar.stop();


                }


                @Override
                public void onCodeSent(@NonNull String s,
                                       @NonNull PhoneAuthProvider.ForceResendingToken token) {
                    Toast.makeText(getContext(), "Verification code sent..", Toast.LENGTH_LONG).show();

                    Fragment fragment = new VerifyNumber();
                    Bundle bundle = new Bundle();
                    bundle.putString(AllConstants.VERIFICATION_CODE, s);
                    fragment.setArguments(bundle);
                    getFragmentManager().beginTransaction()
                            .replace(R.id.container, fragment)
                            .commit();
                    binding.progressLayout.setVisibility(View.VISIBLE);
                    binding.progressBar.stop();


                }
            };
}

package com.example.chatme.Fragment;

import android.content.ContentResolver;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.chatme.Adapter.ContactAdapter;
import com.example.chatme.Constants.AllConstants;
import com.example.chatme.Permissions.Permissions;
import com.example.chatme.UserModel;
import com.example.chatme.databinding.FragmentContactBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class ContactFragment extends Fragment implements SearchView.OnQueryTextListener {

    private FragmentContactBinding binding;
    private DatabaseReference databaseReference;
    private Permissions permissions;
    private ArrayList<UserModel> userContacts, appContacts;
    private ContactAdapter contactAdapter;
    private String userPhoneNumber;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentContactBinding.inflate(inflater, container, false);
        View view = binding.getRoot();
        permissions = new Permissions();
        binding.recyclerViewContact.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.recyclerViewContact.setHasFixedSize(true);
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        userPhoneNumber = firebaseAuth.getCurrentUser().getPhoneNumber();
        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle("Contacts");

        getUserContacts();

        binding.contactSearchView.setOnQueryTextListener(this);
        return view;
    }


    private void getUserContacts() {


        if (permissions.isContactOk(getContext())) {
            userContacts = new ArrayList<>();
            String[] projection = new String[]{
                    ContactsContract.Contacts.DISPLAY_NAME,
                    ContactsContract.CommonDataKinds.Phone.NUMBER
            };
            ContentResolver cr = getContext().getContentResolver();
            Cursor cursor = cr.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, projection, null, null, null);
            if (cursor != null) {
                userContacts.clear();
                try {


                    while (cursor.moveToNext()) {

                        String name = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
                        String number = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));

                        number = number.replaceAll("\\s", "");
                        String num = String.valueOf(number.charAt(0));

                        if (num.equals("0"))
                            number = number.replaceFirst("(?:0)+", "+92");

                        UserModel userModel = new UserModel();
                        userModel.setName(name);
                        userModel.setNumber(number);
                        userContacts.add(userModel);

                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }

            cursor.close();
            getAppContacts(userContacts);

        } else permissions.requestContact(getActivity());
    }

    private void getAppContacts(final ArrayList<UserModel> mobileContacts) {

        appContacts = new ArrayList<>();
        databaseReference = FirebaseDatabase.getInstance().getReference("Users");
        Query query = databaseReference.orderByChild("number");

        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    appContacts.clear();
                    for (DataSnapshot ds : dataSnapshot.getChildren()) {
                        String number = ds.child("number").getValue().toString();

                        for (UserModel userModel : mobileContacts) {

                            if (userModel.getNumber().equals(number) && !number.equals(userPhoneNumber)) {

                                String image = ds.child("image").getValue().toString();
                                String status = ds.child("status").getValue().toString();
                                String uID = ds.child("uID").getValue().toString();

                                String name = ds.child("name").getValue().toString();
                                UserModel registeredUser = new UserModel();

                                registeredUser.setName(name);
                                registeredUser.setStatus(status);
                                registeredUser.setImage(image);
                                registeredUser.setuID(uID);

                                appContacts.add(registeredUser);
                                break;
                            }
                        }
                    }
                    contactAdapter = new ContactAdapter(getContext(), appContacts);
                    binding.recyclerViewContact.setAdapter(contactAdapter);


                } else Toast.makeText(getContext(), "No Data Found", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case AllConstants.CONTACTS_REQUEST_CODE:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    getUserContacts();
                } else
                    Toast.makeText(getContext(), "Contact Permission denied", Toast.LENGTH_SHORT).show();
        }
    }


    @Override
    public boolean onQueryTextSubmit(String query) {
        return false;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        if (contactAdapter != null)
            contactAdapter.getFilter().filter(newText);
        return false;
    }
}
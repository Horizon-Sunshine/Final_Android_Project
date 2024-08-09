package com.example.final_project_iot;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

public class SignupFragment extends Fragment {
    private FragmentActivity activity;

    public SignupFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activity = getActivity();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_signup, container, false);

        // Add listeners for the buttons
        Button goto_login_button = view.findViewById(R.id.goto_login_button);
        goto_login_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((ProfileActivity) activity).switchFragment(new LoginFragment());
            }
        });

        Button sign_up_button = view.findViewById(R.id.signup_button);
        sign_up_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TextView username = view.findViewById(R.id.username_edit);
                TextView password = view.findViewById(R.id.password_edit);
                TextView phone_num = view.findViewById(R.id.phone_edit);
                ((ProfileActivity) activity).sign_up(username.getText().toString(), password.getText().toString(), phone_num.getText().toString());
            }
        });
        return view;
    }
}
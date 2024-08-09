package com.example.final_project_iot;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;

public class LoginFragment extends Fragment {
    private FragmentActivity activity;

    public LoginFragment() {
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
        View view = inflater.inflate(R.layout.fragment_login, container, false);

        // Add listeners for the buttons
        Button goto_signup_button = view.findViewById(R.id.goto_signup_button);
        goto_signup_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((ProfileActivity) activity).switchFragment(new SignupFragment());
            }
        });
        Button login_button = view.findViewById(R.id.login_button);
        login_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TextView username = view.findViewById(R.id.username_edit);
                TextView password = view.findViewById(R.id.password_edit);
                ((ProfileActivity) activity).login_attempt(username.getText().toString(), password.getText().toString());
            }
        });
        return view;
    }
}
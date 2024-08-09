package com.example.final_project_iot;

import android.content.SharedPreferences;
import android.graphics.Paint;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

public class ProfileFragment extends Fragment {
    private FragmentActivity activity;
    private SharedPreferences sp;

    public ProfileFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activity = getActivity();
        sp = activity.getSharedPreferences("Logged_in_sp", 0);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        TextView profile_title_textview = view.findViewById(R.id.profile_textView);
        profile_title_textview.setText(getString(R.string.profile_title, sp.getString("username", "error")));
        profile_title_textview.setPaintFlags(profile_title_textview.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);

        // Add listener for the button
        Button logout_button = view.findViewById(R.id.logout_button);
        logout_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((ProfileActivity) activity).logout();
            }
        });

        return view;
    }
}
package com.devzeze.restasyncdemo;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


public class EmptyFragment extends Fragment {

    public EmptyFragment() {
    }

    public static EmptyFragment newInstance() {

        EmptyFragment fragment = new EmptyFragment();
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_async, container, false);
        return view;
    }
}

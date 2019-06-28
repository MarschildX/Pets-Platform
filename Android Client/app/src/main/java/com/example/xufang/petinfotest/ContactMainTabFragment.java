package com.example.xufang.petinfotest;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 *
 * 该类对应主activity的第三个fragment，暂时没有任何功能
 *
 * @author FangXu
 * @date 2018/06/23
 *
 */
public class ContactMainTabFragment extends Fragment{
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.tab03,container,false);
    }
}

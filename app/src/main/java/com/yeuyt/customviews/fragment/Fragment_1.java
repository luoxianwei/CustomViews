package com.yeuyt.customviews.fragment;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.yeuyt.customviews.R;
import com.yeuyt.tagsview.TagsView;


public class Fragment_1 extends Fragment {
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.frag_tags_viewgroup, container, false);
        TagsView group = view.findViewById(R.id.tag_view_group);
        String[] ss = {"java", "android", "玄学", "数据结构", "调试", "python", "dart", "C++"};
        group.setTags(ss, true);
        group.setOnChildClickListener(new TagsView.OnChildClickListener() {
            @Override
            public void onChildClick(View view, int position) {

            }
        });
        return view;
    }
}

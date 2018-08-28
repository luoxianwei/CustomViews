package com.yeuyt.customviews.fragment;

import android.animation.Animator;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.yeuyt.customviews.R;
import com.yeuyt.textpathview.painter.ArrowPainter;
import com.yeuyt.textpathview.utils.PathAnimatorListener;
import com.yeuyt.textpathview.view.TextPathView;

public class Fragment_2 extends Fragment {
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.frag_pathview, container, false);
        final TextPathView pathView = v.findViewById(R.id.path_view);
        pathView.setPathPainter(new ArrowPainter());
        pathView.setDuration(20000);
        pathView.setAnimatorListener(new PathAnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                super.onAnimationStart(animation);
                System.out.println("Start");
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                Toast.makeText(getActivity(), "aaaaaa", Toast.LENGTH_LONG).show();
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                super.onAnimationCancel(animation);
            }

            @Override
            public void onAnimationRepeat(Animator animation) {
                super.onAnimationRepeat(animation);
                System.out.println("repeat:");
            }
        });

        Button button = new Button(getContext());
        button.setText("Start");
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pathView.startAnimation(0, 1, TextPathView.REVERSE, 0);
            }
        });
        FrameLayout frameLayout = v.findViewById(R.id.root_frame);
        ViewGroup.LayoutParams layoutParams = new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT
        );

        button.setLayoutParams(layoutParams);
        frameLayout.addView(button);
        return v;
    }
}

package me.wangyi.imagepicker.ui;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.support.annotation.NonNull;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.ListView;
import android.widget.PopupWindow;

import java.util.List;

import me.wangyi.imagepicker.R;
import me.wangyi.imagepicker.adapter.FolderAdapter;
import me.wangyi.imagepicker.model.Folder;

/**
 * Created on 2017/5/9.
 *
 * @author WangYi
 * @since 1.0.0
 */

class FolderPopupWindow extends PopupWindow implements View.OnClickListener {
    private ListView mListView;
    private View mMasker;

    private List<Folder> mFolderList;
    private FolderAdapter mFolderAdapter;
    private boolean mAnimRunning = false;

    FolderPopupWindow(Context context, @NonNull List<Folder> folderList) {
        super(context);
        this.mFolderList = folderList;
        final View mRootView = View.inflate(context, R.layout.popupwindow_folder, null);
        mListView = (ListView) mRootView.findViewById(R.id.lvFolders);
        mMasker = mRootView.findViewById(R.id.masker);

        mFolderAdapter = new FolderAdapter(mFolderList);
        mListView.setAdapter(mFolderAdapter);
        mMasker.setOnClickListener(this);

        setContentView(mRootView);
        setBackgroundDrawable(null);
        setOutsideTouchable(true);
        mRootView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                //noinspection deprecation
                mRootView.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                int maxHeight = mRootView.getHeight() * 3 / 4;
                int realHeight = mListView.getHeight();
                ViewGroup.LayoutParams listParams = mListView.getLayoutParams();
                listParams.height = realHeight > maxHeight ? maxHeight : realHeight;
                mListView.setLayoutParams(listParams);
                startShowAnim();
            }
        });
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.masker) {
            dismiss();
        }
    }

    @Override
    public void dismiss() {
        if (!mAnimRunning) {
            startDismissAnim();
        }
    }

    @Override
    public void showAtLocation(View parent, int gravity, int x, int y) {
        if (mFolderList == null || mFolderList.size() == 0 || mAnimRunning) {
            return;
        }
        startShowAnim();
        super.showAtLocation(parent, gravity, x, y);
    }

    private void startShowAnim() {
        ObjectAnimator alpha = ObjectAnimator.ofFloat(mMasker, "alpha", 0, 1);
        ObjectAnimator translationY = ObjectAnimator.ofFloat(mListView, "translationY", mListView.getHeight(), 0);
        AnimatorSet set = new AnimatorSet();
        set.setDuration(300);
        set.playTogether(alpha, translationY);
        set.setInterpolator(new AccelerateDecelerateInterpolator());
        set.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                mAnimRunning = true;
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                mAnimRunning = false;
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
        set.start();
    }

    private void startDismissAnim() {
        ObjectAnimator alpha = ObjectAnimator.ofFloat(mMasker, "alpha", 1, 0);
        ObjectAnimator translationY = ObjectAnimator.ofFloat(mListView, "translationY", 0, mListView.getHeight());
        AnimatorSet set = new AnimatorSet();
        set.setDuration(300);
        set.playTogether(alpha, translationY);
        set.setInterpolator(new AccelerateDecelerateInterpolator());
        set.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                mAnimRunning = true;
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                mAnimRunning = false;
                FolderPopupWindow.super.dismiss();
            }

            @Override
            public void onAnimationCancel(Animator animation) {
            }

            @Override
            public void onAnimationRepeat(Animator animation) {
            }
        });
        set.start();
    }

    void notifyDataSetChanged() {
        mFolderAdapter.notifyDataSetChanged();
    }

    void setOnFolderSelectedListener(FolderAdapter.OnFolderSelectedListener listener) {
        mFolderAdapter.setOnFolderSelectedListener(listener);
    }
}

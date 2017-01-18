package com.cins.daily.mvp.ui.fragment;

import android.annotation.TargetApi;
import android.app.ActivityOptions;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.cins.daily.App;
import com.cins.daily.R;
import com.cins.daily.common.Constants;
import com.cins.daily.component.DaggerNewsComponent;
import com.cins.daily.listener.OnItemClickListener;
import com.cins.daily.module.NewsListModule;
import com.cins.daily.mvp.entity.NewsSummary;
import com.cins.daily.mvp.presenter.NewsListPresenter;
import com.cins.daily.mvp.ui.activities.NewsDetailActivity;
import com.cins.daily.mvp.ui.adapter.NewsRecyclerViewAdapter;
import com.cins.daily.mvp.ui.fragment.base.BaseFragment;
import com.cins.daily.mvp.view.NewsListView;
import com.cins.daily.utils.NetUtil;

import java.util.List;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.cins.daily.component.DaggerNewsComponent.builder;

/**
 * Created by Eric on 2017/1/16.
 */

public class NewsListFragment extends BaseFragment implements NewsListView, OnItemClickListener {

    @BindView(R.id.news_rv)
    RecyclerView mNewsRv;
    @BindView(R.id.progress_bar)
    ProgressBar mProgressBar;

    @Inject
    NewsRecyclerViewAdapter mNewsRecyclerViewAdapter;
    @Inject
    NewsListPresenter mNewsListPresenter;

    private String mNewsId;
    private String mNewsType;
    private int mStartPage;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mNewsId = getArguments().getString(Constants.NEWS_ID);
            mNewsType = getArguments().getString(Constants.NEWS_TYPE);
            mStartPage = getArguments().getInt(Constants.CHANNEL_POSITION);
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_news, container, false);
        ButterKnife.bind(this, view);

        mNewsRv.setHasFixedSize(true);
        //setting the LayoutManager
        mNewsRv.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false));

        DaggerNewsComponent.builder()
                .newsListModule(new NewsListModule(this, mNewsType, mNewsId))
                .build()
                .inject(this);
        mPresenter = mNewsListPresenter;
        mPresenter.onCreate();
        checkNetState();
        return view;
    }

    private void checkNetState() {
        if (!NetUtil.isNetworkAvailable(App.getAppContext())) {
            //TODO: 刚启动app Snackbar不起作用，延迟显示也不好使，这是why？
            Toast.makeText(getActivity(), getActivity().getString(R.string.internet_error), Toast.LENGTH_SHORT).show();
            /*            new Handler().postDelayed(new Runnable() {
                 public void run() {
                     Snackbar.make(mNewsRV, App.getAppContext().getString(R.string.internet_error), Snackbar.LENGTH_LONG);
                 }
            }, 1000);*/

        }
    }

    @Override
    public void onResume() {
        super.onResume();
    }


    @Override
    public void showProgress() {
        mProgressBar.setVisibility(View.VISIBLE);
    }

    @Override
    public void hideProgress() {
        mProgressBar.setVisibility(View.GONE);
    }

    @Override
    public void showErrorMsg(String message) {
        mProgressBar.setVisibility(View.GONE);
        if (NetUtil.isNetworkAvailable(App.getAppContext())) {
            Snackbar.make(mNewsRv, message, Snackbar.LENGTH_LONG).show();
        }
    }


    @Override
    public void onDestroyView() {
        mNewsListPresenter.onDestroy();
        super.onDestroyView();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void onItemClick(View view, int position) {
        List<NewsSummary> newsSummaryList = mNewsRecyclerViewAdapter.getNewsSummaryList();
        goToNewsDetailActivity(view, position, newsSummaryList);
    }

    private void goToNewsDetailActivity(View view, int position, List<NewsSummary> newsSummaryList) {
        Intent intent = new Intent(getActivity(), NewsDetailActivity.class);
        intent.putExtra(Constants.NEWS_POST_ID, newsSummaryList.get(position).getPostid());
        intent.putExtra(Constants.NEWS_IMG_RES, newsSummaryList.get(position).getImgsrc());

        ImageView newsSummaryPhotoIv = (ImageView) view.findViewById(R.id.news_summary_photo_iv);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            ActivityOptions options = ActivityOptions
                    .makeSceneTransitionAnimation(getActivity(), newsSummaryPhotoIv, Constants.TRANSITION_ANIMATION_NEWS_PHOTOS);
            startActivity(intent, options.toBundle());
        } else {
            /*    ActivityOptionsCompat.makeCustomAnimation(this,
                     R.anim.slide_bottom_in, R.anim.slide_bottom_out);
             这个我感觉没什么用处，类似于
             overridePendingTransition(R.anim.slide_bottom_in, android.R.anim.fade_out);*/

                            /*            ActivityOptionsCompat.makeThumbnailScaleUpAnimation(source, thumbnail, startX, startY)
             这个方法可以用于4.x上，是将一个小块的Bitmpat进行拉伸的动画。*/

            //让新的Activity从一个小的范围扩大到全屏
            ActivityOptionsCompat options = ActivityOptionsCompat
                    .makeScaleUpAnimation(view, view.getWidth() / 2, view.getHeight() / 2, 0, 0);
            ActivityCompat.startActivity(getActivity(), intent, options.toBundle());

        }
    }
}
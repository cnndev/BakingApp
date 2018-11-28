package com.example.chihurmnanyanwanevu.bakingapp.ui.fragments;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.exoplayer2.DefaultLoadControl;
import com.google.android.exoplayer2.DefaultRenderersFactory;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.LoadControl;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelector;
import com.google.android.exoplayer2.ui.SimpleExoPlayerView;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.upstream.DefaultHttpDataSourceFactory;
import com.google.android.exoplayer2.util.Util;
import com.example.chihurmnanyanwanevu.bakingapp.R;
import com.example.chihurmnanyanwanevu.bakingapp.data.models.Step;
import com.example.chihurmnanyanwanevu.bakingapp.ui.activities.RecipeActivity;
import com.example.chihurmnanyanwanevu.bakingapp.ui.activities.StepDetailsActivity;
import com.example.chihurmnanyanwanevu.bakingapp.ui.activities.StepsActivity;
import com.example.chihurmnanyanwanevu.bakingapp.utils.NetworkUtil;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

import static android.content.ContentValues.TAG;
import static com.example.chihurmnanyanwanevu.bakingapp.ui.activities.StepDetailsActivity.SELECTED_INDEX;

/**
 * Created by Chihurumnanya
 */

public class StepsDetailsFragment extends Fragment {

    private ListItemClickListener itemClickListener;

    public interface ListItemClickListener {
        void onListItemClick(List<Step> allSteps, int Index, String recipeName);
    }

    public interface Callback {
        void onNewStepSelected(int position);
    }

    private static final String EXOPLAYER_POSITION = "exoplayer_position";
    public static final String STEP_SENT = "step_sent_step_details_fragment";
    public static final String STEP_CLICKED = "step_clicked_step_details_fragment";
    public static final String NUMBER_OF_STEPS = "number_of_steps";
    public static final String IS_TABLET = "is_tablet";

    Callback mCallback;

    @BindView(R.id.root)
    LinearLayout root;

    @BindView(R.id.topLayout)
    FrameLayout topLayout;

    @BindView(R.id.bottomLayout)
    FrameLayout bottomLayout;

    @BindView(R.id.exoPlayerView)
    SimpleExoPlayerView exoPlayerView;

    @BindView(R.id.noVideoIv)
    ImageView noVideo;

    @BindView(R.id.stepDescription_tv)
    TextView stepDescription;

    @BindView(R.id.thumbnailurl)
    ImageView ThumbnailUrl;

    @BindView(R.id.nextVideoBtn)
    FloatingActionButton nextVideo;

    @BindView(R.id.prevVideoBtn)
    FloatingActionButton prevVideo;

    @BindView(R.id.currentStep)
    TextView currentStep;

    private SimpleExoPlayer mExoPlayer;

    private Step step;

    private Integer position;

    private int selectedIndex;


    private Integer numberOfSteps;

    private boolean isTablet;
    private String recipeName;
    private Long exoplayer_position;

    private ArrayList<Step> steps = new ArrayList<>();


    private BroadcastReceiver receiver;
    private IntentFilter intentFilter;
    private boolean isConnected;

    int width = 0;
    int height = 0;

    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.recipe_steps_details, container, false);
        ButterKnife.bind(this, root);
        // ImageUrl Crashes the app...App works perfectly without this block of code
        /*String imageUrl = steps.get(selectedIndex).getThumbnailURL();
        selectedIndex = 0;
        if (imageUrl == "") {
            Uri builtUri = Uri.parse(imageUrl).buildUpon().build();
            ImageView ThumbnairlUrl = (ImageView) root.findViewById(R.id.thumbnailurl);
            Picasso.with(getContext()).load(builtUri).into(ThumbnairlUrl);
        }*/

        if (savedInstanceState != null) {
            if (savedInstanceState.get(EXOPLAYER_POSITION) != null) {
                selectedIndex = savedInstanceState.getInt(SELECTED_INDEX);
                exoplayer_position = savedInstanceState.getLong(EXOPLAYER_POSITION);
            }
        }

        if (getArguments() != null) {

            Bundle bundle = getArguments();

            step = getArguments().getParcelable(STEP_SENT);
            position = getArguments().getInt(STEP_CLICKED, 0);
            numberOfSteps = getArguments().getInt(NUMBER_OF_STEPS, 1);
            isTablet = getArguments().getBoolean(IS_TABLET);

            setStepDescriptionText(position);

            initializeExoPlayer(position);

            if (position == 0) {
                prevVideo.setVisibility(View.INVISIBLE);
            } else if (position == numberOfSteps - 1) {
                nextVideo.setVisibility(View.INVISIBLE);
            }

            currentStep.setText((position + 1) + "/" + numberOfSteps);
        }

        prevVideo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                position--;
                mCallback.onNewStepSelected(position);
            }
        });

        nextVideo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                position++;
                mCallback.onNewStepSelected(position);
            }
        });

        registerInternetReceiver();

        return root;
    }

    private void setStepDescriptionText(int position) {
        stepDescription.setText(step.getDescription());
    }

    private void initializeExoPlayer(int position) {
        if (!step.getVideoURL().isEmpty()) {
            noVideo.setVisibility(View.GONE);
            exoPlayerView.setVisibility(View.VISIBLE);
            initializePlayer(Uri.parse(step.getVideoURL()));
        } else {
            noVideo.setVisibility(View.VISIBLE);
            exoPlayerView.setVisibility(View.GONE);
        }
    }

    private void initializePlayer(Uri uri) {
        if (mExoPlayer == null) {
            mExoPlayer = ExoPlayerFactory.newSimpleInstance(
                    new DefaultRenderersFactory(getActivity()),
                    new DefaultTrackSelector(), new DefaultLoadControl());
            exoPlayerView.setPlayer(mExoPlayer);
            mExoPlayer.setPlayWhenReady(true);
            MediaSource mediaSource = buildMediaSource(uri);

            if (exoplayer_position != null) {
                mExoPlayer.seekTo(exoplayer_position);
            }

            mExoPlayer.prepare(mediaSource, true, false);
        }
    }

    private MediaSource buildMediaSource(Uri uri) {
        return new ExtractorMediaSource(uri,
                new DefaultHttpDataSourceFactory("ua"),
                new DefaultExtractorsFactory(), null, null);
    }

    private void releasePlayer() {
        if (mExoPlayer != null) {
            mExoPlayer.release();
            mExoPlayer = null;
        }
    }

    private void registerInternetReceiver() {
        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                isConnected = NetworkUtil.isNetworkConnection(context);

                if (isConnected) {
                    exoPlayerView.setVisibility(View.VISIBLE);
                    releasePlayer();
                    initializeExoPlayer(position);
                }
            }
        };

        intentFilter = new IntentFilter("android.net.conn.CONNECTIVITY_CHANGE");

        getActivity().registerReceiver(receiver, intentFilter);
    }

    @Override
    public void onConfigurationChanged(final Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        if (isTablet) {
            return;
        }

        setSpecificCaseUi();

        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            topLayout.setLayoutParams(new LinearLayout.LayoutParams(width, height));
            bottomLayout.setLayoutParams(new LinearLayout.LayoutParams(width, height));

        } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
            topLayout.setLayoutParams(new LinearLayout.LayoutParams(height, width / 2));
            bottomLayout.setLayoutParams(new LinearLayout.LayoutParams(height, width / 2));
        }
    }

    private void setSpecificCaseUi() {
        if (isTablet) {
            return;
        }

        exoPlayerView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);

        noVideo.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
    }

    @Override
    public void onStart() {
        super.onStart();

        ViewTreeObserver viewTreeObserver = root.getViewTreeObserver();
        viewTreeObserver.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
                    root.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                } else {
                    root.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                }

                int config = getResources().getConfiguration().orientation;

                if (config != 1) {
                    width = root.getMeasuredWidth();
                    height = root.getMeasuredHeight();

                    if (!isTablet) {
                        topLayout.setLayoutParams(new LinearLayout.LayoutParams(width, height));
                        bottomLayout.setLayoutParams(new LinearLayout.LayoutParams(width, height));
                    }
                } else {
                    height = root.getMeasuredWidth();
                    width = root.getMeasuredHeight();
                }
            }
        });
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        try {
            mCallback = (Callback) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement" + mCallback.toString());
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mCallback = null;
    }

    @Override
    public void onSaveInstanceState(Bundle currentState) {
        if (mExoPlayer != null) {
            currentState.putLong(EXOPLAYER_POSITION, mExoPlayer.getCurrentPosition());
            currentState.putBoolean(EXOPLAYER_POSITION, mExoPlayer.getPlayWhenReady());
        }
        super.onSaveInstanceState(currentState);
        currentState.putInt(STEP_CLICKED, position);
        currentState.putInt(NUMBER_OF_STEPS, numberOfSteps);

    }

    public void onRestoreInstanceState(Bundle savedInstanceState) {
        Log.d(TAG, "onRestoreInstanceState: ");
        super.onSaveInstanceState(savedInstanceState);
        savedInstanceState.putLong("position", mExoPlayer.getCurrentPosition());
        savedInstanceState.putInt(STEP_CLICKED, position);
        savedInstanceState.putInt(NUMBER_OF_STEPS, numberOfSteps);
        savedInstanceState.putBoolean(EXOPLAYER_POSITION, mExoPlayer.getPlayWhenReady());
    }


    @Override
    public void onPause() {
        super.onPause();
        if (Util.SDK_INT <= 23) {
            releasePlayer();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (Util.SDK_INT <= 23) {
            releasePlayer();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (Util.SDK_INT > 23) {
            releasePlayer();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        getActivity().unregisterReceiver(receiver);
    }

}

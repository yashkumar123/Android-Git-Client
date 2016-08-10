package com.example.gaurav.gitfetchapp;

import android.content.Intent;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.gaurav.gitfetchapp.Repositories.RepositoryAdapter;
import com.example.gaurav.gitfetchapp.Repositories.UserRepoJson;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by GAURAV on 28-07-2016.
 */
public class RepositoriesFragment extends Fragment {
    private static final String TAG = RepositoriesFragment.class.getName();
    private View rootView;
    private AccessToken mAccessToken;
    private Intent intent;
    private RepositoryAdapter mRepositoryAdapter;
    private static ArrayList<UserRepoJson> userRepoList = new ArrayList<>();
    private boolean hasRepositoryData = false;

    @BindView(R.id.repository_recycler) RecyclerView repoRecyclerView;

    public RepositoriesFragment() {
        mAccessToken = AccessToken.getInstance();
        Log.v(TAG,"access token: "+mAccessToken.getAccessToken());
    }

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        intent = getActivity().getIntent();
        String token = intent.getExtras().getString(Intent.EXTRA_TEXT);
        GitHubEndpointInterface gitInterface = ServiceGenerator.createService(
                GitHubEndpointInterface.class, mAccessToken);
        Call<ArrayList<UserRepoJson>> call = gitInterface.getUserRepositories();

        mRepositoryAdapter = new RepositoryAdapter(getContext(), R.layout.repository_cardview,
                userRepoList);

        call.enqueue(new Callback<ArrayList<UserRepoJson>>() {
            @Override
            public void onResponse(Call<ArrayList<UserRepoJson>> call, Response<ArrayList<UserRepoJson>> response) {
                ArrayList<UserRepoJson> item = response.body();
                //userRepoList = item;
                hasRepositoryData = true;
//                mRepositoryAdapter = new RepositoryAdapter(getContext(), R.layout.repository_cardview,
//                        userRepoList);
                //mRepositoryAdapter.setData(item);
                mRepositoryAdapter.clear();
                for (UserRepoJson elem : item)
                    mRepositoryAdapter.addItem(elem);
                mRepositoryAdapter.notifyDataSetChanged();
                Log.v(TAG,"items added: "+mRepositoryAdapter.getItemCount());
                //repoRecyclerView.setAdapter(mRepositoryAdapter);
            }

            @Override
            public void onFailure(Call<ArrayList<UserRepoJson>> call, Throwable t) {
                Log.v(TAG,"Stack trace"+t.getMessage());
            }
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup containter,
                             Bundle savedInstanceState){
        rootView = inflater.inflate(R.layout.fragment_repositories, containter, false);
        ButterKnife.bind(this,rootView);

        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        repoRecyclerView.setLayoutManager(layoutManager);
        RecyclerView.ItemDecoration itemDecoration = new
                DividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL_LIST);
        repoRecyclerView.addItemDecoration(itemDecoration);
        repoRecyclerView.setAdapter(mRepositoryAdapter);
        //}
        return rootView;
    }

}
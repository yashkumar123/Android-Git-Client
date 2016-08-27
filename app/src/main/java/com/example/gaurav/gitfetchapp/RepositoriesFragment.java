package com.example.gaurav.gitfetchapp;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Parcelable;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.example.gaurav.gitfetchapp.Events.IssueCommentPayload.User;
import com.example.gaurav.gitfetchapp.Events.Repo;
import com.example.gaurav.gitfetchapp.GooglePlayServices.TrackerApplication;
import com.example.gaurav.gitfetchapp.Repositories.Owner;
import com.example.gaurav.gitfetchapp.Repositories.RepositoryAdapter;
import com.example.gaurav.gitfetchapp.Repositories.RepositoryDetailActivity;
import com.example.gaurav.gitfetchapp.Repositories.RepositoryListAdapter;
import com.example.gaurav.gitfetchapp.Repositories.UserRepoJson;
import com.example.gaurav.gitfetchapp.RepositoryDataBase.RepositoryContract;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.Vector;

import butterknife.BindView;
import butterknife.ButterKnife;
import okhttp3.internal.Util;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by GAURAV on 28-07-2016.
 */
public class RepositoriesFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {
    private static final String TAG = RepositoriesFragment.class.getName();
    private static final String REPO_TAG = "repositories_state";
    private static final int REPOSITORIES_LOADER = 1;
    public static final String REPO_NAMES_PREFS = "repo_names_prefs";
    private View rootView;
    private AccessToken mAccessToken;
    private Intent intent;
    private RepositoryAdapter mRepositoryAdapter;
    private ArrayList<UserRepoJson> userRepoList;
    private boolean hasRepositoryData = false;
    private ArrayList<String> repoNames;
    private Context mContext;
    private RepositoryListAdapter mRepoListAdapter;
    private String[] intentData;
    private Parcelable state;
    private Tracker mTracker;

    Vector<ContentValues> cVVector;
    //@BindView(R.id.repository_recycler) RecyclerView repoRecyclerView;
    @BindView(R.id.repository_recycler)
    ListView repoRecyclerView;

    public static final String[] REPOSITORY_COLUMNS_ALL = {
            // In this case the id needs to be fully qualified with a table name, since
            // the content provider joins the location & weather tables in the background
            // (both have an _id column)
            // On the one hand, that's annoying.  On the other, you can search the weather table
            // using the location set by the user, which is only in the Location table.
            // So the convenience is worth it.
            RepositoryContract.RepositoryEntry.TABLE_NAME + "." + RepositoryContract.RepositoryEntry._ID,
            RepositoryContract.RepositoryEntry.TABLE_NAME + "." + RepositoryContract.RepositoryEntry.COLUMN_ID,
            RepositoryContract.RepositoryEntry.COLUMN_OWNER_KEY,
            RepositoryContract.RepositoryEntry.COLUMN_NAME,
            RepositoryContract.RepositoryEntry.COLUMN_FULL_NAME,
            RepositoryContract.RepositoryEntry.COLUMN_IS_PRIVATE,
            RepositoryContract.RepositoryEntry.TABLE_NAME + "." + RepositoryContract.RepositoryEntry.COLUMN_HTML_URL,
            RepositoryContract.RepositoryEntry.COLUMN_DESCRIPTION,
            RepositoryContract.RepositoryEntry.COLUMN_IS_FORK,
            RepositoryContract.RepositoryEntry.TABLE_NAME + "." + RepositoryContract.RepositoryEntry.COLUMN_URL,
            RepositoryContract.RepositoryEntry.COLUMN_FORKS_URL,
            RepositoryContract.RepositoryEntry.COLUMN_KEY_URL,
            RepositoryContract.RepositoryEntry.COLUMN_COLLABORATORS_URL,
            RepositoryContract.RepositoryEntry.COLUMN_TEAMS_URL,
            RepositoryContract.RepositoryEntry.COLUMN_ISSUE_EVENTS_URL,
            RepositoryContract.RepositoryEntry.TABLE_NAME + "." + RepositoryContract.RepositoryEntry.COLUMN_EVENTS_URL,
            RepositoryContract.RepositoryEntry.COLUMN_ASSIGNEES_URL,
            RepositoryContract.RepositoryEntry.COLUMN_BRANCHES_URL,
            RepositoryContract.RepositoryEntry.COLUMN_TAGS_URL,
            RepositoryContract.RepositoryEntry.COLUMN_BLOBS_URL,
            RepositoryContract.RepositoryEntry.COLUMN_GIT_TAGS_URL,
            RepositoryContract.RepositoryEntry.COLUMN_REFS_URL,
            RepositoryContract.RepositoryEntry.COLUMN_TREES_URL,
            RepositoryContract.RepositoryEntry.COLUMN_STATUSES_URL,
            RepositoryContract.RepositoryEntry.COLUMN_LANGUAGES_URL,
            RepositoryContract.RepositoryEntry.COLUMN_STARGAZERS_URL,
            RepositoryContract.RepositoryEntry.COLUMN_CONTRIBUTORS_URL,
            RepositoryContract.RepositoryEntry.COLUMN_SUBSCRIBERS_URL,
            RepositoryContract.RepositoryEntry.TABLE_NAME + "." + RepositoryContract.RepositoryEntry.COLUMN_SUBSCRIPTION_URL,
            RepositoryContract.RepositoryEntry.COLUMN_COMMITS_URL,
            RepositoryContract.RepositoryEntry.COLUMN_GIT_COMMITS_URL,
            RepositoryContract.RepositoryEntry.COLUMN_COMMENTS_URL,
            RepositoryContract.RepositoryEntry.COLUMN_ISSUE_COMMENT_URL,
            RepositoryContract.RepositoryEntry.COLUMN_CONTENTS_URL,
            RepositoryContract.RepositoryEntry.COLUMN_COMPARE_URL,
            RepositoryContract.RepositoryEntry.COLUMN_MERGES_URL,
            RepositoryContract.RepositoryEntry.COLUMN_ARCHIVE_URL,
            RepositoryContract.RepositoryEntry.COLUMN_DOWNLOADS_URL,
            RepositoryContract.RepositoryEntry.COLUMN_ISSUES_URL,
            RepositoryContract.RepositoryEntry.COLUMN_PULLS_URL,
            RepositoryContract.RepositoryEntry.COLUMN_MILESTONES_URL,
            RepositoryContract.RepositoryEntry.COLUMN_NOTIFICATIONS_URL,
            RepositoryContract.RepositoryEntry.COLUMN_LABELS_URL,
            RepositoryContract.RepositoryEntry.COLUMN_RELEASES_URL,
            RepositoryContract.RepositoryEntry.COLUMN_DEPLOYMENTS_URL,
            RepositoryContract.RepositoryEntry.COLUMN_CREATED_AT,
            RepositoryContract.RepositoryEntry.COLUMN_PUSHED_AT,
            RepositoryContract.RepositoryEntry.COLUMN_GIT_URL,
            RepositoryContract.RepositoryEntry.COLUMN_SSH_URL,
            RepositoryContract.RepositoryEntry.COLUMN_CLONE_URL,
            RepositoryContract.RepositoryEntry.COLUMN_SVN_URL,
            RepositoryContract.RepositoryEntry.COLUMN_HOMEPAGE,
            RepositoryContract.RepositoryEntry.COLUMN_SIZE,
            RepositoryContract.RepositoryEntry.COLUMN_STARGAZERS_COUNT,
            RepositoryContract.RepositoryEntry.COLUMN_WATCHERS_COUNT,
            RepositoryContract.RepositoryEntry.COLUMN_LANGUAGE,
            RepositoryContract.RepositoryEntry.COLUMN_HAS_ISSUES,
            RepositoryContract.RepositoryEntry.COLUMN_HAS_DOWNLOADS,
            RepositoryContract.RepositoryEntry.COLUMN_HAS_WIKI,
            RepositoryContract.RepositoryEntry.COLUMN_HAS_PAGES,
            RepositoryContract.RepositoryEntry.COLUMN_FORKS_COUNT,
            RepositoryContract.RepositoryEntry.COLUMN_MIRROR_URL,
            RepositoryContract.RepositoryEntry.COLUMN_OPEN_ISSUES_COUNT,
            RepositoryContract.RepositoryEntry.COLUMN_FORKS,
            RepositoryContract.RepositoryEntry.COLUMN_OPEN_ISSUES,
            RepositoryContract.RepositoryEntry.COLUMN_WATCHERS,
            RepositoryContract.RepositoryEntry.COLUMN_DEFAULT_BRANCH
    };

    public static final String[] REPOSITORY_COLUMNS = {
            RepositoryContract.RepositoryEntry.TABLE_NAME + "." + RepositoryContract.RepositoryEntry._ID,
            RepositoryContract.RepositoryEntry.COLUMN_OWNER_KEY,
            RepositoryContract.RepositoryEntry.TABLE_NAME + "." + RepositoryContract.RepositoryEntry.COLUMN_ID,
            RepositoryContract.RepositoryEntry.COLUMN_NAME,
            RepositoryContract.RepositoryEntry.COLUMN_FULL_NAME,
            RepositoryContract.RepositoryEntry.TABLE_NAME + "." + RepositoryContract.RepositoryEntry.COLUMN_HTML_URL,
            RepositoryContract.RepositoryEntry.COLUMN_CREATED_AT,
            RepositoryContract.RepositoryEntry.COLUMN_PUSHED_AT,
            RepositoryContract.RepositoryEntry.COLUMN_SIZE,
            RepositoryContract.RepositoryEntry.COLUMN_STARGAZERS_COUNT,
            RepositoryContract.RepositoryEntry.COLUMN_WATCHERS_COUNT,
            RepositoryContract.RepositoryEntry.COLUMN_LANGUAGE,
            RepositoryContract.RepositoryEntry.COLUMN_FORKS_COUNT,
            RepositoryContract.RepositoryEntry.COLUMN_OPEN_ISSUES_COUNT,
            RepositoryContract.RepositoryEntry.COLUMN_FORKS,
            RepositoryContract.RepositoryEntry.COLUMN_OPEN_ISSUES,
            RepositoryContract.RepositoryEntry.COLUMN_WATCHERS,
            RepositoryContract.RepositoryEntry.COLUMN_DEFAULT_BRANCH
    };

    public static final String[] OWNER_COLUMNS = {
            RepositoryContract.OwnerEntry.TABLE_NAME + "." + RepositoryContract.OwnerEntry._ID,
            RepositoryContract.OwnerEntry.COLUMN_LOGIN,
            RepositoryContract.OwnerEntry.TABLE_NAME + "." + RepositoryContract.OwnerEntry.COLUMN_ID,
            RepositoryContract.OwnerEntry.COLUMN_AVATAR_URL,
            RepositoryContract.OwnerEntry.COLUMN_GRAVATAR_ID,
            RepositoryContract.OwnerEntry.COLUMN_URL,
            RepositoryContract.OwnerEntry.TABLE_NAME + "." + RepositoryContract.OwnerEntry.COLUMN_HTML_URL,
            RepositoryContract.OwnerEntry.COLUMN_FOLLOWERS_URL,
            RepositoryContract.OwnerEntry.COLUMN_FOLLOWING_URL,
            RepositoryContract.OwnerEntry.COLUMN_GISTS_URL,
            RepositoryContract.OwnerEntry.COLUMN_STARRED_URL,
            RepositoryContract.OwnerEntry.COLUMN_SUBSCRIPTION_URL,
            RepositoryContract.OwnerEntry.COLUMN_ORGANIZATIONS_URL,
            RepositoryContract.OwnerEntry.COLUMN_REPOS_URL,
            RepositoryContract.OwnerEntry.COLUMN_EVENTS_URL,
            RepositoryContract.OwnerEntry.COLUMN_RECEIVED_EVENTS_URL,
            RepositoryContract.OwnerEntry.COLUMN_TYPE,
            RepositoryContract.OwnerEntry.COLUMN_SITE_ADMIN
    };

    public static final int COLUMN_REPOSITORY_TABLE_ID =  0;
    public static final int COLUMN_OWNER_KEY = 1;
    public static final int COLUMN_ID = 2;
    public static final int COLUMN_NAME = 3;
    public static final int COLUMN_FULL_NAME = 4;
    public static final int COLUMN_HTML_URL = 5;
    public static final int COLUMN_CREATED_AT =6;
    public static final int COLUMN_PUSHED_AT = 7;
    public static final int COLUMN_SIZE = 8;
    public static final int COLUMN_STARGAZERS_COUNT = 9;
    public static final int COLUMN_WATCHERS_COUNT = 10;
    public static final int COLUMN_LANGUAGE = 11;
    public static final int COLUMN_FORKS_COUNT = 12;
    public static final int COLUMN_OPEN_ISSUES_COUNT = 13;
    public static final int COLUMN_FORKS = 14;
    public static final int COLUMN_OPEN_ISSUES = 15;
    public static final int COLUMN_WATCHERS = 16;
    public static final int COLUMN_DEFAULT_BRANCH = 17;

    public RepositoriesFragment() {
        mAccessToken = AccessToken.getInstance();
        repoNames = new ArrayList<>();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mContext = context;
    }

    @Override
    public void onStart() {
        super.onStart();
        Log.v(TAG,"on start");
        //fetchRepositories();
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.v(TAG,"On resume");
        Log.i(TAG, "Setting screen name: " + TAG);
        mTracker.setScreenName("Image~" + TAG);
        mTracker.send(new HitBuilders.ScreenViewBuilder().build());
    }

    @Override
    public void onSaveInstanceState(Bundle outState){
        outState.putParcelableArrayList(REPO_TAG, mRepoListAdapter.getRepositories());
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        Log.v(TAG,"on Create");
        intent = getActivity().getIntent();
        //String token = intent.getExtras().getString(Intent.EXTRA_TEXT);
        //intentData = intent.getExtras().getStringArray(Intent.EXTRA_TEXT);
        //String token = intentData[1];
        userRepoList = null;
        mRepositoryAdapter = new RepositoryAdapter(getContext(), R.layout.repository_cardview,
                new ArrayList<UserRepoJson>());
        mRepoListAdapter = new RepositoryListAdapter(getContext(),null);
        if(savedInstanceState != null)
            userRepoList = savedInstanceState.getParcelableArrayList(REPO_TAG);

        if(userRepoList == null)
            fetchRepositories();
        else{
            Log.v(TAG,"requesting data from database");
            mRepoListAdapter.updateValues(userRepoList);
        }

    }

    public void updateList(ArrayList<UserRepoJson> list){
        userRepoList = list;
        for(UserRepoJson elem: userRepoList)
            Log.v(TAG,"elem: "+elem.getName());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup containter,
                             Bundle savedInstanceState){
        rootView = inflater.inflate(R.layout.fragment_repositories, containter, false);
        ButterKnife.bind(this,rootView);

        if(!Utility.hasConnection(getContext()))
            Toast.makeText(getActivity(),R.string.notOnline, Toast.LENGTH_SHORT).show();
        /*LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        repoRecyclerView.setLayoutManager(layoutManager);
        RecyclerView.ItemDecoration itemDecoration = new
                DividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL_LIST);
        repoRecyclerView.addItemDecoration(itemDecoration);
        repoRecyclerView.setAdapter(mRepositoryAdapter);*/
        //}
        return rootView;
    }

    @Override
    public void onPause() {
        state = repoRecyclerView.onSaveInstanceState();
        super.onPause();
    }

    @Override
    public void onViewCreated(final View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        TrackerApplication application = (TrackerApplication) getActivity().getApplication();
        mTracker = application.getDefaultTracker();

        repoRecyclerView.setAdapter(mRepoListAdapter);
        repoRecyclerView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                Log.v(TAG,"OnItem Click "+position);
                Intent intent = new Intent(mContext, RepositoryDetailActivity.class);
                intent.putExtra(Intent.EXTRA_TEXT, mRepoListAdapter.getItem(position)); //userRepoList.get(position));
                mContext.startActivity(intent);
            }
        });

        // Restore previous state (including selected item index and scroll position)
        if(state != null) {
            repoRecyclerView.onRestoreInstanceState(state);
        }
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getLoaderManager().initLoader(REPOSITORIES_LOADER, null, this);

        ((AppCompatActivity) getActivity()).getSupportActionBar().setBackgroundDrawable(
               getResources().getDrawable(R.drawable.app_bar_gradient));
                //new ColorDrawable(getResources().getColor(R.color.colorPrimary)));
        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle("Repositories");

        Window window = getActivity().getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.setStatusBarColor(getResources().getColor(R.color.colorPrimaryDark));
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());

        String owner = prefs.getString(PreLoginDeciderActivity.USERNAME_KEY,null);
        // PreLoginDeciderActivity.getLoginName();// intentData[0];
        Uri repoWithOwnerUri = RepositoryContract.RepositoryEntry.buildRepositoryUriWithOwner(owner);
        Log.v(TAG,"repo with owner uri: "+repoWithOwnerUri);
        return new CursorLoader(getActivity(),
                repoWithOwnerUri,
                REPOSITORY_COLUMNS,
                null,
                null,
                null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mRepoListAdapter.swapCursor(data);
        mRepoListAdapter.clear();
        ArrayList<UserRepoJson> items = new ArrayList<>();
        if( data != null && data.moveToFirst()) {
            int ownerId = data.getInt(COLUMN_OWNER_KEY);
            Owner owner = new Owner();
            Cursor repoCursor = mContext.getContentResolver().query(
                    RepositoryContract.OwnerEntry.CONTENT_URI,
                    OWNER_COLUMNS,
                    RepositoryContract.OwnerEntry._ID + " = ?",
                    new String[]{Integer.toString(ownerId)},
                    null);
            if(repoCursor != null && repoCursor.moveToFirst()) {
                Log.v(TAG,"owner");
                owner.setId(repoCursor.getInt(repoCursor.getColumnIndex(RepositoryContract.OwnerEntry.COLUMN_ID)));
                owner.setLogin(repoCursor.getString(repoCursor.getColumnIndex(RepositoryContract.OwnerEntry.COLUMN_LOGIN)));
                owner.setAvatarUrl(repoCursor.getString(repoCursor.getColumnIndex(RepositoryContract.OwnerEntry.COLUMN_AVATAR_URL)));
                owner.setGravatarId(repoCursor.getString(repoCursor.getColumnIndex(RepositoryContract.OwnerEntry.COLUMN_GRAVATAR_ID)));
                owner.setUrl(repoCursor.getString(repoCursor.getColumnIndex(RepositoryContract.OwnerEntry.COLUMN_URL)));
                owner.setHtmlUrl(repoCursor.getString(repoCursor.getColumnIndex(RepositoryContract.OwnerEntry.COLUMN_HTML_URL)));
                owner.setFollowersUrl(repoCursor.getString(repoCursor.getColumnIndex(RepositoryContract.OwnerEntry.COLUMN_FOLLOWERS_URL)));
                owner.setFollowingUrl(repoCursor.getString(repoCursor.getColumnIndex(RepositoryContract.OwnerEntry.COLUMN_FOLLOWING_URL)));
                owner.setGistsUrl(repoCursor.getString(repoCursor.getColumnIndex(RepositoryContract.OwnerEntry.COLUMN_GISTS_URL)));
                owner.setStarredUrl(repoCursor.getString(repoCursor.getColumnIndex(RepositoryContract.OwnerEntry.COLUMN_STARRED_URL)));
                owner.setSubscriptionsUrl(repoCursor.getString(repoCursor.getColumnIndex(RepositoryContract.OwnerEntry.COLUMN_SUBSCRIPTION_URL)));
                owner.setOrganizationsUrl(repoCursor.getString(repoCursor.getColumnIndex(RepositoryContract.OwnerEntry.COLUMN_ORGANIZATIONS_URL)));
                owner.setReposUrl(repoCursor.getString(repoCursor.getColumnIndex(RepositoryContract.OwnerEntry.COLUMN_REPOS_URL)));
                owner.setEventsUrl(repoCursor.getString(repoCursor.getColumnIndex(RepositoryContract.OwnerEntry.COLUMN_EVENTS_URL)));
                owner.setReceivedEventsUrl(repoCursor.getString(repoCursor.getColumnIndex(RepositoryContract.OwnerEntry.COLUMN_RECEIVED_EVENTS_URL)));
                owner.setType(repoCursor.getString(repoCursor.getColumnIndex(RepositoryContract.OwnerEntry.COLUMN_TYPE)));
                owner.setSiteAdmin(false);
            }
            do{
                UserRepoJson item = new UserRepoJson();
                item.setId(data.getInt(COLUMN_ID));
                item.setOwner(owner);
                item.setName(data.getString(COLUMN_NAME));
                item.setFullName(data.getString(COLUMN_FULL_NAME));
                item.setHtmlUrl( data.getString(COLUMN_HTML_URL));
                item.setCreatedAt(Utility.formatDateString(data.getString(COLUMN_CREATED_AT)));
                item.setPushedAt(Utility.formatDateString(data.getString(COLUMN_PUSHED_AT)));
                item.setSize(data.getInt(COLUMN_SIZE));
                item.setStargazersCount(data.getInt(COLUMN_STARGAZERS_COUNT));
                item.setWatchersCount(data.getInt(COLUMN_WATCHERS_COUNT));
                item.setLanguage(data.getString(COLUMN_LANGUAGE));
                item.setForksCount(data.getInt(COLUMN_FORKS_COUNT));
                item.setOpenIssuesCount(data.getInt(COLUMN_OPEN_ISSUES_COUNT));
                item.setForks(data.getInt(COLUMN_FORKS));
                item.setOpenIssues(data.getInt(COLUMN_OPEN_ISSUES));
                item.setWatchers(data.getInt(COLUMN_WATCHERS));
                item.setDefaultBranch(data.getString(COLUMN_DEFAULT_BRANCH));

                items.add(item);
            }while(data.moveToNext());
            mRepoListAdapter.updateValues(items);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mRepoListAdapter.swapCursor(null);
    }

    public void fetchRepositories(){
        Log.v(TAG,"requesting data over network");
        GitHubEndpointInterface gitInterface = ServiceGenerator.createService(
                GitHubEndpointInterface.class, mAccessToken);
        Call<ArrayList<UserRepoJson>> call = gitInterface.getUserRepositories();

        if(Utility.hasConnection(getContext())) {
            call.enqueue(new Callback<ArrayList<UserRepoJson>>() {
                @Override
                public void onResponse(Call<ArrayList<UserRepoJson>> call, Response<ArrayList<UserRepoJson>> response) {
                    if (response.isSuccessful()) {
                        ArrayList<UserRepoJson> item = response.body();
                        //updateList(item);
                        cVVector = new Vector<ContentValues>(item.size());
                        hasRepositoryData = true;
                        mRepositoryAdapter.clear();

                        Cursor repoCursor = mContext.getContentResolver().query(
                                RepositoryContract.OwnerEntry.CONTENT_URI,
                                new String[]{RepositoryContract.OwnerEntry._ID},
                                RepositoryContract.OwnerEntry.COLUMN_LOGIN + " = ?",
                                new String[]{item.get(0).getOwner().getLogin()},
                                null);

                        long ownerId;


                        if (repoCursor.moveToFirst()) {
                            int ownerIndex = repoCursor.getColumnIndex(RepositoryContract.OwnerEntry._ID);
                            ownerId = repoCursor.getLong(ownerIndex);
                        } else {

                            Owner ownerItem = item.get(0).getOwner();
                            ContentValues ownerValues = new ContentValues();
                            ownerValues.put(RepositoryContract.OwnerEntry.COLUMN_ID, ownerItem.getId());
                            ownerValues.put(RepositoryContract.OwnerEntry.COLUMN_LOGIN, ownerItem.getLogin());
                            ownerValues.put(RepositoryContract.OwnerEntry.COLUMN_AVATAR_URL, ownerItem.getAvatarUrl());
                            ownerValues.put(RepositoryContract.OwnerEntry.COLUMN_GRAVATAR_ID, ownerItem.getGravatarId());
                            ownerValues.put(RepositoryContract.OwnerEntry.COLUMN_URL, ownerItem.getUrl());
                            ownerValues.put(RepositoryContract.OwnerEntry.COLUMN_HTML_URL, ownerItem.getHtmlUrl());
                            ownerValues.put(RepositoryContract.OwnerEntry.COLUMN_EVENTS_URL, ownerItem.getEventsUrl());
                            ownerValues.put(RepositoryContract.OwnerEntry.COLUMN_FOLLOWERS_URL, ownerItem.getFollowersUrl());
                            ownerValues.put(RepositoryContract.OwnerEntry.COLUMN_FOLLOWING_URL, ownerItem.getFollowingUrl());
                            ownerValues.put(RepositoryContract.OwnerEntry.COLUMN_SUBSCRIPTION_URL, ownerItem.getSubscriptionsUrl());
                            ownerValues.put(RepositoryContract.OwnerEntry.COLUMN_ORGANIZATIONS_URL, ownerItem.getOrganizationsUrl());
                            ownerValues.put(RepositoryContract.OwnerEntry.COLUMN_GISTS_URL, ownerItem.getGistsUrl());
                            ownerValues.put(RepositoryContract.OwnerEntry.COLUMN_STARRED_URL, ownerItem.getStarredUrl());
                            ownerValues.put(RepositoryContract.OwnerEntry.COLUMN_RECEIVED_EVENTS_URL, ownerItem.getReceivedEventsUrl());
                            ownerValues.put(RepositoryContract.OwnerEntry.COLUMN_REPOS_URL, ownerItem.getReposUrl());
                            ownerValues.put(RepositoryContract.OwnerEntry.COLUMN_TYPE, ownerItem.getType());
                            ownerValues.put(RepositoryContract.OwnerEntry.COLUMN_SITE_ADMIN, ownerItem.getSiteAdmin());

                            // Finally, insert owner data into the database.
                            Uri insertedUri = getContext().getContentResolver().insert(
                                    RepositoryContract.OwnerEntry.CONTENT_URI,
                                    ownerValues
                            );
                            ownerId = ContentUris.parseId(insertedUri);
                        }

                        for (UserRepoJson elem : item) {
                            mRepositoryAdapter.addItem(elem);
                            repoNames.add(elem.getName());
                            ContentValues repoValues = new ContentValues();
                            repoValues.put(RepositoryContract.RepositoryEntry.COLUMN_OWNER_KEY, ownerId);
                            repoValues.put(RepositoryContract.RepositoryEntry.COLUMN_ID, elem.getId());
                            repoValues.put(RepositoryContract.RepositoryEntry.COLUMN_NAME, elem.getName());
                            repoValues.put(RepositoryContract.RepositoryEntry.COLUMN_FULL_NAME, elem.getFullName());
                            repoValues.put(RepositoryContract.RepositoryEntry.COLUMN_HTML_URL, elem.getHtmlUrl());
                            repoValues.put(RepositoryContract.RepositoryEntry.COLUMN_CREATED_AT,
                                    Utility.formatDateString(elem.getCreatedAt()));
                            repoValues.put(RepositoryContract.RepositoryEntry.COLUMN_PUSHED_AT,
                                    Utility.formatDateString(elem.getPushedAt()));
                            repoValues.put(RepositoryContract.RepositoryEntry.COLUMN_SIZE, elem.getSize());
                            repoValues.put(RepositoryContract.RepositoryEntry.COLUMN_STARGAZERS_COUNT, elem.getStargazersCount());
                            repoValues.put(RepositoryContract.RepositoryEntry.COLUMN_WATCHERS_COUNT, elem.getWatchersCount());
                            repoValues.put(RepositoryContract.RepositoryEntry.COLUMN_LANGUAGE, elem.getLanguage());
                            repoValues.put(RepositoryContract.RepositoryEntry.COLUMN_FORKS_COUNT, elem.getForksCount());
                            repoValues.put(RepositoryContract.RepositoryEntry.COLUMN_OPEN_ISSUES_COUNT, elem.getOpenIssuesCount());
                            repoValues.put(RepositoryContract.RepositoryEntry.COLUMN_FORKS, elem.getForks());
                            repoValues.put(RepositoryContract.RepositoryEntry.COLUMN_OPEN_ISSUES, elem.getOpenIssues());
                            repoValues.put(RepositoryContract.RepositoryEntry.COLUMN_WATCHERS, elem.getWatchers());
                            repoValues.put(RepositoryContract.RepositoryEntry.COLUMN_DEFAULT_BRANCH, elem.getDefaultBranch());

                            cVVector.add(repoValues);
                        }

                        int inserted = 0;
                        // add to database
                        if (cVVector.size() > 0) {
                            ContentValues[] cvArray = new ContentValues[cVVector.size()];
                            cVVector.toArray(cvArray);
                            inserted = mContext.getContentResolver().bulkInsert(
                                    RepositoryContract.RepositoryEntry.CONTENT_URI, cvArray);
                        }

                        mRepositoryAdapter.notifyDataSetChanged();
                        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(mContext);
                        SharedPreferences.Editor editor = pref.edit();
                        editor.putStringSet(REPO_NAMES_PREFS, new HashSet<String>(repoNames));
                        editor.apply();
                    }
                    //repoRecyclerView.setAdapter(mRepositoryAdapter);
                }

                @Override
                public void onFailure(Call<ArrayList<UserRepoJson>> call, Throwable t) {
                    Log.v(TAG, "Stack trace" + t.getMessage());
                }
            });
        } else {
            Toast.makeText(getActivity(),R.string.notOnline, Toast.LENGTH_SHORT).show();
        }
    }
}

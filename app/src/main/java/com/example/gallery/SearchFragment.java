package com.example.gallery;


import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.gallery.Adapters.ImageAdapter;
import com.example.gallery.Models.PhotoResult;
import com.example.gallery.Retrofit.Flickr;
import com.example.gallery.Retrofit.RetrofitClient;
import com.mancj.materialsearchbar.MaterialSearchBar;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import retrofit2.Retrofit;


/**
 * A simple {@link Fragment} subclass.
 */
public class SearchFragment extends Fragment {

    static SearchFragment instance;

    public static SearchFragment getInstance() {
        if(instance==null)
            instance=new SearchFragment();
        return instance;
    }

    View view=null;

    private RecyclerView searchRecyclerView;
    private ImageAdapter adapter;
    private ProgressDialog dialog;
    private MaterialSearchBar searchBar;
    boolean networkConnected;
    Flickr mService;
    CompositeDisposable compositeDisposable;
    Snackbar snackbar;

    public SearchFragment() {
        // Required empty public constructor
        compositeDisposable=new CompositeDisposable();
        Retrofit retrofit= RetrofitClient.getInstance();
        mService=retrofit.create(Flickr.class);
    }

    public BroadcastReceiver mConnReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            boolean noConnectivity = intent.getBooleanExtra(ConnectivityManager.EXTRA_NO_CONNECTIVITY, false);
            String reason = intent.getStringExtra(ConnectivityManager.EXTRA_REASON);
            boolean isFailover = intent.getBooleanExtra(ConnectivityManager.EXTRA_IS_FAILOVER, false);

            NetworkInfo currentNetworkInfo = (NetworkInfo) intent.getParcelableExtra(ConnectivityManager.EXTRA_NETWORK_INFO);
            NetworkInfo otherNetworkInfo = (NetworkInfo) intent.getParcelableExtra(ConnectivityManager.EXTRA_OTHER_NETWORK_INFO);

            networkConnected=currentNetworkInfo.isConnected();

            if (currentNetworkInfo.isConnected()) {
                //Toast.makeText(getContext(), "Connected", Toast.LENGTH_LONG).show();
            } else {
                makeSnackBar();
            }
        }
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view= inflater.inflate(R.layout.fragment_search, container, false);

        searchRecyclerView=(RecyclerView)view.findViewById(R.id.searchRecyclerView);

        searchRecyclerView.setLayoutManager(new GridLayoutManager(getActivity(),2));

        searchBar=(MaterialSearchBar)view.findViewById(R.id.materialSearchBar);
        searchBar.setEnabled(true);
        dialog=new ProgressDialog(getActivity());
        dialog.setTitle("Searching Images");
        dialog.setMessage("Please wait while we search the images...");
        dialog.setCanceledOnTouchOutside(false);
        searchRecyclerView.addItemDecoration(new SpacesItemDecoration(16));

        //getSearchResultsFromFlickr();


        setRetainInstance(true);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        searchBar.setOnSearchActionListener(new MaterialSearchBar.OnSearchActionListener() {
            @Override
            public void onSearchStateChanged(boolean enabled) {

            }

            @Override
            public void onSearchConfirmed(CharSequence text) {
                if(networkConnected) {
                    if (TextUtils.isEmpty(text.toString()) == false)
                        getSearchResultsFromFlickr(text.toString());
                }
                else
                    makeSnackBar();
            }

            @Override
            public void onButtonClicked(int buttonCode) {

            }
        });
        searchBar.addTextChangeListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(networkConnected) {
                    if (TextUtils.isEmpty(s.toString()) == false)
                        getSearchResultsFromFlickr(s.toString());
                }
                else
                    makeSnackBar();
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        getActivity().registerReceiver(this.mConnReceiver,
                new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));

    }

    private void getSearchResultsFromFlickr(String text) {
        compositeDisposable.add(mService.getSearchResult("flickr.photos.search",
                Common.API_KEY,
                "url_s",
                "json",
                "1",
                text)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<PhotoResult>() {
                    @Override
                    public void accept(PhotoResult photoResult) throws Exception {
                        displayResult(photoResult);

                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        dialog.dismiss();

                        //Toast.makeText(getActivity(),throwable.getMessage(),Toast.LENGTH_LONG).show();

                    }
                })
        );

    }
    private void displayResult(PhotoResult photoResult) {
        adapter=new ImageAdapter(photoResult);
        searchRecyclerView.setAdapter(adapter);
    }


    public void makeSnackBar(){
        snackbar=Snackbar.make(view,"Internet not there",Snackbar.LENGTH_INDEFINITE);
        snackbar.setAction("Retry", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(networkConnected==true) {
                    if (TextUtils.isEmpty(searchBar.getText()) == false)
                        getSearchResultsFromFlickr(searchBar.getText());
                }
                else
                    makeSnackBar();
            }
        });
        snackbar.show();
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        getActivity().unregisterReceiver(this.mConnReceiver);
    }


    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);

    }


}

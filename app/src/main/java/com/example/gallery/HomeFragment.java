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
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.gallery.Adapters.ImageAdapter;
import com.example.gallery.Models.PhotoResult;
import com.example.gallery.Retrofit.Flickr;
import com.example.gallery.Retrofit.RetrofitClient;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import retrofit2.Retrofit;


/**
 * A simple {@link Fragment} subclass.
 */
public class HomeFragment extends Fragment {

    static HomeFragment instance;

    public static HomeFragment getInstance() {
        if(instance==null)
            instance=new HomeFragment();
        return instance;
    }

    private int previousTotal = 0;
    private boolean loading = true;
    private int visibleThreshold = 5;
    int firstVisibleItem, visibleItemCount, totalItemCount;

    private RecyclerView homeRecyclerView;
    private ImageAdapter adapter;
    private ProgressDialog dialog;
    private static int currentPage=1;
    private boolean first=true;
    int pastVisiblesItems;
    boolean networkConnected=true;
    private GridLayoutManager gridLayoutManager;
    Flickr mService;
    CompositeDisposable compositeDisposable;
    Snackbar snackbar;
    View view;

    public HomeFragment() {
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
                dialog.dismiss();
                //Toast.makeText(getContext(), "Not Connected", Toast.LENGTH_LONG).show();
                makeSnackBar();

            }
        }
    };


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view=inflater.inflate(R.layout.fragment_home, container, false);


        homeRecyclerView=(RecyclerView)view.findViewById(R.id.homeRecyclerView);

        gridLayoutManager=new GridLayoutManager(getActivity(),2);
        homeRecyclerView.setLayoutManager(gridLayoutManager);

        dialog=new ProgressDialog(getActivity());
        dialog.setTitle("Loading Images");
        dialog.setMessage("Please wait while we load the images...");
        dialog.setCanceledOnTouchOutside(false);
        homeRecyclerView.addItemDecoration(new SpacesItemDecoration(16));



        homeRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
            }

            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                if(dy > 0)
                {
                    visibleItemCount = homeRecyclerView.getChildCount();
                    totalItemCount = gridLayoutManager.getItemCount();
                    firstVisibleItem = gridLayoutManager.findFirstVisibleItemPosition();


                    if (loading) {
                        if (totalItemCount > previousTotal) {
                            loading = false;
                            previousTotal = totalItemCount;
                        }
                    }
                    if (!loading && (totalItemCount - visibleItemCount)
                            <= (firstVisibleItem + visibleThreshold)) {


                        currentPage++;
                        System.out.println(currentPage+ " loaded");
                        getPhotosFromFlickr();

                        loading = true;
                    }
                }
            }
        });
        return view;
    }
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        getActivity().registerReceiver(this.mConnReceiver,
                new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));


        if(networkConnected==true)
            getPhotosFromFlickr();
        else{
            makeSnackBar();
        }
    }

    private void getPhotosFromFlickr() {
        dialog.show();
        compositeDisposable.add(mService.getRecentResult("flickr.photos.getRecent",
                Common.API_KEY,
                "url_s",
                "json",
                currentPage,
                20,
                "1")
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<PhotoResult>() {
                    @Override
                    public void accept(PhotoResult photoResult) throws Exception {
                        setPhotosInView(photoResult);

                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        dialog.dismiss();
                        snackbar.show();
                        //Toast.makeText(getActivity(),throwable.getMessage(),Toast.LENGTH_LONG).show();
                    }
                })

        );
    }

    private void setPhotosInView(PhotoResult photoResult) {

        dialog.dismiss();
        String url="https://farm"+photoResult.getPhotos().photo.get(0).getFarm()+".staticflickr.com/"+
                photoResult.photos.photo.get(0).getServer()+"/"+
                photoResult.photos.photo.get(0).getId()+"_"+photoResult.photos.photo.get(0).getSecret()+"_m.jpg";
        System.out.println(photoResult.getPhotos().getTotal());
        if(first) {
            adapter = new ImageAdapter(photoResult);
            homeRecyclerView.setAdapter(adapter);
            first=false;
        }
        else{
            adapter.add(photoResult.getPhotos().photo);
        }

        adapter.notifyDataSetChanged();
    }

    public void makeSnackBar(){
        snackbar=Snackbar.make(view,"Internet not there",Snackbar.LENGTH_INDEFINITE);
        snackbar.setAction("Retry", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(networkConnected==true)
                    getPhotosFromFlickr();
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
}

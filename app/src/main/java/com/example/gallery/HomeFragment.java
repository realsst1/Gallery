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

    private RecyclerView homeRecyclerView;
    private ImageAdapter adapter;
    private ProgressDialog dialog;
    boolean networkConnected;
    Flickr mService;
    CompositeDisposable compositeDisposable;
    Snackbar snackbar;
    View view1;

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

                snackbar=Snackbar.make(getView(),"Internet not there",Snackbar.LENGTH_INDEFINITE);
                snackbar.setAction("Retry", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                            if(networkConnected==false)
                                getPhotosFromFlickr();
                            else
                                snackbar.show();
                    }
                });
                snackbar.show();
            }
        }
    };


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view=inflater.inflate(R.layout.fragment_home, container, false);


        homeRecyclerView=(RecyclerView)view.findViewById(R.id.homeRecyclerView);

        homeRecyclerView.setLayoutManager(new GridLayoutManager(getActivity(),2));

        dialog=new ProgressDialog(getActivity());
        dialog.setTitle("Loading Images");
        dialog.setMessage("Please wait while we load the images...");
        dialog.setCanceledOnTouchOutside(false);
        homeRecyclerView.addItemDecoration(new SpacesItemDecoration(16));


        view1=view;
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
            snackbar=Snackbar.make(getView(),"Internet not there",Snackbar.LENGTH_INDEFINITE);
            snackbar.setAction("Retry", new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(networkConnected==false)
                        getPhotosFromFlickr();
                    else
                        snackbar.show();
                }
            });
            snackbar.show();
        }

    }

    private void getPhotosFromFlickr() {
        dialog.show();
        compositeDisposable.add(mService.getRecentResult("flickr.photos.getRecent",
                Common.API_KEY,
                "url_s",
                "json",
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
        //System.out.println(url);
        adapter=new ImageAdapter(photoResult);
        homeRecyclerView.setAdapter(adapter);
        adapter.notifyDataSetChanged();
    }



}

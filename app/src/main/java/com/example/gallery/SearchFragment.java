package com.example.gallery;


import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
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

    private RecyclerView searchRecyclerView;
    private ImageAdapter adapter;
    private ProgressDialog dialog;
    private MaterialSearchBar searchBar;
    Flickr mService;
    CompositeDisposable compositeDisposable;

    public SearchFragment() {
        // Required empty public constructor
        compositeDisposable=new CompositeDisposable();
        Retrofit retrofit= RetrofitClient.getInstance();
        mService=retrofit.create(Flickr.class);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view= inflater.inflate(R.layout.fragment_search, container, false);
        searchRecyclerView=(RecyclerView)view.findViewById(R.id.searchRecyclerView);

        searchRecyclerView.setLayoutManager(new GridLayoutManager(getActivity(),2));

        searchBar=(MaterialSearchBar)view.findViewById(R.id.materialSearchBar);
        searchBar.setEnabled(true);
        dialog=new ProgressDialog(getActivity());
        dialog.setTitle("Searching Images");
        dialog.setMessage("Please wait while we search the images...");
        dialog.setCanceledOnTouchOutside(false);
        searchRecyclerView.addItemDecoration(new SpacesItemDecoration(16));
        searchBar.setOnSearchActionListener(new MaterialSearchBar.OnSearchActionListener() {
            @Override
            public void onSearchStateChanged(boolean enabled) {

            }

            @Override
            public void onSearchConfirmed(CharSequence text) {
                getSearchResultsFromFlickr(text.toString());
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
                getSearchResultsFromFlickr(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        //getSearchResultsFromFlickr();
        return view;
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
                        Toast.makeText(getActivity(),throwable.getMessage(),Toast.LENGTH_LONG).show();

                    }
                })
        );

    }
    private void displayResult(PhotoResult photoResult) {
        adapter=new ImageAdapter(photoResult);
        searchRecyclerView.setAdapter(adapter);
    }

}

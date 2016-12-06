package com.davidmiguel.gobees.apiaries;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.davidmiguel.gobees.R;
import com.davidmiguel.gobees.addeditapiary.AddEditApiaryActivity;
import com.davidmiguel.gobees.apiaries.ApiariesAdapter.ApiaryItemListener;
import com.davidmiguel.gobees.apiary.ApiaryActivity;
import com.davidmiguel.gobees.apiary.ApiaryHivesFragment;
import com.davidmiguel.gobees.data.model.Apiary;
import com.davidmiguel.gobees.utils.ScrollChildSwipeRefreshLayout;

import java.util.ArrayList;
import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Display a list of apiaries.
 */
public class ApiariesFragment extends Fragment
        implements ApiariesContract.View, ApiaryItemListener {

    private ApiariesContract.Presenter presenter;
    private ApiariesAdapter listAdapter;
    private View noApiariesView;
    private LinearLayout apiariesView;

    public ApiariesFragment() {
        // Requires empty public constructor
    }

    public static ApiariesFragment newInstance() {
        return new ApiariesFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        listAdapter = new ApiariesAdapter(new ArrayList<Apiary>(0), this);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.apiaries_frag, container, false);

        // Set up apiaries list view
        RecyclerView recyclerView = (RecyclerView) root.findViewById(R.id.apiaries_list);
        LinearLayoutManager llm = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(llm);
        recyclerView.setAdapter(listAdapter);
        apiariesView = (LinearLayout) root.findViewById(R.id.apiariesLL);

        // Set up  no apiaries view
        noApiariesView = root.findViewById(R.id.no_apiaries);

        // Set up floating action button
        FloatingActionButton fab =
                (FloatingActionButton) getActivity().findViewById(R.id.fab_add_apiary);
        fab.setImageResource(R.drawable.ic_add);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                presenter.addEditApiary();
            }
        });

        // Set up progress indicator
        final ScrollChildSwipeRefreshLayout swipeRefreshLayout =
                (ScrollChildSwipeRefreshLayout) root.findViewById(R.id.refresh_layout);
        swipeRefreshLayout.setColorSchemeColors(
                ContextCompat.getColor(getActivity(), R.color.colorPrimary),
                ContextCompat.getColor(getActivity(), R.color.colorAccent),
                ContextCompat.getColor(getActivity(), R.color.colorPrimaryDark)
        );

        // Set the scrolling view in the custom SwipeRefreshLayout
        swipeRefreshLayout.setScrollUpChild(recyclerView);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                presenter.loadApiaries(false);
            }
        });

        // Listen menu options
        setHasOptionsMenu(true);

        return root;
    }

    @Override
    public void onResume() {
        super.onResume();
        presenter.start();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.apiaries_frag_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_refresh:
                presenter.loadApiaries(true);
                break;
        }
        return true;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        presenter.result(requestCode, resultCode);
    }

    @Override
    public void setLoadingIndicator(final boolean active) {
        if (getView() == null) {
            return;
        }
        final SwipeRefreshLayout srl =
                (SwipeRefreshLayout) getView().findViewById(R.id.refresh_layout);
        // Make sure setRefreshing() is called after the layout is done with everything else
        srl.post(new Runnable() {
            @Override
            public void run() {
                srl.setRefreshing(active);
            }
        });
    }

    @Override
    public void showApiaries(@NonNull List<Apiary> apiaries) {
        listAdapter.replaceData(apiaries);
        apiariesView.setVisibility(View.VISIBLE);
        noApiariesView.setVisibility(View.GONE);
    }

    @Override
    public void showAddEditApiary() {
        Intent intent = new Intent(getContext(), AddEditApiaryActivity.class);
        startActivityForResult(intent, AddEditApiaryActivity.REQUEST_ADD_APIARY);
    }

    @Override
    public void showApiaryDetail(long apiaryId) {
        Intent intent = new Intent(getActivity(), ApiaryActivity.class);
        intent.putExtra(ApiaryHivesFragment.ARGUMENT_APIARY_ID, apiaryId);
        getActivity().startActivity(intent);
    }

    @Override
    public void showLoadingApiariesError() {
        showMessage(getString(R.string.loading_apiaries_error));
    }

    @Override
    public void showNoApiaries() {
        showNoApiariesViews();
    }

    @Override
    public void showSuccessfullySavedMessage() {
        showMessage(getString(R.string.successfully_saved_apiary_message));
    }

    @Override
    public boolean isActive() {
        return isAdded();
    }

    @Override
    public void setPresenter(@NonNull ApiariesContract.Presenter presenter) {
        this.presenter = checkNotNull(presenter);
    }

    @Override
    public void onApiaryClick(Apiary clickedApiary) {
        presenter.openApiaryDetail(clickedApiary);
    }

    @Override
    public void onApiaryDelete(Apiary clickedApiary) {
        // TODO delete apiary
    }

    /**
     * Shows no apiaries views.
     */
    private void showNoApiariesViews() {
        apiariesView.setVisibility(View.GONE);
        noApiariesView.setVisibility(View.VISIBLE);
    }

    /**
     * Shows a snackbar with the given message.
     *
     * @param message message to show.
     */
    @SuppressWarnings("ConstantConditions")
    private void showMessage(String message) {
        Snackbar.make(getView(), message, Snackbar.LENGTH_LONG).show();
    }
}
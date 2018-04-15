package com.example.nayan.appanalysis2.base;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.nayan.appanalysis2.R;

import java.util.List;

import me.everything.providers.core.Entity;

/**
 * Created by Dev on 12/27/2017.
 */
public abstract class RecycleViewListFragment<T extends Entity> extends BaseFragment {

    private List<T> mEntities;
    private RecyclerView mRecyclerView;
    private EntitiesAdapter mAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_recycle, container, false);
        setToolbarTitle(getTitle());

        // set view + adapter
        mRecyclerView = (RecyclerView) view.findViewById(R.id.recycler_view);
        LinearLayoutManager llm = new LinearLayoutManager(getApplicationContext());
        mRecyclerView.setLayoutManager(llm);
        mAdapter = new EntitiesAdapter();
        mRecyclerView.setAdapter(mAdapter);

        // load data
        GetEntitiesTask<T> getEntitiesTask = new GetEntitiesTask.Builder<T>()
                .setFetcher(getFetcher())
                .setCallback(new GetEntitiesTask.TaskListener<T>() {
                    @Override
                    public void onComplete(List<T> entities) {
                        mEntities = entities;
                        mAdapter.notifyDataSetChanged();
                    }
                })
                .build();
        getEntitiesTask.execute();

        return view;
    }

    private int getCount() {
        return mEntities == null ? 0 : mEntities.size();
    }

    protected abstract String getTitle();

    protected abstract void bindEntity(T entity, TextView title, TextView details);

    protected abstract GetEntitiesTask.DataFetcher<T> getFetcher();

    protected void onSelected(final T entity) {
        CharSequence[] dialogItems = getDialogItems();
        if (dialogItems != null) {
            AlertDialog.Builder dialog = new AlertDialog.Builder(getActivity());
            dialog.setItems(dialogItems, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    onDialogItemSelected(entity, which);
                }
            });
            dialog.show();
        }
    }

    protected CharSequence[] getDialogItems() {
        return null;
    }

    protected void onDialogItemSelected(T entity, int which) {
    }

    private class OnClickListener implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            int position = mRecyclerView.getChildAdapterPosition(v);
            T entity = mEntities.get(position);
            onSelected(entity);
        }

    }

    private static class RowViewHolder extends RecyclerView.ViewHolder {

        private final TextView mTitle;
        private final TextView mDetails;

        public RowViewHolder(View itemView) {
            super(itemView);
            mTitle = (TextView) itemView.findViewById(R.id.title);
            mDetails = (TextView) itemView.findViewById(R.id.details);
        }

    }

    private class EntitiesAdapter extends RecyclerView.Adapter<RowViewHolder> {

        @Override
        public RowViewHolder onCreateViewHolder(ViewGroup parent, int pos) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.view_row_item, parent, false);
            view.setOnClickListener(new OnClickListener());
            return new RowViewHolder(view);
        }

        @Override
        public void onBindViewHolder(RowViewHolder rowViewHolder, int pos) {
            bindEntity(mEntities.get(pos), rowViewHolder.mTitle, rowViewHolder.mDetails);
        }

        @Override
        public int getItemCount() {
            return getCount();
        }
    }

}

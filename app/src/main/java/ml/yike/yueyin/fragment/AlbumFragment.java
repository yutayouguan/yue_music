package ml.yike.yueyin.fragment;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import ml.yike.yueyin.R;
import ml.yike.yueyin.activity.ModelActivity;
import ml.yike.yueyin.adapter.AlbumAdapter;
import ml.yike.yueyin.database.DBManager;
import ml.yike.yueyin.entity.AlbumInfo;
import ml.yike.yueyin.util.MyMusicUtil;

import java.util.ArrayList;

public class AlbumFragment extends Fragment {

    private RecyclerView recyclerView;

    private AlbumAdapter adapter;

    private ArrayList<AlbumInfo> albumInfoList = new ArrayList<>();

    private DBManager dbManager;

    private Context mContext;


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.mContext = context;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.fragment_singer,container,false);
        dbManager = DBManager.getInstance(getContext());
        recyclerView = (RecyclerView)view.findViewById(R.id.singer_recycler_view);
        adapter = new AlbumAdapter(getContext(),albumInfoList);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setAdapter(adapter);
        adapter.setOnItemClickListener(new AlbumAdapter.OnItemClickListener() {
            @Override
            public void onContentClick(View content, int position) {
                Intent intent = new Intent(mContext,ModelActivity.class);
                intent.putExtra(ModelActivity.KEY_TITLE,albumInfoList.get(position).getName());
                intent.putExtra(ModelActivity.KEY_TYPE, ModelActivity.ALBUM_TYPE);
                mContext.startActivity(intent);
            }
        });
        return view;
    }


    @Override
    public void onResume() {
        super.onResume();
        albumInfoList.clear();
        albumInfoList.addAll(MyMusicUtil.groupByAlbum((ArrayList)dbManager.getAllMusicFromMusicTable()));
        adapter.notifyDataSetChanged();
    }
}

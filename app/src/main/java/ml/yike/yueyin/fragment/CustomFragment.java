package ml.yike.yueyin.fragment;

import android.os.Bundle;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import ml.yike.yueyin.R;



public class CustomFragment extends Fragment {

    private TextView textView;
    private String title;

    public static CustomFragment newInstance(String title) {
        CustomFragment newFragment = new CustomFragment();
        Bundle bundle = new Bundle();
        bundle.putString("title", title);
        newFragment.setArguments(bundle);
        return newFragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle args = getArguments();
        if (args != null) {
            title = args.getString("title");
        }
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view =  inflater.inflate(R.layout.fragment_custom, container, false);
        textView = (TextView)view.findViewById(R.id.custom_fragment_tv);
        textView.setText(title);
        return view;
    }
}

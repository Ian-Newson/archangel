package iannewson.com.archangel.fragments;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.widget.Toast;

public class BaseFragment extends Fragment {

    public void toast(String message) {
        Context context = getContext();
        if (null == context) return;
        Toast.makeText(context, message, Toast.LENGTH_LONG).show();
    }

}

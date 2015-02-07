package psobolik.dockclock;

import android.app.Activity;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;


public class AboutDialogFragment extends DialogFragment {
    public static AboutDialogFragment newInstance() {
        return new AboutDialogFragment();
    }

    public AboutDialogFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.about_dialog, container, false);
        TextView textView = (TextView) view.findViewById(R.id.versionTextView);
        textView.setText(getVersionString());
        return view;
    }

    private String getVersionString() {
        String result = "";
        try {
            Activity activity = getActivity();
            PackageInfo packageInfo = activity.getPackageManager().getPackageInfo(activity.getPackageName(), 0);
            result = String.format("Version %s", packageInfo.versionName);
        } catch (PackageManager.NameNotFoundException e) {
            // Do nothing; return default
        }
        return result;
    }

}

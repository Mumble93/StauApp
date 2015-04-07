package com.sta.dhbw.stauapp;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;

public class GpsAlertDialogFragment extends DialogFragment
{
    public static GpsAlertDialogFragment newInstance(int title)
    {
        GpsAlertDialogFragment frag = new GpsAlertDialogFragment();
        Bundle args = new Bundle();
        args.putInt("title", title);
        frag.setArguments(args);
        return frag;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState)
    {
        int title = getArguments().getInt("title");

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(title)
                .setCancelable(false)
                .setMessage(R.string.gps_alert_message)
                .setNegativeButton(R.string.dialog_close, new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which)
                    {
                        dialog.dismiss();
                        getActivity().finish();
                    }
                });
        return builder.create();
    }
}

package com.sta.dhbw.stauapp;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;

import com.sta.dhbw.stauapp.Utils.ConnectionIssue;

public class AlertDialogFragment extends DialogFragment
{
    public static AlertDialogFragment newInstance(ConnectionIssue issue, boolean finishOnConfirm)
    {
        AlertDialogFragment frag = new AlertDialogFragment();
        Bundle args = new Bundle();
        args.putSerializable("issue", issue);
        args.putBoolean("finishOnConfirm", finishOnConfirm);
        frag.setArguments(args);
        return frag;
    }

    public static AlertDialogFragment newInstance(ConnectionIssue issue)
    {
        return newInstance(issue, true);
    }

    @Override
    @NonNull
    public Dialog onCreateDialog(Bundle savedInstanceState)
    {
        ConnectionIssue issue = (ConnectionIssue) getArguments().getSerializable("issue");
        final boolean finishOnConfirm = getArguments().getBoolean("finishOnConfirm");
        int title, message;

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setCancelable(false)
                .setNegativeButton(R.string.dialog_close, new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which)
                    {
                        dialog.dismiss();
                        if (finishOnConfirm)
                        {
                            getActivity().finish();
                        }
                    }
                });

        switch (issue)
        {
            case GPS_NOT_AVAILABLE:
                title = R.string.gps_alert_dialog_title;
                message = R.string.gps_alert_message;
                break;
            case NETWORTK_NOT_AVAILABLE:
                title = R.string.network_alert_dialog_title;
                message = R.string.network_alert_message;
                break;
            case SERVER_NOT_AVAILABLE:
                title = R.string.server_unavailable_dialog_title;
                message = R.string.server_unavailable_message;
                break;
            default:
                title = 0;
                message = 0;
        }

        builder.setTitle(title)
                .setMessage(message);
        AlertDialog dialog = builder.create();
        dialog.setCanceledOnTouchOutside(false);

        return dialog;
    }
}

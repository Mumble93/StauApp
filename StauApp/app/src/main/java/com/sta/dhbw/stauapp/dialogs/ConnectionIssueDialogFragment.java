package com.sta.dhbw.stauapp.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;

import com.sta.dhbw.stauapp.R;
import com.sta.dhbw.stauapp.util.Utils.ConnectionIssue;

public class ConnectionIssueDialogFragment extends DialogFragment
{
    public static ConnectionIssueDialogFragment newInstance(ConnectionIssue issue, boolean finishOnConfirm)
    {
        ConnectionIssueDialogFragment frag = new ConnectionIssueDialogFragment();
        Bundle args = new Bundle();
        args.putSerializable("issue", issue);
        args.putBoolean("finishOnConfirm", finishOnConfirm);
        frag.setArguments(args);
        return frag;
    }

    public static ConnectionIssueDialogFragment newInstance(ConnectionIssue issue)
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
                            ConnectionIssueDialogFragment.this.getActivity().finish();
                        }
                    }
                });

        switch (issue)
        {
            case GPS_NOT_AVAILABLE:
                title = R.string.gps_alert_dialog_title;
                message = R.string.gps_alert_message;
                break;
            case NETWORK_NOT_AVAILABLE:
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

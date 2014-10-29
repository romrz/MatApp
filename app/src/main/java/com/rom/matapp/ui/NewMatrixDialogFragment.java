package com.rom.matapp.ui;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;

import com.rom.matapp.R;
import com.rom.matapp.WorkSpaceActivity;

/**
 * Created by rom on 30/08/14.
 */
public class NewMatrixDialogFragment extends DialogFragment {

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        LayoutInflater inflater = getActivity().getLayoutInflater();

        builder.setView(inflater.inflate(R.layout.new_matrix_dialog_layout, null));

        builder.setTitle(R.string.new_matrix_title)
            .setPositiveButton(R.string.create, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int id) {

                    Dialog dialog = NewMatrixDialogFragment.this.getDialog();

                    int rows = ((MyNumberPicker) dialog.findViewById(R.id.rows)).getValue();
                    int cols = ((MyNumberPicker) dialog.findViewById(R.id.cols)).getValue();

                    ((WorkSpaceActivity) getActivity()).addMatrix(rows, cols);

                }
            })
            .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int id) {

                }
            });

        return builder.create();
    }

}

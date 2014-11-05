package com.rom.matapp.ui;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import com.rom.matapp.R;
import com.rom.matapp.WorkSpaceActivity;

/**
 * Created by rom on 5/11/14.
 */
public class ChangeMatrixNameDialogFragment extends DialogFragment {

    public Dialog onCreateDialog(Bundle saveInstanceState) {

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        LayoutInflater inflater = getActivity().getLayoutInflater();

        View view = inflater.inflate(R.layout.change_matrix_name_dialog, null);

        ((EditText) view.findViewById(R.id.matrix_name))
                .setText(((WorkSpaceActivity) getActivity()).getFocusedMatrix().getName());

        builder.setView(view)
                .setPositiveButton("Change", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int which) {

                        Dialog dialog = getDialog();

                        String name = ((EditText) dialog.findViewById(R.id.matrix_name)).getText().toString();

                        MatrixView matrixView = ((WorkSpaceActivity) getActivity()).getFocusedMatrix();

                        matrixView.setName(name);
                        matrixView.toggleContracted();
                        matrixView.toggleContracted();

                        matrixView = null;


                    }
                })
                .setNegativeButton("Cancel", new  DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int which) {

                    }
                });


        return builder.create();
    }

}

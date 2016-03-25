package oneonefour.robertking.map;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;


/**
 * Created by Robert on 22/03/2016.
 */
public class ChangeNameFragment extends DialogFragment{
    private LoginActivity activity;
    private String tempName;
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        activity = (LoginActivity) getActivity();
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        builder.setTitle("Change name");
        final View myView  = inflater.inflate(R.layout.change_name_layout, null);
        builder.setView(myView);
        TextView tx = (TextView)myView.findViewById(R.id.change_name_text);
        tx.setText("Your current name is " + activity.getUsername());
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //Change Username;
                EditText et = (EditText) myView.findViewById(R.id.change_name_et);
                if (!et.getText().toString().matches("")) {
                    tempName = et.getText().toString();
                    activity.setUsername(tempName);
                    SharedPreferences pref = activity.getPreferences(Context.MODE_PRIVATE);
                    SharedPreferences.Editor edit = pref.edit();
                    edit.putString(getString(R.string.preference_key_name),tempName);
                    edit.apply();
                } else {
                    Toast.makeText(getActivity().getApplicationContext(), "Please enter a username", Toast.LENGTH_SHORT).show();
                    new ChangeNameFragment().show(getFragmentManager(), "ChangeNameFrag");
                }
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                ChangeNameFragment.this.getDialog().cancel();
            }
        });

        return builder.create();
    }
}

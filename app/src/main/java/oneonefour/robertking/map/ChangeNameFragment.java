package oneonefour.robertking.map;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

/**
 * Created by Robert on 22/03/2016.
 */
public class ChangeNameFragment extends DialogFragment implements Response.Listener<String> , Response.ErrorListener{
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
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //Change Username;
                EditText et = (EditText) myView.findViewById(R.id.change_name_et);
                if (!et.getText().toString().matches("")) {
                    tempName = et.getText().toString();
                    final String url = "http://86.149.141.247:8080/MapGame/update_name.php?name="+activity.getUsername()+ "&newName="+tempName;
                    StringRequest changeUserName = new StringRequest(Request.Method.GET, url,ChangeNameFragment.this,ChangeNameFragment.this);
                    RequestSingleton.getInstance(ChangeNameFragment.this.getActivity().getApplicationContext()).addToRequestQueue(changeUserName);
                } else {
                    Toast.makeText(getActivity().getApplicationContext(),"Please enter a username",Toast.LENGTH_SHORT).show();
                    new ChangeNameFragment().show(getFragmentManager(),"ChangeNameFrag");
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

    @Override
    public void onErrorResponse(VolleyError error) {
        if(error == null) return;
        Toast.makeText(activity.getApplicationContext(),"An error happened, along the lines of: " + error.getMessage(),Toast.LENGTH_LONG).show();
    }

    @Override
    public void onResponse(String response) {
        activity.setUsername(tempName);
        SharedPreferences sharedPreferences = this.getActivity().getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor edit = sharedPreferences.edit();
        edit.putString(getString(R.string.preference_key_name), tempName);
        edit.commit();
        Log.d("ChangeName", response);
        Toast.makeText(activity.getApplicationContext(),"Name Changed Succesfully",Toast.LENGTH_LONG).show();
    }
}

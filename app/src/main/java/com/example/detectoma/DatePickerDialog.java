package com.example.detectoma;

import android.app.Dialog;
import android.os.Bundle;
import android.widget.DatePicker;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentActivity;

import java.util.Calendar;

public class DatePickerDialog extends DialogFragment
        implements android.app.DatePickerDialog.OnDateSetListener {


    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {

        //get current date and set it as default
        final Calendar c = Calendar.getInstance();

        int year = c.get(Calendar.YEAR);
        int month= c.get(Calendar.MONTH);
        int day=c.get(Calendar.DAY_OF_MONTH);

        return new android.app.DatePickerDialog(getActivity(),this, year, month,day);


    }

    @Override
    public void onDateSet(DatePicker datePicker, int year, int month, int dayOfMOnth) {
        //do something with the date chosen by user
        Toast.makeText(getActivity(), "Day: "+dayOfMOnth + "\nMonth: "+month+ "\nYear: "+year, Toast.LENGTH_SHORT).show();
    }
}
package com.zillion.dost;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.AppCompatEditText;

public class enterNumber extends AppCompatActivity {

    AppCompatEditText edtPhoneNumber;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_enter_number);

        edtPhoneNumber = (AppCompatEditText) findViewById(R.id.phone_number_edt);

        edtPhoneNumber.requestFocus();
    }
}

package com.group_7.mhd.driver;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.group_7.mhd.driver.Common.Common;
import com.group_7.mhd.driver.Model.Shipper;
import com.group_7.mhd.driver.Model.User;
import com.rengwuxian.materialedittext.MaterialEditText;

import java.util.Locale;
import java.util.regex.Pattern;

import io.paperdb.Paper;

public class SignIn extends AppCompatActivity {

    private static final Pattern PASSWORD_PATTERN =
            Pattern.compile("^"+
                    "(?=.*[a-z])" +     //at least one lowercase
                    "(?=.*[A-Z])" +     //at least one upercase
                    "(?=.*[0-9])" +     //at least one digit
                    "(?=.*[@#$%^&+=])" +    //at least one special character
                    "(?=\\S+$)" +          //no white space
                    ".{6,}" +               //at least six digit
                    "$");

    EditText editPhone, editPassword;
    Button buttonSignIn;

    FirebaseDatabase db;
    DatabaseReference shipper;

    TextView txtForgetPwd,txtlang;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);

        editPhone = findViewById(R.id.edit_Phone);
        editPassword = findViewById(R.id.edit_Password);
        buttonSignIn = findViewById(R.id.btn_signIn);

        txtForgetPwd = (TextView) findViewById(R.id.txtForgetPwd);
        txtlang = (TextView) findViewById(R.id.txtLanguage);

        //Init Firebase
        db = FirebaseDatabase.getInstance();
        shipper = db.getReference(Common.SHIPPERS_TABLE);

        //init paper
        Paper.init(this);

        txtForgetPwd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showForgetPwdDialog();
            }
        });

        buttonSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (validate())
                {
                    if (Common.isConnectedToInternet(getBaseContext())) {
                        signInUser(editPhone.getText().toString(), editPassword.getText().toString());
                    }else {
                        Toast.makeText(SignIn.this, R.string.check, Toast.LENGTH_SHORT).show();
                        return;
                    }
                }
            }
        });
        txtlang.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showChangeLanguageDialog();
            }
        });
    }


    //signInUser() method
    private void signInUser(String phone, final String password) {
        final ProgressDialog mDialog = new ProgressDialog(SignIn.this);
        mDialog.setMessage("Please Wait...");
        mDialog.show();

        int phoneLength = (phone).length();
        String phoneformat = phone;
        if (phoneLength==10){
            phoneformat = (phone).substring(1);
        }else if (phoneLength==13){
            phoneformat = (phone).substring(4);
        }else if (phoneLength==14){
            phoneformat = (phone).substring(5);
        }

        final String ph = phoneformat;
        //phone and password from user editText field
        shipper.child(phoneformat)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if(dataSnapshot.exists()){

                            Paper.book().write(Common.USER_KEY,ph);
                            Paper.book().write(Common.PWD_KEY,password);

                            mDialog.dismiss();
                            Shipper shipper = dataSnapshot.getValue(Shipper.class);
                            if(shipper.getPassword().equals(password)){
                                startActivity(new Intent(SignIn.this,HomeActivity.class));
                                Common.currentShipper = shipper;
                                finish();
                            }
                            else{
                                mDialog.dismiss();
                                Toast.makeText(SignIn.this, R.string.wropass, Toast.LENGTH_SHORT).show();
                            }
                        }
                        else{
                            mDialog.dismiss();
                            Toast.makeText(SignIn.this, R.string.userd, Toast.LENGTH_SHORT).show();
                        }

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
    }

    private boolean validate() {

        boolean valid = true;
        String phone_validate = editPhone.getText().toString().trim();
        String password_validate = editPassword.getText().toString().trim();

        int phone_length=13;
        if (phone_validate.startsWith("9")){
            phone_length=9;
        }
        if (phone_validate.startsWith("09")){
            phone_length=10;
        }
        if (phone_validate.startsWith("002519")){
            phone_length=14;
        }
        if (phone_validate.isEmpty()||!(phone_validate.startsWith("9")||phone_validate.startsWith("09")||(phone_validate.startsWith("+2519"))||(phone_validate.startsWith("002519")))||!(phone_length==phone_validate.length())) {
            editPhone.setError(getString(R.string.err_tel));
            valid = false;
        }

        if (password_validate.isEmpty()||!PASSWORD_PATTERN.matcher(password_validate).matches()){
            editPassword.setError(getString(R.string.err_password));
            valid = false;
        }
        /*if (password_validate.isEmpty()||!Patterns.EMAIL_ADDRESS.matcher(password_validate).matches()){

        }*/

        return valid;
    }

    private void showForgetPwdDialog() {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Forget Password");
        builder.setMessage("Enter your secure code");

        LayoutInflater inflater = this.getLayoutInflater();
        View forget_view = inflater.inflate(R.layout.forget_password_layout,null);

        builder.setView(forget_view);
        builder.setIcon(R.drawable.ic_security_black_24dp);

        final MaterialEditText editPhone = (MaterialEditText) forget_view.findViewById(R.id.edit_Phone);
        final MaterialEditText editSecureCode = (MaterialEditText) forget_view.findViewById(R.id.edit_SecureCode);


        //create Dialog and show
        final AlertDialog dialog = builder.create();
        dialog.show();

        //Get AlertDialog from dialog
        final AlertDialog diagview = ((AlertDialog) dialog);
        Button ok = (Button) diagview.findViewById(R.id.ok);
        Button cancel = (Button) diagview.findViewById(R.id.cancel);


        ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (validatee()){
                    shipper.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            dialog.dismiss();

                            int phoneLength = editPhone.getText().toString().length();
                            String phoneformat = editPhone.getText().toString();
                            if (phoneLength==10){
                                phoneformat = editPhone.getText().toString().substring(1);
                            }else if (phoneLength==13){
                                phoneformat = editPhone.getText().toString().substring(4);
                            }else if (phoneLength==14){
                                phoneformat = editPhone.getText().toString().substring(5);
                            }

                            Shipper driver = dataSnapshot.child(phoneformat).getValue(Shipper.class);
                            if (driver.getSecureCode().equals(editSecureCode.getText().toString()))
                                Toast.makeText(SignIn.this, "Your Password : "+driver.getPassword(),Toast.LENGTH_LONG).show();
                            else
                                Toast.makeText(SignIn.this,"Wrong Secuer code!", Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                            dialog.dismiss();
                        }
                    });
                }
            }

            private boolean validatee() {
                boolean valid = true;
                String phone_validate = editPhone.getText().toString().trim();
                String password_validate = editSecureCode.getText().toString().trim();

                int phone_length=13;
                if (phone_validate.startsWith("9")){
                    phone_length=9;
                }
                if (phone_validate.startsWith("09")){
                    phone_length=10;
                }
                if (phone_validate.startsWith("002519")){
                    phone_length=14;
                }
                if (phone_validate.isEmpty()||!(phone_validate.startsWith("9")||phone_validate.startsWith("09")||(phone_validate.startsWith("+2519"))||(phone_validate.startsWith("002519")))||!(phone_length==phone_validate.length())) {
                    editPhone.setError(getString(R.string.err_tel));
                    valid = false;
                }

                if (password_validate.isEmpty()||!PASSWORD_PATTERN.matcher(password_validate).matches()){
                    editSecureCode.setError(getString(R.string.err_password));
                    valid = false;
                }
        /*if (password_validate.isEmpty()||!Patterns.EMAIL_ADDRESS.matcher(password_validate).matches()){

        }*/

                return valid;
            }
        });
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
    }
    //langs
    private void showChangeLanguageDialog() {
        final String[] listItems = {"English","አማርኛ"};
        android.support.v7.app.AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(SignIn.this);
        builder.setTitle(R.string.choosel);
        builder.setSingleChoiceItems(listItems, -1, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if(i==0){
                    setLocale("en");
                    recreate();
                }
                else if(i==1){
                    setLocale("am");
                    recreate();
                }
                dialogInterface.dismiss();
            }
        });
        android.support.v7.app.AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }
    private void setLocale(String langs) {
        Locale locale = new Locale(langs);
        locale.setDefault(locale);
        Configuration config = new Configuration();
        config.locale = locale;
        getBaseContext().getResources().updateConfiguration(config,getBaseContext().getResources().getDisplayMetrics());

        SharedPreferences.Editor editor = getSharedPreferences("Settings",MODE_PRIVATE).edit();
        editor.putString("My_Lang",langs);
        editor.apply();
    }
    public void loadLocale(){
        SharedPreferences prefs = getSharedPreferences("Settings", Activity.MODE_PRIVATE);
        String language = prefs.getString("My_Lang","");
        setLocale(language);
    }
}

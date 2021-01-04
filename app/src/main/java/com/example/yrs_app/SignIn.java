package com.example.yrs_app;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.yrs_app.Common.Common;
import com.example.yrs_app.Model.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.rengwuxian.materialedittext.MaterialEditText;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import es.dmoral.toasty.Toasty;
import io.paperdb.Paper;
import uk.co.chrisjenx.calligraphy.CalligraphyConfig;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class SignIn extends AppCompatActivity {

    EditText edtPhone, edtPassword;
    Button btnSignIn;
    CheckBox ckbRemember;
    TextView txtTitle,txtForgotPwd, start_goToRegister, end_goToRegister;

    FirebaseDatabase database;
    DatabaseReference table_user;

    FirebaseFirestore db = FirebaseFirestore.getInstance();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().getDecorView().setLayoutDirection(View.LAYOUT_DIRECTION_LTR);


        setContentView(R.layout.activity_sign_in);

        checkDocumentExistence();


        txtTitle = (TextView) findViewById(R.id.txtTitleSignIn);
        edtPhone = (EditText) findViewById(R.id.edtPhone);
        edtPassword = (EditText) findViewById(R.id.edtPassword);

        btnSignIn = (Button) findViewById(R.id.btnSignIn1);
        ckbRemember = (CheckBox) findViewById(R.id.ckbRemember);

        txtForgotPwd = (TextView) findViewById(R.id.txtForgotPwd);

        start_goToRegister = (TextView) findViewById(R.id.start_goToRegister);
        end_goToRegister = (TextView) findViewById(R.id.end_goToRegister);

        // Init Paper
        Paper.init(this);

        //Init Firebase
        database = FirebaseDatabase.getInstance();
        table_user = database.getReference("User");

        txtForgotPwd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showForgotPwdDialog();
            }
        });

        end_goToRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent goToRegister = new Intent(SignIn.this, SignUp.class);
                startActivity(goToRegister);
            }
        });

        btnSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (Common.isConnectedToInternet(getBaseContext())) {

                    // Save User & Password
                    if(ckbRemember.isChecked())
                    {
                        Paper.book().write(Common.USER_KEY,edtPhone.getText().toString());
                        Paper.book().write(Common.PWD_KEY,edtPassword.getText().toString());

                    }

                    if(edtPhone.getText().toString().isEmpty() || edtPassword.getText().toString().isEmpty())
                    {
                        Toasty.error(SignIn.this, "Error! One of the fields is empty!", Toast.LENGTH_SHORT).show();
                    }
                    else
                    {
                        final ProgressDialog mDialog = new ProgressDialog(SignIn.this);
                        mDialog.setMessage("Loading...");
                        mDialog.show();

                        table_user.addListenerForSingleValueEvent(new ValueEventListener() {

                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {

                                //Check if user not exist in database
                                if (dataSnapshot.child(edtPhone.getText().toString()).exists()) {
                                    // Get User information
                                    mDialog.dismiss();
                                    User user = dataSnapshot.child(edtPhone.getText().toString()).getValue(User.class);
                                    user.setPhone(edtPhone.getText().toString());//set Phone
                                    if (user.getPassword().equals(edtPassword.getText().toString())) {
                                        //Toast.makeText(SignIn.this, "Sign In Successfully", Toast.LENGTH_SHORT).show();
                                        Intent homeIntent = new Intent(SignIn.this, Home.class);
                                        Common.currentUser = user;
                                        statisticsAddLogin();// סטטיסטיקה
                                        startActivity(homeIntent);
                                        finish();

                                        table_user.removeEventListener(this);
                                    } else {
                                        Toasty.error(SignIn.this, "Error! you've entered Wrong Password", Toast.LENGTH_SHORT).show();
                                    }
                                } else {
                                    mDialog.dismiss();
                                    Toasty.error(SignIn.this, "Error! User does not exist in the database!", Toast.LENGTH_SHORT).show();
                                }
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });
                    }


                }
                else
                {
                    Toasty.info(SignIn.this, "Please check your connection!!", Toast.LENGTH_SHORT).show();
                    return;
                }
            }

        });
    }

    private void showForgotPwdDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Forgot Password");
        builder.setMessage("Enter your Phone number and Email!");

        LayoutInflater inflater = this.getLayoutInflater();
        View forgot_view = inflater.inflate(R.layout.forgot_password_layout,null);

        builder.setView(forgot_view);
        builder.setIcon(R.drawable.ic_baseline_security_24);

        final MaterialEditText edtPhone = (MaterialEditText) forgot_view.findViewById(R.id.edtPhone);
        final MaterialEditText edtSecureCode = (MaterialEditText) forgot_view.findViewById(R.id.edtSecureCode);


        builder.setPositiveButton("YES", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //Check if user available
                table_user.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        User user = snapshot.child(edtPhone.getText().toString())
                                .getValue(User.class);
                        if(user.getSecureCode().equals(edtSecureCode.getText().toString()))
                            Toasty.success(SignIn.this, "Your password is: "+user.getPassword(), Toast.LENGTH_LONG).show();
                        else {
                            Toasty.error(SignIn.this, "Error! Wrong Phone number Or Email Address!!", Toast.LENGTH_SHORT).show();
                            showForgotPwdDialog();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
            }
        });

        builder.setNegativeButton("NO", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });

        builder.show();

    }


    private void statisticsAddLogin(){

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd.MM.yyyy");
        String today = simpleDateFormat.format(Calendar.getInstance().getTime());

        db.collection("STATISTICS").document("users").collection("userWeekly").document(today).update("logins", FieldValue.increment(+1))
                .addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful())
                    Log.d("myTag","operation was made");
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

            }
        });


        simpleDateFormat = new SimpleDateFormat("MM.yyyy");
        String thisMonth = simpleDateFormat.format(Calendar.getInstance().getTime());

        db.collection("STATISTICS").document("users").collection("userMonthly").document(thisMonth).update("logins", FieldValue.increment(+1))
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful())
                            Log.d("myTag","operation was made");
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

            }
        });


    }

    // בודק אם התיקיות של התאריכים של היום והחודש קיימים בבסיס הנתונים , אם לא יוצר אותם בפונקצייה statisticsAddLogin
    private void checkDocumentExistence(){

        //create todays date
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd.MM.yyyy");
        final String today = simpleDateFormat.format(Calendar.getInstance().getTime());


        //check for todays date existnce in firebease , if there isnt one create one with default values
        db.collection("STATISTICS").document("users").collection("userWeekly").document(today)
                .get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if(task.isSuccessful()) {
                            DocumentSnapshot documentSnapshot = task.getResult();
                            if(!documentSnapshot.exists()){

                                Map<String, Object> userActivity = new HashMap<>();
                                userActivity.put("logins", 1);
                                userActivity.put("registered", 0);

                                db.collection("STATISTICS").document("users").collection("userWeekly").document(today).set(userActivity);
                            }
                        }
                    }
                });

        //create this months date
        simpleDateFormat = new SimpleDateFormat("MM.yyyy");
        final String thisMonth = simpleDateFormat.format(Calendar.getInstance().getTime());

        //check for todays date existnce in firebease , if there isnt one create one with default values
        db.collection("STATISTICS").document("users").collection("userMonthly").document(thisMonth)
                .get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if(task.isSuccessful()) {
                            DocumentSnapshot documentSnapshot = task.getResult();
                            if(!documentSnapshot.exists()){

                                Map<String, Object> userActivity = new HashMap<>();
                                userActivity.put("logins", 1);
                                userActivity.put("registered", 0);

                                db.collection("STATISTICS").document("users").collection("userMonthly").document(thisMonth).set(userActivity);
                            }
                        }
                    }
                });

    }






}

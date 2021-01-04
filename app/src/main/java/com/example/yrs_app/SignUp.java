package com.example.yrs_app;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
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
import com.rengwuxian.materialedittext.MaterialEditText;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import es.dmoral.toasty.Toasty;
import uk.co.chrisjenx.calligraphy.CalligraphyConfig;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class SignUp extends AppCompatActivity {

    TextView txtTitle, start_goToSignIn, end_goToSignIn;
    EditText edtPhone,edtName,edtPassword,edtSecureCode;
    CheckBox ckbTerms;
    Button btnSignUp;
    FirebaseFirestore db = FirebaseFirestore.getInstance();



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().getDecorView().setLayoutDirection(View.LAYOUT_DIRECTION_LTR);

        checkDocumentExistence();

        setContentView(R.layout.activity_sign_up);

        txtTitle = (TextView) findViewById(R.id.txtTitleSignUp);
        start_goToSignIn = (TextView) findViewById(R.id.start_goToSignIn);
        end_goToSignIn = (TextView) findViewById(R.id.end_goToSignIn);

        edtName=(EditText) findViewById(R.id.edtName);
        edtPassword=(EditText) findViewById(R.id.edtPassword);
        edtPhone=(EditText) findViewById(R.id.edtPhone);
        edtSecureCode=(EditText) findViewById(R.id.edtSecureCode);

        ckbTerms = (CheckBox) findViewById(R.id.ckbTerms);

        btnSignUp=(Button) findViewById(R.id.btnSignUp);

        end_goToSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent goToRegister = new Intent(SignUp.this, SignIn.class);
                startActivity(goToRegister);
            }
        });


        //Init Firebase
        final FirebaseDatabase database = FirebaseDatabase.getInstance();
        final DatabaseReference table_user = database.getReference("User");

        btnSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Common.isConnectedToInternet(getBaseContext())) {

                    if(edtSecureCode.getText().toString().isEmpty() || edtPhone.getText().toString().isEmpty() ||
                            edtName.getText().toString().isEmpty() || edtPassword.getText().toString().isEmpty())
                    {
                        Toasty.error(SignUp.this, "Error! One of the fields is empty!", Toast.LENGTH_SHORT).show();
                    }
                    else
                    {
                        final ProgressDialog mDialog = new ProgressDialog(SignUp.this);
                        mDialog.setMessage("Loading...");
                        mDialog.show();

                        table_user.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {

                                if (ckbTerms.isChecked()){

                                    // Check if User's Phone already exists
                                    if (dataSnapshot.child(edtPhone.getText().toString()).exists()) {
                                        mDialog.dismiss();
                                        Toasty.error(SignUp.this, "This phone number has already registered", Toast.LENGTH_SHORT).show();
                                    }

                                    else {
                                        mDialog.dismiss();
                                        User user = new User(
                                                edtName.getText().toString(),
                                                edtPassword.getText().toString(),
                                                edtSecureCode.getText().toString()
                                        );
                                        table_user.child(edtPhone.getText().toString()).setValue(user);
                                        Toasty.success(SignUp.this, "Sign up successfully!!", Toast.LENGTH_SHORT).show();

                                        Intent homeIntent = new Intent(SignUp.this, Home.class);
                                        Common.currentUser = user;

                                        //להשיג מידע סטטיסטי של הרשמה
                                        statisticsAddRegisteredUsers();

                                        startActivity(homeIntent);
                                        finish();// אם תהיה בעיה בעתיד עם ההרשמות פשוט להשאיר רק את שורה זו

                                        table_user.removeEventListener(this);

                                    }

                                }
                                else
                                {
                                    mDialog.dismiss();
                                    Toasty.warning(SignUp.this, "You must accept all the Terms!", Toast.LENGTH_SHORT).show();
                                }

                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });
                    }
                }
                else
                {
                    Toasty.info(SignUp.this, "Please check your connection!!", Toast.LENGTH_SHORT).show();
                    return;
                }


            }
        });



    }

    private void statisticsAddRegisteredUsers(){

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd.MM.yyyy");
        String today = simpleDateFormat.format(Calendar.getInstance().getTime());

        db.collection("STATISTICS").document("users").collection("userWeekly").document(today).update("registered", FieldValue.increment(+1))
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

        db.collection("STATISTICS").document("users").collection("userMonthly").document(thisMonth).update("registered", FieldValue.increment(+1))
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

        db.collection("STATISTICS").document("users")
                .update("registeredOverall" , FieldValue.increment(+1))
                .addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {

            }
        });


    }

    // בודק אם התיקיות של התאריכים של היום והחודש קיימים בבסיס הנתונים , אם לא יוצר אותם בפונקצייה statisticsAddRegisteredUsers
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
                                userActivity.put("logins", 0);
                                userActivity.put("registered", 1);

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
                                userActivity.put("logins", 0);
                                userActivity.put("registered", 1);

                                db.collection("STATISTICS").document("users").collection("userMonthly").document(thisMonth).set(userActivity);
                            }
                        }
                    }
                });

    }


}

package com.pspdfkit.annotatedviewer;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.pspdfkit.datastructures.TextSelection;
import com.pspdfkit.ui.PdfActivity;
import com.pspdfkit.ui.special_mode.controller.TextSelectionController;
import com.pspdfkit.ui.special_mode.manager.TextSelectionManager;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.IOException;

import io.reactivex.annotations.NonNull;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MyActivity extends PdfActivity implements TextSelectionManager.OnTextSelectionChangeListener,
        TextSelectionManager.OnTextSelectionModeChangeListener{

    private Activity activity;
    private Context context;
    private static final String TAG = "MyActivity.TextSelection";
    private String url = "https://api.dictionaryapi.dev/api/v2/entries/en_US/";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.activity = this;
        this.context = this;
        getPdfFragment().addOnTextSelectionModeChangeListener(this);
        getPdfFragment().addOnTextSelectionChangeListener(this);
    }

    @Override
    public void onEnterTextSelectionMode(@NonNull TextSelectionController controller) {
        try {
            if(controller.getTextSelection().text.split(" ").length==1){
                displayBottomSheet(controller.getTextSelection().text);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onExitTextSelectionMode(@NonNull TextSelectionController controller) {
    }

    @Override
    public boolean onBeforeTextSelectionChange(@androidx.annotation.Nullable TextSelection textSelection, @androidx.annotation.Nullable TextSelection textSelection1) {
        return true;
    }

    @Override
    public void onAfterTextSelectionChange(@androidx.annotation.Nullable TextSelection textSelection, @androidx.annotation.Nullable TextSelection textSelection1) {
        try {
            if(textSelection.text.split(" ").length==1){
                displayBottomSheet(textSelection.text);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    void run(String word , TextView textView) throws IOException {
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(url+word)
                .build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                call.cancel();
            }
            @Override
            public void onResponse(Call call, Response response) throws IOException {

                final String myResponse = response.body().string();
                try {
                    JSONArray reader = new JSONArray(myResponse);
                    JSONArray jsonArray = reader.getJSONObject(0).getJSONArray("meanings");
                    if(jsonArray.length()>0){
                        JSONArray jsonArray1 = jsonArray.getJSONObject(0).getJSONArray("definitions");
                        String meaning = jsonArray1.getJSONObject(0).getString("definition");
                        Log.i("meanings  ",""+meaning);
                        MyActivity.this.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                textView.setText(meaning);
//                                Toast.makeText(MyActivity.this, ""+meaning, Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    MyActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(MyActivity.this, "Meaning not found", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        });
    }

    private void displayBottomSheet(String word) {
        final BottomSheetDialog bottomSheetTeachersDialog = new BottomSheetDialog(MyActivity.this, R.style.BottomSheetDialogTheme);
        View layout = LayoutInflater.from(MyActivity.this).inflate(R.layout.bottom_sheet_layout, null);
        bottomSheetTeachersDialog.setContentView(layout);
        bottomSheetTeachersDialog.setCancelable(false);
        bottomSheetTeachersDialog.setCanceledOnTouchOutside(true);
        bottomSheetTeachersDialog.show();
        TextView wordTxt = layout.findViewById(R.id.word);
        wordTxt.setText(word);
        TextView defination = layout.findViewById(R.id.defination);
        try {
            run(word, defination);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
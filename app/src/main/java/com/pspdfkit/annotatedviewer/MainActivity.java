/*
 *   Copyright © 2019-2021 PSPDFKit GmbH. All rights reserved.
 *
 *   The PSPDFKit Sample applications are licensed with a modified BSD license.
 *   Please see License for details. This notice may not be removed from this file.
 */
package com.pspdfkit.annotatedviewer;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.Toast;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.pspdfkit.PSPDFKit;
import com.pspdfkit.configuration.activity.PdfActivityConfiguration;
import com.pspdfkit.configuration.page.PageFitMode;
import com.pspdfkit.configuration.page.PageScrollDirection;
import com.pspdfkit.document.download.DownloadJob;
import com.pspdfkit.document.download.DownloadProgressFragment;
import com.pspdfkit.document.download.DownloadRequest;
import com.pspdfkit.ui.PdfActivityIntentBuilder;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;


public class MainActivity extends AppCompatActivity {

    private List<PDFModel> pdfList;
    private static final int REQUEST_OPEN_DOCUMENT = 1;
    private static final String DEMO_DOCUMENT_ASSET_NAME = "demo.pdf";
    private @Nullable
    Disposable documentExtraction;
    private PDFListAdapter pdfAdapter;
    FrameLayout demoFrame;
    private static final String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    @Override
    protected void onCreate(@Nullable final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        pdfList = new ArrayList<>();
        final FloatingActionButton openDocumentButton = findViewById(R.id.main_btn_open_document);
        openDocumentButton.setOnClickListener(v -> launchSystemFilePicker());
        //   final Button openDemoDocumentButton = findViewById(R.id.main_btn_open_example);
        //   openDemoDocumentButton.setOnClickListener(v -> prepareAndShowDemoDocument());
        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.listview);
        demoFrame = (FrameLayout) findViewById(R.id.demo_frame);

        pdfAdapter = new PDFListAdapter(MainActivity.this, pdfList, new PDFListAdapter.ClickListener() {
            @Override
            public void onClick(PDFModel pdfModel, View v) {
                prepareAndShowDocument(pdfModel.getName());
            }
        });

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(pdfAdapter);
        Log.e("size", String.valueOf(pdfList.size()));

    }

    @Override
    protected void onResume() {
        if (pdfList.size() > 0) {
            demoFrame.setVisibility(View.GONE);
        } else {
            demoFrame.setVisibility(View.VISIBLE);
        }
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        final Disposable runningDocumentExtraction = documentExtraction;
        if (runningDocumentExtraction != null) {
            runningDocumentExtraction.dispose();
            documentExtraction = null;
        }
    }

    private void prepareAndShowDocument(@NonNull final Uri uri) {
        this.addPDFModel(uri);
        if (PSPDFKit.isOpenableUri(this, uri)) {
            launchPdfActivity(uri);
        } else {
            final DownloadRequest request = new DownloadRequest.Builder(this)
                    .uri(uri)
                    .build();
            final DownloadJob job = DownloadJob.startDownload(request);
            final DownloadProgressFragment fragment = new DownloadProgressFragment();
            fragment.show(getSupportFragmentManager(), "download-fragment");
            fragment.setJob(job);
        }
    }

    public void addPDFModel(Uri uri) {
        boolean flag = true;
        for (PDFModel model : pdfList) {
            if (uri.toString().equalsIgnoreCase(model.getName().toString())) {
                flag = false;
            }
        }
        PDFModel model = new PDFModel(uri);
        if (flag) {
            pdfList.add(model);
        }
        pdfAdapter.notifyDataSetChanged();
    }


    private void prepareAndShowDemoDocument() {
        if (this.documentExtraction != null) return;
        this.documentExtraction = ExtractAssetTask.extractAsync(DEMO_DOCUMENT_ASSET_NAME, this)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe((documentFile) -> {
                    documentExtraction = null;
                    prepareAndShowDocument(Uri.fromFile(documentFile));
                });
    }

    private void launchPdfActivity(@NonNull final Uri uri) {

        final PdfActivityConfiguration pspdfkitConfiguration = new PdfActivityConfiguration.Builder(getApplicationContext())
                .scrollDirection(PageScrollDirection.HORIZONTAL)
                .showPageNumberOverlay()
                .showThumbnailGrid()
                .fitMode(PageFitMode.FIT_TO_WIDTH)
                .build();
        final Intent intent = PdfActivityIntentBuilder.fromUri(MainActivity.this, uri)
                .configuration(pspdfkitConfiguration)
                .activityClass(MyActivity.class)
                .build();
        startActivity(intent);
    }

    private void launchSystemFilePicker() {
        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    1);

        } else {
            final Intent openIntent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            openIntent.addCategory(Intent.CATEGORY_OPENABLE);
            openIntent.setType("application/*");
            startActivityForResult(openIntent, REQUEST_OPEN_DOCUMENT);

        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        Log.i("onRequestPermissions", permissions.toString());
        switch (requestCode) {
            case 1:
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    final Intent openIntent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                    openIntent.addCategory(Intent.CATEGORY_OPENABLE);
                    openIntent.setType("application/*");
                    startActivityForResult(openIntent, REQUEST_OPEN_DOCUMENT);
                } else {
                    Toast.makeText(MainActivity.this, "Sorry you need to accept the permissions.", Toast.LENGTH_SHORT);
                }
                return;
        }
    }



    @Override
    protected void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_OPEN_DOCUMENT && resultCode == Activity.RESULT_OK && data != null) {
            final Uri uri = data.getData();
            if (uri != null) {
                prepareAndShowDocument(uri);
            }
        }
    }
}

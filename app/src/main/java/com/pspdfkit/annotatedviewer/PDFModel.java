package com.pspdfkit.annotatedviewer;

import android.net.Uri;

public class PDFModel {
    private Uri name;

    public PDFModel() {
    }

    public PDFModel(Uri name) {
        this.name = name;
    }

    public Uri getName() {
        return name;
    }

    public void setName(Uri name) {
        this.name = name;
    }
}

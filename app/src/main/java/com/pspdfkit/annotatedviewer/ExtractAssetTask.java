/*
 *   Copyright © 2019-2021 PSPDFKit GmbH. All rights reserved.
 *
 *   The PSPDFKit Sample applications are licensed with a modified BSD license.
 *   Please see License for details. This notice may not be removed from this file.
 */

package com.pspdfkit.annotatedviewer;

import android.content.Context;

import androidx.annotation.NonNull;

import com.pspdfkit.document.download.DownloadJob;
import com.pspdfkit.document.download.DownloadRequest;
import com.pspdfkit.document.download.source.AssetDownloadSource;

import java.io.File;

import io.reactivex.Single;
import io.reactivex.schedulers.Schedulers;


@SuppressWarnings("SameParameterValue")
public final class ExtractAssetTask {

    @NonNull
    static Single<File> extractAsync(@NonNull final String assetPath,
                                     @NonNull final Context context) {
        return Single.<File>create((emitter) -> {
            final File  outputFile = new File(context.getFilesDir(), assetPath);
            final DownloadRequest request = new DownloadRequest.Builder(context)
                .source(new AssetDownloadSource(context, assetPath))
                .outputFile(outputFile)
                .overwriteExisting(false)
                .build();
            final DownloadJob job = DownloadJob.startDownload(request);
            job.setProgressListener(new DownloadJob.ProgressListenerAdapter() {
                @Override
                public void onComplete(@NonNull final File output) {
                    emitter.onSuccess(output);
                }

                @Override
                public void onError(@NonNull final Throwable exception) {
                    super.onError(exception);
                    emitter.tryOnError(exception);
                }
            });
            emitter.setCancellable(job::cancel);
        }).subscribeOn(Schedulers.io());
    }
}

/*
 * Copyright (C) 2011 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.android.providers.downloads;

import android.content.Context;
import android.drm.DrmManagerClient;
import android.drm.DrmRights;
import android.util.Log;

import java.io.IOException;
import java.io.File;

public class DownloadDrmHelper {

    /** The MIME type of special DRM files */
    public static final String MIMETYPE_DRM_MESSAGE = "application/vnd.oma.drm.message";
    public static final String MIMETYPE_DRM_CONTENT = "application/vnd.oma.drm.content";

    /** The extensions of special DRM files */
    public static final String EXTENSION_DRM_MESSAGE = ".dm";

    public static final String EXTENSION_INTERNAL_FWDL = ".fl";
    public static final String EXTENSION_INTERNAL_DRM = ".dcf";

    public static final String BUY_LICENSE = "android.drmservice.intent.action.BUY_LICENSE";

    /**
     * Checks if the Media Type is a DRM Media Type
     *
     * @param drmManagerClient A DrmManagerClient
     * @param mimetype Media Type to check
     * @return True if the Media Type is DRM else false
     */
    public static boolean isDrmMimeType(Context context, String mimetype) {
        boolean result = false;
        if (context != null) {
            try {
                DrmManagerClient drmClient = new DrmManagerClient(context);
                if (drmClient != null && mimetype != null && mimetype.length() > 0) {
                    result = drmClient.canHandle("", mimetype);
                    drmClient.release();
                }
            } catch (IllegalArgumentException e) {
                Log.w(Constants.TAG,
                        "DrmManagerClient instance could not be created, context is Illegal.");
            } catch (IllegalStateException e) {
                Log.w(Constants.TAG, "DrmManagerClient didn't initialize properly.");
            }
        }
        return result;
    }

    /**
     * Checks if the Media Type needs to be DRM converted
     *
     * @param mimetype Media type of the content
     * @return True if convert is needed else false
     */
    public static boolean isDrmConvertNeeded(String mimetype) {
        return MIMETYPE_DRM_MESSAGE.equals(mimetype) || MIMETYPE_DRM_CONTENT.equals(mimetype);
    }

    /**
     * Modifies the file extension for a DRM Forward Lock file NOTE: This
     * function shouldn't be called if the file shouldn't be DRM converted
     */
    public static String modifyDrmFileExtension(String filename, String mimeType) {
        if (filename != null) {
            int extensionIndex;
            filename = filename.replaceAll(" ", "_");
            extensionIndex = filename.lastIndexOf(".");
            if (extensionIndex != -1) {
                filename = filename.substring(0, extensionIndex);
            }
            if (MIMETYPE_DRM_MESSAGE.equals(mimeType)) {
                filename = filename.concat(EXTENSION_DRM_MESSAGE);
            } else if (MIMETYPE_DRM_CONTENT.equals(mimeType)) {
                filename = filename.concat(EXTENSION_INTERNAL_DRM);
            }
        }
        return filename;
    }

    /**
     * Return the original MIME type of the given file, using the DRM framework
     * if the file is protected content.
     */
    public static String getOriginalMimeType(Context context, File file, String currentMime) {
        final DrmManagerClient client = new DrmManagerClient(context);
        try {
            final String rawFile = file.toString();
            if (client.canHandle(rawFile, null)) {
                return client.getOriginalMimeType(rawFile);
            } else {
                return currentMime;
            }
        } finally {
            client.release();
        }
    }

    /**
     * installs the rights.
     *
     * @param context The context
     * @param path Path to the file
     * @param mimeType mimeType
     */
    public static void saveRights(Context context, String path, String mimeType) {
        path = path.replace("/storage/emulated/0", "/storage/emulated/legacy");
        DrmRights drmRights = new DrmRights(path, mimeType);
        DrmManagerClient drmClient = new DrmManagerClient(context);
        try {
            drmClient.saveRights(drmRights, path, null);
            drmClient.release();
        } catch (IOException ex) {
            Log.i("DownloadManager", "--Exception==" + ex.toString());
        }
    }
}

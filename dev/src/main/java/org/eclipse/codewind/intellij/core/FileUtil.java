/*******************************************************************************
 * Copyright (c) 2019 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.codewind.intellij.core;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.StringTokenizer;
import java.util.stream.Stream;

public class FileUtil {

    public static boolean makeDir(String path) {
        boolean result = true;

        if (path != null) {
            try {
                File fp = new File(path);
                if (!fp.exists() || !fp.isDirectory()) {
                    // Create the directory.
                    result = fp.mkdirs();
                }
            } catch (Exception e) {
                Logger.logWarning("Failed to create directory: " + path, e);
                result = false;
            }
        }
        return result;
    }

    public static void copyFile(InputStream inStream, String path) throws IOException, FileNotFoundException {
        FileOutputStream outStream = null;
        try {
            outStream = new FileOutputStream(path);
            byte[] bytes = new byte[1024];
            int bytesRead = 0;
            while ((bytesRead = inStream.read(bytes)) > 0) {
                outStream.write(bytes, 0, bytesRead);
            }
        } finally {
            if (outStream != null) {
                try {
                    outStream.close();
                } catch (IOException e) {
                    // Ignore
                }
            }
        }
    }

    /**
     * Removes the given directory.
     * If recursive is true, any files and subdirectories within
     * the directory will also be deleted; otherwise the
     * operation will fail if the directory is not empty.
     */
    public static void deleteDirectory(String dir, boolean recursive) throws IOException {
        if (dir == null || dir.length() <= 0) {
            return;
        }

        // Safety feature. Prevent to remove directory from the root
        // of the drive, i.e. directory with less than 2 file separator.
        if ((new StringTokenizer(dir.replace(File.separatorChar, '/'), "/")).countTokens() < 2) {
            return;
        }

        File fp = new File(dir);
        if (!fp.exists() || !fp.isDirectory())
            throw new IOException("Directory does not exist: " + fp.toString());

        if (recursive) {
            // Remove the contents of the given directory before delete.
            String[] fileList = fp.list();
            if (fileList != null) {
                String curBasePath = dir + File.separator;
                for (int i = 0; i < fileList.length; i++) {
                    // Remove each file one at a time.
                    File curFp = new File(curBasePath + fileList[i]);
                    if (curFp.exists()) {
                        if (curFp.isDirectory()) {
                            // Remove the directory and sub directories;
                            deleteDirectory(dir + File.separator + fileList[i], recursive);
                        } else {
                            if (!curFp.delete())
                                Logger.log("Could not delete " + curFp.getName());
                        }
                    }
                }
            }
        }
        boolean isSuccess = fp.delete();

        if (!isSuccess) {
            throw new IOException("Directory cannot be removed.");
        }
    }

    /**
     * Copy the contents of the given source directory into the given target directory
     *
     * @param source
     *            the directory to copy from
     * @param target
     *            the directory to copy into
     */
    public static void copyDirectory(Path source, Path target) throws IOException {
        if (!Files.exists(target)) {
            Files.createDirectories(target);
        }
        try (Stream<Path> files = Files.walk(source)) {
            files
                    .filter(file -> !file.equals(source))
                    .map(source::relativize)
                    .forEach(file -> copyFile(source.resolve(file), target.resolve(file)));
        }
    }

    private static void copyFile(Path source, Path target) {
        try {
            if (Files.isDirectory(source)) {
                Files.createDirectories(target);
                return;
            }
            Files.copy(source, target, StandardCopyOption.COPY_ATTRIBUTES);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}

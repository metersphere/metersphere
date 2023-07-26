/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to you under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.jmeter.services;

import io.metersphere.utils.LoggerUtil;
import org.apache.commons.io.input.BOMInputStream;
import org.apache.commons.lang3.StringUtils;
import org.apache.jmeter.gui.JMeterFileFilter;
import org.apache.jmeter.save.CSVSaveService;
import org.apache.jmeter.threads.JMeterContextService;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jorphan.util.JOrphanUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.file.Files;
import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

/**
 * This class provides thread-safe access to files, and to
 * provide some simplifying assumptions about where to find files and how to
 * name them. For instance, putting supporting files in the same directory as
 * the saved test plan file allows users to refer to the file with just it's
 * name - this FileServer class will find the file without a problem.
 * Eventually, I want all in-test file access to be done through here, with the
 * goal of packaging up entire test plans as a directory structure that can be
 * sent via rmi to remote servers (currently, one must make sure the remote
 * server has all support files in a relative-same location) and to package up
 * test plans to execute on unknown boxes that only have Java installed.
 */
public class FileServer {

    private static final Logger log = LoggerFactory.getLogger(FileServer.class);

    /**
     * The default base used for resolving relative files, i.e.<br/>
     * {@code System.getProperty("user.dir")}
     */
    private static final String DEFAULT_BASE = System.getProperty("user.dir");// $NON-NLS-1$

    /**
     * Default base prefix: {@value}
     */
    private static final String BASE_PREFIX_DEFAULT = "~/"; // $NON-NLS-1$

    private static final String BASE_PREFIX =
            JMeterUtils.getPropDefault("jmeter.save.saveservice.base_prefix", // $NON-NLS-1$
                    BASE_PREFIX_DEFAULT);

    private File base;

    private final Map<String, FileEntry> files = new HashMap<>();

    private static final FileServer server = new FileServer();

    // volatile needed to ensure safe publication
    private volatile String scriptName;

    // Cannot be instantiated
    private FileServer() {
        base = new File(DEFAULT_BASE);
        log.info("Default base='{}'", DEFAULT_BASE);
    }

    /**
     * @return the singleton instance of the server.
     */
    public static FileServer getFileServer() {
        return server;
    }

    /**
     * Resets the current base to DEFAULT_BASE.
     */
    public synchronized void resetBase() {
        checkForOpenFiles();
        base = new File(DEFAULT_BASE);
        log.info("Reset base to '{}'", base);
    }

    /**
     * Sets the current base directory for relative file names from the provided path.
     * If the path does not refer to an existing directory, then its parent is used.
     * Normally the provided path is a file, so using the parent directory is appropriate.
     *
     * @param basedir the path to set, or {@code null} if the GUI is being cleared
     * @throws IllegalStateException if files are still open
     */
    public synchronized void setBasedir(String basedir) {
        checkForOpenFiles(); // TODO should this be called if basedir == null?
        if (basedir != null) {
            File newBase = new File(basedir);
            if (!newBase.isDirectory()) {
                newBase = newBase.getParentFile();
            }
            base = newBase;
            log.info("Set new base='{}'", base);
        }
    }

    /**
     * Sets the current base directory for relative file names from the provided script file.
     * The parameter is assumed to be the path to a JMX file, so the base directory is derived
     * from its parent.
     *
     * @param scriptPath the path of the script file; must be not be {@code null}
     * @throws IllegalStateException    if files are still open
     * @throws IllegalArgumentException if scriptPath parameter is null
     */
    public synchronized void setBaseForScript(File scriptPath) {
        if (scriptPath == null) {
            throw new IllegalArgumentException("scriptPath must not be null");
        }
        setScriptName(scriptPath.getName());
        // getParentFile() may not work on relative paths
        setBase(scriptPath.getAbsoluteFile().getParentFile());
    }

    /**
     * Sets the current base directory for relative file names.
     *
     * @param jmxBase the path of the script file base directory, cannot be null
     * @throws IllegalStateException    if files are still open
     * @throws IllegalArgumentException if {@code basepath} is null
     */
    public synchronized void setBase(File jmxBase) {
        if (jmxBase == null) {
            throw new IllegalArgumentException("jmxBase must not be null");
        }
        checkForOpenFiles();
        base = jmxBase;
        log.info("Set new base='{}'", base);
    }

    /**
     * Check if there are entries in use.
     * <p>
     * Caller must ensure that access to the files map is single-threaded as
     * there is a window between checking the files Map and clearing it.
     *
     * @throws IllegalStateException if there are any entries still in use
     */
    private void checkForOpenFiles() throws IllegalStateException {
        if (filesOpen()) { // checks for entries in use
            throw new IllegalStateException("Files are still open, cannot change base directory");
        }
        files.clear(); // tidy up any unused entries
    }

    public synchronized String getBaseDir() {
        return base.getAbsolutePath();
    }

    public static String getDefaultBase() {
        return DEFAULT_BASE;
    }

    /**
     * Calculates the relative path from DEFAULT_BASE to the current base,
     * which must be the same as or a child of the default.
     *
     * @return the relative path, or {@code "."} if the path cannot be determined
     */
    public synchronized File getBaseDirRelative() {
        // Must first convert to absolute path names to ensure parents are available
        File parent = new File(DEFAULT_BASE).getAbsoluteFile();
        File f = base.getAbsoluteFile();
        ArrayDeque<String> l = new ArrayDeque<>();
        while (f != null) {
            if (f.equals(parent)) {
                if (l.isEmpty()) {
                    break;
                }
                File rel = new File(l.pop());
                while (!l.isEmpty()) {
                    rel = new File(rel, l.pop());
                }
                return rel;
            }
            l.push(f.getName());
            f = f.getParentFile();
        }
        return new File(".");
    }

    /**
     * Creates an association between a filename and a File inputOutputObject,
     * and stores it for later use - unless it is already stored.
     *
     * @param filename - relative (to base) or absolute file name (must not be null)
     */
    public void reserveFile(String filename) {
        reserveFile(filename, null);
    }

    /**
     * Creates an association between a filename and a File inputOutputObject,
     * and stores it for later use - unless it is already stored.
     *
     * @param filename    - relative (to base) or absolute file name (must not be null)
     * @param charsetName - the character set encoding to use for the file (may be null)
     */
    public void reserveFile(String filename, String charsetName) {
        reserveFile(filename, charsetName, filename, false);
    }

    /**
     * Creates an association between a filename and a File inputOutputObject,
     * and stores it for later use - unless it is already stored.
     *
     * @param filename    - relative (to base) or absolute file name (must not be null)
     * @param charsetName - the character set encoding to use for the file (may be null)
     * @param alias       - the name to be used to access the object (must not be null)
     */
    public void reserveFile(String filename, String charsetName, String alias) {
        reserveFile(filename, charsetName, alias, false);
    }

    /**
     * Creates an association between a filename and a File inputOutputObject,
     * and stores it for later use - unless it is already stored.
     *
     * @param filename    - relative (to base) or absolute file name (must not be null or empty)
     * @param charsetName - the character set encoding to use for the file (may be null)
     * @param alias       - the name to be used to access the object (must not be null)
     * @param hasHeader   true if the file has a header line describing the contents
     * @return the header line; may be null
     * @throws IllegalArgumentException if header could not be read or filename is null or empty
     */
    public synchronized String reserveFile(String filename, String charsetName, String alias, boolean hasHeader) {
        if (filename == null || filename.isEmpty()) {
            throw new IllegalArgumentException("Filename must not be null or empty");
        }
        if (alias == null) {
            throw new IllegalArgumentException("Alias must not be null");
        }
        // todo 这里更改了原始代码
        if(!new File(filename).exists()){
            log.error("file does not exist [ "+ filename+" ]");
            return "";
        }

        String threadName = JMeterContextService.getContext().getThread().getThreadName();
        if (!StringUtils.contains(alias, threadName)) {
            alias = StringUtils.join(threadName, alias);
        }
        FileEntry fileEntry = files.get(alias);
        if (fileEntry == null) {
            fileEntry = new FileEntry(resolveFileFromPath(filename), null, charsetName);
            if (filename.equals(alias)) {
                log.info("Stored: {}", filename);
            } else {
                log.info("Stored: {} Alias: {}", filename, alias);
            }
            files.put(alias, fileEntry);
            if (hasHeader) {
                try {
                    fileEntry.headerLine = readLine(alias, false);
                    if (fileEntry.headerLine == null) {
                        fileEntry.exception = new EOFException("File is empty: " + fileEntry.file);
                    }
                } catch (IOException | IllegalArgumentException e) {
                    fileEntry.exception = e;
                }
            }
        }
        if (hasHeader && fileEntry.headerLine == null) {
            throw new IllegalArgumentException("Could not read file header line for file " + filename,
                    fileEntry.exception);
        }
        return fileEntry.headerLine;
    }

    /**
     * Resolves file name into {@link File} instance.
     * When filename is not absolute and not found from current working dir,
     * it tries to find it under current base directory
     *
     * @param filename original file name
     * @return {@link File} instance
     */
    private File resolveFileFromPath(String filename) {
        File f = new File(filename);
        if (f.isAbsolute() || f.exists()) {
            return f;
        } else {
            return new File(base, filename);
        }
    }

    /**
     * Get the next line of the named file, recycle by default.
     *
     * @param filename the filename or alias that was used to reserve the file
     * @return String containing the next line in the file
     * @throws IOException when reading of the file fails, or the file was not reserved properly
     */
    public String readLine(String filename) throws IOException {
        return readLine(filename, true);
    }

    /**
     * Get the next line of the named file, first line is name to false
     *
     * @param filename the filename or alias that was used to reserve the file
     * @param recycle  - should file be restarted at EOF?
     * @return String containing the next line in the file (null if EOF reached and not recycle)
     * @throws IOException when reading of the file fails, or the file was not reserved properly
     */
    public String readLine(String filename, boolean recycle) throws IOException {
        return readLine(filename, recycle, false);
    }

    /**
     * Get the next line of the named file
     *
     * @param filename        the filename or alias that was used to reserve the file
     * @param recycle         - should file be restarted at EOF?
     * @param ignoreFirstLine - Ignore first line
     * @return String containing the next line in the file (null if EOF reached and not recycle)
     * @throws IOException when reading of the file fails, or the file was not reserved properly
     */
    public synchronized String readLine(String filename, boolean recycle,
                                        boolean ignoreFirstLine) throws IOException {
        String threadName = JMeterContextService.getContext().getThread().getThreadName();
        if (!StringUtils.contains(filename, threadName)) {
            filename = StringUtils.join(threadName, filename);
        }

        FileEntry fileEntry = files.get(filename);
        if (fileEntry != null) {
            if (fileEntry.inputOutputObject == null) {
                fileEntry.inputOutputObject = createBufferedReader(fileEntry);
            } else if (!(fileEntry.inputOutputObject instanceof Reader)) {
                throw new IOException("File " + filename + " already in use");
            }
            BufferedReader reader = (BufferedReader) fileEntry.inputOutputObject;
            String line = reader.readLine();
            if (line == null && recycle) {
                reader.close();
                reader = createBufferedReader(fileEntry);
                fileEntry.inputOutputObject = reader;
                if (ignoreFirstLine) {
                    // read first line and forget
                    reader.readLine();//NOSONAR
                }
                line = reader.readLine();
            }
            log.debug("Read:{}", line);
            return line;
        }
        // todo 这里更改了原始代码
        //throw new IOException("File never reserved: " + filename);
        log.error("File never reserved: " + filename);
        return "";
    }

    /**
     * @param alias           the file name or alias
     * @param recycle         whether the file should be re-started on EOF
     * @param ignoreFirstLine whether the file contains a file header which will be ignored
     * @param delim           the delimiter to use for parsing
     * @return the parsed line, will be empty if the file is at EOF
     * @throws IOException when reading of the aliased file fails, or the file was not reserved properly
     */
    public synchronized String[] getParsedLine(String alias, boolean recycle, boolean ignoreFirstLine, char delim) throws IOException {
        BufferedReader reader = getReader(alias, recycle, ignoreFirstLine);
        return CSVSaveService.csvReadFile(reader, delim);
    }

    /**
     * Return BufferedReader handling close if EOF reached and recycle is true
     * and ignoring first line if ignoreFirstLine is true
     *
     * @param alias           String alias
     * @param recycle         Recycle at eof
     * @param ignoreFirstLine Ignore first line
     * @return {@link BufferedReader}
     */
    private BufferedReader getReader(String alias, boolean recycle, boolean ignoreFirstLine) throws IOException {
        String threadName = JMeterContextService.getContext().getThread().getThreadName();
        if (!StringUtils.contains(alias, threadName)) {
            alias = StringUtils.join(threadName, alias);
        }

        FileEntry fileEntry = files.get(alias);
        if (fileEntry != null) {
            BufferedReader reader;
            if (fileEntry.inputOutputObject == null) {
                reader = createBufferedReader(fileEntry);
                fileEntry.inputOutputObject = reader;
                if (ignoreFirstLine) {
                    // read first line and forget
                    reader.readLine(); //NOSONAR
                }
            } else if (!(fileEntry.inputOutputObject instanceof Reader)) {
                throw new IOException("File " + alias + " already in use");
            } else {
                reader = (BufferedReader) fileEntry.inputOutputObject;
                if (recycle) { // need to check if we are at EOF already
                    reader.mark(1);
                    int peek = reader.read();
                    if (peek == -1) { // already at EOF
                        reader.close();
                        reader = createBufferedReader(fileEntry);
                        fileEntry.inputOutputObject = reader;
                        if (ignoreFirstLine) {
                            // read first line and forget
                            reader.readLine(); //NOSONAR
                        }
                    } else { // OK, we still have some data, restore it
                        reader.reset();
                    }
                }
            }
            return reader;
        } else {
            throw new IOException("File never reserved: " + alias);
        }
    }

    private BufferedReader createBufferedReader(FileEntry fileEntry) throws IOException {
        if (!fileEntry.file.canRead() || !fileEntry.file.isFile()) {
            throw new IllegalArgumentException("File " + fileEntry.file.getName() + " must exist and be readable");
        }
        BOMInputStream fis = new BOMInputStream(Files.newInputStream(fileEntry.file.toPath())); //NOSONAR
        InputStreamReader isr = null;
        // If file encoding is specified, read using that encoding, otherwise use default platform encoding
        String charsetName = fileEntry.charSetEncoding;
        if (!JOrphanUtils.isBlank(charsetName)) {
            isr = new InputStreamReader(fis, charsetName);
        } else if (fis.hasBOM()) {
            isr = new InputStreamReader(fis, fis.getBOM().getCharsetName());
        } else {
            @SuppressWarnings("DefaultCharset") final InputStreamReader withPlatformEncoding = new InputStreamReader(fis);
            isr = withPlatformEncoding;
        }
        return new BufferedReader(isr);
    }

    public synchronized void write(String filename, String value) throws IOException {
        String threadName = JMeterContextService.getContext().getThread().getThreadName();
        if (!StringUtils.contains(filename, threadName)) {
            filename = StringUtils.join(threadName, filename);
        }
        FileEntry fileEntry = files.get(filename);
        if (fileEntry != null) {
            if (fileEntry.inputOutputObject == null) {
                fileEntry.inputOutputObject = createBufferedWriter(fileEntry);
            } else if (!(fileEntry.inputOutputObject instanceof Writer)) {
                throw new IOException("File " + filename + " already in use");
            }
            BufferedWriter writer = (BufferedWriter) fileEntry.inputOutputObject;
            log.debug("Write:{}", value);
            writer.write(value);
        } else {
            throw new IOException("File never reserved: " + filename);
        }
    }

    private BufferedWriter createBufferedWriter(FileEntry fileEntry) throws IOException {
        OutputStream fos = Files.newOutputStream(fileEntry.file.toPath());
        OutputStreamWriter osw;
        // If file encoding is specified, write using that encoding, otherwise use default platform encoding
        String charsetName = fileEntry.charSetEncoding;
        if (!JOrphanUtils.isBlank(charsetName)) {
            osw = new OutputStreamWriter(fos, charsetName);
        } else {
            @SuppressWarnings("DefaultCharset") final OutputStreamWriter withPlatformEncoding = new OutputStreamWriter(fos);
            osw = withPlatformEncoding;
        }
        return new BufferedWriter(osw);
    }

    public synchronized void closeFiles() throws IOException {
        for (Map.Entry<String, FileEntry> me : files.entrySet()) {
            closeFile(me.getKey(), me.getValue());
        }
        files.clear();
    }

    public synchronized void closeFiles(String threadName) {
        try {
            for (Iterator<Map.Entry<String, FileEntry>> it = files.entrySet().iterator(); it.hasNext(); ) {
                Map.Entry<String, FileEntry> entry = it.next();
                if (entry.getKey().startsWith(threadName)) {
                    closeFile(entry.getKey(), entry.getValue());
                    it.remove();
                }
            }
            LoggerUtil.info("清除当前线程组占用文件后剩余文件数：" + files.size() + " 个", threadName);
        } catch (Exception e) {
            log.error("close files exception", e);
        }
    }

    /**
     * @param name the name or alias of the file to be closed
     * @throws IOException when closing of the aliased file fails
     */
    public synchronized void closeFile(String name) throws IOException {
        String threadName = JMeterContextService.getContext().getThread().getThreadName();
        if (!StringUtils.contains(name, threadName)) {
            name = StringUtils.join(threadName, name);
        }

        FileEntry fileEntry = files.get(name);
        closeFile(name, fileEntry);
    }

    private void closeFile(String name, FileEntry fileEntry) throws IOException {
        if (fileEntry != null && fileEntry.inputOutputObject != null) {
            log.info("Close: {}", name);
            fileEntry.inputOutputObject.close();
            fileEntry.inputOutputObject = null;
        }
    }

    boolean filesOpen() { // package access for test code only
        return files.values().stream()
                .anyMatch(fileEntry -> fileEntry.inputOutputObject != null);
    }

    /**
     * Method will get a random file in a base directory
     * <p>
     * TODO hey, not sure this method belongs here.
     * FileServer is for thread safe File access relative to current test's base directory.
     *
     * @param basedir    name of the directory in which the files can be found
     * @param extensions array of allowed extensions, if <code>null</code> is given,
     *                   any file be allowed
     * @return a random File from the <code>basedir</code> that matches one of
     * the extensions
     */
    public File getRandomFile(String basedir, String[] extensions) {
        File input = null;
        if (basedir != null) {
            File src = new File(basedir);
            File[] lfiles = src.listFiles(new JMeterFileFilter(extensions));
            if (lfiles != null) {
                // lfiles cannot be null as it has been checked before
                int count = lfiles.length;
                input = lfiles[ThreadLocalRandom.current().nextInt(count)];
            }
        }
        return input;
    }

    /**
     * Get {@link File} instance for provided file path,
     * resolve file location relative to base dir or script dir when needed
     *
     * @param path original path to file, maybe relative
     * @return {@link File} instance
     */
    public File getResolvedFile(String path) {
        reserveFile(path);
        return files.get(path).file;
    }

    private static class FileEntry {
        private String headerLine;
        private Throwable exception;
        private final File file;
        private Closeable inputOutputObject;
        private final String charSetEncoding;

        FileEntry(File f, Closeable o, String e) {
            file = f;
            inputOutputObject = o;
            charSetEncoding = e;
        }
    }

    /**
     * Resolve a file name that may be relative to the base directory. If the
     * name begins with the value of the JMeter property
     * "jmeter.save.saveservice.base_prefix" - default "~/" - then the name is
     * assumed to be relative to the basename.
     *
     * @param relativeName filename that should be checked for
     *                     <code>jmeter.save.saveservice.base_prefix</code>
     * @return the updated filename
     */
    public static String resolveBaseRelativeName(String relativeName) {
        if (relativeName.startsWith(BASE_PREFIX)) {
            String newName = relativeName.substring(BASE_PREFIX.length());
            return new File(getFileServer().getBaseDir(), newName).getAbsolutePath();
        }
        return relativeName;
    }

    /**
     * @return JMX Script name
     * @since 2.6
     */
    public String getScriptName() {
        return scriptName;
    }

    /**
     * @param scriptName Script name
     * @since 2.6
     */
    public void setScriptName(String scriptName) {
        this.scriptName = scriptName;
    }
}
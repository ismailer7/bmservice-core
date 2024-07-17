package com.bmservice.core.remote;

import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;

import java.io.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Slf4j
public class SSH {
    private JSch jsch;
    private Session session;
    private String connectionType;
    private String host;
    private String port;
    private String username;
    private String password;
    private String rsaKey;

    public SSH() {
        this.connectionType = "password";
        this.port = "22";
    }

    public static SSH SSHPassword(final String host, final String port, final String username, final String password) {
        final SSH ssh = new SSH();
        ssh.setHost(host);
        ssh.setPort(port);
        ssh.setUsername(username);
        ssh.setPassword(password);
        ssh.setConnectionType("password");
        return ssh;
    }

    public static SSH SSHKey(final String host, final String username, final String port, final String rsaKey) {
        final SSH ssh = new SSH();
        ssh.setHost(host);
        ssh.setPort(port);
        ssh.setUsername(username);
        ssh.setRsaKey(rsaKey);
        ssh.setConnectionType("key");
        return ssh;
    }

    public void connect() {
        try {
            this.jsch = new JSch();
            if (this.connectionType.equalsIgnoreCase("password")) {
                (this.session = this.jsch.getSession(this.username, this.host, Integer.parseInt(this.port))).setPassword(this.password);
                this.session.setConfig("StrictHostKeyChecking", "no");
                this.session.setConfig("PreferredAuthentications", "publickey,keyboard-interactive,password");
            }
            else {
                this.jsch.addIdentity(this.rsaKey);
                this.session = this.jsch.getSession(this.username, this.host, Integer.parseInt(this.port));
            }
            this.session.connect();
        }
        catch (Exception e) {
            log.error(e.getMessage());
        }
    }

    public void disconnect() {
        if (this.isConnected()) {
            this.session.disconnect();
            this.jsch = null;
        }
    }

    public boolean isConnected() {
        return this.session != null && this.session.isConnected();
    }

    public synchronized String cmd(final String command) {
        String output = "";
        try {
            final ChannelExec channelExec = (ChannelExec)this.session.openChannel("exec");
            final InputStream in = channelExec.getInputStream();
            channelExec.setCommand(command);
            channelExec.connect();
            final BufferedReader reader = new BufferedReader(new InputStreamReader(in));
            String line;
            while ((line = reader.readLine()) != null) {
                output = output + line + "\n";
            }
            channelExec.disconnect();
        }
        catch (Exception e) {
            log.error(e.getMessage());
        }
        return output;
    }

    public synchronized List<String> cmdLines(final String command) {
        final List<String> lines = new ArrayList<String>();
        try {
            final ChannelExec channelExec = (ChannelExec)this.session.openChannel("exec");
            final InputStream in = channelExec.getInputStream();
            channelExec.setCommand(command);
            channelExec.connect();
            final BufferedReader reader = new BufferedReader(new InputStreamReader(in));
            String line;
            while ((line = reader.readLine()) != null) {
                if (!"".equals(line.replaceAll("(?m)^[ \t]*\r?\n", ""))) {
                    lines.add(line);
                }
            }
            channelExec.disconnect();
        }
        catch (Exception e) {
            log.error(e.getMessage());
        }
        return lines;
    }

    public synchronized void downloadFile(final String remotePath, final String localPath) {
        if (this.isConnected()) {
            try {
                final ChannelSftp sftpChannel = (ChannelSftp)this.session.openChannel("sftp");
                sftpChannel.connect();
                final InputStream out = sftpChannel.get(remotePath);
                try (final BufferedReader br = new BufferedReader(new InputStreamReader(out))) {
                    final List<String> lines = new ArrayList<String>();
                    String line;
                    while ((line = br.readLine()) != null) {
                        lines.add(line);
                    }
                    FileUtils.writeLines(new File(localPath), (Collection)lines);
                }
                sftpChannel.disconnect();
            }
            catch (Exception e) {
                log.error(e.getMessage());
            }
        }
    }

    public synchronized void uploadFile(final String localPath, final String remotePath) {
        if (this.isConnected()) {
            try {
                final File file = new File(localPath);
                if (!file.isDirectory() && file.exists()) {
                    try (final InputStream is = new FileInputStream(file)) {
                        final ChannelSftp sftpChannel = (ChannelSftp)this.session.openChannel("sftp");
                        sftpChannel.connect();
                        sftpChannel.put(is, remotePath);
                        sftpChannel.disconnect();
                    }
                }
            }
            catch (Exception e) {
                log.error(e.getMessage());
            }
        }
    }

    public synchronized void uploadFile(final String localPath, final String remotePath, final ChannelSftp sftpChannel) {
        if (this.isConnected()) {
            try {
                final File file = new File(localPath);
                if (!file.isDirectory() && file.exists()) {
                    try (final InputStream is = new FileInputStream(file)) {
                        sftpChannel.put(is, remotePath);
                    }
                }
            }
            catch (Exception e) {
                log.error(e.getMessage());
            }
        }
    }

    public synchronized void uploadContent(final String content, final String remotePath) {
        if (this.isConnected()) {
            try {
                final ChannelSftp sftpChannel = (ChannelSftp)this.session.openChannel("sftp");
                sftpChannel.connect();
                try (final InputStream is = new ByteArrayInputStream(content.getBytes())) {
                    sftpChannel.put(is, remotePath);
                }
                sftpChannel.disconnect();
            }
            catch (Exception e) {
                log.error(e.getMessage());
            }
        }
    }

    public synchronized String readContent(final String remotePath) {
        String lines = "";
        if (this.isConnected()) {
            try {
                final ChannelSftp sftpChannel = (ChannelSftp)this.session.openChannel("sftp");
                sftpChannel.connect();
                final InputStream out = sftpChannel.get(remotePath);
                try (final BufferedReader br = new BufferedReader(new InputStreamReader(out))) {
                    String line;
                    while ((line = br.readLine()) != null) {
                        lines += line;
                    }
                }
                sftpChannel.disconnect();
            }
            catch (Exception e) {
                log.error(e.getMessage());
            }
        }
        return lines;
    }

    public String getHost() {
        return this.host;
    }

    public void setHost(final String host) {
        this.host = host;
    }

    public String getPort() {
        return this.port;
    }

    public void setPort(final String port) {
        this.port = port;
    }

    public String getUsername() {
        return this.username;
    }

    public void setUsername(final String username) {
        this.username = username;
    }

    public String getPassword() {
        return this.password;
    }

    public void setPassword(final String password) {
        this.password = password;
    }

    public String getRsaKey() {
        return this.rsaKey;
    }

    public void setRsaKey(final String rsaKey) {
        this.rsaKey = rsaKey;
    }

    public JSch getJsch() {
        return this.jsch;
    }

    public void setJsch(final JSch jsch) {
        this.jsch = jsch;
    }

    public Session getSession() {
        return this.session;
    }

    public void setSession(final Session session) {
        this.session = session;
    }

    public String getConnectionType() {
        return this.connectionType;
    }

    public void setConnectionType(final String connectionType) {
        this.connectionType = connectionType;
    }
}

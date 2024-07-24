package com.bmservice.core.worker;

import com.bmservice.core.helper.TypesParser;
import com.bmservice.core.remote.SSH;
import com.bmservice.core.utils.Strings;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.io.File;

@Slf4j
@Data
public class SenderWorker implements Runnable {

    private int dropId;
    private SSH ssh;
    private File pickupFile;

    public SenderWorker(final int dropId, final SSH ssh, final File pickupFile) {
        this.dropId = dropId;
        this.ssh = ssh;
        this.pickupFile = pickupFile;
    }

    @Override
    public void run() {
        log.info("Starting SenderWorker for pickUpFile: {}", pickupFile);
        try {
            if (this.ssh != null && this.pickupFile != null && this.pickupFile.exists()) {
                final int progress = TypesParser.safeParseInt(String.valueOf(this.pickupFile.getName().split("\\_")[2]));
                final String file = "/var/spool/bluemail/tmp/pickup_" + Strings.getSaltString(20, true, true, true, false) + ".txt";
                this.ssh.uploadFile(this.pickupFile.getAbsolutePath(), file);
                this.ssh.cmd("mv " + file + " /var/spool/bluemail/pickup/");
                if (this.dropId > 0) {
                    ServerWorker.updateDrop(this.dropId, progress);
                }
            }
        }
        catch (Exception e) {
            log.error(e.getMessage());
        }
    }
}

package com.bmservice.core.worker;

import com.bmservice.core.remote.SSH;
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
    }
}

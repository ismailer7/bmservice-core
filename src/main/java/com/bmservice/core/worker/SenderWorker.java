package com.bmservice.core.worker;

import com.bmservice.core.remote.SSH;
import lombok.extern.slf4j.Slf4j;

import java.io.File;

@Slf4j
public class SenderWorker extends Thread {


    public SenderWorker(int id, SSH ssh, File pickupsFile2) {
    }

    @Override
    public void start() {
        log.info("Starting SenderWorker");
    }
}

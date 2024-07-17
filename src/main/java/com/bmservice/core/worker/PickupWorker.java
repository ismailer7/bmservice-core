package com.bmservice.core.worker;

import com.bmservice.core.component.DropComponent;
import com.bmservice.core.models.admin.Server;
import com.bmservice.core.models.admin.Vmta;
import lombok.extern.slf4j.Slf4j;

import java.util.LinkedHashMap;
import java.util.List;

@Slf4j
public class PickupWorker extends Thread {
    public PickupWorker(int i, DropComponent drop, Server server, List<LinkedHashMap<String, Object>> linkedHashMaps, Vmta periodVmta) {
    }

    @Override
    public void start() {
        log.info("Starting PickupWorker");
    }
}

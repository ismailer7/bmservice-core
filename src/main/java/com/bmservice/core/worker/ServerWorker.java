package com.bmservice.core.worker;

import com.bmservice.core.component.DropComponent;
import com.bmservice.core.models.admin.Server;
import com.bmservice.core.models.admin.Vmta;

import java.util.List;

public class ServerWorker implements Runnable {

    DropComponent drop;
    Server server;
    List<Vmta> serverVmtas;
    int offset;
    int serverLimit;


    public ServerWorker(DropComponent drop, Server server, List<Vmta> serverVmta, int offset, int serverLimit) {
        this.drop = drop;
        this.server = server;
        this.serverVmtas = serverVmta;
        this.offset = offset;
        this.serverLimit = serverLimit;
    }

    @Override
    public void run() {
        // TODO server worker iplementation.
    }
}

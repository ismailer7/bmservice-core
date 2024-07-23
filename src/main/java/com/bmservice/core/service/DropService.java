package com.bmservice.core.service;

import com.bmservice.core.component.DropComponent;
import com.bmservice.core.component.RotatorComponent;
import com.bmservice.core.exception.BmServiceCoreException;
import com.bmservice.core.mapper.list.ListMapper;
import com.bmservice.core.models.admin.Server;
import com.bmservice.core.models.admin.Vmta;
import com.bmservice.core.worker.ServerWorker;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@Service
public class DropService {

    static volatile RotatorComponent PLACEHOLDERS_ROTATOR;
    static volatile RotatorComponent HEADERS_ROTATOR;

    @Autowired
    private ListMapper listMapper;

    public void send(DropComponent dropComponent) {
        if(!dropComponent.getServers().isEmpty() && !dropComponent.getVmtas().isEmpty()) {

            PLACEHOLDERS_ROTATOR = dropComponent.isHasPlaceholders() ? new RotatorComponent(Arrays.asList(dropComponent.getPlaceholders()), dropComponent.getPlaceholdersRotation()) : null;
            HEADERS_ROTATOR = new RotatorComponent(Arrays.asList(dropComponent.getHeaders()), dropComponent.getHeadersRotation());

            var serversSize = dropComponent.getServers().size();
            var vmtasSize = dropComponent.getVmtas().size();
            var dataCount = dropComponent.getDataCount();
            final ExecutorService serversExecutor = Executors.newFixedThreadPool(serversSize);
            List<Vmta> serverVmtas = null;
            int offset = 0;
            int vmtasLimit = 0;
            int serverLimit = 0;
            int limitRest = 0;

            if(dropComponent.isSend()) {
                if("servers".equalsIgnoreCase(dropComponent.getEmailsSplitType())) {
                    serverLimit = (int) Math.ceil((double) dataCount / serversSize);
                    limitRest = dropComponent.getDataCount() - serverLimit * serversSize;
                } else {
                    vmtasLimit = (int) Math.ceil((double) dataCount / vmtasSize) ;
                    limitRest = dataCount - vmtasLimit * vmtasSize;
                }
            }
                for (int i = 0; i < serversSize; i++) {
                    final Server server = dropComponent.getServers().get(i);
                    if(server != null && "vmtas".equalsIgnoreCase(dropComponent.getEmailsSplitType())) {
                        serverLimit = 0;
                    }
                    serverVmtas = new ArrayList<>();
                    if(!dropComponent.getVmtas().isEmpty()) {
                        for(Vmta vmta: dropComponent.getVmtas()) {
                            if(vmta.getServerId() == server.getId()) {
                                serverVmtas.add(vmta);
                            }
                            if(!dropComponent.isSend() || !"vmtas".equalsIgnoreCase(dropComponent.getEmailsSplitType())) {
                                continue;
                            }
                            serverLimit += vmtasLimit;
                        }
                    }
                    if(i == serversSize - 1) {
                        serverLimit += limitRest;
                    }

                    // call the server worker for each server

                    ServerWorker worker = new ServerWorker(dropComponent, server, serverVmtas, offset, serverLimit, listMapper);
                    serversExecutor.submit(worker);
                    offset += serverLimit;
                }
            serversExecutor.shutdown();
            try {
                serversExecutor.awaitTermination(10L, TimeUnit.DAYS);
            } catch (InterruptedException e) {
                throw new BmServiceCoreException("Error Terminating Server Worker Thread !");
            }
        }

    }

    public static synchronized String getCurrentPlaceHolder() {
        return (String)((PLACEHOLDERS_ROTATOR != null) ? PLACEHOLDERS_ROTATOR.getCurrentValue() : "");
    }

    public static synchronized void rotatePlaceHolders() {
        if (PLACEHOLDERS_ROTATOR != null) {
            PLACEHOLDERS_ROTATOR.rotate();
        }
    }

    public static synchronized String getCurrentHeader() {
        return (String)((HEADERS_ROTATOR != null) ? HEADERS_ROTATOR.getCurrentValue() : "");
    }

    public static synchronized void rotateHeaders() {
        if (HEADERS_ROTATOR != null) {
            HEADERS_ROTATOR.rotate();
        }
    }

}

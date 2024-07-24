package com.bmservice.core.worker;

import com.bmservice.core.BmServiceCoreConstants;
import com.bmservice.core.component.DropComponent;
import com.bmservice.core.component.RotatorComponent;
import com.bmservice.core.exception.BmServiceCoreException;
import com.bmservice.core.exception.DatabaseException;
import com.bmservice.core.exception.ThreadException;
import com.bmservice.core.helper.DropsHelper;
import com.bmservice.core.helper.TypesParser;
import com.bmservice.core.mapper.list.ListMapper;
import com.bmservice.core.models.admin.Server;
import com.bmservice.core.models.admin.Vmta;
import com.bmservice.core.remote.SSH;
import com.bmservice.core.utils.Strings;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@Slf4j
public class ServerWorker implements Runnable {

    DropComponent drop;
    Server server;
    List<Vmta> vmtas;
    int offset;
    int limit;

    String query;
    List<PickupWorker> pickupWorkers;
    List<SenderWorker> sendersWorkers;

    ListMapper listMapper;


    public ServerWorker(DropComponent drop, Server server, List<Vmta> vmtas, int offset, int limit, ListMapper listMapper) {
        this.drop = drop;
        this.server = server;
        this.vmtas = vmtas;
        this.offset = offset;
        this.limit = limit;

        this.pickupWorkers = new ArrayList<>();
        this.sendersWorkers = new ArrayList<>();

        this.listMapper = listMapper;

        if(drop.isSend()) {
            this.query = "SELECT * FROM (";
            var sb = new StringBuilder();
            drop.getLists().entrySet().stream().map(en -> {
                this.query = this.query + "SELECT id, '" + String.valueOf(en.getValue()) + "'AS table'" + String.valueOf(en.getKey()) + "' AS list_id, fname,lname,email" ;
                return en;
            }).map(en -> {
                sb.append(this.query);
                String s;
                if(String.valueOf(en.getValue()).contains("seeds")) {
                    s = ", generate_series(1," + drop.getEmailsPerSeeds() + ") AS serie";
                } else {
                    s = ", id AS serie";
                }
                this.query = sb.append(s).toString();
                return en;
            }).forEachOrdered(en -> this.query = this.query + " FROM " + String.valueOf(en.getValue()) + " UNION ALL ");
            this.query = this.query.substring(0, this.query.length() - 10) + " WHERE (offers_excluded IS NULL OR offers_excluded = '' OR NOT ('"
                    + this.drop.getOfferId() + "' = ANY(string_toarray(offers_excluded,',')))) ORDER BY id OFFSET " + this.drop.getDataStart()
                    + " LIMIT " + this.drop.getDataCount() + ") as sub OFFSET " + this.offset + " LIMIT " + this.limit;
        }

        if(!this.vmtas.isEmpty()) {
            final int rotation = this.drop.isSend() ? this.drop.getVmtasRotation() : this.drop.getTestEmails().length;
            this.drop.setVmtasRotator(new RotatorComponent(this.vmtas, rotation));
        }
        this.drop.setPickupsFolder(BmServiceCoreConstants.BASE_PATH + "/tmp/pickups/server_" + this.server.getId() + "_" + Strings.getSaltString(20, true, true, true, false));
        new File(this.drop.getPickupsFolder()).mkdirs();
    }

    @Override
    public void run() {
        log.info("Server Worker Started for server '{}' Started !", server.getId());

        SSH ssh = null;
        boolean errorOccured = false;
        boolean isStopped = false;

        try {
                if(this.server != null && this.server.getId() > 0 && !this.vmtas.isEmpty()) {
                    ssh = SSH.SSHPassword(this.server.getMainIp(), String.valueOf(this.server.getSshPort()), this.server.getUsername(), this.server.getPassword());
                    ssh.connect();

                    if(this.drop.isUploadImages()) {
                        DropsHelper.uploadImage(this.drop, ssh);
                    }

                    if(this.vmtas.isEmpty()) {
                        throw new BmServiceCoreException("No Vtmas found for server '" + this.server.getId() + "'");
                    }
                    List<LinkedHashMap<String, Object>> result = null;
                    if(this.drop.isSend()) {
                        var sql = new HashMap<String, String>();
                        sql.put("sql", "SELECT * from hotma.fresh_de_dedata_1");
                        var test = listMapper.execute(sql);
                        this.drop.setEmailsCount(result.size());
                    } else {
                        result = new ArrayList<LinkedHashMap<String, Object>>();
                        if (this.drop.getTestEmails() != null && this.drop.getTestEmails().length > 0) {
                            for (final Vmta vmta : this.vmtas) {
                                if (vmta != null) {
                                    for (final String testEmail : this.drop.getTestEmails()) {
                                        final LinkedHashMap<String, Object> tmp = new LinkedHashMap<String, Object>();
                                        tmp.put("id", 0);
                                        tmp.put("email", testEmail.trim());
                                        tmp.put("table", "");
                                        tmp.put("list_id", 0);
                                        result.add(tmp);
                                    }
                                }
                            }
                        }
                    }


                    if (this.drop.isSend() && this.drop.isNewDrop()) {
                        DropsHelper.saveDrop(this.drop, this.server);
                        if (this.drop.getId() > 0) {
                            if (this.vmtas.isEmpty()) {
                                throw new Exception("No Vmtas Found !");
                            }
                            final int vmtasTotal = (int)Math.ceil(this.drop.getEmailsCount() / this.vmtas.size());
                            final int vmtasRest = this.drop.getEmailsCount() - vmtasTotal * this.vmtas.size();
                            int index = 0;
                            if (!this.vmtas.isEmpty()) {
                                for (final Vmta vmta2 : this.vmtas) {
                                    if (index < vmtasRest) {
                                        DropsHelper.saveDropVmta(this.drop, vmta2, vmtasTotal + 1);
                                    }
                                    else {
                                        DropsHelper.saveDropVmta(this.drop, vmta2, vmtasTotal);
                                    }
                                    ++index;
                                }
                            }
                        }
                    }

                    if (this.drop.isSend()) {
                        DropsHelper.writeThreadStatusFile(this.server.getId(), this.drop.getPickupsFolder());
                    }
                    if (!this.drop.isSend()) {
                        this.drop.setEmailsCount(this.drop.getTestEmails().length * this.vmtas.size());
                    }

                    var calcBatch = (this.drop.getBatch() > this.drop.getEmailsCount()) ? this.drop.getEmailsCount() : this.drop.getBatch();
                    this.drop.setBatch(calcBatch);
                    this.drop.setBatch(this.drop.getBatch() == 0 ? 1 : this.drop.getBatch());
                    final ExecutorService pickupsExecutor = Executors.newFixedThreadPool(100);
                    if (this.drop.getBatch() == 0) {
                        throw new Exception("Batch should be greather than 0 !");
                    }

                    final int pickupsNumber = (this.drop.getEmailsCount() % this.drop.getBatch() == 0) ? ((int)Math.ceil(this.drop.getEmailsCount() / this.drop.getBatch())) : ((int)Math.ceil(this.drop.getEmailsCount() / this.drop.getBatch()) + 1);
                    int start = 0;
                    int finish = this.drop.getBatch();
                    Vmta periodVmta = null;
                    PickupWorker worker = null;


                    for (int i = 0; i < pickupsNumber; ++i) {
                        if (this.drop != null && this.drop.isSend() && this.drop.getId() > 0) {
                            final String status = this.DropStatus();
                            if (DropsHelper.hasToStopDrop(this.server.getId(), this.drop.getPickupsFolder()) || Objects.equals(status, "interrupted")) {
                                //this.interrupt();
                                pickupsExecutor.shutdownNow();
                                this.interruptDrop();
                                isStopped = true;
                                this.drop.setStoped(true);
                                break;
                            }
                        }
                        if (isStopped || this.drop.isStoped()) {
                            pickupsExecutor.shutdownNow();
                            // this.interrupt();
                            this.pickupWorkers.forEach(previousWorker -> {
                                /*if (previousWorker.isAlive()) {
                                    previousWorker.interrupt();
                                }*/
                                return;
                            });
                            break;
                        }
                        periodVmta = ("emails-per-period".equalsIgnoreCase(this.drop.getVmtasEmailsProcces()) ? this.drop.getCurrentVmta() : null);
                        worker = new PickupWorker(i, this.drop, this.server, result.subList(start, finish), periodVmta);
                        // worker.setUncaughtExceptionHandler(new ThreadException());
                        pickupsExecutor.submit(worker);
                        this.pickupWorkers.add(worker);
                        start += this.drop.getBatch();
                        finish += this.drop.getBatch();
                        if (finish > result.size()) {
                            finish = result.size();
                        }
                        if (start >= result.size()) {
                            break;
                        }
                    }


                    pickupsExecutor.shutdown();
                    pickupsExecutor.awaitTermination(1L, TimeUnit.DAYS);
                    if (!isStopped && !this.drop.isStoped()) {
                        File[] pickupsFiles = new File(this.drop.getPickupsFolder()).listFiles();
                        if (pickupsFiles != null && pickupsFiles.length > 0 && ssh.isConnected() && !this.drop.isStoped()) {
                            final File[] tmp2 = this.drop.isSend() ? new File[pickupsFiles.length - 1] : new File[pickupsFiles.length];
                            int idx = 0;
                            for (final File pickupsFile : pickupsFiles) {
                                if (pickupsFile.getName().startsWith("pickup_")) {
                                    tmp2[idx] = pickupsFile;
                                    ++idx;
                                }
                            }
                            pickupsFiles = tmp2;
                            Arrays.sort(pickupsFiles, Comparator.comparing(f -> TypesParser.safeParseInt(f.getName().split("_")[1])));
                            final ExecutorService senderExecutor = Executors.newFixedThreadPool(100);
                            SenderWorker senderWorker = null;
                            for (final File pickupsFile2 : pickupsFiles) {
                                if (this.drop != null && this.drop.isSend() && this.drop.getId() > 0) {
                                    final String status2 = this.DropStatus();
                                    if (DropsHelper.hasToStopDrop(this.server.getId(), this.drop.getPickupsFolder()) || status2 == "interrupted") {
                                        senderExecutor.shutdownNow();
                                        this.interruptDrop();
                                        isStopped = true;
                                        break;
                                    }
                                }
                                if (isStopped || this.drop.isStoped()) {
                                    senderExecutor.shutdownNow();
                                    // this.interrupt();
                                    this.sendersWorkers.forEach(previousWorker -> {
                                        /*if (previousWorker.isAlive()) {
                                            previousWorker.interrupt();
                                        }*/
                                        return;
                                    });
                                    break;
                                }
                                senderWorker = new SenderWorker(this.drop.getId(), ssh, pickupsFile2);
                                //senderWorker.setUncaughtExceptionHandler(new ThreadException());
                                senderExecutor.submit(senderWorker);
                                this.sendersWorkers.add(senderWorker);
                                if (this.drop.getDelay() > 0L) {
                                    Thread.sleep(this.drop.getDelay());
                                }
                            }
                            senderExecutor.shutdown();
                            senderExecutor.awaitTermination(1L, TimeUnit.DAYS);
                        }




                }
            }
        } catch (Exception e) {
            log.error(e.getMessage());
        }


        /*String sql = "SELECT * FROM list.servers limit 1";
        HashMap<String, String> map = new HashMap<>();
        map.put("sql", sql);
        try {
            Server server = listMapper.execute(map);
        }catch (Exception e) {
            log.error(e.getMessage());
        }
        log.info(server.toString());*/
    }


    public static synchronized void updateDrop(final int dropId, final int progress) throws DatabaseException {
        //Database.get("master").executeUpdate("UPDATE production.drops SET sent_progress = sent_progress + '" + progress + "'  WHERE id = ?", new Object[] { dropId }, 0);
    }

    public void finishProccess(final SSH ssh, final boolean errorOccured, final boolean isStopped) {
        try {
            if (ssh != null && ssh.isConnected()) {
                ssh.disconnect();
            }
            if (this.drop != null) {
                if (this.drop.getId() > 0 && !errorOccured && !isStopped) {
                    int progress = 0;
                    //final List<LinkedHashMap<String, Object>> result = Database.get("master").executeQuery("SELECT sent_progress FROM production.drops WHERE id =" + this.drop.id, null, 0);

                    final List<LinkedHashMap<String, Object>> result = new ArrayList<>();
                    if (!result.isEmpty()) {
                        progress = (int) result.get(0).get("sent_progress");
                        if (progress == this.drop.getEmailsCount()) {
                            // Database.get("master").executeUpdate("UPDATE production.drops SET status = 'completed' , finish_time = ?  WHERE id = ?", new Object[] { new Timestamp(System.currentTimeMillis()), this.drop.id }, 0);
                        }
                    }
                }
                FileUtils.deleteDirectory(new File(this.drop.getPickupsFolder()));
            }
        }
        catch (Exception e) {
            log.error(e.getMessage());
        }
    }

    public void errorDrop() {
        try {
            // Database.get("master").executeUpdate("UPDATE production.drops SET status = 'error' , finish_time = ?  WHERE id = ?", new Object[] { new Timestamp(System.currentTimeMillis()), this.drop.id }, 0);
            if (this.drop != null) {
                FileUtils.deleteDirectory(new File(this.drop.getPickupsFolder()));
            }
        }
        catch (Exception e) {
            log.error(e.getMessage());
        }
    }

    public void interruptDrop() {
        try {
            //Database.get("master").executeUpdate("UPDATE production.drops SET status = 'interrupted' , finish_time = ?  WHERE id = ?", new Object[] { new Timestamp(System.currentTimeMillis()), this.drop.id }, 0);
            if (this.drop != null) {
                FileUtils.deleteDirectory(new File(this.drop.getPickupsFolder()));
            }
        }
        catch (Exception e) {
            log.error(e.getMessage());
        }
    }

    public String DropStatus() {
        String status = "";
        try {
            // final List<LinkedHashMap<String, Object>> result = Database.get("master").executeQuery("SELECT status FROM production.drops WHERE id =" + this.drop.id, null, 0);
            final List<LinkedHashMap<String, Object>> result = new ArrayList<>();
            if (!result.isEmpty()) {
                status = (String) result.get(0).get("status");
                return status;
            }
        }
        catch (Exception e) {
            log.error(e.getMessage());
        }
        return status;
    }




}

package helper;

import com.bmservice.core.component.DropComponent;
import com.bmservice.core.mapper.system.ServerMapper;
import com.bmservice.core.mapper.system.VmtaMapper;
import com.bmservice.core.models.admin.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ArrayUtils;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Component
@AllArgsConstructor
public class DropsHelper {

    private static ObjectMapper objectMapper;

    private static ServerMapper serverMapper;

    private static VmtaMapper vmtaMapper;

    public static DropComponent parseDropFile(String content) {
        DropComponent drop = null;
        if (!"".equalsIgnoreCase(content)) {
            try {
                var data = objectMapper.readValue(content, TreeMap.class);
                //final TreeMap<String, Object> data = (TreeMap<String, Object>)new ObjectMapper().readValue(content, (Class)TreeMap.class);
                if (data != null && !data.isEmpty()) {
                    //drop = new DropComponent();

                    var dropId = TypesParser.safeParseInt(String.valueOf(Mapper.getMapValue(data, "drop-id", "0")));
                    var isSend = "true".equalsIgnoreCase(String.valueOf(Mapper.getMapValue(data, "drop", "false")));
                    var mailerId = TypesParser.safeParseInt(String.valueOf(Mapper.getMapValue(data, "user-id", "0")));
                    var serversIds = Arrays.copyOf(((List<?>)Mapper.getMapValue(data, "servers", new ArrayList<>())).toArray(), ((List<?>)Mapper.getMapValue(data, "servers", new ArrayList<>())).toArray().length, (Class<? extends String[]>)String[].class);

                    /*if (drop.serversIds != null && drop.serversIds.length > 0) {
                        drop.servers = (List<Server>)ActiveRecord.all(Server.class, "id IN (" + String.join(",", (CharSequence[])drop.serversIds) + ")", null);
                    }*/
                    var servers = new ArrayList<Server>();
                    if(serversIds != null && serversIds.length > 0) {
                        servers.addAll(serverMapper.findAllIn(String.join(",", (CharSequence[])serversIds)));
                    }
                    if (servers.isEmpty()) {
                        throw new Exception("No Servers Found !");
                    }

                    var vmtasRotation = TypesParser.safeParseInt(String.valueOf(Mapper.getMapValue(data, "vmtas-rotation", "1")));
                    var vmtasIds = Arrays.copyOf(((List)Mapper.getMapValue(data, "selected-vmtas", new ArrayList<>())).toArray(), ((List<?>)Mapper.getMapValue(data, "selected-vmtas", new ArrayList<>())).toArray().length, (Class<? extends String[]>)String[].class);

                    if (vmtasIds != null && vmtasIds.length > 0) {
                        String condition = "id IN (";
                        for (final String vmtasId : vmtasIds) {
                            if (vmtasId != null && vmtasId.contains("|")) {
                                condition = condition + vmtasId.split("\\|")[1] + ",";
                            }
                        }
                        condition = condition.substring(0, condition.length() - 1) + ")";
                        //vmtas = (List<Vmta>)ActiveRecord.all(Vmta.class, condition, null);
                    }
                    /*if (drop.vmtas == null || drop.vmtas.isEmpty()) {
                        throw new Exception("No Vmtas Found !");
                    }*/


                    var vmtasEmailsProcess = String.valueOf(Mapper.getMapValue(data, "vmtas-emails-proccess", "vmtas-rotation"));
                    var numberOfEmails = TypesParser.safeParseInt(String.valueOf(Mapper.getMapValue(data, "number-of-emails", "0")));
                    var emailsPeriodType = String.valueOf(Mapper.getMapValue(data, "emails-period-type", "seconds"));
                    var batch = TypesParser.safeParseInt(String.valueOf(Mapper.getMapValue(data, "batch", "1")));
                    var emailsPeriodValue = TypesParser.safeParseInt(String.valueOf(Mapper.getMapValue(data, "emails-period-value", "0")));
                    var delay = TypesParser.safeParseInt(String.valueOf(Mapper.getMapValue(data, "x-delay", "1")));
                    var sponsorId = TypesParser.safeParseInt(String.valueOf(Mapper.getMapValue(data, "sponsor", "0")));
                    var offerId = TypesParser.safeParseInt(String.valueOf(Mapper.getMapValue(data, "offer", "0")));
                    var creativeId = TypesParser.safeParseInt(String.valueOf(Mapper.getMapValue(data, "creative", "0")));
                    var fromNameId = TypesParser.safeParseInt(String.valueOf(Mapper.getMapValue(data, "from-name-id", "0")));





                    var fromName = String.valueOf(Mapper.getMapValue(data, "from-name-text", ""));
                    if ("".equals(fromName) && fromNameId > 0) {
                        var fromNameObject = OfferName.builder().id(fromNameId).build();
                        if (fromNameObject != null && fromNameObject.getValue() != null) {
                            fromName = fromNameObject.getValue();
                        }
                    }

                    var subjectId = TypesParser.safeParseInt(String.valueOf(Mapper.getMapValue(data, "subject-id", "0")));
                    var subject = String.valueOf(Mapper.getMapValue(data, "subject-text", ""));
                    if ("".equals(subject) && subjectId > 0) {
                        var subjectObject = OfferSubject.builder().id(subjectId).build();
                        if (subjectObject != null && subjectObject.getValue() != null) {
                            subject = subjectObject.getValue();
                        }
                    }

                    var headersRotation = 1;
                    var headers = Arrays.copyOf(((List<?>)Mapper.getMapValue(data, "headers", new ArrayList<>())).toArray(), ((List<?>)Mapper.getMapValue(data, "headers", new ArrayList<>())).toArray().length, (Class<? extends String[]>)String[].class);
                    var bounceEmail = String.valueOf(Mapper.getMapValue(data, "bounce-email", ""));
                    var returnPath = String.valueOf(Mapper.getMapValue(data, "return-path", ""));
                    if (!bounceEmail.contains("@") && !returnPath.contains("@")) {
                        returnPath = ((bounceEmail.isEmpty() || returnPath.isEmpty()) ? "" : (bounceEmail + "@" + returnPath));
                        bounceEmail = returnPath;
                    }

                    if("emails-per-period".equalsIgnoreCase(vmtasEmailsProcess)) {
                        if (numberOfEmails == 0) {
                            throw new Exception("Number of Emails for Period is 0 !");
                        }
                        batch = 1;

                        switch (emailsPeriodType) {
                            case "seconds": {
                                emailsPeriodValue *= 1000;
                                break;
                            }
                            case "minutes": {
                                emailsPeriodValue = emailsPeriodValue * 60 * 1000;
                                break;
                            }
                            case "hours": {
                                emailsPeriodValue = emailsPeriodValue * 60 * 60 * 1000;
                                break;
                            }
                        }
                        delay = (int)  Math.ceil(emailsPeriodValue / numberOfEmails);
                    }



                    var fromEmail = String.valueOf(Mapper.getMapValue(data, "from-email", "from@[domain]"));
                    var replyTo = String.valueOf(Mapper.getMapValue(data, "reply-to", "reply@[domain]"));
                    var received = String.valueOf(Mapper.getMapValue(data, "received", ""));
                    var to = String.valueOf(Mapper.getMapValue(data, "to", "[email]"));
                    var placeholdersRotation = TypesParser.safeParseInt(String.valueOf(Mapper.getMapValue(data, "placeholders-rotation", "1")));
                    var placeholders = ("".equalsIgnoreCase(String.valueOf(Mapper.getMapValue(data, "body-placeholders", ""))) ? new String[0] : String.valueOf(Mapper.getMapValue(data, "body-placeholders", "")).split("\r\n"));
                    var hasPlaceholders = (placeholders.length > 0);
                    var uploadImages = "on".equalsIgnoreCase(String.valueOf(Mapper.getMapValue(data, "upload-images", "off")));
                    var charset = String.valueOf(Mapper.getMapValue(data, "charset", "utf-8"));
                    var contentTransferEncoding = String.valueOf(Mapper.getMapValue(data, "content-transfer-encoding", "7bit"));
                    var contentType = String.valueOf(Mapper.getMapValue(data, "content-type", "text/html"));
                    var body = String.valueOf(Mapper.getMapValue(data, "body", ""));
                    var trackOpens = (isSend && "on".equalsIgnoreCase(String.valueOf(Mapper.getMapValue(data, "track-opens", "off"))));
                    var ispId = TypesParser.safeParseInt(String.valueOf(Mapper.getMapValue(data, "isp-id", "0")));
                    var isp = Isp.builder().id(ispId).build();
                    var staticDomain = String.valueOf(Mapper.getMapValue(data, "static-domain", ""));
                    var emailsSplitType = String.valueOf(Mapper.getMapValue(data, "emails-split-type", "vmtas"));
                    var testFrequency = TypesParser.safeParseInt(String.valueOf(Mapper.getMapValue(data, "send-test-after", "-1")));
                    var testEmails = String.valueOf(Mapper.getMapValue(data, "recipients-emails", "")).split("\\;");
                    var rcptfrom = String.valueOf(Mapper.getMapValue(data, "rcpt-from", ""));
                    var dataStart = TypesParser.safeParseInt(String.valueOf(Mapper.getMapValue(data, "data-start", "0")));
                    var dataCount = TypesParser.safeParseInt(String.valueOf(Mapper.getMapValue(data, "data-count", "0")));
                    var emailsPerSeeds = TypesParser.safeParseInt(String.valueOf(Mapper.getMapValue(data, "emails-per-seed", "1")));

                    var emailsCount = 0;
                    var lists = new HashMap<Integer, String>();
                    if (isSend && !"".equals(Mapper.getMapValue(data, "lists", ""))) {
                        final String[] listsParts = String.valueOf(Mapper.getMapValue(data, "lists", "")).split("\\,");
                        if (listsParts.length > 0) {
                            for (final String tmp : listsParts) {
                                lists.put(TypesParser.safeParseInt(String.valueOf(tmp.split("\\|")[0])), String.valueOf(tmp.split("\\|")[1]));
                            }
                        }
                    }
                    var listsCount = lists.size();
                    var isAutoResponse = false;
                    var randomCaseAutoResponse = false;
                    var autoResponseRotation = 0;
                    var autoReplyEmails = new String[] {""};
                    if (isSend) {
                        isAutoResponse = "on".equalsIgnoreCase(String.valueOf(Mapper.getMapValue(data, "auto-response", "off")));
                        randomCaseAutoResponse = "on".equalsIgnoreCase(String.valueOf(Mapper.getMapValue(data, "random-case-auto-response", "off")));
                        autoResponseRotation = TypesParser.safeParseInt(String.valueOf(Mapper.getMapValue(data, "auto-response-frequency", "0")));
                        autoReplyEmails = (String[])("".equals(String.valueOf(Mapper.getMapValue(data, "auto-reply-emails", ""))) ? null : String.valueOf(Mapper.getMapValue(data, "auto-reply-emails", "")).split("\n"));
                    }
                    var redirectFileName = "r.php";
                    var optoutFileName = "optout.php";

                    /*final String dataSourcePath = new File(System.getProperty("base.path")).getAbsolutePath() + "/applications/bluemail/configs/application.ini";
                    if (new File(new File(System.getProperty("base.path")).getAbsolutePath() + "/applications/bluemail/configs/application.ini").exists()) {
                        final HashMap<String, String> map = Mapper.readProperties(dataSourcePath);
                        if (map != null && !map.isEmpty()) {
                            if (map.containsKey("redirect_file")) {
                                drop.redirectFileName = String.valueOf(map.get("redirect_file"));
                            }
                            if (map.containsKey("optout_file")) {
                                drop.optoutFileName = String.valueOf(map.get("optout_file"));
                            }
                        }
                    }*/

                    drop = DropComponent.builder().id(dropId).isNewDrop(drop.getId() == 0).isSend(isSend).content(content).mailerId(mailerId)
                            .randomTags(getAllRandomTags(drop.getContent()))
                            .serversIds(serversIds).servers(servers).vmtasRotation(vmtasRotation).vmtasIds(vmtasIds).vmtasEmailsProcces(vmtasEmailsProcess)
                            .batch(batch).delay(delay).numberOfEmails(numberOfEmails).emailsPeriodValue(emailsPeriodValue).emailsPeriodType(emailsPeriodType)
                            .sponsorId(sponsorId).sponsor(Sponsor.builder().id(sponsorId).build()).offerId(offerId).offer(Offer.builder().id(offerId).build()).creativeId(creativeId).fromNameId(fromNameId).fromName(fromName).subjectId(subjectId).subject(subject)
                            .bounceEmail(bounceEmail).returnPath(returnPath).headersRotation(headersRotation).headers(headers).fromEmail(fromEmail).replyTo(replyTo).received(received).to(to)
                            .placeholdersRotation(placeholdersRotation).placeholders(placeholders).hasPlaceholders(hasPlaceholders).uploadImages(uploadImages).charset(charset).contentTransferEncoding(contentTransferEncoding).content(contentType).body(body)
                            .trackOpens(trackOpens).ispId(ispId).isp(isp).staticDomain(staticDomain).emailsSplitType(emailsSplitType).testFrequency(testFrequency).testEmails(testEmails).rcptfrom(rcptfrom).dataStart(dataStart).dataCount(dataCount).emailsPerSeeds(emailsPerSeeds).emailsCount(emailsCount)
                            .lists(lists).listsCount(listsCount).isAutoResponse(isAutoResponse).randomCaseAutoResponse(randomCaseAutoResponse).autoResponseRotation(autoResponseRotation).autoReplyEmails(autoReplyEmails).redirectFileName(redirectFileName).optoutFileName(optoutFileName).build();


/*
                    drop.id = TypesParser.safeParseInt(String.valueOf(Mapper.getMapValue(data, "drop-id", "0")));
                    drop.setNewDrop(drop.getId() == 0);
                    drop.isSend = "true".equalsIgnoreCase(String.valueOf(Mapper.getMapValue(data, "drop", "false")));
                    drop.content = content;
                    drop.mailerId = TypesParser.safeParseInt(String.valueOf(Mapper.getMapValue(data, "user-id", "0")));
                    drop.randomTags = getAllRandomTags(drop.content);
                    drop.serversIds = Arrays.copyOf(((List<?>)Mapper.getMapValue(data, "servers", new ArrayList())).toArray(), ((List<?>)Mapper.getMapValue(data, "servers", new ArrayList())).toArray().length, (Class<? extends String[]>)String[].class);
                    if (drop.serversIds != null && drop.serversIds.length > 0) {
                        drop.servers = (List<Server>)ActiveRecord.all(Server.class, "id IN (" + String.join(",", (CharSequence[])drop.serversIds) + ")", null);
                    }
                    if (drop.servers == null || drop.servers.isEmpty()) {
                        throw new Exception("No Servers Found !");
                    }
                    drop.vmtasRotation = TypesParser.safeParseInt(String.valueOf(Mapper.getMapValue(data, "vmtas-rotation", "1")));
                    drop.vmtasIds = Arrays.copyOf(((List)Mapper.getMapValue(data, "selected-vmtas", new ArrayList<>())).toArray(), ((List<?>)Mapper.getMapValue(data, "selected-vmtas", new ArrayList<>())).toArray().length, (Class<? extends String[]>)String[].class);
                    if (drop.vmtasIds != null && drop.vmtasIds.length > 0) {
                        String condition = "id IN (";
                        for (final String vmtasId : drop.vmtasIds) {
                            if (vmtasId != null && vmtasId.contains("|")) {
                                condition = condition + vmtasId.split("\\|")[1] + ",";
                            }
                        }
                        condition = condition.substring(0, condition.length() - 1) + ")";
                        drop.vmtas = (List<Vmta>)ActiveRecord.all(Vmta.class, condition, null);
                    }
                    if (drop.vmtas == null || drop.vmtas.isEmpty()) {
                        throw new Exception("No Vmtas Found !");
                    }
                    drop.vmtasEmailsProcces = String.valueOf(Mapper.getMapValue(data, "vmtas-emails-proccess", "vmtas-rotation"));
                    drop.batch = TypesParser.safeParseInt(String.valueOf(Mapper.getMapValue(data, "batch", "1")));
                    drop.delay = TypesParser.safeParseInt(String.valueOf(Mapper.getMapValue(data, "x-delay", "1")));
                    drop.numberOfEmails = TypesParser.safeParseInt(String.valueOf(Mapper.getMapValue(data, "number-of-emails", "0")));
                    drop.emailsPeriodValue = TypesParser.safeParseInt(String.valueOf(Mapper.getMapValue(data, "emails-period-value", "0")));
                    drop.emailsPeriodType = String.valueOf(Mapper.getMapValue(data, "emails-period-type", "seconds"));
                    if ("emails-per-period".equalsIgnoreCase(drop.vmtasEmailsProcces)) {
                        if (drop.numberOfEmails == 0) {
                            throw new Exception("Number of Emails for Period is 0 !");
                        }
                        drop.batch = 1;
                        final String emailsPeriodType = drop.emailsPeriodType;
                        switch (emailsPeriodType) {
                            case "seconds": {
                                drop.emailsPeriodValue *= 1000;
                                break;
                            }
                            case "minutes": {
                                drop.emailsPeriodValue = drop.emailsPeriodValue * 60 * 1000;
                                break;
                            }
                            case "hours": {
                                drop.emailsPeriodValue = drop.emailsPeriodValue * 60 * 60 * 1000;
                                break;
                            }
                        }
                        drop.delay = (int)Math.ceil(drop.emailsPeriodValue / drop.numberOfEmails);
                    }
                    drop.sponsorId = TypesParser.safeParseInt(String.valueOf(Mapper.getMapValue(data, "sponsor", "0")));
                    drop.sponsor = new Sponsor(drop.sponsorId);
                    drop.offerId = TypesParser.safeParseInt(String.valueOf(Mapper.getMapValue(data, "offer", "0")));
                    drop.offer = new Offer(drop.offerId);
                    drop.creativeId = TypesParser.safeParseInt(String.valueOf(Mapper.getMapValue(data, "creative", "0")));
                    drop.fromNameId = TypesParser.safeParseInt(String.valueOf(Mapper.getMapValue(data, "from-name-id", "0")));
                    drop.fromName = String.valueOf(Mapper.getMapValue(data, "from-name-text", ""));
                    if ("".equals(drop.fromName) && drop.fromNameId > 0) {
                        drop.fromNameObject = new OfferName(drop.fromNameId);
                        if (drop.fromNameObject != null && drop.fromNameObject.value != null) {
                            drop.fromName = drop.fromNameObject.value;
                        }
                    }
                    drop.subjectId = TypesParser.safeParseInt(String.valueOf(Mapper.getMapValue(data, "subject-id", "0")));
                    drop.subject = String.valueOf(Mapper.getMapValue(data, "subject-text", ""));
                    if ("".equals(drop.subject) && drop.subjectId > 0) {
                        drop.subjectObject = new OfferSubject(drop.subjectId);
                        if (drop.subjectObject != null && drop.subjectObject.value != null) {
                            drop.subject = drop.subjectObject.value;
                        }
                    }
                    drop.headersRotation = 1;
                    drop.headers = Arrays.copyOf(((List)Mapper.getMapValue(data, "headers", new ArrayList())).toArray(), ((List)Mapper.getMapValue(data, "headers", new ArrayList())).toArray().length, (Class<? extends String[]>)String[].class);
                    drop.bounceEmail = String.valueOf(Mapper.getMapValue(data, "bounce-email", ""));
                    drop.returnPath = String.valueOf(Mapper.getMapValue(data, "return-path", ""));
                    if (!drop.bounceEmail.contains("@") && !drop.returnPath.contains("@")) {
                        drop.returnPath = (("".equals(drop.bounceEmail) || "".equals(drop.returnPath)) ? "" : (drop.bounceEmail + "@" + drop.returnPath));
                        drop.bounceEmail = drop.returnPath;
                    }
                    drop.fromEmail = String.valueOf(Mapper.getMapValue(data, "from-email", "from@[domain]"));
                    drop.replyTo = String.valueOf(Mapper.getMapValue(data, "reply-to", "reply@[domain]"));
                    drop.received = String.valueOf(Mapper.getMapValue(data, "received", ""));
                    drop.to = String.valueOf(Mapper.getMapValue(data, "to", "[email]"));
                    drop.placeholdersRotation = TypesParser.safeParseInt(String.valueOf(Mapper.getMapValue(data, "placeholders-rotation", "1")));
                    drop.placeholders = ("".equalsIgnoreCase(String.valueOf(Mapper.getMapValue(data, "body-placeholders", ""))) ? new String[0] : String.valueOf(Mapper.getMapValue(data, "body-placeholders", "")).split("\r\n"));
                    drop.hasPlaceholders = (drop.placeholders.length > 0);
                    drop.uploadImages = "on".equalsIgnoreCase(String.valueOf(Mapper.getMapValue(data, "upload-images", "off")));
                    drop.charset = String.valueOf(Mapper.getMapValue(data, "charset", "utf-8"));
                    drop.contentTransferEncoding = String.valueOf(Mapper.getMapValue(data, "content-transfer-encoding", "7bit"));
                    drop.contentType = String.valueOf(Mapper.getMapValue(data, "content-type", "text/html"));
                    drop.body = String.valueOf(Mapper.getMapValue(data, "body", ""));
                    drop.trackOpens = (drop.isSend && "on".equalsIgnoreCase(String.valueOf(Mapper.getMapValue(data, "track-opens", "off"))));
                    drop.ispId = TypesParser.safeParseInt(String.valueOf(Mapper.getMapValue(data, "isp-id", "0")));
                    drop.isp = new Isp(drop.ispId);
                    drop.staticDomain = String.valueOf(Mapper.getMapValue(data, "static-domain", ""));
                    drop.emailsSplitType = String.valueOf(Mapper.getMapValue(data, "emails-split-type", "vmtas"));
                    drop.testFrequency = TypesParser.safeParseInt(String.valueOf(Mapper.getMapValue(data, "send-test-after", "-1")));
                    drop.testEmails = String.valueOf(Mapper.getMapValue(data, "recipients-emails", "")).split("\\;");
                    drop.rcptfrom = String.valueOf(Mapper.getMapValue(data, "rcpt-from", ""));
                    drop.dataStart = TypesParser.safeParseInt(String.valueOf(Mapper.getMapValue(data, "data-start", "0")));
                    drop.dataCount = TypesParser.safeParseInt(String.valueOf(Mapper.getMapValue(data, "data-count", "0")));
                    drop.emailsPerSeeds = TypesParser.safeParseInt(String.valueOf(Mapper.getMapValue(data, "emails-per-seed", "1")));
                    drop.emailsCount = 0;
                    if (drop.isSend && !"".equals(Mapper.getMapValue(data, "lists", ""))) {
                        final String[] listsParts = String.valueOf(Mapper.getMapValue(data, "lists", "")).split("\\,");
                        if (listsParts != null && listsParts.length > 0) {
                            drop.lists = new HashMap<Integer, String>();
                            for (final String tmp : listsParts) {
                                drop.lists.put(TypesParser.safeParseInt(String.valueOf(tmp.split("\\|")[0])), String.valueOf(tmp.split("\\|")[1]));
                            }
                        }
                    }
                    drop.listsCount = ((drop.lists != null) ? drop.lists.size() : 0);
                    if (drop.isSend) {
                        drop.isAutoResponse = "on".equalsIgnoreCase(String.valueOf(Mapper.getMapValue(data, "auto-response", "off")));
                        drop.randomCaseAutoResponse = "on".equalsIgnoreCase(String.valueOf(Mapper.getMapValue(data, "random-case-auto-response", "off")));
                        drop.autoResponseRotation = TypesParser.safeParseInt(String.valueOf(Mapper.getMapValue(data, "auto-response-frequency", "0")));
                        drop.autoReplyEmails = (String[])("".equals(String.valueOf(Mapper.getMapValue(data, "auto-reply-emails", ""))) ? null : String.valueOf(Mapper.getMapValue(data, "auto-reply-emails", "")).split("\n"));
                    }
                    drop.redirectFileName = "r.php";
                    drop.optoutFileName = "optout.php";
                    final String dataSourcePath = new File(System.getProperty("base.path")).getAbsolutePath() + "/applications/bluemail/configs/application.ini";
                    if (new File(new File(System.getProperty("base.path")).getAbsolutePath() + "/applications/bluemail/configs/application.ini").exists()) {
                        final HashMap<String, String> map = Mapper.readProperties(dataSourcePath);
                        if (map != null && !map.isEmpty()) {
                            if (map.containsKey("redirect_file")) {
                                drop.redirectFileName = String.valueOf(map.get("redirect_file"));
                            }
                            if (map.containsKey("optout_file")) {
                                drop.optoutFileName = String.valueOf(map.get("optout_file"));
                            }
                        }
                    }*/
                }
            }
            catch (Exception e) {
                log.error(e.getMessage());
            }
        }
        return drop;
    }

    public static String[] getAllRandomTags(final String content) {
        String[] tags = new String[0];
        final Pattern p = Pattern.compile("\\[(.*?)\\]");
        final Matcher m = p.matcher(content);
        while (m.find()) {
            final String match = m.group(1);
            final String tag = match.replaceAll("[0-9]", "");
            if ("a".equalsIgnoreCase(tag) || "an".equalsIgnoreCase(tag) || "al".equalsIgnoreCase(tag) || "au".equalsIgnoreCase(tag) || "anl".equalsIgnoreCase(tag) || "anu".equalsIgnoreCase(tag) || "n".equalsIgnoreCase(tag)) {
                tags = (String[]) ArrayUtils.add((Object[])tags, (Object)match);
            }
        }
        return tags;
    }

}

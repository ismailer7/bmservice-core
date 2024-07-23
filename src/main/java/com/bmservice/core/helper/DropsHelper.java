package com.bmservice.core.helper;

import com.bmservice.core.BmServiceCoreConstants;
import com.bmservice.core.component.DropComponent;
import com.bmservice.core.exception.BmServiceCoreException;
import com.bmservice.core.mapper.system.ServerMapper;
import com.bmservice.core.mapper.system.VmtaMapper;
import com.bmservice.core.models.admin.*;
import com.bmservice.core.remote.SSH;
import com.bmservice.core.utils.Strings;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Component;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Component
@AllArgsConstructor
public class DropsHelper {

    private final ObjectMapper objectMapper;

    private final ServerMapper serverMapper;

    private final VmtaMapper vmtaMapper;

    public static void saveDrop(final DropComponent dropComponent, final Server server) throws Exception {
        /*final Drop drop = new Drop();
        drop.id = 0;
        drop.pids = dropComponent.pickupsFolder + File.separator + "drop_status_" + server.id;
        drop.userId = dropComponent.mailerId;
        drop.serverId = server.id;
        drop.ispId = dropComponent.ispId;
        drop.status = "in-progress";
        drop.startTime = new Timestamp(System.currentTimeMillis());
        drop.finishTime = null;
        drop.totalEmails = dropComponent.emailsCount;
        drop.sentProgress = 0;
        drop.offerId = dropComponent.offerId;
        drop.offerFromNameId = dropComponent.fromNameId;
        drop.offerSubjectId = dropComponent.subjectId;
        drop.recipientsEmails = String.join(",", (CharSequence[])dropComponent.testEmails);
        drop.header = new String(Base64.encodeBase64(String.join("\n\n", (CharSequence[])dropComponent.headers).getBytes()));
        drop.creativeId = dropComponent.creativeId;
        String[] lists = new String[0];
        for (final Map.Entry<Integer, String> en : dropComponent.lists.entrySet()) {
            lists = (String[])ArrayUtils.add((Object[])lists, (Object)String.valueOf(en.getValue()));
        }
        drop.lists = String.join("|", (CharSequence[])lists);
        drop.postData = new String(Base64.encodeBase64(dropComponent.content.getBytes()));
        dropComponent.id = drop.insert();
        if (dropComponent.id == 0) {
            throw new Exception("Error While Saving Drop !");
        }*/
    }

    public static void saveDropVmta(final DropComponent dropComponent, final Vmta vmta, final int totalSent) throws Exception {
        /*final DropIp dropIp = new DropIp();
        dropIp.id = 0;
        dropIp.serverId = vmta.serverId;
        dropIp.ispId = dropComponent.ispId;
        dropIp.dropId = dropComponent.id;
        dropIp.ipId = vmta.ipId;
        dropIp.dropDate = new Timestamp(System.currentTimeMillis());
        dropIp.totalSent = totalSent;
        dropIp.delivered = 0;
        dropIp.bounced = 0;
        if (dropIp.insert() == 0) {
            throw new Exception("Error While Saving Drop Ip !");
        }*/
    }

    public DropComponent parseDropFile(String content) {
        DropComponent drop = null;
        if (!"".equalsIgnoreCase(content)) {
            try {
                var data = objectMapper.readValue(content, TreeMap.class);
                if (data != null && !data.isEmpty()) {
                    var dropId = TypesParser.safeParseInt(String.valueOf(Mapper.getMapValue(data, "drop-id", "0")));
                    var isSend = "true".equalsIgnoreCase(String.valueOf(Mapper.getMapValue(data, "drop", "false")));
                    var mailerId = TypesParser.safeParseInt(String.valueOf(Mapper.getMapValue(data, "user-id", "0")));
                    var serversIds = Arrays.copyOf(((List<?>)Mapper.getMapValue(data, "servers", new ArrayList<>())).toArray(), ((List<?>)Mapper.getMapValue(data, "servers", new ArrayList<>())).toArray().length, (Class<? extends String[]>)String[].class);
                    var servers = new ArrayList<Server>();
                    if(serversIds.length > 0) {
                        servers.addAll(serverMapper.findAllIn(String.join(",", serversIds)));
                    }
                    if (servers.isEmpty()) {
                        throw new BmServiceCoreException("No Servers Found !");
                    }
                    var vmtasRotation = TypesParser.safeParseInt(String.valueOf(Mapper.getMapValue(data, "vmtas-rotation", "1")));
                    var vmtasIds = Arrays.copyOf(((List<?>)Mapper.getMapValue(data, "selected-vmtas", new ArrayList<>())).toArray(), ((List<?>)Mapper.getMapValue(data, "selected-vmtas", new ArrayList<>())).toArray().length, (Class<? extends String[]>)String[].class);
                    List<Vmta> vmtas = new ArrayList<>();
                    if (vmtasIds.length > 0) {
                        var ids = new StringBuilder();
                        for (final String vmtasId : vmtasIds) {
                            if (vmtasId != null && vmtasId.contains("|")) {
                                ids.append(vmtasId.split("\\|")[1]);
                                ids.append(",");
                            }
                        }
                        var idsList = ids.substring(0, ids.length() - 1);
                        vmtas.addAll(vmtaMapper.findAllIn(idsList));
                    }
                    if (vmtas.isEmpty()) {
                        throw new BmServiceCoreException("No Vmtas Found !");
                    }
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
                            throw new BmServiceCoreException("Number of Emails for Period is 0 !");
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
                            default:
                                break;
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
                        final String[] listsParts = String.valueOf(Mapper.getMapValue(data, "lists", "")).split(",");
                        for (final String tmp : listsParts) {
                            lists.put(TypesParser.safeParseInt(String.valueOf(tmp.split("\\|")[0])), String.valueOf(tmp.split("\\|")[1]));
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
                        autoReplyEmails = "".equals(String.valueOf(Mapper.getMapValue(data, "auto-reply-emails", ""))) ? null : String.valueOf(Mapper.getMapValue(data, "auto-reply-emails", "")).split("\n");
                    }
                    var redirectFileName = "r.php";
                    var optoutFileName = "optout.php";

                   /* final String dataSourcePath = new File(System.getProperty("base.path")).getAbsolutePath() + "/applications/bluemail/configs/application.ini";
                    if (new File(new File(System.getProperty("base.path")).getAbsolutePath() + "/applications/bluemail/configs/application.ini").exists()) {
                        final HashMap<String, String> map = Mapper.readProperties(dataSourcePath);
                        if (!map.isEmpty()) {
                            if (map.containsKey("redirect_file")) {
                                redirectFileName = String.valueOf(map.get("redirect_file"));
                            }
                            if (map.containsKey("optout_file")) {
                                optoutFileName = String.valueOf(map.get("optout_file"));
                            }
                        }
                    }*/

                    drop = DropComponent.builder().id(dropId).isNewDrop(dropId == 0).isSend(isSend).content(content).mailerId(mailerId).randomTags(getAllRandomTags(content))
                            .serversIds(serversIds).servers(servers).vmtas(vmtas).vmtasRotation(vmtasRotation).vmtasIds(vmtasIds).vmtasEmailsProcces(vmtasEmailsProcess)
                            .batch(batch).delay(delay).numberOfEmails(numberOfEmails).emailsPeriodValue(emailsPeriodValue).emailsPeriodType(emailsPeriodType)
                            .sponsorId(sponsorId).sponsor(Sponsor.builder().id(sponsorId).build()).offerId(offerId).offer(Offer.builder().id(offerId).build()).creativeId(creativeId).fromNameId(fromNameId).fromName(fromName).subjectId(subjectId).subject(subject)
                            .bounceEmail(bounceEmail).returnPath(returnPath).headersRotation(headersRotation).headers(headers).fromEmail(fromEmail).replyTo(replyTo).received(received).to(to)
                            .placeholdersRotation(placeholdersRotation).placeholders(placeholders).hasPlaceholders(hasPlaceholders).uploadImages(uploadImages).charset(charset).contentTransferEncoding(contentTransferEncoding).content(contentType).body(body)
                            .trackOpens(trackOpens).ispId(ispId).isp(isp).staticDomain(staticDomain).emailsSplitType(emailsSplitType).testFrequency(testFrequency).testEmails(testEmails).rcptfrom(rcptfrom).dataStart(dataStart).dataCount(dataCount).emailsPerSeeds(emailsPerSeeds).emailsCount(emailsCount)
                            .lists(lists).listsCount(listsCount).isAutoResponse(isAutoResponse).randomCaseAutoResponse(randomCaseAutoResponse).autoResponseRotation(autoResponseRotation).autoReplyEmails(autoReplyEmails).redirectFileName(redirectFileName).optoutFileName(optoutFileName).build();
                }
            }
            catch (Exception e) {
                throw new BmServiceCoreException(e.getMessage());
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
            final String tag = match.replaceAll("\\d", "");
            if ("a".equalsIgnoreCase(tag) || "an".equalsIgnoreCase(tag) || "al".equalsIgnoreCase(tag) || "au".equalsIgnoreCase(tag) || "anl".equalsIgnoreCase(tag) || "anu".equalsIgnoreCase(tag) || "n".equalsIgnoreCase(tag)) {
                tags = (String[]) ArrayUtils.add((Object[])tags, (Object)match);
            }
        }
        return tags;
    }

    public static String replaceRandomTags(String value, final String[] randomTags) {
        if (value != null && !"".equals(value) && randomTags != null && randomTags.length > 0) {
            for (final String randomTag : randomTags) {
                if (value.contains(randomTag)) {
                    value = StringUtils.replace(value, "[" + randomTag + "]", replaceRandomTag(randomTag));
                }
            }
        }
        return value;
    }

    public static String replaceRandomTag(final String tag) {
        final int size = TypesParser.safeParseInt(tag.replaceAll("[^0-9]", ""));
        final String replaceAll;
        final String type = replaceAll = tag.replaceAll("[0-9]", "");
        switch (replaceAll) {
            case "a": {
                return Strings.getSaltString(size, true, true, false, false);
            }
            case "al": {
                return Strings.getSaltString(size, true, true, false, false).toLowerCase();
            }
            case "au": {
                return Strings.getSaltString(size, true, true, false, false).toUpperCase();
            }
            case "an": {
                return Strings.getSaltString(size, true, true, true, false);
            }
            case "anl": {
                return Strings.getSaltString(size, true, true, true, false).toLowerCase();
            }
            case "anu": {
                return Strings.getSaltString(size, true, true, true, false).toUpperCase();
            }
            case "n": {
                return Strings.getSaltString(size, false, false, true, false);
            }
            default: {
                return "";
            }
        }
    }

    public static void uploadImage(final DropComponent drop, final SSH ssh) {
        if (drop != null && !"".equalsIgnoreCase(drop.getBody()) && ssh != null && ssh.isConnected()) {
            try {
                final Document doc = Jsoup.parse(drop.getBody());
                final Elements images = doc.select("img");
                ByteArrayOutputStream out = null;
                for (final Element image : images) {
                    final String src = image.attr("src");
                    final URL url = new URL(src);
                    final URLConnection uc = url.openConnection();
                    uc.addRequestProperty("User-Agent", "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.0)");
                    uc.connect();
                    final InputStream inStream = uc.getInputStream();
                    if (inStream != null && inStream.available() > 0) {
                        try (final InputStream in = new BufferedInputStream(inStream)) {
                            out = new ByteArrayOutputStream();
                            final byte[] buf = new byte[1024];
                            int n = 0;
                            while (-1 != (n = in.read(buf))) {
                                out.write(buf, 0, n);
                            }
                            out.close();
                        }
                        if (out.size() <= 0) {
                            continue;
                        }
                        final byte[] response = out.toByteArray();
                        if (response == null || response.length <= 0 || !new File(System.getProperty("base.path") + "/tmp/").exists()) {
                            continue;
                        }
                        final String extension = src.substring(src.lastIndexOf("."));
                        final String imageName = Strings.getSaltString(20, true, true, true, false) + extension;
                        try (final FileOutputStream fos = new FileOutputStream(System.getProperty("base.path") + "/tmp/" + imageName)) {
                            fos.write(response);
                        }
                        if (!new File(System.getProperty("base.path") + "/tmp/" + imageName).exists()) {
                            continue;
                        }
                        ssh.uploadFile(System.getProperty("base.path") + "/tmp/" + imageName, "/var/www/html/img/" + imageName);
                        var newBody = StringUtils.replace(drop.getBody(), src, "http://[domain]/img/" + imageName);
                        drop.setBody(newBody);
                        new File(BmServiceCoreConstants.BASE_PATH + "/tmp/" + imageName).delete();
                    }
                }
            }
            catch (Exception ex) {}
        }
    }


    public static void writeThreadStatusFile(final int serverId, final String folder) {
        try {
            FileUtils.writeStringToFile(new File(folder + File.separator + "drop_status_" + serverId), "0");
        }
        catch (Exception e) {
            log.error(e.getMessage());
        }
    }

    public static boolean hasToStopDrop(final int serverId, final String folder) {
        boolean stop = false;
        try {
            stop = String.valueOf(FileUtils.readFileToString(new File(folder + File.separator + "drop_status_" + serverId))).trim().contains("1");
            if (stop) {
                System.out.println("Stoped !");
            }
        }
        catch (Exception e) {
            log.error(e.getMessage());
        }
        return stop;
    }

}

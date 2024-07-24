package com.bmservice.core.worker;

import com.bmservice.core.component.DropComponent;
import com.bmservice.core.exception.DatabaseException;
import com.bmservice.core.helper.DropsHelper;
import com.bmservice.core.helper.TypesParser;
import com.bmservice.core.models.admin.Server;
import com.bmservice.core.models.admin.Vmta;
import com.bmservice.core.models.lists.Fresh;
import com.bmservice.core.security.Crypto;
import com.bmservice.core.service.DropService;
import com.bmservice.core.utils.Domains;
import com.bmservice.core.utils.Strings;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.net.QuotedPrintableCodec;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;

@Slf4j
@Data
public class PickupWorker implements Runnable {

    private int index;
    private DropComponent drop;
    private Server server;
    private List<LinkedHashMap<String, Object>> emails;
    private Vmta defaultVmta;

    public PickupWorker(final int index, final DropComponent drop, final Server server, final List<LinkedHashMap<String, Object>> emails, final Vmta defaultVmta) {
        this.index = index;
        this.drop = drop;
        this.server = server;
        this.emails = emails;
        this.defaultVmta = defaultVmta;
    }

    @Override
    public void run() {
        log.info("Starting PickupWorker number {}", this.index + 1);
        try {
            final StringBuilder pickup = new StringBuilder();
            int globalCounter = 0;
            int pickupTotal = 0;
            if (this.emails != null && !this.emails.isEmpty() && !this.drop.isStoped()) {
                final StringBuilder messageIdBuilder = new StringBuilder();
                Fresh email = this.createEmailObject(this.emails.get(0));
                Vmta vmta = (this.defaultVmta != null) ? this.defaultVmta : (Vmta) this.drop.getVmtasRotator().getList().get(0);
                final String messageId = "<" + messageIdBuilder.append(messageIdBuilder.hashCode()).append('.').append(Strings.getSaltString(13, false, false, true, false)).append('.').append(System.currentTimeMillis()).append('@').append(vmta.getDomain()).toString() + ">";
                final String mailDate = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss Z").format(new Date());
                final String globalBounceEmail = this.replaceTags(this.drop.getBounceEmail(), vmta, email, mailDate, messageId, this.drop.getStaticDomain(), this.drop.getId(), this.drop.getMailerId());
                pickup.append("XACK ON \n");
                pickup.append("XMRG FROM: <");
                pickup.append(globalBounceEmail);
                pickup.append(">\n");
                for (final LinkedHashMap<String, Object> row : this.emails) {
                    email = this.createEmailObject(row);
                    vmta = ((this.defaultVmta != null) ? this.defaultVmta : this.drop.getCurrentVmta());
                    this.createMailMerge(email, vmta, pickup);
                    globalCounter = this.drop.updateCounter();
                    if (this.drop.isSend() && globalCounter > 0 && globalCounter % this.drop.getTestFrequency() == 0 && this.drop.getTestEmails() != null && this.drop.getTestEmails().length > 0) {
                        for (final String testEmail : this.drop.getTestEmails()) {
                            email = new Fresh();
                            //email.setSchema("");
                            //email.setTable("");
                            email.setEmail(testEmail.trim());
                            email.setFname(testEmail.trim().split("\\@")[0]);
                            email.setLname(testEmail.trim().split("\\@")[0]);
                            this.createMailMerge(email, vmta, pickup);
                        }
                    }
                    DropService.rotatePlaceHolders();
                    ++pickupTotal;
                }
                final String header = new String(String.valueOf(DropService.getCurrentHeader()).getBytes());
                String body = new String(this.drop.getBody().getBytes());
                pickup.append("XPRT 1 LAST \n");
                pickup.append(header);
                pickup.append("\n");
                if (this.drop.isSend() && this.drop.isTrackOpens()) {
                    pickup.append("<img alt='' src='http://[domain]/[open]' width='1px' height='1px' style='visibility:hidden'/>");
                    pickup.append("\n");
                }
                if (!"".equals(body)) {
                    final String contentTransferEncoding = this.drop.getContentTransferEncoding();
                    switch (contentTransferEncoding) {
                        case "Quoted-Printable": {
                            body = new QuotedPrintableCodec().encode(body);
                            break;
                        }
                        case "base64": {
                            body = new String(Base64.encodeBase64(body.getBytes()));
                            break;
                        }
                    }
                }
                pickup.append("\n");
                pickup.append(body);
                pickup.append("\n.\n");
                DropService.rotateHeaders();
                FileUtils.writeStringToFile(new File(this.drop.getPickupsFolder() + File.separator + "pickup_" + this.index + "_" + pickupTotal + "_" + Strings.getSaltString(8, true, true, true, false) + ".txt"), pickup.toString());
            }
        }
        catch (Exception e) {
            log.error(e.getMessage());
        }
    }

    public synchronized void createMailMerge(final Fresh email, final Vmta vmta, final StringBuilder pickup) throws Exception {
        if (email != null && !"".equals(email.getEmail().trim()) && vmta != null) {
            String url = "r.php?t=c&d=" + this.drop.getId() + "&l=" + email.getListId() + "&c=" + email.getId();
            String unsub = "r.php?t=u&d=" + this.drop.getId() + "&l=" + email.getListId() + "&c=" + email.getId();
            String optout = "opt.php?d=" + this.drop.getId() + "&l=" + email.getListId() + "&c=" + email.getId() + "&em=" + Crypto.md5(email.getEmail());
            if (!this.drop.isSend() || email.getTable().contains("seeds")) {
                url = "r.php?t=c&d=0&l=0&c=0&cr=" + this.drop.getCreativeId();
                unsub = "r.php?t=u&d=0&l=0&c=0&cr=" + this.drop.getCreativeId();
                optout = "opt.php?d=0&l=0&c=0&em=" + Crypto.md5(email.getEmail());
            }
            if ("Quoted-Printable".equalsIgnoreCase(this.drop.getContentTransferEncoding())) {
                url = new QuotedPrintableCodec().encode(url);
                unsub = new QuotedPrintableCodec().encode(unsub);
                optout = new QuotedPrintableCodec().encode(optout);
            }
            final StringBuilder messageIdBuilder = new StringBuilder();
            final String messageId = "<" + messageIdBuilder.append(messageIdBuilder.hashCode()).append('.').append(Strings.getSaltString(13, false, false, true, false)).append('.').append(System.currentTimeMillis()).append('@').append(vmta.getDomain()).toString() + ">";
            final String mailDate = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss Z").format(new Date());
            final String bounceEmail = this.replaceTags(this.drop.getBounceEmail(), vmta, email, mailDate, messageId, this.drop.getStaticDomain(), this.drop.getId(), this.drop.getMailerId());
            final String returnPath = this.replaceTags(this.drop.getReturnPath(), vmta, email, mailDate, messageId, this.drop.getStaticDomain(), this.drop.getId(), this.drop.getMailerId());
            final String fromEmail = this.replaceTags(this.drop.getFromEmail(), vmta, email, mailDate, messageId, this.drop.getStaticDomain(), this.drop.getId(), this.drop.getMailerId());
            final String replyTo = this.replaceTags(this.drop.getReplyTo(), vmta, email, mailDate, messageId, this.drop.getStaticDomain(), this.drop.getId(), this.drop.getMailerId());
            final String to = this.replaceTags(this.drop.getTo(), vmta, email, mailDate, messageId, this.drop.getStaticDomain(), this.drop.getId(), this.drop.getMailerId());
            final String received = StringUtils.replace(this.replaceTags(this.drop.getReceived(), vmta, email, mailDate, messageId, this.drop.getStaticDomain(), this.drop.getId(), this.drop.getMailerId()), "[return_path]", returnPath);
            final String fromName = this.replaceTags(this.drop.getFromName(), vmta, email, mailDate, messageId, this.drop.getStaticDomain(), this.drop.getId(), this.drop.getMailerId());
            final String subject = this.replaceTags(this.drop.getSubject(), vmta, email, mailDate, messageId, this.drop.getStaticDomain(), this.drop.getId(), this.drop.getMailerId());
            final String autoResponseEmail = "";
            pickup.append("XDFN ");
            pickup.append("rcpt=\"");
            pickup.append(email.getEmail());
            pickup.append("\" ");
            pickup.append("mail_date=\"");
            pickup.append(mailDate);
            pickup.append("\" ");
            pickup.append("message_id=\"");
            pickup.append(messageId);
            pickup.append("\" ");
            pickup.append("ip=\"");
            pickup.append(vmta.getIpValue());
            pickup.append("\" ");
            pickup.append("vmta_name=\"");
            pickup.append(vmta.getName());
            pickup.append("\" ");
            pickup.append("smtphost=\"");
            pickup.append(vmta.getSmtphost());
            pickup.append("\" ");
            pickup.append("username=\"");
            pickup.append(vmta.getUsername());
            pickup.append("\" ");
            pickup.append("password=\"");
            pickup.append(vmta.getPassword());
            pickup.append("\" ");
            if (this.drop.getStaticDomain() != null && !"".equalsIgnoreCase(this.drop.getStaticDomain())) {
                pickup.append("rdns=\"");
                pickup.append(this.drop.getStaticDomain());
                pickup.append("\" ");
                pickup.append("domain=\"");
                pickup.append(this.drop.getStaticDomain());
                pickup.append("\" ");
            }
            else {
                pickup.append("rdns=\"");
                pickup.append(vmta.getDomain());
                pickup.append("\" ");
                pickup.append("domain=\"");
                pickup.append(Domains.getDomainName(vmta.getDomain()));
                pickup.append("\" ");
            }
            pickup.append("server=\"");
            pickup.append(this.server.getName());
            pickup.append("\" ");
            pickup.append("mailer_id=\"");
            pickup.append(this.drop.getMailerId());
            pickup.append("\" ");
            pickup.append("drop_id=\"");
            pickup.append(this.drop.getId());
            pickup.append("\" ");
            pickup.append("list_id=\"");
            pickup.append(email.getListId());
            pickup.append("\" ");
            pickup.append("email_id=\"");
            pickup.append(email.getId());
            pickup.append("\" ");
            pickup.append("email=\"");
            pickup.append(email.getEmail());
            pickup.append("\" ");
            pickup.append("auto_reply_email=\"");
            pickup.append(autoResponseEmail);
            pickup.append("\" ");
            pickup.append("fname=\"");
            pickup.append(email.getFname());
            pickup.append("\" ");
            pickup.append("lname=\"");
            pickup.append(email.getLname());
            pickup.append("\" ");
            pickup.append("email_name=\"");
            pickup.append(email.getEmail().split("\\@")[0]);
            pickup.append("\" ");
            pickup.append("from_email=\"");
            pickup.append(fromEmail);
            pickup.append("\" ");
            pickup.append("return_path=\"");
            pickup.append(returnPath);
            pickup.append("\" ");
            pickup.append("reply_to=\"");
            pickup.append(replyTo);
            pickup.append("\" ");
            pickup.append("to=\"");
            pickup.append(to);
            pickup.append("\" ");
            pickup.append("received=\"");
            pickup.append(received);
            pickup.append("\" ");
            pickup.append("from_name=\"");
            pickup.append(fromName);
            pickup.append("\" ");
            pickup.append("subject=\"");
            pickup.append(subject);
            pickup.append("\" ");
            pickup.append("content_transfer_encoding=\"");
            pickup.append(this.drop.getContentTransferEncoding());
            pickup.append("\" ");
            pickup.append("content_type=\"");
            pickup.append(this.drop.getContentType());
            pickup.append("\" ");
            pickup.append("charset=\"");
            pickup.append(this.drop.getCharset());
            pickup.append("\" ");
            if (this.drop.isSend() && this.drop.isTrackOpens()) {
                String openUrl = "r.php?t=o&d=" + this.drop.getId() + "&l=" + email.getListId() + "&c=" + email.getId();
                if ("Quoted-Printable".equalsIgnoreCase(this.drop.getContentTransferEncoding())) {
                    openUrl = new QuotedPrintableCodec().encode(openUrl);
                }
                pickup.append("open=\"");
                pickup.append(openUrl);
                pickup.append("\" ");
            }
            pickup.append("url=\"");
            pickup.append(url);
            pickup.append("\" ");
            pickup.append("unsub=\"");
            pickup.append(unsub);
            pickup.append("\" ");
            pickup.append("optout=\"");
            pickup.append(optout);
            pickup.append("\" ");
            if (this.drop.isHasPlaceholders()) {
                pickup.append("placeholder=\"");
                pickup.append(DropService.getCurrentPlaceHolder());
                pickup.append("\" ");
            }
            if (this.drop.getRandomTags() != null && this.drop.getRandomTags().length > 0) {
                for (final String randomTag : this.drop.getRandomTags()) {
                    pickup.append(randomTag).append("=\"");
                    pickup.append(DropsHelper.replaceRandomTag(randomTag));
                    pickup.append("\" ");
                }
            }
            pickup.append("\n");
            pickup.append("XDFN *vmta=\"");
            pickup.append(vmta.getName());
            pickup.append("\" *jobId=\"");
            pickup.append(this.drop.getMailerId());
            pickup.append("\" *from=\"");
            pickup.append(bounceEmail);
            pickup.append("\" *envId=\"");
            pickup.append(this.drop.getId());
            pickup.append("_");
            pickup.append(vmta.getIpId());
            pickup.append("_");
            pickup.append(email.getId());
            pickup.append("_");
            pickup.append(email.getListId());
            pickup.append("\"\n");
            pickup.append("RCPT TO:<");
            if (!"".equalsIgnoreCase(this.drop.getRcptfrom())) {
                pickup.append(this.drop.getRcptfrom());
            }
            else {
                pickup.append(email.getEmail());
            }
            pickup.append(">");
            pickup.append("\n");
        }
    }


    public Fresh createEmailObject(final LinkedHashMap<String, Object> row) throws DatabaseException {
        final Fresh email = new Fresh();
        if (!"".equalsIgnoreCase(String.valueOf(row.get("table")))) {
            email.setSchema(String.valueOf(row.get("table")).split("\\.")[0]);
            email.setTable(String.valueOf(row.get("table")).split("\\.")[1]);
        }
        else {
            email.setSchema("");
            email.setTable("");
        }
        email.setEmail(String.valueOf(row.get("email")));
        email.setFname(((email.getFname() == null || "".equals(email.getFname()) || "null".equalsIgnoreCase(email.getFname())) ? email.getEmail().split("\\@")[0] : email.getFname()));
        email.setLname(((email.getLname() == null || "".equals(email.getLname()) || "null".equalsIgnoreCase(email.getLname())) ? email.getFname() : email.getLname()));
        email.setListId(TypesParser.safeParseInt(String.valueOf(row.get("list_id"))));
        return email;
    }

    public String replaceTags(final String value, final Vmta vmta, final Fresh email, final String mailDate, final String messageId, final String staticDomain, final int dropId, final int mailerId) {
        String val = value;
        if (value != null && !"".equals(value)) {
            val = StringUtils.replace(value, "[drop_id]", String.valueOf(dropId));
            val = StringUtils.replace(value, "[mailer_id]", String.valueOf(mailerId));
            val = StringUtils.replace(value, "[ip]", vmta.getIpValue());
            val = StringUtils.replace(val, "[smtphost]", vmta.getSmtphost());
            val = StringUtils.replace(val, "[username]", vmta.getUsername());
            val = StringUtils.replace(val, "[password]", vmta.getPassword());
            if (staticDomain != null && !"".equalsIgnoreCase(staticDomain)) {
                val = StringUtils.replace(val, "[rdns]", staticDomain);
                val = StringUtils.replace(val, "[domain]", staticDomain);
            }
            else {
                val = StringUtils.replace(val, "[rdns]", vmta.getDomain().toUpperCase());
                val = StringUtils.replace(val, "[domain]", Domains.getDomainName(vmta.getDomain()));
            }

            if (email != null) {
                val = StringUtils.replace(val, "[email_id]", String.valueOf(email.getId()));
                val = StringUtils.replace(val, "[list_id]", String.valueOf(email.getListId()));
                val = StringUtils.replace(val, "[email]", email.getEmail());
                val = StringUtils.replace(val, "[fname]", email.getFname());
                val = StringUtils.replace(val, "[lname]", email.getLname());
                val = StringUtils.replace(val, "[email_name]", email.getEmail().split("\\@")[0]);
            }
            if (mailDate != null && !"".equalsIgnoreCase(mailDate)) {
                val = StringUtils.replace(val, "[mail_date]", mailDate);
            }
            if (messageId != null && !"".equalsIgnoreCase(messageId)) {
                val = StringUtils.replace(val, "[message_id]", messageId);
            }
            val = StringUtils.replace(val, "[placeholder]", DropService.getCurrentPlaceHolder());
            val = DropsHelper.replaceRandomTags(val, this.drop.getRandomTags());
        }
        return val;
    }

}

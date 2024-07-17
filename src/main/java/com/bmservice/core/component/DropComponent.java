package com.bmservice.core.component;

import com.bmservice.core.models.admin.*;
import com.bmservice.core.models.lists.Fresh;
import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
@Builder
public class DropComponent {

    private int id;
    private boolean isSend;
    private String content;
    private String[] randomTags;
    private int mailerId;
    private boolean isNewDrop;
    private boolean isStoped;
    private String[] serversIds;
    private List<Server> servers;
    private String[] vmtasIds;
    private List<Vmta> vmtas;
    private String vmtasEmailsProcces;
    private int numberOfEmails;
    private int emailsPeriodValue;
    private String emailsPeriodType;
    private int batch;
    private long delay;
    private int vmtasRotation;
    private int fromNameId;
    private String fromName;
    private OfferName fromNameObject;
    private int subjectId;
    private String subject;
    private OfferSubject subjectObject;
    private int headersRotation;
    private String[] headers;
    private String bounceEmail;
    private String fromEmail;
    private String returnPath;
    private String replyTo;
    private String received;
    private String to;
    private boolean hasPlaceholders;
    private int placeholdersRotation;
    private String[] placeholders;
    private boolean uploadImages;
    private String charset;
    private String contentTransferEncoding;
    private String contentType;
    private String body;
    private String redirectFileName;
    private String optoutFileName;
    private boolean trackOpens;
    private String staticDomain;
    private int ispId;
    private Isp isp;
    private String emailsSplitType;
    private int testFrequency;
    private String[] testEmails;
    private String rcptfrom;
    private int dataStart;
    private int dataCount;
    private int emailsCount;
    private int emailsPerSeeds;
    private Map<Integer, String> lists;
    private int listsCount;
    private List<Fresh> emails;
    private int sponsorId;
    private Sponsor sponsor;
    private int offerId;
    private Offer offer;
    private int creativeId;
    private boolean isAutoResponse;
    private boolean randomCaseAutoResponse;
    private int autoResponseRotation;
    private String[] autoReplyEmails;
    private volatile String pickupsFolder;
    private volatile int emailsCounter;
    private volatile RotatorComponent vmtasRotator;

    /*public DropComponent() {
        this.id = 0;
        this.isNewDrop = true;
        this.isStoped = false;
        this.emailsCounter = 0;
    }*/

    public synchronized int updateCounter() {
        return this.emailsCounter++;
    }

    public Vmta getCurrentVmta() {
        return (Vmta)this.vmtasRotator.getCurrentThenRotate();
    }

}

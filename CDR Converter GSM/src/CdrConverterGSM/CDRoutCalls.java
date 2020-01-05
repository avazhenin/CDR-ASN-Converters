/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package CdrConverterGSM;

/**
 *
 * @author home
 */
public class CDRoutCalls {

    String recordType;
    String servedIMSI;
    String servedIMEI;
    String servedMSISDN;
    String callingNumber;
    String calledNumber;
    String forwardNumber;
    String roamingNumber;
    String mscIncomingROUTE;
    String mscOutgoingROUTE;
    String bsc_a;
    String cellA;
    String cellB;
    String lacA;
    String lacB;
    String changeOfLocation;
    String seizureTime;
    String answerTime;
    String releaseTime;
    String callDuration;
    String MSCAddress;
    String callType;
    String serviceCentre;
    String callingPortedFlag;
    String calledPortedFlag;

    public CDRoutCalls() {
        String recordType = new String();
        String servedIMSI = new String();
        String servedIMEI = new String();
        String servedMSISDN = new String();
        String callingNumber = new String();
        String calledNumber = new String();
        String roamingNumber = new String();
        String mscIncomingROUTE = new String();
        String mscOutgoingROUTE = new String();
        String bsc_a = new String();
        String cellA = new String();
        String cellB = new String();
        String lacA = new String();
        String lacB = new String();
        String changeOfLocation = new String();
        String seizureTime = new String();
        String answerTime = new String();
        String releaseTime = new String();
        String callDuration = new String();
        String orgMSCId = new String();
        String callType = new String();
        String serviceCentre = new String();
        String calledPortedFlag = new String();
        String callingPortedFlag = new String();
        String forwardNumber = new String();
    }

    public CDRoutCalls(
            String recordType,
            String servedIMSI,
            String servedIMEI,
            String servedMSISDN,
            String callingNumber,
            String calledNumber,
            String forwardNumber,
            String roamingNumber,
            String mscIncomingROUTE,
            String mscOutgoingROUTE,
            String bsc_a,
            String cellA,
            String cellB,
            String lacA,
            String lacB,
            String changeOfLocation,
            String seizureTime,
            String answerTime,
            String releaseTime,
            String callDuration,
            String orgMSCId,
            String callType,
            String serviceCentre,
            String calledPortedFlag,
            String callinPortedFlag) {
        this.recordType = recordType;
        this.servedIMSI = servedIMSI;
        this.servedIMEI = servedIMEI;
        this.servedMSISDN = servedMSISDN;
        this.callingNumber = callingNumber;
        this.calledNumber = calledNumber;
        this.forwardNumber = forwardNumber;
        this.roamingNumber = roamingNumber;
        this.mscIncomingROUTE = mscIncomingROUTE;
        this.mscOutgoingROUTE = mscOutgoingROUTE;
        this.bsc_a = bsc_a;
        this.cellA = cellA;
        this.cellB = cellB;
        this.lacA = lacA;
        this.lacB = lacB;
        this.changeOfLocation = changeOfLocation;
        this.seizureTime = seizureTime;
        this.answerTime = answerTime;
        this.releaseTime = releaseTime;
        this.callDuration = callDuration;
        this.MSCAddress = orgMSCId;
        this.callType = callType;
        this.serviceCentre = serviceCentre;
        this.calledPortedFlag = calledPortedFlag;
        this.callingPortedFlag = callinPortedFlag;
    }

}

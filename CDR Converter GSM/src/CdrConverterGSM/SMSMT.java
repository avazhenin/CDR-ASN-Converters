/**
 * SMS Mobile Terminating
 */
package CdrConverterGSM;

import static CdrConverterGSM.Worker.log;
import java.io.File;

/**
 *
 * @author Vazhenin
 */
public class SMSMT {

    ASN asn = new ASN();
    int eventPos;
    Utils utils = new Utils();

    public SMSMT() {
    }

    CDRoutCalls parse(byte[] b, int spos, int epos) {
        CDRoutCalls outCols = new CDRoutCalls();

        for (eventPos = spos; eventPos <= epos; eventPos++) {
            try {
                String tag = asn.get_tag(b, eventPos);
                int tagl = asn.get_tag_length(b, eventPos);
                int tagEndPos = eventPos + (tag.length() / 2) + tagl;
                eventPos = tagEndPos - tagl; // skip to call data                
                String dataHex = getTagData(b, tagl);
                log.debug("tag " + tag + " length " + tagl + " data " + dataHex);
                switch (tag.toLowerCase().trim()) {
                    case "80":
                        outCols.recordType = dataHex;
                        break;
                    case "81":
                        outCols.serviceCentre = utils.getMsisdn(asn.TBCDSTRING(dataHex));
                        break;
                    case "82":
                        outCols.servedIMSI = utils.getImsi(asn.TBCDSTRING(dataHex));
                        break;
                    case "83":
                        outCols.servedIMEI = asn.TBCDSTRING(dataHex);
                        break;                        
                    case "84":
                        outCols.servedMSISDN = utils.getMsisdn(asn.TBCDSTRING(dataHex));
                        outCols.callingNumber = utils.getMsisdn(asn.TBCDSTRING(dataHex));
                        break;
                    case "9f8149":
                        outCols.calledNumber = utils.getMsisdn(asn.TBCDSTRING(dataHex));
                        break;                        
                    case "9f8128":
                        outCols.MSCAddress = asn.TBCDSTRING(dataHex);
                        break;                                                  
                    case "88":
                        outCols.seizureTime = utils.getDate(dataHex, "yymmddHHmmss");
                        break;                        
                    case "a7":
                        outCols.lacB = utils.getLAC(dataHex);
                        outCols.cellB = utils.getCI(dataHex);
                        break;                                                       
                }
                /**
                 * if k != tag end postition , means tag value wasnt parsed
                 */
                if (eventPos != tagEndPos) {
                    eventPos = tagEndPos;
                    log.info("Tag " + tag + " wasnt parsed properly");
                }
            } catch (Exception e) {
                log.info(e);
            }

        }

        return outCols;
    }

    String getTagData(byte[] b, int tagLength) {
        String result = new String();
        for (int l = 0; l < tagLength; l++) {
            eventPos++;
            result += asn.byte_to_hex(b[eventPos]);
        }
        return result;
    }

}

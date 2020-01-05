/**
 * Mobile Originating Call
 */
package CdrConverterGSM;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import org.apache.log4j.Logger;

/**
 *
 * @author Vazhenin
 */
public class MOC {

    ASN asn = new ASN();
    int eventPos;
    Utils utils = new Utils();
    static Logger log = Logger.getLogger(MOC.class);

    public MOC() {
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

//                if (dataHex.indexOf("0751722074")!=-1) {
//                    log.info(dataHex);
//                }                
                switch (tag.toLowerCase().trim()) {
                    case "80":
                        outCols.recordType = dataHex;
                        break;
                    case "81":
                        outCols.servedIMSI = utils.getImsi(asn.TBCDSTRING(dataHex));
                        break;
                    case "82":
                        outCols.servedIMEI = asn.TBCDSTRING(dataHex);
                        break;
                    case "83":
                        outCols.servedMSISDN = utils.getMsisdn(asn.TBCDSTRING(dataHex));
                        break;
                    case "84":
                        outCols.callingNumber = utils.getMsisdn(asn.TBCDSTRING(dataHex));
                        break;
                    case "85":
                        outCols.calledNumber = utils.getMsisdn(asn.TBCDSTRING(dataHex));
                        break;
                    case "aa":
                        outCols.mscIncomingROUTE = utils.getRoute(dataHex);
                        break;
                    case "ab":
                        outCols.mscOutgoingROUTE = utils.getRoute(dataHex);
                        break;
                    case "99":
                        outCols.callDuration = String.valueOf(Integer.parseInt(dataHex, asn.HEX_RADIX));
                        break;
                    case "9f27":
                        outCols.MSCAddress = utils.getMSC(asn.TBCDSTRING(dataHex));
                        break;
                    case "ac": // location
                        outCols.lacA = utils.getLAC(dataHex);
                        outCols.cellA = utils.getCI(dataHex);                        
                        break;
                    case "96":
                        outCols.seizureTime = utils.getDate(dataHex, "yyMMddHHmmss");
                        break;
                    case "9f817f":
                        outCols.calledPortedFlag = dataHex;
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
                e.printStackTrace();
//                log.info(dataHex,e);
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

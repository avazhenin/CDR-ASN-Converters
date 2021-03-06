/**
 * Incoming Gateway Record
 */
package CdrConverterGSM;

import static CdrConverterGSM.MOC.log;
import static CdrConverterGSM.Worker.log;
import java.io.File;
import org.apache.log4j.Logger;

/**
 *
 * @author Vazhenin
 */
public class IGR {

    ASN asn = new ASN();
    int eventPos;
    Utils utils = new Utils();
    static Logger log = Logger.getLogger(IGR.class);
    public IGR() {
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
                        outCols.callingNumber = utils.getMsisdn(asn.TBCDSTRING(dataHex));
                        break;
                    case "82":
                        outCols.calledNumber = utils.getMsisdn(asn.TBCDSTRING(dataHex));
                        break;
                    case "a4":
                        outCols.mscIncomingROUTE = utils.getRoute(dataHex);
                        break;                        
                    case "a5":
                        outCols.mscOutgoingROUTE = utils.getRoute(dataHex);
                        break;   
                    case "86":
                        outCols.seizureTime = utils.getDate(dataHex, "yyMMddHHmmss");
                        break;  
                    case "89":
                        outCols.callDuration = String.valueOf(Integer.parseInt(dataHex, asn.HEX_RADIX));
                        break;  
                    case "bf8175": // location
                        outCols.lacA = utils.getLAC(dataHex);
                        outCols.cellA = utils.getCI(dataHex);                        
                        break;  
                    case "9f816f":
                        outCols.servedIMSI = utils.getImsi(asn.TBCDSTRING(dataHex));
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

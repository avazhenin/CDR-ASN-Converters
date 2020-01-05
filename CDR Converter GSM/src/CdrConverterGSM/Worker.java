/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package CdrConverterGSM;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

/**
 *
 * @author vazhenin
 */
public class Worker {

    String cdrin_path;
    String cdrout_path;
    String logFileFullPath;
    String log4gFullPath;
    String cdroutext;
    int max_cdr_files;
    ASN asn = new ASN();
    byte[] b;

    static Logger log = Logger.getLogger(Worker.class);

    public Worker() {
    }

    public Worker(String paramFileFullPath) {

        ParseXMLUtilities xml = new ParseXMLUtilities(paramFileFullPath);
        xml.initiate();

        this.cdrin_path = xml.getNodeValue(xml.getChildNodes("parameters"), "cdrin");
        this.cdrout_path = xml.getNodeValue(xml.getChildNodes("parameters"), "cdrout");
        this.max_cdr_files = Integer.parseInt(xml.getNodeValue(xml.getChildNodes("parameters"), "maxCDRFilesToRead"));
        this.cdroutext = xml.getNodeValue(xml.getChildNodes("parameters"), "cdroutext");
        this.log4gFullPath = xml.getNodeValue(xml.getChildNodes("parameters"), "log4jPath");

        PropertyConfigurator.configure(this.log4gFullPath);
    }

    void run() {

        try {
            /* read files */
            File[] files = getFiles(cdrin_path);

            for (int i = 0; i < Math.min(files.length, max_cdr_files); i++) {/* start of reading files loop */

                File file = files[i];

                File cdr = new File(files[i].getAbsoluteFile().toString());
                BufferedInputStream bis = new BufferedInputStream(new FileInputStream(cdr));

                b = new byte[bis.available()];
                bis.read(b);

                ArrayList<CDRoutCalls> outCDRrecords = new ArrayList<CDRoutCalls>();
                IGR igr = new IGR();
                MOC moc = new MOC();
                MTC mtc = new MTC();
                SMSMO smsmo = new SMSMO();
                SMSMT smsmt = new SMSMT();
                MTR mtr = new MTR();
                CFR cfr = new CFR();
                OGR ogr = new OGR();

                for (int j = 0; j < b.length; j++) {/* start of reading CDR bytes */

                    /**
                     * Parsing Tag = Charging Data Record File
                     */
                    String charchinDataRecordFile = asn.byte_to_hex(b[j]);
                    int charchinDataRecordFileLength = asn.get_tag_length(b, j);
                    j++;
                    int skipBytes = asn.get_bitpos_decimal(1, 7, asn.get_tag_bits(b[j]));
                    j += skipBytes + 1;

                    /**
                     * Parsing Tag = Header
                     */
                    String header = asn.byte_to_hex(b[j]);
                    int headerLength = asn.get_tag_length(b, j);
                    j++;
                    skipBytes = asn.get_bitpos_decimal(1, 7, asn.get_tag_bits(b[j]));
                    j += skipBytes + headerLength;

                    String hex_tag = asn.byte_to_hex(b[j]);
                    if (asn.byte_to_hex(b[j]).indexOf("a1") == -1) {
                        log.info(hex_tag);
                    }
                    if (asn.byte_to_hex(b[j]).indexOf("a1") != -1) { // tag sequence
                        int sequenceTagLength = asn.get_tag_length(b, j);
                        int changePos = asn.get_bitpos_decimal(1, 7, asn.get_tag_bits(b[j + 1]));
                        int sequence_start = j;
                        int sequence_data_start = j + changePos + 2;
                        int sequence_stop = j + sequenceTagLength + changePos + 1;
                        log.debug("Found call sequence " + hex_tag + ". length " + sequenceTagLength + " pos " + sequence_start + " " + sequence_stop + " sequence data start pos " + sequence_data_start);

                        /**
                         * Call event records
                         */
                        for (int k = sequence_data_start; k <= sequence_stop; k++) {

//                            String eventTag = asn.byte_to_hex(b[k]);
                            String eventTag = asn.get_tag(b, k);
                            int eventTagLength = asn.get_tag_length(b, k);
                            k += eventTag.length() / 2;
                            int event_tag_data_start = k + asn.get_bitpos_decimal(1, 7, asn.get_tag_bits(b[k])) + 1;
                            int event_tag_data_stop = event_tag_data_start + eventTagLength - 1;

                            for (int l = event_tag_data_start; l <= event_tag_data_stop; l++) {
                                try {
                                    String dataHex = "";
                                    String tag = asn.get_tag(b, l);
                                    int tagl = asn.get_tag_length(b, l);
                                    int tagEndPos = l + (tag.length() / 2) + tagl;
                                    l = tagEndPos - tagl; // skip to call data

                                    /**
                                     * Parse tag 80
                                     */
                                    for (int m = 0; m < tagl; m++) {
                                        l++;
                                        dataHex += asn.byte_to_hex(b[l]);
                                    }
                                    
                                    if (dataHex.equalsIgnoreCase("00")) {
                                        // Mobile Originating Call
                                        outCDRrecords.add(moc.parse(b, event_tag_data_start, event_tag_data_stop));
                                    } else if (dataHex.equalsIgnoreCase("01")) {
                                        // Mobile Terminating Call
                                        outCDRrecords.add(mtc.parse(b, event_tag_data_start, event_tag_data_stop));
                                    } else if (dataHex.equalsIgnoreCase("05")) {
                                        // Mobile Transit record
                                        outCDRrecords.add(mtr.parse(b, event_tag_data_start, event_tag_data_stop));
                                    } else if (dataHex.equalsIgnoreCase("06")) {
                                        // SMS MO
                                        outCDRrecords.add(smsmo.parse(b, event_tag_data_start, event_tag_data_stop));
                                    } else if (dataHex.equalsIgnoreCase("07")) {
                                        // SMS MT
                                        outCDRrecords.add(smsmt.parse(b, event_tag_data_start, event_tag_data_stop));
                                    } else if (dataHex.equalsIgnoreCase("03")) {
                                        // Incoming Gateway Record
                                        outCDRrecords.add(igr.parse(b, event_tag_data_start, event_tag_data_stop));
                                    } else if (dataHex.equalsIgnoreCase("04")) {
                                        // Outgoing Gateway Record
//                                        outCDRrecords.add(ogr.parse(b, event_tag_data_start, event_tag_data_stop));
                                    } else if (dataHex.equalsIgnoreCase("64")) {
                                        // Call Forwarding Record
                                        outCDRrecords.add(cfr.parse(b, event_tag_data_start, event_tag_data_stop));
                                    } else {
                                        log.info("Unrecognized TAG " + dataHex);
                                    }

                                    /**
                                     * if k != tag end postition , means tag
                                     * value wasnt parsed
                                     */
                                    if (l != tagEndPos) {
                                        l = tagEndPos;
                                        log.info("Tag " + tag + " wasnt parsed properly");
                                    }
                                    l = event_tag_data_stop;
                                    k = l;
                                } catch (Exception e) {
                                    log.info("l=" + l, e);
                                }
                            }
                            j = k;
//                            return;
                        }
                        if (j >= sequence_stop) {
                            j = b.length; // consider that we read all the cdr sequence and must quit now
                        }
                    } // end tag sequence                    
                }
                /* end of read CDR bytes */
                new ExportCDR().export(cdrout_path, cdroutext, outCDRrecords, file.getName());

                try {
                    for (int j = 0; j < outCDRrecords.size(); j++) {
                        CDRoutCalls rec = outCDRrecords.get(j);
                        if (rec.servedMSISDN.indexOf("asd") != -1) {
                            log.info(rec.callingNumber);
                            log.info(rec.calledNumber);
                            log.info(rec.callDuration);
                            log.info(rec.servedIMEI);
                            log.info(rec.seizureTime);
                            log.info(rec.servedIMSI);
                            log.info(rec.mscIncomingROUTE);
                            log.info(rec.mscOutgoingROUTE);
                            log.info(rec.cellA);
                            log.info(rec.lacA);
                        }
                    }
                } catch (Exception e) {
                }

                b = null; // prepare bytes for new CDR
                log.info(file.getName() + " has been converted");
            }/* end of reading files loop */

        } catch (Exception e) {
            log.info("ERROR", e);
        }

    }

    public File[] getFiles(String path) {
        File f = new File(path);

        File[] cdrFilesList = f.listFiles();

        return cdrFilesList;
    }

}

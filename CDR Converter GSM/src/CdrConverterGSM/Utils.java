/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package CdrConverterGSM;

import java.math.BigInteger;
import java.text.SimpleDateFormat;
import java.util.Date;
import org.apache.log4j.Logger;

/**
 *
 * @author vazhenin
 */
public class Utils {

    static Logger log = Logger.getLogger(Utils.class);
    ASN asn = new ASN();

    String getMSC(String tbcdstring) {
        return tbcdstring.substring(2, tbcdstring.length());
    }    
    
    String getMsisdn(String tbcdstring) {
        return tbcdstring.substring(2, tbcdstring.length() - 1);
    }

    String getDate(String dataHex, String format) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
        String result = "";
        try {
            result = sdf.format((new SimpleDateFormat(format).parse(dataHex.substring(0, dataHex.length() - 6)))).toString();
        } catch (Exception e) {
            log.info(e);
        }
        return result;
    }

    String getImsi(String data) {
        return data.substring(0, data.length() - 1);
    }

    String getRoute(String data) {
        String res = "";

        try {
            int pos = 0; // initiate initial position
            byte b = asn.hex_to_byte(data.substring(pos, pos + 2));// get byte value
            int[] bits = asn.get_tag_bits(b); // get byte bits
            int lpos = asn.get_bitpos_decimal(1, 7, bits); // get number of byte we need to read for getting tag data length
            pos += 2; // step to reading tag length
            int length = Integer.parseInt(data.substring(pos, pos + lpos * 2), asn.HEX_RADIX); // get tag data length
            pos = pos + lpos * 2; // skip pos to tag data

            // concatenate chars tag data
            for (int i = 0; i < length; i++) {
                res += String.valueOf((char) Integer.parseInt(data.substring(pos, pos + 2), asn.HEX_RADIX));
                pos += 2;
            }
        } catch (Exception e) {
            log.info(data, e);
        }

        return res;
    }

    String getLAC(String tagData) {
        int pos = 0; // initiate initial position
        byte b;
        int[] bits;
        int lpos;
        int length;
        String res;

        pos = 0; // initiate initial position
        b = asn.hex_to_byte(tagData.substring(pos, pos + 2));// get byte value        
        bits = asn.get_tag_bits(b); // get byte bits
        lpos = asn.get_bitpos_decimal(1, 7, bits); // get number of byte we need to read for getting tag data length
        pos += 2; // step to reading tag length
        length = Integer.parseInt(tagData.substring(pos, pos + lpos * 2), asn.HEX_RADIX); // get tag data length
        pos = pos + lpos * 2; // skip pos to tag data
        res = String.valueOf(Integer.parseInt(tagData.substring(pos, pos + length * 2), asn.HEX_RADIX));

        return res;
    }

    // Cell Identification
    String getCI(String tagData) {
        int pos = 0; // initiate initial position
        byte b;
        int[] bits;
        int lpos;
        int length;
        String res;

        pos = 0; // initiate initial position
        b = asn.hex_to_byte(tagData.substring(pos, pos + 2));// get byte value        
        bits = asn.get_tag_bits(b); // get byte bits
        lpos = asn.get_bitpos_decimal(1, 7, bits); // get number of byte we need to read for getting tag data length
        pos += 2; // step to reading tag length
        length = Integer.parseInt(tagData.substring(pos, pos + lpos * 2), asn.HEX_RADIX); // get tag data length
        pos = pos + lpos * 2; // skip pos to tag data
        pos += length * 2;

        b = asn.hex_to_byte(tagData.substring(pos, pos + 2));// get byte value        
        bits = asn.get_tag_bits(b); // get byte bits
        lpos = asn.get_bitpos_decimal(1, 7, bits); // get number of byte we need to read for getting tag data length
        pos += 2; // step to reading tag length
        length = Integer.parseInt(tagData.substring(pos, pos + lpos * 2), asn.HEX_RADIX); // get tag data length
        pos = pos + lpos * 2; // skip pos to tag data
        res = String.valueOf(Integer.parseInt(tagData.substring(pos, pos + length * 2), asn.HEX_RADIX));
//        res = res.substring(1, res.length() - 1);
        return res;
    }

    String getCell(String tagData) {
        String cell = getCI(tagData);
        return cell.substring(cell.length() - 1, cell.length());
    }

    String getBSC(String tagData) {
        String bsc = getCI(tagData);
        return bsc.substring(0, bsc.length() - 1);
    }
}

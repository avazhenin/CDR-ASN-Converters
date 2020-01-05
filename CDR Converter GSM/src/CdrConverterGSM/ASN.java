/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package CdrConverterGSM;

import static CdrConverterGSM.Worker.log;
import org.apache.log4j.Logger;

/**
 *
 * @author vazhenin
 */
public class ASN {

    final int HEX_RADIX = 16;
    final int DEC_RADIX = 10;
    final int BIT_RADIX = 2;
    static Logger log = Logger.getLogger(ASN.class);

    public ASN() {
    }

    // returns convertion of byte to hexadecimal
    String byte_to_hex(byte b) {
        String hex = Integer.toHexString(b & 0xFF);

        if (hex.length() == 1) {
            hex = "0" + hex;
        }
//        log.debug(hex);
        return hex;
    }

    byte hex_to_byte(String hex) {
        return (byte) Integer.parseInt(hex, HEX_RADIX);
    }

    /**
     * returns arrays of integers , size = 8, each array pos has bit value
     * Example: declare int[] bits bits[0]=1 bits[1]=0 bits[2]=0 bits[3]=0
     * bits[4]=0 bits[5]=0 bits[6]=1 bits[7]=1
     */
    int[] get_tag_bits(byte b) {
        int[] bits = new int[8];
        try {
            String binaryString = Integer.toBinaryString(Integer.parseInt(byte_to_hex(b), HEX_RADIX));
            int bitsLeft = 7;
            int offset = 0;
            if (binaryString.length() < 7) {
                offset = 1;
            }

            /* filling zero bits */
            for (int i = 0; i < (bitsLeft - binaryString.length()); i++) {
                bits[i] = 0;
            }
            /* filling bits we have*/
            for (int i = 0; i < binaryString.length(); i++) {
                bits[bitsLeft - binaryString.length() + i + 1] = Integer.valueOf(String.valueOf(binaryString.charAt(i)));
            }

        } catch (Exception e) {
            log.info("BYTE:" + b, e);
        }
        return bits;
    }

    /**
     * returns tag length Example: Tag value is A0 Tag length HEX value is 82 ,
     * HEX value 82 consists of bits 1000 0010, we need to read bits from 1 to 7
     * in order understand how many bytes we have to read to get tag length.
     * here we use method get_bitpos_decimal(); bit 10 = 2 next HEX values 0101
     * determin tag length = 257
     */
    int get_tag_length(byte b[], int pos) {
        int length = -1;
        try {
            String tag = get_tag(b, pos);
            pos += tag.length() / 2; // step forward to get tag length hex value        
            int[] tagbits = get_tag_bits(b[pos]); // got tag bits, to get number of bytes to read, in order to get tag length
            int bytes_num = get_bitpos_decimal(1, 7, tagbits);
            String hexlength = "";
            if (tagbits[0] == 0) {
                length = Integer.parseInt(byte_to_hex(b[pos]), HEX_RADIX);
            } else {
                for (int k = 0; k < bytes_num; k++) {
                    pos++; // change pos to calc tag length
                    hexlength += byte_to_hex(b[(pos)]);
                }
                length = Integer.parseInt(hexlength, HEX_RADIX);
            }
        } catch (Exception e) {
            log.info(e);            
            String debugHex = new String();
            for (int i = 0; i < b.length; i++) {
                byte c = b[i];
                debugHex+=byte_to_hex(c);
            }
            log.fatal("Error at position " + pos + " bytes " + debugHex);
        }
        return length;
    }

    /**
     * return decimal number of specified bits in start / end positions Example:
     * we pass to read bits from 1 to 7: we have bits value 1000 0011 , as a
     * result we get 000 0011
     */
    int get_bitpos_decimal(int sp, int ep, int[] bits) {
        int dec = 0;
        try {
            if (bits[0] == 1) {
                String posbits = "";
                for (int i = sp; i <= ep; i++) {
                    posbits += String.valueOf(bits[i]);
                }
                dec = Integer.parseInt(posbits, BIT_RADIX);
                dec = (dec == 0) ? 1 : dec; // if length tag is 80, then next read bytes length = 0, in this case we return 1
            } else {
                dec = 1;
            }
        } catch (Exception e) {
            log.info("sp=" + sp + "; ep=" + ep);
        }
        return dec;
    }

    /**
     * 0 - primitive 1 - contructed
     */
    int tag_type(byte[] b, int pos) {
        int type = -1;
        int[] tag_bits = get_tag_bits(b[pos]);

        if (get_bitpos_decimal(3, 7, tag_bits) >= 31) {
            return 1;
        }

        if (tag_bits[2] == 0) {
            type = 0;
        } else if (tag_bits[2] == 1) {
            type = 1;
        }
        return type;
    }

    /**
     * Returns event tag if tag is composed, then composed tag returns
     */
    String get_tag(byte[] b, int pos) {

        String res = "";
        int tag_type = tag_type(b, pos);
        if (tag_type == 0) {
            res = byte_to_hex(b[pos]);
        } /* composed tag*/ else if (tag_type == 1) {
            /* if bits 5-1 set to 1 , we look for further octets*/
            int[] bits = get_tag_bits(b[pos]);
            if (bits[3] == 1 && bits[4] == 1 && bits[5] == 1 && bits[6] == 1 && bits[7] == 1) {
                res += byte_to_hex(b[pos]);
                pos++; // get second octet
                bits = get_tag_bits(b[pos]);
                /* if subcequent octet's 8 bit = 1 */
                while (bits[0] == 1) {
                    res += byte_to_hex(b[pos]);
                    pos++;
                    bits = get_tag_bits(b[pos]);
                }
                res += byte_to_hex(b[pos]);
            }/**
             * if tag is composed, but not all of bits 5-1 have value 1
             */
            else {
                res += byte_to_hex(b[pos]);
            }

        }
        return res;
    }

    String TBCDSTRING(String data) {
        String res = "";

        for (int i = 0; i < data.length(); i++) {
            res += data.substring(i + 1, i + 2) + "" + data.substring(i, i + 1);
            i++;
        }

        return res;
    }

}

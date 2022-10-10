package io.metersphere.utils;

import io.metersphere.commons.exception.MSException;

import java.math.BigInteger;
import java.util.Iterator;
import java.util.Map;

public class TypeUtils {
    public static String castToString(Object value) {
        if (value == null) {
            return null;
        }
        return value.toString();
    }

    public static Byte castToByte(Object value) {
        if (value == null) {
            return null;
        }

        if (value instanceof Number) {
            return ((Number) value).byteValue();
        }

        if (value instanceof String) {
            String strVal = (String) value;
            if (strVal.length() == 0 //
                    || "null".equals(strVal) //
                    || "NULL".equals(strVal)) {
                return null;
            }
            return Byte.parseByte(strVal);
        }

        if (value instanceof Boolean) {
            return (Boolean) value ? (byte) 1 : (byte) 0;
        }
        MSException.throwException("can not cast to byte, value : " + value);
        return null;
    }

    public static Character castToChar(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Character) {
            return (Character) value;
        }
        if (value instanceof String) {
            String strVal = (String) value;
            if (strVal.length() == 0) {
                return null;
            }
            if (strVal.length() != 1) {
                MSException.throwException("can not cast to char, value : " + value);
            }
            return strVal.charAt(0);
        }
        MSException.throwException("can not cast to char, value : " + value);
        return null;
    }

    public static Short castToShort(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Number) {
            return ((Number) value).shortValue();
        }

        if (value instanceof String) {
            String strVal = (String) value;
            if (strVal.length() == 0 //
                    || "null".equals(strVal) //
                    || "NULL".equals(strVal)) {
                return null;
            }
            return Short.parseShort(strVal);
        }

        if (value instanceof Boolean) {
            return ((Boolean) value).booleanValue() ? (short) 1 : (short) 0;
        }

        MSException.throwException("can not cast to short, value : " + value);
        return null;
    }


    public static BigInteger castToBigInteger(Object value) {
        if (value == null) {
            return null;
        }

        if (value instanceof Float) {
            Float floatValue = (Float) value;
            if (Float.isNaN(floatValue) || Float.isInfinite(floatValue)) {
                return null;
            }
            return BigInteger.valueOf(floatValue.longValue());
        } else if (value instanceof Double) {
            Double doubleValue = (Double) value;
            if (Double.isNaN(doubleValue) || Double.isInfinite(doubleValue)) {
                return null;
            }
            return BigInteger.valueOf(doubleValue.longValue());
        } else if (value instanceof BigInteger) {
            return (BigInteger) value;
        }

        String strVal = value.toString();

        if (strVal.length() == 0
                || strVal.equalsIgnoreCase("null")) {
            return null;
        }

        if (strVal.length() > 65535) {
            MSException.throwException("decimal overflow");
        }
        return new BigInteger(strVal);
    }

    public static Float castToFloat(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Number) {
            return ((Number) value).floatValue();
        }
        if (value instanceof String) {
            String strVal = value.toString();
            if (strVal.length() == 0 //
                    || "null".equals(strVal) //
                    || "NULL".equals(strVal)) {
                return null;
            }
            if (strVal.indexOf(',') != -1) {
                strVal = strVal.replaceAll(",", "");
            }
            return Float.parseFloat(strVal);
        }

        if (value instanceof Boolean) {
            return (Boolean) value ? 1F : 0F;
        }

        MSException.throwException("can not cast to float, value : " + value);
        return null;
    }

    public static Double castToDouble(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Number) {
            return ((Number) value).doubleValue();
        }
        if (value instanceof String) {
            String strVal = value.toString();
            if (strVal.length() == 0 //
                    || "null".equals(strVal) //
                    || "NULL".equals(strVal)) {
                return null;
            }
            if (strVal.indexOf(',') != -1) {
                strVal = strVal.replaceAll(",", "");
            }
            return Double.parseDouble(strVal);
        }

        if (value instanceof Boolean) {
            return (Boolean) value ? 1D : 0D;
        }

        MSException.throwException("can not cast to double, value : " + value);
        return null;
    }

    public static boolean isNumber(String str) {
        for (int i = 0; i < str.length(); ++i) {
            char ch = str.charAt(i);
            if (ch == '+' || ch == '-') {
                if (i != 0) {
                    return false;
                }
            } else if (ch < '0' || ch > '9') {
                return false;
            }
        }
        return true;
    }

    public static Long castToLong(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Number) {
            return ((Number) value).longValue();
        }

        if (value instanceof String) {
            String strVal = (String) value;
            if (strVal.length() == 0 //
                    || "null".equals(strVal) //
                    || "NULL".equals(strVal)) {
                return null;
            }
            if (strVal.indexOf(',') != -1) {
                strVal = strVal.replaceAll(",", "");
            }
            try {
                return Long.parseLong(strVal);
            } catch (NumberFormatException ex) {
                //
            }

        }

        if (value instanceof Map) {
            Map map = (Map) value;
            if (map.size() == 2
                    && map.containsKey("andIncrement")
                    && map.containsKey("andDecrement")) {
                Iterator iter = map.values().iterator();
                iter.next();
                Object value2 = iter.next();
                return castToLong(value2);
            }
        }

        if (value instanceof Boolean) {
            return (Boolean) value ? 1L : 0L;
        }

        MSException.throwException("can not cast to long, value : " + value);
        return null;
    }

    public static Boolean castToBoolean(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Boolean) {
            return (Boolean) value;
        }

        if (value instanceof Number) {
            return ((Number) value).intValue() == 1;
        }

        if (value instanceof String) {
            String strVal = (String) value;
            if (strVal.length() == 0 //
                    || "null".equals(strVal) //
                    || "NULL".equals(strVal)) {
                return null;
            }
            if ("true".equalsIgnoreCase(strVal) //
                    || "1".equals(strVal)) {
                return Boolean.TRUE;
            }
            if ("false".equalsIgnoreCase(strVal) //
                    || "0".equals(strVal)) {
                return Boolean.FALSE;
            }
            if ("Y".equalsIgnoreCase(strVal) //
                    || "T".equals(strVal)) {
                return Boolean.TRUE;
            }
            if ("F".equalsIgnoreCase(strVal) //
                    || "N".equals(strVal)) {
                return Boolean.FALSE;
            }
        }
        MSException.throwException("can not cast to boolean, value : " + value);
        return null;
    }

    public static byte[] castToBytes(Object value) {
        if (value instanceof byte[]) {
            return (byte[]) value;
        }

        if (value instanceof String) {
            return decodeBase64((String) value);
        }
        MSException.throwException("can not cast to byte[], value : " + value);
        return new byte[0];
    }

    public static final int[] IA = new int[256];

    public static byte[] decodeBase64(String s) {
        // Check special case
        int sLen = s.length();
        if (sLen == 0) {
            return new byte[0];
        }

        int sIx = 0, eIx = sLen - 1; // Start and end index after trimming.

        // Trim illegal chars from start
        while (sIx < eIx && IA[s.charAt(sIx) & 0xff] < 0)
            sIx++;

        // Trim illegal chars from end
        while (eIx > 0 && IA[s.charAt(eIx) & 0xff] < 0)
            eIx--;

        // get the padding count (=) (0, 1 or 2)
        int pad = s.charAt(eIx) == '=' ? (s.charAt(eIx - 1) == '=' ? 2 : 1) : 0; // Count '=' at end.
        int cCnt = eIx - sIx + 1; // Content count including possible separators
        int sepCnt = sLen > 76 ? (s.charAt(76) == '\r' ? cCnt / 78 : 0) << 1 : 0;

        int len = ((cCnt - sepCnt) * 6 >> 3) - pad; // The number of decoded bytes
        byte[] dArr = new byte[len]; // Preallocate byte[] of exact length

        // Decode all but the last 0 - 2 bytes.
        int d = 0;
        for (int cc = 0, eLen = (len / 3) * 3; d < eLen; ) {
            // Assemble three bytes into an int from four "valid" characters.
            int i = IA[s.charAt(sIx++)] << 18 | IA[s.charAt(sIx++)] << 12 | IA[s.charAt(sIx++)] << 6
                    | IA[s.charAt(sIx++)];

            // Add the bytes
            dArr[d++] = (byte) (i >> 16);
            dArr[d++] = (byte) (i >> 8);
            dArr[d++] = (byte) i;

            // If line separator, jump over it.
            if (sepCnt > 0 && ++cc == 19) {
                sIx += 2;
                cc = 0;
            }
        }

        if (d < len) {
            // Decode last 1-3 bytes (incl '=') into 1-3 bytes
            int i = 0;
            for (int j = 0; sIx <= eIx - pad; j++)
                i |= IA[s.charAt(sIx++)] << (18 - j * 6);

            for (int r = 16; d < len; r -= 8)
                dArr[d++] = (byte) (i >> r);
        }

        return dArr;
    }

    public static Integer castToInt(Object value) {
        if (value == null) {
            return null;
        }

        if (value instanceof Integer) {
            return (Integer) value;
        }

        if (value instanceof Number) {
            return ((Number) value).intValue();
        }

        if (value instanceof String) {
            String strVal = (String) value;
            if (strVal.length() == 0 //
                    || "null".equals(strVal) //
                    || "NULL".equals(strVal)) {
                return null;
            }
            if (strVal.indexOf(',') != -1) {
                strVal = strVal.replaceAll(",", "");
            }
            return Integer.parseInt(strVal);
        }

        if (value instanceof Boolean) {
            return (Boolean) value ? 1 : 0;
        }
        if (value instanceof Map) {
            Map map = (Map) value;
            if (map.size() == 2
                    && map.containsKey("andIncrement")
                    && map.containsKey("andDecrement")) {
                Iterator iter = map.values().iterator();
                iter.next();
                Object value2 = iter.next();
                return castToInt(value2);
            }
        }
        MSException.throwException("can not cast to int, value : " + value);
        return 0;
    }

}

package net.minidev.json.parser;

/*
 *    Copyright 2011-2023 JSON-SMART authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import net.minidev.json.writer.JsonReader;
import net.minidev.json.writer.JsonReaderI;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;

import static net.minidev.json.parser.ParseException.ERROR_UNEXPECTED_CHAR;
import static net.minidev.json.parser.ParseException.ERROR_UNEXPECTED_EOF;
import static net.minidev.json.parser.ParseException.ERROR_UNEXPECTED_LEADING_0;
import static net.minidev.json.parser.ParseException.ERROR_UNEXPECTED_TOKEN;
import static net.minidev.json.parser.ParseException.ERROR_UNEXPECTED_UNICODE;
import static net.minidev.json.parser.ParseException.ERROR_UNEXPECTED_JSON_DEPTH;

/**
 * JSONParserBase is the common code between {@link JSONParserString} and
 * {@link JSONParserReader}
 *
 * @author Uriel Chemouni &lt;uchemouni@gmail.com&gt;
 * @see JSONParserMemory
 * @see JSONParserStream
 */
abstract class JSONParserBase {
    protected char c;
    /**
     * hard coded maximal depth for JSON parsing
     */
    public final static int MAX_DEPTH = 400;
    protected int depth = 0;

    JsonReader base;
    public final static byte EOI = 0x1A;
    protected static final char MAX_STOP = 126; // '}' -> 125
    private String lastKey;

    protected static boolean[] stopAll = new boolean[MAX_STOP];
    protected static boolean[] stopArray = new boolean[MAX_STOP];
    protected static boolean[] stopKey = new boolean[MAX_STOP];
    protected static boolean[] stopValue = new boolean[MAX_STOP];
    protected static boolean[] stopX = new boolean[MAX_STOP];

    static {
        stopKey[':'] = stopKey[EOI] = true;
        stopValue[','] = stopValue['}'] = stopValue[EOI] = true;
        stopArray[','] = stopArray[']'] = stopArray[EOI] = true;
        stopX[EOI] = true;
        stopAll[','] = stopAll[':'] = true;
        stopAll[']'] = stopAll['}'] = stopAll[EOI] = true;
    }

    /*
     * End of static declaration
     */
    //
    //
    protected final MSB sb = new MSB(15);
    protected Object xo;
    protected String xs;
    protected int pos;

    /*
     * Parsing flags
     */
    protected final boolean acceptLeadinZero;
    protected final boolean acceptNaN;
    protected final boolean acceptNonQuote;
    protected final boolean acceptSimpleQuote;
    protected final boolean acceptUselessComma;
    protected final boolean checkTaillingData;
    protected final boolean checkTaillingSpace;
    protected final boolean ignoreControlChar;
    protected final boolean useHiPrecisionFloat;
    protected final boolean useIntegerStorage;
    protected final boolean reject127;
    protected final boolean unrestictBigDigit;

    public JSONParserBase(int permissiveMode) {
        this.acceptNaN = (permissiveMode & JSONParser.ACCEPT_NAN) > 0;
        this.acceptNonQuote = (permissiveMode & JSONParser.ACCEPT_NON_QUOTE) > 0;
        this.acceptSimpleQuote = (permissiveMode & JSONParser.ACCEPT_SIMPLE_QUOTE) > 0;
        this.ignoreControlChar = (permissiveMode & JSONParser.IGNORE_CONTROL_CHAR) > 0;
        this.useIntegerStorage = (permissiveMode & JSONParser.USE_INTEGER_STORAGE) > 0;
        this.acceptLeadinZero = (permissiveMode & JSONParser.ACCEPT_LEADING_ZERO) > 0;
        this.acceptUselessComma = (permissiveMode & JSONParser.ACCEPT_USELESS_COMMA) > 0;
        this.useHiPrecisionFloat = (permissiveMode & JSONParser.USE_HI_PRECISION_FLOAT) > 0;
        this.checkTaillingData = (permissiveMode & (JSONParser.ACCEPT_TAILLING_DATA
                | JSONParser.ACCEPT_TAILLING_SPACE)) != (JSONParser.ACCEPT_TAILLING_DATA
                | JSONParser.ACCEPT_TAILLING_SPACE);
        this.checkTaillingSpace = (permissiveMode & JSONParser.ACCEPT_TAILLING_SPACE) == 0;
        this.reject127 = (permissiveMode & JSONParser.REJECT_127_CHAR) > 0;
        this.unrestictBigDigit = (permissiveMode & JSONParser.BIG_DIGIT_UNRESTRICTED) > 0;
    }

    public void checkControleChar() throws ParseException {
        if (ignoreControlChar)
            return;
        int l = xs.length();
        for (int i = 0; i < l; i++) {
            char c = xs.charAt(i);
            if (c < 0)
                continue;
            if (c <= 31)
                throw new ParseException(pos + i, ERROR_UNEXPECTED_CHAR, c);
            if (c == 127) {
                if (reject127)
                    throw new ParseException(pos + i, ParseException.ERROR_UNEXPECTED_CHAR, c);
            }
        }
    }

    public void checkLeadinZero() throws ParseException {
        int len = xs.length();
        if (len == 1)
            return;
        if (len == 2) {
            if (xs.equals("00"))
                throw new ParseException(pos, ERROR_UNEXPECTED_LEADING_0, xs);
            return;
        }
        char c1 = xs.charAt(0);
        char c2 = xs.charAt(1);
        if (c1 == '-') {
            char c3 = xs.charAt(2);
            if (c2 == '0' && c3 >= '0' && c3 <= '9')
                throw new ParseException(pos, ERROR_UNEXPECTED_LEADING_0, xs);
            return;
        }
        if (c1 == '0' && c2 >= '0' && c2 <= '9')
            throw new ParseException(pos, ERROR_UNEXPECTED_LEADING_0, xs);
    }

    protected Number extractFloat() throws ParseException {
        if (!acceptLeadinZero)
            checkLeadinZero();

        try {
            if (!useHiPrecisionFloat)
                return Float.parseFloat(xs);

            // follow JSonIJ parsing method
            // use extra CPU to check if the result can be return as double without
            // precision lost
            if (!unrestictBigDigit) {
                double asDouble = Double.parseDouble(xs);
                final String doubleStr = String.valueOf(asDouble);
                // we need a compare `e` `E` `e+` `E+`
                if (compareDoublePrecision(doubleStr, xs)) {
                    return asDouble;
                }
            }
            return new BigDecimal(xs);

        } catch (NumberFormatException e) {
            throw new ParseException(pos, ERROR_UNEXPECTED_TOKEN, xs);
        }
    }

    private boolean compareDoublePrecision(String convert, String origin) {
        final char[] charArray = convert.toCharArray();
        final char[] originArray = origin.toCharArray();
        if (charArray.length > originArray.length) {
            return false;
        }
        int j = 0;
        for (int i = 0; i < charArray.length; i++) {
            if (charArray[i] < '0' || charArray[i] > '9') {
                if (originArray[j] >= '0' && originArray[j] <= '9') {
                    return false;
                } else {
                    j++;
                    if (originArray[j] == '+') {
                        j++;
                    }
                    continue;
                }
            }
            if (charArray[i] != originArray[j]) {
                return false;
            }
            j++;
        }
        return j == originArray.length;
    }

    /**
     * use to return Primitive Type, or String, Or JsonObject or JsonArray generated
     * by a ContainerFactory
     */
    protected <T> T parse(JsonReaderI<T> mapper) throws ParseException {
        this.pos = -1;
        T result;
        try {
            read();
            result = readFirst(mapper);
            if (checkTaillingData) {
                if (!checkTaillingSpace)
                    skipSpace();
                if (c != EOI)
                    throw new ParseException(pos - 1, ERROR_UNEXPECTED_TOKEN, c);
            }
        } catch (IOException e) {
            throw new ParseException(pos, e);
        }
        xs = null;
        xo = null;
        return result;
    }

    protected Number parseNumber(String s) throws ParseException {
        // position
        int p = 0;
        // length
        int l = s.length();
        // max position long base 10 length
        int max = 19;
        boolean neg;

        if (s.charAt(0) == '-') {
            p++;
            max++;
            neg = true;
            if (!acceptLeadinZero && l >= 3 && s.charAt(1) == '0')
                throw new ParseException(pos, ERROR_UNEXPECTED_LEADING_0, s);
        } else {
            neg = false;
            if (!acceptLeadinZero && l >= 2 && s.charAt(0) == '0')
                throw new ParseException(pos, ERROR_UNEXPECTED_LEADING_0, s);
        }

        boolean mustCheck;
        if (l < max) {
            max = l;
            mustCheck = false;
        } else if (l > max) {
            return new BigInteger(s, 10);
        } else {
            max = l - 1;
            mustCheck = true;
        }

        long r = 0;
        while (p < max) {
            r = (r * 10L) + ('0' - s.charAt(p++));
        }
        if (mustCheck) {
            boolean isBig;
            if (r > -922337203685477580L) {
                isBig = false;
            } else if (r < -922337203685477580L) {
                isBig = true;
            } else {
                if (neg)
                    isBig = (s.charAt(p) > '8');
                else
                    isBig = (s.charAt(p) > '7');
            }
            if (isBig)
                return new BigInteger(s, 10);
            r = r * 10L + ('0' - s.charAt(p));
        }
        if (neg) {
            if (this.useIntegerStorage && r >= Integer.MIN_VALUE)
                return (int) r;
            return r;
        }
        r = -r;
        if (this.useIntegerStorage && r <= Integer.MAX_VALUE)
            return (int) r;
        return r;
    }

    abstract protected void read() throws IOException;

    protected <T> T readArray(JsonReaderI<T> mapper) throws ParseException, IOException {
        if (c != '[')
            throw new RuntimeException("Internal Error");
        if (++this.depth > MAX_DEPTH) {
            throw new ParseException(pos, ERROR_UNEXPECTED_JSON_DEPTH, c);
        }
        Object current = mapper.createArray();
        read();
        boolean needData = false;
        // special case needData is false and can close is true
        if (c == ',' && !acceptUselessComma)
            throw new ParseException(pos, ERROR_UNEXPECTED_CHAR, (char) c);
        for (; ; ) {
            switch (c) {
                case ' ':
                case '\r':
                case '\n':
                case '\t':
                    read();
                    continue;
                case ']':
                    if (needData && !acceptUselessComma)
                        throw new ParseException(pos, ERROR_UNEXPECTED_CHAR, (char) c);
                    this.depth--;
                    read(); /* unstack */
                    //
                    return mapper.convert(current);
                case ':':
                case '}':
                    throw new ParseException(pos, ERROR_UNEXPECTED_CHAR, (char) c);
                case ',':
                    if (needData && !acceptUselessComma)
                        throw new ParseException(pos, ERROR_UNEXPECTED_CHAR, (char) c);
                    read();
                    needData = true;
                    continue;
                case EOI:
                    throw new ParseException(pos - 1, ERROR_UNEXPECTED_EOF, "EOF");
                default:
                    mapper.addValue(current, readMain(mapper, stopArray));
                    needData = false;
                    continue;
            }
        }
    }

    /**
     * use to return Primitive Type, or String, Or JsonObject or JsonArray generated
     * by a ContainerFactory
     */
    protected <T> T readFirst(JsonReaderI<T> mapper) throws ParseException, IOException {
        for (; ; ) {
            switch (c) {
                // skip spaces
                case ' ':
                case '\r':
                case '\n':
                case '\t':
                    read();
                    continue;
                    // invalid state
                case ':':
                case '}':
                case ']':
                    throw new ParseException(pos, ERROR_UNEXPECTED_CHAR, c);
                    // start object
                case '{':
                    return readObject(mapper);
                // start Array
                case '[':
                    return readArray(mapper);
                // start string
                case '"':
                case '\'':
                    readString();
                    //
                    return mapper.convert(xs);
                // string or null
                case 'n':
                    readNQString(stopX);
                    if ("null".equals(xs)) {
                        //
                        return null;
                    }
                    if (!acceptNonQuote)
                        throw new ParseException(pos, ERROR_UNEXPECTED_TOKEN, xs);
                    //
                    return mapper.convert(xs);
                // string or false
                case 'f':
                    readNQString(stopX);
                    if ("false".equals(xs)) {
                        //
                        return mapper.convert(Boolean.FALSE);
                    }
                    if (!acceptNonQuote)
                        throw new ParseException(pos, ERROR_UNEXPECTED_TOKEN, xs);
                    //
                    return mapper.convert(xs);
                // string or true
                case 't':
                    readNQString(stopX);
                    if ("true".equals(xs)) {
                        //
                        return mapper.convert(Boolean.TRUE);
                    }
                    if (!acceptNonQuote)
                        throw new ParseException(pos, ERROR_UNEXPECTED_TOKEN, xs);
                    //
                    return mapper.convert(xs);
                // string or NaN
                case 'N':
                    readNQString(stopX);
                    if (!acceptNaN)
                        throw new ParseException(pos, ERROR_UNEXPECTED_TOKEN, xs);
                    if ("NaN".equals(xs)) {
                        //
                        return mapper.convert(Float.valueOf(Float.NaN));
                    }
                    if (!acceptNonQuote)
                        throw new ParseException(pos, ERROR_UNEXPECTED_TOKEN, xs);
                    //
                    return mapper.convert(xs);
                // digits
                case '0':
                case '1':
                case '2':
                case '3':
                case '4':
                case '5':
                case '6':
                case '7':
                case '8':
                case '9':
                case '-':
                    xo = readNumber(stopX);
                    //
                    return mapper.convert(xo);
                default:
                    readNQString(stopX);
                    if (!acceptNonQuote)
                        throw new ParseException(pos, ERROR_UNEXPECTED_TOKEN, xs);
                    //
                    return mapper.convert(xs);
            }
        }
    }

    /**
     * use to return Primitive Type, or String, Or JsonObject or JsonArray generated
     * by a ContainerFactory
     */
    protected Object readMain(JsonReaderI<?> mapper, boolean stop[]) throws ParseException, IOException {
        for (; ; ) {
            switch (c) {
                // skip spaces
                case ' ':
                case '\r':
                case '\n':
                case '\t':
                    read();
                    continue;
                    // invalid state
                case ':':
                case '}':
                case ']':
                    throw new ParseException(pos, ERROR_UNEXPECTED_CHAR, c);
                    // start object
                case '{':
                    return readObject(mapper.startObject(lastKey));
                // start Array
                case '[':
                    return readArray(mapper.startArray(lastKey));
                // start string
                case '"':
                case '\'':
                    readString();
                    //
                    return xs;
                // string or null
                case 'n':
                    readNQString(stop);
                    if ("null".equals(xs)) {
                        //
                        return null;
                    }
                    if (!acceptNonQuote)
                        throw new ParseException(pos, ERROR_UNEXPECTED_TOKEN, xs);
                    //
                    return xs;
                // string or false
                case 'f':
                    readNQString(stop);
                    if ("false".equals(xs)) {
                        //
                        return Boolean.FALSE;
                    }
                    if (!acceptNonQuote)
                        throw new ParseException(pos, ERROR_UNEXPECTED_TOKEN, xs);
                    //
                    return xs;
                // string or true
                case 't':
                    readNQString(stop);
                    if ("true".equals(xs)) {
                        //
                        return Boolean.TRUE;
                    }
                    if (!acceptNonQuote)
                        throw new ParseException(pos, ERROR_UNEXPECTED_TOKEN, xs);
                    //
                    return xs;
                // string or NaN
                case 'N':
                    readNQString(stop);
                    if (!acceptNaN)
                        throw new ParseException(pos, ERROR_UNEXPECTED_TOKEN, xs);
                    if ("NaN".equals(xs)) {
                        //
                        return Float.valueOf(Float.NaN);
                    }
                    if (!acceptNonQuote)
                        throw new ParseException(pos, ERROR_UNEXPECTED_TOKEN, xs);
                    //
                    return xs;
                // digits
                case '0':
                case '1':
                case '2':
                case '3':
                case '4':
                case '5':
                case '6':
                case '7':
                case '8':
                case '9':
                case '-':
                    //
                    //
                    return readNumber(stop);
                default:
                    readNQString(stop);
                    if (!acceptNonQuote)
                        throw new ParseException(pos, ERROR_UNEXPECTED_TOKEN, xs);
                    //
                    return xs;
            }
        }
    }

    abstract protected void readNoEnd() throws ParseException, IOException;

    abstract protected void readNQString(boolean[] stop) throws IOException;

    abstract protected Object readNumber(boolean[] stop) throws ParseException, IOException;

    protected <T> T readObject(JsonReaderI<T> mapper) throws ParseException, IOException {
        //
        if (c != '{')
            throw new RuntimeException("Internal Error");
        if (++this.depth > MAX_DEPTH) {
            throw new ParseException(pos, ERROR_UNEXPECTED_JSON_DEPTH, c);
        }
        Object current = mapper.createObject();
        boolean needData = false;
        boolean acceptData = true;
        for (; ; ) {
            read();
            switch (c) {
                case ' ':
                case '\r':
                case '\t':
                case '\n':
                    continue;
                case ':':
                case ']':
                case '[':
                case '{':
                    throw new ParseException(pos, ERROR_UNEXPECTED_CHAR, c);
                case '}':
                    if (needData && !acceptUselessComma)
                        throw new ParseException(pos, ERROR_UNEXPECTED_CHAR, (char) c);
                    this.depth--;
                    read(); /* unstack */
                    //
                    return mapper.convert(current);
                case ',':
                    if (needData && !acceptUselessComma)
                        throw new ParseException(pos, ERROR_UNEXPECTED_CHAR, (char) c);
                    acceptData = needData = true;
                    continue;
                case '"':
                case '\'':
                default:
                    // int keyStart = pos;
                    if (c == '\"' || c == '\'') {
                        readString();
                    } else {
                        readNQString(stopKey);
                        if (!acceptNonQuote)
                            throw new ParseException(pos, ERROR_UNEXPECTED_TOKEN, xs);
                    }
                    String key = xs;
                    if (!acceptData)
                        throw new ParseException(pos, ERROR_UNEXPECTED_TOKEN, key);

                    // Skip spaces
                    skipSpace();

                    if (c != ':') {
                        if (c == EOI)
                            throw new ParseException(pos - 1, ERROR_UNEXPECTED_EOF, null);
                        throw new ParseException(pos - 1, ERROR_UNEXPECTED_CHAR, c);
                    }
                    readNoEnd(); /* skip : */
                    lastKey = key;
                    Object value = readMain(mapper, stopValue);
                    mapper.setValue(current, key, value);
                    lastKey = null;

                    // Object duplicate = obj.put(key, readMain(stopValue));
                    // if (duplicate != null)
                    // throw new ParseException(keyStart, ERROR_UNEXPECTED_DUPLICATE_KEY, key);
                    // handler.endObjectEntry();
                    // should loop skipping read step
                    skipSpace();
                    if (c == '}') {
                        this.depth--;
                        read(); /* unstack */
                        //
                        return mapper.convert(current);
                    }
                    if (c == EOI) // Fixed on 18/10/2011 reported by vladimir
                        throw new ParseException(pos - 1, ERROR_UNEXPECTED_EOF, null);
                    // if c==, continue
                    if (c == ',')
                        acceptData = needData = true;
                    else
                        throw new ParseException(pos - 1, ERROR_UNEXPECTED_TOKEN, c);
                    // acceptData = needData = false;
            }
        }
    }

    /**
     * store and read
     */
    abstract void readS() throws IOException;

    abstract protected void readString() throws ParseException, IOException;

    protected void readString2() throws ParseException, IOException {
        /* assert (c == '\"' || c == '\'') */
        char sep = c;
        for (; ; ) {
            read();
            switch (c) {
                case EOI:
                    throw new ParseException(pos - 1, ERROR_UNEXPECTED_EOF, null);
                case '"':
                case '\'':
                    if (sep == c) {
                        read();
                        xs = sb.toString();
                        return;
                    }
                    sb.append(c);
                    break;
                case '\\':
                    read();
                    switch (c) {
                        case 't':
                            sb.append('\t');
                            break;
                        case 'n':
                            sb.append('\n');
                            break;
                        case 'r':
                            sb.append('\r');
                            break;
                        case 'f':
                            sb.append('\f');
                            break;
                        case 'b':
                            sb.append('\b');
                            break;
                        case '\\':
                            sb.append('\\');
                            break;
                        case '/':
                            sb.append('/');
                            break;
                        case '\'':
                            sb.append('\'');
                            break;
                        case '"':
                            sb.append('"');
                            break;
                        case 'u':
                            sb.append(readUnicode(4));
                            break;
                        case 'x':
                            sb.append(readUnicode(2));
                            break;
                        default:
                            break;
                    }
                    break;
                case '\0': // end of string
                case (char) 1: // Start of heading
                case (char) 2: // Start of text
                case (char) 3: // End of text
                case (char) 4: // End of transmission
                case (char) 5: // Enquiry
                case (char) 6: // Acknowledge
                case (char) 7: // Bell
                case '\b': // 8: backSpase
                case '\t': // 9: horizontal tab
                case '\n': // 10: new line
                case (char) 11: // Vertical tab
                case '\f': // 12: form feed
                case '\r': // 13: return carriage
                case (char) 14: // Shift Out, alternate character set
                case (char) 15: // Shift In, resume defaultn character set
                case (char) 16: // Data link escape
                case (char) 17: // XON, with XOFF to pause listings;
                case (char) 18: // Device control 2, block-mode flow control
                case (char) 19: // XOFF, with XON is TERM=18 flow control
                case (char) 20: // Device control 4
                case (char) 21: // Negative acknowledge
                case (char) 22: // Synchronous idle
                case (char) 23: // End transmission block, not the same as EOT
                case (char) 24: // Cancel line, MPE echoes !!!
                case (char) 25: // End of medium, Control-Y interrupt
                    // case (char) 26: // Substitute == EOI
                case (char) 27: // escape
                case (char) 28: // File Separator
                case (char) 29: // Group Separator
                case (char) 30: // Record Separator
                case (char) 31: // Unit Separator
                    if (ignoreControlChar)
                        continue;
                    throw new ParseException(pos, ERROR_UNEXPECTED_CHAR, c);
                case (char) 127: // del
                    if (ignoreControlChar)
                        continue;
                    if (reject127)
                        throw new ParseException(pos, ERROR_UNEXPECTED_CHAR, c);
                default:
                    sb.append(c);
            }
        }
    }

    protected char readUnicode(int totalChars) throws ParseException, IOException {
        int value = 0;
        for (int i = 0; i < totalChars; i++) {
            value = value * 16;
            read();
            if (c <= '9' && c >= '0')
                value += c - '0';
            else if (c <= 'F' && c >= 'A')
                value += (c - 'A') + 10;
            else if (c >= 'a' && c <= 'f')
                value += (c - 'a') + 10;
            else if (c == EOI)
                throw new ParseException(pos, ERROR_UNEXPECTED_EOF, "EOF");
            else
                throw new ParseException(pos, ERROR_UNEXPECTED_UNICODE, c);
        }
        return (char) value;
    }

    protected void skipDigits() throws IOException {
        for (; ; ) {
            if (c < '0' || c > '9')
                return;
            readS();
        }
    }

    protected void skipNQString(boolean[] stop) throws IOException {
        for (; ; ) {
            if ((c == EOI) || (c >= 0 && c < MAX_STOP && stop[c]))
                return;
            readS();
        }
    }

    protected void skipSpace() throws IOException {
        for (; ; ) {
            if (c > ' ' || c == EOI)
                return;
            readS();
        }
    }

    public static class MSB {
        char b[];
        int p;

        public MSB(int size) {
            b = new char[size];
            p = -1;
        }

        public void append(char c) {
            p++;
            if (b.length <= p) {
                char[] t = new char[b.length * 2 + 1];
                System.arraycopy(b, 0, t, 0, b.length);
                b = t;
            }
            b[p] = c;
        }

        public void append(int c) {
            p++;
            if (b.length <= p) {
                char[] t = new char[b.length * 2 + 1];
                System.arraycopy(b, 0, t, 0, b.length);
                b = t;
            }
            b[p] = (char) c;
        }

        public String toString() {
            return new String(b, 0, p + 1);
        }

        public void clear() {
            p = -1;
        }
    }
}

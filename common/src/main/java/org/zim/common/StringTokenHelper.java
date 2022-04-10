package org.zim.common;

public class StringTokenHelper {

    private String source;

    private int idx = -1;

    public StringTokenHelper(String source) {
        if (source == null || source.isEmpty()) {
            return;
        }
        this.source = source.trim();
        idx = 0;
    }

    public String next() {
        if (idx == -1 || idx >= source.length()) {
            return null;
        }
        StringBuilder b = new StringBuilder();
        for (; idx < source.length(); idx++) {
            char c = source.charAt(idx);
            if (c == ' ') {
                break;
            }
            b.append(c);
        }
        if (b.length() > 0) {
            while (idx < source.length() && source.charAt(idx) == ' ') {
                idx++;
            }
            return b.toString();
        }
        idx = -1;
        return null;
    }

    public boolean hasNext() {
        if (idx == -1 || idx >= source.length()) {
            return false;
        }
        return source.charAt(idx) != ' ';
    }

    public String remaining() {
        return idx >= 0 && idx < source.length() ? source.substring(idx) : null;
    }
}

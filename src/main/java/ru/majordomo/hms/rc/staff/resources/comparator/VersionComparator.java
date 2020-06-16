package ru.majordomo.hms.rc.staff.resources.comparator;

import java.util.Comparator;

public class VersionComparator implements Comparator<String> {

    @Override
    public int compare(String v1, String v2) {
        if (v1 == null) {
            v1 = "";
        }
        if (v2 == null) {
            v2 = "";
        }
        String[] thisParts = v1.split("\\.");
        String[] thatParts = v2.split("\\.");
        int length = Math.max(thisParts.length, thatParts.length);
        for(int i = 0; i < length; i++) {
            int thisPart = 0;
            if (i < thisParts.length) {
                try {
                    thisPart = Integer.parseInt(thisParts[i]);
                } catch (NumberFormatException ignore) {
                    thisPart = Integer.MIN_VALUE;
                }
            }
            int thatPart = 0;
            if (i < thatParts.length) {
                try {
                    thatPart = Integer.parseInt(thatParts[i]);
                } catch (NumberFormatException ignore) {
                    thatPart = Integer.MIN_VALUE;
                }
            }
            if(thisPart > thatPart)
                return -1;
            if(thisPart < thatPart)
                return 1;
        }
        return 0;
    }
}

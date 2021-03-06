package ru.majordomo.hms.rc.staff.cleaner;

import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class Cleaner {
    public String cleanString(String input) {
        return input != null ? input.trim()
                .replace("\\", "")
                .replace("<", "")
                .replace(">", "")
                .replace("?", "")
                .replace("#", "")
                .replace("~", "") : null;
    }

    public List<String> cleanListWithStrings(List<String> stringList) {
        for (int i = 0; i < stringList.size(); i++) {
            String element = cleanString(stringList.get(i));
            if (element.equals("")) {
                stringList.remove(i);
            } else {
                stringList.set(i, element);
            }
        }
        return stringList;
    }
}

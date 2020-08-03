package ru.majordomo.hms.rc.staff.resources.comparator;

import com.google.common.base.Objects;
import org.apache.commons.lang.StringUtils;
import ru.majordomo.hms.rc.staff.resources.Service;
import ru.majordomo.hms.rc.staff.resources.template.ApplicationServer;
import ru.majordomo.hms.rc.staff.resources.template.DatabaseServer;
import ru.majordomo.hms.rc.staff.resources.template.HttpServer;
import ru.majordomo.hms.rc.staff.resources.template.Template;

import javax.annotation.Nullable;
import java.util.Comparator;

/**
 * Предназначен для сортировки сервисов так чтобы внизу оказывались самые предпочтительные сервисы
 * персональный - php - с самой большой версией - default безопасность.
 *
 * используется в pm и appscat
 */
public class StaffServiceComparator implements Comparator<Service> {
    private final VersionComparator versionComparator = new VersionComparator();

    @Override
    public int compare(@Nullable Service s1, @Nullable Service s2) {
        if (s1 == null || s2 == null) {
            return s1 == s2 ? 0 : (s1 != null)  ? -1 : 1;
        }
        int compareResult = Boolean.compare(s2.switchedOn, s1.switchedOn);
        if (compareResult != 0) {
            return compareResult;
        }
        compareResult = Boolean.compare(StringUtils.isEmpty(s1.getAccountId()), StringUtils.isEmpty(s2.getAccountId()));
        if (compareResult != 0) {
            return compareResult;
        }

        Template t1 = s1.getTemplate();
        Template t2 = s2.getTemplate();
        compareResult = templateRating(s1.getTemplate()) - templateRating(s2.getTemplate());
        if (compareResult != 0) {
            return compareResult;
        }

        if (t1 instanceof ApplicationServer && t2 instanceof ApplicationServer) {
            ApplicationServer a1 = (ApplicationServer) t1;
            ApplicationServer a2 = (ApplicationServer) t2;
            int languageCmp = a1.getLanguage().compareTo(a2.getLanguage());
            if (languageCmp != 0) {
                return languageCmp;
            }

            int versionCmp = versionComparator.compare(a1.getVersion(), a2.getVersion());
            if (versionCmp != 0) {
                return versionCmp;
            }

            return securityLevelRating(s1.getInstanceProps().get(ApplicationServer.Spec.SECURITY_LEVEL))
                    - securityLevelRating(s2.getInstanceProps().get(ApplicationServer.Spec.SECURITY_LEVEL));
        }
        return 0;
    }

    private int securityLevelRating(@Nullable String securityLevel) {
        if (securityLevel == null) {
            securityLevel = "";
        }
        switch (securityLevel) {
            case ApplicationServer.Security.DEFAULT: return 0;
            case ApplicationServer.Security.UNSAFE: return 1;
            case "": return 2;
            case ApplicationServer.Security.HARDENED_NOCHMOD: return 3;
            case ApplicationServer.Security.HARDENED: return 4;
            default: return 9;
        }
    }

    private int templateRating(@Nullable Template template) {
        if (template instanceof ApplicationServer) {
            return 0;
        } else if (template instanceof HttpServer) {
            return 1;
        } else if (template instanceof DatabaseServer) {
            return 2;
        }
        return 10;
    }
}

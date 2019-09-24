package ru.majordomo.hms.rc.staff.resourceProcessor.support;

@FunctionalInterface
public interface ResourceByUrlBuilder<T> {
    T get(String url);
}

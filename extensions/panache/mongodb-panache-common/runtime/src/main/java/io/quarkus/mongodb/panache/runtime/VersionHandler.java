package pearl.infra.mongo;

import io.quarkus.mongodb.panache.annotation.Version;

import java.lang.reflect.Field;
import java.util.Arrays;

/**
 * Handle @Version in queries.
 **/
public class VersionHandler {

    private Object entity;
    private Field versionField;
    private Long versionValue;

    public static VersionHandler of(Object entity) {
        VersionHandler versionUtil = new VersionHandler();
        versionUtil.entity = entity;
        versionUtil.extractVersionField();
        versionUtil.extractVersionValue();
        return versionUtil;
    }

    public boolean containsVersionAnnotation() {
        return versionField != null;
    }

    public boolean containsVersionValue() {
        return versionValue != null;
    }

    public boolean containsVersionAnnotationAndValue() {
        return containsVersionAnnotation() && containsVersionValue();
    }

    private void extractVersionField() {
        Field[] fields = entity.getClass().getFields();
        this.versionField = Arrays.stream(fields)
                .filter(field -> field.isAnnotationPresent(Version.class))
                .findFirst().orElse(null);
    }

    public void extractVersionValue() {
        try {
            if (!containsVersionAnnotation()) {
                return;
            }

            boolean canAccess = versionField.canAccess(entity);
            versionField.setAccessible(true);
            Long versionValue = (Long) versionField.get(entity);
            versionField.setAccessible(canAccess);
            this.versionValue = versionValue;
        } catch (IllegalAccessException e) {
            throw new RuntimeException("Error on set the version value");
        }
    }

    public void adjustVersionValue() {
        try {
            if (!containsVersionAnnotation()) {
                return;
            }

            boolean canAccess = versionField.canAccess(entity);
            versionField.setAccessible(true);

            Long versionValue = (Long) versionField.get(entity);
            versionField.set(entity, versionValue == null ? 0l : versionValue + 1);

            versionField.setAccessible(canAccess);
        } catch (IllegalAccessException e) {
            throw new RuntimeException("Error on set the version value");
        }
    }
}

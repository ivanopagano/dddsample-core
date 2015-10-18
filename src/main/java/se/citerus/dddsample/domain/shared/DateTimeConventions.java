package se.citerus.dddsample.domain.shared;

import java.time.ZoneId;

/**
 * Contains conventional definitions for
 * date and time calculations within the model
 */
public interface DateTimeConventions {

    /**
     * The reference timezone id
     */
    public static final ZoneId REFERENCE_ZONE = ZoneId.of("UTC");

}

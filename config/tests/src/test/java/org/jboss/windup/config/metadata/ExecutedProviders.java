package org.jboss.windup.config.metadata;

import java.util.ArrayList;
import java.util.List;

import org.jboss.windup.config.RuleProvider;

/**
 * Static holder for test data.
 *
 * @author <a href="mailto:lincolnbaxter@gmail.com">Lincoln Baxter, III</a>
 */
public class ExecutedProviders {
    private static List<RuleProvider> providers = new ArrayList<>();

    public static void executedProvider(RuleProvider provider) {
        providers.add(provider);
    }

    public static List<RuleProvider> getProviders() {
        return providers;
    }
}

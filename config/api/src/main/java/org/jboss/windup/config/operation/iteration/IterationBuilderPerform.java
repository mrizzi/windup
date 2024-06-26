/*
 * Copyright 2014 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.jboss.windup.config.operation.iteration;

import org.jboss.windup.config.operation.Iteration;
import org.ocpsoft.rewrite.config.Operation;

/**
 * Intermediate step to construct an {@link Iteration}.
 *
 * @author <a href="mailto:lincolnbaxter@gmail.com">Lincoln Baxter, III</a>
 */
public interface IterationBuilderPerform {
    /**
     * Perform the given {@link Operation} when the conditions set in this {@link Iteration} are met.
     */
    IterationBuilderOtherwise otherwise(Operation operation);

    /**
     * End the {@link Iteration}
     */
    IterationBuilderComplete endIteration();
}

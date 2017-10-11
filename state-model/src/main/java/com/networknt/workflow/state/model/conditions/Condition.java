/*
 * Copyright 2010-2017 Amazon.com, Inc. or its affiliates. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License").
 * You may not use this file except in compliance with the License.
 * A copy of the License is located at
 *
 *  http://aws.amazon.com/apache2.0
 *
 * or in the "license" file accompanying this file. This file is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */

package com.networknt.workflow.state.model.conditions;

import com.networknt.workflow.state.model.internal.Buildable;
import com.networknt.workflow.state.model.states.Choice;

/**
 * Base interface for conditions used in {@link Choice}s.
 *
 * <p>This interface should not be implemented outside of the SDK.</p>
 */
public interface Condition {

    /**
     * No-op model that always returns null.
     */
    Condition.Builder NULL_BUILDER = new Builder() {
        @Override
        public Condition build() {
            return null;
        }
    };

    /**
     * Base model interface for conditions used in {@link Choice}s.
     */
    interface Builder extends Buildable<Condition> {
    }
}

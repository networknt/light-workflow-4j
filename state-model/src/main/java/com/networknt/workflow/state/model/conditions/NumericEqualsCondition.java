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

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.DoubleNode;
import com.fasterxml.jackson.databind.node.LongNode;
import com.fasterxml.jackson.databind.node.NumericNode;
import com.networknt.workflow.state.model.internal.PropertyNames;
import com.networknt.workflow.state.model.states.Choice;

/**
 * Binary condition for Numeric equality comparison. Supports both integral and floating point numeric types.
 *
 * @see <a href="https://states-language.net/spec.html#choice-state">https://states-language.net/spec.html#choice-state</a>
 * @see Choice
 */
public final class NumericEqualsCondition implements BinaryCondition<String> {

    @JsonProperty(PropertyNames.VARIABLE)
    private final String variable;

    @JsonProperty(PropertyNames.NUMERIC_EQUALS)
    private final NumericNode expectedValue;

    private NumericEqualsCondition(Builder builder) {
        this.variable = builder.variable;
        this.expectedValue = builder.expectedValue;
    }

    /**
     * @return Builder instance to construct a {@link NumericEqualsCondition}.
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * @return The JSONPath expression that determines which piece of the input document is used for the comparison.
     */
    @Override
    public String getVariable() {
        return variable;
    }

    /**
     * @return The expected value for this condition.
     */
    @Override
    @JsonIgnore
    public String getExpectedValue() {
        return expectedValue.asText();
    }

    /**
     * Builder for a {@link NumericEqualsCondition}.
     */
    public static final class Builder extends BinaryConditionBuilder {

        @JsonProperty(PropertyNames.VARIABLE)
        private String variable;

        @JsonProperty(PropertyNames.NUMERIC_EQUALS)
        private NumericNode expectedValue;

        private Builder() {
        }

        /**
         * Sets the JSONPath expression that determines which piece of the input document is used for the comparison.
         *
         * @param variable Reference path.
         * @return This object for method chaining.
         */
        @Override
        public Builder variable(String variable) {
            this.variable = variable;
            return this;
        }

        /**
         * Sets the expected value for this condition.
         *
         * @param expectedValue Expected value.
         * @return This object for method chaining.
         */
        public Builder expectedValue(long expectedValue) {
            this.expectedValue = new LongNode(expectedValue);
            return this;
        }

        /**
         * Sets the expected value for this condition.
         *
         * @param expectedValue Expected value.
         * @return This object for method chaining.
         */
        public Builder expectedValue(double expectedValue) {
            this.expectedValue = new DoubleNode(expectedValue);
            return this;
        }

        @Override
        Builder expectedValue(JsonNode expectedValue) {
            // TODO handle not numeric
            this.expectedValue = (NumericNode) expectedValue;
            return this;
        }

        @Override
        String type() {
            return PropertyNames.NUMERIC_EQUALS;
        }

        /**
         * @return An immutable {@link NumericEqualsCondition} object.
         */
        @Override
        public NumericEqualsCondition build() {
            return new NumericEqualsCondition(this);
        }
    }
}

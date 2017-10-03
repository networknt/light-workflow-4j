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

package com.networknt.workflow.state.builder;

import static org.junit.Assert.assertEquals;
import static com.networknt.workflow.state.builder.StepFunctionBuilder.eq;
import static com.networknt.workflow.state.builder.StepFunctionBuilder.gt;
import static com.networknt.workflow.state.builder.StepFunctionBuilder.gte;
import static com.networknt.workflow.state.builder.StepFunctionBuilder.lt;
import static com.networknt.workflow.state.builder.StepFunctionBuilder.lte;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.time.ZonedDateTime;
import java.util.Date;
import org.junit.Test;

public class StepFunctionBuilderTest {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Test
    public void singleSucceedState() {
        final StateMachine stateMachine = StepFunctionBuilder.stateMachine()
                .startAt("InitialState")
                .timeoutSeconds(30)
                .comment("My Simple State Machine")
                .state("InitialState", StepFunctionBuilder.succeedState()
                        .comment("Initial State")
                        .inputPath("$.input")
                        .outputPath("$.output"))
                .build();
        assertStateMachine(stateMachine, "SingleSucceedState.json");
    }

    @Test
    public void singleTaskState() {
        final StateMachine stateMachine = StepFunctionBuilder.stateMachine()
                .startAt("InitialState")
                .state("InitialState", StepFunctionBuilder.taskState()
                        .comment("Initial State")
                        .timeoutSeconds(10)
                        .heartbeatSeconds(1)
                        .transition(StepFunctionBuilder.next("NextState"))
                        .resource("resource-arn")
                        .inputPath("$.input")
                        .resultPath("$.result")
                        .outputPath("$.output"))
                .state("NextState", StepFunctionBuilder.succeedState())
                .build();

        assertStateMachine(stateMachine, "SimpleTaskState.json");
    }

    @Test
    public void taskStateWithEnd() {
        final StateMachine stateMachine = StepFunctionBuilder.stateMachine()
                .startAt("InitialState")
                .state("InitialState", StepFunctionBuilder.taskState()
                        .resource("resource-arn")
                        .transition(StepFunctionBuilder.end()))
                .build();

        assertStateMachine(stateMachine, "TaskStateWithEnd.json");
    }

    @Test
    public void singleTaskStateWithRetries() {
        final StateMachine stateMachine = StepFunctionBuilder.stateMachine()
                .startAt("InitialState")
                .state("InitialState", StepFunctionBuilder.taskState()
                        .transition(StepFunctionBuilder.next("NextState"))
                        .resource("resource-arn")
                        .retriers(StepFunctionBuilder.retrier()
                                          .errorEquals("Foo", "Bar")
                                          .intervalSeconds(20)
                                          .maxAttempts(3)
                                          .backoffRate(2.0),
                                  StepFunctionBuilder.retrier()
                                          .retryOnAllErrors()
                                          .intervalSeconds(30)
                                          .maxAttempts(10)
                                          .backoffRate(2.0)))
                .state("NextState", StepFunctionBuilder.succeedState())
                .build();

        assertStateMachine(stateMachine, "SimpleTaskStateWithRetries.json");
    }

    @Test
    public void singleTaskStateWithCatchers() {
        final StateMachine stateMachine = StepFunctionBuilder.stateMachine()
                .startAt("InitialState")
                .state("InitialState", StepFunctionBuilder.taskState()
                        .transition(StepFunctionBuilder.next("NextState"))
                        .resource("resource-arn")
                        .catchers(StepFunctionBuilder.catcher()
                                          .errorEquals("Foo", "Bar")
                                          .transition(StepFunctionBuilder.next("RecoveryState"))
                                          .resultPath("$.result-path"),
                                  StepFunctionBuilder.catcher()
                                          .catchAll()
                                          .transition(StepFunctionBuilder.next("OtherRecoveryState"))))
                .state("NextState", StepFunctionBuilder.succeedState())
                .state("RecoveryState", StepFunctionBuilder.succeedState())
                .state("OtherRecoveryState", StepFunctionBuilder.succeedState())
                .build();

        assertStateMachine(stateMachine, "SimpleTaskStateWithCatchers.json");
    }

    @Test
    public void singlePassStateWithJsonResult() {
        final StateMachine stateMachine = StepFunctionBuilder.stateMachine()
                .startAt("InitialState")
                .state("InitialState", StepFunctionBuilder.passState()
                        .comment("Pass through state")
                        .inputPath("$.input")
                        .outputPath("$.output")
                        .resultPath("$.result")
                        .transition(StepFunctionBuilder.next("NextState"))
                        .result("{\"Foo\": \"Bar\"}"))
                .state("NextState", StepFunctionBuilder.succeedState())
                .build();

        assertStateMachine(stateMachine, "SinglePassStateWithJsonResult.json");
    }

    @Test
    public void singlePassStateWithObjectResult() {
        final StateMachine stateMachine = StepFunctionBuilder.stateMachine()
                .startAt("InitialState")
                .state("InitialState", StepFunctionBuilder.passState()
                        .transition(StepFunctionBuilder.end())
                        .result(new SimplePojo("value")))
                .build();

        assertStateMachine(stateMachine, "SinglePassStateWithObjectResult.json");
    }

    @Test
    public void singleWaitState_WaitForSeconds() {
        final StateMachine stateMachine = StepFunctionBuilder.stateMachine()
                .startAt("InitialState")
                .state("InitialState", StepFunctionBuilder.waitState()
                        .comment("My wait state")
                        .inputPath("$.input")
                        .outputPath("$.output")
                        .waitFor(StepFunctionBuilder.seconds(10))
                        .transition(StepFunctionBuilder.next("NextState")))
                .state("NextState", StepFunctionBuilder.succeedState())
                .build();

        assertStateMachine(stateMachine, "SingleWaitStateWithSeconds.json");
    }

    @Test
    public void singleWaitState_WaitUntilSecondsPath() {
        final StateMachine stateMachine = StepFunctionBuilder.stateMachine()
                .startAt("InitialState")
                .state("InitialState", StepFunctionBuilder.waitState()
                        .waitFor(StepFunctionBuilder.secondsPath("$.seconds"))
                        .transition(StepFunctionBuilder.end()))
                .build();

        assertStateMachine(stateMachine, "SingleWaitStateWithSecondsPath.json");
    }

    @Test
    public void singleWaitState_WaitUntilTimestamp() {
        final StateMachine stateMachine = StepFunctionBuilder.stateMachine()
                .startAt("InitialState")
                .state("InitialState", StepFunctionBuilder.waitState()
                        .waitFor(StepFunctionBuilder.timestamp(Date.from(ZonedDateTime.parse("2016-03-14T01:59:00Z").toInstant())))
                        .transition(StepFunctionBuilder.end()))
                .build();

        assertStateMachine(stateMachine, "SingleWaitStateWithTimestamp.json");
    }

    @Test
    public void singleWaitState_WaitUntilTimestampWithMillisecond() {
        long millis = ZonedDateTime.parse("2016-03-14T01:59:00.123Z").toInstant().toEpochMilli();
        final StateMachine stateMachine = StepFunctionBuilder.stateMachine()
                .startAt("InitialState")
                .state("InitialState", StepFunctionBuilder.waitState()
                        .waitFor(StepFunctionBuilder.timestamp(new Date(millis)))
                        .transition(StepFunctionBuilder.end()))
                .build();

        assertStateMachine(stateMachine, "SingleWaitStateWithTimestampWithMilliseconds.json");
    }

    @Test
    public void singleWaitState_WaitUntilTimestampWithTimezone() {
        long epochMilli = ZonedDateTime.parse("2016-03-14T01:59:00.123-08:00").toInstant().toEpochMilli();
        final StateMachine stateMachine = StepFunctionBuilder.stateMachine()
                .startAt("InitialState")
                .state("InitialState", StepFunctionBuilder.waitState()
                        .waitFor(StepFunctionBuilder.timestamp(new Date(epochMilli)))
                        .transition(StepFunctionBuilder.end()))
                .build();

        assertStateMachine(stateMachine, "SingleWaitStateWithTimestampWithTimezone.json");
    }

    @Test
    public void singleWaitState_WaitUntilTimestampWithPath() {
        final StateMachine stateMachine = StepFunctionBuilder.stateMachine()
                .startAt("InitialState")
                .state("InitialState", StepFunctionBuilder.waitState()
                        .waitFor(StepFunctionBuilder.timestampPath("$.timestamp"))
                        .transition(StepFunctionBuilder.end()))
                .build();

        assertStateMachine(stateMachine, "SingleWaitStateWithTimestampWithPath.json");
    }

    @Test
    public void singleFailState() {
        final StateMachine stateMachine = StepFunctionBuilder.stateMachine()
                .startAt("InitialState")
                .state("InitialState", StepFunctionBuilder.failState()
                        .comment("My fail state")
                        .cause("InternalError")
                        .error("java.lang.Exception"))
                .build();

        assertStateMachine(stateMachine, "SingleFailState.json");
    }

    @Test
    public void simpleChoiceState() {
        final StateMachine stateMachine = StepFunctionBuilder.stateMachine()
                .startAt("InitialState")
                .state("InitialState", StepFunctionBuilder.choiceState()
                        .comment("My choice state")
                        .defaultStateName("DefaultState")
                        .inputPath("$.input")
                        .outputPath("$.output")
                        .choice(StepFunctionBuilder.choice().transition(StepFunctionBuilder.next("NextState"))
                                        .condition(StepFunctionBuilder.eq("$.var", "value"))))
                .state("NextState", StepFunctionBuilder.succeedState())
                .state("DefaultState", StepFunctionBuilder.succeedState())
                .build();

        assertStateMachine(stateMachine, "SimpleChoiceState.json");
    }

    @Test
    public void choiceStateWithMultipleChoices() {
        final StateMachine stateMachine = StepFunctionBuilder.stateMachine()
                .startAt("InitialState")
                .state("InitialState", StepFunctionBuilder.choiceState()
                        .defaultStateName("DefaultState")
                        .choices(
                                StepFunctionBuilder.choice().transition(StepFunctionBuilder.next("NextState"))
                                        .condition(StepFunctionBuilder.eq("$.var", "value")),
                                StepFunctionBuilder.choice().transition(StepFunctionBuilder.next("OtherNextState"))
                                        .condition(StepFunctionBuilder.gt("$.number", 10))))
                .state("NextState", StepFunctionBuilder.succeedState())
                .state("OtherNextState", StepFunctionBuilder.succeedState())
                .state("DefaultState", StepFunctionBuilder.succeedState())
                .build();

        assertStateMachine(stateMachine, "ChoiceStateWithMultipleChoices.json");
    }

    @Test
    public void choiceStateWithAndCondition() {
        final StateMachine stateMachine = StepFunctionBuilder.stateMachine()
                .startAt("InitialState")
                .state("InitialState", StepFunctionBuilder.choiceState()
                        .defaultStateName("DefaultState")
                        .choice(StepFunctionBuilder.choice().transition(StepFunctionBuilder.next("NextState"))
                                        .condition(
                                                StepFunctionBuilder.and(StepFunctionBuilder.eq("$.var", "value"),
                                                    StepFunctionBuilder.eq("$.other-var", 10)
                                                   ))))
                .state("NextState", StepFunctionBuilder.succeedState())
                .state("DefaultState", StepFunctionBuilder.succeedState())
                .build();

        assertStateMachine(stateMachine, "ChoiceStateWithAndCondition.json");
    }

    @Test
    public void choiceStateWithOrCondition() {
        final StateMachine stateMachine = StepFunctionBuilder.stateMachine()
                .startAt("InitialState")
                .state("InitialState", StepFunctionBuilder.choiceState()
                        .defaultStateName("DefaultState")
                        .choice(StepFunctionBuilder.choice().transition(StepFunctionBuilder.next("NextState"))
                                        .condition(
                                                StepFunctionBuilder.or(StepFunctionBuilder.gt("$.var", "value"),
                                                   StepFunctionBuilder.lte("$.other-var", 10)
                                                  ))))
                .state("NextState", StepFunctionBuilder.succeedState())
                .state("DefaultState", StepFunctionBuilder.succeedState())
                .build();

        assertStateMachine(stateMachine, "ChoiceStateWithOrCondition.json");
    }

    @Test
    public void choiceStateWithNotCondition() {
        final StateMachine stateMachine = StepFunctionBuilder.stateMachine()
                .startAt("InitialState")
                .state("InitialState", StepFunctionBuilder.choiceState()
                        .defaultStateName("DefaultState")
                        .choice(StepFunctionBuilder.choice().transition(StepFunctionBuilder.next("NextState"))
                                        .condition(StepFunctionBuilder.not(StepFunctionBuilder.gte("$.var", "value")))))
                .state("NextState", StepFunctionBuilder.succeedState())
                .state("DefaultState", StepFunctionBuilder.succeedState())
                .build();

        assertStateMachine(stateMachine, "ChoiceStateWithNotCondition.json");
    }

    @Test
    public void choiceStateWithComplexCondition() {
        final StateMachine stateMachine = StepFunctionBuilder.stateMachine()
                .startAt("InitialState")
                .state("InitialState", StepFunctionBuilder.choiceState()
                        .defaultStateName("DefaultState")
                        .choice(StepFunctionBuilder.choice().transition(StepFunctionBuilder.next("NextState"))
                                        .condition(StepFunctionBuilder.and(
                                                StepFunctionBuilder.gte("$.var", "value"),
                                                StepFunctionBuilder.lte("$.other-var", "foo"),
                                                StepFunctionBuilder.or(
                                                        StepFunctionBuilder.lt("$.numeric", 9000.1),
                                                        StepFunctionBuilder.not(StepFunctionBuilder.gte("$.numeric", 42))
                                                  )
                                                      ))))
                .state("NextState", StepFunctionBuilder.succeedState())
                .state("DefaultState", StepFunctionBuilder.succeedState())
                .build();

        assertStateMachine(stateMachine, "ChoiceStateWithComplexCondition.json");
    }

    @Test
    public void choiceStateWithAllPrimitiveConditions() {
        final Date date = Date.from(ZonedDateTime.parse("2016-03-14T01:59:00.000Z").toInstant());
        final StateMachine stateMachine = StepFunctionBuilder.stateMachine()
                .startAt("InitialState")
                .state("InitialState", StepFunctionBuilder.choiceState()
                        .defaultStateName("DefaultState")
                        .choice(StepFunctionBuilder.choice().transition(StepFunctionBuilder.next("NextState"))
                                        .condition(StepFunctionBuilder.and(
                                                StepFunctionBuilder.eq("$.string", "value"),
                                                StepFunctionBuilder.gt("$.string", "value"),
                                                StepFunctionBuilder.gte("$.string", "value"),
                                                StepFunctionBuilder.lt("$.string", "value"),
                                                StepFunctionBuilder.lte("$.string", "value"),
                                                StepFunctionBuilder.eq("$.integral", 42),
                                                StepFunctionBuilder.gt("$.integral", 42),
                                                StepFunctionBuilder.gte("$.integral", 42),
                                                StepFunctionBuilder.lt("$.integral", 42),
                                                StepFunctionBuilder.lte("$.integral", 42),
                                                StepFunctionBuilder.eq("$.double", 9000.1),
                                                StepFunctionBuilder.gt("$.double", 9000.1),
                                                StepFunctionBuilder.gte("$.double", 9000.1),
                                                StepFunctionBuilder.lt("$.double", 9000.1),
                                                StepFunctionBuilder.lte("$.double", 9000.1),
                                                StepFunctionBuilder.eq("$.timestamp", date),
                                                StepFunctionBuilder.gt("$.timestamp", date),
                                                StepFunctionBuilder.gte("$.timestamp", date),
                                                StepFunctionBuilder.lt("$.timestamp", date),
                                                StepFunctionBuilder.lte("$.timestamp", date),
                                                StepFunctionBuilder.eq("$.boolean", true),
                                                StepFunctionBuilder.eq("$.boolean", false)
                                                      ))))
                .state("NextState", StepFunctionBuilder.succeedState())
                .state("DefaultState", StepFunctionBuilder.succeedState())
                .build();

        assertStateMachine(stateMachine, "ChoiceStateWithAllPrimitiveCondition.json");
    }

    @Test
    public void simpleParallelState() {
        final StateMachine stateMachine = StepFunctionBuilder.stateMachine()
                .startAt("InitialState")
                .state("InitialState", StepFunctionBuilder.parallelState()
                        .comment("My parallel state")
                        .inputPath("$.input")
                        .outputPath("$.output")
                        .resultPath("$.result")
                        .transition(StepFunctionBuilder.next("NextState"))
                        .branches(
                                StepFunctionBuilder.branch()
                                        .comment("Branch one")
                                        .startAt("BranchOneInitial")
                                        .state("BranchOneInitial", StepFunctionBuilder.succeedState()),
                                StepFunctionBuilder.branch()
                                        .comment("Branch two")
                                        .startAt("BranchTwoInitial")
                                        .state("BranchTwoInitial", StepFunctionBuilder.succeedState())
                                 ))
                .state("NextState", StepFunctionBuilder.succeedState())
                .build();

        assertStateMachine(stateMachine, "SimpleParallelState.json");
    }

    @Test
    public void parallelStateWithRetriers() {
        final StateMachine stateMachine = StepFunctionBuilder.stateMachine()
                .startAt("InitialState")
                .state("InitialState", StepFunctionBuilder.parallelState()
                        .transition(StepFunctionBuilder.end())
                        .branches(
                                StepFunctionBuilder.branch()
                                        .comment("Branch one")
                                        .startAt("BranchOneInitial")
                                        .state("BranchOneInitial", StepFunctionBuilder.succeedState()),
                                StepFunctionBuilder.branch()
                                        .comment("Branch two")
                                        .startAt("BranchTwoInitial")
                                        .state("BranchTwoInitial", StepFunctionBuilder.succeedState())
                                 )
                        .retriers(StepFunctionBuilder.retrier()
                                          .errorEquals("Foo", "Bar")
                                          .intervalSeconds(10)
                                          .backoffRate(1.0)
                                          .maxAttempts(3),
                                  StepFunctionBuilder.retrier()
                                          .retryOnAllErrors()
                                          .intervalSeconds(10)
                                          .backoffRate(1.0)
                                          .maxAttempts(3)
                                 ))
                .build();

        assertStateMachine(stateMachine, "ParallelStateWithRetriers.json");
    }

    @Test
    public void parallelStateWithCatchers() {
        final StateMachine stateMachine = StepFunctionBuilder.stateMachine()
                .startAt("InitialState")
                .state("InitialState", StepFunctionBuilder.parallelState()
                        .transition(StepFunctionBuilder.end())
                        .branches(
                                StepFunctionBuilder.branch()
                                        .comment("Branch one")
                                        .startAt("BranchOneInitial")
                                        .state("BranchOneInitial", StepFunctionBuilder.succeedState()),
                                StepFunctionBuilder.branch()
                                        .comment("Branch two")
                                        .startAt("BranchTwoInitial")
                                        .state("BranchTwoInitial", StepFunctionBuilder.succeedState())
                                 )
                        .catchers(StepFunctionBuilder.catcher()
                                          .errorEquals("Foo", "Bar")
                                          .transition(StepFunctionBuilder.next("RecoveryState"))
                                          .resultPath("$.result"),
                                  StepFunctionBuilder.catcher()
                                          .catchAll()
                                          .transition(StepFunctionBuilder.next("OtherRecoveryState"))
                                          .resultPath("$.result")
                                 ))
                .state("RecoveryState", StepFunctionBuilder.succeedState())
                .state("OtherRecoveryState", StepFunctionBuilder.succeedState())
                .build();

        assertStateMachine(stateMachine, "ParallelStateWithCatchers.json");
    }

    @Test(expected = Exception.class)
    public void stateMachineFromJson_MalformedJson_ThrowsException() {
        StateMachine.fromJson("{");
    }

    private void assertStateMachine(StateMachine stateMachine, String resourcePath) {
        final JsonNode expected = loadExpected(resourcePath);
        assertEquals(expected, serialize(stateMachine));
        assertEquals(expected, serialize(roundTripStateMachine(stateMachine)));
    }

    /**
     * Serializes StateMachine into JSON and deserialize back into a StateMachine from the JSON.
     *
     * @param stateMachine State machine to round trip.
     * @return Round-tripped state machine.
     */
    private StateMachine roundTripStateMachine(StateMachine stateMachine) {
        return StateMachine.fromJson(stateMachine.toJson()).build();
    }

    private JsonNode serialize(StateMachine stateMachine) {
        try {
            return MAPPER.readTree(stateMachine.toJson());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private JsonNode loadExpected(String resourcePath) {
        return TestResourceLoader.loadAsJson(resourcePath);
    }

}

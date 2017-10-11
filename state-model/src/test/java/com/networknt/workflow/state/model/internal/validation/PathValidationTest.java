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

package com.networknt.workflow.state.model.internal.validation;

import static com.networknt.workflow.state.model.StepFunctionBuilder.eq;

import com.networknt.workflow.state.model.StepFunctionBuilder;
import org.junit.Test;
import com.networknt.workflow.state.model.states.Catcher;
import com.networknt.workflow.state.model.states.ParallelState;
import com.networknt.workflow.state.model.states.PassState;
import com.networknt.workflow.state.model.states.TaskState;
import com.networknt.workflow.state.model.states.WaitState;

public class PathValidationTest {

    @Test(expected = ValidationException.class)
    public void choiceState_InputPathInvalid_ThrowsException() {
        StepFunctionBuilder.stateMachine()
                .startAt("ChoiceState")
                .state("ChoiceState", StepFunctionBuilder.choiceState()
                        .choice(StepFunctionBuilder.choice()
                                        .condition(StepFunctionBuilder.eq("$.foo", "bar"))
                                        .transition(StepFunctionBuilder.next("End")))
                        .inputPath("$."))
                .state("End", StepFunctionBuilder.succeedState())
                .build();
    }

    @Test(expected = ValidationException.class)
    public void choiceState_OutputPathInvalid_ThrowsException() {
        StepFunctionBuilder.stateMachine()
                .startAt("ChoiceState")
                .state("ChoiceState", StepFunctionBuilder.choiceState()
                        .choice(StepFunctionBuilder.choice()
                                        .condition(StepFunctionBuilder.eq("$.foo", "bar"))
                                        .transition(StepFunctionBuilder.next("End")))
                        .outputPath("["))
                .state("End", StepFunctionBuilder.succeedState())
                .build();
    }

    @Test(expected = ValidationException.class)
    public void choiceState_ConditionWithInvalidPath_ThrowsException() {
        StepFunctionBuilder.stateMachine()
                .startAt("ChoiceState")
                .state("ChoiceState", StepFunctionBuilder.choiceState()
                        .choice(StepFunctionBuilder.choice()
                                        .condition(StepFunctionBuilder.eq("$.", "bar"))
                                        .transition(StepFunctionBuilder.next("End"))))
                .state("End", StepFunctionBuilder.succeedState())
                .build();
    }

    @Test(expected = ValidationException.class)
    public void parallelState_InputPathInvalid_ThrowsException() {
        StepFunctionBuilder.stateMachine()
                .startAt("ParallelState")
                .state("ParallelState", validParallelState().inputPath("$."))
                .build();
    }

    @Test(expected = ValidationException.class)
    public void parallelState_OutputPathInvalid_ThrowsException() {
        StepFunctionBuilder.stateMachine()
                .startAt("ParallelState")
                .state("ParallelState", validParallelState().outputPath("$."))
                .build();
    }

    @Test(expected = ValidationException.class)
    public void parallelState_ResultPathInvalid_ThrowsException() {
        StepFunctionBuilder.stateMachine()
                .startAt("ParallelState")
                .state("ParallelState", validParallelState().resultPath("$."))
                .build();
    }

    @Test(expected = ValidationException.class)
    public void parallelState_ResultPathInvalidReferencePath_ThrowsException() {
        StepFunctionBuilder.stateMachine()
                .startAt("ParallelState")
                .state("ParallelState", validParallelState().resultPath("$.foo[*]"))
                .build();
    }

    @Test(expected = ValidationException.class)
    public void passState_InputPathInvalid_ThrowsException() {
        StepFunctionBuilder.stateMachine()
                .startAt("PassState")
                .state("PassState", validPassState().inputPath("$."))
                .build();
    }

    @Test(expected = ValidationException.class)
    public void passState_OutputPathInvalid_ThrowsException() {
        StepFunctionBuilder.stateMachine()
                .startAt("PassState")
                .state("PassState", validPassState().outputPath("$."))
                .build();
    }

    @Test(expected = ValidationException.class)
    public void passState_ResultPathInvalid_ThrowsException() {
        StepFunctionBuilder.stateMachine()
                .startAt("PassState")
                .state("PassState", validPassState().resultPath("$."))
                .build();
    }

    @Test(expected = ValidationException.class)
    public void passState_ResultPathInvalidReferencePath_ThrowsException() {
        StepFunctionBuilder.stateMachine()
                .startAt("PassState")
                .state("PassState", validPassState().resultPath("$.foo[*]"))
                .build();
    }

    @Test(expected = ValidationException.class)
    public void succeedState_InputPathInvalid_ThrowsException() {
        StepFunctionBuilder.stateMachine()
                .startAt("SucceedState")
                .state("SucceedState", StepFunctionBuilder.succeedState().inputPath("$."))
                .build();
    }

    @Test(expected = ValidationException.class)
    public void succeedState_OutputPathInvalid_ThrowsException() {
        StepFunctionBuilder.stateMachine()
                .startAt("SucceedState")
                .state("SucceedState", StepFunctionBuilder.succeedState().outputPath("$."))
                .build();
    }

    @Test(expected = ValidationException.class)
    public void taskState_InputPathInvalid_ThrowsException() {
        StepFunctionBuilder.stateMachine()
                .startAt("TaskState")
                .state("TaskState", validTaskState().inputPath("$."))
                .build();
    }

    @Test(expected = ValidationException.class)
    public void taskState_OutputPathInvalid_ThrowsException() {
        StepFunctionBuilder.stateMachine()
                .startAt("TaskState")
                .state("TaskState", validTaskState().outputPath("$."))
                .build();
    }

    @Test(expected = ValidationException.class)
    public void taskState_ResultPathInvalid_ThrowsException() {
        StepFunctionBuilder.stateMachine()
                .startAt("TaskState")
                .state("TaskState", validTaskState().resultPath("$."))
                .build();
    }

    @Test(expected = ValidationException.class)
    public void taskState_ResultPathInvalidReferencePath_ThrowsException() {
        StepFunctionBuilder.stateMachine()
                .startAt("TaskState")
                .state("TaskState", validTaskState().resultPath("$.foo[*]"))
                .build();
    }

    @Test(expected = ValidationException.class)
    public void taskState_CatcherWithResultPathInvalid_ThrowsException() {
        StepFunctionBuilder.stateMachine()
                .startAt("TaskState")
                .state("TaskState", validTaskState().catcher(validCatcher("End").resultPath("$.")))
                .state("End", StepFunctionBuilder.succeedState())
                .build();
    }

    @Test(expected = ValidationException.class)
    public void taskState_CatcherResultPathInvalidReferencePath_ThrowsException() {
        StepFunctionBuilder.stateMachine()
                .startAt("TaskState")
                .state("TaskState", validTaskState().catcher(validCatcher("End").resultPath("$.foo[*]")))
                .state("End", StepFunctionBuilder.succeedState())
                .build();
    }

    @Test(expected = ValidationException.class)
    public void waitState_InputPathInvalid_ThrowsException() {
        StepFunctionBuilder.stateMachine()
                .startAt("WaitState")
                .state("WaitState", validWaitState().inputPath("$."))
                .build();
    }

    @Test(expected = ValidationException.class)
    public void waitState_OutputPathInvalid_ThrowsException() {
        StepFunctionBuilder.stateMachine()
                .startAt("WaitState")
                .state("WaitState", validWaitState().inputPath("$.foo[*}"))
                .build();
    }

    private ParallelState.Builder validParallelState() {
        return StepFunctionBuilder.parallelState()
                .comment("My parallel state")
                .inputPath("$.input")
                .outputPath("$.output")
                .resultPath("$.result")
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
                         );
    }

    private WaitState.Builder validWaitState() {
        return StepFunctionBuilder.waitState()
                .waitFor(StepFunctionBuilder.seconds(10))
                .transition(StepFunctionBuilder.end());
    }

    private PassState.Builder validPassState() {
        return StepFunctionBuilder.passState().transition(StepFunctionBuilder.end());
    }

    private TaskState.Builder validTaskState() {
        return StepFunctionBuilder.taskState()
                .resource("foo")
                .transition(StepFunctionBuilder.end());
    }

    private Catcher.Builder validCatcher(String catcherTransition) {
        return StepFunctionBuilder.catcher().transition(StepFunctionBuilder.next(catcherTransition)).catchAll();
    }
}

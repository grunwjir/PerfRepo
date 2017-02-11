
package org.perfrepo.web.adapter.dummy_impl;

import org.perfrepo.dto.metric.MetricDto;
import org.perfrepo.dto.test_execution.*;
import org.perfrepo.dto.util.SearchResult;
import org.perfrepo.dto.util.validation.ValidationErrors;
import org.perfrepo.web.adapter.TestExecutionAdapter;
import org.perfrepo.web.adapter.dummy_impl.storage.Storage;
import org.perfrepo.web.adapter.exceptions.AdapterException;
import org.perfrepo.web.adapter.exceptions.BadRequestException;
import org.perfrepo.web.adapter.exceptions.NotFoundException;
import org.perfrepo.web.adapter.exceptions.ValidationException;

import javax.inject.Inject;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Temporary implementation of {@link TestExecutionAdapter} for development purpose.
 *
 * @author Jiri Grunwald (grunwjir@gmail.com)
 */
public class TestExecutionAdapterDummyImpl implements TestExecutionAdapter {

    @Inject
    private Storage storage;

    @Override
    public TestExecutionDto getTestExecution(Long id) {
        TestExecutionDto testExecution = storage.testExecution().getById(id);

        if (testExecution == null) {
            throw new NotFoundException("Test execution does not exist.");
        }

        return testExecution;
    }

    @Override
    public TestExecutionDto createTestExecution(TestExecutionDto testExecution) {
        validate(testExecution);

        testExecution.setTest(storage.test().getById(testExecution.getTest().getId()));

        // it is not possible to set it, only in test execution detail
        testExecution.setExecutionParameters(new LinkedHashSet<>());
        testExecution.setExecutionValuesGroups(new LinkedHashSet<>());

        return storage.testExecution().create(testExecution);
    }

    @Override
    public TestExecutionDto updateTestExecution(TestExecutionDto testExecution) {
        if (testExecution == null) {
            throw new BadRequestException("Bad input data.");
        }

        if (testExecution.getId() == null) {
            throw new NotFoundException("Test execution does not exist.");
        }

        TestExecutionDto originExecutionTest = storage.testExecution().getById(testExecution.getId());

        if (originExecutionTest == null) {
            throw new NotFoundException("Test execution does not exist.");
        }

        validate(testExecution);

        testExecution.setExecutionParameters(originExecutionTest.getExecutionParameters());
        testExecution.setExecutionValuesGroups(originExecutionTest.getExecutionValuesGroups());

        return storage.testExecution().update(testExecution);
    }

    @Override
    public TestExecutionDto updateTestExecutionParameters(Long testExecutionId, Set<ParameterDto> testExecutionParameters) {
        TestExecutionDto testExecution = storage.testExecution().getById(testExecutionId);

        if (testExecution == null) {
            throw new NotFoundException("Test execution does not exist.");
        }

        validateParameters(testExecutionParameters);

        testExecution.setExecutionParameters(testExecutionParameters);
        return testExecution;
    }

    @Override
    public void removeTestExecution(Long id) {
        TestExecutionDto testExecution = storage.testExecution().getById(id);

        if (testExecution == null) {
            throw new NotFoundException("Test execution does not exist.");
        }

        boolean removed = storage.testExecution().delete(testExecution.getId());

        if (!removed) {
            throw new AdapterException("You can not delete test execution.");
        }
    }

    @Override
    public List<TestExecutionDto> getAllTestExecutions() {
        return storage.testExecution().getAll();
    }

    @Override
    public SearchResult<TestExecutionDto> searchTestExecutions(TestExecutionSearchCriteria searchParams) {
        // delay...
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return storage.testExecution().search(searchParams);
    }

    @Override
    public AttachmentDto getTestExecutionAttachment(Long attachmentId) {

        AttachmentDto a = new AttachmentDto();

        String x = "ahoj jak se mas";
        a.setFilename("hello.txt");
        a.setContent(x.getBytes());

        return a;
    }

    @Override
    public TestExecutionDto addExecutionValues(Long testExecutionId, Long metricId, List<ValueDto> executionValues) {
        return updateExecutionValues(testExecutionId, metricId, executionValues, true);
    }

    @Override
    public TestExecutionDto setExecutionValues(Long testExecutionId, Long metricId, List<ValueDto> executionValues) {
        return updateExecutionValues(testExecutionId, metricId, executionValues, false);
    }

    private void validate(TestExecutionDto testExecution) {
        ValidationErrors validation = new ValidationErrors();

        // test name
        if (testExecution.getName() == null) {
            validation.addFieldError("name", "Test execution name is a required field");
        } else if (testExecution.getName().trim().length() < 3) {
            validation.addFieldError("name", "Test execution name must be at least three characters.");
        }

        if (testExecution.getTags() == null) {
            validation.addFieldError("tags", "Test execution tags is a required field");
        } else if (testExecution.getTags().isEmpty()) {
            validation.addFieldError("tags", "Test execution tags is a required field");
        }

        // test description
        if (testExecution.getComment() != null && testExecution.getComment().length() > 500) {
            validation.addFieldError("comment", "Test execution description must not be more than 500 characters.");
        }

        if (validation.hasFieldErrors()) {
            throw new ValidationException("Test execution contains validation errors, please fix it.", validation);
        }
    }

    private void validateParameters(Set<ParameterDto> parameters) {
        ValidationErrors validation = new ValidationErrors();

        if (parameters != null && !parameters.isEmpty()) {

            parameters.forEach(parameter -> {
                if (parameter.getName() == null || parameter.getName().length() < 3) {
                    validation.addFieldError("name", "Parameter name must be at least three characters.");
                }

                if (parameter.getValue() == null || parameter.getValue().length() < 3) {
                    validation.addFieldError("value", "Parameter value must be at least three characters.");
                }
            });
        }

        if (validation.hasFieldErrors()) {
            throw new ValidationException("Test execution contains validation errors, please fix it.", validation);
        }
    }

    private void validateValues(Long testExecutionId, Long metricId, List<ValueDto> executionValues) {
        if (testExecutionId == null || storage.testExecution().getById(testExecutionId) == null) {
            throw new BadRequestException("Not existing test execution.");
        }

        if (metricId == null || storage.metric().getById(metricId) == null) {
            throw new BadRequestException("Not existing metric.");
        }
    }

    private TestExecutionDto updateExecutionValues(Long testExecutionId, Long metricId, List<ValueDto> executionValues, boolean appendValues) {
        validateValues(testExecutionId, metricId, executionValues);

        TestExecutionDto testExecution = storage.testExecution().getById(testExecutionId);
        MetricDto metric = storage.metric().getById(metricId);

        ValuesGroupDto valueGroupDto = null;

        if (testExecution.getExecutionValuesGroups() != null) {
            // find existing values group
            Optional<ValuesGroupDto> valuesGroupOptional = testExecution.getExecutionValuesGroups()
                    .stream()
                    .filter(valuesGroupDto -> valuesGroupDto.getMetricId().equals(metricId)).findFirst();
            valueGroupDto = valuesGroupOptional.isPresent() ? valuesGroupOptional.get() : null;
        }

        if (valueGroupDto == null) {
            // values group doesn't exist
            valueGroupDto = new ValuesGroupDto();
            valueGroupDto.setMetricId(metricId);
            if (testExecution.getExecutionValuesGroups() == null) {
                testExecution.setExecutionValuesGroups(new LinkedHashSet<>());
            }
            testExecution.getExecutionValuesGroups().add(valueGroupDto);
        }

        if (valueGroupDto.getValues() == null) {
            valueGroupDto.setValues(new ArrayList<>());
        }

        if (!appendValues) {
            valueGroupDto.getValues().clear();
        }
        // add values
        valueGroupDto.getValues().addAll(executionValues);

        // update parameter names
        Set<String> parameterNames = valueGroupDto.getValues()
                .stream().filter(valueObject -> valueObject != null && valueObject.getParameters() != null)
                .flatMap(valueObject -> valueObject.getParameters().stream().map(parameterDto -> parameterDto.getName()))
                .collect(Collectors.toSet());
        valueGroupDto.setParameterNames(parameterNames);

        return storage.testExecution().getById(testExecutionId);
    }
}

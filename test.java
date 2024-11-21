import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;

import java.util.Set;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class LoanscapeRuleValidationCustomTest<T> {
    private LoanscapeRuleValidationCustom<T> validationCustom;
    private Function<T, Object> mockValidationFunction;
    private LoanscapeMessageType mockErrorType;

    @BeforeEach
    void setUp() {
        mockValidationFunction = mock(Function.class);
        mockErrorType = mock(LoanscapeMessageType.class);
        validationCustom = new LoanscapeRuleValidationCustom<>(mockValidationFunction, mockErrorType);
    }

    @Test
    void validate_ShouldReturnNull_WhenValidationFunctionReturnsNull() {
        T businessObject = mock(Object.class);
        when(mockValidationFunction.apply(businessObject)).thenReturn(null);

        assertNull(validationCustom.validate(businessObject));
    }

    @Test
    void validate_ShouldReturnInfoFunctionalMessage_WhenValidationFunctionReturnsFalse() {
        T businessObject = mock(Object.class);
        when(mockValidationFunction.apply(businessObject)).thenReturn(false);

        FunctionalInformation result = validationCustom.validate(businessObject);

        assertNotNull(result);
        assertEquals("Expected info message", result.getMessage());
    }

    @Test
    void validate_ShouldThrowException_WhenValidationFunctionReturnsInvalidType() {
        T businessObject = mock(Object.class);
        when(mockValidationFunction.apply(businessObject)).thenReturn("InvalidType");

        assertThrows(IllegalArgumentException.class, () -> validationCustom.validate(businessObject));
    }

    @Test
    void validate_ShouldReturnErrorFunctionalMessage_WhenValidationFunctionReturnsNonEmptySet() {
        T businessObject = mock(Object.class);
        Set<String> errorSet = Set.of("error1", "error2");
        when(mockValidationFunction.apply(businessObject)).thenReturn(errorSet);

        FunctionalInformation result = validationCustom.validate(businessObject);

        assertNotNull(result);
        assertEquals("Expected error message", result.getMessage());
    }

    @Test
    void validate_ShouldReturnInfoFunctionalMessage_WhenValidationFunctionReturnsEmptySet() {
        T businessObject = mock(Object.class);
        Set<String> errorSet = Set.of();
        when(mockValidationFunction.apply(businessObject)).thenReturn(errorSet);

        FunctionalInformation result = validationCustom.validate(businessObject);

        assertNotNull(result);
        assertEquals("Expected info message", result.getMessage());
    }

    @Test
    void getInfoFunctionalMessage_ShouldReturnCorrectFunctionalInformation() {
        T businessObject = mock(Object.class);
        FunctionalInformation result = validationCustom.getInfoFunctionalMessage(businessObject);

        assertNotNull(result);
        assertEquals(mockErrorType, result.getErrorType());
    }

    @Test
    void getErrorFunctionalMessage_ShouldReturnCorrectFunctionalInformation() {
        T businessObject = mock(Object.class);
        FunctionalInformation result = validationCustom.getErrorFunctionalMessage(businessObject);

        assertNotNull(result);
        assertEquals(mockErrorType, result.getErrorType());
    }

    import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;

import java.util.Set;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class LoanscapeRuleValidationCustomTest {
    private LoanscapeRuleValidationCustom<String> validationCustom;
    private Function<String, Object> mockValidationFunction;
    private LoanscapeMessageType mockErrorType;

    @BeforeEach
    void setUp() {
        mockValidationFunction = mock(Function.class);
        mockErrorType = mock(LoanscapeMessageType.class);
        validationCustom = new LoanscapeRuleValidationCustom<>(mockValidationFunction, mockErrorType);
    }

    @Test
    void validate_ShouldReturnNull_WhenValidationFunctionReturnsNull() {
        String businessObject = "TestObject";
        when(mockValidationFunction.apply(businessObject)).thenReturn(null);

        assertNull(validationCustom.validate(businessObject));
    }

    @Test
    void validate_ShouldReturnInfoFunctionalMessage_WhenValidationFunctionReturnsFalse() {
        String businessObject = "TestObject";
        when(mockValidationFunction.apply(businessObject)).thenReturn(false);

        FunctionalInformation result = validationCustom.validate(businessObject);

        assertNotNull(result);
        // Adjust the following assertion based on your expected message
        assertEquals("Expected info message", result.getMessage());
    }

    @Test
    void validate_ShouldThrowException_WhenValidationFunctionReturnsInvalidType() {
        String businessObject = "TestObject";
        when(mockValidationFunction.apply(businessObject)).thenReturn("InvalidType");

        assertThrows(IllegalArgumentException.class, () -> validationCustom.validate(businessObject));
    }

    @Test
    void validate_ShouldReturnErrorFunctionalMessage_WhenValidationFunctionReturnsNonEmptySet() {
        String businessObject = "TestObject";
        Set<String> errorSet = Set.of("error1", "error2");
        when(mockValidationFunction.apply(businessObject)).thenReturn(errorSet);

        FunctionalInformation result = validationCustom.validate(businessObject);

        assertNotNull(result);
        // Adjust the following assertion based on your expected message
        assertEquals("Expected error message", result.getMessage());
    }

    @Test
    void validate_ShouldReturnInfoFunctionalMessage_WhenValidationFunctionReturnsEmptySet() {
        String businessObject = "TestObject";
        Set<String> errorSet = Set.of();
        when(mockValidationFunction.apply(businessObject)).thenReturn(errorSet);

        FunctionalInformation result = validationCustom.validate(businessObject);

        assertNotNull(result);
        // Adjust the following assertion based on your expected message
        assertEquals("Expected info message", result.getMessage());
    }

    @Test
    void getInfoFunctionalMessage_ShouldReturnCorrectFunctionalInformation() {
        String businessObject = "TestObject";
        FunctionalInformation result = validationCustom.getInfoFunctionalMessage(businessObject);

        assertNotNull(result);
        // Adjust the assertions based on the expected values
        assertEquals(mockErrorType, result.getErrorType());
    }

    @Test
    void getErrorFunctionalMessage_ShouldReturnCorrectFunctionalInformation() {
        String businessObject = "TestObject";
        FunctionalInformation result = validationCustom.getErrorFunctionalMessage(businessObject);

        assertNotNull(result);
        // Adjust the assertions based on the expected values
        assertEquals(mockErrorType, result.getErrorType());
    }
}

}

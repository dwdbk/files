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
}

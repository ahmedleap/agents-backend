package com.agentsbackend.DTO.requests;

import com.agentsbackend.enums.PortfolioSizeRange;
import com.agentsbackend.enums.RiskTolerance;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("UpdateClientRequestDTO Tests")
class UpdateClientRequestDTOTest {

    private static Validator validator;

    @BeforeAll
    static void setUpValidator() {
        validator = Validation.buildDefaultValidatorFactory().getValidator();
    }

    private Set<ConstraintViolation<UpdateClientRequestDTO>> validate(UpdateClientRequestDTO dto) {
        return validator.validate(dto);
    }

    @Test
    @DisplayName("All fields null is valid (nothing sent means nothing to check)")
    void allNullIsValid() {
        assertTrue(validate(new UpdateClientRequestDTO()).isEmpty());
    }

    @Test
    @DisplayName("All fields valid")
    void allFieldsValid() {
        UpdateClientRequestDTO dto = new UpdateClientRequestDTO("Jane", "Q", "Doe",
                RiskTolerance.CONSERVATIVE, PortfolioSizeRange.OVER_500K);
        assertTrue(validate(dto).isEmpty());
    }

    @Test
    @DisplayName("Empty middle name is allowed so it can be cleared")
    void emptyStringClearsMiddleName() {
        UpdateClientRequestDTO dto = new UpdateClientRequestDTO();
        dto.setMiddleName("");
        assertTrue(validate(dto).isEmpty());
    }

    @Test
    @DisplayName("Middle name made of only spaces is allowed (unlike first and last name)")
    void spacesOnlyMiddleNameAllowed() {
        UpdateClientRequestDTO dto = new UpdateClientRequestDTO();
        dto.setMiddleName("   ");
        assertTrue(validate(dto).isEmpty());
    }

    @Test
    @DisplayName("Blank first or last name is rejected")
    void blankNamesRejected() {
        UpdateClientRequestDTO dto = new UpdateClientRequestDTO();
        dto.setFirstName("");
        dto.setLastName("   ");
        assertEquals(2, validate(dto).size());
    }

    @Test
    @DisplayName("Names longer than 50 characters are rejected")
    void longNamesRejected() {
        UpdateClientRequestDTO dto = new UpdateClientRequestDTO();
        dto.setFirstName("a".repeat(51));
        dto.setMiddleName("b".repeat(51));
        dto.setLastName("c".repeat(51));
        assertEquals(3, validate(dto).size());
    }

    @Test
    @DisplayName("hasAnyField is false only when every field is null")
    void hasAnyField() {
        assertFalse(new UpdateClientRequestDTO().hasAnyField());

        UpdateClientRequestDTO onlyEnum = new UpdateClientRequestDTO();
        onlyEnum.setPortfolioSizeRange(PortfolioSizeRange.UNDER_50K);
        assertTrue(onlyEnum.hasAnyField());

        UpdateClientRequestDTO onlyEmptyString = new UpdateClientRequestDTO();
        onlyEmptyString.setMiddleName("");
        assertTrue(onlyEmptyString.hasAnyField());
    }

    @Test
    @DisplayName("toString includes the fields")
    void toStringIncludesFields() {
        UpdateClientRequestDTO dto = new UpdateClientRequestDTO();
        dto.setFirstName("Jane");
        assertTrue(dto.toString().contains("firstName='Jane'"));
    }
}

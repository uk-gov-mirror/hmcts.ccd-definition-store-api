package uk.gov.hmcts.ccd.definition.store.domain.validation.casefield;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import uk.gov.hmcts.ccd.definition.store.domain.validation.ValidationResult;
import uk.gov.hmcts.ccd.definition.store.repository.entity.AccessProfileEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseFieldACLEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseFieldEntity;

import static org.hamcrest.collection.IsEmptyCollection.empty;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsInstanceOf.instanceOf;
import static org.junit.Assert.assertThat;
import static org.mockito.BDDMockito.given;

public class CaseFieldEntityACLValidatorImplTest {

    @Rule
    public MockitoRule rule = MockitoJUnit.rule();

    private CaseFieldEntityACLValidatorImpl validator;

    private CaseFieldACLEntity caseFieldUserRole;

    private CaseFieldEntity caseField;

    @Mock
    private AccessProfileEntity userRole;

    @Mock
    private CaseFieldEntityValidationContext caseFieldEntityValidationContext;

    @Before
    public void setup() {

        caseFieldUserRole = new CaseFieldACLEntity();

        given(caseFieldEntityValidationContext.getCaseReference()).willReturn("case_type");

        caseField = new CaseFieldEntity();
        caseField.addCaseFieldACL(caseFieldUserRole);
        caseField.setReference("case_field");

        validator = new CaseFieldEntityACLValidatorImpl();
    }

    @Test
    public void shouldHaveValidationErrorWhenUserNotFound() {

        caseFieldUserRole.setAccessProfile(null);
        caseFieldUserRole.setAccessProfileId("nf_access_profile_id");
        final ValidationResult result = validator.validate(caseField, caseFieldEntityValidationContext);

        assertThat(result.getValidationErrors().size(), is(1));
        assertThat(result.getValidationErrors().get(0),
            instanceOf(CaseFieldEntityInvalidUserRoleValidationError.class));
        assertThat(result.getValidationErrors().get(0).getDefaultMessage(), is(
            "Invalid UserRole nf_access_profile_id for case type 'case_type', case field 'case_field'"));
    }

    @Test
    public void shouldHaveNoValidationErrorWhenAccessProfileFound() {

        caseFieldUserRole.setAccessProfile(userRole);

        final ValidationResult result = validator.validate(caseField, caseFieldEntityValidationContext);

        assertThat(result.getValidationErrors(), empty());
    }
}

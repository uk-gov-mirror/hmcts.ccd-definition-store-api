package uk.gov.hmcts.ccd.definition.store.domain.validation.casefield;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.ccd.definition.store.domain.validation.ValidationResult;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseFieldEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseTypeEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CategoryEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.FieldTypeEntity;

import static java.util.Collections.singletonList;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

@DisplayName("Categories can be only set on the Document CaseField type")
class CaseFieldEntityCategoryValidatorImplTest {

    public static final String SOME_CATEGORY_ID = "someCategoryId";
    public static final String SOME_CASE_TYPE = "someCaseType";
    public static final String VALID_CATEGORY_ID = "validCategoryId";
    public static final String OTHER_CATEGORY_ID = "otherCategoryId";
    public static final String INVALID_CATEGORY_ID1 = "invalid^CategoryId";
    public static final String INVALID_CATEGORY_ID2 = "##someInvalidCategoryId";

    private FieldTypeEntity fieldTypeEntity;
    private CaseFieldEntity caseFieldEntity;

    private CaseFieldEntityCategoryValidatorImpl instance = new CaseFieldEntityCategoryValidatorImpl();

    @BeforeEach
    public void setUp() {
        fieldTypeEntity = mock(FieldTypeEntity.class);

        CaseTypeEntity caseTypeEntity = mock(CaseTypeEntity.class);
        given(caseTypeEntity.getReference()).willReturn("someCaseType");

        given(fieldTypeEntity.getReference()).willReturn("Document");
        given(fieldTypeEntity.isDocumentType()).willReturn(true);

        caseFieldEntity = new CaseFieldEntity();
        caseFieldEntity.setCategoryId(SOME_CATEGORY_ID);
        caseFieldEntity.setCaseType(caseTypeEntity);
        caseFieldEntity.setFieldType(fieldTypeEntity);
    }

    @DisplayName("Category can be only set for Document field type")
    @Nested
    class DocumentFieldType {

        @DisplayName("Should not fire ValidationError when Category set on Document type")
        @Test
        public void shouldNotFireValidationErrorWhenCategorySetOnDocumentType() {
            given(fieldTypeEntity.getReference()).willReturn("Document");
            given(fieldTypeEntity.isDocumentType()).willReturn(true);

            ValidationResult validationResult = instance.validate(caseFieldEntity, createCaseFieldEntityValidationContext());

            assertTrue(validationResult.isValid());
        }

        @DisplayName("Should fire ValidationError when Category set on non Document type")
        @Test
        public void shouldFireValidationErrorWhenCategorySetOnNonDocumentType() {
            given(fieldTypeEntity.getReference()).willReturn("Text");
            given(fieldTypeEntity.isDocumentType()).willReturn(false);

            ValidationResult validationResult = instance.validate(caseFieldEntity, createCaseFieldEntityValidationContext());

            assertFalse(validationResult.isValid());
            assertEquals(1, validationResult.getValidationErrors().size());

            assertTrue(validationResult.getValidationErrors().get(0) instanceof
                CaseFieldEntityCategoryValidatorImpl.ValidationError);

            assertEquals(
                caseFieldEntity,
                ((CaseFieldEntityCategoryValidatorImpl.ValidationError) validationResult.getValidationErrors().get(0))
                    .getEntity()
            );
        }
    }

    @DisplayName("CategoryId format")
    @Nested
    class CategoryIdFormat {

        @DisplayName("Should not fire ValidationError for 'validCategoryId' CategoryId")
        @Test
        public void shouldNotFireValidationErrorWhenCategoryIdValidReferencingCategoriesTab() {
            caseFieldEntity.setCategoryId(VALID_CATEGORY_ID);

            ValidationResult validationResult = instance.validate(caseFieldEntity, createCaseFieldEntityValidationContext("validCategoryId"));

            assertTrue(validationResult.isValid());
        }


        @DisplayName("Should not fire ValidationError for '#validCategoryId' CategoryId")
//        @Test TODO: RDM-7984
        public void shouldNotFireValidationErrorWhenCategoryIdValidReferencingFixedListsTab() {
            caseFieldEntity.setCategoryId("#validCategoryId");

            ValidationResult validationResult = instance.validate(caseFieldEntity, createCaseFieldEntityValidationContext("#validCategoryId"));

            assertTrue(validationResult.isValid());
        }

        @DisplayName("Should fire ValidationError for 'invalid^CategoryId' CategoryId")
        @Test
        public void shouldFireValidationErrorWhenCategoryIdInvalid() {
            caseFieldEntity.setCategoryId(INVALID_CATEGORY_ID1);

            ValidationResult validationResult = instance.validate(caseFieldEntity, createCaseFieldEntityValidationContext());

            assertFalse(validationResult.isValid());
            assertEquals(1, validationResult.getValidationErrors().size());

            assertTrue(validationResult.getValidationErrors().get(0) instanceof
                CaseFieldEntityCategoryValidatorImpl.ValidationError);

            assertEquals(
                caseFieldEntity,
                ((CaseFieldEntityCategoryValidatorImpl.ValidationError) validationResult.getValidationErrors().get(0))
                    .getEntity()
            );
        }

        @DisplayName("Should fire ValidationError for '##someInvalidCategoryId' CategoryId")
        @Test
        public void shouldFireValidationErrorWhenCategoryIdInvalid1() {
            caseFieldEntity.setCategoryId(INVALID_CATEGORY_ID2);

            ValidationResult validationResult = instance.validate(caseFieldEntity, createCaseFieldEntityValidationContext());

            assertFalse(validationResult.isValid());
            assertEquals(1, validationResult.getValidationErrors().size());
        }
    }

    @DisplayName("Check CategoryId defined in Categories")
    @Nested
    class CheckCategoryIdDefinedInCategories {

        @DisplayName("Should not fire ValidationError when CategoryId referencing Categories tab")
        @Test
        public void shouldNotFireValidationErrorWhenCategoryIdReferencingCategoriesTab() {
            caseFieldEntity.setCategoryId(VALID_CATEGORY_ID);

            ValidationResult validationResult = instance
                .validate(caseFieldEntity, createCaseFieldEntityValidationContext(VALID_CATEGORY_ID));

            assertTrue(validationResult.isValid());
        }

        @DisplayName("Should fire ValidationError when CategoryId not referencing Categories tab")
        @Test
        public void shouldFireValidationErrorWhenCategoryIdNotReferencingCategoriesTab() {
            caseFieldEntity.setCategoryId(VALID_CATEGORY_ID);

            ValidationResult validationResult = instance
                .validate(caseFieldEntity, createCaseFieldEntityValidationContext(OTHER_CATEGORY_ID));

            assertFalse(validationResult.isValid());
            assertEquals(1, validationResult.getValidationErrors().size());

            assertTrue(validationResult.getValidationErrors().get(0) instanceof
                CaseFieldEntityCategoryValidatorImpl.ValidationError);

            assertEquals(
                caseFieldEntity,
                ((CaseFieldEntityCategoryValidatorImpl.ValidationError) validationResult.getValidationErrors().get(0))
                    .getEntity()
            );
        }
    }

    private CaseFieldEntityValidationContext createCaseFieldEntityValidationContext(String categoryId) {
        CategoryEntity categoryEntity = new CategoryEntity();
        categoryEntity.setCategoryId(categoryId);

        CaseTypeEntity caseTypeEntity = mock(CaseTypeEntity.class);
        given(caseTypeEntity.getReference()).willReturn(SOME_CASE_TYPE);
        given(caseTypeEntity.getCategories()).willReturn(singletonList(categoryEntity));
        return new CaseFieldEntityValidationContext(caseTypeEntity);
    }

    private CaseFieldEntityValidationContext createCaseFieldEntityValidationContext() {
        return createCaseFieldEntityValidationContext(SOME_CATEGORY_ID);
    }
}

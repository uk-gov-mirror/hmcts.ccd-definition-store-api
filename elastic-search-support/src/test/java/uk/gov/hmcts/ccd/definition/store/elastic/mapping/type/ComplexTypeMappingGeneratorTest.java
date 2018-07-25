package uk.gov.hmcts.ccd.definition.store.elastic.mapping.type;

import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.ccd.definition.store.elastic.hamcresutil.IsEqualJSON.equalToJSONInFile;
import static uk.gov.hmcts.ccd.definition.store.utils.CaseFieldBuilder.newField;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.runners.MockitoJUnitRunner;
import uk.gov.hmcts.ccd.definition.store.elastic.TestUtils;
import uk.gov.hmcts.ccd.definition.store.elastic.mapping.AbstractMapperTest;
import uk.gov.hmcts.ccd.definition.store.elastic.mapping.StubTypeMappingGenerator;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseFieldEntity;
import uk.gov.hmcts.ccd.definition.store.utils.CaseFieldBuilder;
import uk.gov.hmcts.ccd.definition.store.utils.FieldTypeBuilder;

@RunWith(MockitoJUnitRunner.class)
public class ComplexTypeMappingGeneratorTest extends AbstractMapperTest implements TestUtils {

    @InjectMocks
    private ComplexTypeMappingGenerator complexTypeMapper;

    @Before
    public void setup() {
        super.setup();

        when(config.getSecurityClassificationMapping()).thenReturn("securityClassificationMapping");
        addMappingGenerator(new StubTypeMappingGenerator("Text", "dataMapping","dataClassificationMapping"));
        complexTypeMapper.inject(stubTypeMappersManager);
    }

    @Test
    public void shouldGenerateMappingForAllComplexTypeSubfields() {
        CaseFieldEntity personAddress = newComplexField();

        String result = complexTypeMapper.dataMapping(personAddress);

        assertThat(result, equalToJSONInFile(
                readFileFromClasspath("json/type_mapping_complex_type.json")));
    }

    @Test
    public void shouldGenerateClassificationMappingForAllComplexTypeSubfields() {
        CaseFieldEntity personAddress = newComplexField();

        String result = complexTypeMapper.dataClassificationMapping(personAddress);

        assertThat(result, equalToJSONInFile(
                readFileFromClasspath("json/type_mapping_classification_complex_type.json")));

    }

    private CaseFieldEntity newComplexField() {
        CaseFieldBuilder complexField = newField("personAddress", "Address");
        complexField.withComplexField("AddressLine1", FieldTypeBuilder.textFieldType());
        complexField.withComplexField("AddressLine2", FieldTypeBuilder.textFieldType());
        return complexField.buildComplex();
    }
}
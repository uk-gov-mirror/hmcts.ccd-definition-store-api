package uk.gov.hmcts.ccd.definition.store.domain.showcondition;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class ShowConditionParser {

    private static final Logger LOG = LoggerFactory.getLogger(ShowConditionParser.class);

    private static final String AND_CONDITION_REGEX = "\\sAND\\s(?=(([^\"]*\"){2})*[^\"]*$)";
    private static final String AND_OPERATOR = " AND ";
    private static final String OR_CONDITION_REGEX = "\\sOR\\s(?=(([^\"]*\"){2})*[^\"]*$)";
    private Pattern orConditionPattern = Pattern.compile(OR_CONDITION_REGEX);
    private static final String OR_OPERATOR = " OR ";
    private static final Pattern EQUALITY_CONDITION_PATTERN = Pattern.compile("\\s*?(.*)\\s*?(=|CONTAINS)\\s*?(\".*\")\\s*?");
    private static final Pattern NOT_EQUAL_CONDITION_PATTERN = Pattern.compile("\\s*?(.*)\\s*?(!=|CONTAINS)\\s*?(\"" +
                                                                               ".*\")\\s*?");

    public ShowCondition parseShowCondition(String rawShowConditionString) throws InvalidShowConditionException {
        try {
            if (rawShowConditionString != null) {
                String andOrOperator = AND_OPERATOR;
                String[] conditions;
                Matcher matcher = orConditionPattern.matcher(rawShowConditionString);
                if (matcher.find()) {
                    conditions = rawShowConditionString.split(OR_CONDITION_REGEX);
                    andOrOperator = OR_OPERATOR;
                } else {
                    conditions = rawShowConditionString.split(AND_CONDITION_REGEX);
                }
                Optional<ShowCondition> optShowCondition = buildShowCondition(conditions, andOrOperator);
                if (optShowCondition.isPresent()) {
                    ShowCondition showCondition = optShowCondition.get();
                    if (showCondition.getFieldsWithSubtypes().stream().noneMatch(this::fieldContainsEmpties)
                        && showCondition.getFields().stream().noneMatch(this::fieldContainsEmpties)) {
                        return showCondition;
                    }
                }
            }
        } catch (Exception e) {
            LOG.error("Error occurred while parsing show condition", e);
            // Do nothing; we're throwing InvalidShowConditionException below
        }
        throw new InvalidShowConditionException(rawShowConditionString);
    }

    private boolean fieldContainsEmpties(String field) {
        return field.contains(" ") || field.contains("..");
    }

    private Optional<ShowCondition> buildShowCondition(String[] andConditions, String andOrOperator) {
        ShowCondition.Builder showConditionBuilder = new ShowCondition.Builder();
        String operator = "";
        Matcher matcher = null;

        for (String andCondition : andConditions) {
            Matcher notEqualityMatcher = NOT_EQUAL_CONDITION_PATTERN.matcher(andCondition);
            if(notEqualityMatcher.find()) {
                matcher = notEqualityMatcher;
            } else {
                Matcher equalityMatcher = EQUALITY_CONDITION_PATTERN.matcher(andCondition);
                if(equalityMatcher.find()) {
                    matcher = equalityMatcher;
                }
            }
            if (matcher != null) {
                showConditionBuilder
                    .showConditionExpression(operator + parseEqualityCondition(matcher))
                    .field(getLeftHandSideOfEquals(matcher));
                operator = andOrOperator;
            } else {
                return Optional.empty();
            }
        }
        if (showConditionBuilder.hasShowCondition()) {
            return Optional.of(showConditionBuilder.build());
        }
        return Optional.empty();
    }

    private String parseEqualityCondition(Matcher matcher) {
        return getLeftHandSideOfEquals(matcher) + getEqualsSign(matcher) + getRightHandSideOfEquals(matcher);
    }

    private String getLeftHandSideOfEquals(Matcher matcher) {
        return matcher.group(1).trim();
    }

    private String getEqualsSign(Matcher matcher) {
        return matcher.group(2).trim();
    }

    private String getRightHandSideOfEquals(Matcher matcher) {
        return matcher.group(3).trim();
    }
}

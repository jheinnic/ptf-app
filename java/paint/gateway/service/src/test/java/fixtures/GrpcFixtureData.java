package fixtures;

import java.util.UUID;

import lombok.experimental.UtilityClass;

@UtilityClass
public class GrpcFixtureData
{
	public static final String VALID_EXAMPLE_ID_ONE_STRING = "3ddeb300-3871-11e8-aec1-e4ce8f47a632"; 
	public static final UUID VALID_EXAMPLE_ID_ONE_UUID = UUID.fromString(VALID_EXAMPLE_ID_ONE_STRING);

	public static final String VALID_EXAMPLE_ID_TWO_STRING = "3ffeb301-3871-11e8-aec1-e4ce8f47a632"; 
	public static final UUID VALID_EXAMPLE_ID_TWO_UUID = UUID.fromString(VALID_EXAMPLE_ID_TWO_STRING);

	public static final String VALID_PILOT_ID_ONE_STRING = "3aaeb2fe-3871-11e8-aec1-e4ce8f47a632"; 
	public static final UUID VALID_PILOT_ID_ONE_UUID = UUID.fromString(VALID_PILOT_ID_ONE_STRING);

	public static final String VALID_PILOT_ID_TWO_STRING = "3cceb2ff-3871-11e8-aec1-e4ce8f47a632"; 
	public static final UUID VALID_PILOT_ID_TWO_UUID = UUID.fromString(VALID_PILOT_ID_TWO_STRING);

	public static final String VALID_FIRST_NAME_ONE = "firstNameOne";
	public static final String VALID_FIRST_NAME_TWO = "firstNameTwo";

	public static final String VALID_MIDDLE_NAME_ONE = "middleNameOne";
	public static final String VALID_MIDDLE_NAME_TWO = "middleNameTwo";

	public static final String VALID_LAST_NAME_ONE = "lastNameOne";
	public static final String VALID_LAST_NAME_TWO = "lastNameTwo";
}

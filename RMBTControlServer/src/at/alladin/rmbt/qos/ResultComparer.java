/*******************************************************************************
 * Copyright 2013-2015 alladin-IT GmbH
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package at.alladin.rmbt.qos;

import java.lang.reflect.Field;
import java.util.Collection;

import at.alladin.rmbt.qos.testscript.TestScriptInterpreter;
import at.alladin.rmbt.shared.hstoreparser.Hstore;
import at.alladin.rmbt.shared.hstoreparser.HstoreParser;


/**
 * 
 * @author lb
 *
 */
public class ResultComparer {
	public final static int RESULT_COULD_NOT_COMPARE = -1;
	public final static int RESULT_FAILURE = 0;
	public final static int RESULT_SUCCESS = 1;

	/**
	 * 
	 * @param result1
	 * @param result2
	 * @return
	 * @throws IllegalAccessException 
	 * @throws IllegalArgumentException 
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static <T extends AbstractResult> ResultDesc compare(AbstractResult result1, AbstractResult result2, Hstore hstore, ResultOptions options) throws IllegalArgumentException, IllegalAccessException {
		HstoreParser<T> parser = (HstoreParser<T>) hstore.getParser(result1.getClass());
		
		if (!result1.getClass().equals(result2.getClass())) {
			System.out.println("could not compare: " + result1.getClass() + " <-> " + result2.getClass());
			return null;
		}

		if ((result1.getOperator()!=null || result1.getEvaluate()!=null) && result2.getOperator()==null) {
			for (Field f : parser.getAnnotatedFields()) {
				f.setAccessible(true);
				if (f.get(result1) != null && !Collection.class.isAssignableFrom(f.getType())) {
					Object r = TestScriptInterpreter.interprete(String.valueOf(f.get(result1)), hstore, result2, false, options);
					f.set(result2, String.valueOf(r));
				}
			}
		 	//return (result2.compareWithExpectedResult(result1) == RESULT_SUCCESS ? result1.getOnSuccess() : result1.getOnFailure());
			ResultDesc resultDesc = (runCompare(result2, result1) == RESULT_SUCCESS ? 
					new ResultDesc(ResultDesc.STATUS_CODE_SUCCESS, result1.getOnSuccess(), result2, hstore, options) : 
					new ResultDesc(ResultDesc.STATUS_CODE_FAILURE, result1.getOnFailure(), result2, hstore, options));
			
			//resultDesc.setValue(String.valueOf(TestScriptInterpreter.interprete(resultDesc.getValue(), parser, result2, true)));
			return resultDesc;

		}
		else if ((result2.getOperator()!=null || result2.getEvaluate()!=null) && result1.getOperator()==null) {
			for (Field f : parser.getAnnotatedFields()) {
				f.setAccessible(true);
				if (f.get(result2) != null && !Collection.class.isAssignableFrom(f.getType())) {
					Object r = TestScriptInterpreter.interprete(String.valueOf(f.get(result2)), hstore, result1, false, options);
					f.set(result2, String.valueOf(r));	
				}
			}
			//return (result1.compareWithExpectedResult(result2) == RESULT_SUCCESS ? result2.getOnSuccess() : result2.getOnFailure());
			ResultDesc resultDesc = (runCompare(result1, result2) == RESULT_SUCCESS ? 
					new ResultDesc(ResultDesc.STATUS_CODE_SUCCESS, result2.getOnSuccess(), result1, hstore, options) : 
					new ResultDesc(ResultDesc.STATUS_CODE_FAILURE, result2.getOnFailure(), result1, hstore, options));
			
			//resultDesc.setValue(String.valueOf(TestScriptInterpreter.interprete(resultDesc.getValue(), parser, result1, true)));
			return resultDesc;
		}

		System.out.println("Could not compare: Both comparators either set or not set or evaluate missing");
		return null;
	}
	
	/**
	 * 
	 * @param result
	 * @param expectedResult
	 * @return
	 */
	private static int runCompare(AbstractResult<?> result, AbstractResult<?> expectedResult) {
		//makes the comparison shorter (less code) and (maybe ;)) faster. needed for the logical xor operation:
		if (expectedResult.getOperator()!=null) {
			boolean controlFlag = (expectedResult.getOperator().equals(AbstractResult.COMPARATOR_EQUALS) || 
					expectedResult.getOperator().equals(AbstractResult.COMPARATOR_GREATER_THEN) ||
					expectedResult.getOperator().equals(AbstractResult.COMPARATOR_GREATER_OR_EQUALS)) ? true : false;
		
			return runCompare(expectedResult.getOperator(), controlFlag, result, expectedResult);
		}
		else if (expectedResult.getEvaluate()!=null) {			
			String eval = expectedResult.getEvaluate();
			return eval.equals("true") ? ResultComparer.RESULT_SUCCESS : ResultComparer.RESULT_FAILURE;
		}
		
		return ResultComparer.RESULT_COULD_NOT_COMPARE;
	}
	
	/**
	 * 
	 * @param operator
	 * @param controlFlag
	 * @param result
	 * @param expectedResult
	 * @return
	 */
	private static int runCompare(String operator, boolean controlFlag, Object result, Object expectedResult) {
		try {
			//try to compare each field if the expected result's field is not null
			for (Field f: result.getClass().getDeclaredFields()) {
				f.setAccessible(true);
				if (f.get(expectedResult) != null) {
					int compareResult = compareFields(f, operator, controlFlag, result, expectedResult);
					if (compareResult != ResultComparer.RESULT_SUCCESS) {
						return compareResult;
					}					
				}
			}
		}
		catch (Throwable t) {
			t.printStackTrace();
			return ResultComparer.RESULT_COULD_NOT_COMPARE;
		}
		
		return ResultComparer.RESULT_SUCCESS;
	}
	
	/**
	 * 
	 * @param f
	 * @param controlFlag
	 * @param result
	 * @param expectedResult
	 * @return
	 */
	private static int compareFields(Field f, String operator, boolean controlFlag, Object result, Object expectedResult) {
		String leftPart, rightPart;
		
		//System.out.println("comparing: " + f.getName() + " operator: " + operator  + " result: " + result + " expectedResult: " + expectedResult);
		
		try {
			//make sure also private fields can be compared:
			f.setAccessible(true);
			
			//check if field is a collection... if true, then it will get a bit complicated here:
			if (Collection.class.isAssignableFrom(f.getType())) {
				int compareResult = ResultComparer.RESULT_SUCCESS;
				//for each item on the right side (= expected test result) there must be (at least) one item on the left side (= test result),
				//that fulfills the requirements of the equation to return TRUE
				for (Object expectedItem : ((Collection<?>)f.get(expectedResult))) {
					Collection<?> collection = (Collection<?>) f.get(result);
					
					for (Object item : collection) {
						//compare each field on the right side with the current field from the left side
						if ((compareResult = runCompare(operator, controlFlag, item, expectedItem)) == ResultComparer.RESULT_SUCCESS) {
							//if there was a success then return to previous for and continue with the next item
							break;
						}
					}
				}
				return compareResult;
			}
			
			//if the field is not a collection it can (hopefully) be compared
			else if (f.get(expectedResult) != null) {
				switch (operator) {
				case AbstractResult.COMPARATOR_EQUALS:
				case AbstractResult.COMPARATOR_NOT_EQUALS:
					//System.out.println("-- EQ, NE --");
					leftPart = (String) f.get(result);
					rightPart = (String) f.get(expectedResult);
					//use logical xor to determine the comparison result
					if (rightPart.trim().equals(leftPart.trim()) ^ controlFlag) {
						return ResultComparer.RESULT_FAILURE;	
					}
					break;
					
				case AbstractResult.COMPARATOR_GREATER_THEN:
				case AbstractResult.COMPARATOR_LOWER_THEN:
					//System.out.println("-- GT, LT --");
					leftPart = (String) f.get(result);
					rightPart = (String) f.get(expectedResult);
					if ((Long.compare(Long.valueOf(leftPart), Long.valueOf(rightPart)) > 0) ^ controlFlag) {
						return ResultComparer.RESULT_FAILURE;	
					}
					break;
					
				case AbstractResult.COMPARATOR_GREATER_OR_EQUALS:
				case AbstractResult.COMPARATOR_LOWER_OR_EQUALS:
					//System.out.println("-- GE, LE --");
					leftPart = (String) f.get(result);
					rightPart = (String) f.get(expectedResult);
					if ((Long.compare(Long.valueOf(leftPart), Long.valueOf(rightPart)) >= 0) ^ controlFlag) {
						return ResultComparer.RESULT_FAILURE;	
					}
					break;
				}					
			}	
		}
		catch (Throwable t) {
			System.out.println(t.getClass().getCanonicalName() + ": " + t.getLocalizedMessage());
			return ResultComparer.RESULT_COULD_NOT_COMPARE;
		}
		
		return ResultComparer.RESULT_SUCCESS;

	}
}
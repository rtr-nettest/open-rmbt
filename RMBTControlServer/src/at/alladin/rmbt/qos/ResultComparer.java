/*******************************************************************************
 * Copyright 2013-2016 alladin-IT GmbH
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

import at.alladin.rmbt.qos.annotations.NonComparableField;
import at.alladin.rmbt.qos.testscript.TestScriptInterpreter;
import at.alladin.rmbt.qos.testscript.TestScriptInterpreter.EvalResult;
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
	public final static int RESULT_INFO = 2;

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
				if (!f.isAnnotationPresent(NonComparableField.class) && f.get(result1) != null && !Collection.class.isAssignableFrom(f.getType())) {
					Object r = TestScriptInterpreter.interprete(String.valueOf(f.get(result1)), hstore, result2, false, options);
					f.set(result2, (r instanceof EvalResult) ? r : String.valueOf(r));
				}
			}

			return getResultDescription(result1, result2, hstore, options);			
		}
		else if ((result2.getOperator()!=null || result2.getEvaluate()!=null) && result1.getOperator()==null) {
			for (Field f : parser.getAnnotatedFields()) {
				f.setAccessible(true);
				if (!f.isAnnotationPresent(NonComparableField.class) && f.get(result2) != null && !Collection.class.isAssignableFrom(f.getType())) {
					Object r = TestScriptInterpreter.interprete(String.valueOf(f.get(result2)), hstore, result1, false, options);
					f.set(result2, (r instanceof EvalResult) ? r : String.valueOf(r));	
				}
			}

			return getResultDescription(result2, result1, hstore, options);
		}

		System.out.println("Could not compare: Both comparators either set or not set or evaluate missing");
		return null;
	}
	
	/**
	 * 
	 * @param result1
	 * @param result2
	 * @param hstore
	 * @param options
	 * @return
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private static ResultDesc getResultDescription(AbstractResult result1, AbstractResult result2, Hstore hstore, ResultOptions options) {
		final int result = runCompare(result2, result1);
		if ("true".equals(result1.getSuccessCondition()) || "1".equals(result1.getSuccessCondition())) {
			if (result == RESULT_SUCCESS) {
				return new ResultDesc(ResultDesc.STATUS_CODE_SUCCESS, result1.getOnSuccess(), result2, hstore, options);
			}
			else if (result == RESULT_FAILURE || result == RESULT_COULD_NOT_COMPARE) {
				return new ResultDesc(ResultDesc.STATUS_CODE_FAILURE, result1.getOnFailure(), result2, hstore, options);
			}
		}
		else {
			if (result == RESULT_FAILURE) {
				return new ResultDesc(ResultDesc.STATUS_CODE_SUCCESS, result1.getOnSuccess(), result2, hstore, options);
			}
			else if (result == RESULT_SUCCESS || result == RESULT_COULD_NOT_COMPARE) {
				return new ResultDesc(ResultDesc.STATUS_CODE_FAILURE, result1.getOnFailure(), result2, hstore, options);
			}
		}
		
		try {
			return new ResultDesc(ResultDesc.StatusCode.values()[result], result1.getOnSuccess(), result2, hstore, options);
		} catch (Exception e) {
			//fallback: unknown status code:
			return new ResultDesc(ResultDesc.StatusCode.INFO, result1.getOnSuccess(), result2, hstore, options);
		}
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
			//System.out.println(expectedResult.getClass() + " evaluate: " + expectedResult.getEvaluate());
			final Object eval = expectedResult.getEvaluate();
			if (eval instanceof String) {
				return eval.equals("true") ? ResultComparer.RESULT_SUCCESS : ResultComparer.RESULT_FAILURE;
			}
			else if (eval instanceof EvalResult) {
				switch (((EvalResult) eval).getType()) {
				case FAILURE:
					expectedResult.setOnFailure(((EvalResult) eval).getResultKey());
					return RESULT_FAILURE;
				case SUCCESS:
					expectedResult.setOnSuccess(((EvalResult) eval).getResultKey());
					return RESULT_SUCCESS;
				default:
					expectedResult.setOnSuccess(((EvalResult) eval).getResultKey());
					return ((EvalResult) eval).getType().ordinal();
				}
			}
			else {
				return ResultComparer.RESULT_COULD_NOT_COMPARE;
			}
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
		//System.out.println("comparing: " + f.getName() + " operator: " + operator  + " result: " + result + " expectedResult: " + expectedResult);
		
		int resultOfCompare = ResultComparer.RESULT_SUCCESS;
		
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
				final Object leftPart = String.valueOf(f.get(result));
				final Object rightPart = String.valueOf(f.get(expectedResult));
				
				//System.out.print(leftPart + " [" + operator + "] " + rightPart + " = ");
				
				switch (operator) {
				case AbstractResult.COMPARATOR_EQUALS:
				case AbstractResult.COMPARATOR_NOT_EQUALS:
					//System.out.println("-- EQ, NE --");
					//use logical xor to determine the comparison result
					if (String.valueOf(rightPart).trim().equals(String.valueOf(leftPart).trim()) ^ controlFlag) {
						resultOfCompare = ResultComparer.RESULT_FAILURE;	
					}
					break;
					
				case AbstractResult.COMPARATOR_GREATER_THEN:
				case AbstractResult.COMPARATOR_LOWER_THEN:
					
					//System.out.println("-- GT, LT --");
					if ((compare(leftPart, rightPart) > 0) ^ controlFlag) {
						resultOfCompare = ResultComparer.RESULT_FAILURE;	
					}
					break;
					
				case AbstractResult.COMPARATOR_GREATER_OR_EQUALS:
				case AbstractResult.COMPARATOR_LOWER_OR_EQUALS:
					//System.out.println("-- GE, LE --");
					if ((compare(leftPart, rightPart) >= 0) ^ controlFlag) {
						resultOfCompare = ResultComparer.RESULT_FAILURE;	
					}
					break;
				}					
			}	
		}
		catch (Throwable t) {
			t.printStackTrace();
			System.out.println("Field: " + f.toString() + ", " + t.getClass().getCanonicalName() + ": " + t.getLocalizedMessage());
			return ResultComparer.RESULT_COULD_NOT_COMPARE;
		}
		
		//System.out.println(resultOfCompare);
		return resultOfCompare;

	}
	
	/**
	 * Try to compare to objects. <br>
	 * First: check if objects are both not null. If not true the not null object is "greater".<br>
	 * If true: try a long comparison; on exception: try a double comparison and on another exception: finally try a string comparison
	 * @param o1
	 * @param o2
	 * @return
	 */
	public final static int compare(Object o1, Object o2) {
		if (o1 != null && o2 != null) {
			final String s1 = String.valueOf(o1);
			final String s2 = String.valueOf(o2);
			
			try {
				final Long l1 = Long.valueOf(s1);
				final Long l2 = Long.valueOf(s2);
				
				return Long.compare(l1, l2);
			}
			catch (NumberFormatException e1) {
				try {
					final Double d1 = Double.valueOf(s1);
					final Double d2 = Double.valueOf(s2);
				
					return Double.compare(d1, d2);
				}
				catch (NumberFormatException e2) {
					return s1.compareTo(s2);
				}
			}
		}
		else if (o2 == null) {
			return 1;
		}
		else if (o1 == null) {
			return -1;
		}
		
		return 0;
	}
}
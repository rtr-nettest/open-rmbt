/*******************************************************************************
 * Copyright 2013-2014 alladin-IT GmbH
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
package at.alladin.rmbt.shared.hstoreparser.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Hstore annotation used for casting objects
 * @author lb
 *
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface HstoreCast {
	/**
	 * <i>required</i><br>
	 * defines the class that the object will be cast to
	 * @return
	 */
	Class<?> clazz();
	
	/**
	 * <i>optional</i><br>
	 * <i>default</i> <b>true</b><br>
	 * if set to <b>true</b> a simple cast will be done like: <code>String s = (String) object;</code><br>
	 * if set to <b>false</b> there won't be a cast, but instead a new instance of the object will be created (see also: {@link HstoreCast.constructorParamClazz})
	 * @return
	 */
	boolean simpleCast() default true;
	
	/**
	 * <i>optional</i><br>
	 * <i>default</i> <b>{@link String}</b><br>
	 * if {@link HstoreCast.simpleCast} is set to <b>false</b> the Hstore parser will try to find a constructor with the given parameter and instantiate a new object passing the hstore value as a parameter.
	 * @return
	 */
	Class<?> constructorParamClazz() default String.class;	
}

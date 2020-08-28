/***************************************************************************
 * Copyright 2019 alladin-IT GmbH
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
 ***************************************************************************/

package at.rtr.rmbt.qos.testserver.tcp.competences;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.List;

/**
 * 
 * @author lb@alladin.at
 *
 */
public interface Competence {

	/**
	 * Check if the incoming data (first line) applies to this specific competence.
	 * @param firstLine
	 * @return
	 */
	boolean appliesTo(final String firstLine);
	
	/**
	 * Read full request   
	 * @param firstLine
	 * @return
	 */
	String readFullRequest(final String firstLine, final BufferedReader br) throws IOException;
	
	/**
	 * Process request and return action(s).
	 * @param tcpClientHandler
	 * @return
	 */
	List<Action> processRequest(final String data);
}

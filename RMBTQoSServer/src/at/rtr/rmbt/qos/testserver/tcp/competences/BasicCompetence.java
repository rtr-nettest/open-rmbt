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
import java.util.ArrayList;
import java.util.List;

import at.rtr.rmbt.qos.testserver.ClientHandler;

/**
 * 
 * @author lb@alladin.at
 *
 */
public class BasicCompetence implements Competence {

	@Override
	public boolean appliesTo(final String data) {
		return true;
	}

	@Override
	public List<Action> processRequest(final String data) {
		final List<Action> result = new ArrayList<>();
		result.add(new ResponseAction(ClientHandler.getBytesWithNewline(new String(data))));
		return result;
	}

	@Override
	public String readFullRequest(String firstLine, BufferedReader br) throws IOException {
		return firstLine;
	}
}

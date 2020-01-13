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

import java.io.FilterOutputStream;

import at.rtr.rmbt.qos.testserver.tcp.TcpMultiClientServer;
import at.rtr.rmbt.qos.testserver.util.TestServerConsole;
import at.rtr.rmbt.qos.testserver.ServerPreferences.TestServerServiceEnum;
import at.rtr.rmbt.qos.testserver.tcp.TcpClientHandler;

public class ResponseAction implements Action {

	private final byte[] data;
	
	public ResponseAction(final byte[] data) {
		this.data = data;
	}
	
	public byte[] getData() {
		return data;
	}
	
	@Override
	public boolean execute(final TcpClientHandler tcpClientHandler, final byte[] requestData, FilterOutputStream os) {
		if (data != null) {
			try {
				os.write(data);
				os.flush();
				return true;
			}
			catch (final Exception e) {
				TestServerConsole.error("ResponseAction error!", e,
						TcpMultiClientServer.VERBOSE_LEVEL_REQUEST_RESPONSE, TestServerServiceEnum.TCP_SERVICE);
			}
		}
		return false;
	}
}

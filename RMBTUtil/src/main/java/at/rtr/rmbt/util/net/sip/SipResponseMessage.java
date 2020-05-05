/*******************************************************************************
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
 ******************************************************************************/

package at.rtr.rmbt.util.net.sip;

/**
 * 
 * @author lb@alladin.at
 *
 */
public class SipResponseMessage extends SipMessage {

	/**
	 * 
	 * @author lb@alladin.at
	 *
	 */
	public enum SipResponseType {
		TRYING(100),
		RINGING(180),
		OK(200);
		
		final int code;
		
		private SipResponseType(final int code) {
			this.code = code;
		}

		public int getCode() {
			return code;
		}
		
		public static SipResponseType getByCode(final int code) {
			switch(code) {
			case 100:
				return TRYING;
			case 180:
				return RINGING;
			case 200:
				return OK;
			}
			
			return null;
		}
	}
	
	private final SipResponseType type;
	
	public SipResponseMessage(final SipResponseType type) {
		this.type = type;
	}
	
	public SipResponseMessage(final SipResponseType type, final SipRequestMessage request) {
		this.type = type;
		this.setTo(request.getFrom());
		this.setFrom(request.getTo());
		this.setVia(request.getVia());
	}

	public SipResponseType getType() {
		return type;
	}

	@Override
	String getFirstLine() {
		return SIP_PROTOCOL_STRING + " " + type.getCode() + " " + type.toString();
	}

	@Override
	public String toString() {
		return "SipResponseMessage{" +
				"type=" + type +
				"} " + super.toString();
	}
}

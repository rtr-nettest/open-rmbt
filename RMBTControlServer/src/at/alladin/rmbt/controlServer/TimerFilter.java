/*******************************************************************************
 * Copyright 2015 alladin-IT GmbH
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
 *******************************************************************************/
package at.alladin.rmbt.controlServer;

import org.restlet.Request;
import org.restlet.Response;
import org.restlet.routing.Filter;

public class TimerFilter extends Filter {

	ThreadLocal<Long> timestamp = new ThreadLocal<>();
	
	@Override
	protected int beforeHandle(Request request, Response response) {
		timestamp.set(System.currentTimeMillis());
		return super.beforeHandle(request, response);
	}
	
	@Override
	protected void afterHandle(Request request, Response response) {
		System.out.println(request.getResourceRef().getIdentifier() + " Time needed: " + (System.currentTimeMillis() - timestamp.get()) + "ms");
		super.afterHandle(request, response);
	}
}

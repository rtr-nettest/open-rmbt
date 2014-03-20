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
package at.alladin.rmbt.shared;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.Charset;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;
import java.util.ResourceBundle.Control;

public class ResourceControl extends Control
{
	final List<String> formats = Collections.singletonList("properties");
	
	@Override
	public List<String> getFormats(String baseName)
	{
		if (baseName == null)
			throw new NullPointerException();
		return formats;
	}
	
	@Override
	public ResourceBundle newBundle(String baseName, Locale locale,
			String format, ClassLoader loader, boolean reload)
			throws IllegalAccessException, InstantiationException, IOException
	{
		if (baseName == null || locale == null
                || format == null || loader == null)
              throw new NullPointerException();
          ResourceBundle bundle = null;
          if (format.equals("properties")) {
              String bundleName = toBundleName(baseName, locale);
              String resourceName = toResourceName(bundleName, format);
              InputStream stream = null;
              if (reload) {
                  URL url = loader.getResource(resourceName);
                  if (url != null) {
                      URLConnection connection = url.openConnection();
                      if (connection != null) {
                          // Disable caches to get fresh data for
                          // reloading.
                          connection.setUseCaches(false);
                          stream = connection.getInputStream();
                      }
                  }
              } else {
                  stream = loader.getResourceAsStream(resourceName);
              }
              if (stream != null) {
                  try (BufferedInputStream bis = new BufferedInputStream(stream);
                      InputStreamReader isr = new InputStreamReader(bis, Charset.forName("UTF-8"));)
                  {
                      bundle = new PropertyResourceBundle(isr);
                  }    
              }
          }
          return bundle;
	}
	
	@Override
	public long getTimeToLive(String baseName, Locale locale)
	{
		// TODO remove
		// for debug
//		return TTL_DONT_CACHE;
		
		return TTL_NO_EXPIRATION_CONTROL;
	}
}

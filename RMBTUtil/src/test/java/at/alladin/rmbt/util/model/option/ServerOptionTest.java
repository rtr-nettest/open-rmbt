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
package at.alladin.rmbt.util.model.option;

import java.io.File;
import java.io.FileReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import junit.framework.TestCase;

import org.junit.Test;

public class ServerOptionTest extends TestCase{

	@Test
	public void testOptionFunctionParametersAndTitles() throws Exception {
		URL url = Thread.currentThread().getContextClassLoader().getResource("at/alladin/rmbt/util/model/option/test1.json");
		File file = new File(url.getPath());
		ServerOption option = ServerOption.getGson().fromJson(new FileReader(file), ServerOption.class);
		
		assertEquals("option list size", 3, option.getOptionList().size());
		assertEquals("parameter list size", 1, option.getParameterMap().size());
		assertEquals("option list item index 0 title", "Download", option.getOptionList().get(0).getTitle());
		assertEquals("option list item index 1 title", "Upload", option.getOptionList().get(1).getTitle());
		assertEquals("option list item index 2 title", "Ping", option.getOptionList().get(2).getTitle());
		assertNull("unknown parameter value", option.getParameterMap().get("unknown"));
		assertEquals("parameter 1 value", false, option.getParameterMap().get("map_type_is_mobile"));
	}

	@Test
	public void testOptionFunctionParentCorrectness() throws Exception {
		URL url = Thread.currentThread().getContextClassLoader().getResource("at/alladin/rmbt/util/model/option/test1.json");
		File file = new File(url.getPath());
		ServerOption option = ServerOption.getGson().fromJson(new FileReader(file), ServerOption.class);
		
		assertNull("main option parent", option.getParent());
		assertEquals("option index 0 parent title", option.getTitle(), option.getOptionList().get(0).getParent().getTitle());
		assertEquals("option index 1 parent title", option.getTitle(), option.getOptionList().get(1).getParent().getTitle());
		assertEquals("option index 2 parent title", option.getTitle(), option.getOptionList().get(2).getParent().getTitle());
	}

	@Test
	public void testOptionFunctionLoadingFunction() throws Exception {
		URL url = Thread.currentThread().getContextClassLoader().getResource("at/alladin/rmbt/util/model/option/test2.json");
		File file = new File(url.getPath());
		ServerOption option = ServerOption.getGson().fromJson(new FileReader(file), ServerOption.class);
		
		assertEquals("main option function size", 2, option.getFunctionList().size());
		assertEquals("main option function 0 name", "drop_param", option.getFunctionList().get(0).getName());
		assertEquals("main option function 0 param 'key'", "title", option.getFunctionList().get(0).getParameterMap().get("key"));
		assertNull("main option function 1 params", option.getFunctionList().get(1).getParameterMap());
	}
	
	@Test
	public void testOptionSelectedParametersWithInheritance() throws Exception {
		URL url = Thread.currentThread().getContextClassLoader().getResource("at/alladin/rmbt/util/model/option/test2.json");
		File file = new File(url.getPath());
		ServerOptionContainer options = new ServerOptionContainer(
				new ArrayList<>(Arrays.asList(ServerOption.getGson().fromJson(new FileReader(file), ServerOption.class))));
		
		List<ServerOption> list = options.select(options.getRootOptions().get(0));
		assertNotNull("selected option sublist not null", list);
		
		List<ServerOption> subList = options.select(list.get(0));
		Map<String, Object> paramMap = options.getSelectedParams();		
		assertNull("selected option 0 sublist is null", subList);
		assertEquals("selected option 0 param 'option'", "a1", paramMap.get("option"));
		assertNull("selected option 0 dropped param 'title'", paramMap.get("title"));
		assertEquals("selected option 0 params size", 2, paramMap.size());
		assertEquals("selected option 0 overriden param 'parent_param'", true, paramMap.get("parent_param"));

		subList = options.select(list.get(1));
		paramMap = options.getSelectedParams();		
		assertNull("selected option 1 sublist is null", subList);
		assertEquals("selected option 1 param 'option'", "a2", paramMap.get("option"));
		assertNull("selected option 1 dropped param 'title'", paramMap.get("title"));
		assertEquals("selected option 1 params size", 2, paramMap.size());
		assertEquals("selected option 1 inherited param 'parent_param'", false, paramMap.get("parent_param"));

		subList = options.select(list.get(2));
		paramMap = options.getSelectedParams();		
		assertNull("selected option 2 sublist is null", subList);
		assertEquals("selected option 2 param 'option'", "a3", paramMap.get("option"));
		assertEquals("selected option 2 params size", 4, paramMap.size());
		assertEquals("selected option 2 param 'titles'", "a3", paramMap.get("titles"));
		assertEquals("selected option 2 inherited param 'parent_param'", false, paramMap.get("parent_param"));
		
		subList = options.select(list.get(3));
		paramMap = options.getSelectedParams();		
		assertNull("selected option 3 sublist is null", subList);
		assertEquals("selected option 3 params size", 1, paramMap.size());
		assertEquals("selected option 3 inherited param 'parent_param'", false, paramMap.get("parent_param"));
	}
	
	@Test
	public void testOptionParameterOverrides() throws Exception {
		URL url = Thread.currentThread().getContextClassLoader().getResource("at/alladin/rmbt/util/model/option/test3.json");
		File file = new File(url.getPath());
		ServerOptionContainer options = new ServerOptionContainer(
				new ArrayList<>(Arrays.asList(ServerOption.getGson().fromJson(new FileReader(file), ServerOption.class))));

		final List<ServerOption> list = options.getRootOptions();
		List<ServerOption> sublist = options.select(list.get(0));
		assertEquals("option 0 select param size", 0, options.getSelectedParams().size());
		
		sublist = options.select(sublist.get(0));
		assertEquals("option 0->0 select param size", 2, options.getSelectedParams().size());
		assertEquals("option 0->0 param 'option'", "a1", options.getSelectedParams().get("option"));
		
		sublist = options.select(sublist.get(0));
		assertEquals("option 0->0->0 select param size", 2, options.getSelectedParams().size());
		assertEquals("option 0->0->0 override param 'option'", "a2", options.getSelectedParams().get("option"));
		
		sublist = options.select(sublist.get(0));
		assertEquals("option 0->0->0->0 select param size", 2, options.getSelectedParams().size());
		assertEquals("option 0->0->0->0 override param 'option'", "a3", options.getSelectedParams().get("option"));

		sublist = options.select(sublist.get(0));
		assertEquals("option 0->0->0->0->0 select param size", 2, options.getSelectedParams().size());
		assertEquals("option 0->0->0->0->0 override param 'option'", "a3", options.getSelectedParams().get("option"));
	}
	
	@Test
	public void testOptionDependsOn() throws Exception {
		URL url = Thread.currentThread().getContextClassLoader().getResource("at/alladin/rmbt/util/model/option/test4.json");
		File file = new File(url.getPath());
		ServerOptionContainer options = new ServerOptionContainer(Arrays.asList(ServerOption.getGson().fromJson(new FileReader(file), ServerOption[].class)));

		final List<ServerOption> list = options.getRootOptions();
		
		assertEquals("option 0 is enabled ", true, list.get(0).isEnabled());
		assertEquals("option 0->0 is enabled ", true, list.get(0).getOptionList().get(0).isEnabled());
		assertEquals("option 0->1 is enabled ", true, list.get(0).getOptionList().get(1).isEnabled());
		assertEquals("option 1->0 is enabled ", true, list.get(1).getOptionList().get(0).isEnabled());
		assertEquals("option 1->1 is not enabled ", false, list.get(1).getOptionList().get(1).isEnabled());

		
		List<ServerOption> subList = options.select(list.get(0));
		//select option 0, suboption 0 (this should set parameter "test" to "1" and enable suboption 1 of option 1) 
		options.select(subList.get(0));
		assertEquals("option 0->0 is enabled ", true, list.get(0).getOptionList().get(0).isEnabled());
		assertEquals("option 0->1 is enabled ", true, list.get(0).getOptionList().get(1).isEnabled());
		assertEquals("option 1->0 is enabled ", true, list.get(1).getOptionList().get(0).isEnabled());
		assertEquals("option 1->1 is enabled ", true, list.get(1).getOptionList().get(1).isEnabled());

		//select option 0, suboption 1 (this should remove the parameter "test" and again disable suboption 1 of option 1)
		options.select(subList.get(1));
		assertEquals("option 0->0 is enabled ", true, list.get(0).getOptionList().get(0).isEnabled());
		assertEquals("option 0->1 is enabled ", true, list.get(0).getOptionList().get(1).isEnabled());
		assertEquals("option 1->0 is enabled ", true, list.get(1).getOptionList().get(0).isEnabled());
		assertEquals("option 1->1 is not enabled ", false, list.get(1).getOptionList().get(1).isEnabled());
	}
	
	@Test
	public void testSetDefaultValues() throws Exception {
		URL url = Thread.currentThread().getContextClassLoader().getResource("at/alladin/rmbt/util/model/option/test5.json");
		File file = new File(url.getPath());
		ServerOptionContainer options = new ServerOptionContainer(Arrays.asList(ServerOption.getGson().fromJson(new FileReader(file), ServerOption[].class)));

		options.setDefault();
		assertEquals("default parameter size ", 3, options.getSelectedParams().size());
		assertEquals("default paramater 'test' value ", 1, (int)(double)options.getSelectedParams().get("test"));
		assertEquals("default paramater 'option' value ", "a1", options.getSelectedParams().get("option"));
		assertEquals("default paramater 'suboption' value ", "option1", options.getSelectedParams().get("suboption"));
	}
}

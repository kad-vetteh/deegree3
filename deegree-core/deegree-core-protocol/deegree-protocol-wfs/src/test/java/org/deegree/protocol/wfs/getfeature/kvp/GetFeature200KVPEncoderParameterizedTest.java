//$HeadURL$
/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2015 by:
 - Department of Geography, University of Bonn -
 and
 - lat/lon GmbH -

 This library is free software; you can redistribute it and/or modify it under
 the terms of the GNU Lesser General Public License as published by the Free
 Software Foundation; either version 2.1 of the License, or (at your option)
 any later version.
 This library is distributed in the hope that it will be useful, but WITHOUT
 ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 details.
 You should have received a copy of the GNU Lesser General Public License
 along with this library; if not, write to the Free Software Foundation, Inc.,
 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA

 Contact information:

 lat/lon GmbH
 Aennchenstr. 19, 53177 Bonn
 Germany
 http://lat-lon.de/

 Department of Geography, University of Bonn
 Prof. Dr. Klaus Greve
 Postfach 1147, 53001 Bonn
 Germany
 http://www.geographie.uni-bonn.de/deegree/

 e-mail: info@deegree.org
 ----------------------------------------------------------------------------*/
package org.deegree.protocol.wfs.getfeature.kvp;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.deegree.commons.utils.kvp.KVPUtils;
import org.deegree.protocol.wfs.getfeature.GetFeature;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

/**
 * @author <a href="mailto:goltz@lat-lon.de">Lyn Goltz</a>
 */
@RunWith(Parameterized.class)
public class GetFeature200KVPEncoderParameterizedTest {

	private Map<String, String> kvpMapUnderTest;

	private String testName;

	public GetFeature200KVPEncoderParameterizedTest(String testName, Map<String, String> kvpMapUnderTest) {
		this.testName = testName;
		this.kvpMapUnderTest = kvpMapUnderTest;
	}

	@Parameters
	public static List<Object[]> data() throws IOException {
		List<Object[]> kvpMaps = new ArrayList<Object[]>();
		kvpMaps.add(new Object[] { "example1.kvp", asKvp("wfs200/example1.kvp") });
		kvpMaps.add(new Object[] { "example2.kvp", asKvp("wfs200/example2.kvp") });
		kvpMaps.add(new Object[] { "example3.kvp", asKvp("wfs200/example2.kvp") });
		kvpMaps.add(new Object[] { "example4.kvp", asKvp("wfs200/example4.kvp") });
		kvpMaps.add(new Object[] { "example5.kvp", asKvp("wfs200/example5.kvp") });
		kvpMaps.add(new Object[] { "example7.kvp", asKvp("wfs200/example7.kvp") });
		kvpMaps.add(new Object[] { "example11.kvp", asKvp("wfs200/example11.kvp") });
		kvpMaps.add(new Object[] { "example12.kvp", asKvp("wfs200/example12.kvp") });
		kvpMaps.add(new Object[] { "example13.kvp", asKvp("wfs200/example13.kvp") });
		kvpMaps.add(new Object[] { "example16.kvp", asKvp("wfs200/example16.kvp") });
		kvpMaps.add(new Object[] { "example17.kvp", asKvp("wfs200/example17.kvp") });
		kvpMaps.add(new Object[] { "example18.kvp", asKvp("wfs200/example18.kvp") });
		kvpMaps.add(new Object[] { "example19.kvp", asKvp("wfs200/example19.kvp") });
		kvpMaps.add(new Object[] { "example20.kvp", asKvp("wfs200/example20.kvp") });
		kvpMaps.add(new Object[] { "example21.kvp", asKvp("wfs200/example21.kvp") });
		kvpMaps.add(new Object[] { "example_bbox_explicit_crs.kvp", asKvp("wfs200/example_bbox_explicit_crs.kvp") });
		return kvpMaps;
	}

	@Test
	public void testExport() throws Exception {
		GetFeature getFeature = GetFeatureKVPAdapter.parse(kvpMapUnderTest, null);

		Map<String, String> exportedKvp = GetFeature200KVPEncoder.export(getFeature);

		assertThat("Failed test resource: " + testName, exportedKvp, is(kvpMapUnderTest));
	}

	private static Map<String, String> asKvp(String name) throws IOException {
		URL exampleURL = GetFeature200KVPEncoderParameterizedTest.class.getResource(name);
		return KVPUtils.readFileIntoMap(exampleURL);
	}

}
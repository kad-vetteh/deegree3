//$HeadURL: svn+ssh://svn.wald.intevation.org/deegree/deegree3/trunk/deegree-core/deegree-core-commons/src/main/java/org/deegree/commons/tom/ElementNode.java $
/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2011 by:
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
package org.deegree.commons.tom;

import java.util.List;

import org.deegree.commons.tom.gml.GMLObject;
import org.deegree.commons.tom.gml.GMLReferenceResolver;

/**
 * Resolves objects by using a list of resolvers. Resolves an object to the first resolver
 * that can resolve it.
 *
 * @author <a href="mailto:schmitz@occamlabs.de">Andreas Schmitz</a>
 * @author last edited by: $Author: mschneider $
 * @version $Revision: 30435 $, $Date: 2011-04-13 17:26:03 +0200 (Wed, 13 Apr 2011) $
 */
public class CombinedReferenceResolver implements GMLReferenceResolver {

	private List<GMLReferenceResolver> resolvers;

	public CombinedReferenceResolver(List<GMLReferenceResolver> resolvers) {
		this.resolvers = resolvers;
	}

	@Override
	public GMLObject getObject(String uri, String baseURL) {
		for (GMLReferenceResolver resolver : resolvers) {
			try {
				GMLObject obj = resolver.getObject(uri, baseURL);
				if (obj != null) {
					return obj;
				}
			}
			catch (Throwable t) {
				// just skip to the next resolver
			}
		}
		return null;
	}

}

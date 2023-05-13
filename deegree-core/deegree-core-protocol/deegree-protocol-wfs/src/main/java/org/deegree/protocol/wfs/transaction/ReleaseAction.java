//$HeadURL$
/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2012 by:
 Department of Geography, University of Bonn
 and
 lat/lon GmbH

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
package org.deegree.protocol.wfs.transaction;

/**
 * Controls how locked features are treated when a transaction request is completed.
 *
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author$
 * @version $Revision$, $Date$
 */
public enum ReleaseAction {

	/**
	 * Indicates that the locks on all feature instances locked using the associated
	 * lockId should be released when the transaction completes, regardless of whether or
	 * not a particular feature instance in the locked set was actually operated upon.
	 */
	ALL,

	/**
	 * Indicates that only the locks on feature instances modified by the transaction
	 * should be released. The other, unmodified, feature instances should remain locked
	 * using the same lockId so that subsequent transactions can operate on those feature
	 * instances. If an expiry period was specified, the expiry counter must be reset to
	 * zero after each transaction unless all feature instances in the locked set have
	 * been operated upon.
	 */
	SOME

}

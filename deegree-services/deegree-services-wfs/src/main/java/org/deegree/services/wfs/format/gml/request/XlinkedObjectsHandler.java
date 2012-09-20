//$HeadURL$
/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2012 by:
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
package org.deegree.services.wfs.format.gml.request;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.xml.stream.XMLStreamException;

import org.deegree.commons.tom.gml.GMLObject;
import org.deegree.commons.tom.gml.GMLReference;
import org.deegree.gml.GmlReferenceResolveOptions;
import org.deegree.gml.feature.GMLForwardReferenceHandler;
import org.deegree.protocol.wfs.getfeature.GetFeature;
import org.deegree.protocol.wfs.getpropertyvalue.GetPropertyValue;
import org.deegree.services.wfs.format.gml.BufferableXMLStreamWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Keeps track of additional (referenced) {@link GMLObject}s that have to be included in {@link GetFeature}/
 * {@link GetPropertyValue} responses.
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
class XlinkedObjectsHandler implements GMLForwardReferenceHandler {

    private static Logger LOG = LoggerFactory.getLogger( XlinkedObjectsHandler.class );

    private LinkedHashMap<String, GMLReference<?>> uriToRef = new LinkedHashMap<String, GMLReference<?>>();

    private Map<GMLReference<?>, GmlReferenceResolveOptions> refToResolveState = new HashMap<GMLReference<?>, GmlReferenceResolveOptions>();

    private final BufferableXMLStreamWriter xmlStream;

    private final boolean localReferencesPossible;

    private final String remoteXlinkTemplate;

    XlinkedObjectsHandler( BufferableXMLStreamWriter xmlStream, boolean localReferencesPossible, String xlinkTemplate ) {
        this.xmlStream = xmlStream;
        this.localReferencesPossible = localReferencesPossible;
        this.remoteXlinkTemplate = xlinkTemplate;
    }

    @Override
    public String requireObject( GMLReference<?> ref, GmlReferenceResolveOptions resolveState ) {
        String uri = ref.getURI();
        LOG.debug( "Exporting forward reference to object {} which must be included in the output.", uri );
        uriToRef.put( uri, ref );
        refToResolveState.put( ref, resolveState );
        return uri;
    }

    @Override
    public String handleReference( GMLReference<?> ref ) {

        String uri = ref.getURI();
        LOG.debug( "Encountered reference to object {}.", uri );
        if ( isNonIdBasedUri( uri ) ) {
            LOG.debug( "Reference to object {} considered non-rewritable.", uri );
            return uri;
        }

        if ( localReferencesPossible ) {
            LOG.debug( "Exporting potential forward reference to object {} which may or may not be exported later.",
                       ref.getId() );
            try {
                xmlStream.activateBuffering();
            } catch ( XMLStreamException e ) {
                throw new RuntimeException( e.getMessage(), e );
            }
            return "{" + ref.getId() + "}";
        }
        LOG.debug( "Exporting reference to object {} as remote reference.", ref.getId() );
        return remoteXlinkTemplate.replace( "{}", ref.getId() );
    }

    private boolean isNonIdBasedUri( String uri ) {
        if ( uri.startsWith( "urn" ) ) {
            return true;
        }
        // hacks for CITE WFS 1.1.0
        if ( uri.startsWith( "http://vancouver1.demo.galdosinc.com" ) ) {
            return true;
        }
        if ( uri.startsWith( "ftp://vancouver1.demo.galdosinc.com" ) ) {
            return true;
        }
        return false;
    }

    Collection<GMLReference<?>> getAdditionalRefs() {
        return uriToRef.values();
    }

    Map<GMLReference<?>, GmlReferenceResolveOptions> getResolveStates() {
        return refToResolveState;
    }

    void clear() {
        uriToRef = new LinkedHashMap<String, GMLReference<?>>();
        refToResolveState = new HashMap<GMLReference<?>, GmlReferenceResolveOptions>();
    }
}

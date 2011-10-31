//$HeadURL: svn+ssh://mschneider@svn.wald.intevation.org/deegree/deegree3/trunk/deegree-datastores/deegree-featurestore/deegree-featurestore-sql/src/main/java/org/deegree/sqldialect/postgis/PostGISDialect.java $
/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2010 by:
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
package org.deegree.sqldialect.postgis;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

import org.deegree.commons.jdbc.ConnectionManager.Type;
import org.deegree.commons.jdbc.SQLIdentifier;
import org.deegree.commons.jdbc.TableName;
import org.deegree.commons.tom.primitive.PrimitiveType;
import org.deegree.commons.tom.sql.DefaultPrimitiveConverter;
import org.deegree.commons.tom.sql.PrimitiveParticleConverter;
import org.deegree.commons.utils.JDBCUtils;
import org.deegree.cs.coordinatesystems.ICRS;
import org.deegree.filter.FilterEvaluationException;
import org.deegree.filter.OperatorFilter;
import org.deegree.filter.sort.SortProperty;
import org.deegree.geometry.Envelope;
import org.deegree.geometry.standard.DefaultEnvelope;
import org.deegree.geometry.standard.primitive.DefaultPoint;
import org.deegree.geometry.utils.GeometryParticleConverter;
import org.deegree.sqldialect.SQLDialect;
import org.deegree.sqldialect.filter.AbstractWhereBuilder;
import org.deegree.sqldialect.filter.PropertyNameMapper;
import org.deegree.sqldialect.filter.UnmappableException;
import org.postgis.PGboxbase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link SQLDialect} for PostgreSQL / PostGIS databases.
 * 
 * @author <a href="mailto:schmitz@lat-lon.de">Andreas Schmitz</a>
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author: mschneider $
 * 
 * @version $Revision: 31186 $, $Date: 2011-07-01 18:01:58 +0200 (Fr, 01. Jul 2011) $
 */
public class PostGISDialect implements SQLDialect {

    private static Logger LOG = LoggerFactory.getLogger( PostGISDialect.class );

    private final boolean useLegacyPredicates;

    public PostGISDialect( boolean useLegacyPredicates ) {
        this.useLegacyPredicates = useLegacyPredicates;
    }

    @Override
    public Type getDBType() {
        return Type.PostgreSQL;
    }

    @Override
    public int getMaxColumnNameLength() {
        return 63;
    }

    @Override
    public int getMaxTableNameLength() {
        return 63;
    }

    public String getDefaultSchema() {
        return "public";
    }

    public String stringPlus() {
        return "||";
    }

    public String stringIndex( String pattern, String string ) {
        return "POSITION(" + pattern + " IN " + string + ")";
    }

    public String cast( String expr, String type ) {
        return expr + "::" + type;
    }

    @Override
    public String geometryMetadata( TableName qTable, String column, boolean isGeographical ) {
        String dbSchema = qTable.getSchema() != null ? qTable.getSchema() : getDefaultSchema();
        String table = qTable.getTable();
        if ( !isGeographical ) {
            return "SELECT coord_dimension,srid,type FROM public.geometry_columns WHERE f_table_schema='"
                   + dbSchema.toLowerCase() + "' AND f_table_name='" + table.toLowerCase()
                   + "' AND f_geometry_column='" + column.toLowerCase() + "'";
        }
        return "SELECT coord_dimension,srid,type FROM public.geography_columns WHERE f_table_schema='"
               + dbSchema.toLowerCase() + "' AND f_table_name='" + table.toLowerCase() + "' AND f_geography_column='"
               + column.toLowerCase() + "'";
    }

    @Override
    public AbstractWhereBuilder getWhereBuilder( PropertyNameMapper mapper, OperatorFilter filter,
                                                 SortProperty[] sortCrit, boolean allowPartialMappings )
                            throws UnmappableException, FilterEvaluationException {
        return new PostGISWhereBuilder( this, mapper, filter, sortCrit, allowPartialMappings, useLegacyPredicates );
    }

    @Override
    public String getUndefinedSrid() {
        return "-1";
    }

    @Override
    public String getBBoxAggregateSnippet( String column ) {
        StringBuilder sql = new StringBuilder();
        if ( useLegacyPredicates ) {
            sql.append( "extent" );
        } else {
            sql.append( "ST_Extent" );
        }
        sql.append( "(" );
        sql.append( column );
        sql.append( ")::BOX2D" );
        return sql.toString();
    }

    @Override
    public Envelope getBBoxAggregateValue( ResultSet rs, int colIdx, ICRS crs )
                            throws SQLException {
        Envelope env = null;
        PGboxbase pgBox = (PGboxbase) rs.getObject( colIdx );
        if ( pgBox != null ) {
            org.deegree.geometry.primitive.Point min = buildPoint( pgBox.getLLB(), crs );
            org.deegree.geometry.primitive.Point max = buildPoint( pgBox.getURT(), crs );
            env = new DefaultEnvelope( null, crs, null, min, max );
        }
        return env;
    }

    private org.deegree.geometry.primitive.Point buildPoint( org.postgis.Point p, ICRS crs ) {
        double[] coords = new double[p.getDimension()];
        coords[0] = p.getX();
        coords[1] = p.getY();
        if ( p.getDimension() > 2 ) {
            coords[2] = p.getZ();
        }
        return new DefaultPoint( null, crs, null, coords );
    }

    @Override
    public GeometryParticleConverter getGeometryConverter( String column, ICRS crs, String srid, boolean is2D ) {
        return new PostGISGeometryConverter( column, crs, srid, useLegacyPredicates );
    }

    @Override
    public PrimitiveParticleConverter getPrimitiveConverter( String column, PrimitiveType pt ) {
        return new DefaultPrimitiveConverter( pt, column );
    }

    @Override
    public void createDB( Connection adminConn, String dbName )
                            throws SQLException {

        String sql = "CREATE DATABASE \"" + dbName + "\" WITH template=template_postgis";

        Statement stmt = null;
        try {
            stmt = adminConn.createStatement();
            stmt.executeUpdate( sql );
        } finally {
            JDBCUtils.close( null, stmt, null, LOG );
        }
    }

    @Override
    public void dropDB( Connection adminConn, String dbName )
                            throws SQLException {

        String sql = "DROP DATABASE \"" + dbName + "\"";
        Statement stmt = null;
        try {
            stmt = adminConn.createStatement();
            stmt.executeUpdate( sql );
        } finally {
            JDBCUtils.close( null, stmt, null, LOG );
        }
    }

    @Override
    public void createAutoColumn( StringBuffer currentStmt, List<StringBuffer> additionalSmts, SQLIdentifier column,
                                  SQLIdentifier table ) {
        currentStmt.append( column );
        currentStmt.append( " serial" );
    }

    @Override
    public ResultSet getTableColumnMetadata( DatabaseMetaData md, TableName qTable )
                            throws SQLException {
        String schema = qTable.getSchema() != null ? qTable.getSchema() : getDefaultSchema();
        String table = qTable.getTable();
        return md.getColumns( null, schema.toLowerCase(), table.toLowerCase(), null );
    }

    /**
     * See http://postgresql.1045698.n5.nabble.com/BUG-3383-Postmaster-Service-Problem-td2123537.html.
     */
    @Override
    public boolean requiresTransactionForCursorMode() {
        return true;
    }

    @Override
    public String getSelectSequenceNextVal( String sequence ) {
        return "SELECT nextval('" + sequence + "')";
    }
}
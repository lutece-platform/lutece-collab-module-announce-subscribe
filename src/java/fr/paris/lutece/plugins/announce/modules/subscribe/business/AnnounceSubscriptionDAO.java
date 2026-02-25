/*
 * Copyright (c) 2002-2026, City of Paris
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 *  1. Redistributions of source code must retain the above copyright notice
 *     and the following disclaimer.
 *
 *  2. Redistributions in binary form must reproduce the above copyright notice
 *     and the following disclaimer in the documentation and/or other materials
 *     provided with the distribution.
 *
 *  3. Neither the name of 'Mairie de Paris' nor 'Lutece' nor the names of its
 *     contributors may be used to endorse or promote products derived from
 *     this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 *
 * License 1.0
 */
package fr.paris.lutece.plugins.announce.modules.subscribe.business;

import fr.paris.lutece.plugins.subscribe.business.SubscriptionFilter;
import fr.paris.lutece.portal.service.plugin.Plugin;
import fr.paris.lutece.util.sql.DAOUtil;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class AnnounceSubscriptionDAO implements IAnnounceSubscriptionDAO
{

    // Constants
    private static final String SQL_QUERY_NEW_PK = "SELECT max( id_subscription ) FROM subscribe_subscription";
    private static final String SQL_QUERY_SELECT = " SELECT id_subscription, id_user, subscription_provider, subscription_key, id_subscribed_resource FROM subscribe_subscription ";
    private static final String SQL_QUERY_SELECT_FROM_SUBSCRIPTION_ID = SQL_QUERY_SELECT + " WHERE id_subscription = ? ";
    private static final String SQL_QUERY_INSERT = "INSERT INTO subscribe_subscription ( id_subscription, id_user, subscription_provider, subscription_key, id_subscribed_resource ) VALUES ( ?, ?, ?, ?, ? ) ";
    private static final String SQL_QUERY_DELETE = "DELETE FROM subscribe_subscription WHERE id_subscription = ? ";
    private static final String SQL_QUERY_UPDATE = "UPDATE subscribe_subscription SET id_user = ?, subscription_provider = ?, subscription_key = ?, id_subscribed_resource = ? WHERE id_subscription = ?";
    private static final String SQL_QUERY_SELECTALL = SQL_QUERY_SELECT;

    private static final String SQL_FILTER_ID_USER = " id_user = ? ";
    private static final String SQL_FILTER_PROVIDER = " subscription_provider = ? ";
    private static final String SQL_FILTER_SUBSCRIPTION_KEY = " subscription_key = ? ";
    private static final String SQL_FILTER_ID_SUBSCRIBED_RESOURCE = " id_subscribed_resource = ? ";
    private static final String CONSTANT_WHERE = " WHERE ";
    private static final String CONSTANT_AND = " AND ";

    private static final String SQL_QUERY_SELECT_BY_CATEGORY = SQL_QUERY_SELECT + CONSTANT_WHERE + SQL_FILTER_PROVIDER + CONSTANT_AND
            + SQL_FILTER_SUBSCRIPTION_KEY + CONSTANT_AND + SQL_FILTER_ID_SUBSCRIBED_RESOURCE;

    private static final String SQL_QUERY_SELECT_BY_USER = SQL_QUERY_SELECT + CONSTANT_WHERE + SQL_FILTER_PROVIDER + CONSTANT_AND + SQL_FILTER_SUBSCRIPTION_KEY
            + CONSTANT_AND + SQL_FILTER_ID_SUBSCRIBED_RESOURCE;

    private static final String SQL_QUERY_SELECT_ALL_FILTERS = SQL_QUERY_SELECT + CONSTANT_WHERE + SQL_FILTER_PROVIDER + CONSTANT_AND
            + SQL_FILTER_SUBSCRIPTION_KEY;

    /**
     * Get a new primary key
     * 
     * @param plugin
     *            The plugin
     * @return The new primary key
     */
    private int newPrimaryKey( Plugin plugin )
    {
        int nKey = 1;
        try ( DAOUtil daoUtil = new DAOUtil( SQL_QUERY_NEW_PK, plugin ) )
        {
            daoUtil.executeQuery( );

            if ( daoUtil.next( ) )
            {
                nKey = daoUtil.getInt( 1 ) + 1;
            }
        }
        return nKey;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void insert( AnnounceSubscriptionDTO subscription, Plugin plugin )
    {
        subscription.setIdSubscription( newPrimaryKey( plugin ) );

        try ( DAOUtil daoUtil = new DAOUtil( SQL_QUERY_INSERT, plugin ) )
        {
            daoUtil.setInt( 1, subscription.getIdSubscription( ) );
            daoUtil.setString( 2, subscription.getUserId( ) );
            daoUtil.setString( 3, subscription.getSubscriptionProvider( ) );
            daoUtil.setString( 4, subscription.getSubscriptionKey( ) );
            daoUtil.setString( 5, subscription.getIdSubscribedResource( ) );

            daoUtil.executeUpdate( );
        }
    }

    @Override
    public void store( AnnounceSubscriptionDTO subscription, Plugin plugin )
    {
        try ( DAOUtil daoUtil = new DAOUtil( SQL_QUERY_UPDATE, plugin ) )
        {
            daoUtil.setString( 1, subscription.getUserId( ) );
            daoUtil.setString( 2, subscription.getSubscriptionProvider( ) );
            daoUtil.setString( 3, subscription.getSubscriptionKey( ) );
            daoUtil.setString( 4, subscription.getIdSubscribedResource( ) );
            daoUtil.setInt( 5, subscription.getIdSubscription( ) );

            daoUtil.executeUpdate( );
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void delete( int nSubscriptionId, Plugin plugin )
    {
        try ( DAOUtil daoUtil = new DAOUtil( SQL_QUERY_DELETE, plugin ) )
        {
            daoUtil.setInt( 1, nSubscriptionId );
            daoUtil.executeUpdate( );
        }
    }

    /**
     * Map the current row of a DAOUtil result set to an AnnounceSubscriptionDTO
     * 
     * @param daoUtil
     *            The DAOUtil positioned on the current row
     * @return The mapped DTO
     */
    private AnnounceSubscriptionDTO dataToObject( DAOUtil daoUtil )
    {
        AnnounceSubscriptionDTO subscription = new AnnounceSubscriptionDTO( );
        subscription.setIdSubscription( daoUtil.getInt( 1 ) );
        subscription.setUserId( daoUtil.getString( 2 ) );
        subscription.setSubscriptionProvider( daoUtil.getString( 3 ) );
        subscription.setSubscriptionKey( daoUtil.getString( 4 ) );
        subscription.setIdSubscribedResource( daoUtil.getString( 5 ) );
        return subscription;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public AnnounceSubscriptionDTO load( int nId, Plugin plugin )
    {
        AnnounceSubscriptionDTO subscription = null;

        try ( DAOUtil daoUtil = new DAOUtil( SQL_QUERY_SELECT_FROM_SUBSCRIPTION_ID, plugin ) )
        {
            daoUtil.setInt( 1, nId );
            daoUtil.executeQuery( );

            if ( daoUtil.next( ) )
            {
                subscription = dataToObject( daoUtil );
            }
        }
        return subscription;
    }

    @Override
    public Collection<AnnounceSubscriptionDTO> selectSubscriptionsList( Plugin plugin )
    {
        Collection<AnnounceSubscriptionDTO> subscriptionList = new ArrayList<AnnounceSubscriptionDTO>( );

        try ( DAOUtil daoUtil = new DAOUtil( SQL_QUERY_SELECTALL, plugin ) )
        {
            daoUtil.executeQuery( );

            while ( daoUtil.next( ) )
            {
                subscriptionList.add( dataToObject( daoUtil ) );
            }
        }
        return subscriptionList;
    }

    @Override
    public List<AnnounceSubscriptionDTO> findByFilter( SubscriptionFilter filter, Plugin plugin )
    {
        List<AnnounceSubscriptionDTO> listSubscription = new ArrayList<AnnounceSubscriptionDTO>( );
        boolean bHasFilter = false;
        StringBuilder sbSql = new StringBuilder( SQL_QUERY_SELECT );
        if ( StringUtils.isNotEmpty( filter.getUserId( ) ) )
        {
            sbSql.append( CONSTANT_WHERE );
            sbSql.append( SQL_FILTER_ID_USER );
            bHasFilter = true;
        }
        if ( filter.getSubscriptionProvider( ) != null )
        {
            if ( bHasFilter )
            {
                sbSql.append( CONSTANT_AND );
            }
            else
            {
                sbSql.append( CONSTANT_WHERE );
                bHasFilter = true;
            }
            sbSql.append( SQL_FILTER_PROVIDER );
        }
        if ( filter.getSubscriptionKey( ) != null )
        {
            if ( bHasFilter )
            {
                sbSql.append( CONSTANT_AND );
            }
            else
            {
                sbSql.append( CONSTANT_WHERE );
                bHasFilter = true;
            }
            sbSql.append( SQL_FILTER_SUBSCRIPTION_KEY );
        }
        if ( filter.getIdSubscribedResource( ) != null )
        {
            if ( bHasFilter )
            {
                sbSql.append( CONSTANT_AND );
            }
            else
            {
                sbSql.append( CONSTANT_WHERE );
                bHasFilter = true;
            }
            sbSql.append( SQL_FILTER_ID_SUBSCRIBED_RESOURCE );
        }

        int nIndex = 1;
        try ( DAOUtil daoUtil = new DAOUtil( sbSql.toString( ), plugin ) )
        {
            if ( StringUtils.isNotEmpty( filter.getUserId( ) ) )
            {
                daoUtil.setString( nIndex++, filter.getUserId( ) );
            }
            if ( filter.getSubscriptionProvider( ) != null )
            {
                daoUtil.setString( nIndex++, filter.getSubscriptionProvider( ) );
            }
            if ( filter.getSubscriptionKey( ) != null )
            {
                daoUtil.setString( nIndex++, filter.getSubscriptionKey( ) );
            }
            if ( filter.getIdSubscribedResource( ) != null )
            {
                daoUtil.setString( nIndex, filter.getIdSubscribedResource( ) );
            }
            daoUtil.executeQuery( );

            while ( daoUtil.next( ) )
            {
                listSubscription.add( dataToObject( daoUtil ) );
            }
        }

        return listSubscription;
    }

    @Override
    public List<AnnounceSubscriptionDTO> findByCategoryId( String strProviderName, int nCategoryId, Plugin plugin )
    {
        List<AnnounceSubscriptionDTO> listSubscription = new ArrayList<>( );

        try ( DAOUtil daoUtil = new DAOUtil( SQL_QUERY_SELECT_BY_CATEGORY, plugin ) )
        {
            int nIndex = 1;
            daoUtil.setString( nIndex++, strProviderName );
            daoUtil.setString( nIndex++, AnnounceSubscriptionKeys.SUBSCRIPTION_CATEGORY );
            daoUtil.setString( nIndex, Integer.toString( nCategoryId ) );
            daoUtil.executeQuery( );

            while ( daoUtil.next( ) )
            {
                listSubscription.add( dataToObject( daoUtil ) );
            }
        }

        return listSubscription;
    }

    @Override
    public List<AnnounceSubscriptionDTO> findByUserName( String strProviderName, String strUserName, Plugin plugin )
    {
        List<AnnounceSubscriptionDTO> listSubscription = new ArrayList<>( );

        try ( DAOUtil daoUtil = new DAOUtil( SQL_QUERY_SELECT_BY_USER, plugin ) )
        {
            int nIndex = 1;
            daoUtil.setString( nIndex++, strProviderName );
            daoUtil.setString( nIndex++, AnnounceSubscriptionKeys.SUBSCRIPTION_USER );
            daoUtil.setString( nIndex, strUserName );
            daoUtil.executeQuery( );

            while ( daoUtil.next( ) )
            {
                listSubscription.add( dataToObject( daoUtil ) );
            }
        }

        return listSubscription;
    }

    @Override
    public List<AnnounceSubscriptionDTO> findAllFilterSubscriptions( String strProviderName, Plugin plugin )
    {
        List<AnnounceSubscriptionDTO> listSubscription = new ArrayList<>( );

        try ( DAOUtil daoUtil = new DAOUtil( SQL_QUERY_SELECT_ALL_FILTERS, plugin ) )
        {
            int nIndex = 1;
            daoUtil.setString( nIndex++, strProviderName );
            daoUtil.setString( nIndex, AnnounceSubscriptionKeys.SUBSCRIPTION_FILTER );
            daoUtil.executeQuery( );

            while ( daoUtil.next( ) )
            {
                listSubscription.add( dataToObject( daoUtil ) );
            }
        }

        return listSubscription;
    }
}

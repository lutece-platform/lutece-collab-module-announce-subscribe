package fr.paris.lutece.plugins.module.announce.subscribe.business;

import fr.paris.lutece.plugins.subscribe.business.SubscriptionFilter;
import fr.paris.lutece.portal.service.plugin.Plugin;
import fr.paris.lutece.util.sql.DAOUtil;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class AnnounceSubscribtionDAO implements IAnnounceSubscribtionDAO {

    // Constants
    private static final String SQL_QUERY_NEW_PK = "SELECT max( id_subscription ) FROM subscribe_subscription";
    private static final String SQL_QUERY_SELECT = " SELECT id_subscription, id_user, subscription_provider, subscription_key, id_subscribed_resource, email_subscribes FROM subscribe_subscription ";
    private static final String SQL_QUERY_SELECT_DISTINCT = " SELECT DISTINCT id_user FROM subscribe_subscription ";
    private static final String SQL_QUERY_SELECT_FROM_SUBSCRIPTION_ID = SQL_QUERY_SELECT
            + " WHERE id_subscription = ? ";
    private static final String SQL_QUERY_INSERT = "INSERT INTO subscribe_subscription ( id_subscription, id_user, subscription_provider, subscription_key, id_subscribed_resource, email_subscribes ) VALUES ( ?, ?, ?, ?, ?, ? ) ";
    private static final String SQL_QUERY_DELETE = "DELETE FROM subscribe_subscription WHERE id_subscription = ? ";
    private static final String SQL_QUERY_UPDATE = "UPDATE subscribe_subscription SET id_user = ?, subscription_provider = ?, subscription_key = ?, id_subscribed_resource = ?, email_subscribes = ? WHERE id_subscription = ?";
    private static final String SQL_QUERY_SELECTALL = "SELECT id_subscription, id_user, subscription_provider, subscription_key, id_subscribed_resource, email_subscribes FROM subscribe_subscription";

    private static final String SQL_FILTER_ID_USER = " id_user = ? ";
    private static final String SQL_FILTER_PROVIDER = " subscription_provider = ? ";
    private static final String SQL_FILTER_SUBSCRIPTION_KEY = " subscription_key = ? ";
    private static final String SQL_FILTER_ID_SUBSCRIBED_RESOURCE = " id_subscribed_resource = ? ";
    private static final String SQL_FILTER_ID_USER_ORDERED = " id_user";
    private static final String SQL_FILTER_EMAIL_SUBSCRIBES = " email_subscribes != ?";
    private static final String CONSTANT_WHERE = " WHERE ";
    private static final String CONSTANT_AND = " AND ";
    private static final String CONSTANT_OR = " OR ";
    private static final String CONSTANT_COMMA = " , ";
    private static final String CONSTANT_GROUP_BY = " GROUP BY ";
    private static final String CONSTANT_ORDER_BY = " ORDER BY ";
    private static final String SQL_FILTER_ID_SUBSCRIPTION = " id_subscription ";
    private static final String CONSTANT_LIMIT = " LIMIT ";

    /**
     * Get a new primary key
     * @param plugin The plugin
     * @return The new primary key
     */
    private int newPrimaryKey( Plugin plugin )
    {
        DAOUtil daoUtil = new DAOUtil( SQL_QUERY_NEW_PK, plugin );
        daoUtil.executeQuery( );

        int nKey = 1;
        if ( daoUtil.next( ) )
        {
            nKey = daoUtil.getInt( 1 ) + 1;
        }

        daoUtil.free( );
        return nKey;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void insert(AnnounceSubscribtionDTO subscription, Plugin plugin)
    {
        DAOUtil daoUtil = new DAOUtil( SQL_QUERY_INSERT, plugin );

        subscription.setIdSubscription( newPrimaryKey( plugin ) );

        daoUtil.setInt( 1, subscription.getIdSubscription( ) );
        daoUtil.setString( 2, subscription.getUserId( ) );
        daoUtil.setString( 3, subscription.getSubscriptionProvider( ) );
        daoUtil.setString( 4, subscription.getSubscriptionKey( ) );
        daoUtil.setString( 5, subscription.getIdSubscribedResource( ) );
        daoUtil.setString( 6, subscription.getEmailSubscribes( ) );

        daoUtil.executeUpdate( );
        daoUtil.free( );
    }

    @Override
    public void store(AnnounceSubscribtionDTO subscription, Plugin plugin) {
        DAOUtil daoUtil = new DAOUtil( SQL_QUERY_UPDATE, plugin );

        daoUtil.setString( 1, subscription.getUserId( ) );
        daoUtil.setString( 2, subscription.getSubscriptionProvider( ) );
        daoUtil.setString( 3, subscription.getSubscriptionKey( ) );
        daoUtil.setString( 4, subscription.getIdSubscribedResource( ) );
        daoUtil.setInt( 5, subscription.getIdSubscription( ) );
        daoUtil.setString( 6, subscription.getEmailSubscribes( ) );

        daoUtil.executeUpdate( );
        daoUtil.free( );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void delete( int nSubscriptionId, Plugin plugin )
    {
        DAOUtil daoUtil = new DAOUtil( SQL_QUERY_DELETE, plugin );
        daoUtil.setInt( 1, nSubscriptionId );
        daoUtil.executeUpdate( );
        daoUtil.free( );
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public AnnounceSubscribtionDTO load(int nId, Plugin plugin) {
        DAOUtil daoUtil = new DAOUtil( SQL_QUERY_SELECT_FROM_SUBSCRIPTION_ID, plugin );
        daoUtil.setInt( 1, nId );
        daoUtil.executeQuery( );

        AnnounceSubscribtionDTO subscription = null;

        if ( daoUtil.next( ) )
        {
            subscription = new AnnounceSubscribtionDTO( );
            subscription.setIdSubscription( daoUtil.getInt( 1 ) );
            subscription.setUserId( daoUtil.getString( 2 ) );
            subscription.setSubscriptionProvider( daoUtil.getString( 3 ) );
            subscription.setSubscriptionKey( daoUtil.getString( 4 ) );
            subscription.setIdSubscribedResource( daoUtil.getString( 5 ) );
            subscription.setEmailSubscribes( daoUtil.getString( 6 ) );
        }

        daoUtil.free( );
        return subscription;
    }



    @Override
    public Collection<AnnounceSubscribtionDTO> selectSubscriptionsList(Plugin plugin) {
        Collection<AnnounceSubscribtionDTO> subscriptionList = new ArrayList<AnnounceSubscribtionDTO>( );
        DAOUtil daoUtil = new DAOUtil( SQL_QUERY_SELECTALL, plugin );
        daoUtil.executeQuery( );

        while ( daoUtil.next( ) )
        {
            AnnounceSubscribtionDTO subscription = new AnnounceSubscribtionDTO( );

            subscription.setIdSubscription( daoUtil.getInt( 1 ) );
            subscription.setUserId( daoUtil.getString( 2 ) );
            subscription.setSubscriptionProvider( daoUtil.getString( 3 ) );
            subscription.setSubscriptionKey( daoUtil.getString( 4 ) );
            subscription.setIdSubscribedResource( daoUtil.getString( 5 ) );
            subscription.setEmailSubscribes( daoUtil.getString( 6 ) );

            subscriptionList.add( subscription );
        }

        daoUtil.free( );
        return subscriptionList;
    }

    @Override
    public List<AnnounceSubscribtionDTO> findByFilter(SubscriptionFilter filter) {
        List<AnnounceSubscribtionDTO> listSubscription = new ArrayList<AnnounceSubscribtionDTO>( );
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
        DAOUtil daoUtil = new DAOUtil( sbSql.toString( ) );
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
            // Warning, no increment here !
            daoUtil.setString( nIndex, filter.getIdSubscribedResource( ) );
        }
        daoUtil.executeQuery( );

        while ( daoUtil.next( ) )
        {
            AnnounceSubscribtionDTO subscription = new AnnounceSubscribtionDTO( );
            subscription.setIdSubscription( daoUtil.getInt( 1 ) );
            subscription.setUserId( daoUtil.getString( 2 ) );
            subscription.setSubscriptionProvider( daoUtil.getString( 3 ) );
            subscription.setSubscriptionKey( daoUtil.getString( 4 ) );
            subscription.setIdSubscribedResource( daoUtil.getString( 5 ) );
            subscription.setEmailSubscribes( daoUtil.getString( 6 ) );
            listSubscription.add( subscription );
        }

        daoUtil.free( );

        return listSubscription;
    }

    @Override
    public List<AnnounceSubscribtionDTO> findByFilterOr(String userId, String categoryId, String limitRows) {
        List<AnnounceSubscribtionDTO> listSubscription = new ArrayList<AnnounceSubscribtionDTO>( );
        boolean bHasFilter = false;
        StringBuilder sbSql = new StringBuilder( SQL_QUERY_SELECT );
        sbSql.append( CONSTANT_WHERE );

        if ( StringUtils.isNotEmpty( userId ) )
        {

            sbSql.append( SQL_FILTER_ID_SUBSCRIBED_RESOURCE );
            bHasFilter = true;
        }
        if ( StringUtils.isNotEmpty( categoryId )  )
        {
            if ( bHasFilter )
            {
                sbSql.append( CONSTANT_OR );
            }
            else
            {
              bHasFilter = true;
            }
            sbSql.append( SQL_FILTER_ID_SUBSCRIBED_RESOURCE );
        }
        if ( bHasFilter ) {
            sbSql.append( CONSTANT_AND);
            sbSql.append(SQL_FILTER_EMAIL_SUBSCRIBES);
        }else
            {
                sbSql.append(SQL_FILTER_EMAIL_SUBSCRIBES);
                bHasFilter = true;
            }

        sbSql.append(CONSTANT_GROUP_BY+SQL_FILTER_ID_USER_ORDERED);
        sbSql.append(CONSTANT_ORDER_BY+SQL_FILTER_ID_SUBSCRIPTION);
        sbSql.append(CONSTANT_LIMIT+limitRows);

        int nIndex = 1;
        DAOUtil daoUtil = new DAOUtil( sbSql.toString( ) );
        if ( StringUtils.isNotEmpty( userId ) )
        {
            daoUtil.setString( nIndex++, userId);
        }
        if ( StringUtils.isNotEmpty( categoryId ) )
        {
            daoUtil.setString( nIndex++, categoryId );
        }
        daoUtil.setString( nIndex, "NULL" );
        daoUtil.executeQuery( );

        while ( daoUtil.next( ) )
        {
            AnnounceSubscribtionDTO subscription = new AnnounceSubscribtionDTO( );
            subscription.setIdSubscription( daoUtil.getInt( 1 ) );
            subscription.setUserId( daoUtil.getString( 2 ) );
            subscription.setSubscriptionProvider( daoUtil.getString( 3 ) );
            subscription.setSubscriptionKey( daoUtil.getString( 4 ) );
            subscription.setIdSubscribedResource( daoUtil.getString( 5 ) );
            subscription.setEmailSubscribes( daoUtil.getString( 6 ) );
            listSubscription.add( subscription );
        }

        daoUtil.free( );

        return listSubscription;
    }
}

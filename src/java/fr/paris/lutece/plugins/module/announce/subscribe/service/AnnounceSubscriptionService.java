package fr.paris.lutece.plugins.module.announce.subscribe.service;

import fr.paris.lutece.plugins.module.announce.subscribe.business.AnnounceSubscribtionDTO;
import fr.paris.lutece.plugins.module.announce.subscribe.business.IAnnounceSubscribtionDAO;
import fr.paris.lutece.plugins.subscribe.business.Subscription;
import fr.paris.lutece.plugins.subscribe.business.SubscriptionFilter;
import fr.paris.lutece.plugins.subscribe.service.ISubscriptionProviderService;
import fr.paris.lutece.portal.service.security.LuteceUser;
import fr.paris.lutece.portal.service.security.LuteceUserService;
import fr.paris.lutece.portal.service.spring.SpringContextService;
import org.apache.commons.lang3.StringUtils;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class AnnounceSubscriptionService {

    private static AnnounceSubscriptionService _instance = new AnnounceSubscriptionService( );
    private IAnnounceSubscribtionDAO _dao = SpringContextService.getBean( "subscribe.announceSubscriptionDAO" );

    /**
     * Private constructor
     */
    private AnnounceSubscriptionService( )
    {
        // Do nothing
    }

    public static AnnounceSubscriptionService getInstance( )
    {
        return _instance;
    }

    public void createSubscription(AnnounceSubscribtionDTO subscription, LuteceUser user )
    {
        createSubscription( subscription, user.getName( ));
    }

    public void createSubscription(AnnounceSubscribtionDTO subscription, String strLuteceUserName)
    {
        subscription.setUserId( strLuteceUserName);
        createSubscription( subscription );
    }

    public void createSubscription( AnnounceSubscribtionDTO subscription )
    {
        _dao.insert( subscription, AnnounceSubscribePlugin.getPlugin( ) );
    }

    public Subscription findBySubscriptionId(int nIdSubscription )
    {
        return _dao.load( nIdSubscription, AnnounceSubscribePlugin.getPlugin( ) );
    }

    public List<AnnounceSubscribtionDTO> findByFilter(SubscriptionFilter filter )
    {
        return _dao.findByFilter( filter );
    }

    public List<AnnounceSubscribtionDTO> findByFilterOr(String userId, String categoryId, String limitRows)
    {
        return _dao.findByFilterOr( userId, categoryId, limitRows );
    }

    public void removeSubscription( int nIdSubscription, boolean bNotifySubscriptionProvider )
    {
        if ( bNotifySubscriptionProvider )
        {
            removeSubscription( findBySubscriptionId( nIdSubscription ), bNotifySubscriptionProvider );
        }
        else
        {
            _dao.delete( nIdSubscription, AnnounceSubscribePlugin.getPlugin( ) );
        }
    }

    public void removeSubscription( Subscription subscription, boolean bNotifySubscriptionProvider )
    {
        if ( bNotifySubscriptionProvider )
        {
            List<ISubscriptionProviderService> listProviders = SpringContextService
                    .getBeansOfType( ISubscriptionProviderService.class );
            for ( ISubscriptionProviderService provider : listProviders )
            {
                if ( StringUtils.equals( subscription.getSubscriptionProvider( ), provider.getProviderName( ) ) )
                {
                    provider.notifySubscriptionRemoval( subscription );
                }
            }
        }
        _dao.delete( subscription.getIdSubscription( ), AnnounceSubscribePlugin.getPlugin( ) );
    }

    public LuteceUser getLuteceUserFromSubscription( Subscription subscription )
    {
        return LuteceUserService.getLuteceUserFromName( subscription.getUserId( ) );
    }

    public Collection<LuteceUser> getSubscriberList(String strSubscriptionProvider, String strSubscriptionKey,
                                                    String strIdSubscribedResource )
    {
        SubscriptionFilter filter = new SubscriptionFilter( );
        filter.setSubscriptionProvider( strSubscriptionProvider );
        filter.setSubscriptionKey( strSubscriptionKey );
        filter.setIdSubscribedResource( strIdSubscribedResource );
        List<AnnounceSubscribtionDTO> listSubscription = findByFilter( filter );
        Set<LuteceUser> usersFound = new HashSet<LuteceUser>( );
        for ( Subscription subscription : listSubscription )
        {
            LuteceUser user = LuteceUserService.getLuteceUserFromName( subscription.getUserId( ) );
            if ( user != null )
            {
                usersFound.add( user );
            }
        }
        return usersFound;
    }

}

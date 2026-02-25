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
package fr.paris.lutece.plugins.announce.modules.subscribe.service;

import fr.paris.lutece.plugins.announce.modules.subscribe.business.AnnounceSubscriptionDTO;
import fr.paris.lutece.plugins.announce.modules.subscribe.business.IAnnounceSubscriptionDAO;
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

public class AnnounceSubscriptionService
{

    private static final String BEAN_DAO = "subscribe.announceSubscriptionDAO";
    private static AnnounceSubscriptionService _instance = new AnnounceSubscriptionService( );
    private IAnnounceSubscriptionDAO _dao;

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

    private IAnnounceSubscriptionDAO getDAO( )
    {
        if ( _dao == null )
        {
            _dao = SpringContextService.getBean( BEAN_DAO );
        }
        return _dao;
    }

    public void createSubscription( AnnounceSubscriptionDTO subscription, LuteceUser user )
    {
        createSubscription( subscription, user.getName( ) );
    }

    public void createSubscription( AnnounceSubscriptionDTO subscription, String strLuteceUserName )
    {
        subscription.setUserId( strLuteceUserName );
        createSubscription( subscription );
    }

    public void createSubscription( AnnounceSubscriptionDTO subscription )
    {
        getDAO( ).insert( subscription, AnnounceSubscribePlugin.getPlugin( ) );
    }

    public Subscription findBySubscriptionId( int nIdSubscription )
    {
        return getDAO( ).load( nIdSubscription, AnnounceSubscribePlugin.getPlugin( ) );
    }

    public List<AnnounceSubscriptionDTO> findByFilter( SubscriptionFilter filter )
    {
        return getDAO( ).findByFilter( filter, AnnounceSubscribePlugin.getPlugin( ) );
    }

    public List<AnnounceSubscriptionDTO> findByCategoryId( String strProviderName, int nCategoryId )
    {
        return getDAO( ).findByCategoryId( strProviderName, nCategoryId, AnnounceSubscribePlugin.getPlugin( ) );
    }

    public List<AnnounceSubscriptionDTO> findByUserName( String strProviderName, String strUserName )
    {
        return getDAO( ).findByUserName( strProviderName, strUserName, AnnounceSubscribePlugin.getPlugin( ) );
    }

    public List<AnnounceSubscriptionDTO> findAllFilterSubscriptions( String strProviderName )
    {
        return getDAO( ).findAllFilterSubscriptions( strProviderName, AnnounceSubscribePlugin.getPlugin( ) );
    }

    public void removeSubscription( int nIdSubscription, boolean bNotifySubscriptionProvider )
    {
        if ( bNotifySubscriptionProvider )
        {
            removeSubscription( findBySubscriptionId( nIdSubscription ), bNotifySubscriptionProvider );
        }
        else
        {
            getDAO( ).delete( nIdSubscription, AnnounceSubscribePlugin.getPlugin( ) );
        }
    }

    public void removeSubscription( Subscription subscription, boolean bNotifySubscriptionProvider )
    {
        if ( bNotifySubscriptionProvider )
        {
            List<ISubscriptionProviderService> listProviders = SpringContextService.getBeansOfType( ISubscriptionProviderService.class );
            for ( ISubscriptionProviderService provider : listProviders )
            {
                if ( StringUtils.equals( subscription.getSubscriptionProvider( ), provider.getProviderName( ) ) )
                {
                    provider.notifySubscriptionRemoval( subscription );
                }
            }
        }
        getDAO( ).delete( subscription.getIdSubscription( ), AnnounceSubscribePlugin.getPlugin( ) );
    }

    public LuteceUser getLuteceUserFromSubscription( Subscription subscription )
    {
        return LuteceUserService.getLuteceUserFromName( subscription.getUserId( ) );
    }

    public Collection<LuteceUser> getSubscriberList( String strSubscriptionProvider, String strSubscriptionKey, String strIdSubscribedResource )
    {
        SubscriptionFilter filter = new SubscriptionFilter( );
        filter.setSubscriptionProvider( strSubscriptionProvider );
        filter.setSubscriptionKey( strSubscriptionKey );
        filter.setIdSubscribedResource( strIdSubscribedResource );
        List<AnnounceSubscriptionDTO> listSubscription = findByFilter( filter );
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

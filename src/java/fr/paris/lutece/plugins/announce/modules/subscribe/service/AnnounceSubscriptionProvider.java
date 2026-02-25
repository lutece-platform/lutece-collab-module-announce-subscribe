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

import fr.paris.lutece.plugins.announce.business.*;
import fr.paris.lutece.plugins.announce.service.IAnnounceSubscriptionProvider;
import fr.paris.lutece.plugins.announce.web.AnnounceApp;
import fr.paris.lutece.plugins.announce.modules.subscribe.business.AnnounceSubscriptionDTO;
import fr.paris.lutece.plugins.announce.modules.subscribe.business.AnnounceSubscriptionKeys;
import fr.paris.lutece.plugins.subscribe.business.Subscription;
import fr.paris.lutece.plugins.subscribe.business.SubscriptionFilter;
import fr.paris.lutece.plugins.subscribe.service.ISubscriptionProviderService;
import fr.paris.lutece.plugins.subscribe.web.SubscribeApp;
import fr.paris.lutece.portal.service.security.LuteceUser;
import fr.paris.lutece.portal.service.security.LuteceUserService;
import fr.paris.lutece.portal.service.spring.SpringContextService;
import fr.paris.lutece.portal.service.template.AppTemplateService;
import fr.paris.lutece.portal.service.util.AppLogService;
import fr.paris.lutece.portal.web.LocalVariables;
import fr.paris.lutece.util.html.HtmlTemplate;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

/**
 * Subscription provider service for announces. Implements both {@link ISubscriptionProviderService} (from plugin-subscribe) and
 * {@link IAnnounceSubscriptionProvider} (from plugin-announce) to provide the optional subscription functionality.
 */
public class AnnounceSubscriptionProvider implements ISubscriptionProviderService, IAnnounceSubscriptionProvider
{
    /**
     * Subscription key to subscribe to announces published by users
     */
    public static final String SUBSCRIPTION_USER = "announce_user";

    /**
     * Subscription key to subscribe to announces of a given category
     */
    public static final String SUBSCRIPTION_CATEGORY = "announce_category";

    /**
     * Subscription key to subscribe to announces matching a filter
     */
    public static final String SUBSCRIPTION_FILTER = "announce_filter";

    // Markers
    private static final String MARK_FILTER = "filter";
    private static final String MARK_CATEGORY = "category";
    private static final String MARK_USER_NAME = "user_name";
    private static final String MARK_USER = "user";
    private static final String PROVIDER_NAME = "announce.announceSubscriptionProvider";
    private static final String BEAN_NAME = "announce.announceSubscriptionProvider";

    // Templates
    private static final String TEMPLATE_FILTER_SUBSCRIPTION_DESCRIPTION = "skin/plugins/announce/subscription/filter_subscription_description.html";
    private static final String TEMPLATE_USER_SUBSCRIPTION_DESCRIPTION = "skin/plugins/announce/subscription/user_subscription_description.html";
    private static final String TEMPLATE_CATEGORY_SUBSCRIPTION_DESCRIPTION = "skin/plugins/announce/subscription/category_subscription_description.html";
    private static volatile AnnounceSubscriptionProvider _instance;

    /**
     * {@inheritDoc}
     */
    @Override
    public String getProviderName( )
    {
        return PROVIDER_NAME;
    }

    /**
     * Get the instance of the service
     *
     * @return The instance of the service
     */
    public static AnnounceSubscriptionProvider getService( )
    {
        if ( _instance == null )
        {
            synchronized( AnnounceSubscriptionProvider.class )
            {
                if ( _instance == null )
                {
                    _instance = SpringContextService.getBean( BEAN_NAME );
                }
            }
        }

        return _instance;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getSubscriptionHtmlDescription( LuteceUser user, String strSubscriptionKey, String strIdSubscribedResource, Locale locale )
    {
        if ( Strings.CS.equals( SUBSCRIPTION_USER, strSubscriptionKey ) )
        {
            Map<String, Object> model = new HashMap<>( );
            LuteceUser subscribedUser = LuteceUserService.getLuteceUserFromName( strIdSubscribedResource );

            model.put( MARK_USER_NAME, strIdSubscribedResource );
            model.put( MARK_USER, subscribedUser );

            HtmlTemplate template = AppTemplateService.getTemplate( TEMPLATE_USER_SUBSCRIPTION_DESCRIPTION, locale, model );

            return template.getHtml( );
        }
        else if ( Strings.CS.equals( SUBSCRIPTION_CATEGORY, strSubscriptionKey ) )
        {
            Map<String, Object> model = new HashMap<>( );

            int nIdCategory = Integer.parseInt( strIdSubscribedResource );

            model.put( MARK_CATEGORY, CategoryHome.findByPrimaryKey( nIdCategory ) );

            HtmlTemplate template = AppTemplateService.getTemplate( TEMPLATE_CATEGORY_SUBSCRIPTION_DESCRIPTION, locale, model );

            return template.getHtml( );
        }
        else if ( Strings.CS.equals( SUBSCRIPTION_FILTER, strSubscriptionKey ) )
        {
            AnnounceSearchFilter filter = AnnounceSearchFilterHome.findByPrimaryKey( Integer.parseInt( strIdSubscribedResource ) );
            Map<String, Object> model = new HashMap<>( );
            model.put( MARK_FILTER, filter );

            if ( filter.getIdCategory( ) > 0 )
            {
                model.put( MARK_CATEGORY, CategoryHome.findByPrimaryKey( filter.getIdCategory( ) ) );
            }

            HtmlTemplate template = AppTemplateService.getTemplate( TEMPLATE_FILTER_SUBSCRIPTION_DESCRIPTION, locale, model );

            return template.getHtml( );
        }

        return StringUtils.EMPTY;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getSubscriptionHtmlDescriptionBis( LuteceUser user, String strSubscriptionKey, String strIdSubscribedResource, Locale locale, String userSub )
    {
        if ( Strings.CS.equals( SUBSCRIPTION_USER, strSubscriptionKey ) )
        {
            Map<String, Object> model = new HashMap<>( );

            String strUserSub = "";
            if ( StringUtils.isNotEmpty( userSub ) )
            {
                LuteceUser subscribedUser = LuteceUserService.getLuteceUserFromName( userSub );
                if ( subscribedUser != null )
                {
                    strUserSub = subscribedUser.getUserInfo( LuteceUser.NAME_FAMILY ) + " " + subscribedUser.getUserInfo( LuteceUser.NAME_GIVEN );
                }
            }

            model.put( MARK_USER_NAME, strIdSubscribedResource );
            model.put( "strUserSub", strUserSub );

            HtmlTemplate template = AppTemplateService.getTemplate( TEMPLATE_USER_SUBSCRIPTION_DESCRIPTION, locale, model );

            return template.getHtml( );
        }

        // Category and filter branches are identical to getSubscriptionHtmlDescription
        return getSubscriptionHtmlDescription( user, strSubscriptionKey, strIdSubscribedResource, locale );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isSubscriptionRemovable( LuteceUser user, String strSubscriptionKey, String strIdSubscribedResource )
    {
        // Subscriptions to user and filters are always removable
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getUrlModifySubscription( LuteceUser user, String strSubscriptionKey, String strIdSubscribedResource )
    {
        if ( Strings.CS.equals( SUBSCRIPTION_FILTER, strSubscriptionKey ) )
        {
            int nIdFilter = ( StringUtils.isNotEmpty( strIdSubscribedResource ) && StringUtils.isNumeric( strIdSubscribedResource ) )
                    ? Integer.parseInt( strIdSubscribedResource )
                    : 0;

            if ( nIdFilter > 0 )
            {
                return AnnounceApp.getUrlSearchAnnounce( LocalVariables.getRequest( ), nIdFilter );
            }
        }
        else if ( Strings.CS.equals( SUBSCRIPTION_USER, strSubscriptionKey ) )
        {
            return AnnounceApp.getUrlViewUserAnnounces( LocalVariables.getRequest( ), strIdSubscribedResource );
        }
        else if ( Strings.CS.equals( SUBSCRIPTION_CATEGORY, strSubscriptionKey ) )
        {
            int nIdCategory = ( StringUtils.isNotEmpty( strIdSubscribedResource ) && StringUtils.isNumeric( strIdSubscribedResource ) )
                    ? Integer.parseInt( strIdSubscribedResource )
                    : 0;

            return AnnounceApp.getUrlViewCategory( LocalVariables.getRequest( ), nIdCategory );
        }

        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void notifySubscriptionRemoval( Subscription subscription )
    {
        if ( Strings.CS.equals( subscription.getSubscriptionKey( ), SUBSCRIPTION_FILTER ) && StringUtils.isNotEmpty( subscription.getIdSubscribedResource( ) )
                && StringUtils.isNumeric( subscription.getIdSubscribedResource( ) ) )
        {
            int nIdFilter = Integer.parseInt( subscription.getIdSubscribedResource( ) );
            AnnounceSearchFilterHome.delete( nIdFilter );
        }

        // We do nothing for users and category subscriptions
    }

    /**
     * Create a subscription to a user
     *
     * @param user
     *            The user that subscribe to another one
     * @param strUserName
     *            The name of the user to subscribe to
     */
    public void createSubscriptionToUser( LuteceUser user, String strUserName )
    {
        createSubscription( user, strUserName, SUBSCRIPTION_USER );
    }

    /**
     * Create a subscription to a filter
     *
     * @param user
     *            The user that subscribe to the filter
     * @param nIdFilter
     *            The id of the filter to subscribe to
     */
    public void createSubscriptionToFilter( LuteceUser user, int nIdFilter )
    {
        createSubscription( user, Integer.toString( nIdFilter ), SUBSCRIPTION_FILTER );
    }

    /**
     * Create a subscription to a category
     *
     * @param user
     *            The user that subscribe to the category
     * @param nIdCategory
     *            The id of the category to subscribe to
     */
    public void createSubscriptionToCategory( LuteceUser user, int nIdCategory )
    {
        createSubscription( user, Integer.toString( nIdCategory ), SUBSCRIPTION_CATEGORY );
    }

    /**
     * Remove a subscription to a user
     *
     * @param user
     *            The user that subscribe to another one
     * @param strUserName
     *            The name of the user to subscribe to
     */
    public void removeSubscriptionToUser( LuteceUser user, String strUserName )
    {
        removeSubscription( user, strUserName, SUBSCRIPTION_USER );
    }

    /**
     * Remove a subscription to a filter
     *
     * @param user
     *            The user that subscribe to the filter
     * @param nIdFilter
     *            The id of the filter to subscribe to
     */
    public void removeSubscriptionToFilter( LuteceUser user, int nIdFilter )
    {
        removeSubscription( user, Integer.toString( nIdFilter ), SUBSCRIPTION_FILTER );
    }

    /**
     * Remove a subscription to a category
     *
     * @param user
     *            The user that subscribe to the category
     * @param nIdCategory
     *            The id of the category to subscribe to
     */
    public void removeSubscriptionToCategory( LuteceUser user, int nIdCategory )
    {
        removeSubscription( user, Integer.toString( nIdCategory ), SUBSCRIPTION_CATEGORY );
    }

    /**
     * Do create a subscription to a user, a filter or a category
     *
     * @param user
     *            The user to subscribe to
     * @param strIdResource
     *            the id of the resource to subscribe to
     * @param strSubscriptionKey
     *            The subscription key
     */
    private void createSubscription( LuteceUser user, String strIdResource, String strSubscriptionKey )
    {
        AnnounceSubscriptionDTO subscription = new AnnounceSubscriptionDTO( );
        subscription.setIdSubscribedResource( strIdResource );
        subscription.setSubscriptionKey( strSubscriptionKey );
        subscription.setSubscriptionProvider( getProviderName( ) );
        subscription.setUserId( user.getName( ) );
        AnnounceSubscriptionService.getInstance( ).createSubscription( subscription );
    }

    /**
     * Do remove a subscription
     *
     * @param user
     *            The user
     * @param strIdResource
     *            The id of the resource to remove the subscription to
     * @param strSubscriptionKey
     *            The subscription key of the subscription to remove
     */
    private void removeSubscription( LuteceUser user, String strIdResource, String strSubscriptionKey )
    {
        SubscriptionFilter filter = new SubscriptionFilter( user.getName( ), getProviderName( ), strSubscriptionKey, strIdResource );
        List<AnnounceSubscriptionDTO> listSubscriptions = AnnounceSubscriptionService.getInstance( ).findByFilter( filter );

        for ( AnnounceSubscriptionDTO subscription : listSubscriptions )
        {
            AnnounceSubscriptionService.getInstance( ).removeSubscription( subscription, false );
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean hasSubscribedToUser( LuteceUser user, String userName )
    {
        return hasSubscribedtoResource( user, userName, SUBSCRIPTION_USER );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getSubscriptionListHtml( HttpServletRequest request )
    {
        return SubscribeApp.getSubscriptionList( request );
    }

    /**
     * Check if a user has subscribed to a category
     *
     * @param user
     *            The subscriber user
     * @param nIdCategory
     *            The id of the subscribed category
     * @return True if the user has subscribed to a category, false otherwise
     */
    public boolean hasSubscribedToCategory( LuteceUser user, int nIdCategory )
    {
        if ( nIdCategory == 0 )
        {
            return false;
        }

        return hasSubscribedtoResource( user, Integer.toString( nIdCategory ), SUBSCRIPTION_CATEGORY );
    }

    /**
     * Check if a user has subscribed to a given resource
     *
     * @param user
     *            The user to check the subscription of
     * @param strIdResource
     *            The id of the resource
     * @param strSubscriptionKey
     *            The subscription key
     * @return True if the user has subscribed to the given resource, false otherwise
     */
    private boolean hasSubscribedtoResource( LuteceUser user, String strIdResource, String strSubscriptionKey )
    {
        SubscriptionFilter filter = new SubscriptionFilter( user.getName( ), getProviderName( ), strSubscriptionKey, strIdResource );
        List<AnnounceSubscriptionDTO> listSubscription = AnnounceSubscriptionService.getInstance( ).findByFilter( filter );

        return CollectionUtils.isNotEmpty( listSubscription );
    }

    /**
     * Get the list of subscriptions to users
     *
     * @return The list of subscriptions to users
     */
    public List<AnnounceSubscriptionDTO> getSubscriptionsToUsers( )
    {
        return getSubscriptionsToResource( SUBSCRIPTION_USER );
    }

    /**
     * Get the list of subscriptions to categories
     *
     * @return The list of subscriptions to categories
     */
    public List<AnnounceSubscriptionDTO> getSubscriptionsToCategories( )
    {
        return getSubscriptionsToResource( SUBSCRIPTION_CATEGORY );
    }

    /**
     * Get the list of subscriptions to filters
     *
     * @return The list of subscriptions to filters
     */
    public List<AnnounceSubscriptionDTO> getSubscriptionsToFilters( )
    {
        return getSubscriptionsToResource( SUBSCRIPTION_FILTER );
    }

    /**
     * Get the list of subscriptions of a given type
     *
     * @param strSubscriptionKey
     *            The type of subscriptions to get
     * @return The list of subscriptions of the given type
     */
    public List<AnnounceSubscriptionDTO> getSubscriptionsToResource( String strSubscriptionKey )
    {
        SubscriptionFilter filter = new SubscriptionFilter( );
        filter.setSubscriptionKey( strSubscriptionKey );
        filter.setSubscriptionProvider( getProviderName( ) );

        return AnnounceSubscriptionService.getInstance( ).findByFilter( filter );
    }

    /**
     * Get all subscriptions to a given category
     *
     * @param nCategoryId
     *            The category id
     * @return The list of subscriptions for this category
     */
    public List<AnnounceSubscriptionDTO> getSubscriptionsByCategory( int nCategoryId )
    {
        return AnnounceSubscriptionService.getInstance( ).findByCategoryId( getProviderName( ), nCategoryId );
    }

    /**
     * Get all subscriptions to a given user
     *
     * @param strUserName
     *            The user name
     * @return The list of subscriptions for this user
     */
    public List<AnnounceSubscriptionDTO> getSubscriptionsByUser( String strUserName )
    {
        return AnnounceSubscriptionService.getInstance( ).findByUserName( getProviderName( ), strUserName );
    }

    /**
     * Get all filter subscriptions that match the given announce. Loads each saved filter and checks if the announce matches its criteria.
     *
     * @param announce
     *            The announce to match against saved filters
     * @return The list of matching filter subscriptions
     */
    public List<AnnounceSubscriptionDTO> getMatchingFilterSubscriptions( Announce announce )
    {
        List<AnnounceSubscriptionDTO> allFilterSubscriptions = AnnounceSubscriptionService.getInstance( ).findAllFilterSubscriptions( getProviderName( ) );
        List<AnnounceSubscriptionDTO> matchingSubscriptions = new ArrayList<>( );

        for ( AnnounceSubscriptionDTO subscription : allFilterSubscriptions )
        {
            String strIdFilter = subscription.getIdSubscribedResource( );

            if ( StringUtils.isEmpty( strIdFilter ) || !StringUtils.isNumeric( strIdFilter ) )
            {
                continue;
            }

            int nIdFilter = Integer.parseInt( strIdFilter );
            AnnounceSearchFilter filter = AnnounceSearchFilterHome.findByPrimaryKey( nIdFilter );

            if ( filter == null )
            {
                AppLogService.info( "AnnounceSubscriptionProvider: filter {} no longer exists, skipping subscription {}", nIdFilter,
                        subscription.getIdSubscription( ) );
                continue;
            }

            if ( doesAnnounceMatchFilter( announce, filter ) )
            {
                matchingSubscriptions.add( subscription );
            }
        }

        return matchingSubscriptions;
    }

    /**
     * Check if an announce matches a search filter's criteria
     *
     * @param announce
     *            The announce to check
     * @param filter
     *            The filter with criteria to match against
     * @return true if the announce matches all non-empty criteria of the filter
     */
    private boolean doesAnnounceMatchFilter( Announce announce, AnnounceSearchFilter filter )
    {
        // Category check
        if ( filter.getIdCategory( ) != 0 && announce.getCategory( ).getId( ) != filter.getIdCategory( ) )
        {
            return false;
        }

        // Sector check
        if ( filter.getIdSector( ) != 0 && announce.getCategory( ).getIdSector( ) != filter.getIdSector( ) )
        {
            return false;
        }

        // Keywords check (at least one keyword must appear in title or description)
        if ( StringUtils.isNotBlank( filter.getKeywords( ) ) )
        {
            String strTitle = StringUtils.defaultString( announce.getTitle( ) ).toLowerCase( );
            String strDescription = StringUtils.defaultString( announce.getDescription( ) ).toLowerCase( );
            String [ ] keywords = filter.getKeywords( ).toLowerCase( ).split( "\\s+" );
            boolean bKeywordFound = false;

            for ( String keyword : keywords )
            {
                if ( StringUtils.isNotBlank( keyword ) && ( strTitle.contains( keyword ) || strDescription.contains( keyword ) ) )
                {
                    bKeywordFound = true;
                    break;
                }
            }

            if ( !bKeywordFound )
            {
                return false;
            }
        }

        // Date min check
        Date dateMin = filter.getDateMin( );
        if ( dateMin != null && announce.getTimePublication( ) < dateMin.getTime( ) )
        {
            return false;
        }

        // Date max check
        Date dateMax = filter.getDateMax( );
        if ( dateMax != null && announce.getTimePublication( ) > dateMax.getTime( ) )
        {
            return false;
        }

        // Price min check
        if ( filter.getPriceMin( ) != 0 && announce.getPrice( ) != null && announce.getPrice( ) < filter.getPriceMin( ) )
        {
            return false;
        }

        // Price max check
        if ( filter.getPriceMax( ) != 0 && announce.getPrice( ) != null && announce.getPrice( ) > filter.getPriceMax( ) )
        {
            return false;
        }

        return true;
    }
}

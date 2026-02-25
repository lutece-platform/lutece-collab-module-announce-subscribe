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
package fr.paris.lutece.plugins.announce.modules.subscribe.service.daemon;

import fr.paris.lutece.plugins.announce.business.*;
import fr.paris.lutece.plugins.announce.modules.subscribe.business.AnnounceSubscriptionDTO;
import fr.paris.lutece.plugins.announce.modules.subscribe.service.AnnounceSubscriptionProvider;
import fr.paris.lutece.portal.service.daemon.Daemon;
import fr.paris.lutece.portal.service.mail.MailService;
import fr.paris.lutece.portal.service.security.LuteceUser;
import fr.paris.lutece.portal.service.security.LuteceUserService;
import fr.paris.lutece.portal.service.template.AppTemplateService;
import fr.paris.lutece.portal.service.util.AppLogService;
import fr.paris.lutece.portal.service.util.AppPathService;
import fr.paris.lutece.portal.service.i18n.I18nService;
import fr.paris.lutece.portal.service.util.AppPropertiesService;
import fr.paris.lutece.util.html.HtmlTemplate;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

/**
 * Daemon to send notification to users when subscribed announces are created. Notifications are grouped by user: each subscriber receives a single email
 * containing all matching announces, batched by a configurable maximum.
 */
public class AnnounceSubscriptionDaemon extends Daemon
{
    private static final String MARK_ANNOUNCES_LIST = "announces_list";
    private static final String MARK_BASE_URL = "base_url";
    private static final String I18N_SUBSCRIPTION_NOTIFICATION_SUBJECT = "module.announce.subscribe.email.subscription.subject";
    private static final String TEMPLATE_EMAIL_ANNOUNCES = "skin/plugins/announce/email_notify_announces.html";

    public static final String MARK_PROD_URL = "prod_url";
    public static final String MARK_URL_SUBSCRIPTION = "url_subscription";
    public static final String MARK_QUERY_SPECIFIC_ANNOUNCE = "page=announce&action=view_announce&announce_id=";
    public static final String MARK_QUERY_URL_SUBSCRIPTION = "page=announce&amp;action=view_subscriptions";

    private static final String PROPERTY_PROD_URL = "lutece.prod.url";
    private static final String PROPERTY_MAX_ANNOUNCES_PER_EMAIL = "announce.subscription.notification.maxAnnouncesPerEmail";
    private static final int DEFAULT_MAX_ANNOUNCES_PER_EMAIL = 10;
    private static final int MIN_ANNOUNCES_PER_EMAIL = 1;
    private static final int MAX_ANNOUNCES_PER_EMAIL = 20;

    /**
     * {@inheritDoc}
     */
    @Override
    public void run( )
    {
        List<AnnounceNotify> listAnnounceToNotify = AnnounceNotifyHome.selectAll( );
        if ( listAnnounceToNotify == null || listAnnounceToNotify.isEmpty( ) )
        {
            return;
        }

        AnnounceSubscriptionProvider provider = AnnounceSubscriptionProvider.getService( );

        // 1. Build map: userId -> List<Announce>
        Map<String, List<Announce>> mapUserAnnounces = new LinkedHashMap<>( );

        for ( AnnounceNotify announceNotify : listAnnounceToNotify )
        {
            Announce announce = AnnounceHome.findByPrimaryKey( announceNotify.getIdAnnounce( ) );
            if ( announce == null )
            {
                AnnounceNotifyHome.delete( announceNotify.getId( ) );
                continue;
            }

            String strAuthorUserId = announce.getUserName( );

            // Collect all subscriber userIds for this announce
            Set<String> userIds = new LinkedHashSet<>( );
            Stream.of( provider.getSubscriptionsByCategory( announce.getCategory( ).getId( ) ), provider.getSubscriptionsByUser( announce.getUserName( ) ),
                    provider.getMatchingFilterSubscriptions( announce ) ).flatMap( Collection::stream ).filter( s -> StringUtils.isNotBlank( s.getUserId( ) ) )
                    .filter( s -> !s.getUserId( ).equals( strAuthorUserId ) ).forEach( s -> userIds.add( s.getUserId( ) ) );

            // Add this announce to each subscriber's list
            for ( String strUserId : userIds )
            {
                mapUserAnnounces.computeIfAbsent( strUserId, k -> new ArrayList<>( ) ).add( announce );
            }

            AnnounceNotifyHome.delete( announceNotify.getId( ) );
        }

        // 2. Send emails per user, batched by max announces per email
        int nMaxPerEmail = AppPropertiesService.getPropertyInt( PROPERTY_MAX_ANNOUNCES_PER_EMAIL, DEFAULT_MAX_ANNOUNCES_PER_EMAIL );
        nMaxPerEmail = Math.max( MIN_ANNOUNCES_PER_EMAIL, Math.min( nMaxPerEmail, MAX_ANNOUNCES_PER_EMAIL ) );
        String strSubject = I18nService.getLocalizedString( I18N_SUBSCRIPTION_NOTIFICATION_SUBJECT, I18nService.getDefaultLocale( ) );
        String strSenderEmail = MailService.getNoReplyEmail( );

        for ( Map.Entry<String, List<Announce>> entry : mapUserAnnounces.entrySet( ) )
        {
            String strEmail = resolveEmail( entry.getKey( ) );
            if ( StringUtils.isNotBlank( strEmail ) )
            {
                List<Announce> allAnnounces = entry.getValue( );
                // Split into batches of nMaxPerEmail
                for ( int i = 0; i < allAnnounces.size( ); i += nMaxPerEmail )
                {
                    List<Announce> batch = allAnnounces.subList( i, Math.min( i + nMaxPerEmail, allAnnounces.size( ) ) );
                    notifyUser( batch, strSubject, strSenderEmail, strSenderEmail, strEmail );
                }
            }
        }
    }

    /**
     * Resolve the email address for a given user ID via LuteceUserService.
     *
     * @param strUserId
     *            The user ID (login) to resolve
     * @return The email address, or null if the user cannot be found
     */
    private String resolveEmail( String strUserId )
    {
        LuteceUser user = LuteceUserService.getLuteceUserFromName( strUserId );

        if ( user == null )
        {
            AppLogService.info( "AnnounceSubscriptionDaemon: could not resolve user '{}', skipping notification", strUserId );
            return null;
        }

        String strEmail = user.getUserInfo( LuteceUser.BUSINESS_INFO_ONLINE_EMAIL );

        if ( StringUtils.isBlank( strEmail ) )
        {
            strEmail = user.getEmail( );
        }

        return strEmail;
    }

    /**
     * Notify a user that announces they have subscribed to have been published
     *
     * @param listAnnounces
     *            The list of published announces to include in the email
     * @param strSubject
     *            The subject of the email to send
     * @param strSenderName
     *            The name of the sender of the email
     * @param strSenderEmail
     *            The email address of the sender of the email
     * @param strUserEmail
     *            The email address of the user to notify
     */
    private void notifyUser( List<Announce> listAnnounces, String strSubject, String strSenderName, String strSenderEmail, String strUserEmail )
    {
        String strProdUrl = AppPropertiesService.getProperty( PROPERTY_PROD_URL );

        if ( StringUtils.isBlank( strProdUrl ) )
        {
            AppLogService.error( "AnnounceSubscriptionDaemon: property 'lutece.prod.url' is not set, email links will be broken" );
            strProdUrl = "";
        }

        if ( !strProdUrl.endsWith( "/" ) && !strProdUrl.isEmpty( ) )
        {
            strProdUrl += "/";
        }

        String strBaseUrl = strProdUrl + AppPathService.getPortalUrl( ) + "?";

        List<Map<String, Object>> announceItems = new ArrayList<>( );
        for ( Announce announce : listAnnounces )
        {
            Map<String, Object> item = new HashMap<>( );
            item.put( "announce", announce );
            item.put( "category", CategoryHome.findByPrimaryKey( announce.getCategory( ).getId( ) ) );
            announceItems.add( item );
        }

        Map<String, Object> model = new HashMap<>( );
        model.put( MARK_ANNOUNCES_LIST, announceItems );
        model.put( MARK_BASE_URL, strProdUrl );
        model.put( MARK_PROD_URL, strBaseUrl + MARK_QUERY_SPECIFIC_ANNOUNCE );
        model.put( MARK_URL_SUBSCRIPTION, strBaseUrl + MARK_QUERY_URL_SUBSCRIPTION );

        HtmlTemplate template = AppTemplateService.getTemplate( TEMPLATE_EMAIL_ANNOUNCES, I18nService.getDefaultLocale( ), model );

        if ( StringUtils.isNotBlank( strSenderEmail ) && StringUtils.isNotBlank( strUserEmail ) )
        {
            MailService.sendMailHtml( null, null, strUserEmail, strSenderName, strSenderEmail, strSubject, template.getHtml( ) );
        }
    }
}

/*
 * Copyright (c) 2002-2015, Mairie de Paris
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
package fr.paris.lutece.plugins.notifygru.modules.appointment;

import fr.paris.lutece.plugins.appointment.business.Appointment;
import fr.paris.lutece.plugins.appointment.business.AppointmentForm;
import fr.paris.lutece.plugins.appointment.business.AppointmentFormHome;
import fr.paris.lutece.plugins.appointment.business.AppointmentHome;
import fr.paris.lutece.plugins.genericattributes.business.Entry;
import fr.paris.lutece.plugins.genericattributes.business.EntryFilter;
import fr.paris.lutece.plugins.genericattributes.business.EntryHome;
import fr.paris.lutece.plugins.genericattributes.business.Response;
import fr.paris.lutece.plugins.workflow.modules.notifygru.service.AbstractServiceProvider;
import fr.paris.lutece.plugins.workflow.modules.notifygru.utils.constants.Constants;
import fr.paris.lutece.plugins.workflowcore.business.resource.ResourceHistory;
import fr.paris.lutece.plugins.workflowcore.service.resource.IResourceHistoryService;
import fr.paris.lutece.portal.service.template.AppTemplateService;
import fr.paris.lutece.util.html.HtmlTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.inject.Inject;



/**
 * The Class NotifyGruAppointment.
 */
public class NotifyGruAppointment extends AbstractServiceProvider
{
    
    /** The Constant MARK_LIST_RESPONSE. */
    // MARKS   
    private static final String MARK_LIST_RESPONSE = "listEntry";
    
    /** The Constant MARK_FIRSTNAME. */
    private static final String MARK_FIRSTNAME = "firstName";
    
    /** The Constant MARK_LASTNAME. */
    private static final String MARK_LASTNAME = "lastName";
    
    /** The Constant MARK_EMAIL. */
    private static final String MARK_EMAIL = "email";
    
    /** The Constant MARK_ENTRY_BASE. */
    private static final String MARK_ENTRY_BASE = "reponse_";
    
    /** The Constant TEMPLATE_INFOS_HELP. */
    private static final String TEMPLATE_INFOS_HELP = "admin/plugins/workflow/modules/notifygru/appointment/freemarker_list.html";

    /** The _resource history service. */
    // SERVICES
    @Inject
    private IResourceHistoryService _resourceHistoryService;
    
    /** The _nid form appointment. */
    private int _nidFormAppointment;
    
    /** The _n order phone number. */
    private int _nOrderPhoneNumber;

    /* (non-Javadoc)
     * @see fr.paris.lutece.plugins.workflow.modules.notifygru.service.IProvider#getUserEmail(int)
     */
    @Override
    public String getUserEmail( int nIdResourceHistory )
    {
        String strEmail = null;
        ResourceHistory resourceHistory = _resourceHistoryService.findByPrimaryKey( nIdResourceHistory );
        Appointment appointment = AppointmentHome.findByPrimaryKey( resourceHistory.getIdResource(  ) );
        strEmail = appointment.getEmail(  );

        return strEmail;
    }

    /* (non-Javadoc)
     * @see fr.paris.lutece.plugins.workflow.modules.notifygru.service.IProvider#getUserGuid(int)
     */
    @Override
    public String getUserGuid( int nIdResourceHistory )
    {
        String strIdUser = "";
        ResourceHistory resourceHistory = _resourceHistoryService.findByPrimaryKey( nIdResourceHistory );
        Appointment appointment = AppointmentHome.findByPrimaryKey( resourceHistory.getIdResource(  ) );
        strIdUser = appointment.getIdUser(  );

        return strIdUser;
    }

    /* (non-Javadoc)
     * @see fr.paris.lutece.plugins.workflow.modules.notifygru.service.IProvider#getOptionalMobilePhoneNumber(int)
     */
    @Override
    public String getOptionalMobilePhoneNumber( int nIdResourceHistory )
    {
        String strPhoneNumber = null;
        ResourceHistory resourceHistory = _resourceHistoryService.findByPrimaryKey( nIdResourceHistory );
        Appointment appointment = AppointmentHome.findByPrimaryKey( resourceHistory.getIdResource(  ) );

        List<Response> listResponses = AppointmentHome.findListResponse( appointment.getIdAppointment(  ) );

        for ( Response response : listResponses )
        {
            Entry entry = EntryHome.findByPrimaryKey( response.getEntry(  ).getIdEntry(  ) );

            if ( entry.getPosition(  ) == getOrderPhoneNumber(  ) )
            {
                strPhoneNumber = response.getResponseValue(  );
            }
        }

        return strPhoneNumber;
    }

    /* (non-Javadoc)
     * @see fr.paris.lutece.plugins.workflow.modules.notifygru.service.IProvider#getInfosHelp(java.util.Locale)
     */
    @Override
    public String getInfosHelp( Locale local )
    {
        Map<String, Object> model = new HashMap<String, Object>(  );
        AppointmentForm formAppointment = AppointmentFormHome.findByPrimaryKey( _nidFormAppointment );

        EntryFilter entryFilter = new EntryFilter(  );
        entryFilter.setIdResource( formAppointment.getIdForm(  ) );
        entryFilter.setResourceType( AppointmentForm.RESOURCE_TYPE );
        entryFilter.setEntryParentNull( EntryFilter.FILTER_TRUE );
        entryFilter.setFieldDependNull( EntryFilter.FILTER_TRUE );

        List<Entry> listEntry = EntryHome.getEntryList( entryFilter );

        model.put( MARK_LIST_RESPONSE, listEntry );

        HtmlTemplate t = AppTemplateService.getTemplateFromStringFtl( AppTemplateService.getTemplate( 
                    TEMPLATE_INFOS_HELP, local, model ).getHtml(  ), local, model );

        String strResourceInfo = t.getHtml(  );

        return strResourceInfo;
    }

    /* (non-Javadoc)
     * @see fr.paris.lutece.plugins.workflow.modules.notifygru.service.IProvider#getInfos(int)
     */
    @Override
    public Map<String, Object> getInfos( int nIdResourceHistory )
    {
        Map<String, Object> model = new HashMap<String, Object>(  );

        if ( nIdResourceHistory > 0 )
        {
            ResourceHistory resourceHistory = _resourceHistoryService.findByPrimaryKey( nIdResourceHistory );
            Appointment appointment = AppointmentHome.findByPrimaryKey( resourceHistory.getIdResource(  ) );

            model.put( MARK_FIRSTNAME, appointment.getFirstName(  ) );
            model.put( MARK_LASTNAME, appointment.getLastName(  ) );
            model.put( MARK_EMAIL, appointment.getEmail(  ) );

            List<Response> listResponses = AppointmentHome.findListResponse( appointment.getIdAppointment(  ) );

            for ( Response response : listResponses )
            {
                Entry entry = EntryHome.findByPrimaryKey( response.getEntry(  ).getIdEntry(  ) );
                model.put( MARK_ENTRY_BASE + entry.getPosition(  ), response.getResponseValue(  ) );
            }
        }
        else
        {
            model.put( MARK_FIRSTNAME, "" );
            model.put( MARK_LASTNAME, "" );
            model.put( MARK_EMAIL, "" );

            AppointmentForm formAppointment = AppointmentFormHome.findByPrimaryKey( _nidFormAppointment );
            EntryFilter entryFilter = new EntryFilter(  );
            entryFilter.setIdResource( formAppointment.getIdForm(  ) );
            entryFilter.setResourceType( AppointmentForm.RESOURCE_TYPE );
            entryFilter.setEntryParentNull( EntryFilter.FILTER_TRUE );
            entryFilter.setFieldDependNull( EntryFilter.FILTER_TRUE );

            List<Entry> listEntry = EntryHome.getEntryList( entryFilter );

            for ( Entry entry : listEntry )
            {
                model.put( MARK_ENTRY_BASE + entry.getPosition(  ), "" );
            }
        }

        return model;
    }

    /**
     * Gets the id form appointment.
     *
     * @return the id form appointment
     */
    public int getIdFormAppointment(  )
    {
        return _nidFormAppointment;
    }

    /**
     * Sets the id form appointment.
     *
     * @param nIdFormAppointment the new id form appointment
     */
    public void setIdFormAppointment( int nIdFormAppointment )
    {
        this._nidFormAppointment = nIdFormAppointment;
    }

    /* (non-Javadoc)
     * @see fr.paris.lutece.plugins.workflow.modules.notifygru.service.IProvider#getOptionalDemandId(int)
     */
    @Override
    public int getOptionalDemandId( int nIdResourceHistory )
    {
        ResourceHistory resourceHistory = _resourceHistoryService.findByPrimaryKey( nIdResourceHistory );
        Appointment appointment = AppointmentHome.findByPrimaryKey( resourceHistory.getIdResource(  ) );

        return appointment.getIdAppointment(  );
    }

    /* (non-Javadoc)
     * @see fr.paris.lutece.plugins.workflow.modules.notifygru.service.IProvider#getOptionalDemandIdType(int)
     */
    @Override
    public int getOptionalDemandIdType( int nIdResourceHistory )
    {
        return Constants.OPTIONAL_INT_VALUE;
    }

    /**
     * Gets the appointment.
     *
     * @param nIdResourceHistory the n id resource history
     * @return the appointment
     */
    public Appointment getAppointment( int nIdResourceHistory )
    {
        ResourceHistory resourceHistory = _resourceHistoryService.findByPrimaryKey( nIdResourceHistory );
        Appointment appointment = AppointmentHome.findByPrimaryKey( resourceHistory.getIdResource(  ) );

        return appointment;
    }

    /**
     * Gets the order phone number.
     *
     * @return the order phone number
     */
    public int getOrderPhoneNumber(  )
    {
        return _nOrderPhoneNumber;
    }

    /**
     * Sets the order phone number.
     *
     * @param nOrderPhoneNumber the new order phone number
     */
    public void setOrderPhoneNumber( int nOrderPhoneNumber )
    {
        _nOrderPhoneNumber = nOrderPhoneNumber;
    }

    @Override
    public String getDemandReference( int nIdResourceHistory ) 
    {
         return "Nothing";
    }

    @Override
    public String getCustomerId( int nIdResourceHistory ) 
    {
       
        return "Nothing";
    }
}

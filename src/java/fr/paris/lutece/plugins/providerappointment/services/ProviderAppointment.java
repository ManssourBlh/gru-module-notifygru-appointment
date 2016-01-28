package fr.paris.lutece.plugins.providerappointment.services;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.inject.Inject;

import org.apache.commons.lang.StringUtils;

import fr.paris.lutece.plugins.appointment.business.Appointment;
import fr.paris.lutece.plugins.appointment.business.AppointmentForm;
import fr.paris.lutece.plugins.appointment.business.AppointmentHome;
import fr.paris.lutece.plugins.appointment.business.calendar.AppointmentSlot;
import fr.paris.lutece.plugins.appointment.business.calendar.AppointmentSlotHome;
import fr.paris.lutece.plugins.appointment.service.AppointmentService;
import fr.paris.lutece.plugins.appointment.service.entrytype.EntryTypePhone;
import fr.paris.lutece.plugins.genericattributes.business.Entry;
import fr.paris.lutece.plugins.genericattributes.business.EntryFilter;
import fr.paris.lutece.plugins.genericattributes.business.EntryHome;
import fr.paris.lutece.plugins.genericattributes.business.Response;
import fr.paris.lutece.plugins.genericattributes.business.ResponseHome;
import fr.paris.lutece.plugins.genericattributes.service.entrytype.EntryTypeServiceManager;
import fr.paris.lutece.plugins.genericattributes.service.entrytype.IEntryTypeService;
import fr.paris.lutece.plugins.workflow.modules.notifygru.service.AbstractServiceProvider;
import fr.paris.lutece.plugins.workflow.modules.notifygru.utils.constants.Constants;
import fr.paris.lutece.plugins.workflowcore.business.resource.ResourceHistory;
import fr.paris.lutece.plugins.workflowcore.service.resource.IResourceHistoryService;
import fr.paris.lutece.portal.service.template.AppTemplateService;
import fr.paris.lutece.util.html.HtmlTemplate;

public class ProviderAppointment extends AbstractServiceProvider {

	// MARKS
    private static final String MARK_MESSAGE = "message";
    private static final String MARK_LIST_RESPONSE = "listResponse";
    private static final String MARK_FIRSTNAME = "firstName";
    private static final String MARK_LASTNAME = "lastName";
    private static final String MARK_EMAIL = "email";
    private static final String MARK_REFERENCE = "reference";
    private static final String MARK_DATE_APPOINTMENT = "date_appointment";
    private static final String MARK_TIME_APPOINTMENT = "time_appointment";
    private static final String MARK_RECAP = "recap";
    
	private static final String TEMPLATE_INFOS_HELP = "admin/plugins/providerappointment/infos_help.html";
	
	private static final String MESSAGE_LABEL_STATUS_RESERVED = "appointment.message.labelStatusReserved";
    private static final String MESSAGE_LABEL_STATUS_UNRESERVED = "appointment.message.labelStatusUnreserved";
	// SERVICES
    @Inject
    private IResourceHistoryService _resourceHistoryService;
    
    private int _nIdAppointment;

	@Override
	public String getUserEmail(int nIdResource) 
	{
		String strEmail = null;
		ResourceHistory resourceHistory = _resourceHistoryService.findByPrimaryKey( nIdResource );
		Appointment appointment = AppointmentHome.findByPrimaryKey( resourceHistory.getIdResource(  ) );
		strEmail = appointment.getEmail(  );
		
		return strEmail;
	}

	@Override
	public String getUserGuid(int nIdResource) 
	{
		String strIdUser = "";
		ResourceHistory resourceHistory = _resourceHistoryService.findByPrimaryKey( nIdResource );
		Appointment appointment = AppointmentHome.findByPrimaryKey( resourceHistory.getIdResource(  ) );
		strIdUser = appointment.getIdUser();
		
		return strIdUser;
	}

	@Override
	public String getOptionalMobilePhoneNumber(int nIdResource) 
	{
		String strPhoneNumber = null;
		ResourceHistory resourceHistory = _resourceHistoryService.findByPrimaryKey( nIdResource );
		Appointment appointment = AppointmentHome.findByPrimaryKey(resourceHistory.getIdResource(  ));
        AppointmentSlot slot = AppointmentSlotHome.findByPrimaryKey( appointment.getIdSlot(  ) );
        EntryFilter entryFilter = new EntryFilter(  );
        entryFilter.setIdResource( slot.getIdForm(  ) );
        entryFilter.setResourceType( AppointmentForm.RESOURCE_TYPE );
        entryFilter.setFieldDependNull( EntryFilter.FILTER_TRUE );

        List<Integer> listIdResponse = AppointmentHome.findListIdResponse( appointment.getIdAppointment(  ) );

        List<Response> listResponses = new ArrayList<Response>( listIdResponse.size(  ) );

        for ( int nIdResponse : listIdResponse )
        {
            listResponses.add( ResponseHome.findByPrimaryKey( nIdResponse ) );
        }

        List<Entry> listEntries = EntryHome.getEntryList( entryFilter );

        for ( Entry entry : listEntries )
        {
            IEntryTypeService entryTypeService = EntryTypeServiceManager.getEntryTypeService( entry );

            if ( entryTypeService instanceof EntryTypePhone )
            {
                for ( Response response : listResponses )
                {
                    if ( ( response.getEntry(  ).getIdEntry(  ) == entry.getIdEntry(  ) ) &&
                            StringUtils.isNotBlank( response.getResponseValue(  ) ) )
                    {
                        strPhoneNumber = response.getResponseValue(  );
                        break;
                    }
                }

                if ( StringUtils.isNotEmpty( strPhoneNumber ) )
                {
                    break;
                }
            }
        }
        
		return strPhoneNumber;
	}

	@Override
	public String getInfosHelp(Locale local) {
		Map<String, Object> model = new HashMap<>();
		List<Response> listResponses = getListResponse(_nIdAppointment);
        model.put(MARK_LIST_RESPONSE, listResponses);
        HtmlTemplate t = AppTemplateService.getTemplateFromStringFtl( AppTemplateService.getTemplate( 
        		TEMPLATE_INFOS_HELP, local, model ).getHtml(  ), local, model );
		//HtmlTemplate t = AppTemplateService.getTemplate( TEMPLATE_INFOS_HELP, local, model );

        String strResourceInfo = t.getHtml();

        return strResourceInfo;
	}

	@Override
	public Map<String, Object> getInfos(int nIdResource) 
	{
		Map<String, Object> model = new HashMap<String, Object>(  );
		
		if(nIdResource>0)
		{
			Appointment appointment=getAppointment(nIdResource);
	        AppointmentSlot slot = AppointmentSlotHome.findByPrimaryKey( appointment.getIdSlot(  ) );
	        List<Response> listResponses = getListResponse(nIdResource);
	        
	        for(Response response: listResponses)
	        {
	        	model.put(response.getEntry().getTitle(), response.getResponseValue());
	        }
	        model.put( MARK_FIRSTNAME, appointment.getFirstName(  ) );
	        model.put( MARK_LASTNAME, appointment.getLastName(  ) );
	        model.put( MARK_EMAIL, appointment.getEmail(  ) );
	        model.put( MARK_REFERENCE, AppointmentService.getService(  ).computeRefAppointment( appointment ) );
	        model.put( MARK_DATE_APPOINTMENT, appointment.getDateAppointment(  ) );

	        String strStartingTime = AppointmentService.getService(  )
	                                                   .getFormatedStringTime( slot.getStartingHour(  ), slot.getStartingMinute(  ) );
	        model.put( MARK_TIME_APPOINTMENT, strStartingTime );
		}
		else
		{
			List<Response> listResponses = getListResponse(_nIdAppointment);
	        for(Response response: listResponses)
	        {
	        	model.put(response.getEntry().getTitle(),"");
	        }
			model.put( MARK_FIRSTNAME, "" );
	        model.put( MARK_LASTNAME, "" );
	        model.put( MARK_EMAIL, "" );
		}
		
        
        return model;
	}

	/**
	 * @return the _nIdAppointment
	 */
	public int getIdAppointment() {
		return _nIdAppointment;
	}

	/**
	 * @param _nIdAppointment the _nIdAppointment to set
	 */
	public void setIdAppointment(int nIdAppointment) {
		this._nIdAppointment = nIdAppointment;
	}

	@Override
	public int getOptionalDemandId(int nIdResource) {
		ResourceHistory resourceHistory = _resourceHistoryService.findByPrimaryKey( nIdResource );
		Appointment appointment = AppointmentHome.findByPrimaryKey(resourceHistory.getIdResource(  ));
		return appointment.getIdAppointment();
	}

	@Override
	public int getOptionalDemandIdType(int nIdResource) {
		return Constants.OPTIONAL_INT_VALUE;
	}
	/**
	 * 
	 * @param nIdResource
	 * @return
	 */
	public Appointment getAppointment(int nIdResource)
	{
		ResourceHistory resourceHistory = _resourceHistoryService.findByPrimaryKey( nIdResource );
		Appointment appointment = AppointmentHome.findByPrimaryKey(resourceHistory.getIdResource(  ));
		return appointment;
	}
	/**
	 * 
	 * @param nIdResponse
	 * @return
	 */
	public List<Response> getListResponse(int nIdResponse){
		List<Integer> listIdResponse = AppointmentHome.findListIdResponse( nIdResponse );

        List<Response> listResponses = new ArrayList<Response>( listIdResponse.size(  ) );
        for ( int IdResponse : listIdResponse )
        {
            listResponses.add( ResponseHome.findByPrimaryKey( IdResponse ) );
        }
		return listResponses;
	}
}


/**
 * MeridianMessageEnquiryWebServiceCallbackHandler.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis2 version: 1.6.2  Built on : Apr 17, 2012 (05:33:49 IST)
 */

    package com.misys.meridian.webservices.SEI;

    /**
     *  MeridianMessageEnquiryWebServiceCallbackHandler Callback class, Users can extend this class and implement
     *  their own receiveResult and receiveError methods.
     */
    public abstract class MeridianMessageEnquiryWebServiceCallbackHandler{



    protected Object clientData;

    /**
    * User can pass in any object that needs to be accessed once the NonBlocking
    * Web service call is finished and appropriate method of this CallBack is called.
    * @param clientData Object mechanism by which the user can pass in user data
    * that will be avilable at the time this callback is called.
    */
    public MeridianMessageEnquiryWebServiceCallbackHandler(Object clientData){
        this.clientData = clientData;
    }

    /**
    * Please use this constructor if you don't want to set any clientData
    */
    public MeridianMessageEnquiryWebServiceCallbackHandler(){
        this.clientData = null;
    }

    /**
     * Get the client data
     */

     public Object getClientData() {
        return clientData;
     }

        
           /**
            * auto generated Axis2 call back method for lookupMessage method
            * override this method for handling normal response from lookupMessage operation
            */
           public void receiveResultlookupMessage(
                    com.misys.meridian.webservices.SEI.MeridianMessageEnquiryWebServiceStub.LookupMessageResponseE result
                        ) {
           }

          /**
           * auto generated Axis2 Error handler
           * override this method for handling error response from lookupMessage operation
           */
            public void receiveErrorlookupMessage(java.lang.Exception e) {
            }
                


    }
    
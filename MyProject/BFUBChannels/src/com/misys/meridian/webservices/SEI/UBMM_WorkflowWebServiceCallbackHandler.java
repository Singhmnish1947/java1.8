
/**
 * UBMM_WorkflowWebServiceCallbackHandler.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis2 version: 1.6.2  Built on : Apr 17, 2012 (05:33:49 IST)
 */

    package com.misys.meridian.webservices.SEI;

    /**
     *  UBMM_WorkflowWebServiceCallbackHandler Callback class, Users can extend this class and implement
     *  their own receiveResult and receiveError methods.
     */
    public abstract class UBMM_WorkflowWebServiceCallbackHandler{



    protected Object clientData;

    /**
    * User can pass in any object that needs to be accessed once the NonBlocking
    * Web service call is finished and appropriate method of this CallBack is called.
    * @param clientData Object mechanism by which the user can pass in user data
    * that will be avilable at the time this callback is called.
    */
    public UBMM_WorkflowWebServiceCallbackHandler(Object clientData){
        this.clientData = clientData;
    }

    /**
    * Please use this constructor if you don't want to set any clientData
    */
    public UBMM_WorkflowWebServiceCallbackHandler(){
        this.clientData = null;
    }

    /**
     * Get the client data
     */

     public Object getClientData() {
        return clientData;
     }

        
           /**
            * auto generated Axis2 call back method for calculateLAUByLAUKeys method
            * override this method for handling normal response from calculateLAUByLAUKeys operation
            */
           public void receiveResultcalculateLAUByLAUKeys(
                    com.misys.meridian.webservices.SEI.UBMM_WorkflowWebServiceStub.CalculateLAUByLAUKeysResponse result
                        ) {
           }

          /**
           * auto generated Axis2 Error handler
           * override this method for handling error response from calculateLAUByLAUKeys operation
           */
            public void receiveErrorcalculateLAUByLAUKeys(java.lang.Exception e) {
            }
                
           /**
            * auto generated Axis2 call back method for calculateLAU method
            * override this method for handling normal response from calculateLAU operation
            */
           public void receiveResultcalculateLAU(
                    com.misys.meridian.webservices.SEI.UBMM_WorkflowWebServiceStub.CalculateLAUResponse result
                        ) {
           }

          /**
           * auto generated Axis2 Error handler
           * override this method for handling error response from calculateLAU operation
           */
            public void receiveErrorcalculateLAU(java.lang.Exception e) {
            }
                
           /**
            * auto generated Axis2 call back method for validate method
            * override this method for handling normal response from validate operation
            */
           public void receiveResultvalidate(
                    com.misys.meridian.webservices.SEI.UBMM_WorkflowWebServiceStub.ValidateResponse result
                        ) {
           }

          /**
           * auto generated Axis2 Error handler
           * override this method for handling error response from validate operation
           */
            public void receiveErrorvalidate(java.lang.Exception e) {
            }
                
           /**
            * auto generated Axis2 call back method for lookupMessage method
            * override this method for handling normal response from lookupMessage operation
            */
           public void receiveResultlookupMessage(
                    com.misys.meridian.webservices.SEI.UBMM_WorkflowWebServiceStub.LookupMessageResponse result
                        ) {
           }

          /**
           * auto generated Axis2 Error handler
           * override this method for handling error response from lookupMessage operation
           */
            public void receiveErrorlookupMessage(java.lang.Exception e) {
            }
                
           /**
            * auto generated Axis2 call back method for calculateLAUWithHostIDAndNetwork method
            * override this method for handling normal response from calculateLAUWithHostIDAndNetwork operation
            */
           public void receiveResultcalculateLAUWithHostIDAndNetwork(
                    com.misys.meridian.webservices.SEI.UBMM_WorkflowWebServiceStub.CalculateLAUWithHostIDAndNetworkResponse result
                        ) {
           }

          /**
           * auto generated Axis2 Error handler
           * override this method for handling error response from calculateLAUWithHostIDAndNetwork operation
           */
            public void receiveErrorcalculateLAUWithHostIDAndNetwork(java.lang.Exception e) {
            }
                
           /**
            * auto generated Axis2 call back method for validateSWIFTMessage method
            * override this method for handling normal response from validateSWIFTMessage operation
            */
           public void receiveResultvalidateSWIFTMessage(
                    com.misys.meridian.webservices.SEI.UBMM_WorkflowWebServiceStub.ValidateSWIFTMessageResponse result
                        ) {
           }

          /**
           * auto generated Axis2 Error handler
           * override this method for handling error response from validateSWIFTMessage operation
           */
            public void receiveErrorvalidateSWIFTMessage(java.lang.Exception e) {
            }
                
           /**
            * auto generated Axis2 call back method for calculateLAUByReference method
            * override this method for handling normal response from calculateLAUByReference operation
            */
           public void receiveResultcalculateLAUByReference(
                    com.misys.meridian.webservices.SEI.UBMM_WorkflowWebServiceStub.CalculateLAUByReferenceResponse result
                        ) {
           }

          /**
           * auto generated Axis2 Error handler
           * override this method for handling error response from calculateLAUByReference operation
           */
            public void receiveErrorcalculateLAUByReference(java.lang.Exception e) {
            }
                


    }
    
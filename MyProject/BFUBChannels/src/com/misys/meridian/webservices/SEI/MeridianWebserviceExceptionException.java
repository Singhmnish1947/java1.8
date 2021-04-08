
/**
 * MeridianWebserviceExceptionException.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis2 version: 1.6.2  Built on : Apr 17, 2012 (05:33:49 IST)
 */

package com.misys.meridian.webservices.SEI;

public class MeridianWebserviceExceptionException extends java.lang.Exception{

    private static final long serialVersionUID = 1506501970668L;
    
    private com.misys.meridian.webservices.SEI.MeridianMessageEnquiryWebServiceStub.MeridianWebserviceExceptionE faultMessage;

    
        public MeridianWebserviceExceptionException() {
            super("MeridianWebserviceExceptionException");
        }

        public MeridianWebserviceExceptionException(java.lang.String s) {
           super(s);
        }

        public MeridianWebserviceExceptionException(java.lang.String s, java.lang.Throwable ex) {
          super(s, ex);
        }

        public MeridianWebserviceExceptionException(java.lang.Throwable cause) {
            super(cause);
        }
    

    public void setFaultMessage(com.misys.meridian.webservices.SEI.MeridianMessageEnquiryWebServiceStub.MeridianWebserviceExceptionE msg){
       faultMessage = msg;
    }
    
    public com.misys.meridian.webservices.SEI.MeridianMessageEnquiryWebServiceStub.MeridianWebserviceExceptionE getFaultMessage(){
       return faultMessage;
    }
}
    
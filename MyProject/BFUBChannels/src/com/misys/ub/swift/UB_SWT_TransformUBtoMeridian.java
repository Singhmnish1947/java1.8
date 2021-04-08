package com.misys.ub.swift;

import java.io.IOException;
import javax.xml.parsers.ParserConfigurationException;
import org.xml.sax.SAXException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;



public class UB_SWT_TransformUBtoMeridian {
	
	private transient final static Log LOGGER = LogFactory
			.getLog(UB_SWT_TransformUBtoMeridian.class.getName());

	
	public String executeFiles(String requestMsg) throws ClassNotFoundException, InstantiationException, IllegalAccessException, ClassCastException, SAXException, IOException, ParserConfigurationException {
		    String message = null;
		    if (LOGGER.isInfoEnabled()) {
				LOGGER.info(requestMsg);
			}
			if (requestMsg.contains("UB_MT350")) {
				UB_MT350_FileCreator ubMT350FileCreator = new UB_MT350_FileCreator();
				message = ubMT350FileCreator.MT350_Transform(requestMsg);
			} else if (requestMsg.contains("UB_MT103")) {
				UB_MT103_FileCreator ubMT103FileCreator = new UB_MT103_FileCreator();
				message = ubMT103FileCreator.MT103_Transform(requestMsg);
			}

			else if (requestMsg.contains("UB_MT940950")) {
				UB_MT940950_FileCreator ubMT940950FileCreator = new UB_MT940950_FileCreator();
				message = ubMT940950FileCreator.MT940950_Transform(requestMsg);
			} else if (requestMsg.contains("UB_MT900910")) {
				UB_MT900910_FileCreator ubMT900910FileCreator  = new UB_MT900910_FileCreator();
				message = ubMT900910FileCreator.MT900910_Transform(requestMsg);
			} else if (requestMsg.contains("UB_MT200")) {
				UB_MT200_FileCreator ubMT200FileCreator = new UB_MT200_FileCreator();
				message = ubMT200FileCreator.MT200_Transform(requestMsg);
			} else if (requestMsg.contains("UB_MT210")) {
				UB_MT210_FileCreator ubMT210FileCreator = new UB_MT210_FileCreator();
				message = ubMT210FileCreator.MT210_Transform(requestMsg);
			} else if (requestMsg.contains("UB_MT330")) {
				UB_MT330_FileCreator ubMT330FileCreator = new UB_MT330_FileCreator();
				message = ubMT330FileCreator.MT330_Transform(requestMsg);
			} else if (requestMsg.contains("UB_MT300")) {
				UB_MT300FileCreator ubMT300FileCreator = new UB_MT300FileCreator();
				message = ubMT300FileCreator.MT300_Transform(requestMsg);
			} else if (requestMsg.contains("UB_MT202")) {
				UB_MT202_FileCreator ubMT202FileCreator = new UB_MT202_FileCreator();
				message = ubMT202FileCreator.MT202_Transform(requestMsg);
			} else if (requestMsg.contains("UB_MT320")) {
				UB_MT320_FileCreator ubMT320FileCreator = new UB_MT320_FileCreator();
				message = ubMT320FileCreator.MT320_Transform(requestMsg);
			} else if (requestMsg.contains("UB_MT110")) {
				UB_MT110_FileCreator ubMT110FileCreator = new UB_MT110_FileCreator();
				message = ubMT110FileCreator.MT110_Transform(requestMsg);
			} else if (requestMsg.contains("UB_MT111")) {
				UB_MT111_FileCreator ubMT111FileCreator = new UB_MT111_FileCreator();
				message = ubMT111FileCreator.MT111_Transform(requestMsg);
			} else if (requestMsg.contains("UB_MT942")) {
				UB_MT942_FileCreator ubMT942FileCreator = new UB_MT942_FileCreator();
				message = ubMT942FileCreator.MT942_Transform(requestMsg);
			} else if (requestMsg.contains("UB_MT203")) {
				UB_MT203_FileCreator ubMT203FileCreator = new UB_MT203_FileCreator();
				message = ubMT203FileCreator.UB_MT203_Transform(requestMsg);
			} else if (requestMsg.contains("UB_MT205")) {
				UB_MT205_FileCreator ubMT205FileCreator = new UB_MT205_FileCreator();
				message = ubMT205FileCreator.MT205_Transform(requestMsg);
			} else if (requestMsg.contains("UB_MT201")) {
				UB_MT201_FileCreator ubMT201FileCreator = new UB_MT201_FileCreator();
				message = ubMT201FileCreator.UB_MT201_Transform(requestMsg);
			}else if (requestMsg.contains("UB_MT192")) {
				UB_MT192_FileCreator ubMT192FileCreator = new UB_MT192_FileCreator();
				message = ubMT192FileCreator.MT192_Transform(requestMsg);
			}else if (requestMsg.contains("UB_MT292")) {
				UB_MT292_FileCreator ubMT292FileCreator = new UB_MT292_FileCreator();
				message = ubMT292FileCreator.MT292_Transform(requestMsg);
			}else if (requestMsg.contains("UB_MT992")) {
				UB_MT992_FileCreator ubMT992FileCreator = new UB_MT992_FileCreator();
				message = ubMT992FileCreator.MT992_Transform(requestMsg);
			}

		return message;
	}


}

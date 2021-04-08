package com.trapedza.bankfusion.fatoms;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.HashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.misys.bankfusion.common.exception.ExceptionUtil;
import com.misys.ub.channels.events.ChannelsEventCodes;
import com.misys.ub.common.events.CommonsEventCodes;
import com.misys.ub.treasury.events.TreasuryEventCodes;
import com.trapedza.bankfusion.boundary.outward.BankFusionResourceSupport;
import com.trapedza.bankfusion.core.BankFusionException;
import com.trapedza.bankfusion.core.CommonConstants;
import com.trapedza.bankfusion.core.EventsHelper;
import com.trapedza.bankfusion.servercommon.commands.BankFusionEnvironment;
import com.trapedza.bankfusion.steps.refimpl.AbstractPOSCompletionFatom;
import com.trapedza.bankfusion.utils.BankFusionMessages;

public class POSCompletionFatom extends AbstractPOSCompletionFatom {

	/**
	 * <code>svnRevision</code> = $Revision: 1.0 $
	 */
	public static final String svnRevision = "$Revision: 1.0 $";
	static {
		com.trapedza.bankfusion.utils.Tracer.register(svnRevision);
	}

	private transient final static Log logger = LogFactory.getLog(POSCompletionFatom.class.getName());
	private static final String FILE_LOCATION = "business/atm/sparrow/POSCompletionFile/";

	public POSCompletionFatom(BankFusionEnvironment env) {
		super(env);
	}

	public void process(BankFusionEnvironment env) {
		String fileName = getF_IN_FileName();
		int portNumber = getF_IN_PortNumber().intValue();
		Socket socket = null;
		try {
			InputStream inStream = null;

			inStream = BankFusionResourceSupport.getResourceLoader().getInputStreamResource(FILE_LOCATION + fileName);
			DataInputStream din = new DataInputStream(inStream);
			boolean tocontinue = true;
			socket = new Socket("127.0.0.1", portNumber);
			PrintWriter toServer = new PrintWriter(socket.getOutputStream());
			String regMessage = "0000000000000000000000997000000000000000000000000000000000000000000";
			String deRegMessage = "0000000000000000000000930000000000000000000000000000000000000000000";
			toServer.print(regMessage + "\r\n");
			while (tocontinue) {
				String message = din.readLine();
				if (message == null) {
					return;
				}
				if (message.equals(CommonConstants.EMPTY_STRING)) {
					continue;
				}
				toServer.print(message + "\r\n");
				toServer.flush();
			}
			toServer.print(deRegMessage + "\r\n");
		}
		catch (NullPointerException exception) {
		 EventsHelper.handleEvent(ChannelsEventCodes.E_POS_Completion_File_Not_Found,new Object[]{} , new HashMap(), env);
		logger.error(ExceptionUtil.getExceptionAsString(exception));
		}
		catch (IOException exception) {
	
			 EventsHelper.handleEvent(ChannelsEventCodes.E_COULD_NOT_ESTABLISH_CONNECTION_WITH_THE_HOST,new Object[]{} , new HashMap(), env);
		logger.error(ExceptionUtil.getExceptionAsString(exception));
		}
		catch (Exception exception) {
			logger.error(ExceptionUtil.getExceptionAsString(exception));

		}
		finally{
			try {
				if(socket != null)
				socket.close();
			} catch (Exception e) {
				logger.error(ExceptionUtil.getExceptionAsString(e));
			}
		}
	}
}

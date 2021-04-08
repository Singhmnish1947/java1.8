package com.misys.ub.fatoms.batch.CardTechBalanceDownload;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.trapedza.bankfusion.atm.sparrow.helper.ATMHelper;
import com.trapedza.bankfusion.batch.fatom.AbstractFatomContext;
import com.trapedza.bankfusion.batch.process.AbstractProcessAccumulator;
import com.trapedza.bankfusion.batch.process.IBatchPostProcess;
import com.trapedza.bankfusion.batch.process.PostProcessException;
import com.trapedza.bankfusion.batch.process.engine.IBatchStatus;
import com.trapedza.bankfusion.core.SystemInformationManager;
import com.trapedza.bankfusion.servercommon.commands.BankFusionEnvironment;
import com.trapedza.bankfusion.servercommon.expression.builder.functions.ConvertToString;
public class CardTechBalanceDownloadPostProcess implements IBatchPostProcess {

	 /**
     * <code>svnRevision</code> = $Revision: 1.0 $
     */
    public static final String svnRevision = "$Revision: 1.0 $"; //$NON-NLS-1$
    static {
        com.trapedza.bankfusion.utils.Tracer.register(svnRevision);
    }

    private static final transient Log logger = LogFactory.getLog(CardTechBalanceDownloadPostProcess.class.getName());
    private Date todaysDate = null;
	private Date dateTimeTruncated = null;
    private int todaysDayOfYear = 0;
    private BankFusionEnvironment environment;
    private AbstractFatomContext context;
    private AbstractProcessAccumulator accumulator;
    private IBatchStatus status;
    private String cardTechFilePath = null;
	private ATMHelper atmHelper =  new ATMHelper();
    public void init(BankFusionEnvironment env, AbstractFatomContext ctx, IBatchStatus stats) throws PostProcessException {

        this.environment = env;
        this.context = ctx;
        this.status = stats;
    	Map inputValues = context.getInputTagDataMap();
    	cardTechFilePath = (String)inputValues.get("CARDTECHFILEPATH");
    }

    public IBatchStatus process(AbstractProcessAccumulator accumulator) throws PostProcessException {
        this.accumulator = accumulator;

        renameFile("online");
        status.setStatus(true);        
        
        return status;
    }
    private Date truncateTime(Date d) {
		GregorianCalendar gCal = new GregorianCalendar();
		gCal.setTime(d);
		gCal.set(Calendar.HOUR_OF_DAY,0);
		gCal.set(Calendar.MINUTE,0);
		gCal.set(Calendar.SECOND, 0);
		gCal.set(Calendar.MILLISECOND,0);
		return gCal.getTime();
	}
	private long dateDiff(Date todaysDate2, Date yearStartDate) {

		long dateDiff, DAY = 0;
		long daysDifference = 0;
		DAY = 86400000L; // 24L * 60L * 60L * 1000L; One day in milliseconds
		Calendar cal = Calendar.getInstance();
		cal.setLenient(true);
		cal.setTime(todaysDate2);
		cal.setTime(yearStartDate);		
		dateDiff =  todaysDate2.getTime()- yearStartDate.getTime();
		daysDifference = dateDiff/DAY;
		return (++daysDifference);
	}
    
    private void renameFile(String fileName)
    {
    	try{
    	Calendar cal = Calendar.getInstance();
		cal.setLenient(true);
		todaysDate = SystemInformationManager.getInstance().getBFBusinessDate();
		Calendar currYear = Calendar.getInstance(); 
		currYear.setTime(todaysDate);
		int year = currYear.get(Calendar.YEAR);
		cal.setTime(todaysDate);
		dateTimeTruncated = truncateTime(todaysDate);
		String currentYear= Integer.toString(year);
		String dateTime="01/01/"+currentYear;
	
		Date dtTmp = new SimpleDateFormat("MM/dd/yy").parse(dateTime);
		Date defaultTime=truncateTime(dtTmp);

		long noOfDays=dateDiff(dateTimeTruncated,defaultTime);

		int julDay = (int)noOfDays;
		
        String newFileName = cardTechFilePath + fileName;
        String julianDay = ConvertToString.run(new Integer(julDay));
        String convertedJulianDay = atmHelper.leftPad(julianDay, "0", 3);
        
		newFileName = newFileName + "-f." +convertedJulianDay;
        
		File input = new File(cardTechFilePath + fileName);
		File newFile = new File(newFileName);
		input.renameTo(newFile);
    	}
    	catch (Exception e1) {
						
		}
    }
}


/**
 * 
 */
package com.trapedza.bankfusion.fatoms;

import com.trapedza.bankfusion.servercommon.commands.BankFusionEnvironment;
import com.trapedza.bankfusion.steps.refimpl.AbstractUB_ALD_RatingAgency_AddRemove_Rows_Of_Vector;
import com.trapedza.bankfusion.steps.refimpl.IUB_ALD_RatingAgency_AddRemove_Rows_Of_Vector;
import com.trapedza.bankfusion.utils.BankFusionMessages;

import com.trapedza.bankfusion.core.BankFusionException;
import com.trapedza.bankfusion.core.CommonConstants;
import com.trapedza.bankfusion.core.VectorTable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @author sreesanth.tn
 *
 */
public class UB_ALD_RatingAgency_AddRemove_Rows extends
		AbstractUB_ALD_RatingAgency_AddRemove_Rows_Of_Vector implements
		IUB_ALD_RatingAgency_AddRemove_Rows_Of_Vector {

	/**
	 * <code>svnRevision</code> = $Revision: 1.0 $
	 */
	public static final String svnRevision = "$Revision: 1.0 $";
	static {
		com.trapedza.bankfusion.utils.Tracer.register(svnRevision);
	}



	private static final String ADD = "ADD";
	private static final String REMOVE = "REMOVE";
	private static final String RATINGTERM = "UBRATINGTERM";
	private static final String RATINGVALUE = "UBRATINGVALUE";
	private static final String SEQNO = "UBSEQUENCENO";
	private static final String AGENCYCODE= "UBRATINGCODE";
	private static final String SRNO = "SRNO";
	private static final String VER = "VERSIONNUM";
	private static final String SEL = "SELECT";
	private static final String BOID = "BOID";
	private static final String LT = "LT";
	private transient final static Log logger = LogFactory.getLog(UB_ALD_RatingAgency_AddRemove_Rows.class.getName());
	
	/**
	 * @param env
	 */
	public UB_ALD_RatingAgency_AddRemove_Rows(BankFusionEnvironment env) {
		super(env);
		// TODO Auto-generated constructor stub
	}
	public void process(BankFusionEnvironment environment) {
		
		VectorTable input = getF_IN_INPUT();
		String ratingTerm = getF_IN_RatingTerm();
		String ratingValue = getF_IN_RatingValue();
		String action = getF_IN_Action(); 
		if(action.equalsIgnoreCase(ADD)){
			Validate(ratingValue);
			CheckDuplication(ratingTerm, ratingValue, input);
			setF_OUT_OUTPUT(AddRow(ratingTerm,ratingValue,input));
		}
		else if(action.equalsIgnoreCase(REMOVE))
			setF_OUT_OUTPUT(RemoveRow(input));
		else
			ValidateAllRows(input);
		return;
	}

	private VectorTable AddRow(String term, String value, VectorTable vector){
		HashMap map = new HashMap();
		int seqNo = 0;
		boolean sel = false;
		if(vector.size()>0){		// If Vector Not empty
			Object terms[] = vector.getColumn(RATINGTERM);
			for(int i=0; i<terms.length; i++){
				if(terms[i].toString().equals(term))
					seqNo++;
			}
		}else
			sel = true;
		map.put(RATINGTERM, term);
		map.put(RATINGVALUE, value);
		map.put(SEQNO, seqNo+1);
		map.put(AGENCYCODE,CommonConstants.EMPTY_STRING);
		map.put(SRNO,vector.size()+1);
		map.put(VER,0);
		map.put(SEL,sel);
		map.put(BOID,CommonConstants.EMPTY_STRING);
		VectorTable n = new VectorTable(map);
		vector.addAll(n);
		return vector;
		
	}
	
	private VectorTable RemoveRow(VectorTable vector){
		if(vector.size()<=0)
			throw new BankFusionException(40413025,BankFusionMessages.getFormattedMessage(40413025, new Object[]{}));
		HashMap map = new HashMap();
		VectorTable modified = new VectorTable();
		int i=0;
		int index = vector.getSelectedRowIndex();
		map = vector.getRowTags(index);
		String selectedTerm=map.get(RATINGTERM).toString().trim();
		
		// Throw Error if Already Existing in DB
		if(map.get(BOID).toString().length()!=0)
			throw new BankFusionException(40413015,BankFusionMessages.getFormattedMessage(40413015, new Object[]{}));

		// Add all rows before deleted row to output 
		for(i=0; i<index; i++){
			map = vector.getRowTags(i);
			VectorTable temp = new VectorTable(map);
			modified.addAll(temp);
		}
		
		// Add all rows after deleted row to output
		for(i=index+1; i<vector.size(); i++){
			map = vector.getRowTags(i);
			map.put(SRNO,((Integer)map.get(SRNO)).intValue()-1);			// Decrement SRNO by 1
			if(map.get(RATINGTERM).toString().equals(selectedTerm))			// Decrement SEQNO if term = Selected Term
				map.put(SEQNO, ((Integer)map.get(SEQNO)).intValue()-1);
			VectorTable temp = new VectorTable(map);
			modified.addAll(temp);
		}
		if(modified.size()>0)
			modified.selectRow(0);
		return modified;
	}
	private void Validate(String value){
/*		String regExpr = "[a-zA-Z0-9]+";
		Pattern patternMatch = Pattern.compile(regExpr);
		Matcher matcher = patternMatch.matcher(value);*/
		if(value.length()==0)
			throw new BankFusionException(40413014,BankFusionMessages.getFormattedMessage(40413014, new Object[]{}));
		else if(value.length()>15)
			throw new BankFusionException(40413011,BankFusionMessages.getFormattedMessage(40413011, new Object[]{}));
/*		else if(!matcher.matches())
			throw new BankFusionException(40413012,new Object[] { value },logger, null);*/
	}
	
	private void ValidateAllRows(VectorTable vector){
		HashMap temp = new HashMap();
		ArrayList<String> shortTerm = new ArrayList<String>();
		ArrayList<String> longTerm = new ArrayList<String>();
		String term = CommonConstants.EMPTY_STRING;
		String value = CommonConstants.EMPTY_STRING;
		for(int i=0;i<vector.size();i++){
			temp = vector.getRowTags(i);
			term = temp.get(RATINGTERM).toString();
			value = temp.get(RATINGVALUE).toString();
			Validate(value);
			if(term.equalsIgnoreCase(LT)){
				if(longTerm.contains(value)){
					throw new BankFusionException(40413013,BankFusionMessages.getFormattedMessage(40413013, new Object[] { value, term }));
				}else
					longTerm.add(value);
			}else{
				if(shortTerm.contains(value)){
					throw new BankFusionException(40413013,BankFusionMessages.getFormattedMessage(40413013, new Object[] { value, term }));
				}else
					shortTerm.add(value);				
			}
		}
		
	}
	
	private void CheckDuplication(String term, String value,VectorTable vector){
		HashMap rec = new HashMap();
		for(int i=0; i<vector.size(); i++){
			rec = vector.getRowTags(i);
			if((rec.get(RATINGTERM).toString().equalsIgnoreCase(term)) && 
					(rec.get(RATINGVALUE).toString().equals(value)) ){
				throw new BankFusionException(40413013,BankFusionMessages.getFormattedMessage(40413013, new Object[] { value, term }));
			}
		}
	}
}

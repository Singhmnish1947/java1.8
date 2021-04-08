package com.finastra.fbe.atm.batch;

import java.util.Map;

import com.trapedza.bankfusion.batch.fatom.AbstractPersistableFatomContext;

public class OfflinePosCompletionContext extends AbstractPersistableFatomContext {

    private String batchProcessName;

    private static final String PROCESS_CLASSNAME =
        loadProcessClassName("OfflinePosCompletion", "com.finastra.fbe.fatoms.batch.staticblockexpiry.OfflinePosCompletion");

    public OfflinePosCompletionContext(String batchProcessName) {
        this.batchProcessName = batchProcessName;
        // TODO Auto-generated constructor stub
    }

    @Override
    public String getProcessClassName() {
        // TODO Auto-generated method stub
        return PROCESS_CLASSNAME;
    }

    @Override
    public boolean isMultiNodeSupported() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public Object[] getAdditionalProcessParams() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getBatchProcessName() {
        // TODO Auto-generated method stub
        return batchProcessName;
    }

    @Override
    public Map getInputTagDataMap() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Map getOutputTagDataMap() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void setAdditionalProcessParams(Object[] paramArrayOfObject) {
        // TODO Auto-generated method stub

    }

    @Override
    public void setBatchProcessName(String paramString) {
        // TODO Auto-generated method stub

    }

    @Override
    public void setInputTagDataMap(Map paramMap) {
        // TODO Auto-generated method stub

    }

    @Override
    public void setOutputTagDataMap(Map paramMap) {
        // TODO Auto-generated method stub

    }

}

package com.trapedza.bankfusion.fatoms;

import java.util.ArrayList;
import java.util.List;

import bf.com.misys.fbe.types.ModeOfOperationConrtol;
import bf.com.misys.fbe.types.ModeOfOperationCtrlDtls;

import com.misys.bankfusion.common.exception.BankFusionException;
import com.trapedza.bankfusion.bo.refimpl.IBOUB_ACC_ModeOfOperationCtrl;
import com.trapedza.bankfusion.core.SimplePersistentObject;
import com.trapedza.bankfusion.persistence.core.IPersistenceObjectsFactory;
import com.trapedza.bankfusion.servercommon.commands.BankFusionEnvironment;
import com.trapedza.bankfusion.servercommon.core.BankFusionThreadLocal;
import com.trapedza.bankfusion.steps.refimpl.AbstractUB_ATM_ISO8583_UpdateModeOfOperationCtrlDtls;

public class ATMUpdateModeOfOperationCtrlDtls extends AbstractUB_ATM_ISO8583_UpdateModeOfOperationCtrlDtls {

    /**
     *
     */
    private static final long serialVersionUID = -6535320239693479233L;

    IPersistenceObjectsFactory factory;

    private static final String GC_TRUE = "true";

    private IPersistenceObjectsFactory getFactory() {
        return BankFusionThreadLocal.getPersistanceFactory();
    }

    @SuppressWarnings("deprecation")
    public ATMUpdateModeOfOperationCtrlDtls(BankFusionEnvironment environment) {
        super(environment);
    }

    @Override
    public void process(BankFusionEnvironment env) throws BankFusionException {

        ModeOfOperationCtrlDtls modeOfOpUser = getF_IN_modeOfOperationCtrlDtls();
        ModeOfOperationConrtol[] modeOfOperationControl = modeOfOpUser.getModeOfOperationControl();

        factory = BankFusionThreadLocal.getPersistanceFactory();

        List<IBOUB_ACC_ModeOfOperationCtrl> listmodeOfOperationCtrlDtls = factory.findAll(IBOUB_ACC_ModeOfOperationCtrl.BONAME,
                null, true);
        ArrayList<String> modeOfOpInUXP = new ArrayList();
        ArrayList<String> modeOfOpInDBAfterDelete = new ArrayList();

        for (ModeOfOperationConrtol modeOfOperationCtrl : modeOfOperationControl) {
            modeOfOpInUXP.add(modeOfOperationCtrl.getModeOfOperation());
        }

        for (IBOUB_ACC_ModeOfOperationCtrl modeOfOpInDB : listmodeOfOperationCtrlDtls) {
            // deletion

            if (!modeOfOpInUXP.contains(modeOfOpInDB.getBoID())) {
                deleteModeOfOperationConfigRecords(modeOfOpInDB.getBoID());
                continue;

            }
            modeOfOpInDBAfterDelete.add(modeOfOpInDB.getBoID());// used
                                                                // in
                                                                // update
                                                                // and
                                                                // insert
        }
        for (int j = 0; j < modeOfOpInUXP.size(); j++) {

            if (modeOfOpInDBAfterDelete.contains(modeOfOperationControl[j].getModeOfOperation())) {
                // if exist in db already, perform update
                updateCustDtls(modeOfOperationControl[j].getModeOfOperation(), modeOfOperationControl[j].getIsCardAllowed(),
                        modeOfOperationControl[j].getIsAuthReq());
            }
            else {
                insertCustDtls(modeOfOperationControl[j].getModeOfOperation(), modeOfOperationControl[j].getIsCardAllowed(),
                        modeOfOperationControl[j].getIsAuthReq());
            }

        }

    }

    private void deleteModeOfOperationConfigRecords(String modeOfOperation) throws BankFusionException {
        ArrayList<String> params = new ArrayList<String>();
        String whereClause = " WHERE " + IBOUB_ACC_ModeOfOperationCtrl.UBMODEOFOPERATIONIDPK + "= ? ";

        params.add(modeOfOperation);

        List<SimplePersistentObject> dbRows = BankFusionThreadLocal.getPersistanceFactory().findByQuery(
                IBOUB_ACC_ModeOfOperationCtrl.BONAME, whereClause, params, null, true);
        deleteRecords(IBOUB_ACC_ModeOfOperationCtrl.BONAME, dbRows);
        BankFusionThreadLocal.getPersistanceFactory().findByQuery(IBOUB_ACC_ModeOfOperationCtrl.BONAME, whereClause, params, null,
                true);
    }

    private void deleteRecords(String boName, List<? extends SimplePersistentObject> records) {
        if (records != null && records.size() > 0) {
            for (SimplePersistentObject spo : records) {
                deleteRecord(boName, spo);
            }
        }

    }

    private void deleteRecord(String boName, SimplePersistentObject spo) {
        getFactory().remove(boName, spo);
    }

    private void insertCustDtls(String modeOfOperation, String isCardAllowedStr, String isAuthReqStr) throws BankFusionException {
        IBOUB_ACC_ModeOfOperationCtrl modeOfOp = (IBOUB_ACC_ModeOfOperationCtrl) getFactory().getStatelessNewInstance(
                IBOUB_ACC_ModeOfOperationCtrl.BONAME);
        // String mode = GUIDGen.getNewGUID();
        modeOfOp.setBoID(modeOfOperation);

        boolean isCardAllowed = false;
        if (GC_TRUE.equals(isCardAllowedStr)) {
            isCardAllowed = true;
        }

        boolean isAuthReq = false;
        if (GC_TRUE.equals(isAuthReqStr)) {
            isAuthReq = true;
        }

        modeOfOp.setF_UBISCARDALLOWED(isCardAllowed);
        modeOfOp.setF_UBISAUTHREQ(isAuthReq);

        getFactory().create(IBOUB_ACC_ModeOfOperationCtrl.BONAME, modeOfOp);
    }

    private void updateCustDtls(String modeOfOperation, String isCardAllowedStr, String isAuthReqStr) throws BankFusionException {
        IBOUB_ACC_ModeOfOperationCtrl mode = null;
        ArrayList<String> params = new ArrayList<String>();
        String whereClause = " WHERE " + IBOUB_ACC_ModeOfOperationCtrl.UBMODEOFOPERATIONIDPK + "= ? ";

        params.add(modeOfOperation);
        List<IBOUB_ACC_ModeOfOperationCtrl> modeOfOpCtrl = new ArrayList<IBOUB_ACC_ModeOfOperationCtrl>();

        modeOfOpCtrl = factory.findByQuery(IBOUB_ACC_ModeOfOperationCtrl.BONAME, whereClause, params, null, true);

        mode = (IBOUB_ACC_ModeOfOperationCtrl) modeOfOpCtrl.get(0);
        mode.setBoID(modeOfOperation);

        boolean isCardAllowed = false;
        if (GC_TRUE.equals(isCardAllowedStr)) {
            isCardAllowed = true;
        }

        boolean isAuthReq = false;
        if (GC_TRUE.equals(isAuthReqStr)) {
            isAuthReq = true;
        }

        mode.setF_UBISCARDALLOWED(isCardAllowed);
        mode.setF_UBISAUTHREQ(isAuthReq);

    }

}

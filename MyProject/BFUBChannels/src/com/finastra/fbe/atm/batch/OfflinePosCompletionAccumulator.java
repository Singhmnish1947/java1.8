package com.finastra.fbe.atm.batch;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.trapedza.bankfusion.batch.process.AbstractProcessAccumulator;

public class OfflinePosCompletionAccumulator extends AbstractProcessAccumulator {
    
    Map<String, String> map = null;
    List<Integer> accumulator = null;
    List<Integer> mergeAccumulator = null;
    List<Integer> savePoint = null;
    List<AbstractProcessAccumulator> listAcc = new ArrayList<>();
    public OfflinePosCompletionAccumulator(Object[] arg0) {
        super(arg0);
        map = Collections.synchronizedMap(new HashMap<String, String>());
        accumulator = Collections.synchronizedList(new ArrayList<Integer>());
        mergeAccumulator = Collections.synchronizedList(new ArrayList<Integer>());
        savePoint = Collections.synchronizedList(new ArrayList<Integer>());
        List<AbstractProcessAccumulator> listAcc = new ArrayList<>();
    }

    @Override
    public void addAccumulatorForMerging(AbstractProcessAccumulator arg0) {
        // TODO Auto-generated method stub
        if(listAcc == null) {
            listAcc = new ArrayList<>();
        }
        listAcc.add(arg0);
        
    }

    @Override
    public void acceptChanges() {
        // TODO Auto-generated method stub
        savePoint.addAll(accumulator);
    } 

    @Override
    public void accumulateTotals(Object[] arg0) {
        // TODO Auto-generated method stub
        accumulator.add((Integer)arg0[0]);
    }

    @Override
    public Object[] getMergedTotals() {
        // TODO Auto-generated method stub
        mergeAccumulator.clear();
        if(listAcc!=null) {
            listAcc.stream().forEach(acc -> {
                 mergeAccumulatedTotals(acc.getProcessWorkerTotals());
                });
        }
        mergeAccumulatedTotals(getProcessWorkerTotals());
        Object[] obj =new Object[1];
        obj[0] = mergeAccumulator;
        return obj;
    }

    @Override
    public Object[] getProcessWorkerTotals() {
        // TODO Auto-generated method stub
        Object[] ob = new Object[1];
        ob[0] = accumulator;
        int i=0;
        return ob;
    }

    @Override
    public void mergeAccumulatedTotals(Object[] arg0) {
        // TODO Auto-generated method stub
        mergeAccumulator.addAll((List<Integer>)(arg0[0]));
    }

    @Override
    public void restoreState() {
        // TODO Auto-generated method stub

    }

    @Override
    public void storeState() {
        // TODO Auto-generated method stub
        accumulator.addAll(savePoint);
    }

}

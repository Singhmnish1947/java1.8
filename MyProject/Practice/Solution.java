import java.util.*;
import java.io.*;
import java.math.*;

/**
 * Auto-generated code below aims at helping you parse
 * the standard input according to the problem statement.
 **/
class Solution {

    public static void main(String args[]) {
        Scanner in = new Scanner(System.in);
        int numberOfDays = in.nextInt();
        String output ="";

        String[] ind = new String[numberOfDays];
        for (int i = 0; i < numberOfDays; i++) {
            String ingredient = in.next();
            ind[i]=ingredient;
        }

        String fat = "FAT";
        String fiber = "FIBER";
        String carb = "CARB";

        int countOfFats=0;
        int countOfFibers=0;
        int countOfCarbs=0;
        int totalCount=0;

        for(int i=0; i<numberOfDays;i++){
            if(ind[i].contains(fat)){
                countOfFats++;
            }
            if(ind[i].contains(fiber)){
                countOfFibers++;
            }
            if(ind[i].contains(carb)){
                countOfCarbs++;
            }
        totalCount = countOfFats + countOfFibers + countOfCarbs;
        if((3>=totalCount && 2>=countOfFats )||(3>=totalCount && 2>=countOfFibers)||(3>=totalCount &&2>=countOfCarbs) ){
            output= output+"1";
            countOfFats=countOfFats-countOfFats;
            countOfFibers=countOfFibers-countOfFibers ;
            countOfCarbs=countOfCarbs-countOfCarbs;
        }
        else{
             output= output+"0";
        }
    }
    System.out.println(output);
    }
}
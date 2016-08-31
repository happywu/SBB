import java.io.*;
import java.util.BitSet;
import java.util.Iterator;
import java.util.Random;
import java.util.TreeSet;

/**
 * Created by happywu on 18/08/16.
 */
public class Main {
    public static void main(String[] args){

        TreeSet<sbbLearner> teams= new TreeSet<sbbLearner>(new sbbLearner.LearnerBidLexicalCompare());

        sbbLearner learner1 = new sbbLearner(1,1,1,1,1);
        sbbLearner learner2 = new sbbLearner(2,2,2,2,2);
        sbbLearner learner3 = new sbbLearner(3,3,3,3,3);
        learner1.key(2);
        learner2.key(1);
        learner3.key(3);
        learner1.lastCompareFactor(2);
        learner2.lastCompareFactor(1);
        learner3.lastCompareFactor(3);

        teams.add(learner1);
        teams.add(learner3);
        teams.add(learner2);


        Iterator iter = teams.iterator();
        while(iter.hasNext()){
            sbbLearner tmp = (sbbLearner) iter.next();
            System.out.println(tmp.id() + " "  + tmp.key() + " " + tmp.lastCompareFactor());
        }

        iter = teams.iterator();
        while(iter.hasNext()){
            sbbLearner tmp = (sbbLearner) iter.next();
            tmp.key(41);
            System.out.println(tmp.id() + " "  + tmp.key() + " " + tmp.lastCompareFactor());
        }
        long time = System.currentTimeMillis();
        System.out.println(" TIME IS " + time);

    }
    public void FileOpen(){
        String filepath = "checkpoints/cp.1.-1.7100.0.rslt";
        // String filepath = "checkpoints/a.txt";
        File file = new File(filepath);
        if(!file.exists()||!file.isFile()) {
            System.out.println("FILE DOES NOT EXIST!");
            return;
        }
        try {
            FileInputStream fileInputStream = new FileInputStream(file);
            InputStreamReader inputStreamReader = new InputStreamReader(fileInputStream);
            BufferedReader reader = new BufferedReader(inputStreamReader);
            String oneline = reader.readLine();
            System.out.println(oneline);

        } catch (FileNotFoundException e){
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

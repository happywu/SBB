import com.illposed.osc.OSCListener;
import com.illposed.osc.OSCMessage;
import com.illposed.osc.OSCPortIn;
import com.illposed.osc.OSCPortOut;
import com.sun.corba.se.spi.orbutil.fsm.Input;

import java.io.*;
import java.net.InetAddress;
import java.util.*;

/**
 * Created by happywu on 22/08/16.
 */
public class SBBHPAgent {
    private static int NUM_DIR  = 4;
    private static int CppAction;
    private int NUM_REWARD = 3;
    private static int NUM_TEST_GAMES = 1;

    private static double P_ADD_PROFILE_POINT =  0.0005;
    private static int MAX_BEHAVIOUR_STEPS =  5;
    private static int NUM_SENSOR_INPUTS =  94;
    private static int SBB_DIM =  28;
    private static int ATOMIC_ACCEPT =  -1;
    private static double DOUBLE_DYNAMIC_RANGE =  50.0;
    private static double BOOL_DYNAMIC_RANGE =  10.0;
    private static int POINT_DIM = 8;
    private static Vector<Double> currentState = new Vector<Double>();
    private static Vector<Integer> neighbours = new Vector<Integer>();
    private static Vector<Double> rewards = new Vector<Double>();
    private static double xCoord;
    private static double yCoord;
    private static Vector<Boolean> BOOL_INPUTS = new Vector<Boolean>(){};
    private static boolean[] stateVarIsBool = new boolean[NUM_SENSOR_INPUTS];

    static PrintWriter writer;
    static OSCPortIn serverPort;
    static OSCPortOut outgoingPort;
    static int game_server_port;
    static InetAddress agent_IP;
    static int agent_port;
    static int delay;

    static boolean moveReady;
    static boolean gameReady;
    static boolean newState;
    static boolean episodeEnd;

    static int maxLevel;

    int prevGhostScore;
    int numEatenGhosts;


    // Command-line parameters
    static boolean checkpoint = false;
    static int checkpointInMode = -1;
    static int hostFitnessMode = 1;
    static int hostToReplay = -1;
    static int phase = sbbMist._TRAIN_PHASE;
    static int port = -1;
    static int seed = -1;
    static boolean replay = false;
    static int statMod = -1;
    static long tMain = 1;
    static int tPickup = -1;
    static int tStart = 0;
    static boolean useMemory = false;
    static boolean usePoints = false;
    private static boolean visual = false;
    public void seed(int _seed){game_server_port = _seed;}
    public int seed(){return game_server_port ;}
    public boolean visual(){
        return visual;
    }
    public int delay(){
        return delay;
    }

    public boolean gameReady(){
        return gameReady;
    }

    public void gameReady(boolean b){
        gameReady = b;
    }
    public int maxLevel(){ return maxLevel; }
    public int port() { return game_server_port; }

    public static void parseCommandlineArguments(String[] args){
        for (int i = 0; i < args.length; i ++ ) {
            if(args[i].charAt(0)== '-' && args[i].length()>1) {
                String option =args[i];
                String value = (i+1 < args.length ? args[i+1] : null);
                if (option.equals("-C")) {
                    checkpoint = true;
                    checkpointInMode = Integer.parseInt(value);
                }
                if (option.equals("-f")) {
                    hostFitnessMode = Integer.parseInt(value);
                }
                if (option.equals("-O")) {
                    statMod = Integer.parseInt(value);
                }
                if (option.equals("-M")) {
                    useMemory = true;
                }
                if (option.equals("-P")) {
                    usePoints = true;
                }
                if (option.equals("-p")) {
                    agent_port = Integer.parseInt(value);
                    port = agent_port+ 1;
                }
                if (option.equals("-R")) {
                    replay = true;
                    hostToReplay = Integer.parseInt(value);
                }
                if (option.equals("-s")) {
                    seed = Integer.parseInt(value);
                }
                if (option.equals("-T")) {
                    tMain = Integer.parseInt(value);
                }
                if (option.equals("-t")) {
                    tStart = Integer.parseInt(value);
                    tPickup = tStart;
                }
                if (option.equals("-V")) {
                    visual = true;
                }
                if (option.equals("-h")) {
                    System.out.format("\nHelp\n\nCommand Line Options:\n\n");
                    System.out.println("-C <mode to read checkpoint from> (Read a checkpoint created during TAIN_MODE:0, VALIDATION_MODE:1, or TEST_MODE:2. Requires checkpoint file and -t option.)");
                    System.out.println("-f <hostFitnessMode> (GameScore:0 Pillscore:1, Ghostscore:2)");
                    System.out.println("-O <statMod>");
                    System.out.println("-M (use memory)");
                    System.out.println("-P (use points)");
                    System.out.println("-p <port> (Base port for communicating with game server. ports to p+3 should be free.)");
                    System.out.println("-R <hostIdToReplay> (Load and replay a specific host ID. Requires checkpoint file and options -C, and -t.)");
                    System.out.println("-s <seed> (Random seed)");
                    System.out.println("-T <generations>");
                    System.out.println("-t <t> (When loading populations form a checkpoint, t is the generation of the checkpoint file. Requires checkpoint file and option -C.)");
                    System.out.println("-V (Run with visualization.)");
                    System.exit(0);

                }
            }
        }

        /*
        System.out.println("====== mspacmanSBBHPAgent parameters:\n");
        System.out.println("chechkpoint " + checkpoint);
        System.out.println("checkPointInMode " + checkpointInMode);
        System.out.println("hostFitnessMode " + hostFitnessMode);
        System.out.println("statMod " + statMod);
        System.out.println("useMemory " + useMemory);
        System.out.println("usePoints " + usePoints);
        System.out.println("port " + port);
        System.out.println("replay " + replay);
        System.out.println("hostToReplay " + hostToReplay);
        System.out.println("seed " + seed);
        System.out.println("tMain " + tMain);
        System.out.println("tPickup " + tPickup);
        System.out.println("visual " + visual + "\n");*/

    }
    public static void setupSockets(){
        try{
            agent_IP = InetAddress.getLocalHost();

            //Set up sockets
            outgoingPort = new OSCPortOut(agent_IP, agent_port);
            serverPort = new OSCPortIn(port);



            OSCListener state = new OSCListener() {
                @Override
                public void acceptMessage(Date time, OSCMessage message) {
                    //episodeEnd.store((int)argv[0]->f);
                    //rewards.clear();
                    //neighbours.clear();
                    //currentState.clear();
                    rewards.set(0,Double.parseDouble( message.getArguments()[1].toString()));
                    rewards.set(1,Double.parseDouble( message.getArguments()[2].toString()));
                    rewards.set(2,Double.parseDouble( message.getArguments()[3].toString()));
                    xCoord = Double.parseDouble( message.getArguments()[4].toString());
                    yCoord = Double.parseDouble( message.getArguments()[5].toString());
                    neighbours.set(0,(Math.round((Float)message.getArguments()[6])));
                    neighbours.set(1,(Math.round((Float)message.getArguments()[7])));
                    neighbours.set(2,(Math.round((Float)message.getArguments()[8])));
                    neighbours.set(3,(Math.round((Float)message.getArguments()[9])));
                    for (int i = 0; i < NUM_SENSOR_INPUTS; i++) {
                        currentState.set(i,Double.parseDouble( message.getArguments()[i+10].toString()));
                    }
                    newState = true;
                }
            }; serverPort.addListener("state", state);

            OSCListener end = new OSCListener() {
                @Override
                public void acceptMessage(Date time, OSCMessage message) {
                    episodeEnd = true;
                }
            }; serverPort.addListener("end", end);


            serverPort.startListening();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public void sendEnd() {
        gameReady = false;
        OSCMessage msg = new OSCMessage("end");
        try{
            outgoingPort.send(msg);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private static void mspacmanDiscretizeState(Vector<Double> state, int steps){
        for (int i = 0; i < state.size(); i++) {
            if (!stateVarIsBool[i])
                state.set(i, sbbMist.discretize(state.get(i),0, DOUBLE_DYNAMIC_RANGE, steps));
        }
    }

    // dir must be one of 4 directions: 0-3. There are 28 inputs in total, 6 non-directed and 22 directed
    public static void getDirectedState(Vector<Double> currentState, Vector<Double> directionalState, int dir){
        directionalState.clear();
        for (int i = 0; i < 6; i++)//6 non-directed inputs: 0-5
            directionalState.add(currentState.get(i));
        for (int i = 0; i < 22; i++)//22 directed inputs; dir=0(6-27); dir=1(28-49); dir=2(50-71); dir=3(72-93)
            directionalState.add(currentState.get((6+(dir*22))+i));
    }

    // Checks if the nextAction will lead pacman into a wall
    public static boolean isTowardWall(Vector<Integer> n, int nextAction) {
        if (n.get(nextAction) >= 0)
            return false;
        else
            return true;
    }
    public static void init(Vector<Double> state, Vector<Integer> neighbours, int dim){
        for (int i = 0; i < dim; i++) {
            if(BOOL_INPUTS.contains(i))
                stateVarIsBool[i] = true;
            else
                stateVarIsBool[i] = false;
        }
    }


    public static void runEval(sbbHP sbbEval, int port, int t, int phase, boolean visual, int[] timeGenTotalInGame, int hostToReplay, long[] eval) {

        Random random = new Random();
        long timeStartGame;
        Vector < Double > behaviourSequence  = new Vector<Double>(); // store a discretized trajectory for diversity maintenance
        Vector < Double > tmpBehaviourSequence = new Vector<Double>();
        Vector < Double > directedState = new Vector<Double>();
        Vector < Double > selectedDirectedState = new Vector<Double>() ;
        int step;
        int currentAction;
        int prevAction;
        int atomicAction;
        long[] decisionInstructions = new long[1];
        long decisionInstructionsSum;
        int prevProfileId = -1;
        int newProfilePoints = 0;
        TreeMap<Integer,Integer> directedActions = new TreeMap<Integer,Integer>(Collections.reverseOrder());
        TreeMap<Double,Integer>  acceptedDirectionPreferences = new TreeMap<Double,Integer>(Collections.reverseOrder());
        TreeMap<Double,Integer> rejectedDirectionPreferences = new TreeMap<Double,Integer>(Collections.reverseOrder());
        TreeSet <sbbLearner>  learnersRanked = new TreeSet<sbbLearner>(new sbbLearner.LearnerBidLexicalCompare());

        Vector <sbbTeam> teams = new Vector<sbbTeam>();
        sbbEval.getTeams(teams);
        Vector <sbbPoint> points = new Vector<sbbPoint>();
        sbbEval.getPoints(points);
        if (visual){
            OSCMessage msg = new OSCMessage("visual");
            try {
                outgoingPort.send(msg);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }



        int diffcnt = 0;

        try {
            String pathname = "8900.txt";
            File file = new File(pathname);
            InputStreamReader reader = new InputStreamReader(new FileInputStream(file));
            BufferedReader br = new BufferedReader(reader);
            String line = br.readLine();
            //GetState(line);

            //System.out.println(teams.size());
            int prevF = 0;
            for (int i = 0; i < teams.size(); i++) {
                int numEval = visual ? NUM_TEST_GAMES : (sbbEval.usePoints() ? points.size() : (hostToReplay >= 0 ? NUM_TEST_GAMES : sbbEval.episodesPerGeneration()));
                if (hostToReplay < 0 || (hostToReplay >= 0 && teams.get(i).id() == hostToReplay)) {
                    for (int p = 0; p < numEval; p++) {
                        decisionInstructionsSum = 0;
                        if ((sbbEval.usePoints() && !teams.get(i).hasOutcome(points.get(p))) || (!sbbEval.usePoints() && teams.get(i).numOutcomes(phase) < sbbEval.numStoredOutcomesPerHost(phase))) {
                            //System.out.print("runEval t " + t  + " tm " + teams.get(i).id() );
                            writer.print("runEval t " + t + " tm " + teams.get(i).id());
                            if (sbbEval.usePoints())
                                //    System.out.println(" on point " + points.get(p).id());
                                writer.println(" on point " + points.get(p).id());
                            else
                                //    System.out.println(" numOut " + teams.get(i).numOutcomes(phase));
                                writer.println(" numOut " + teams.get(i).numOutcomes(phase));
                            if (sbbEval.usePoints()) {
                                if (false) {
                                } else {
                                    Vector<Double> pState = new Vector<Double>();
                                    points.get(p).pointState(pState);
                                /*
                                mspacmanPointServer.send("point", "ffffffff",
                                        (float)pState[0],//initialMaze
                                        (float)pState[1],//initialPacmanLocation
                                        (float)pState[2],//initialBlinkyLoc
                                        (float)pState[3],//initialPinkyLoc
                                        (float)pState[4],//initialInkyLoc
                                        (float)pState[5],//initialSueLoc
                                        (float)pState[6],//proportionOfPillsPresent
                                        (float)pState[7]);//powerPillsPresent?*/
                                }
                            }

                            OSCMessage msg = new OSCMessage("start");
                            try {
                                outgoingPort.send(msg);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            prevAction = 0;
                            step = 0;
                            timeStartGame = System.currentTimeMillis();
                            //if (sbbEval.useMemory()) teams.get(i).resetMemory();
                            while (!newState && !episodeEnd) {
                                try {
                                    Thread.sleep(1);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                            //  System.out.println("NEW STATE COMES!");
                            newState = false; //wait for initial state from server*/
                            int ccnt = 0;
                            while (!episodeEnd) {
                                acceptedDirectionPreferences.clear();
                                rejectedDirectionPreferences.clear();
                                double[] BB = new double[4];
                                for (int d = 0; d < NUM_DIR; d++) { //0:UP 1:RIGHT 2:DOWN 3:LEFT
                                    if (!isTowardWall(neighbours, d)) {
                                        getDirectedState(currentState, directedState, d);
                                        learnersRanked.clear();
                                        decisionInstructions[0] = 0;
                                        atomicAction = sbbEval.getAction(teams.get(i), directedState, (phase == sbbMist._TRAIN_PHASE ? true : false), learnersRanked, decisionInstructions);
                                        if (atomicAction == ATOMIC_ACCEPT) {
                                            if (!acceptedDirectionPreferences.containsKey(learnersRanked.first().bidVal()))
                                                acceptedDirectionPreferences.put(learnersRanked.first().bidVal(), d);
                                        } else {
                                            if (!rejectedDirectionPreferences.containsKey(learnersRanked.first().bidVal()))
                                                rejectedDirectionPreferences.put(learnersRanked.first().bidVal(), d);
                                        }
                                  //      System.out.println( "atomic Action: " + atomicAction + "dir: "+ d + " learnersRanked: "+ (learnersRanked.first()).bidVal());
                                        if (random.nextDouble() < P_ADD_PROFILE_POINT && teams.get(i).id() != prevProfileId) {
                                            sbbEval.addProfilePoint(directedState, rewards, phase, t);
                                            prevProfileId = (int) teams.get(i).id();
                                            newProfilePoints++;
                                        }
                                        BB[d] = learnersRanked.first().bidVal();
                                    }
                                }
                                //System.out.println(acceptedDirectionPreferences.size() + " : " + rejectedDirectionPreferences.size());
                                if (acceptedDirectionPreferences.size() > 0)
                                    currentAction = acceptedDirectionPreferences.firstEntry().getValue();
                                else
                                    currentAction = rejectedDirectionPreferences.lastEntry().getValue();
                            /*
                            Set<Double> keyset = rejectedDirectionPreferences.keySet();
                            for(Double key: keyset){
                                Integer a = rejectedDirectionPreferences.get(key);
                                System.out.println(" action: "+ key + "," + a);
                            }*/


                                behaviourSequence.add((double) ((currentAction * -1) - 1)); //actions are represented as negatives
                                getDirectedState(currentState, selectedDirectedState, currentAction);//sensor readings for the chosen direction
                                mspacmanDiscretizeState(selectedDirectedState, sbbEval.stateDiscretizationSteps());//only need the last 5!

                                for (int ii = 0; ii < selectedDirectedState.size(); ii++)
                                    behaviourSequence.add(selectedDirectedState.get(ii));

             //                   System.out.println("ACTION!: "+ (ccnt++) + " " + currentAction + " " + CppAction);
                                /*
                                if(currentAction!=CppAction){
                                    for(int j = 0;j<4;j++)System.out.print(BB[j]+ "   ,   ");
                                    System.out.println("DIFFERENT!" + currentAction + " " + CppAction );
                                    diffcnt++;
                                  //  OutPutState();
                                }*/

                                /*
                                writer.print((ccnt++) + " ");
                                for (int ii = 0; ii < currentState.size(); ii++) {
                                    writer.format("%.2f,", currentState.get(ii));
                                }
                                writer.println(currentAction);*/


                                msg = new OSCMessage("act");
                                msg.addArgument((int) currentAction);
                                try {
                                    outgoingPort.send(msg);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                                step++;
                                decisionInstructionsSum += decisionInstructions[0];
                                prevAction = currentAction;
                                //wait for new state from server (newState must be updated in state_handler)
                                /*
                                line = br.readLine();
                                if(line==null)break;
                                GetState(line);*/
                                while (!newState && !episodeEnd) {
                                    try {
                                        Thread.sleep(1);
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                }
                                newState = false;
                            }
                            timeGenTotalInGame[0] += (System.currentTimeMillis() - timeStartGame);
                            episodeEnd = false;
                            //get behaviour sequence for last 5 interactions
                            int start = Math.min((1 + sbbEval.dimBehavioural()) * MAX_BEHAVIOUR_STEPS, (int) behaviourSequence.size());
                            for (int b = behaviourSequence.size() - start; b < behaviourSequence.size(); b++)
                                tmpBehaviourSequence.add(behaviourSequence.get(b));

                            if (sbbEval.usePoints())
                                sbbEval.setOutcome(teams.get(i), points.get(p), tmpBehaviourSequence, rewards, phase, t);
                            else sbbEval.setOutcome(teams.get(i), tmpBehaviourSequence, rewards, phase, t);
                            eval[0]++;
                            behaviourSequence.clear();
                            tmpBehaviourSequence.clear();
                            System.out.println(" gameScore " + rewards.get(0) + " pillScore " + rewards.get(1) + " ghostScore " + rewards.get(2) + " steps " + step + " meanDecisionInst " + decisionInstructionsSum / step);
                        }
                    }
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        System.out.println(" diffnum: " + diffcnt);
        System.out.print( "mspacmanSBBHAgent::runEval t " + t + " numProfilePoints " + sbbEval.numProfilePoints());
        System.out.println(" newProfilePoints " + newProfilePoints);
    }

    private static void OutPutState() {
        for(int i=0;i<3;i++)
            System.out.print(rewards.get(i) + ",");
        System.out.print(xCoord+",");
        System.out.print(yCoord+",");

        for(int i=0;i<4;i++)
            System.out.print(neighbours.get(i)+",");

        for(int j=0;j<NUM_SENSOR_INPUTS;j++)
            System.out.print(currentState.get(j)+",");
        System.out.print("\n");
    }

    public static void init2() {
        for (int i = 0; i < 3; i++) rewards.add(0.0);
        for (int i = 0; i < 4; i++) neighbours.add(-1);
        for (int i = 0; i < NUM_SENSOR_INPUTS; i++)
            currentState.add(0.0);
        checkpoint = true;
        checkpointInMode = 0;
        hostFitnessMode = 0;
        statMod = 5;
        // useMemory = true;
        useMemory = false;
        replay = true;
        // hostToReplay = 7924827;
        //  hostToReplay = 7175533;
      //  hostToReplay = 8980520;
          hostToReplay = 9019146;
        tMain = 1000;
        //tStart = 178;
        // tStart = 516;
        //tStart = 709;
        tStart = 736;
        tPickup = tStart;
        visual = true;
        // seed = 7100;
        seed = 8900;
    }

    private static void GetState(String line) {
        String[] Out;
        Out = line.split(",");
        int cnt = 0;
        for(int i=0;i<3;i++)
            rewards.set(i,Double.parseDouble(Out[cnt++]));
        xCoord = Double.parseDouble(Out[cnt++]);
        yCoord = Double.parseDouble(Out[cnt++]);
        for(int i=0;i<4;i++)
            neighbours.set(i,Integer.parseInt(Out[cnt++]));
        for(int i = 0;i<NUM_SENSOR_INPUTS;i++)
            currentState.set(i,Double.parseDouble(Out[cnt++]));
        CppAction = Integer.parseInt(Out[cnt++]);
    }

    public static void init(){
        for(int i=0;i<3;i++)rewards.add(0.0);
        for(int i=0;i<4;i++)neighbours.add(-1);
        for(int i=0;i<NUM_SENSOR_INPUTS;i++)
            currentState.add(0.0);
    }

    public static void main(String args[]) throws FileNotFoundException {

        writer = new PrintWriter("sbbout.txt");




        parseCommandlineArguments(args);
        setupSockets();

        sbbHP sbbMain = new sbbHP();

        init();
        boolean initialize = true;

        //timing
        int timeGenSec0;
        int timeGenSec1;
        int[] timeTotalInGame = new int[1];
        int timeTemp;
        int timeGen;
        int timeSel;
        int timeCleanup;

        init2();
        //SBB Parameter Setup
        sbbMain.id(-1);
        sbbMain.useMemory(useMemory);
        sbbMain.usePoints(usePoints);
        sbbMain.seed(seed);
        sbbMain.dimPoint(POINT_DIM);
        sbbMain.dimBehavioural(SBB_DIM);
        sbbMain.hostFitnessMode(hostFitnessMode);
      //  sbbMain.setParams();
        sbbMain.numAtomicActions(2); //Binary action, yes or no for direction
        sbbMain.numStoredOutcomesPerHost(sbbMist._TRAIN_PHASE,10);
        init(currentState,neighbours,NUM_SENSOR_INPUTS);

        long[] totalEval = new long[1];
        totalEval[0] = 0;

        //loading populations from a chackpoint file
        String dir = "checkpoints";
        String prefix = "cp";
        String filepath = dir + "/" + prefix + "." + tPickup + "." + sbbMain.id()+ "." + sbbMain.seed() + "." + checkpointInMode +".rslt";
        sbbMain.readCheckpoint(checkpointInMode,filepath);
        ////////////////////////////////////////////////////////////////
        //sbbMain.recalculateLearnerRefs();
        //sbbMain.cleanup(tPickup,false);// don't prune learners because active/inactive is not accurate

        //prepareOutFile(ofs,"checkpoints","cp",levelPickup,tPickup,sbbMain.id(),sbbMain.seed(),checkpointInMode);
        //sbbMain.writeCheckpoint(checkpointInMode,ofs); ofs.close();
        //return 0;

       // System.out.println("YO" + sbbMain._M.first().members().first()._bid.get(0).bits.toString());
        if (replay == true){
            //sbbMain.printTeamInfo(tPickup,_TRAIN_PHASE);
            //sbbMain.printHostGraphsDFS((long)tPickup);
            if (sbbMain.usePoints()) sbbMain.initPoints();
            System.out.println(sbbMain.usePoints() + "POINT");
            runEval(sbbMain,port,tPickup,sbbMist._TRAIN_PHASE,visual,timeTotalInGame,hostToReplay,totalEval);
            //prepareOutFile(ofs,"checkpoints","cp",tPickup,sbbMain.id(),sbbMain.seed(),checkpointInMode);
            //sbbMain.writeCheckpoint(checkpointInMode,ofs); ofs.close();
            //sbbMain.printTeamInfo(tPickup,_TRAIN_PHASE);
            OSCMessage msg = new OSCMessage("exit");
            try{
                outgoingPort.send(msg);
            } catch (Exception e) {
                e.printStackTrace();
            }
            System.out.println("Goodbye cruel world.");
        }
    }
}

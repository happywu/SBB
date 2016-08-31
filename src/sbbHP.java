import sun.reflect.generics.tree.Tree;

import java.io.*;
import java.util.*;

import static jdk.nashorn.internal.objects.NativeString.split;

/**
 * Created by happywu on 22/08/16.
 */
public class sbbHP {

    /********************************************************************************************
     *  sbbHP member variables and data structures.
     ********************************************************************************************/

      /* Populations */
    protected TreeSet< sbbTeam > _M = new TreeSet<sbbTeam>(new sbbTeam.teamIdComp()); /* Teams */
    protected TreeMap< Long, sbbTeam> _teamMap = new TreeMap<Long, sbbTeam>();
    protected Vector< sbbTeam > _mEvaluate = new Vector<sbbTeam>();
    protected TreeSet< sbbLearner > _L = new TreeSet<sbbLearner>(new sbbLearner.LearnerBidLexicalCompare()); /* Learners. */
    protected TreeSet< sbbPoint > _P = new TreeSet<sbbPoint>(new sbbPoint.pointLexicalLessThan());

    //Vector < double > bid1; /* First highest bid, reporting only */
    //Vector < double > bid2; /* Second highest bid, reporting only */
    protected  sbbTeam currentChampion;
    protected  double _compatibilityThresholdGeno;
    protected  double _compatibilityThresholdIncrement;
    protected  double _compatibilityThresholdPheno;
    protected  boolean _competitivePoints;
    protected  int _dimP;
    protected  int _dimB;
    protected  int _diversityMode;
    protected  long _episodesPerGeneration; /* How many games should each team get to play before calculating fit and performing selection. */
    protected  int _distMode;
    protected  int _fitMode;
    //	void getDistinctions(set < point * > &, map < point *, Vector < short > * > &);
    protected  int _id;
    protected  int _keepawayStateCode;
    protected  int _knnNov;
    protected  long learner_count;
    protected  long _maxProgSize;
    protected  double _maxTrainingReward;
    protected  long _mdom; /* Number of individuals pushed out of the population by younger individuals. */
    protected  long _Mgap; /* Team generation gap. */
    protected  int _minOutcomesForNoveltyArchive;
    protected  int  m_lastAction;
    protected  long _Msize; /* Team population size. */
    protected  int _numActions; /* Number of actions, actions 0, 1, ..., _numActions - 1 are assumed. */
    protected  int _numExpectedAdditionsToArchive;
    protected  long[] _numStoredOutcomesPerHost = new long[4];
    protected  long _omega; /* Maximum team size. */
    // protected  ostringstream oss; /* logging, reporting */
    protected  double _paretoEpsilonTeam;
    protected  double _pAtomic;
    protected  double _pBidAdd;
    protected  double _pBidDelete;
    protected  double _pBidMutate;
    protected  double _pBidSwap;
    protected  long _pdom; /* Number of individuals pushed out of the population by younger individuals. */
    protected  long _Pgap;
    protected  int _phase; // 0:train 1:validation 2:test
    protected  long point_count;
    protected  double _pma; /* Probability of learner addition. */
    protected  double _pmd; /* Probability of learner deletion.*/
    protected  double _pmm; /* Probability of learner mutation. */
    protected  double _pmn; /* Probability of learner action mutation. */
    protected  double _pNoveltyGeno; //percentage of nov relative to fit in score
    protected  double _pNoveltyPheno; //percentage of nov relative to fit in score
    protected  Vector < sbbPoint > _profilePoints = new Vector<sbbPoint>(); /* Points used to build the learner profiles. */
    protected  ArrayDeque< sbbPoint > _profilePointsFIFO = new ArrayDeque<sbbPoint>();
    protected  long _Psize;
    protected  int _seed;
    protected  double _sigmaShare;
    protected  long _simTime; //simulator time
    protected  TreeSet < sbbPoint > _solvedPoints = new TreeSet<sbbPoint>(); /* Unique points solved so far during training. */
    protected  int _stateDiscretizationSteps;
    protected  int _tCull; /* what generation to test for restart. */
    protected  long team_count;
    protected  int _teamPow;
    protected  long _testPhaseEpochs; /* How many epochs in test phase. */
    protected  boolean _useMemory;
    protected  boolean _usePoints;
    protected  long _validPhaseEpochs; /* How many epochs in validation phase. */
    protected Iterator iter = _mEvaluate.iterator();
    protected sbbTeam _team;
    public int dimPoint(){ return _dimP; }
    public void dimPoint(int dim){ _dimP = dim; }
    public int dimBehavioural(){ return _dimB; }
    public void dimBehavioural(int dim){ _dimB = dim; }
    public int diversityMode(){ return _diversityMode; }
    public void diversityMode(int i){ _diversityMode = i; }
    public int episodesPerGeneration() { return (int) _episodesPerGeneration; }
    public void episodesPerGeneration(int e) { _episodesPerGeneration = e; }
    public int hostDistanceMode(){ return _distMode; }
    public void hostDistanceMode(int i){ _distMode = i; }
    public int hostFitnessMode(){ return _fitMode; }
    public void hostFitnessMode(int i){ _fitMode = i; }
    public long id() { return _id; }
    public void id(long id) { _id = (int) id; }
    public double maxTrainingReward() { return _maxTrainingReward; }
    public void  maxTrainingReward(double d) { _maxTrainingReward = d;}
    public int numProfilePoints() { return _profilePointsFIFO.size(); }
    public int numStoredOutcomesPerHost(int phase) { return (int) _numStoredOutcomesPerHost[phase]; }
    public void numStoredOutcomesPerHost(int phase, long nso) { _numStoredOutcomesPerHost[phase] = nso; }
    public int phase() { return _phase; }
    public void phase(int p) { _phase = p; }
    public long pSize() { return _Psize; }
    public long seed() { return _seed; }
    public void seed(long seed) {
        _seed = (int) seed;
        //readCheckpoint would find the highest team_count and learner_count and start there
        team_count = 1000*seed;
        learner_count = 1000*seed;
        point_count = 1000*seed;
    }
    public void setupTeamCount(int s) { team_count = 1000*s; }
    public int stateDiscretizationSteps() { return _stateDiscretizationSteps; }
    public int tCull(){ return _tCull; }
    public int testPhaseEpochs(){ return (int) _testPhaseEpochs; }
    public void testPhaseEpochs(int e){ _testPhaseEpochs = e; }
    public boolean useMemory() { return _useMemory; }
    public void useMemory(boolean m) { _useMemory = m; }
    public boolean usePoints() { return _usePoints; }
    public void usePoints(boolean p) { _usePoints = p; }

    public int validPhaseEpochs(){ return (int) _validPhaseEpochs; }

    /********************************************************************************************
     * Methods to implement the SBB algorithm.
     ********************************************************************************************/

    public void activeTeam(int id){
        while(iter.hasNext()){
            _team = (sbbTeam) iter.next();
            if(_team.id() == id)break;
        }
    }
    public int activeTeamId() {
        return (int) _team.id();
    }
    public void addProfilePoint(Vector < Double > bState, Vector <Double> rewards, int phase, long gtime){
        _profilePointsFIFO.add(new sbbPoint(gtime,phase,point_count++, bState, rewards));
        while (_profilePointsFIFO.size() > sbbLearner.PROFILE_SIZE){
            _profilePointsFIFO.removeFirst();
        }
    }
    public int currentChampId(){ return (int) currentChampion.id();}
    public int currentChampNumOutcomes(int phase){ return (int) currentChampion.numOutcomes(phase);}
    public int evaluationVectorSize(){ return _mEvaluate.size(); }
    public int getAction(Vector < Double > state, boolean updateActive, TreeSet<sbbLearner> learnersRanked, long[] inst)
    {
        TreeSet< sbbTeam> visitedTeams = new TreeSet<sbbTeam>();
        return _team.getAction(state, _teamMap, updateActive, learnersRanked, inst, visitedTeams,_useMemory);
    }
    public int getAction(sbbTeam  tm, Vector < Double > state, boolean updateActive, TreeSet <sbbLearner> learnersRanked, long[] inst)
    {
        TreeSet< sbbTeam> visitedTeams = new TreeSet<sbbTeam>(new sbbTeam.teamScoreLexicalCompare());
        return tm.getAction(state, _teamMap, updateActive, learnersRanked, inst, visitedTeams,_useMemory);
    }
    public long getCurrentTeamId(){ return _team.id(); }
    public void getFirstTeam(){
        _team = _mEvaluate.firstElement();
    }
    public void getLastTeam(){
        _team = _mEvaluate.lastElement();
    }
    public void getLearnerPop(TreeSet <sbbLearner> lp){ lp = (TreeSet<sbbLearner>) _L.clone(); }
    public void getNextTeam(){
        if(_team != _mEvaluate.lastElement()) {
            _team = (sbbTeam) iter.next();
        }
    }
    public void getPoints(Vector <sbbPoint> p) {
        Iterator iter = _P.iterator();
        while(iter.hasNext()){
            p.add(new sbbPoint((sbbPoint) iter.next()));
        }
    }
    public void getTeams(Vector <sbbTeam> t) {
        Iterator iter = _M.iterator();
        while(iter.hasNext()){
            t.add(new sbbTeam((sbbTeam) iter.next()));
        }
    }
    public int lSize(){ return _L.size(); }
    public int mSize(){ return _M.size(); }
    public int numAtomicActions()
    {  return _numActions; }
    public void numAtomicActions(int a) { _numActions = a; }
    public void replaceLearnerPop(TreeSet <sbbLearner> lp){ _L = (TreeSet<sbbLearner>) lp.clone(); }
    public void resetOutcomes(int phase){
        Iterator iter = _M.iterator();
        while(iter.hasNext()){
            sbbTeam team = (sbbTeam) iter.next();
            team.resetOutcomes(phase);
        }
    }
    /* Team tm will store a clone of the point with the same id. */
    public void setOutcome(sbbTeam tm, Vector < Double > bState, Vector<Double> rewards, int phase, long gtime){
        tm.setOutcome(new sbbPoint(gtime, phase, point_count++, bState, rewards) , rewards.get(_fitMode), _numStoredOutcomesPerHost[phase]);
    }
    public void setOutcome(sbbTeam tm, sbbPoint pt, Vector < Double > bState, Vector<Double> rewards, int phase, long gtime){
        Vector<Double> pState = new Vector<Double>();
        pt.pointState(pState);
        sbbPoint teamPoint = new sbbPoint(gtime, phase, pt.id(), pState, bState, rewards);
        tm.setOutcome(teamPoint , rewards.get(_fitMode), _numStoredOutcomesPerHost[phase]);
    }
    public long simTime() { return _simTime; }
    public sbbPoint initUniformPointGeneric(long gtime, long id, int phase){
        Random rand = new Random();
        Vector < Double > pState = new Vector<Double>();
        Vector < Double > bState = new Vector<Double>();

        for (int i = 0; i < _dimP; i++)
            pState.add(rand.nextDouble());
        for (int i = 0; i < _dimB; i++)
            bState.add(rand.nextDouble());
        Vector<Double> rewards = new Vector<Double>();
        rewards.add(0.0);
        rewards.add(0.0);
        rewards.add(0.0);
        sbbPoint p = new sbbPoint(gtime, phase, id, pState, bState, rewards);
        return p;
    }
    public void initPoints(){
        TreeSet<Long> usedIds;
        while(_P.size() < _Psize - _Pgap)
            _P.add(initUniformPointGeneric(-1, point_count++, sbbMist._TRAIN_PHASE));
    }
    public int setRoots(){
        _teamMap.clear();
        Iterator iter = _M.iterator();
        while(iter.hasNext()){
            sbbTeam team = (sbbTeam) iter.next();
            team.root(true);
            _teamMap.put(team.id(),team);
        }
        int nroots = 0;
        iter = _L.iterator();
        while(iter.hasNext()){
            sbbLearner learner = (sbbLearner) iter.next();
            if(learner.action() >= 0) {
                _teamMap.get(learner.action()).root(false);
                nroots++;
            }
        }
        return nroots;
    }
    /***********************************************************************************************************
     * Read in populations from a checkpoint file. (This whole process needs a rewrite sometime.)
     **********************************************************************************************************/
    public void readCheckpoint(int phase, String filepath){

        File file = new File(filepath);
        if(!file.exists()||!file.isFile()) {
            System.out.println("FILE DOES NOT EXIST!");
            return;
        }
        //std::pair<std::set<team *, teamIdComp>::iterator,bool> ret;

        TreeSet< sbbLearner > testTeamMembers = new TreeSet<sbbLearner>();
        String oneline;
        char delim=':';

        long memberId = 0;
        long max_team_count = -1;
        long max_learner_count = -1;
        String token;

        String[] outcomeFields;
        try {
            FileInputStream fileInputStream = new FileInputStream(file);
            InputStreamReader inputStreamReader = new InputStreamReader(fileInputStream);
            BufferedReader reader = new BufferedReader(inputStreamReader);

            while((oneline = reader.readLine())!=null) {

                outcomeFields = oneline.split(":");

                if (outcomeFields[0].equals("seed")) {
                    _seed = Integer.parseInt(outcomeFields[1]);
                } else if (outcomeFields[0].equals("learner")) {
                    long id = 0;
                    long gtime = 0;
                    long action = 0;
                    long dim = 0;
                    int nrefs = 0;
                    Vector<sbbLearner.instruction>bid = new Vector<sbbLearner.instruction>();
                    sbbLearner.instruction  in;
                    id = Integer.parseInt(outcomeFields[1]);
                    if (id > max_learner_count) max_learner_count = id;
                    gtime = Integer.parseInt(outcomeFields[2]);
                    action = Integer.parseInt(outcomeFields[3]);
                    dim = Integer.parseInt(outcomeFields[4]);
                    if (phase < sbbMist._TEST_PHASE) nrefs = Integer.parseInt(outcomeFields[5]);
                    else nrefs = 0;

                    for (int ii = 6; ii < outcomeFields.length; ii++) {
                        token = outcomeFields[ii].toString();
                        in = new sbbLearner.instruction();
                        in.bits.clear();
                        for (int j = 22, k = 0; j >= 0; j--, k++) { //there are 23 bits in each instruction
                            if (token.charAt(j) == '1') {
                                in.bits.set(k);
                            }
                        }
                        bid.add(in);
                    }
                    sbbLearner l;
                    l = new sbbLearner(gtime, gtime, action, dim, id, nrefs, bid);
                   // System.out.println(bid.get(0).bits.toString());
                    _L.add(l);
                } else if (outcomeFields[0].equals("team")) {
                    long id = 0;
                    long gtime = 0;
                    long numOutcomes = 0;
                    TreeSet<sbbLearner> members;
                    sbbTeam m;
                    id = Integer.parseInt(outcomeFields[1]);
                    if (id > max_team_count) max_team_count = id;
                    gtime = Integer.parseInt(outcomeFields[2]);
                    numOutcomes = Integer.parseInt(outcomeFields[3]);

                    m = new sbbTeam(gtime, id);
                    m.tmpNumOutcomes(numOutcomes);
                    for (int ii = 4; ii < outcomeFields.length; ii++) {
                        memberId = Integer.parseInt(outcomeFields[ii]);
                        Iterator iter = _L.iterator();
                        while(iter.hasNext()){
                            sbbLearner learner = (sbbLearner) iter.next();
                            if(learner.id() == memberId)
                                m.addLearner(learner);
                          //  System.out.println(" HE: " + learner.lastCompareFactor() + learner.getBid().get(0).bits.toString());
                        }
                    }
                    //if we get a duplicate team id, scale by 100 until unique
                    while(!_M.add(m)){
                        m.id(m.id() * 100);
                    }
                }
            }
            fileInputStream.close();
            inputStreamReader.close();
            reader.close();

        }catch (FileNotFoundException e){
            e.printStackTrace();
        }catch (IOException e){
            e.printStackTrace();
        }
   /* In _TEST_PHASE all learner nrefs will be set to zero, so we recalculate them here based on
    * the single test team.
    */
        if (phase == sbbMist._TEST_PHASE || phase == sbbMist._PLAY_PHASE){
            testTeamMembers = (TreeSet<sbbLearner>) _M.first().members().clone();
            Iterator iter = testTeamMembers.iterator();
            while(iter.hasNext()){
                sbbLearner team = (sbbLearner) iter.next();
                team.refInc();
            }
        }

        learner_count = max_learner_count+1;
        team_count = max_team_count+1;

        setRoots();
    }
}

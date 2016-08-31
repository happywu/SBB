import sun.reflect.generics.tree.Tree;

import java.util.*;

/**
 * Created by happywu on 17/08/16.
 */
public class sbbTeam {
    private TreeSet< sbbLearner > _active = new TreeSet<sbbLearner>(new sbbLearner.LearnerBidLexicalCompare()); /* Active member learners, a subset of _members, activated in getAction(). */
    private Vector<Long> _ancestors = new Vector<Long>(); /* lineage */
    private boolean _archived;
    private static long _count; /* Next id to use. */
    //<Double> _distances_0;
    //multiset <Double> _distances_1;

    private TreeMap<Double,Integer> _distances_0 = new TreeMap<Double,Integer>();
    private TreeMap<Double,Integer> _distances_1 = new TreeMap<Double,Integer>();
    private int _domBy; /* Number of other teams dominating this team. */
    private int _domOf; /* Number of other teams that this team dominates. */
    private double _fit;
    private long _gtime; /* Time step at which generated. */
    private long _id; /* Unique id of team. */
    private double _key; /* For sorting. */
    private int _lastCompareFactor;
    private HashMap < sbbPoint, Double > _margins = new HashMap<sbbPoint, Double>(); /* Maps point->margin. */
    private TreeSet< sbbLearner > _members = new TreeSet<sbbLearner>(new sbbLearner.LearnerBidLexicalCompare()); /* The member learners. */
    private int _nodes; /* Number of nodes at all levels assuming this team is the root. */
    //Map < sbbPoint, Double, pointLexicalLessThan > _outcomes; /* Maps point->outcome. */
    private TreeMap<sbbPoint, Double> _outcomes = new TreeMap<sbbPoint, Double>(new sbbPoint.pointLexicalLessThan());
    private double _parentFitness;
    private double _parentNovelty;
    private double _parentScore;
    private boolean _root;
    private double _score;
    private long _tmpNumOutcomes;

    public TreeSet< sbbLearner > activeMembers(){
        return _active;
    }

    public sbbTeam(sbbTeam B){
        _archived = B.archived();
        _domBy = B.domBy();
        _domOf = B.domOf();
        _fit = B.fit();
        _gtime = B.gtime();
        _id = B.id();
        _key = B.key();
        _lastCompareFactor = B.lastCompareFactor();
        _nodes = B.nodes();
        _parentFitness = B.parentFitness();
        _parentNovelty = B.parentNovelty();
        _parentScore = B.parentScore();
        _root = B.root();
        _score = B.score();
        _tmpNumOutcomes = B.tmpNumOutcomes();

        _active.clear();
        for(sbbLearner learner : B._active){
            _active.add(new sbbLearner(learner));
        }

        _ancestors.clear();
        for(long key : B._ancestors){
            _ancestors.add(key);
        }

        _members.clear();
        for(sbbLearner key : B.members()){
            _members.add(new sbbLearner(key));
        }

        _distances_0.clear();
        for(Map.Entry<Double,Integer> entry : B._distances_0.entrySet()){
            Double key = entry.getKey();
            Integer value = entry.getValue();
            _distances_0.put(key,value);
        }

        _distances_1.clear();
        for(Map.Entry<Double,Integer> entry : B._distances_1.entrySet()){
            Double key = entry.getKey();
            Integer value = entry.getValue();
            _distances_1.put(key,value);
        }


        _margins.clear();
        for(Map.Entry<sbbPoint,Double> entry : B._margins.entrySet()){
            sbbPoint key = entry.getKey();
            Double value = entry.getValue();
            _margins.put(new sbbPoint(key),value);
        }

        _outcomes.clear();
        for(Map.Entry<sbbPoint,Double> entry : B._outcomes.entrySet()){
            sbbPoint key= entry.getKey();
            Double value = entry.getValue();
            _outcomes.put(new sbbPoint(key),value);
        }



    }
    public sbbTeam(long gtime, long id){
        _archived = false;
        _domBy = -1;
        _domOf = -1;
        _fit = 0.0;
        _gtime = gtime;
        _id = id;
        _key = 0;
        _lastCompareFactor = -1;
        _nodes = 1;
        _parentFitness = 0.0;
        _parentNovelty = 0.0;
        _parentScore = 0.0;
        _root = false;
        _score = 0;
        _tmpNumOutcomes = -1;
    }
    void addAncestor(long a) { _ancestors.add(a); }
    void addDistance(int type, double d){
        if (type == 0) {
            if(_distances_0.get(d) == null){
                _distances_0.put(d,1);
            }else{
                _distances_0.put(d,_distances_0.get(d));
            }
        }
        else if (type == 1){
            if(_distances_1.get(d) == null){
                _distances_1.put(d,1);
            }else{
                _distances_1.put(d,_distances_1.get(d));
            }
        }
    }
    public boolean addLearner(sbbLearner lr){
        boolean added;

       /* if _memebers has already lr, return false */
        added = _members.add(new sbbLearner(lr));

        return added;
    }
    /***************  to be done ***************/
    public String checkpoint(int phase){
        return null;
    }


    //double archived(){ return _archived; }
    public void archived(boolean a) { _archived = a; }
    public boolean archived() {return  _archived ; }
    public int asize(){ return _active.size(); } /* The number of active learners in this team. */
    // public String checkpoint(int);
    public void clearDistances(){ _distances_0.clear(); _distances_1.clear();}
    public long collectiveAge(long t){
        long age = 0;
        Iterator<sbbLearner> iter = _members.iterator();
        while(iter.hasNext()){
            age += t - iter.next().gtime();
        }
        return age;
    }
    // void deleteMargin(point *); /* Delete margin. */
    /*************** to be done ***************/
    // void deleteOutcome(point *); /* Delete outcome. */

    // add all features of team members
    public TreeSet<Long> features(){
        TreeSet<Long> ans = new TreeSet<Long>();

        Iterator<sbbLearner> iter = _members.iterator();
        while(iter.hasNext()){
            ans.addAll(iter.next().features());
        }
        return ans;
    }

    public double nov(int type, int kNN){
        double nov = 0;

        int i = 0;
        // kNN + 2
        if(type == 0){
            for(Map.Entry<Double,Integer> entry : _distances_0.entrySet()){
                Double key = entry.getKey();
                Integer value = entry.getValue();
                while(i<=kNN&&value>0) {
                    nov += key;
                    i++;
                    value--;
                }
                if(i>kNN)break;
            }
        }else{
            for(Map.Entry<Double,Integer> entry : _distances_1.entrySet()){
                Double key = entry.getKey();
                Integer value = entry.getValue();
                while(i<=kNN&&value>0) {
                    nov += key;
                    i++;
                    value--;
                }
                if(i>kNN)break;
            }
        }

        return (double)nov/i;
    }

    public double symbiontUtilityDistance(sbbTeam t, long omega){
        Vector<Long> symbiontIntersection = new Vector<Long>((int)omega);
        Vector<Long> symbiontUnion = new Vector<Long>((int)omega);
        int symIntersection;
        int symUnion;
        Vector < Long > team1Ids = new Vector<Long>();
        Vector < Long > team2Ids = new Vector<Long>();
        TreeSet<sbbLearner> activeMembers = new TreeSet<sbbLearner>();
        activeMembers = t.activeMembers();
       /* if either team has no active members then return 0 */
        if (_active.size() < 1 || activeMembers.size() < 1)
            return 0.0;
        Iterator<sbbLearner> iter1 = _active.iterator();
        while(iter1.hasNext()){
            team1Ids.add(iter1.next().id());
        }
        Iterator<sbbLearner> iter2 = activeMembers.iterator();
        while(iter2.hasNext()){
            team2Ids.add(iter2.next().id());
        }


        Collections.sort(team1Ids);
        Collections.sort(team2Ids);

        symbiontIntersection.clear();

        symbiontIntersection = intersection(team1Ids,team2Ids);
        symIntersection = symbiontIntersection.size();
        symbiontUnion.clear();
        symbiontUnion = union(team1Ids,team2Ids);
        symUnion = symbiontUnion.size();
        return 1.0 - ((double)symIntersection / (double)symUnion);
    }

    // This version populates learnersRanked with all learners at each level, sorted by LearnerBidLexicalCompare
    public int getAction(Vector<Double> state, TreeMap<Long, sbbTeam> teamMap, boolean updateActive,
                         TreeSet<sbbLearner> learnersRanked, long[] decisionInstructions,
                         TreeSet<sbbTeam> visitedTeams, boolean useMemory
    ){
        /*
      Returns the index of the action to be taken in the environment, i.e., the
      action of the winning bidder in the level 0 team.
      */


//        if(_members.size() < 2)
        //           die(__FILE__, __FUNCTION__, __LINE__, "team is too small");


        Iterator iter = _members.iterator();
        //System.out.println("YO000");
        while(iter.hasNext()){
          //  System.out.println(cnt++);
            sbbLearner learner_now = (sbbLearner) iter.next();
            //System.out.println("sbbTeam:getAction: " + learner_now.action() + learner_now._bid.get(0).toString() + " " + learner_now._bid.get(1).toString());
         //   System.out.println(" FIR leanr_ "+ learner_now.action());
            learner_now.bidVal(learner_now.bid(state, useMemory));
           // System.out.println("sbbteam:getaction: afterbid: " + learner_now.bidVal());
            learner_now.key(learner_now.bidVal());
            decisionInstructions[0] += learner_now.esize();
        }
        for(sbbLearner learner : _members){
           // System.out.println(" straing"  + learner.bidVal());
            learnersRanked.add(new sbbLearner(learner));
        }

        Iterator tmpiter = learnersRanked.iterator();

        while(tmpiter.hasNext()){
            sbbLearner learner = (sbbLearner) tmpiter.next();
            //System.out.println("cant see why!!!"  + learner.bidVal());
        }
        //System.out.println("YO111");
        if(updateActive == true){
            for(sbbLearner learner:learnersRanked){
                _active.add(new sbbLearner(learner));
            }
        }

        tmpiter = learnersRanked.iterator();

        while(tmpiter.hasNext()){
            sbbLearner learner = (sbbLearner) tmpiter.next();
        //    System.out.println("!!!"  + learner.bidVal());
        }

        //Loop through learners highest to lowest bid:
        //1. If the learner is atomic, return the action
        //2. Else if the meta-action has not been visited yet, follow the meta-action
        //3. Go to step 1.
        //Note: Every team has at least 1 "fail-safe" atomic learner.
        //  long teamIdToFollow = -(numeric_limits<long>::max());
        long teamIdToFollow = -(Long.MAX_VALUE);
        iter = learnersRanked.iterator();

        while(iter.hasNext()){
            sbbLearner learner_now = (sbbLearner) iter.next();
         //   System.out.println(learner_now.action() + "learner_action");
            if(learner_now.action()<0){ // atomic
                    /* Force champion learner with atomic action to beginning of set,
                    followed by every other learner visited in order of bid */
                learner_now.key(Double.MAX_VALUE);
                return (int) learner_now.action();
            }else{
                if(!visitedTeams.contains(teamMap.get(learner_now.action()))){
                    teamIdToFollow = learner_now.action();
                    break;
                }
            }
        }
        //System.out.println("YO222");
        if (teamIdToFollow == -(Long.MAX_VALUE))
            System.out.println("WTF FOUND NO TEAM OR ATOMIC!");

       /* Repeat at the chosen team in the level below (don't update active learners). */
        visitedTeams.add(teamMap.get(teamIdToFollow));
        return teamMap.get(teamIdToFollow).getAction(state, teamMap, false, learnersRanked, decisionInstructions, visitedTeams, useMemory);
    }

    public void getAllNodes(TreeMap<Long, sbbTeam> teamMap,TreeSet<sbbTeam> visitedTeams, TreeSet<sbbLearner> learners){

        visitedTeams.add(teamMap.get(_id));

        Iterator iter = _members.iterator();
        while(iter.hasNext()){
            sbbLearner learner_now = (sbbLearner) iter.next();
            learners.add(learner_now);
            if(learner_now.action() >= 0 && !visitedTeams.contains(learner_now.action()))
                teamMap.get(learner_now.action()).getAllNodes(teamMap, visitedTeams, learners);
        }
    }

    // Fill F with every feature indexed by every learner in this policy (tree).If we ever build
    // massive policy trees, this should be changed to a more efficient traversal. For now just
    // look at every node.
    public int policyFeatures(TreeMap<Long, sbbTeam> teamMap, TreeSet<sbbTeam> visitedTeams, TreeSet<Long> F){
        visitedTeams.add(teamMap.get(_id));

        TreeSet<Long> featuresSingle = new TreeSet<Long>();
        int numLearnersInPolicy = 0;
        Iterator iter = _members.iterator();
        while(iter.hasNext()) {
            sbbLearner learner_now = (sbbLearner) iter.next();
            numLearnersInPolicy++;
            featuresSingle.clear();
            featuresSingle = (TreeSet<Long>) learner_now.features().clone();

            F.addAll(featuresSingle);


            System.out.print("policyFeatures tm " + _id + " numFeatSingle " + featuresSingle.size() + " feat");
            Iterator iter2 = featuresSingle.iterator();
            while (iter2.hasNext()) {
                System.out.print(" " + iter2.next());
            }
            System.out.print(" numPFeat " + F.size() + " pFeat");
            iter2 = F.iterator();
            while (iter2.hasNext()) {
                System.out.print(" " + iter2.next());
            }
            System.out.format("\n");

            if (learner_now.action() >= 0 && !visitedTeams.contains(teamMap.get(learner_now.action())))
                numLearnersInPolicy += teamMap.get(learner_now.action()).policyFeatures(teamMap, visitedTeams, F);
        }
        return numLearnersInPolicy;
    }


    // Count all effective instructions in every learner in this policy (tree). Make sure to
    // mark introns first.
    public  int policyInstructions(TreeMap <Long, sbbTeam> teamMap, TreeSet <sbbTeam> visitedTeams, boolean effective)
    {
        visitedTeams.add(teamMap.get(_id));

        int numInstructions = 0;
        Iterator iter = _members.iterator();
        while(iter.hasNext()){
            sbbLearner learner_now = (sbbLearner) iter.next();
            if(effective)
                numInstructions += learner_now.esize();
            else numInstructions += learner_now.size();
            if(learner_now.action() >= 0 && ! visitedTeams.contains(teamMap.get(learner_now.action())))
                numInstructions += teamMap.get(learner_now.action()).policyInstructions(teamMap, visitedTeams, effective);
        }
        return numInstructions;
    }


    public void getBehaviourSequence(Vector<Integer> s, int phase){
        Vector <Double> singleEpisodeBehaviour;

        Set<sbbPoint> keyset = _outcomes.keySet();
        for(sbbPoint sbbpoint : keyset){
            if(sbbpoint.phase() == phase){
                singleEpisodeBehaviour = (Vector<Double>) sbbpoint.behaviouralState().clone();
                s.addAll((Vector<Integer>) singleEpisodeBehaviour.clone());
            }
        }
    }

    public boolean getMargin(sbbPoint pt, double[] margin){
        if(!_margins.containsKey(pt))return false;
        margin[0] = _margins.get(pt);
        return true;
    }


    public double getMaxOutcome(int i,int phase){
        double maxOut = 0;
        Set<sbbPoint> keyset = _outcomes.keySet();
        for(sbbPoint sbbpoint : keyset){
            if(sbbpoint.phase() == phase && sbbpoint.auxDouble(i) > maxOut)
                maxOut = sbbpoint.auxDouble(i);
        }
        return maxOut;
    }

    public double getMinOutcome(int i,int phase){
        double minOut = Double.MIN_VALUE;
        Set<sbbPoint> keyset = _outcomes.keySet();
        for(sbbPoint sbbpoint : keyset){
            if(sbbpoint.phase() == phase && sbbpoint.auxDouble(i) < minOut)
                minOut = sbbpoint.auxDouble(i);
        }
        return minOut;
    }


    public double getMeanOutcome(int i,int phase, int topPortion){
        Vector<Double> outcomes = new Vector<Double>();
        double sum = 0;
        if(_outcomes.size() == 0 )return sum;

        Set<sbbPoint> keyset = _outcomes.keySet();
        for(sbbPoint sbbpoint : keyset){
            if(sbbpoint.phase() == phase)
                outcomes.add(sbbpoint.auxDouble(i));
        }
        Collections.sort(outcomes,Collections.reverseOrder());
        for(int j = 0 ;j < (int)topPortion * outcomes.size();j++){
            sum += outcomes.get(j);
        }
        return sum / (int) (topPortion * outcomes.size());
    }

    public double getMedianOutcom(int i,int phase){
        Vector<Double> outcomes = new Vector<Double>();
        if(_outcomes.size() == 0) return 0;
        Set<sbbPoint> keyset = _outcomes.keySet();
        for(sbbPoint sbbpoint : keyset){
            if(sbbpoint.phase() == phase)
                outcomes.add(sbbpoint.auxDouble(i));
        }
        return sbbMist.vecMedian(outcomes);
    }

    public boolean getOutcome(sbbPoint pt, double[] out){
        if(_outcomes.containsKey(pt)){
            out[0] = _outcomes.get(pt);
            return true;
        }else return false;
    }

    public boolean hasOutcome(sbbPoint pt){
        if(_outcomes.containsKey(pt))return true;
        return false;
    }

    /* Calculate normalized compression distance w.r.t another team. */
    public double ncdBehaviouralDistance(sbbTeam t, int phase){
        Vector<Integer> theirBehaviourSequence = new Vector<Integer>();
        t.getBehaviourSequence(theirBehaviourSequence, phase);
        Vector<Integer> myBehaviourSequence = new Vector<Integer>();
        getBehaviourSequence(myBehaviourSequence, phase);
        if(myBehaviourSequence.size() == 0 || theirBehaviourSequence.size() == 0)
            return -1;
        return sbbMist.normalizedCompressionDistance(myBehaviourSequence, theirBehaviourSequence);
    }

    public int numAtomic(){
        int numAtomic = 0;
        Iterator iter = _members.iterator();
        while(iter.hasNext()){
            sbbLearner learner = (sbbLearner) iter.next();
            if(learner.action() < 0) numAtomic++;
        }
        return numAtomic;
    }

    public long numOutcomes(int phase){
        long numOut = 0;
        Set<sbbPoint> keyset = _outcomes.keySet();
        for(sbbPoint sbbpoint : keyset){
            if(sbbpoint.phase() == phase)numOut++;
        }
        return numOut;
    }

    public void outcomes(int i,int phase, Vector<Double> outcomes){
        Set<sbbPoint> keyset = _outcomes.keySet();
        for(sbbPoint sbbpoint : keyset){
            if(sbbpoint.phase() == phase)
                outcomes.add(sbbpoint.auxDouble(i));
        }
    }

    public void outcomes(TreeMap<sbbPoint, Double> outcomes, int phase){
        outcomes = (TreeMap<sbbPoint, Double>) _outcomes.clone();
        Set<sbbPoint> keyset = _outcomes.keySet();
        for(sbbPoint sbbpoint : keyset){
            if(sbbpoint.phase() == phase)
                outcomes.put(sbbpoint, _outcomes.get(sbbpoint));
        }
    }

    /*
    public StringBuilder printBids(String prefix){
        Iterator iter = _members.iterator();
        while(iter.hasNext()){
            sbbLearner learner = (sbbLearner) iter.next();


        }
    }*/

    public void removeLearner(sbbLearner lr){
        if(!_members.contains(lr)){

        }
        _members.remove(lr);
        _active.remove(lr);
    }

    public void resetOutcomes(int phase){
        Set<sbbPoint> keyset = _outcomes.keySet();
        for(sbbPoint sbbpoint : keyset){
            if(sbbpoint.phase() == phase)
                _outcomes.remove(sbbpoint);
        }
    }

    public void setMargins(sbbPoint pt, double margin){
        _margins.put(pt, margin);
    }


    public void setOutcome(sbbPoint pt, double out, long nso){
        _outcomes.put(pt, out);
        if(numOutcomes(pt.phase()) > nso){
            Set<sbbPoint> keyset = _outcomes.keySet();
            for(sbbPoint sbbpoint : keyset) {
                if(sbbpoint.phase() == pt.phase()) {
                    _outcomes.remove(sbbpoint);
                    break;
                }
            }
        }
    }

    public void updateActiveMembersFromIds(Vector<Long> activeMemberIds){
        Collections.sort(activeMemberIds);
        Iterator iter = _members.iterator();
        while(iter.hasNext()){
            sbbLearner learner = (sbbLearner) iter.next();
            if(activeMemberIds.contains(learner.id()))
                _active.add(learner);
        }
    }


    public Vector<Long> union(Vector<Long> list1, Vector<Long> list2){


        Vector<Long> ans = new Vector<Long>(list1);
        ans.removeAll(list2);
        ans.addAll(list2);
        return ans;
    }
    public Vector<Long> intersection(Vector<Long> list1, Vector<Long> list2) {
        Vector<Long> ans = new Vector<Long>(list1);

        ans.retainAll(list2);
        return ans;
    }
    public int domBy() {return _domBy;}
    public void domBy(int d){_domBy = d;}
    public int domOf() {return _domOf;}
    public void domOf(int d){_domOf = d;}
   /*
   void features(set < long > &) const;
   double symbiontUtilityDistance(team*, long);
   double symbiontUtilityDistance(Vector < long > &, long);
   void getAllNodes(map <long, team*> &teamMap, set <team *> &, set <sbbLearner *> &);
   int getAction(Vector < double > &,map <long, team*> &teamMap, boolean, set <sbbLearner *, LearnerBidLexicalCompare> &, long &, set <team*> &, boolean);*/

    public Vector<Long> getAllAncestors() { return _ancestors; }
    // void getBehaviourSequence(Vector <int> &, int);
    public double fit(){ return _fit; }
    public void fit(double f) { _fit = f; }
    //boolean getMargin(point *, double *); /* Get margin, return true if found. */
    // double getMaxOutcome(int,int);
    // double getMeanOutcome(int,int,double);
    // double getMedianOutcome(int,int);
    // double getMinOutcome(int,int);
    // boolean getOutcome(point *, double *); /* Get outcome, return true if found. */
    public long gtime() { return _gtime; }
    // boolean hasOutcome(point * p); /* Return true if team has an outcome for this point. */
    public long id() { return _id; }
    public   void id(long id) { _id = id; }
    double key() { return _key; }
    public void key(double key) { _key = key; }
    public int lastCompareFactor() { return _lastCompareFactor; }
    public void lastCompareFactor(int c) { _lastCompareFactor = c; }
    public TreeSet< sbbLearner> members() {
        return _members;
    }
    public   int nodes() { return _nodes; } /* This is the number of nodes at all levels, assuming this team is the root. */
    public   long numMargins() { return _margins.size(); } /* Number of margins. */
    public double parentFitness(){ return _parentFitness; }
    public void parentFitness(double f) { _parentFitness = f; }
    public   double parentNovelty(){ return _parentNovelty; }
    public   void parentNovelty(double n) { _parentNovelty = n; }
    public double parentScore(){ return _parentScore; }
    public   void parentScore(double n) { _parentScore = n; }
    // int policyFeatures(map <long, team*> &teamMap, set <team*> &, set < long > &); //populates set with features and returns number of nodes(learners) in policy
    //void resetOutcomes(int); /* Delete all outcomes from phase. */
    public boolean root(){ return _root; }
    public void root(boolean r){ _root = r;}
    //void setMargin(point *, double); /* Set margin. */
    public double score(){ return _score; }
    public void score(double f) { _score = f; }
    // void setOutcome(point *, double, long); /* Set outcome. */
    public long size() { return _members.size(); } /* This is the team size of the root node. */
    public long tmpNumOutcomes() { return _tmpNumOutcomes; }
    public void tmpNumOutcomes(long i) {_tmpNumOutcomes = i; }

    public static class teamIdComp implements Comparator<sbbTeam>{
        @Override
        public int compare(sbbTeam o1, sbbTeam o2) {
            return (int) (o1.id() - o2.id());
        }
    }

    public static class teamScoreLexicalCompare implements Comparator<sbbTeam> {
        @Override
        public int compare(sbbTeam t1, sbbTeam t2) {
            //      set < long > policyFeaturesT1;
            //      set <team*> teams;
            //      int pf_t1 = t1->policyFeatures(teams,policyFeaturesT1);
            //      set < long > policyFeaturesT2;
            //      teams.clear();
            //      int pf_t2 = t2->policyFeatures(teams,policyFeaturesT2);
            if (t1.score() != t2.score()){
                /*************************************** to be done here, can't change the original t1 & t2 ****************/
                t1.lastCompareFactor(0);
                t2.lastCompareFactor(0);
                /***************************************return double to int might be wrong **********************/
                return (int) (t1.score() - t2.score());
            } //score, higher is better
            else if (t1.asize() != t2.asize()){
                t1.lastCompareFactor(1);
                t2.lastCompareFactor(1);
                return t1.asize() - t2.asize();
            } //team size post intron removal (active members), smaller is better
            //else if (pf_t1 != pf_t2){ t1->lastCompareFactor(3); t2->lastCompareFactor(3); return pf_t1 < pf_t2;} //total number of learners (nodes), less is better
            //else if (policyFeaturesT1.size()  != policyFeaturesT2.size()){ t1->lastCompareFactor(4); t2->lastCompareFactor(4); return policyFeaturesT1.size() < policyFeaturesT2.size();} //total number of features, less is better
            else if (t1.gtime() != t2.gtime()){
                t1.lastCompareFactor(5);
                t2.lastCompareFactor(5);
                return (int) (t1.gtime() - t2.gtime());
            } //age, younger is better
            else {
                t1.lastCompareFactor(6);
                t2.lastCompareFactor(6);
                return (int) (t1.id() - t2.id());
            } //correlated to age but technically arbirary, id is guaranteed to be unique and thus ensures deterministic comparison
        }
    }


}

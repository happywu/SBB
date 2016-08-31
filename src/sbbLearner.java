import java.util.*;

import static java.lang.StrictMath.*;

/**
 * Created by happywu on 16/08/16.
 */
public class sbbLearner {
    public static class instruction extends BitSet{
        public BitSet bits;
        public instruction(){
            bits = new BitSet(23);
            for(int i=0;i<23;i++)
                bits.set(i,false);
        }
        public instruction(instruction ins){
            bits = new BitSet(23);
            for(int i=0;i<23;i++)
                if(ins.bits.get(i))
                    bits.set(i);
        }
        public instruction(long num){
            bits = new BitSet(23);
            bits = BitSet.valueOf(new long[]{num});
            for(int i=bits.length();i<23;i++)
                bits.set(i,false);
        }
        public void LeftShift(int shiftnum){
            BitSet ans = new BitSet(23);
            for(int i=shiftnum;i<23;i++){
                if(i-shiftnum<bits.length()&&bits.get(i-shiftnum)==true)
                    ans.set(i);
            }
            bits = (BitSet) ans.clone();
            for(int i=bits.length();i<23;i++)
                bits.set(i,false);
        }
        public void RightShift(int shiftnum){
            if(shiftnum>=bits.length())bits = BitSet.valueOf(new long[]{0});
            else bits = bits.get(shiftnum,bits.length());
            for(int i=bits.length();i<23;i++)
                bits.set(i,false);
        }

        public BitSet Complement() {
            BitSet ans = new BitSet(23);
            for(int i=0;i<23;i++)
                if(i>=bits.length()||bits.get(i)==false){
                    ans.set(i);
                }
            for(int i=ans.length();i<23;i++)
                ans.set(i,false);
            return ans;
        }
        public boolean equals(instruction b){
            for(int i=0;i<23;i++)
                if(bits.get(i)!=b.bits.get(i))return false;
            return true;
        }
        public String toString(){
            String ans = "";
            for(int i=22;i>=0;i--)
                if(bits.get(i))ans+="1";
            else ans+="0";
            return ans;
        }
    }

    /* 000 0000 0000 0000 0000 0001 */
    public final instruction _modeMask = new instruction(0x1);
    /* 000 0000 0000 0000 0000 1110 */
    public final instruction _opMask = new instruction(0xE);
    /* 000 0000 0000 0000 0111 0000 */
    public final instruction _dstMask = new instruction(0x70);
    /* 111 1111 1111 1111 1000 0000 */
    public final instruction _srcMask = new instruction(0x7FFF80);
    /* Rx <- op Rx Ry */
    public final instruction _mode0 = new instruction(0x0);
    /* Rx <- op Rx Iy */
    public final instruction _mode1 = new instruction(0x1);


    public final instruction _opSum = new instruction(0x0);
    public final instruction _opDiff = new instruction(0x1);
    public final instruction _opProd = new instruction(0x2);
    public final instruction _opDiv = new instruction(0x3);
    public final instruction _opCos = new instruction(0x4);
    public final instruction _opLog = new instruction(0x5);
    public final instruction _opExp = new instruction(0x6);
    public final instruction _opCond = new instruction(0x7);

    public final int _modeShift = 0;
    public final int _opShift = _modeMask.bits.cardinality();
    public final int _dstShift =  (_modeMask.bits.cardinality() + _opMask.bits.cardinality());
    public final int _srcShift = _modeMask.bits.cardinality() + _opMask.bits.cardinality() + _dstMask.bits.cardinality();

    private static long _count = 0; /* Next id to use. */

    public static final int REGISTERS = 8; /* Should match _dstMask. */
    /* Used to define unique bidding behaviour. */
    public static final int PROFILE_SIZE_INI = 50; /* Compare this many bid values. */
    public static final int PROFILE_SIZE = 50;
    public static final double BID_EPSILON = 1e-5; /* Using this equality threshold. */


    //typedef bitset < 16+3+3+1>instruction;

    private long _action; /* Action index. */
    private long _ancestral_gtime;
    Vector<instruction> _bid = new Vector<instruction>(); /* Bid program. */
    private double _bidVal; /* Most recent bid value. */
    private long _dim; /* Expected dimension of input feature vector. */
    private int _esize; /* Size of program not counting structural introns identified by markIntrons(). */
    private TreeSet<Long> _features = new TreeSet<Long>(); /* Features indexed by non-introns in this learner, determined in markIntrons(). */
    private boolean _frozen; /* If true, never modify this learner. */
    private long _gtime; /* Time step at which generated. */
    private long _id; /* Unique id of learner. */
    /* Mark structural introns (set to TRUE if NOT an intron).
       For example, _introns[0] is TRUE if the first instruction is effective. */
    private Vector<Boolean> _introns = new Vector<Boolean>();
    private double _key;
    private int _lastCompareFactor;
    private boolean _lifer; /* If true, never remove this learner from a team. */
    private long _nrefs; /* Number of references by teams. */
    private Vector < Double > _profile = new Vector<Double>(); /* Bid profile. */

    private Double[] _REG = new Double[REGISTERS];
    public sbbLearner(sbbLearner learner){
        _action = learner.action();
        _ancestral_gtime = learner.ancestralGtime();
        _bidVal = learner.bidVal();
        _dim = learner.dim();
        _frozen = learner.frozen();
        _gtime = learner.gtime();
        _id = learner.id();
        _key = learner.key();
        _nrefs = learner.refs();
        _esize = learner.esize();
        _lifer = learner.lifer();
        _lastCompareFactor = learner.lastCompareFactor();

        _introns.clear();
        for(boolean key: learner._introns){
            _introns.add(key);
        }

        _profile.clear();;
        for(double key : learner._profile){
            _profile.add(key);
        }

        _features.clear();
        for(Long key : learner._features){
            _features.add(Long.valueOf(key));
        }

        _bid.clear();
        for(instruction key : learner._bid){
            _bid.add(new instruction(key));
        }

        for(int i=0;i<learner._REG.length;i++)
            _REG[i] = learner._REG[i];
    }
    public sbbLearner(long gtime, long action, long maxProgSize, long dim, long id){
        _action = action;

        _ancestral_gtime = gtime;
        _dim = dim;
        _frozen = false;
        _gtime = gtime;
        _id = id;
        _key = 0;
        _nrefs = 0;


        Arrays.fill(_REG,0.0);
        Random rand = new Random();

        int progSize = 1 + ((int) (rand.nextDouble() * maxProgSize));

        for(int i = 0; i < progSize; i++)
        {
            instruction in = new instruction();

            for(int j = 0; j < in.bits.length(); j++)
                if(rand.nextDouble() < 0.5) in.bits.flip(j);

            _bid.add(in);
        }

        markIntrons(_bid);
    }

    public sbbLearner(long gtime, sbbLearner plr, long id){
        _action = plr.action();
        _ancestral_gtime = plr.ancestralGtime();
        _dim = plr._dim;
        _frozen = plr.frozen();
        _gtime = gtime;
        _id = id;
        _nrefs = 0;
        _key = 0;

        _bid.clear();
        Arrays.fill(_REG,0.0);

        Iterator iter = plr._bid.iterator();
        while(iter.hasNext()){
            instruction ins = (instruction) iter.next();
            _bid.add(new instruction(ins));
        }

        markIntrons(_bid);

    }

    public sbbLearner(long gtime, long ancestral_gtime, long action, long dim, long id, long nrefs, Vector<instruction> bid){
        _action= action;
        _ancestral_gtime = ancestral_gtime;
        _bid.clear();
        for(instruction ins : bid){
            _bid.add(new instruction(ins));
        }
        //_bid = (Vector<instruction>) bid.clone();
        _dim= dim;
        _frozen = false;
        _gtime = gtime;
        _id = id;
        _key = 0;
        _nrefs = nrefs;

        Arrays.fill(_REG,0.0);
        markIntrons(_bid);
    }


    public void markIntrons(Vector<instruction> prog){
        ArrayList<instruction> arraylist = new ArrayList<instruction>(prog);

        ListIterator riter = arraylist.listIterator(arraylist.size());

        instruction mode;
        instruction op;

        int reg;
        long feat;

        BitSet target = new BitSet(REGISTERS);

        _introns.clear();
        _esize = 0;
        _features.clear();


   /* Mark the first register. */
        target.clear();
        target.set(0);

       // System.out.println("Size: "  + prog.size());
        instruction prog_now = new instruction();
        while(riter.hasPrevious()){ /* From last to first instruction */
            prog_now = (instruction) riter.previous();

            /* Get destination register */
            instruction tmp = new instruction(prog_now);
          //  System.out.println("now: " + tmp.bits.toString() + _dstMask.bits.toString() + " " + _dstShift);

            tmp.bits.and((BitSet) _dstMask.bits.clone());

            tmp.RightShift(_dstShift);
            if(tmp.bits.length()>0)
                reg = (int) tmp.bits.toLongArray()[0];
            else reg = 0;

          //  System.out.println("now: " + tmp.bits.toString() + _dstMask.bits.toString() + " " + _dstShift + " " + reg +  " " + target.get(reg));
            if(target.get(reg) == true){ /* Destination register is a target */
                _introns.add(true);
                _esize ++;

               // op = (instruction) prog_now.clone();
                op = new instruction(prog_now);
                op.bits.and((BitSet) _opMask.bits.clone());
                op.RightShift(_opShift);

               // System.out.println("op:" +op.bits.toString());
                /* If the operation is unary, remove destination register from target set. */
                if(op.equals(_opCos)||op.equals(_opLog)||op.equals(_opExp)) {
                    target.set(reg, false);
                }

                //mode = (instruction) prog_now.clone();
                mode = new instruction(prog_now);
                mode.bits.and((BitSet) _modeMask.bits.clone());
                mode.RightShift(_modeShift);
              //  System.out.println("mode:" +mode.bits.toString());

                if(mode.equals(_mode0)){/* Rx <- op Rx Ry, need to target Ry. */


                    tmp = new instruction(prog_now);
                    tmp.bits.and((BitSet) _srcMask.bits.clone());
                    tmp.RightShift(_srcShift);
                    if(tmp.bits.length()>0)
                        reg = (int) tmp.bits.toLongArray()[0] % REGISTERS;
                    else reg = 0;

               //     System.out.println("reg:" +reg);
                    target.set(reg, true);
                }else { /* Rx <- op Rx Iy, get feature index y. */
                    tmp = new instruction(prog_now);
                    //tmp = (instruction) prog_now.clone();
                    tmp.bits.and((BitSet) _srcMask.bits.clone());
                    tmp.RightShift(_srcShift);
                    feat = tmp.bits.toLongArray()[0] % _dim;
                    _features.add(feat);
                }
            }else{ /* Not a target, mark as intron. */
                _introns.add(false);
            }
        }
        Collections.reverse(_introns);
        /*
        Iterator iter = prog.iterator();
        int cnt =0;
        while(iter.hasNext()){
            instruction ins = (instruction) iter.next();
            System.out.println("sbbLearner mark: " +ins.toString() + " " + _introns.get(cnt++));
        }*/
     //   System.out.println("sbblearner mark: " + _esize + _bid.get(0).bits.toString());

    }

    public boolean muBid(double pBidDelete, double pBidAdd, double pBidSwap, double pBidMutate, long maxProgSize){
        boolean changed = false;
        Random rand = new Random();
        /* Remove random instruction. */
        if(_bid.size() > 1 && rand.nextDouble() < pBidDelete)
        {
            int i = (int) (rand.nextDouble() * _bid.size());

            _bid.remove(i);

            changed = true;

        }

        /* Insert random instruction. */
        if(_bid.size() < maxProgSize && rand.nextDouble() < pBidAdd)
        {
            instruction instr = new instruction();

            for(int j = 0; j < instr.bits.length(); j++)
                if(rand.nextDouble() < 0.5) instr.bits.flip(j);

            int i = (int) (rand.nextDouble() * (_bid.size() + 1));

            _bid.add(i, instr);

            changed = true;
        }



        /* Flip single bit of random instruction. */
        if(rand.nextDouble() < pBidMutate)
        {
            int i = (int) (rand.nextDouble() * _bid.size());
            int j = (int) (rand.nextDouble() * _bid.get(0).length());

            _bid.get(i).bits.flip(j);

            changed = true;
        }

        /* Swap positions of two instructions. */
        if(_bid.size() > 1 && rand.nextDouble() < pBidSwap)
        {
            int i = (int) (rand.nextDouble() * _bid.size());

            int j;

            do{
                j = (int) (rand.nextDouble() * _bid.size());
            } while(i == j);

            instruction tmp;

            tmp = _bid.get(i);
            _bid.set(i, _bid.get(j));
            _bid.set(j, tmp);

            changed = true;
        }

        if(changed == true) /* need to mark introns if a change has occured. */
            markIntrons(_bid);

        return changed;
    }

    // to be done
    public String printBid(String prefix){
        return null;
    }


    public double bid(Vector<Double> feature, boolean useMemory){
        if(!useMemory)
            Arrays.fill(_REG,0.0);
        return run(_bid, feature);
    }

    public String checkpoint(){
        // to be done
        return null;
    }



    public double run(Vector<instruction> prog, Vector<Double> feature){
        Iterator iter = prog.iterator();


        instruction mode;
        instruction op;

        int dstReg;
        double srcVal;

      //  System.out.println("sbblearner : run: before" + _REG[0]);
        int k = 0;
        while(iter.hasNext()){

            if(_introns.get(k) == false){
                iter.next();
                k++;
                continue;
            }

            instruction prog_now = (instruction) iter.next();
          //  System.out.println("sbblearner run: BEGEIN: "+prog_now.toString() + " " + _introns.get(k));
            //System.out.println("BEGEIN: "+prog_now.bits.toString());
            mode = new instruction(prog_now);
            //mode = (instruction) prog_now.clone();
            mode.bits.and(_modeMask.bits);
            mode.RightShift(_modeShift);

            op = new instruction(prog_now);
           // op = (instruction) prog_now.clone();
            op.bits.and(_opMask.bits);
            op.RightShift(_opShift);


            instruction tmp = new instruction(prog_now);
           // instruction tmp = (instruction) prog_now.clone();
            tmp.bits.and(_dstMask.bits);
            tmp.RightShift(_dstShift);
            if(tmp.bits.length()>0)
                dstReg = (int) tmp.bits.toLongArray()[0];
            else dstReg = 0;
            //System.out.println("des:"+dstReg);

            tmp = new instruction(prog_now);
            //tmp = (instruction) prog_now.clone();
            tmp.bits.and(_srcMask.bits);
            tmp.RightShift(_srcShift);
            if(mode.equals(_mode0))
                if(tmp.bits.length()>0)
                    srcVal = _REG[(int)tmp.bits.toLongArray()[0]%REGISTERS];
                else srcVal = _REG[0];
            else {
                if(tmp.bits.length()>0)
                srcVal = feature.get((int) ((int) tmp.bits.toLongArray()[0] % _dim));
                else srcVal = feature.get(0);
            }

           // System.out.println("op:"+op.toString());
            if(op.equals(_opSum)) {
                _REG[dstReg] = _REG[dstReg] + srcVal;
            }else if(op.equals(_opDiff)){
                _REG[dstReg] = _REG[dstReg] - srcVal;
            }else if(op.equals(_opProd)){
                _REG[dstReg] = _REG[dstReg] * srcVal;
            }else if(op.equals(_opDiv)){
                if(srcVal!=0)
                _REG[dstReg] = _REG[dstReg] / srcVal;
            }else if(op.equals(_opCos)){
                _REG[dstReg] = cos(srcVal);
            }else if(op.equals(_opLog)){
                _REG[dstReg] = log(abs(srcVal));
            }else if(op.equals(_opExp)){
                _REG[dstReg] = exp(srcVal);
            }else if(op.equals(_opCond)){
                if(_REG[dstReg] < srcVal)
                    _REG[dstReg] = - _REG[dstReg];
            }else{
                // output method to be done
            }

            if(_REG[dstReg].isInfinite()){
                _REG[dstReg] = Double.valueOf(0);
            }

         //   System.out.println("sbblearner : run : " + dstReg + " " + srcVal + " " + mode.toString() + " " + tmp.toString() + " " + op.toString() + " " + _REG[0]);
            k++;
        }

       // System.out.println("bidval:" + _REG[0]);
       // System.out.println("sbblearner : run: end" + _REG[0]  );
        return _REG[0];
    }

    public void setFeature(instruction ins, long feat){
        instruction feature = new instruction(feat);

        ins.bits.and(_srcMask.Complement());

        feature.LeftShift(_srcShift);

        ins.bits.or(feature.bits);
    }



    public TreeSet<Long> features(){
        return _features;
    }
    public long action() { return _action; }
    public void action(int a) { _action = a; }
    public long ancestralGtime() { return _ancestral_gtime; }
    //double bid(double *, bool);
    public double bidVal(){ return _bidVal; }
    public void bidVal(double b){ _bidVal = b; }
    // String checkpoint(){};
    public long dim() { return _dim; }
    public void dim(long d) { _dim = d; }
    public int esize() { return _esize; } /* Not counting introns. */
    //void features(Set < Long > F) { F.add(_features.begin(), _features.end()); }
    public boolean frozen() { return _frozen; }
    public void frozen(boolean f) { _frozen = f; }
    public Vector<instruction> getBid(){
        return  _bid;
    }
    public Vector<Double> getProfile() { return  _profile; }
    public long gtime() { return _gtime; }
    public long id() { return _id; }
    public void id(long i) { _id = i; }
    public boolean isProfileEqualTo(Vector < Double > p) {
        /**************** to be done */
        //return isEqual(_profile, p, BID_EPSILON);
        return false;
    }
    public double key(){ return _key; }
    public void key(double key) { _key = key; }
    public int lastCompareFactor() { return _lastCompareFactor; }
    public void lastCompareFactor(int c) { _lastCompareFactor = c; }
    /*
   // learner(long, long, int, long, long); /* Create arbitrary learner. */
    //learner(long, learner &, long); /* Create learner from another learner. */
    // learner(long, long, long, long, long, long, vector < instruction * >); /* Create learner from checkpoint file. */
    //  ~learner();
    public boolean lifer() { return _lifer; }
    public void lifer(boolean l) { _lifer = l; }
    /* Mutate action, return true if the action was actually changed. */
    public boolean muAction(long action) { long a = _action; _action = action; return a != action; }
    //public boolean muBid(double, double, double, double, size_t); /* Mutate bid, return true if any changes occured. */
    public long numFeatures() { return _features.size(); } /* Not counting introns. */
    //String printBid(String);
    public void setId(long id) { _id = id; }
    public void setNrefs(int nrefs) { _nrefs = nrefs; }
    public void setProfile(Vector < Double > p) {
        _profile = p;
    }
    public int size() { return _bid.size(); }
    public long refs() { return _nrefs; }
    public long refDec() { return --_nrefs; }
    public long refInc() { return ++_nrefs; }
    //  public void zeroRegisters() { memset(_REG, 0, REGISTERS * sizeof(double)); }

    // friend ostream & operator<<(ostream &, const learner &);
    public static class LearnerBidLexicalCompare implements Comparator<sbbLearner>{
        @Override
        public int compare(sbbLearner l1, sbbLearner l2) {
            //if(l1.bidVal() != l2.bidVal()){
            if (l1.key() != l2.key()){//(set to huge val or bid), higher is better
                l1.lastCompareFactor(0);
                l2.lastCompareFactor(0);
                return (int) (l2.key() - l1.key());
               // return (int) (l2.bidVal() - l1.bidVal());
            } else if (l1.esize() != l2.esize()){ //program size post intron removal, smaller is better (assumes markIntrons is up to date)
                l1.lastCompareFactor(1);
                l2.lastCompareFactor(1);
                return l1.esize() - l2.esize();
            } else if (l1.refs() != l2.refs()){//number of references, less is better
                l1.lastCompareFactor(2);
                l2.lastCompareFactor(2);
                return (int) (l1.refs() - l2.refs());
            } else if (l1.numFeatures() != l2.numFeatures()){ //number of features indexed, less is better
                l1.lastCompareFactor(3);
                l2.lastCompareFactor(3);
                return (int) (l1.numFeatures() - l2.numFeatures());
            } else if (l1.gtime() != l2.gtime()){//age, younger is better
                l1.lastCompareFactor(4);
                l2.lastCompareFactor(4);
                return (int) (l2.gtime() - l1.gtime());
            } else if (l1.ancestralGtime() != l2.ancestralGtime()){ //age of oldest ancestor, younger is better
                l1.lastCompareFactor(5);
                l2.lastCompareFactor(5);
                return (int) (l2.ancestralGtime() - l1.ancestralGtime());
            } else {//correlated to age but technically arbirary, id is guaranteed to be unique and thus ensures deterministic comparison
                l1.lastCompareFactor(6);
                l2.lastCompareFactor(6);
                return (int) (l1.id() - l2.id());
            }
        }
    };
}

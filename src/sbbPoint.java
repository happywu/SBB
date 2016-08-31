import java.util.Comparator;
import java.util.Iterator;
import java.util.TreeSet;
import java.util.Vector;

/**
 * Created by happywu on 18/08/16.
 */
public class sbbPoint {
    private Vector <Double> _auxDoubles = new Vector<Double>();
    private Vector< Double > _behaviouralState = new Vector<Double>();
    private long _gtime;
    private long _id;
    private double _key; /* For sorting. */
    private boolean _marked;
    private int _phase; /* Phase: TRAIN_MODE, VALIDATION_MODE, or TEST_MODE. */
    private Vector < Double > _pointState = new Vector<Double>();
    private double _slice; /* For roulette wheel. */
    private boolean _solved; /* If this point has been solved at some time. */


    public sbbPoint(sbbPoint B){
        _id = B.id();
        _gtime = B.gtime();
        _phase = B.phase();
        _key = B.key();
        _marked = B.marked();
        _solved = B.solved();
        _slice = B.slice();


        _auxDoubles.clear();;
        for(int i = 0;i<B._auxDoubles.size();i++)
            _auxDoubles.add(B._auxDoubles.get(i));

       _behaviouralState.clear();;
        for(int i = 0;i<B._behaviouralState.size();i++)
            _behaviouralState.add(B._behaviouralState.get(i));

        _pointState.clear();
        for(int i=0;i<B._pointState.size();i++)
           _pointState.add(B._pointState.get(i));

    }
    public boolean isPointUnique(TreeSet<sbbPoint> P, boolean pointState){
        Vector<Double> state = new Vector<Double>();
        Iterator iter = P.iterator();
        while(iter.hasNext()){
            sbbPoint point = (sbbPoint) iter.next();
            if(pointState) {
                point.pointState(state);
                return !sbbMist.isEqual(_pointState, state, 1e-5);
            }else{
                state = (Vector<Double>) point.behaviouralState().clone();
                return !sbbMist.isEqual(_behaviouralState, state, 1e-5);
            }
        }
        return true;
    }
    public sbbPoint(long gtime, int phase, long id, Vector<Double> bState, Vector<Double> auxDoubles) {
        _id = id;
        _gtime = gtime;
        _phase = phase;
        _key = 0;
        _marked = false;
        _solved = false;
        _slice = 0;
        _pointState.clear();
        for(int i=0;i<bState.size();i++)
            _behaviouralState.add(bState.get(i));

        for(int i=0;i<3;i++)
            _auxDoubles.add(auxDoubles.get(i));
    }

    public sbbPoint(long gtime, int phase, long id, Vector<Double> pState, Vector<Double> bState, Vector<Double> auxDoubles){
        _id = id;
        _gtime = gtime;
        _phase = phase;
        _key = 0;
        _marked = false;
        _solved = false;
        _slice = 0;
        _pointState.clear();
        for(int i=0;i<pState.size();i++)
            _pointState.add(pState.get(i));
        for(int i=0;i<bState.size();i++)
            _behaviouralState.add(bState.get(i));

        for(int i=0;i<3;i++)
            _auxDoubles.add(auxDoubles.get(i));
    }
    public sbbPoint(long gtime, int phase, long id, Vector<Double> pState){
        _id = id;
        _gtime = gtime;
        _phase = phase;
        _key = 0;
        _marked = false;
        _solved = false;
        _slice = 0;
        _pointState.clear();
        for(int i=0;i<pState.size();i++)
            _pointState.add(pState.get(i));
        for(int i=0;i<3;i++)
            _auxDoubles.add(0.0);
    }

    public void setBehaviouralState(Vector<Double> bState){
        _behaviouralState.clear();
        for(int i=0;i<bState.size();i++)
            _behaviouralState.add(bState.get(i));
    }
    public double auxDouble(int i){ return _auxDoubles.get(i); }
    public void auxDouble(int i, double d) { _auxDoubles.set(i,d); }
    public Vector<Double> behaviouralState(){ return _behaviouralState; }
    //string checkPoint();
    public int dimPoint(){ return _pointState.size(); }
    public int dimBehavioural(){ return _behaviouralState.size(); }
    public long gtime(){ return _gtime; }
    public long id(){ return _id; }
    //	public boolean isPointUnique(Set < sbbPoint > , boolean); /* Return true if the point is unique w.r.t. another point. */
    public double key(){ return _key; }
    public void key(double key){ _key = key; }
    public void mark(){ _marked = true; }
    public boolean marked(){ return _marked; }
    public int phase(){ return _phase; }
    public void phase(int p){ _phase = p; }
	/*
	public sbbPoint(long,  int,   long,  vector< double > &,  vector< double > &, vector < double > &);
	public sbbPoint(long,  int,   long,  vector< double > &,  vector< double > &);
	public point(long,  int,   long,  vector< double > &);*/

    public void pointState(Vector < Double > v){ v = (Vector<Double>) _pointState.clone(); }
    //	public void setBehaviouralState(vector < double > &);
    public double slice(){ return _slice; }
    public void slice(double slice){ _slice = slice; }
    public boolean solved(){ return _solved; }
    public void solved(boolean s){ _solved = s; }

    public static class pointLexicalLessThan implements Comparator<sbbPoint>{
        @Override
        public int compare(sbbPoint o1, sbbPoint o2) {
            return (int) (o1.id() - o2.id());
        }
    }

}

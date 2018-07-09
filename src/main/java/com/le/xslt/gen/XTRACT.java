package com.le.xslt.gen;
import org.w3c.dom.*;
import java.util.regex.*;
import java.util.*;

public abstract class XTRACT 
{
    protected XSLTGen xg;
    protected int row, column;
    public boolean checkRoot = false;
    
    public String elemName;
    public String dtdElem;
    public String dtd;
    public String imp_dtd;
    protected boolean ROOT;

    public ArrayList symtable;
    public ArrayList symtype;
    public int init_char;
    public int new_sym;
    public Node[] symnode;

    protected ArrayList sequence;
    protected ArrayList sg;
    protected ArrayList sf;
    
    public XTRACT(int column, boolean ROOT, XSLTGen xg, String elemName) {
        this.xg       = xg;
        this.column   = column;
        this.elemName = elemName;
        this.ROOT     = ROOT;
        
        row = xg.xmldom.get_NoTags();
        symtable = new ArrayList();
	symtype  = new ArrayList();
        symnode  = new Node[row + 50];
        sequence = new ArrayList();
        sg       = new ArrayList();
	sf       = new ArrayList();
        
        init_char = 'a';
    }

    public XTRACT(ArrayList seq, ArrayList sym) {
	this.sequence = seq;
	this.symtable = sym;

	symtype = new ArrayList();
	symnode = new Node[row + 50];
	sg      = new ArrayList();
	sf      = new ArrayList();

	init_char = 'a';

	new_sym = symtable.size();
 
        generalize();
	print_seq(sg);
//	print_seq(symtable); 
    
	System.out.println();

 	factorSubsets();
 	print_seq(sf);
 	print_seq(symtable);

	System.out.println();

        dtd = mdl();
    }
       
    public abstract void findAllSequences();
    
    public char getSymbol(String name, Node node) {
        int location = symtable.indexOf(name);
        
        if (location == -1) {
            symnode[symtable.size()] = node;
            symtable.add(name);
	    symtype.add("");

	    if (symtable.size() <= 26)
	        return ((char) (init_char + symtable.size() - 1));
	    else
		return ((char) (init_char + symtable.size() + 5));
        }
        else {
	    if (location < 26)
                return ((char) (init_char + location));
	    else
		return ((char) (init_char + location + 6));
	}
    }

    public char getSymbol(String name, Node node, String type) {
        int location = symtable.indexOf(name);
        
        if (location == -1) {
            symnode[symtable.size()] = node;
            symtable.add(name);
	    symtype.add(type);
                        
	    if (symtable.size() <= 26)
	        return ((char) (init_char + symtable.size() - 1));
	    else
		return ((char) (init_char + symtable.size() + 5));
        }
        else {
	    if (location < 26)
                return ((char) (init_char + location));
	    else
		return ((char) (init_char + location + 6));
	}
    }
    
    public void print_seq(ArrayList seq) {
        int i;
        
	System.out.println();
        System.out.print(dtdElem + " : ");
        
        for (i = 0; i < seq.size() - 1; i++)
            System.out.print(seq.get(i) + " , ");
        
        System.out.println(seq.get(i));
    }     
    
    public void generalize() {
        String s1, s2;
        int len_s1, d;
        
        double ds[] = new double[3];
        ds[0] = 0.1;
        ds[1] = 0.5;
        ds[2] = 1;

        for (int i = 0; i < sequence.size(); i++) {
            insert_sg((String) sequence.get(i));

            for (int r = 2; r <= 4; r++) {              
                s1 = discoverSeqPattern((String) sequence.get(i), r);          
                len_s1 = s1.length();
                
                for (int j = 0; j < ds.length; j++) {
                    d = (int) (ds[j]*len_s1);
                    s2 = discoverOrPattern(s1, d);
	            insert_sg(s2);                  
                }
            }
	}
    }
    
    private String discoverSeqPattern(String s, int r) {
        int i, j, k, len, repetition;
        boolean change;
        String seq, next;
        
        do {
            change = false;
            
            len = s.length();
            
            for (i = 1; i <= (len / r); i++) {
                for (j = 0; (j + i) < len; j++) {
                    seq = s.substring(j, j + i);
                    repetition = 1;
                                        
                    for (k = (j + i); (k + i) <= len; k += i) {
                        next = s.substring(k, k + i);
                        
                        if (seq.compareTo(next) == 0)
                            repetition++;
                        else
                            break;
                    }
                    
                    if (repetition >= r) {
                        change = true;
                        
                        s = replaceSeq(s, seq, j, repetition);
                        break;
                    }                   
                }
                
                if (change == true)
                    break;
            }
        } while (change == true);
        
        return s;
    }
    
    private String replaceSeq(String s, String seq, int start, int repetition) {
        String snew = s.substring(0, start);
        int seqLen = seq.length();                      
        String name;
        
        if (seqLen == 1)
            name = seq + "*";
        else
            name = "(" + seq + ")*"; 
            
        snew = snew + getSymbol(name, null);   

        int endStart = start + (seqLen * repetition);
        
        snew = snew + s.substring(endStart, s.length());
        
        return snew;
    }
    
    private String discoverOrPattern(String s, int d) {
        int m;
        
        String[] ss = partition(s, d);
        
        for (int j = 0; j < ss.length; j++) {
            if (ss[j] == null)
                break;
            
            String sdist = "";    
            for (int i = (ss[j].length() - 1); i >= 0; i--)
                if (ss[j].indexOf(ss[j].charAt(i)) == i)
                    sdist = ss[j].charAt(i) + sdist;
                
            m = sdist.length();

            if (m > 1) 
                ss[j] = replaceOr(sdist);
        }
        
        s = concatStrings(ss);
        
        return s;
    }
    
    private String[] partition(String s, int d) {
        String[] ss;
        int i, start, end, l;
        int sLen = s.length();
        
        ss  = new String[sLen];
        i   = start = 0;
        end = 1;
        ss[i] = s.substring(start, end);
        
        while (end < sLen) {
            while ((end < sLen) && 
                   (((l = findOccurrence(s, ss[i], end)) != -1) && 
                     (l <= d))) {
                end += l;
                ss[i] = s.substring(start, end);
            }   
        
            if (end < sLen) {
                i++;
                start = end;
                end++;
                ss[i] = s.substring(start, end);
            }
        }
        
        return ss;
    }
    
    private int findOccurrence(String s, String ss, int end) {
        String check;
        
        for (int i = end; i < s.length(); i++) {            
            if (ss.indexOf(s.charAt(i)) != -1)
                return (i - end + 1);
        }
        
        return -1;
    }
        
    private String replaceOr(String part) {
        String seq  = "(";
        int i;
        
        for (i = 0; i < (part.length() - 1); i++)
            seq = seq + part.charAt(i) + "|";
        seq = seq + part.charAt(i) + ")*";
            
        return java.lang.Character.toString(getSymbol(seq, null));    
    }       
        
    private String concatStrings(String[] ss) {
        String s = "";
        
        for (int j = 0; j < ss.length; j++) {
            if (ss[j] == null)
                break;
                
            s = s + ss[j];
        }
        
        return s;
    }
    
    private void insert_sg(String s) {
        if (!sg.contains(s))
            sg.add(s);
    }

    private void insert_sf(String s) {
	if (!sf.contains(s))
	    sf.add(s);
    }
    
    public void factorSubsets() {
	int i, j, index;
	String d, d1, f;
	ArrayList score_sg, score_s;
	ArrayList s1, seedSet, s;

	score_sg = computeScore(sg, sg);
//	print_seq(score_sg);

	s1 = new ArrayList();
	
	for (i = 0; i < sg.size(); i++) {
	    sf.add(sg.get(i));
	    s1.add(sg.get(i));
	}

	seedSet = new ArrayList();

	int k = sg.size() / 10;
	if (k == 0)
	    k = 1;

   	for (i = 1 ; i <= k && !s1.isEmpty(); i++) {
	    index = maxScore(score_sg);

	    d = (String) s1.get(index);
//	    System.out.println(d);
	   
	    s1.remove(index);
	    score_sg.remove(index);

	    seedSet.add(d);

	    for (j = 0; j < s1.size(); j++) {
//	    	System.out.print(overlap(d, (String) s1.get(j)) + ", ");
	        if (overlap(d, (String) s1.get(j)) > 0) {
		    s1.remove(j);
		    score_sg.remove(j);
		    j--;
		}
	    }
		
//	    if (!s1.isEmpty())
//	   	print_seq(s1);
	}

	s = new ArrayList();
	s1.clear();

	for (i = 0; i < seedSet.size(); i++) {
	    d = (String) seedSet.get(i);
//	    System.out.println("d = " + d);

	    s.clear();
	    s.add(d);
	    
	    for (j = 0; j < sg.size(); j++) {
//		System.out.print(overlap(d, (String) sg.get(j)) + ", ");
	        if (overlap(d, (String) sg.get(j)) <= 0)
		    s1.add(sg.get(j));	
	    }
	
//	    print_seq(s1);

	    while (!s1.isEmpty()) {
	        score_s = computeScore(s1, s);
//	        print_seq(score_s);

		index = maxScore(score_s);
		d1 = (String) s1.get(index);
//		System.out.println(d1);

		s1.remove(index);
		score_s.remove(index);

		s.add(d1);

		for (j = 0; j < s1.size(); j++) {
//		    System.out.print(overlap(d1, (String) s1.get(j)) + ", ");
		    if (overlap(d1, (String) s1.get(j)) > 0) {
			s1.remove(j);
			score_s.remove(j);
			j--;
		    }
		}

//		if (!s1.isEmpty())
//	  	    print_seq(s1);		
	    }

//	    print_seq(s);

	    f = factor(s);
//	    System.out.println(f + " - " + decodeSymbols(f));
            f = f.replace('|', ':');
            String[] fs = f.split(":");	    

	    for (j = 0; j < fs.length; j++) {
		if (fs[j].length() == 1)
		    fs[j] = (String) symtable.get(fs[j].charAt(0) - 'a');

		insert_sf(fs[j]);
	    }
	}
    }

    private ArrayList cover(String d) {
	d = decodeSymbols(d);
	ArrayList covered = new ArrayList();

	for (int i = 0; i < sequence.size(); i++) {
	    if (((String) sequence.get(i)).matches(d)) 
		covered.add(sequence.get(i));
	}

	return covered;
    }

    private double overlap(String d, String d1) {
	ArrayList cover_d  = cover(d),
		  cover_d1 = cover(d1);
	int len_d  = cover_d.size(),
	    len_d1 = cover_d1.size();
	int i, inter = 0, uni;

	for (i = 0; i < len_d; i++) {
	    if (cover_d1.contains(cover_d.get(i)))
		inter++;
	}

	uni = len_d + len_d1 - inter;

	return ((double) inter) / ((double) uni);
    }
	
    private ArrayList pref(String d) {
	int len = d.length();
	ArrayList prefix = new ArrayList(len);

	for (int i = 0; i < len; i++) 
	    prefix.add(i, d.substring(0, i + 1));

	return prefix;
    }

    private ArrayList suf(String d) {
	int len = d.length();
	ArrayList suffix = new ArrayList(len);

	for (int i = 0; i < len; i++) 
	    suffix.add(i, d.substring(len - i - 1, len));
	
	return suffix;
    }

    private int psup(String p, ArrayList s) {
	int count = 0;

	for (int i = 0; i < s.size(); i++) {
	    if (decodeSymbols((String) s.get(i)).startsWith(p))
		count++;
	}

	return count;
    }

    private int ssup(String suf, ArrayList s) {
	int count = 0;

	for (int i = 0; i < s.size(); i++) {
	    if (decodeSymbols((String) s.get(i)).endsWith(suf))
		count++;
	}

	return count;
    }

    private int calculateScore(String d, ArrayList s) {
	ArrayList prefix = pref(decodeSymbols(d)),
		  suffix = suf(decodeSymbols(d));
	int i, curr_score, max_score = 0;

	for (i = 0; i < prefix.size(); i++) {
	    curr_score = ((String) prefix.get(i)).length() * 
		psup((String) prefix.get(i), s);

	    if (curr_score > max_score)
		max_score = curr_score;
	}

	for (i = 0; i < suffix.size(); i++) {
	    curr_score = ((String) suffix.get(i)).length() * 
		ssup((String) suffix.get(i), s);

	    if (curr_score > max_score)
		max_score = curr_score;
	}
		
	return max_score;
    }

    private ArrayList computeScore(ArrayList ds, ArrayList s) {
	ArrayList score = new ArrayList(ds.size());

	for (int i = 0; i < ds.size(); i++)
	    score.add(i, new Integer(calculateScore((String) ds.get(i), s)));

	return score;
    }

    private int maxScore(ArrayList score) {
	int i, max = java.lang.Integer.MIN_VALUE, maxIndex = -1;

	for (i = 0; i < score.size(); i++) {
	    if (((Integer) score.get(i)).intValue() > max) {
		max = ((Integer) score.get(i)).intValue();
		maxIndex = i;
	    }
	}

	return maxIndex;
    }

    private int curr_set;

    public class DivisorList
    {
	ArrayList v, q, r;
	int v_size, q_size, r_size;
	int v_char, q_char, r_char;

	public DivisorList(ArrayList v, ArrayList q, ArrayList r) {
	    this.v = v;
	    this.q = q;
	    this.r = r;

	    v_size = v.size();
	    q_size = q.size();
	    r_size = r.size();

	    int i;
	    v_char = q_char = r_char = 0;

	    for (i = 0; i < v_size; i++) 
		v_char += ((String) v.get(i)).length();

	    for (i = 0; i < q_size; i++)
		q_char += ((String) q.get(i)).length();

	    for (i = 0; i < r_size; i++)
		r_char += ((String) r.get(i)).length();
	}
    }

    private String factor(ArrayList s) {
	int i, j, k;
	ArrayList[] divisorSet = findAllDivisors(s);
	String seq = "";

	if (curr_set == 0) {
//	    print_seq(s);

	    for (i = 0; i < s.size(); i++) {
		seq = seq + getSymbol((String) s.get(i), null);

		if (i < s.size() - 1)
		    seq = seq + "|";
	    }

//	    System.out.println(seq);

	    return seq;
	}

	DivisorList[] divisorList = new DivisorList[curr_set];
	ArrayList v;

	for (i = 0; i < curr_set; i++) {
	    v = divisorSet[i];
	    ArrayList q = divide(s, v);

	    ArrayList voq = new ArrayList();

	    for (j = 0; j < v.size(); j++) {
		for (k = 0; k < q.size(); k++) {
		    String v_str = (String) v.get(j);
		    if (v_str.compareTo("1") == 0)
			v_str = "";

		    String q_str = (String) q.get(k);
		    if (q_str.compareTo("1") == 0)
			q_str = "";

		    String vq = v_str + q_str;
		    voq.add(vq);
		}
	    }

	    ArrayList r = new ArrayList();

	    for (j = 0; j < s.size(); j++) {
		String curr_s = (String) s.get(j);

		if (!voq.contains(curr_s))
		    r.add(curr_s);
	    }
/*
	    print_seq(v);
	    print_seq(q);
	    print_seq(r);
	    System.out.println();
*/
	    divisorList[i] = new DivisorList(v, q, r);
	}

	Arrays.sort(divisorList, new Comparator() 
	{
	    public int compare(Object o1, Object o2) {
		DivisorList dl1 = (DivisorList) o1;
		DivisorList dl2 = (DivisorList) o2;

		if (dl1.v_size != dl2.v_size)
		    return (dl1.v_size - dl2.v_size);

		if (dl1.v_char != dl2.v_char)
		    return (dl1.v_char - dl2.v_char);

		if (dl1.q_size != dl2.q_size)
		    return (dl1.q_size - dl2.q_size);

		if (dl1.q_char != dl2.q_char)
		    return (dl1.q_char - dl2.q_char);

		if (dl1.r_size != dl2.r_size)
		    return (dl1.r_size - dl2.r_size);

		if (dl1.r_char != dl2.r_char)
		    return (dl1.r_char - dl2.r_char);
	
	        return 0;
	    }
	});

	String fact_v = factor(divisorList[0].v),
	       fact_q = factor(divisorList[0].q),
	       fact_r = factor(divisorList[0].r);

	if ((decodeSymbols(fact_r).compareTo("") != 0) && 
	    (decodeSymbols(fact_r).compareTo("1") != 0)) {
	    char sym_r = getSymbol(fact_r, null);
	    seq = sym_r + "|";
	}

	String vq = "";

	if ((decodeSymbols(fact_v).compareTo("") != 0) && 
	    (decodeSymbols(fact_v).compareTo("1") != 0)) {
	    char sym_v = getSymbol("(" + fact_v + ")", null);
	    vq = java.lang.Character.toString(sym_v);
	}

	if ((decodeSymbols(fact_q).compareTo("") != 0) && 
	    (decodeSymbols(fact_q).compareTo("1") != 0)) {
	    char sym_q = getSymbol("(" + fact_q + ")", null);
	    vq = vq + java.lang.Character.toString(sym_q);
	}

	vq = java.lang.Character.toString(getSymbol(vq, null));

	seq = seq + vq;
		
	return seq;
    }

    private int ssup_seq(String suf, ArrayList s) {
	int count = 0;

	for (int i = 0; i < s.size(); i++) {
	    if (((String) s.get(i)).endsWith(suf))
		count++;
	}

	return count;
    }

    private boolean divisorExist(ArrayList[] divisorSet, ArrayList v) {
	for (int i = 0; i < curr_set; i++) {
	    if (divisorSet[i].equals(v))
		return true;
	}

	return false;
    }

    private ArrayList[] findAllDivisors(ArrayList s) {
	int i, j;
	ArrayList suffix = new ArrayList(),
		  curr_suffix;

	for (i = 0; i < s.size(); i++) {
	    curr_suffix = suf((String) s.get(i));

	    for (j = 0; j < curr_suffix.size(); j++) {
		if ((!suffix.contains(curr_suffix.get(j))) &&
		    (ssup_seq((String) curr_suffix.get(j), s) >= 2))
		    suffix.add(curr_suffix.get(j));
	    }
	}

//	print_seq(s);
//	print_seq(suffix);
//	System.out.println(suffix.size());

	ArrayList[] divisorSet = new ArrayList[suffix.size()];
	curr_set = 0;

	for (i = 0; i < suffix.size(); i++) {
	    String suf  = (String) suffix.get(i);
	    int suf_len = suf.length();

	    ArrayList v = new ArrayList();

	    for (j = 0; j < s.size(); j++) {
		String ps  = (String) s.get(j);
		int ps_len = ps.length();

	        if (ps.endsWith(suf)) {
		    String p;

		    if (ps_len == suf_len)	
			p = "1";
		    else
		    	p = ps.substring(0, ps_len - suf_len);

		    if (!v.contains(p))
			v.add(p);
		}
	    }
	
//	    print_seq(v);

	    if (!divisorExist(divisorSet, v)) {
		divisorSet[curr_set++] = v;
//		print_seq(v);
	    }
	} 

//	System.out.println(curr_set);

	return divisorSet;
    }

    private ArrayList divide(ArrayList s, ArrayList v) {
	int i, j, p_len, ps_len;
	String p, ps;
	ArrayList[] qp    = new ArrayList[v.size()];
	ArrayList dist_qp = new ArrayList();

	for (i = 0; i < v.size(); i++) {
	    p     = (String) v.get(i);
	    p_len = p.length();

	    qp[i] = new ArrayList();

	    if (p.compareTo("1") == 0) {
		for (j = 0; j < s.size(); j++)
		    qp[i].add(s.get(j));
	    }
	    else {
	    	for (j = 0; j < s.size(); j++) {
		    ps     = (String) s.get(j);
		    ps_len = ps.length();

		    if (ps.startsWith(p))
			qp[i].add(ps.substring(p_len, ps_len));
		}
	    }

	    for (j = 0; j < qp[i].size(); j++) {
		if (!dist_qp.contains(qp[i].get(j)))
		    dist_qp.add(qp[i].get(j));
	    }
	}

	ArrayList q = new ArrayList();
	boolean inter;

	for (i = 0; i < dist_qp.size(); i++) {
	    inter = true;
	    String curr_s = (String) dist_qp.get(i);

	    for (j = 0; j < v.size(); j++) {
		if (!qp[j].contains(curr_s)) {
		    inter = false;
		    break;
		}
	    }

	    if (inter)
		q.add(curr_s);
	}	    

	return q;
    }

    public String mdl() {
        int i, j;
        
        int s = new_sym;    /* Number of subelement symbols in the sequences. */
        int m = 6;          /* Number of metacharacters: "( ) * | + ?". */
        
        /* 
        ** Assume that each sequence can be generated from 1 DTD, without
        ** the need to combine two or more DTDs in Sg. This assumption is made
        ** based on the behaviour of our case, i.e. every node of the same tag
        ** name will have similar set of children. 
        */
        int[] cost = new int[sf.size()];
        
        for (i = 0; i < sf.size(); i++) {
            boolean valid = true;
            
            for (j = 0; j < sequence.size(); j++) {
                if (((String) sequence.get(j)).matches(decodeSymbols(
		     (String) sf.get(i))) == false)
                    valid = false;
            }
        
            if (valid == true) {
                cost[i] = (int) Math.ceil(((String) sf.get(i)).length() * 
                    (Math.log(s + m) / Math.log(2)));

                String encode;          
                for (j = 0; j < sequence.size(); j++) {
                    encode = seq((String) sf.get(i), (String) sequence.get(j));
//                  System.out.println(encode);
            
                    cost[i] += encode.length();
                }
            }
            else 
                cost[i] = -1;
        }
    
        int min = findMinCost(cost);
    
        if (min != -1)    
            return (String) sf.get(min);
        else
            return "";
    }
    
    private String seq(String d, String s) {
        int i, j;
        String d1;
        
//      System.out.println("** " + d + " - " + s);

        int dlen = d.length();
        int slen = s.length();
    
        if (d.compareTo(s) == 0)
            return "";
        else if (noMetaChars(d) == true) {
            String[][] subseq = new String[dlen][2];
            int end = slen;
            
            for (i = dlen - 1; i >= 0; i--) {
                d1 = decodeSymbols(d.substring(0, i));
                
                for (j = end; j >= 0; j--) {
                    if (s.substring(0, j).matches(d1) == true) {
                        if (java.lang.Character.isUpperCase(d.charAt(i))) 
			    subseq[i][0] = (String) symtable.get(d.charAt(i) -
				init_char - 6);
			else {
			    if ((d.charAt(i) - init_char) >= new_sym)
                                subseq[i][0] = (String) symtable.get(
				    d.charAt(i) - init_char);
                            else
                            	subseq[i][0] = java.lang.Character.toString(
                                    d.charAt(i));
			}
                        subseq[i][1] = s.substring(j, end);
                    
                        end = j;
                        break;
                    }
                }
            }                            
        
            String ret = "";
            for (i = 0; i < dlen; i++) {
                ret = ret + seq(subseq[i][0], subseq[i][1]);
//              System.out.println(subseq[i][0] + ", " + subseq[i][1]);  
            }
                
            return ret;
        }
        else if (d.endsWith("*") == true) {
            if (s.compareTo("") == 0) 
                return "0";
                
            d = d.substring(0, dlen - 1);
            d1 = decodeSymbols(d);
            
            String rep_d = d1;
            for (i = 1; (s.matches(rep_d) == false); i++)
                rep_d = rep_d + d1;    
            
            int k = i;
            int no_bits = (int) Math.floor(Math.log(k) / Math.log(2)) + 1;
            
            String ret = "";
            for (i = 0; i < no_bits; i++)
                ret = ret + "1";
            ret = ret + "0" + encodeToBits(k, no_bits);

            String[] subseq = new String[k];        
            int end   = slen,
            d1len = d1.length();
        
            for (i = k - 1; i >= 0; i--) {
                d1 = rep_d.substring(0, i * d1len);
            
                for (j = end; j >= 0; j--) {
                    if (s.substring(0, j).matches(d1) == true) {
                        subseq[i] = s.substring(j, end);
                        end = j;
                        break;
                    }
                }
            }       

            if ((d.startsWith("(") == true) && (d.endsWith(")") == true))
                d = d.substring(1, d.length() - 1);
            
            for (i = 0; i < k; i++) {
                ret = ret + seq(d, subseq[i]);
//              System.out.println(subseq[i]);
            }
            
            return ret;
        }
        else if (d.indexOf("|") != -1) {            
            d = d.replace('|', ':');
            String[] ds = d.split(":");
            
            int m = ds.length;
            int no_bits = (int) Math.ceil(Math.log(m) / Math.log(2));
                        
            String ind;
            
            for (i = 0; i < m; i++) {
                d1 = decodeSymbols(ds[i]);
       
                if (s.matches(d1) == true) {
                    ind = encodeToBits(i, no_bits);
                    
                    return (ind + seq(ds[i], s));
                }
            }
    	}
    
        return "";
    }
    
    private String encodeToBits(int num, int no_bits) {
        String code = "";
        int bit, power;
        
        for (int i = (no_bits - 1); i >= 0; i--) {
            power = (int) Math.pow(2, i);
            bit = (int) (num / power);
            code = code + Integer.toString(bit);
            num -= (bit * power);
        }
        
        return code;
    }
    
    private boolean noMetaChars(String d) {
        if ((d.indexOf("|") == -1) && (d.indexOf("*") == -1) &&
            (d.indexOf("+") == -1) && (d.indexOf("?") == -1) &&
            (d.indexOf("(") == -1) && (d.indexOf(")") == -1))
            return true;
        
        return false;
    }
    
    private String decodeSymbols(String d) {
        int i, loc;
        boolean clean;
        
        do {
            clean = true;
            for (int j = new_sym; j < symtable.size(); j++) {
		if (j < 26)
		    loc = j;
		else
		    loc = j + 6;
 
                if (d.indexOf((char) (init_char + loc)) != -1) {
                    clean = false;
                    d = d.replaceAll(java.lang.Character.toString(
                        (char) (init_char + loc)), (String) symtable.get(j));
                    break;
                }                   
            }               
        } while (clean == false);    
        
        return d;   
    }
        
    private int findMinCost(int[] cost) {
        int i, minIndex;
        
        for (i = 0; (i < cost.length) && (cost[i] == -1); i++) 
            ;
        
        if (i < cost.length) {
            minIndex = i;
        
            for (i = minIndex + 1; i < cost.length; i++) {
                if (cost[i] != -1) {
                    if (cost[i] < cost[minIndex])
                        minIndex = i;
                    else if (cost[i] == cost[minIndex]) {
                        if (((String) sf.get(i)).length() < 
			    ((String) sf.get(minIndex)).length())
                            minIndex = i;
                    }
                }
            }
        }
        else
            minIndex = -1;
        
        return minIndex;
    }

    public String getImportantPart() {
        String d, part = "";
    
        if (ROOT == true)
            part = dtd;
        else {
            char imp = getSymbol(elemName, null);
    
            for (int i = 0; i < dtd.length(); i++) {
                d = decodeSymbols(Character.toString(dtd.charAt(i)));
    
                if (d.indexOf(imp) != -1)
                part = part + dtd.charAt(i);
            }
        }

        part = decodeSymbols(part);
    
        part = discoverSeqPattern(part, 2);
        part = decodeSymbols(part);

        return part;
    }

    public static void main(String[] argv) {
	ArrayList seq = new ArrayList();
	ArrayList sym = new ArrayList();
/*
	seq.add("b");
	seq.add("c");
	seq.add("ab");
	seq.add("ac");
	seq.add("df");
	seq.add("dg");
	seq.add("ef");
	seq.add("eg");

	sym.add("a");
	sym.add("b");
	sym.add("c");
	sym.add("d");
	sym.add("e");
	sym.add("f");
	sym.add("g");
	sym.add("h");
*/

	seq.add("ab");
	seq.add("abab");
	seq.add("ac");
	seq.add("ad");
	seq.add("bc");
	seq.add("bd");
//	seq.add("bbd");
//	seq.add("bbbbe");

	sym.add("a");
	sym.add("b");
	sym.add("c");
	sym.add("d");
	sym.add("e");

	XTRACT xt = new MatchDTD(seq, sym);
	System.out.println("\"" + xt.dtd + "\"");
    } 
}

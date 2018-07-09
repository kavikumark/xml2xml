package com.le.xslt.util;

import java.util.StringTokenizer;

public class DateFormatIndexes {
public static void main(String[] args) {
	String dt = "2013-10-22";
	String f1 = "yyyy-mm-dd";
	String f2 = "dd/mm/yyyy";
	int d1 = getIndex(f1,"yyyy",true);
	int d2 = getIndex(f1,"yyyy",false);
	String formattedString = getFormattedString(f1,f2,"@date");
	System.out.println(d1);
	System.out.println(d2);
	String dd = dt.substring(d1, d2);
	System.out.println(dd);
	
	
//	"
//	  concat(substring(@date, 9, 2), 
//	         '/', 
//	         substring(@date, 6, 2), +
//	         '/', 
//	         substring(@date, 1, 4))"	
	
}

private static String getFormattedString(String f1, String f2, String src) {
	StringTokenizer newTokens = new StringTokenizer(f2, "-/:,",true);
	StringBuffer result = new StringBuffer("concat(");
	while(newTokens.hasMoreTokens()) {
		String field = newTokens.nextToken();
		int fieldBegin = getIndex(f1, field, true);
		int fieldEnd = getIndex(f1, field, false);
		result.append("substring("+src+","+fieldBegin+","+fieldEnd+")");
		if(newTokens.hasMoreTokens()) {
			result.append(",'"+newTokens.nextToken()+"',");
		}
	}
	result.append(")");
		System.out.println(result.toString());
		return result.toString();
}

private static int getIndex(String f1, String field, boolean b) {
	if(b) return f1.indexOf(field);
	else return f1.indexOf(field) + field.length();
}
}

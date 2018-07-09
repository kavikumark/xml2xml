package com.le.xslt.gen;
public class SequenceTable {
    public String xml;
    public int[][] html;
    public int curr_html = 0;
    
    public SequenceTable(String xml, int html_len) {
        this.xml = xml;
        
        html = new int[html_len][2];
    }
    
    public void addRule(int table, int index) {
        html[curr_html][0] = table;
        html[curr_html][1] = index;
        curr_html++;
    }
    
    public void print_seq() {
        System.out.println(xml + " : ");
        
        for (int i = 0; i < curr_html; i++) {           
            switch (html[i][0]) {
                case 0 :
                    System.out.print("    TEXT_TABLE : ");
                    break;
                case 1 :
                    System.out.print("    ATTR_TABLE : ");
                    break;
                case 2 :
                    System.out.print("    STRUCT_TABLE : ");
                    break;
                default :
                    break;
            }   
            System.out.println(html[i][1]);
        }
    }
}
    
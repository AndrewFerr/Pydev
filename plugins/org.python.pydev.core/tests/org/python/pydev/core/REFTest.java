package org.python.pydev.core;

import java.io.File;
import java.util.HashSet;

import junit.framework.TestCase;

public class REFTest extends TestCase {

    
    public void testLog() {
        //These are the values we get from python.
        double[] expected = new double[]{
                0.0,
                1.70951129135,
                2.70951129135,
                3.4190225827,
                3.96936229592,
                4.4190225827,
                4.79920493809,
                5.12853387405,
                5.4190225827,
                5.67887358727,
                5.91393741372,
                6.12853387405,
                6.32594348113,
                6.50871622944,
                6.67887358727,
                6.83804516541,
                6.9875638801,
                7.12853387405,
                7.26188004907,
        };
        
        for(int i=1;i<20;i++){
//            System.out.println(i+": "+(i+Math.round(REF.log(i, 1.4))));
            assertTrue(""+expected[i-1]+" !="+ REF.log(i, 1.5)+"for log "+i, 
                    Math.abs(expected[i-1] - REF.log(i, 1.5)) < 0.01);
        }

    }
    
    public void testGetTempFile() throws Exception {
        REF.clearTempFilesAt(new File("."), "ref_test_case");
        File parentDir = new File(".");
        try {
            assertEquals("ref_test_case0", writeAt(parentDir).getName());
            assertEquals("ref_test_case1", writeAt(parentDir).getName());
            assertEquals("ref_test_case2", writeAt(parentDir).getName());
        } finally{
            try {
                HashSet<String> expected = new HashSet<String>();
                expected.add("ref_test_case0");
                expected.add("ref_test_case1");
                expected.add("ref_test_case2");
                assertEquals(expected, REF.getFilesStartingWith(parentDir, "ref_test_case"));
                
                assertEquals("ref_test_case3", REF.getTempFileAt(parentDir, "ref_test_case").getName());
                assertEquals("ref_test_case4", REF.getTempFileAt(parentDir, "ref_test_case").getName());
                REF.clearTempFilesAt(parentDir, "ref_test_case");
                assertEquals("ref_test_case0", REF.getTempFileAt(parentDir, "ref_test_case").getName());
                
            } finally {
                REF.clearTempFilesAt(parentDir, "ref_test_case");
            }
        }
        
    }

    public File writeAt(File parentDir) {
        File tempFileAt = REF.getTempFileAt(parentDir, "ref_test_case");
        REF.writeStrToFile("foo", tempFileAt);
        return tempFileAt;
    }
}

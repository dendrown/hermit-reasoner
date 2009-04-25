package org.semanticweb.HermiT.reasoner;

import java.util.*;

import org.semanticweb.HermiT.*;
import org.semanticweb.HermiT.model.*;
import org.semanticweb.HermiT.datatypes.*;
import org.semanticweb.HermiT.datatypes.rdftext.*;

public class RDFTextTest extends AbstractReasonerTest {
    protected static final String RDF_TEXT=Prefixes.s_semanticWebPrefixes.get("rdf")+"text";
    protected static final String XSD_NS=Prefixes.s_semanticWebPrefixes.get("xsd");

    public RDFTextTest(String name) {
        super(name);
    }
    public void testInvalidStringLiterals() throws Exception {
        assertEquals("blah",DatatypeRegistry.parseLiteral("blah@",RDF_TEXT));
        assertEquals(new RDFTextDataValue("blah","en"),DatatypeRegistry.parseLiteral("blah@en",RDF_TEXT));
        assertEquals("blah blah",DatatypeRegistry.parseLiteral("blah blah",XSD_NS+"token"));
        try {
            DatatypeRegistry.parseLiteral("abc@123",RDF_TEXT);
            fail();
        }
        catch (MalformedLiteralException expected) {
        }
        try {
            DatatypeRegistry.parseLiteral("\u0002blah@en",RDF_TEXT);
            fail();
        }
        catch (MalformedLiteralException expected) {
        }
        try {
            DatatypeRegistry.parseLiteral(" blah@en",XSD_NS+"token");
            fail();
        }
        catch (MalformedLiteralException expected) {
        }
        try {
            DatatypeRegistry.parseLiteral("blah  blah@en",XSD_NS+"token");
            fail();
        }
        catch (MalformedLiteralException expected) {
        }
    }
    public void testLength() throws Exception {
        assertDRSatisfiable(true,
            DR("xsd:string","xsd:length",INT("2")),
            OO(STR("ab"))
        );
        assertDRSatisfiable(false,
            DR("xsd:string","xsd:length",INT("3")),
            OO(STR("ab"))
        );
        assertDRSatisfiable(true,
            DR("xsd:string","xsd:minLength",INT("2"),"xsd:maxLength",INT("6")),
            NOT(DR("xsd:string","xsd:minLength",INT("3"),"xsd:maxLength",INT("5"))),
            OO(STR("ab"))
        );
        assertDRSatisfiable(true,
            DR("xsd:string","xsd:minLength",INT("2"),"xsd:maxLength",INT("6")),
            NOT(DR("xsd:string","xsd:minLength",INT("3"),"xsd:maxLength",INT("5"))),
            OO(STR("abcdef"))
        );
        assertDRSatisfiable(false,
            DR("xsd:string","xsd:minLength",INT("2"),"xsd:maxLength",INT("6")),
            NOT(DR("xsd:string","xsd:minLength",INT("3"),"xsd:maxLength",INT("5"))),
            OO(STR("abcde"))
        );
    }
    public void testSize() throws Exception {
        assertDRSatisfiable(true,
            DR("xsd:string","xsd:length",INT("0"))
        );
        assertDRSatisfiable(false,2,
            DR("xsd:string","xsd:length",INT("0"))
        );
        assertDRSatisfiable(false,
            DR("xsd:string","xsd:length",INT("0")),
            NOT(OO(STR("")))
        );
    }
    public void testIntersection() throws Exception {
        assertDRSatisfiable(true,
            DR("xsd:string","xsd:minLength",INT("0")),
            NOT(DR("xsd:string","xsd:minLength",INT("1")))
        );
        assertDRSatisfiable(false,2,
            DR("xsd:string","xsd:minLength",INT("0")),
            NOT(DR("xsd:string","xsd:minLength",INT("1")))
        );
    }
    public void testExplicitSize() throws Exception {
        RDFTextLengthInterval imax2=interval(0,1);
        assertEquals(10000000,imax2.subtractSizeFrom(10000000+1+RDFTextLengthInterval.CHARACTER_COUNT));
        RDFTextLengthInterval imin1max2=interval(1,1);
        assertEquals(10000000,imin1max2.subtractSizeFrom(10000000+RDFTextLengthInterval.CHARACTER_COUNT));
    }
    public void testEnumerate() throws Exception {
        RDFTextLengthInterval imax1=interval(0,0);
        List<Object> values=new ArrayList<Object>();
        imax1.enumerateValues(values);
        List<Object> control=new ArrayList<Object>();
        control.add("");
        assertContainsAll(values,control.toArray());
    }
    protected static RDFTextLengthInterval interval(int minLength,int maxLength) {
        return new RDFTextLengthInterval(RDFTextLengthInterval.LanguageTagMode .ABSENT,minLength,maxLength);
    }
    public void testPattern1() throws Exception {
        assertDRSatisfiable(true,
            DR("xsd:string","xsd:pattern",STR("ab(c+)")),
            OO(STR("abc"),STR("abbb"))
        );
        assertDRSatisfiable(false,2,
            DR("xsd:string","xsd:pattern",STR("ab(c+)")),
            OO(STR("abc"),STR("abbb"))
        );
    }
    public void testPattern2() throws Exception {
        assertDRSatisfiable(true,3,
            DR("xsd:string","xsd:pattern",STR("ab(c|d|e)"))
        );
        assertDRSatisfiable(false,4,
            DR("xsd:string","xsd:pattern",STR("ab(c|d|e)"))
        );
    }
    public void testPattern3() throws Exception {
        assertDRSatisfiable(false,
            DR("xsd:string","xsd:pattern",STR("ab(c|d|e)")),
            NOT(OO(STR("abc"),STR("abd"),STR("abe")))
        );
    }
    public void testPatternAndLength1() throws Exception {
        assertDRSatisfiable(true,
            DR("xsd:string","xsd:pattern",STR("ab(c+)"),"xsd:length",INT("5"))
        );
        assertDRSatisfiable(false,
            DR("xsd:string","xsd:pattern",STR("ab(c+)"),"xsd:minLength",INT("4"),"xsd:maxLength",INT("5")),
            NOT(OO(STR("abcc"),STR("abccc")))
        );
        assertDRSatisfiable(true,
            DR("xsd:string","xsd:pattern",STR("ab(c+)"),"xsd:minLength",INT("4")),
            NOT(OO(STR("abcc"),STR("abccc")))
        );
    }
    public void testPatternAndLength2() throws Exception {
        ValueSpaceSubset subset=subset("xsd:string","xsd:pattern","ab(c+)","xsd:minLength",5);
        assertTrue(subset.hasCardinalityAtLeast(5000));
        try {
            subset.enumerateDataValues(new ArrayList<Object>());
            fail();
        }
        catch (Exception expected) {
        }
        assertFalse(subset.containsDataValue("ab"));
        assertTrue(subset.containsDataValue("abccccccccccc"));
    }
    public void testPatternAndLength3() throws Exception {
        ValueSpaceSubset subset=subset("xsd:string","xsd:pattern","ab(c+)","xsd:minLength",5,"xsd:maxLength",10);
        assertTrue(subset.hasCardinalityAtLeast(6));
        assertFalse(subset.hasCardinalityAtLeast(7));
        Set<Object> values=new HashSet<Object>();
        subset.enumerateDataValues(values);
        assertContainsAll(values,"abccc","abcccc","abccccc","abcccccc","abccccccc","abcccccccc");
    }
    public void testPatternComplement1() throws Exception {
        assertDRSatisfiable(true,3,
            DR("xsd:string","xsd:pattern",STR("ab(c*)")),
            NOT(DR("xsd:string","xsd:minLength",INT("5")))
        );
        assertDRSatisfiable(false,4,
            DR("xsd:string","xsd:pattern",STR("ab(c*)")),
            NOT(DR("xsd:string","xsd:minLength",INT("5")))
        );
        assertDRSatisfiable(false,
            DR("xsd:string","xsd:pattern",STR("ab(c*)")),
            NOT(DR("xsd:string","xsd:minLength",INT("5"))),
            NOT(OO(STR("ab"),STR("abc"),STR("abcc")))
        );
    }
    public void testComplement2() throws Exception {
        ValueSpaceSubset main=subset("xsd:string","xsd:pattern","ab(c*)");
        DatatypeRestriction restriction=restriction("xsd:string","xsd:minLength",5);
        ValueSpaceSubset intersection=DatatypeRegistry.conjoinWithDRNegation(main,restriction);
        assertTrue(intersection.hasCardinalityAtLeast(3));
        assertFalse(intersection.hasCardinalityAtLeast(4));
        Set<Object> values=new HashSet<Object>();
        intersection.enumerateDataValues(values);
        assertContainsAll(values,"ab","abc","abcc");
    }
    public void testComplement3() throws Exception {
        ValueSpaceSubset main=subset("rdf:text");
        DatatypeRestriction restriction=restriction("xsd:string","xsd:minLength",5);
        ValueSpaceSubset intersection=DatatypeRegistry.conjoinWithDRNegation(main,restriction);
        assertFalse(intersection.containsDataValue("abcde"));
        assertTrue(intersection.containsDataValue("abcd"));
        assertTrue(intersection.containsDataValue(new RDFTextDataValue("abcdefgh","en")));
        assertFalse(intersection.containsDataValue(new RDFTextDataValue("abcdefgh","123")));
    }
    public void testComplement4() throws Exception {
        ValueSpaceSubset main=subset("rdf:text","xsd:pattern","a+");
        DatatypeRestriction restriction=restriction("xsd:string","xsd:minLength",5);
        ValueSpaceSubset intersection=DatatypeRegistry.conjoinWithDRNegation(main,restriction);
        assertFalse(intersection.containsDataValue("aaaaa"));
        assertTrue(intersection.containsDataValue("aaaa"));
        assertTrue(intersection.containsDataValue(new RDFTextDataValue("aaaaaaaa","en")));
        assertFalse(intersection.containsDataValue(new RDFTextDataValue("aaaaaaaa","123")));
    }
    public void testLangRange1() throws Exception {
        ValueSpaceSubset main=subset("rdf:text","rdf:langRange","en");
        assertFalse(main.containsDataValue("abc"));
        assertFalse(main.containsDataValue(new RDFTextDataValue("abc","de")));
        assertTrue(main.containsDataValue(new RDFTextDataValue("abc","en")));
        assertFalse(main.containsDataValue(new RDFTextDataValue("abc","enn")));
        assertTrue(main.containsDataValue(new RDFTextDataValue("abc","en-us")));
    }
    public void testLangRange2() throws Exception {
        ValueSpaceSubset main=subset("rdf:text","rdf:langRange","*");
        assertFalse(main.containsDataValue("abc"));
        assertTrue(main.containsDataValue(new RDFTextDataValue("abc","de")));
        assertTrue(main.containsDataValue(new RDFTextDataValue("abc","en")));
        assertTrue(main.containsDataValue(new RDFTextDataValue("abc","enn")));
        assertTrue(main.containsDataValue(new RDFTextDataValue("abc","en-us")));
    }
    protected static DatatypeRestriction restriction(String datatypeURI,Object... arguments) {
        String[] facetURIs=new String[arguments.length/2];
        Object[] facetValues=new Object[arguments.length/2];
        for (int index=0;index<arguments.length;index+=2) {
            facetURIs[index/2]=Prefixes.STANDARD_PREFIXES.expandAbbreviatedURI((String)arguments[index]);
            facetValues[index/2]=arguments[index+1];
        }
        return DatatypeRestriction.create(Prefixes.STANDARD_PREFIXES.expandAbbreviatedURI(datatypeURI),facetURIs,facetValues);
    }
    protected static ValueSpaceSubset subset(String datatypeURI,Object... arguments) {
        DatatypeRestriction restriction=restriction(datatypeURI,arguments);
        return DatatypeRegistry.createValueSpaceSubset(restriction);
    }
}

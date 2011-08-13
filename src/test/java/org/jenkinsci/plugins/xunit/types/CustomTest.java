package org.jenkinsci.plugins.xunit.types;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Singleton;
import com.thalesgroup.dtkit.util.converter.ConversionService;
import com.thalesgroup.dtkit.util.validator.ValidationError;
import com.thalesgroup.dtkit.util.validator.ValidationService;
import com.thalesgroup.hudson.plugins.xunit.types.CustomInputMetric;
import org.custommonkey.xmlunit.Diff;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.*;

/**
 * @author Gregory Boissinot
 */
public class CustomTest {

    private static Injector injector;

    @BeforeClass
    public static void initInjector() {
        injector = Guice.createInjector(new AbstractModule() {
            @Override
            protected void configure() {
                //Optional binding, provided by default in Guice)
                bind(ValidationService.class).in(Singleton.class);
                bind(ConversionService.class).in(Singleton.class);
            }
        });
    }

    private String readXmlAsString(File input)
            throws IOException {
        String xmlString = "";

        if (input == null) {
            throw new IOException("The input stream object is null.");
        }

        FileInputStream fileInputStream = new FileInputStream(input);
        InputStreamReader inputStreamReader = new InputStreamReader(fileInputStream);
        BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
        String line = bufferedReader.readLine();
        while (line != null) {
            xmlString += line + "\n";
            line = bufferedReader.readLine();
        }
        fileInputStream.close();
        fileInputStream.close();
        bufferedReader.close();

        return xmlString;
    }

    private void convertAndValidate(String inputXMLPath, String inputXSLPath, String expectedResultPath) throws Exception {

        CustomInputMetric customInputMetric = injector.getInstance(CustomInputMetric.class);
        customInputMetric.setCustomXSLFile(new File(this.getClass().getResource(inputXSLPath).toURI()));

        File outputXMLFile = File.createTempFile("result", "xml");
        File inputXMLFile = new File(this.getClass().getResource(inputXMLPath).toURI());

        //The input file must be valid
        boolean inputResult = customInputMetric.validateInputFile(inputXMLFile);
        for (ValidationError validatorError : customInputMetric.getInputValidationErrors()) {
            System.out.println("[ERROR] " + validatorError.toString());
        }
        Assert.assertTrue(inputResult);

        customInputMetric.convert(inputXMLFile, outputXMLFile);
        Diff myDiff = new Diff(readXmlAsString(new File(this.getClass().getResource(expectedResultPath).toURI())), readXmlAsString(outputXMLFile));
        Assert.assertTrue("XSL transformation did not work" + myDiff, myDiff.similar());

        //The generated output file must be valid
        boolean outputResult = customInputMetric.validateOutputFile(outputXMLFile);
        for (ValidationError validatorError : customInputMetric.getOutputValidationErrors()) {
            System.out.println(validatorError);
        }
        Assert.assertTrue(outputResult);

        outputXMLFile.deleteOnExit();
    }

    @Test
    public void testTestcase1() throws Exception {
        convertAndValidate("customTool/testcase1/input.xml", "customTool/testcase1/input.xsl", "customTool/testcase1/result.xml");
    }

}

package com.thalesgroup.dtkit.junit;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Singleton;
import com.thalesgroup.dtkit.metrics.model.InputMetricXSL;
import com.thalesgroup.dtkit.util.converter.ConversionService;
import com.thalesgroup.dtkit.util.validator.ValidationError;
import com.thalesgroup.dtkit.util.validator.ValidationService;
import org.custommonkey.xmlunit.Diff;
import org.custommonkey.xmlunit.XMLUnit;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;

import java.io.File;

public class AbstractTest {

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

    @Before
    public void setUp() {
        XMLUnit.setIgnoreWhitespace(true);
        XMLUnit.setNormalizeWhitespace(true);
        XMLUnit.setIgnoreComments(true);
    }


    public void convertAndValidate(Class<? extends InputMetricXSL> InputMetricXSLClassType, String inputXMLPath, String expectedResultPath) throws Exception {

        InputMetricXSL inputMetricXSL = injector.getInstance(InputMetricXSLClassType);

        File outputXMLFile = File.createTempFile("result", "xml");
        File inputXMLFile = new File(this.getClass().getResource(inputXMLPath).toURI());

        //The input file must be valid
        boolean inputResult = inputMetricXSL.validateInputFile(inputXMLFile);
        for (ValidationError validatorError : inputMetricXSL.getInputValidationErrors()) {
            System.out.println("[ERROR] " + validatorError.toString());
        }
        Assert.assertTrue(inputResult);

        inputMetricXSL.convert(inputXMLFile, outputXMLFile);
        Diff myDiff = new Diff(XSLUtil.readXmlAsString(new File(this.getClass().getResource(expectedResultPath).toURI())), XSLUtil.readXmlAsString(outputXMLFile));
        Assert.assertTrue("XSL transformation did not work" + myDiff, myDiff.similar());

        //The generated output file must be valid
        boolean outputResult = inputMetricXSL.validateOutputFile(outputXMLFile);
        for (ValidationError validatorError : inputMetricXSL.getOutputValidationErrors()) {
            System.out.println(validatorError);
        }
        Assert.assertTrue(outputResult);

        outputXMLFile.deleteOnExit();
    }

}
